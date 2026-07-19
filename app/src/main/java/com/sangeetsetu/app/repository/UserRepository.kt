package com.sangeetsetu.app.repository

import android.util.Log
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

/**
 * Repository for managing user data, profiles, and authentication state.
 * Handles Firestore operations for the 'users' collection with audit verification.
 */
object UserRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    private const val COLLECTION_USERS = "users"

    /**
     * Returns the current authenticated user's ID.
     */
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Fetches a user profile by ID from Firestore.
     */
    suspend fun getUser(uid: String): Result<User?> = getUserProfile(uid)

    suspend fun getUserProfile(uid: String): Result<User?> {
        return try {
            val document = db.collection(COLLECTION_USERS).document(uid).get().await()
            val user = document.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user profile for $uid", e)
            Result.failure(e)
        }
    }

    /**
     * Saves or updates a full user profile in Firestore with audit verification.
     */
    suspend fun saveUserProfile(user: User): Result<Unit> {
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

    /**
     * Updates specific fields of a user profile in Firestore with audit verification.
     */
    suspend fun updateProfileFields(uid: String, updates: Map<String, Any>): Result<Unit> {
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

    /**
     * Sets up a real-time listener for a specific user's profile.
     */
    fun listenToUserProfile(uid: String, onUpdate: (User?) -> Unit): ListenerRegistration? {
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

    /**
     * Generates a unique sequential Artist ID (e.g., SSA000001) using a Firestore transaction.
     */
    suspend fun getNextArtistId(): String {
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
            // Fallback to timestamp based if transaction fails
            "SSA${System.currentTimeMillis().toString().takeLast(6)}"
        }
    }

    /**
     * Synchronizes Boolean flags and Artist ID based on String statuses.
     * This ensures 'isVerified', 'isApproved', and 'artistId' are always consistent with 
     * 'verificationStatus' and 'approvalStatus'.
     */
    fun syncArtistStatus(user: User): User {
        val isVerified = user.verificationStatus == "VERIFIED"
        val isApproved = user.approvalStatus == "APPROVED"
        
        // If Approved, it must be Verified
        val finalVerificationStatus = if (isApproved) "VERIFIED" else user.verificationStatus
        val finalIsVerified = isApproved || isVerified

        return user.copy(
            isVerified = finalIsVerified,
            verificationStatus = finalVerificationStatus,
            isApproved = isApproved,
            status = user.accountStatus // Sync legacy status
        )
    }

    /**
     * Atomically updates artist status and ensures all related flags are synchronized.
     * Uses a Firestore transaction to handle Artist ID generation if needed.
     */
    suspend fun updateArtistStatusTransaction(uid: String, statusUpdates: Map<String, Any>): Result<Unit> {
        return try {
            db.runTransaction { transaction ->
                val userRef = db.collection(COLLECTION_USERS).document(uid)
                val snapshot = transaction.get(userRef)
                if (!snapshot.exists()) throw Exception("User not found")
                
                val user = snapshot.toObject(User::class.java)!!
                
                // Merge updates into current user object to calculate sync
                val updatedMap = snapshot.data?.toMutableMap() ?: mutableMapOf()
                updatedMap.putAll(statusUpdates)
                
                val verificationStatus = updatedMap["verificationStatus"] as? String ?: user.verificationStatus
                val approvalStatus = updatedMap["approvalStatus"] as? String ?: user.approvalStatus
                val accountStatus = updatedMap["accountStatus"] as? String ?: user.accountStatus
                
                val isApproved = approvalStatus == "APPROVED"
                val isVerified = isApproved || (verificationStatus == "VERIFIED")
                
                val finalUpdates = statusUpdates.toMutableMap()
                finalUpdates["isApproved"] = isApproved
                finalUpdates["isVerified"] = isVerified
                finalUpdates["verificationStatus"] = if (isApproved) "VERIFIED" else verificationStatus
                finalUpdates["status"] = accountStatus // Legacy sync
                
                // Artist ID Logic: Mandatory for Verified or Approved
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

    /**
     * Migration script to fix inconsistent artist statuses across the entire database.
     */
    suspend fun runArtistStatusMigration(): Result<Int> {
        return try {
            val querySnapshot = db.collection(COLLECTION_USERS)
                .whereEqualTo("userType", "Artist")
                .get().await()
            
            var fixedCount = 0
            val batch = db.batch()
            
            for (doc in querySnapshot.documents) {
                val user = doc.toObject(User::class.java) ?: continue
                val syncedUser = syncArtistStatus(user)
                
                // Check if any critical field changed
                if (user.isVerified != syncedUser.isVerified || 
                    user.isApproved != syncedUser.isApproved ||
                    user.verificationStatus != syncedUser.verificationStatus ||
                    (syncedUser.isVerified && syncedUser.artistId.isEmpty())) {
                    
                    val updates = mutableMapOf<String, Any>(
                        "isVerified" to syncedUser.isVerified,
                        "isApproved" to syncedUser.isApproved,
                        "verificationStatus" to syncedUser.verificationStatus,
                        "status" to syncedUser.accountStatus
                    )
                    
                    // Note: We don't generate Artist IDs in a batch script easily if we want them sequential.
                    // For migration, we'll assign timestamp-based ones if missing and verified.
                    if (syncedUser.isVerified && user.artistId.isEmpty()) {
                        updates["artistId"] = "SSA_MIG_${System.currentTimeMillis().toString().takeLast(5)}"
                    }
                    
                    batch.update(doc.reference, updates)
                    fixedCount++
                }
            }
            
            if (fixedCount > 0) batch.commit().await()
            Result.success(fixedCount)
        } catch (e: Exception) {
            Log.e("UserRepository", "Migration failed", e)
            Result.failure(e)
        }
    }

    /**
     * Provides a real-time Flow of all users in the system.
     * Used primarily by Admin screens.
     */
    fun getAllUsersFlow(): Flow<List<User>> = callbackFlow {
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

    /**
     * Signs out the current user and clears authentication state.
     */
    fun logout() {
        try {
            auth.signOut()
            Log.d("UserRepository", "User logged out successfully")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error during logout", e)
        }
    }
}
