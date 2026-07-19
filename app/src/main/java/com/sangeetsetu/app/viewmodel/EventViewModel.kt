package com.sangeetsetu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IEventRepository
import com.sangeetsetu.app.model.Event
import com.sangeetsetu.app.util.RefreshSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: IEventRepository
) : ViewModel() {

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchEvents(fromServer = false)
        viewModelScope.launch {
            RefreshSignal.refreshEvent.collect {
                fetchEvents(fromServer = true)
            }
        }
    }

    fun fetchEvents(fromServer: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _events.value = eventRepository.getAllEvents(fromServer)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load events"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getEventById(eventId: String, onResult: (Event?) -> Unit) {
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventById(eventId)
                onResult(event)
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }
}
