package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.ISystemLogRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.AdminLog
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemLogRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: IUserRepository
) : ISystemLogRepository {

    override fun getRecentLogsFlow(limit: Int): Flow<List<AdminLog>> = callbackFlow {
        val subscription = db.collection("admin_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("SystemLogRepo", "Error listening to logs", error)
                    return@addSnapshotListener
                }
                val logs = snapshot?.toObjects(AdminLog::class.java) ?: emptyList()
                trySend(logs)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun logAction(action: String): Result<Unit> {
        val uid = userRepository.getCurrentUserId() ?: "unknown"
        val id = db.collection("admin_logs").document().id
        val log = AdminLog(
            id = id,
            adminId = uid,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        
        return FirestoreAudit.verifiedWrite("admin_logs", id) {
            db.collection("admin_logs").document(id).set(log).await()
        }
    }
}
