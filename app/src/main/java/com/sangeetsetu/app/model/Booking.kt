package com.sangeetsetu.app.model

data class Booking(
    val id: String = "",
    val userId: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val userName: String = "",
    val eventType: String = "",
    val eventDate: String = "",
    val eventTime: String = "",
    val location: String = "",
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val transactionId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)

enum class BookingStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED
}
