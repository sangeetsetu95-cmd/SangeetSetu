package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Role
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : IUserRepository {
    
    private val COLLECTION_USERS = "users"

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val document = db.collection(COLLECTION_USERS).document(uid).get().await()
            val user = document.toObject(User::class.java)
            
            // Automatic Migration Check for single user
            if (user != null && user.userType == "Artist" && user.categoryId.isEmpty()) {
                Log.d("UserRepository", "On-the-fly migration for artist: $uid")
                
                val categoriesSnapshot = db.collection("categories").get().await()
                val categoriesList = categoriesSnapshot.toObjects(com.sangeetsetu.app.model.Category::class.java)
                
                val targetCategory = categoriesList.find { it.name.equals(user.category, ignoreCase = true) }
                    ?: categoriesList.find { it.name.contains(user.category.replace(" Player", ""), ignoreCase = true) }

                if (targetCategory != null) {
                    val updates = mutableMapOf<String, Any>(
                        "categoryId" to targetCategory.id,
                        "category" to targetCategory.name
                    )
                    db.collection(COLLECTION_USERS).document(uid).update(updates)
                }
            }

            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user profile for $uid", e)
            Result.failure(e)
        }
    }

    override suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            FirestoreAudit.verifiedWrite(
                collectionName = COLLECTION_USERS,
                documentId = user.uid,
                expectedData = mapOf("uid" to user.uid, "email" to user.email)
            ) {
                db.collection(COLLECTION_USERS).document(user.uid).set(user).await()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error saving user profile for ${user.uid}", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProfileFields(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            FirestoreAudit.verifiedWrite(
                collectionName = COLLECTION_USERS,
                documentId = uid,
                expectedData = updates
            ) {
                db.collection(COLLECTION_USERS).document(uid).update(updates).await()
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating profile fields for $uid", e)
            Result.failure(e)
        }
    }

    override fun listenToUserProfile(uid: String, onUpdate: (User?) -> Unit): ListenerRegistration? {
        return try {
            db.collection(COLLECTION_USERS).document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("UserRepository", "Listen error for $uid", error)
                        return@addSnapshotListener
                    }
                    val user = snapshot?.toObject(User::class.java)
                    onUpdate(user)
                }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error setting up profile listener for $uid", e)
            null
        }
    }

    override fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
        val subscription = db.collection(COLLECTION_USERS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("UserRepository", "UID=${getCurrentUserId()} Collection=users Operation=Listen FirestoreCode=${error.code}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                
                val users = try {
                    snapshot?.toObjects(User::class.java) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("UserRepository", "Error parsing users list", e)
                    emptyList()
                }
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getRole(roleName: String): Result<Role?> {
        return try {
            val document = db.collection("roles").document(roleName).get().await()
            val role = document.toObject(Role::class.java)
            Result.success(role)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching role $roleName", e)
            Result.failure(e)
        }
    }

    override suspend fun getNextArtistId(): String {
        return try {
            db.runTransaction { transaction ->
                val configRef = db.collection("metadata").document("artist_config")
                val snapshot = transaction.get(configRef)
                
                val lastId = if (snapshot.exists()) {
                    snapshot.getLong("lastId") ?: 0L
                } else {
                    0L
                }
                
                val nextId = lastId + 1
                transaction.set(configRef, mapOf("lastId" to nextId))
                
                "SSA${nextId.toString().padStart(6, '0')}"
            }.await()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error generating next artist ID", e)
            "SSA${System.currentTimeMillis() % 1000000}" // Fallback
        }
    }

    override suspend fun updateArtistStatusTransaction(uid: String, statusUpdates: Map<String, Any>): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val userRef = db.collection(COLLECTION_USERS).document(uid)
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) throw Exception("User not found")
                
                val user = snapshot.toObject(User::class.java)!!
                
                // Merge updates
                val updatedMap = snapshot.data?.toMutableMap() ?: mutableMapOf()
                updatedMap.putAll(statusUpdates)
                
                val vStatus = updatedMap["verificationStatus"] as? String ?: user.verificationStatus
                val aStatus = updatedMap["approvalStatus"] as? String ?: user.approvalStatus
                val accStatus = updatedMap["accountStatus"] as? String ?: user.accountStatus
                
                val isApproved = aStatus == "APPROVED"
                val isVerified = isApproved || (vStatus == "VERIFIED")
                
                val finalUpdates = statusUpdates.toMutableMap()
                finalUpdates["isApproved"] = isApproved
                finalUpdates["isVerified"] = isVerified
                finalUpdates["verificationStatus"] = if (isApproved) "VERIFIED" else vStatus
                finalUpdates["status"] = accStatus 
                
                var currentArtistId = updatedMap["artistId"] as? String ?: user.artistId
                if ((isVerified || isApproved) && currentArtistId.isEmpty()) {
                    val configRef = db.collection("metadata").document("artist_config")
                    val configSnap = transaction.get(configRef)
                    val lastId = if (configSnap.exists()) configSnap.getLong("lastId") ?: 0L else 0L
                    val nextId = lastId + 1
                    transaction.set(configRef, mapOf("lastId" to nextId))
                    currentArtistId = "SSA${nextId.toString().padStart(6, '0')}"
                    finalUpdates["artistId"] = currentArtistId
                }
                
                transaction.update(userRef, finalUpdates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("UserRepository", "Transaction failed for $uid", e)
            Result.failure(e)
        }
    }

    override suspend fun runArtistStatusMigration(): Result<Int> {
        return try {
            val querySnapshot = db.collection(COLLECTION_USERS)
                .whereEqualTo("userType", "Artist")
                .get().await()
            
            var fixedCount = 0
            val batch = db.batch()
            
            for (doc in querySnapshot.documents) {
                val user = doc.toObject(User::class.java) ?: continue
                
                val isApproved = user.approvalStatus == "APPROVED"
                val isVerified = isApproved || (user.verificationStatus == "VERIFIED")
                val finalVStatus = if (isApproved) "VERIFIED" else user.verificationStatus

                if (user.isVerified != isVerified || 
                    user.isApproved != isApproved || 
                    user.verificationStatus != finalVStatus ||
                    (isVerified && user.artistId.isEmpty())) {
                    
                    val updates = mutableMapOf<String, Any>(
                        "isVerified" to isVerified,
                        "isApproved" to isApproved,
                        "verificationStatus" to finalVStatus,
                        "status" to user.accountStatus
                    )
                    
                    if (isVerified && user.artistId.isEmpty()) {
                        updates["artistId"] = "SSA_MIG_${System.currentTimeMillis().toString().takeLast(5)}"
                    }
                    
                    batch.update(doc.reference, updates)
                    fixedCount++
                }
            }
            
            if (fixedCount > 0) batch.commit().await()
            Result.success(fixedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun runCategoryMigration(): Result<Int> {
        return try {
            val categoriesSnapshot = db.collection("categories").get().await()
            val categoriesList = categoriesSnapshot.toObjects(com.sangeetsetu.app.model.Category::class.java)
            
            val querySnapshot = db.collection(COLLECTION_USERS)
                .whereEqualTo("userType", "Artist")
                .get().await()
            
            var fixedCount = 0
            val batch = db.batch()
            
            for (doc in querySnapshot.documents) {
                val user = doc.toObject(User::class.java) ?: continue
                val oldCategoryName = user.category
                val currentCategoryId = user.categoryId
                
                // 1. Find the correct category from DB
                val targetCategory = categoriesList.find { it.id == currentCategoryId }
                    ?: categoriesList.find { it.name.equals(oldCategoryName, ignoreCase = true) }
                    ?: categoriesList.find { it.name.contains(oldCategoryName.replace(" Player", ""), ignoreCase = true) }

                if (targetCategory != null) {
                    val updates = mutableMapOf<String, Any>()
                    
                    if (user.categoryId != targetCategory.id) {
                        updates["categoryId"] = targetCategory.id
                    }
                    
                    if (user.category != targetCategory.name) {
                        updates["category"] = targetCategory.name
                    }

                    if (updates.isNotEmpty()) {
                        batch.update(doc.reference, updates)
                        fixedCount++
                    }
                }
            }
            
            if (fixedCount > 0) batch.commit().await()
            Result.success(fixedCount)
        } catch (e: Exception) {
            Log.e("UserRepository", "Category migration failed", e)
            Result.failure(e)
        }
    }

    override fun logout() {
        try {
            auth.signOut()
            Log.d("UserRepository", "User logged out successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error during logout", e)
        }
    }
}
