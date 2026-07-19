package com.sangeetsetu.app.model

data class SystemSettings(
    val appName: String = "Sangeet Setu",
    val maintenanceMode: Boolean = false,
    val supportPhone: String = "+91 9119119119",
    val supportEmail: String = "support@sangeetsetu.com",
    val latestVersionCode: Int = 1,
    val minimumVersionCode: Int = 1,
    val updateUrl: String = "https://sangeetsetu.com/download",
    val isUpdateMandatory: Boolean = false,
    val broadcastTopic: String = "all_users",
    val upiId: String = "sangeetsetu@upi",
    val upiQrUrl: String = "",
    val features: Map<String, Boolean> = mapOf(
        "ai_assistant" to true,
        "referral_system" to true,
        "live_telecast" to true,
        "online_booking" to true,
        "artist_registration" to true,
        "dynamic_nav" to true,
        "quick_categories" to false,
        "chat_privacy_filter" to true
    ),
    val aiSettings: Map<String, String> = mapOf(
        "model" to "gemini-1.5-flash",
        "system_prompt" to "You are Sangeet Setu AI Assistant, helpful for artists and organizers."
    ),
    val theme: Map<String, String> = mapOf(
        "primary" to "#D4AF37",
        "secondary" to "#0B0B0F",
        "background" to "#0B0B0F"
    ),
    val chatAutoDeleteDays: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis(),
    val updatedBy: String = "admin"
)
