package com.sangeetsetu.app.domain.usecase

import com.sangeetsetu.app.domain.repository.IBookingRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.User
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

data class ArtistData(
    val profile: User?,
    val bookings: List<Booking>
)

class GetArtistDataUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    private val bookingRepository: IBookingRepository
) {
    suspend operator fun invoke(): Result<ArtistData> {
        val uid = userRepository.getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            coroutineScope {
                val profileDeferred = async { userRepository.getUserProfile(uid) }
                val bookingsDeferred = async { bookingRepository.getBookingsForUser(uid) }
                
                val profile = profileDeferred.await().getOrNull()
                val bookings = bookingsDeferred.await()
                
                Result.success(ArtistData(profile, bookings))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
