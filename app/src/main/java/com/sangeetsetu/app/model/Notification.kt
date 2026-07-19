package com.sangeetsetu.app.model

data class Notification(
    val id: String = "",
    val userId: String = "", // Target user (Artist or User)
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val bookingId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    
    // Rich notification fields
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val chatId: String = ""
)

enum class NotificationType {
    GENERAL,
    BOOKING_UPDATE,
    PAYMENT_SUCCESS,
    ARTIST_APPROVAL,
    LIVE_EVENT,
    BROADCAST,
    CHAT
}
