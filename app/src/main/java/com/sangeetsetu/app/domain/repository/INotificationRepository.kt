package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.NotificationType
import kotlinx.coroutines.flow.Flow

interface INotificationRepository {
    fun getUnreadCountFlow(): Flow<Int>
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
    )
}
