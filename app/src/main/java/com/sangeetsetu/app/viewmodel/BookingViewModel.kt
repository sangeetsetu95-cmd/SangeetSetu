package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IBookingRepository
import com.sangeetsetu.app.domain.repository.ISystemLogRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val bookingRepository: IBookingRepository,
    private val userRepository: IUserRepository,
    private val systemLogRepository: ISystemLogRepository
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        refreshBookings()
    }

    fun refreshBookings() {
        val uid = userRepository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                _isLoading.value = true
                bookingRepository.getBookingsForUserFlow(uid).collect { 
                    _bookings.value = it 
                    _isLoading.value = false
                }
            }
        }
    }

    fun fetchAllBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                bookingRepository.getAllBookingsFlow().collect { 
                    _bookings.value = it 
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = "Failed to load bookings: ${e.message}"
            }
        }
    }

    fun createBooking(
        booking: Booking,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = userRepository.getCurrentUserId()
        if (uid == null) {
            onError("User not authenticated")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // In a full implementation, the user's name should also come from userRepository or auth
                val finalBooking = booking.copy(
                    userId = uid
                    // userName should be set in Repository or by fetching profile
                )
                
                bookingRepository.createBooking(finalBooking).onSuccess {
                    onSuccess()
                }.onFailure { e ->
                    _error.value = "Booking creation failed: ${e.message}"
                    onError(e.localizedMessage ?: "Failed to create booking")
                }
            } catch (e: Exception) {
                Log.e("BookingVM", "createBooking CRITICAL FAILURE", e)
                onError(e.localizedMessage ?: "Failed to create booking")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val booking = bookingRepository.getBookingById(bookingId) ?: return@launch
                
                bookingRepository.updateBookingStatus(booking, newStatus).onSuccess {
                    systemLogRepository.logAction("Updated booking $bookingId to $newStatus")
                    onSuccess()
                }.onFailure { e -> _error.value = "Status update failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("BookingVM", "updateBookingStatus CRITICAL FAILURE", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processPayment(bookingId: String, transactionId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val booking = bookingRepository.getBookingById(bookingId) ?: return@launch

                bookingRepository.confirmPayment(booking, transactionId).onSuccess {
                    systemLogRepository.logAction("Confirmation received for booking $bookingId")
                    onSuccess()
                }.onFailure { e -> _error.value = "Confirmation failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("BookingVM", "processPayment CRITICAL FAILURE", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun logAction(action: String) {
        viewModelScope.launch {
            systemLogRepository.logAction(action)
        }
    }
}
