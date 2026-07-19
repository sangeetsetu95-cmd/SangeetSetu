package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.INotificationRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Notification
import com.sangeetsetu.app.model.NotificationType
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: IUserRepository
) : INotificationRepository {

    override fun getUnreadCountFlow(): Flow<Int> = callbackFlow {
        val uid = userRepository.getCurrentUserId() ?: return@callbackFlow
        val subscription = db.collection("notifications")
            .whereEqualTo("userId", uid)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "UID=$uid Collection=notifications Operation=Listen FirestoreCode=${error.code}", error)
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(0)
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.size() ?: 0)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: NotificationType,
        bookingId: String?,
        senderId: String,
        senderName: String,
        senderPhoto: String,
        chatId: String
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
