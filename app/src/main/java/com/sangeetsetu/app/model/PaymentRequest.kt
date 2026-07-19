package com.sangeetsetu.app.model

data class PaymentRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: PaymentStatus = PaymentStatus.PENDING,
    val upiIdUsed: String = ""
)
