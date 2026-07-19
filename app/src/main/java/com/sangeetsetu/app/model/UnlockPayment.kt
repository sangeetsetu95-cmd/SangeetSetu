package com.sangeetsetu.app.model

data class UnlockPayment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val amount: Double = 11.0,
    val transactionId: String = "",
    val paymentStatus: String = "pending", // "pending", "success", "failed"
    val unlockedAt: Long = System.currentTimeMillis()
)
