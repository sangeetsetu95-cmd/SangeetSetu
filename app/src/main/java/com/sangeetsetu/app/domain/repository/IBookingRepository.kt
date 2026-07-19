package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import kotlinx.coroutines.flow.Flow

interface IBookingRepository {
    fun getAllBookingsFlow(): Flow<List<Booking>>
    fun getBookingsForUserFlow(userId: String): Flow<List<Booking>>
    suspend fun getAllBookings(): List<Booking>
    suspend fun getBookingsForUser(userId: String): List<Booking>
    suspend fun getBookingById(bookingId: String): Booking?
    suspend fun createBooking(booking: Booking): Result<String>
    suspend fun updateBookingStatus(booking: Booking, newStatus: BookingStatus): Result<Unit>
    suspend fun confirmPayment(booking: Booking, transactionId: String): Result<Unit>
}
