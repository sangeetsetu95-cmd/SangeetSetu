package com.sangeetsetu.app.repository

import com.sangeetsetu.app.model.AdminLog
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object SystemLogRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    fun getRecentLogsFlow(limit: Int = 20): Flow<List<AdminLog>> = callbackFlow {
        val subscription = db.collection("admin_logs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("SystemLogRepository", "UID=${auth.currentUser?.uid} Collection=admin_logs Operation=Listen FirestoreCode=${error.code}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(try {
                    snapshot?.toObjects(AdminLog::class.java) ?: emptyList()
                } catch (e: Exception) {
                    android.util.Log.e("SystemLogRepository", "Error parsing AdminLog", e)
                    emptyList()
                })
            }
        awaitClose { subscription.remove() }
    }



    suspend fun logAction(action: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: "system"
        val logId = db.collection("admin_logs").document().id
        val log = AdminLog(
            id = logId,
            adminId = uid,
            action = action,
            timestamp = System.currentTimeMillis()
        )
        return FirestoreAudit.verifiedWrite("admin_logs", logId) {
            db.collection("admin_logs").document(logId).set(log).await()
        }
    }
}
