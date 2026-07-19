package com.sangeetsetu.app.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IDynamicFormRepository
import com.sangeetsetu.app.domain.repository.IStorageRepository
import com.sangeetsetu.app.model.AppUISettings
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.District
import com.sangeetsetu.app.model.FormField
import com.sangeetsetu.app.model.FormFieldType
import com.sangeetsetu.app.model.Instrument
import com.sangeetsetu.app.model.RegistrationSettings
import com.sangeetsetu.app.model.State
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.util.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    application: Application,
    private val dynamicFormRepository: IDynamicFormRepository,
    private val storageRepository: IStorageRepository,
    private val userRepository: com.sangeetsetu.app.domain.repository.IUserRepository,
    private val logRepository: com.sangeetsetu.app.domain.repository.ISystemLogRepository
) : AndroidViewModel(application) {
    private val _formFields = MutableStateFlow<List<FormField>>(emptyList())
    val formFields = _formFields.asStateFlow()

    private val _uiSettings = MutableStateFlow(AppUISettings())
    val uiSettings = _uiSettings.asStateFlow()

    private val _registrationSettings = MutableStateFlow(RegistrationSettings())
    val registrationSettings = _registrationSettings.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories = _categories.asStateFlow()

    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())
    val instruments = _instruments.asStateFlow()

    private val _states = MutableStateFlow<List<State>>(emptyList())
    val states = _states.asStateFlow()

    private val _districts = MutableStateFlow<List<District>>(emptyList())
    val districts = _districts.asStateFlow()

    private val _formState = MutableStateFlow<Map<String, Any>>(emptyMap())
    val formState = _formState.asStateFlow()

    private val _errors = MutableStateFlow<Map<String, String>>(emptyMap())
    val errors = _errors.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted = _isSubmitted.asStateFlow()

    val isFormValid = combine(_formState, _formFields, _categories) { state, fields, cats ->
        val selectedCatId = state["category"] as? String ?: ""
        val categoryObj = cats.find { it.id == selectedCatId }
        val selectedCatName = categoryObj?.name ?: selectedCatId
        
        fields.all { field ->
            val isApplicable = field.categoryFilter.isEmpty() || 
                              field.categoryFilter.contains(selectedCatId) || 
                              field.categoryFilter.contains(selectedCatName)

            if (field.isEnabled && isApplicable && field.isRequired) {
                val value = state[field.id]
                value != null && (value !is String || value.isNotBlank()) && (value !is List<*> || value.isNotEmpty())
            } else {
                true
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _registrationResult = MutableSharedFlow<Result<String>>()
    val registrationResult = _registrationResult.asSharedFlow()

    init {
        loadConfig()
    }

    private var loadConfigJob: Job? = null

    private fun loadConfig() {
        loadConfigJob?.cancel()
        loadConfigJob = viewModelScope.launch {
            Log.d("RegistrationVM", "loadConfig: Starting configuration fetch")
            _isLoading.value = true
            try {
                // Pre-fill logic needs to be safe
                val uid = userRepository.getCurrentUserId()
                Log.d("RegistrationVM", "loadConfig: Current UID: $uid")
                
                val profile = try {
                    if (uid != null) {
                        withTimeout(5000L) {
                            userRepository.getUserProfile(uid).getOrNull()
                        }
                    } else null
                } catch (e: Exception) {
                    Log.w("RegistrationVM", "loadConfig: Profile fetch failed/timed out, continuing")
                    null
                }

                // Combine all flows from repository
                val combinedFlow = combine(
                    dynamicFormRepository.getFormFields().onEach { Log.d("RegistrationVM", "Flow: fields count=${it.size}") },
                    dynamicFormRepository.getUISettings().onEach { Log.d("RegistrationVM", "Flow: UI settings emitted") },
                    dynamicFormRepository.getRegistrationSettings().onEach { Log.d("RegistrationVM", "Flow: Reg settings emitted") },
                    dynamicFormRepository.getCategories().onEach { Log.d("RegistrationVM", "Flow: Categories count=${it.size}") },
                    dynamicFormRepository.getInstruments().onEach { Log.d("RegistrationVM", "Flow: Instruments count=${it.size}") },
                    dynamicFormRepository.getStates().onEach { Log.d("RegistrationVM", "Flow: States count=${it.size}") }
                ) { args ->
                    ConfigData(
                        fields = args[0] as List<FormField>,
                        ui = args[1] as AppUISettings,
                        reg = args[2] as RegistrationSettings,
                        cats = args[3] as List<Category>,
                        insts = args[4] as List<Instrument>,
                        states = args[5] as List<State>
                    )
                }

                // Wait for the FIRST valid emission with a 10s timeout
                Log.d("RegistrationVM", "loadConfig: Waiting for initial Firestore data")
                val initialData = withTimeout(10000L) {
                    combinedFlow.first()
                }

                Log.d("RegistrationVM", "loadConfig: Initial data received successfully")
                
                if (initialData.fields.isEmpty()) {
                    Log.d("RegistrationVM", "loadConfig: Firestore returned empty fields, using defaults")
                    applyDefaultConfig(profile)
                } else {
                    updateStates(initialData, profile)
                }
                
                // STOP SPINNER - We have enough to show the UI
                Log.d("RegistrationVM", "loadConfig: Stopping loading spinner")
                _isLoading.value = false

                // Start continuous collection for real-time updates in a separate job
                launch {
                    Log.d("RegistrationVM", "loadConfig: Starting real-time update collector")
                    combinedFlow.collect { data ->
                        Log.d("RegistrationVM", "loadConfig: Real-time update received")
                        if (data.fields.isNotEmpty()) {
                            updateStates(data, profile)
                        }
                    }
                }

            } catch (e: TimeoutCancellationException) {
                Log.e("RegistrationVM", "loadConfig: Timeout reached while fetching config", e)
                handleLoadFailure(Exception("Connection timed out. Loaded standard form."))
            } catch (e: Exception) {
                Log.e("RegistrationVM", "loadConfig: Error fetching configuration: ${e.message}", e)
                handleLoadFailure(e)
            } finally {
                // Ensure loading is stopped if we haven't already
                if (_isLoading.value) {
                    Log.d("RegistrationVM", "loadConfig: Finally block setting isLoading to false")
                    _isLoading.value = false
                }
            }
        }

        // Listen for state changes to update districts
        viewModelScope.launch {
            _formState.map { it["state"] as? String }.distinctUntilChanged().collect { selectedStateName ->
                if (!selectedStateName.isNullOrEmpty()) {
                    Log.d("RegistrationVM", "State changed to $selectedStateName, fetching districts")
                    val stateId = _states.value.find { it.name == selectedStateName }?.id ?: ""
                    dynamicFormRepository.getDistricts(stateId).collect {
                        _districts.value = it
                    }
                }
            }
        }
    }

    private fun handleLoadFailure(e: Exception) {
        if (_formFields.value.isEmpty()) {
            Log.d("RegistrationVM", "handleLoadFailure: Applying fallback default config")
            applyDefaultConfig(null)
        }
        
        viewModelScope.launch {
            _registrationResult.emit(Result.failure(e))
        }
    }

    private data class ConfigData(
        val fields: List<FormField>,
        val ui: AppUISettings,
        val reg: RegistrationSettings,
        val cats: List<Category>,
        val insts: List<Instrument>,
        val states: List<State>
    )

    private fun updateStates(data: ConfigData, profile: User? = null) {
        _formFields.value = data.fields
        _uiSettings.value = data.ui
        _registrationSettings.value = data.reg
        _categories.value = data.cats
        _instruments.value = data.insts
        _states.value = data.states

        // Initialize form state if empty
        if (_formState.value.isEmpty() && data.fields.isNotEmpty()) {
            val initialState = mutableMapOf<String, Any>()
            data.fields.forEach { field ->
                val prefilledValue = when(field.id) {
                    "name" -> profile?.name
                    "email" -> profile?.email
                    "phone" -> profile?.phone
                    "whatsapp" -> profile?.whatsapp
                    "category" -> {
                        if (!profile?.categoryId.isNullOrEmpty()) {
                            profile?.categoryId
                        } else {
                            // Find ID by name if categoryId is missing
                            _categories.value.find { it.name.equals(profile?.category, ignoreCase = true) }?.id ?: profile?.category
                        }
                    }
                    "state" -> profile?.state
                    "district" -> profile?.district
                    "city" -> profile?.city
                    "aboutMe" -> profile?.aboutMe
                    "photoUrl" -> profile?.photoUrl
                    else -> null
                }
                initialState[field.id] = prefilledValue ?: field.defaultValue
            }
            _formState.value = initialState
        }
    }

    private fun applyDefaultConfig(profile: User? = null) {
        val defaultFields = listOf(
            FormField("name", "Full Name", "Enter your full name", FormFieldType.TEXT, isRequired = true, displayOrder = 1),
            FormField("email", "Email Address", "name@example.com", FormFieldType.EMAIL, isRequired = true, displayOrder = 2),
            FormField("phone", "Mobile Number", "+91 XXXXX XXXXX", FormFieldType.MOBILE, isRequired = true, displayOrder = 3),
            FormField("whatsapp", "WhatsApp Number", "+91 XXXXX XXXXX", FormFieldType.MOBILE, isRequired = false, displayOrder = 4),
            FormField("category", "Category", "Select your category", FormFieldType.DROPDOWN, isRequired = true, displayOrder = 5),
            FormField("state", "State", "Select State", FormFieldType.STATE, isRequired = true, displayOrder = 6),
            FormField("district", "District", "Select District", FormFieldType.DISTRICT, isRequired = true, displayOrder = 7),
            FormField("city", "City / Town", "Enter city name", FormFieldType.TEXT, isRequired = true, displayOrder = 8),
            FormField("experience", "Experience (Years)", "e.g. 5", FormFieldType.NUMBER, isRequired = true, displayOrder = 9),
            FormField("aboutMe", "About Me", "Tell us about your artistic journey...", FormFieldType.MULTILINE_TEXT, isRequired = true, displayOrder = 10),
            FormField("photoUrl", "Profile Photo", "", FormFieldType.PHOTO_UPLOAD, isRequired = true, displayOrder = 11)
        )
        _formFields.value = defaultFields
        
        if (_formState.value.isEmpty()) {
            val initialState = mutableMapOf<String, Any>()
            defaultFields.forEach { field ->
                val prefilledValue = when(field.id) {
                    "name" -> profile?.name
                    "email" -> profile?.email
                    "phone" -> profile?.phone
                    "whatsapp" -> profile?.whatsapp
                    "category" -> {
                        if (!profile?.categoryId.isNullOrEmpty()) {
                            profile?.categoryId
                        } else {
                            // Find ID by name if categoryId is missing
                            _categories.value.find { it.name.equals(profile?.category, ignoreCase = true) }?.id ?: profile?.category
                        }
                    }
                    "state" -> profile?.state
                    "district" -> profile?.district
                    "city" -> profile?.city
                    "aboutMe" -> profile?.aboutMe
                    "photoUrl" -> profile?.photoUrl
                    else -> null
                }
                initialState[field.id] = prefilledValue ?: field.defaultValue
            }
            _formState.value = initialState
        }
    }

    fun onFieldChange(fieldId: String, value: Any) {
        if (fieldId == "retry") {
            loadConfig()
            return
        }

        val currentState = _formState.value.toMutableMap()
        currentState[fieldId] = value
        _formState.value = currentState
        
        // Clear error when field changes
        if (_errors.value.containsKey(fieldId)) {
            val currentErrors = _errors.value.toMutableMap()
            currentErrors.remove(fieldId)
            _errors.value = currentErrors
        }
    }

    fun uploadFile(fieldId: String, uri: Uri, type: FormFieldType) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val finalUri = if (type == FormFieldType.PHOTO_UPLOAD || type == FormFieldType.MULTIPLE_PHOTOS) {
                ImageUtils.compressImage(getApplication(), uri) ?: uri
            } else {
                uri
            }

            val (folder, resourceType) = when(type) {
                FormFieldType.PHOTO_UPLOAD, FormFieldType.MULTIPLE_PHOTOS -> "artist_photos" to "image"
                FormFieldType.VIDEO_UPLOAD -> "artist_videos" to "video"
                FormFieldType.AUDIO_UPLOAD -> "artist_audio" to "video"
                else -> "artist_files" to "auto"
            }
            val fileName = "reg_${UUID.randomUUID()}"
            
            storageRepository.uploadMedia(finalUri, folder, fileName, resourceType).fold(
                onSuccess = { resource ->
                    if (type == FormFieldType.MULTIPLE_PHOTOS) {
                        val currentList = (_formState.value[fieldId] as? List<String>) ?: emptyList()
                        onFieldChange(fieldId, currentList + resource.url)
                    } else {
                        onFieldChange(fieldId, resource.url)
                    }
                    _isLoading.value = false
                },
                onFailure = {
                    _errors.value = _errors.value + (fieldId to "Upload failed: ${it.message}")
                    _isLoading.value = false
                }
            )
        }
    }

    fun submitRegistration() {
        if (_isSubmitted.value) return

        viewModelScope.launch {
            _isLoading.value = true
            val validationErrors = validateForm()
            if (validationErrors.isNotEmpty()) {
                _errors.value = validationErrors
                _registrationResult.emit(Result.failure(Exception("Please fix the errors")))
                _isLoading.value = false
                return@launch
            }

            try {
                val uid = userRepository.getCurrentUserId() ?: throw Exception("User not authenticated")
                val currentState = _formState.value
                val regSettings = _registrationSettings.value

                val updates = currentState.toMutableMap()
                
                // Ensure categoryId and category (name) are both set correctly
                val selectedCatId = currentState["category"] as? String ?: ""
                val categoryObj = _categories.value.find { it.id == selectedCatId }
                if (categoryObj != null) {
                    updates["categoryId"] = categoryObj.id
                    updates["category"] = categoryObj.name
                }
                
                updates["profileCompleted"] = true
                updates["registrationCompleted"] = true
                updates["updatedAt"] = System.currentTimeMillis()
                updates["registrationStatus"] = "COMPLETED"
                
                if (regSettings.autoApproval) {
                    updates["approvalStatus"] = "APPROVED"
                    updates["status"] = "ACTIVE"
                    updates["isVerified"] = true
                } else {
                    updates["approvalStatus"] = "PENDING"
                    updates["status"] = "PENDING"
                }

                if (!updates.containsKey("userType")) {
                    updates["userType"] = "Artist"
                    updates["role"] = "artist"
                } else {
                    updates["role"] = (updates["userType"] as String).lowercase()
                }

                withTimeout(45000L) {
                    userRepository.updateProfileFields(uid, updates)
                        .onSuccess {
                            logRepository.logAction("User $uid completed dynamic registration")
                            _isSubmitted.value = true
                            _registrationResult.emit(Result.success("Registration submitted successfully!"))
                        }
                        .onFailure { throw it }
                }

            } catch (e: TimeoutCancellationException) {
                _registrationResult.emit(Result.failure(Exception("Submission timed out. Please check your internet connection.")))
            } catch (e: Exception) {
                _registrationResult.emit(Result.failure(e))
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateForm(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val currentState = _formState.value
        val selectedCatId = currentState["category"] as? String ?: ""
        val categoryObj = _categories.value.find { it.id == selectedCatId }
        val selectedCatName = categoryObj?.name ?: selectedCatId

        _formFields.value.forEach { field ->
            val isApplicable = field.categoryFilter.isEmpty() || 
                              field.categoryFilter.contains(selectedCatId) || 
                              field.categoryFilter.contains(selectedCatName)
            
            if (field.isEnabled && isApplicable) {
                val value = currentState[field.id]
                
                if (field.isRequired && (value == null || (value is String && value.isBlank()) || (value is List<*> && value.isEmpty()))) {
                    errors[field.id] = "${field.label} is required"
                }
                
                if (value is String && value.isNotBlank()) {
                    when(field.type) {
                        FormFieldType.MOBILE -> {
                            if (!Regex("^[6-9]\\d{9}$").matches(value)) {
                                errors[field.id] = "Enter valid 10-digit mobile number"
                            }
                        }
                        FormFieldType.EMAIL -> {
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                                errors[field.id] = "Enter valid email address"
                            }
                        }
                        FormFieldType.NUMBER -> {
                            if (value.toDoubleOrNull() == null) {
                                errors[field.id] = "Please enter a valid number"
                            }
                        }
                        else -> {}
                    }

                    if (field.id == "whatsapp" && !Regex("^[6-9]\\d{9}$").matches(value)) {
                        errors[field.id] = "Enter valid WhatsApp number"
                    }
                    
                    if (field.validationRegex != null && field.validationRegex.isNotEmpty()) {
                        if (!Regex(field.validationRegex).matches(value)) {
                            errors[field.id] = field.errorMessage ?: "Invalid format"
                        }
                    }
                }
            }
        }
        return errors
    }
}
