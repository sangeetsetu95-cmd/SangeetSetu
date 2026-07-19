package com.sangeetsetu.app.domain.usecase

import com.sangeetsetu.app.domain.repository.IBookingRepository
import com.sangeetsetu.app.model.BookingStatus
import javax.inject.Inject

class UpdateBookingStatusUseCase @Inject constructor(
    private val bookingRepository: IBookingRepository
) {
    suspend operator fun invoke(bookingId: String, newStatus: BookingStatus): Result<Unit> {
        val booking = bookingRepository.getBookingById(bookingId) 
            ?: return Result.failure(Exception("Booking not found"))
            
        return bookingRepository.updateBookingStatus(booking, newStatus)
    }
}
