package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IMainRepository
import com.sangeetsetu.app.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val banners: List<Banner> = emptyList(),
    val categories: List<Category> = emptyList(),
    val featuredArtists: List<User> = emptyList(),
    val vipArtists: List<User> = emptyList(),
    val upcomingEvents: List<Event> = emptyList(),
    val popularServices: List<Service> = emptyList(),
    val homeSections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mainRepository: IMainRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        syncHomeData()
    }

    private fun syncHomeData() {
        viewModelScope.launch {
            // Using separate launches to avoid combine blocking if one flow is slow
            launch {
                mainRepository.getBannersFlow().collect { data ->
                    _uiState.update { it.copy(banners = data, isLoading = false) }
                }
            }
            launch {
                mainRepository.getCategoriesFlow().collect { data ->
                    _uiState.update { it.copy(categories = data) }
                }
            }
            launch {
                mainRepository.getArtistsFlow("All").collect { data ->
                    _uiState.update { it.copy(featuredArtists = data) }
                }
            }
            launch {
                mainRepository.getVipArtistsFlow().collect { data ->
                    _uiState.update { it.copy(vipArtists = data) }
                }
            }
            launch {
                mainRepository.getUpcomingEventsFlow().collect { data ->
                    _uiState.update { it.copy(upcomingEvents = data) }
                }
            }
            launch {
                mainRepository.getServicesFlow().collect { data ->
                    _uiState.update { it.copy(popularServices = data) }
                }
            }
            launch {
                mainRepository.getHomeSectionsFlow().collect { data ->
                    _uiState.update { it.copy(homeSections = data) }
                }
            }
        }
    }
    
    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        syncHomeData()
    }
}
