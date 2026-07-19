package com.sangeetsetu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.*
import com.sangeetsetu.app.repository.DynamicFormRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminFormViewModel : ViewModel() {
    private val _fields = MutableStateFlow<List<FormField>>(emptyList())
    val fields = _fields.asStateFlow()

    private val _uiSettings = MutableStateFlow(AppUISettings())
    val uiSettings = _uiSettings.asStateFlow()

    private val _regSettings = MutableStateFlow(RegistrationSettings())
    val regSettings = _regSettings.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                DynamicFormRepository.getFormFields(),
                DynamicFormRepository.getUISettings(),
                DynamicFormRepository.getRegistrationSettings(),
                DynamicFormRepository.getCategories()
            ) { fields, ui, reg, cats ->
                _fields.value = fields
                _uiSettings.value = ui
                _regSettings.value = reg
                _categories.value = cats
            }.onCompletion {
                _isLoading.value = false
            }.collect()
        }
    }

    fun addField(field: FormField) {
        viewModelScope.launch {
            DynamicFormRepository.saveFormField(field)
        }
    }

    fun updateField(field: FormField) {
        viewModelScope.launch {
            DynamicFormRepository.saveFormField(field)
        }
    }

    fun deleteField(fieldId: String) {
        viewModelScope.launch {
            DynamicFormRepository.deleteFormField(fieldId)
        }
    }

    fun saveUISettings(settings: AppUISettings) {
        viewModelScope.launch {
            DynamicFormRepository.saveUISettings(settings)
        }
    }

    fun saveRegSettings(settings: RegistrationSettings) {
        viewModelScope.launch {
            DynamicFormRepository.saveRegistrationSettings(settings)
        }
    }

    fun reorderFields(fromIndex: Int, toIndex: Int) {
        val currentFields = _fields.value.toMutableList()
        val movedField = currentFields.removeAt(fromIndex)
        currentFields.add(toIndex, movedField)
        
        viewModelScope.launch {
            currentFields.forEachIndexed { index, field ->
                DynamicFormRepository.saveFormField(field.copy(displayOrder = index))
            }
        }
    }
}
