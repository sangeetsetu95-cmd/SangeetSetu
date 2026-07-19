package com.sangeetsetu.app.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val iconUrl: String = "", // Keeping for backward compatibility
    val cloudinaryPublicId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
) {
    @get:Exclude
    val displayImage: String get() = if (imageUrl.isNotEmpty()) imageUrl else iconUrl
}

data class Banner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val imageUrl: String = "",
    val cloudinaryPublicId: String = "",
    val bannerType: String = "General", // Featured, Verified, RisingStars, MostBooked, VIP, Festival
    val clickActionType: String = "Category", // Category, Artist, Custom
    val actionUrl: String = "", // Value for the action (e.g., category name or artist ID)
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Default 1 month
    @get:PropertyName("order") @set:PropertyName("order") var order: Int = 0,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
) {
    @get:Exclude @set:PropertyName("active")
    var activeLegacy: Boolean
        get() = isActive
        set(value) { isActive = value }
        
    @get:Exclude @set:PropertyName("displayOrder")
    var displayOrderLegacy: Int
        get() = order
        set(value) { order = value }
}

data class AdminStats(
    val totalUsers: Int = 0,
    val totalArtists: Int = 0,
    val activeArtists: Int = 0,
    val totalBookings: Int = 0,
    val todayBookings: Int = 0,
    val pendingBookings: Int = 0,
    val confirmedBookings: Int = 0,
    val completedBookings: Int = 0,
    val cancelledBookings: Int = 0,
    val totalCategories: Int = 0,
    val activeUsers: Int = 0,
    val totalSongs: Int = 0,
    val totalAlbums: Int = 0,
    val newRegistrationsToday: Int = 0
)

data class BroadcastMessage(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val targetType: TargetType = TargetType.ALL,
    val targetCategory: String? = null,
    val targetState: String? = null,
    val scheduleTime: Long = System.currentTimeMillis(),
    val isSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TargetType {
    ALL, ARTISTS, ORGANIZERS, BY_CATEGORY, BY_LOCATION
}

data class Offer(
    val id: String = "",
    val code: String = "",
    val description: String = "",
    val discountPercentage: Int = 0,
    val maxDiscount: Double = 0.0,
    val validUntil: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    @get:PropertyName("active") @set:PropertyName("active") var active: Boolean = true
) {
    @get:Exclude @set:PropertyName("isActive")
    var isActiveLegacy: Boolean
        get() = active
        set(value) { active = value }
}

data class HomeSection(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: String = "HorizontalList", // HorizontalList, Grid, List, Banner, Promo
    val contentType: String = "Artists", // Artists, Categories, Events, Bhajans
    val contentFilter: String = "All", // Filter for content (e.g., category name or "Verified")
    val imageUrl: String = "", // For promo banners
    val displayOrder: Int = 0,
    val isVisible: Boolean = true
)

data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val iconUrl: String = "",
    @get:PropertyName("active") @set:PropertyName("active") var active: Boolean = true
) {
    @get:Exclude @set:PropertyName("isActive")
    var isActiveLegacy: Boolean
        get() = active
        set(value) { active = value }
}

data class AdminLog(
    val id: String = "",
    val adminId: String = "",
    val action: String = "",
    val targetId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationLog(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "", // "All" for broadcast
    val title: String = "",
    val message: String = "",
    val type: String = "", // CHAT, BROADCAST, etc.
    val status: String = "SENT", // SENT, DELIVERED, READ
    val timestamp: Long = System.currentTimeMillis()
)

data class SubscriptionPlan(
    val id: String = "",
    val name: String = "", // e.g., "Silver", "Gold", "Diamond"
    val durationMonths: Int = 1,
    val description: String = "",
    val features: List<String> = emptyList(),
    @get:PropertyName("active") @set:PropertyName("active") var active: Boolean = true,
    val order: Int = 0
) {
    @get:Exclude @set:PropertyName("isActive")
    var isActiveLegacy: Boolean
        get() = active
        set(value) { active = value }
}

data class ConfigItem(
    val id: String = "",
    val name: String = "",
    val value: String = "",
    val iconUrl: String? = null,
    val description: String? = null,
    val order: Int = 0,
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
    val parentId: String? = null // For districts (parent state id)
)

data class AppInformation(
    val id: String = "default",
    val faq: List<FAQItem> = emptyList(),
    val contactInfo: ContactInfo = ContactInfo(),
    val socialLinks: List<SocialLink> = emptyList(),
    val termsAndConditions: String = "",
    val privacyPolicy: String = "",
    val referralOffer: String = ""
)

data class FAQItem(
    val question: String = "",
    val answer: String = ""
)

data class ContactInfo(
    val email: String = "sangeetsetu95@gmail.com",
    val phone: String = "+91 9258585074",
    val address: String = ""
)

data class SocialLink(
    val platform: String = "",
    val url: String = ""
)

data class Bhajan(
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val audioUrl: String = "",
    val imageUrl: String = "",
    val duration: String = "",
    val category: String = "",
    val uploadDate: Long = System.currentTimeMillis()
)

data class BadgeConfig(
    val id: String = "", // "verified" or "vip"
    val imageUrl: String = "",
    val cloudinaryPublicId: String = "",
    val isEnabled: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class NavigationItem(
    val id: String = "",
    val label: String = "",
    val route: String = "",
    val iconName: String = "home", // mapped to ImageVector
    val displayOrder: Int = 0,
    @get:PropertyName("isVisible") @set:PropertyName("isVisible") var isVisible: Boolean = true,
    val type: String = "Bottom" // Bottom, SideDrawer, MoreScreen
)

data class PopUpConfig(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val actionRoute: String = "",
    val actionText: String = "Learn More",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = false,
    val showOncePerSession: Boolean = true,
    val priority: Int = 0
)

data class Translation(
    val key: String = "",
    val values: Map<String, String> = emptyMap() // "hi" -> "नमस्ते", "en" -> "Hello"
)
