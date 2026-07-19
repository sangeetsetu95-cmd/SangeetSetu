package com.sangeetsetu.app.model

import com.google.firebase.firestore.PropertyName

data class Announcement(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val bannerUrl: String? = null,
    val actionLink: String? = null,
    val actionLabel: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // Default 30 days
    @get:PropertyName("isActive") @set:PropertyName("isActive")
    var isActive: Boolean = true,
    
    // Targeting
    val targetType: String = "ALL", // ALL, USERS, ARTISTS, VIP_USERS, VERIFIED_ARTISTS, CATEGORY, LOCATION, CUSTOM
    val targetCategories: List<String> = emptyList(),
    val targetStates: List<String> = emptyList(),
    val targetDistricts: List<String> = emptyList(),
    val targetUserIds: List<String> = emptyList(),
    
    // Legacy support
    val recipientCategories: List<String> = emptyList(),
    
    // Stats
    val sentCount: Int = 0,
    val readCount: Int = 0,
    val clickCount: Int = 0,
    
    val senderId: String = "admin"
)
