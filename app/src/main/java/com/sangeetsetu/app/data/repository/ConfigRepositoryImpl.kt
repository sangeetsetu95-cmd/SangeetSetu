package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IConfigRepository
import com.sangeetsetu.app.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : IConfigRepository {

    override fun getItems(collection: String): Flow<List<ConfigItem>> = callbackFlow {
        val subscription = db.collection(collection)
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConfigRepository", "Error listening to items: $collection", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { 
                    it.toObject(ConfigItem::class.java)?.copy(id = it.id) 
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveItem(collection: String, item: ConfigItem): Result<Unit> {
        return try {
            val docRef = if (item.id.isEmpty()) db.collection(collection).document() else db.collection(collection).document(item.id)
            docRef.set(item.copy(id = docRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteItem(collection: String, id: String): Result<Unit> {
        return try {
            db.collection(collection).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>> = callbackFlow {
        val subscription = db.collection("subscription_plans")
            .orderBy("order", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConfigRepository", "Error listening to subscription plans", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { 
                    it.toObject(SubscriptionPlan::class.java)?.copy(id = it.id) 
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    override fun getHomeSections(): Flow<List<HomeSection>> = callbackFlow {
        val subscription = db.collection("home_sections")
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConfigRepository", "Error listening to home sections", error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull { 
                    it.toObject(HomeSection::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { subscription.remove() }
    }

    override fun getAppInfo(): Flow<AppInformation> = callbackFlow {
        val subscription = db.collection("settings").document("app_info")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ConfigRepository", "Error listening to app info", error)
                    return@addSnapshotListener
                }
                val info = snapshot?.toObject(AppInformation::class.java) ?: AppInformation()
                trySend(info)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateAppInfo(info: AppInformation): Result<Unit> {
        return try {
            db.collection("settings").document("app_info").set(info).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
