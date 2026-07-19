package com.sangeetsetu.app.repository

import com.sangeetsetu.app.model.Notification
import com.sangeetsetu.app.model.NotificationType
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object NotificationRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    fun getUnreadCountFlow(): Flow<Int> = callbackFlow {
        val uid = UserRepository.getCurrentUserId() ?: return@callbackFlow
        val subscription = db.collection("notifications")
            .whereEqualTo("userId", uid)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("NotificationRepository", "UID=$uid Collection=notifications Operation=Listen FirestoreCode=${error.code}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(0)
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }


    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        bookingId: String? = null,
        senderId: String = "",
        senderName: String = "",
        senderPhoto: String = "",
        chatId: String = ""
    ) {
        val id = db.collection("notifications").document().id
        val notification = Notification(
            id = id,
            userId = userId,
            title = title,
            message = message,
            type = type,
            bookingId = bookingId,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            senderId = senderId,
            senderName = senderName,
            senderPhoto = senderPhoto,
            chatId = chatId
        )
        
        FirestoreAudit.verifiedWrite("notifications", id) {
            db.collection("notifications").document(id).set(notification).await()
        }
    }
}
