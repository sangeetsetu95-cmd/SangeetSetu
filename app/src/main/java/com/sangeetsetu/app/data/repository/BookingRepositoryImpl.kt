package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IBookingRepository
import com.sangeetsetu.app.domain.repository.INotificationRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.model.NotificationType
import com.sangeetsetu.app.model.PaymentStatus
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: IUserRepository,
    private val notificationRepository: INotificationRepository
) : IBookingRepository {

    override fun getAllBookingsFlow(): Flow<List<Booking>> = callbackFlow {
        val subscription = db.collection("bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookingRepository", "UID=${userRepository.getCurrentUserId()} Collection=bookings Operation=Listen FirestoreCode=${error.code}", error)
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                val bookings = try {
                    snapshot?.toObjects(Booking::class.java) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("BookingRepository", "Error parsing Bookings", e)
                    emptyList()
                }
                trySend(bookings)
            }
        awaitClose { subscription.remove() }
    }

    override fun getBookingsForUserFlow(userId: String): Flow<List<Booking>> = callbackFlow {
        val subscription = db.collection("bookings")
            .where(Filter.or(
                Filter.equalTo("userId", userId),
                Filter.equalTo("artistId", userId)
            ))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BookingRepository", "UID=${userRepository.getCurrentUserId()} Collection=bookings Operation=Listen Filtered FirestoreCode=${error.code}", error)
                    if (error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                val filtered = try {
                    snapshot?.toObjects(Booking::class.java)?.sortedByDescending { it.createdAt } ?: emptyList()
                } catch (e: Exception) {
                    Log.e("BookingRepository", "Error parsing user Bookings", e)
                    emptyList()
                }
                trySend(filtered)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun getAllBookings(): List<Booking> {
        return try {
            val snap = db.collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            snap.toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBookingsForUser(userId: String): List<Booking> {
        return try {
            val customerSnap = db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get().await()
            
            val artistSnap = db.collection("bookings")
                .whereEqualTo("artistId", userId)
                .get().await()

            (customerSnap.toObjects(Booking::class.java) + 
             artistSnap.toObjects(Booking::class.java))
                .distinctBy { it.id }
                .sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            db.collection("bookings").document(bookingId).get().await()
                .toObject(Booking::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createBooking(booking: Booking): Result<String> {
        val docRef = db.collection("bookings").document()
        val finalBooking = booking.copy(
            id = docRef.id,
            createdAt = System.currentTimeMillis(),
            status = BookingStatus.PENDING
        )
        
        return FirestoreAudit.verifiedWrite("bookings", docRef.id) {
            docRef.set(finalBooking).await()
            
            // Record in History
            val historyId = db.collection("booking_history").document().id
            val historyItem = mapOf(
                "id" to historyId,
                "bookingId" to docRef.id,
                "oldStatus" to "NONE",
                "newStatus" to BookingStatus.PENDING.name,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("booking_history").document(historyId).set(historyItem).await()

            // Notify Artist
            notificationRepository.sendNotification(
                userId = finalBooking.artistId,
                title = "New Booking Request",
                message = "You have a new booking request from ${finalBooking.userName}",
                type = NotificationType.BOOKING_UPDATE,
                bookingId = finalBooking.id
            )
        }.map { docRef.id }
    }

    override suspend fun updateBookingStatus(booking: Booking, newStatus: BookingStatus): Result<Unit> {
        return FirestoreAudit.verifiedWrite("bookings", booking.id) {
            db.collection("bookings").document(booking.id)
                .update("status", newStatus.name).await()
            
            // Record in History
            val historyId = db.collection("booking_history").document().id
            val historyItem = mapOf(
                "id" to historyId,
                "bookingId" to booking.id,
                "oldStatus" to booking.status.name,
                "newStatus" to newStatus.name,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("booking_history").document(historyId).set(historyItem).await()

            // Send Notifications
            notificationRepository.sendNotification(
                userId = booking.userId,
                title = "Booking Update",
                message = "Your booking for ${booking.artistName} is now ${newStatus.name}",
                type = NotificationType.BOOKING_UPDATE,
                bookingId = booking.id
            )

            notificationRepository.sendNotification(
                userId = booking.artistId,
                title = "Booking Update",
                message = "New update for booking from ${booking.userName}: ${newStatus.name}",
                type = NotificationType.BOOKING_UPDATE,
                bookingId = booking.id
            )
        }
    }

    override suspend fun confirmPayment(booking: Booking, transactionId: String): Result<Unit> {
        return FirestoreAudit.verifiedWrite("bookings", booking.id) {
            val updates = mapOf(
                "paymentStatus" to PaymentStatus.SUCCESS.name,
                "transactionId" to transactionId,
                "status" to BookingStatus.ACCEPTED.name
            )
            db.collection("bookings").document(booking.id).update(updates).await()

            // Payment History
            val paymentId = db.collection("payment_history").document().id
            val payment = mapOf(
                "id" to paymentId,
                "bookingId" to booking.id,
                "userId" to booking.userId,
                "artistId" to booking.artistId,
                "transactionId" to transactionId,
                "status" to "SUCCESS",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("payment_history").document(paymentId).set(payment).await()

            // Notify
            notificationRepository.sendNotification(
                userId = booking.userId,
                title = "Booking Confirmed",
                message = "Your booking for ${booking.artistName} has been confirmed.",
                type = NotificationType.PAYMENT_SUCCESS,
                bookingId = booking.id
            )

            notificationRepository.sendNotification(
                userId = booking.artistId,
                title = "Booking Confirmed",
                message = "Booking from ${booking.userName} has been confirmed.",
                type = NotificationType.PAYMENT_SUCCESS,
                bookingId = booking.id
            )
        }
    }
}
