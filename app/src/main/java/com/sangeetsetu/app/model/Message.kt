package com.sangeetsetu.app.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val type: String = "text",
    val announcementId: String? = null,
    val imageUrl: String? = null,
    val actionLink: String? = null,
    val actionLabel: String? = null
)

data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "CHAT",
    val conversationType: String = "booking", // "booking", "support", or "broadcast"
    val senderRole: String? = null, // "admin" for broadcasts
    // UI specific fields (might not be in Firestore directly or computed)
    val participantName: String = "",
    val participantPhoto: String = "",
    val unreadCount: Int = 0,
    val isVerified: Boolean = false
)
