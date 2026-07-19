package com.sangeetsetu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.*
import com.sangeetsetu.app.domain.repository.IConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val configRepository: IConfigRepository
) : ViewModel() {

    private val _states = MutableStateFlow<List<ConfigItem>>(emptyList())
    val states = _states.asStateFlow()

    private val _districts = MutableStateFlow<List<ConfigItem>>(emptyList())
    val districts = _districts.asStateFlow()

    private val _categories = MutableStateFlow<List<ConfigItem>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _specialities = MutableStateFlow<List<ConfigItem>>(emptyList())
    val specialities = _specialities.asStateFlow()

    private val _instruments = MutableStateFlow<List<ConfigItem>>(emptyList())
    val instruments = _instruments.asStateFlow()

    private val _eventTypes = MutableStateFlow<List<ConfigItem>>(emptyList())
    val eventTypes = _eventTypes.asStateFlow()

    private val _languages = MutableStateFlow<List<ConfigItem>>(emptyList())
    val languages = _languages.asStateFlow()

    private val _experienceLevels = MutableStateFlow<List<ConfigItem>>(emptyList())
    val experienceLevels = _experienceLevels.asStateFlow()

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections = _homeSections.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val subscriptionPlans = _subscriptionPlans.asStateFlow()

    private val _appInfo = MutableStateFlow(AppInformation())
    val appInfo = _appInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadAllConfigs()
    }

    private fun loadAllConfigs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                configRepository.getItems("states").collect { _states.value = it }
            } finally {
                _isLoading.value = false
            }
        }
        viewModelScope.launch {
            configRepository.getItems("districts").collect { _districts.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("categories").collect { _categories.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("specialities").collect { _specialities.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("instruments").collect { _instruments.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("event_types").collect { _eventTypes.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("languages").collect { _languages.value = it }
        }
        viewModelScope.launch {
            configRepository.getItems("experience_levels").collect { _experienceLevels.value = it }
        }
        viewModelScope.launch {
            configRepository.getHomeSections().collect { _homeSections.value = it }
        }
        viewModelScope.launch {
            configRepository.getSubscriptionPlans().collect { _subscriptionPlans.value = it }
        }
        viewModelScope.launch {
            configRepository.getAppInfo().collect { _appInfo.value = it }
        }
    }

    fun getDistrictsForState(stateName: String): List<String> {
        val stateId = _states.value.find { it.name == stateName }?.id ?: return emptyList()
        return _districts.value.filter { it.parentId == stateId }.map { it.name }
    }

    fun saveConfigItem(collection: String, item: ConfigItem, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                configRepository.saveItem(collection, item)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                onError(e.localizedMessage ?: "Save failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteConfigItem(collection: String, id: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                configRepository.deleteItem(collection, id)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                onError(e.localizedMessage ?: "Delete failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAppInfo(info: AppInformation, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                configRepository.updateAppInfo(info)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.localizedMessage
                onError(e.localizedMessage ?: "Update failed")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
