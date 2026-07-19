package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.usecase.GetArtistDataUseCase
import com.sangeetsetu.app.domain.usecase.UpdateBookingStatusUseCase
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDashboardViewModel @Inject constructor(
    private val getArtistDataUseCase: GetArtistDataUseCase,
    private val updateBookingStatusUseCase: UpdateBookingStatusUseCase
) : ViewModel() {

    private val _artistBookings = MutableStateFlow<List<Booking>>(emptyList())
    val artistBookings: StateFlow<List<Booking>> = _artistBookings

    private val _artistProfile = MutableStateFlow<User?>(null)
    val artistProfile: StateFlow<User?> = _artistProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        fetchArtistData()
    }

    fun clearMessage() {
        _message.value = null
    }

    fun fetchArtistData() {
        viewModelScope.launch {
            _isLoading.value = true
            getArtistDataUseCase().onSuccess { data ->
                _artistProfile.value = data.profile
                _artistBookings.value = data.bookings
            }.onFailure { e ->
                Log.e("ArtistDashboardVM", "Failed to fetch artist data", e)
                _message.value = "Failed to load latest data from server."
            }
            _isLoading.value = false
        }
    }

    fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            updateBookingStatusUseCase(bookingId, newStatus).onSuccess {
                _message.value = "Booking status updated successfully"
                fetchArtistData()
            }.onFailure { e ->
                Log.e("ArtistDashboardVM", "Failed to update booking status", e)
                _message.value = "Failed to update status on server."
            }
            _isLoading.value = false
        }
    }
}
