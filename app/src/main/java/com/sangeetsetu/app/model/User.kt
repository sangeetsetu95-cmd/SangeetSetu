package com.sangeetsetu.app.model

import com.google.firebase.firestore.PropertyName

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val artistId: String = "", // SSA000001
    val whatsapp: String = "",
    val city: String = "",
    val district: String = "",
    val state: String = "",
    val age: String = "",
    val experience: String = "0", // Stored as string for flexibility
    val aboutMe: String = "",
    val photoUrl: String = "",
    val cloudinaryPublicId: String = "",
    val facebookLink: String = "",
    val instagramLink: String = "",
    val youtubeLink: String = "",
    val userType: String = "User", // Artist, Organizer, User
    val organizationName: String = "",
    val category: String = "", // Display name for legacy support
    val categoryId: String = "", // Unique ID for category matching
    val skill: String = "",
    val idProofUrl: String = "",
    
    @get:PropertyName("isVerified")
    @set:PropertyName("isVerified")
    var isVerified: Boolean = false,

    @get:PropertyName("isApproved")
    @set:PropertyName("isApproved")
    var isApproved: Boolean = false,

    @get:PropertyName("profileCompleted")
    @set:PropertyName("profileCompleted")
    var profileCompleted: Boolean = false,

    @get:PropertyName("registrationCompleted")
    @set:PropertyName("registrationCompleted")
    var registrationCompleted: Boolean = false,

    val role: String = "user", // admin, user, artist
    val status: String = "ACTIVE", // For legacy compatibility
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED
    val verificationStatus: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val approvalStatus: String = "PENDING", // PENDING, APPROVED, REJECTED
    val favorites: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    
    // Artist Specific Fields (Optional in Firestore)
    val rating: Double = 0.0,
    val reviewsCount: Int = 0,
    val gallery: List<String> = emptyList(),
    val performanceTypes: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val availableForTravel: Boolean = true,
    
    @get:PropertyName("isPremium")
    @set:PropertyName("isPremium")
    var isPremium: Boolean = false,

    @get:PropertyName("isAvailable")
    @set:PropertyName("isAvailable")
    var isAvailable: Boolean = true,

    val totalBookings: Int = 0,
    val referralCount: Int = 0,
    
    val chatWarningCount: Int = 0,
    val chatDisabledUntil: Long = 0,

    @get:PropertyName("rewardClaimed")
    @set:PropertyName("rewardClaimed")
    var rewardClaimed: Boolean = false,

    @get:PropertyName("isVip")
    @set:PropertyName("isVip")
    var isVip: Boolean = false,

    val addresses: List<String> = emptyList(),

    // Presence System Fields
    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var isOnline: Boolean = false,
    
    val lastSeen: Long = 0,
    val presencePrivacy: String = "Everyone", // Everyone, Nobody
    val lastOnlineTimestamp: Long = 0,

    // Notification System
    val fcmToken: String = "",
    val notificationSettings: NotificationSettings = NotificationSettings()
)

data class NotificationSettings(
    val chatNotificationsEnabled: Boolean = true,
    val broadcastNotificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
