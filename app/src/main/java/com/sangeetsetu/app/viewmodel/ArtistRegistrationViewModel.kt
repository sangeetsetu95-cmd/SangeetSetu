package com.sangeetsetu.app.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.repository.MetadataRepository
import com.sangeetsetu.app.repository.StorageRepository
import com.sangeetsetu.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.sangeetsetu.app.R

enum class RegistrationStep(val titleResId: Int) {
    BASIC_INFO(R.string.basic_info),
    ARTIST_DETAILS(R.string.artist_details),
    DOCUMENTS(R.string.documents),
    REVIEW(R.string.review_submit)
}

data class RegistrationForm(
    val name: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val category: String = "",
    val speciality: String = "",
    val state: String = "",
    val district: String = "",
    val experience: String = "",
    val language: List<String> = emptyList(),
    val aboutMe: String = "",
    val profilePhotoUri: Uri? = null,
    val idProofUri: Uri? = null,
    val photoUrl: String = "",
    val idProofUrl: String = ""
)

class ArtistRegistrationViewModel : ViewModel() {
    private val userRepository = UserRepository
    private val metadataRepository = MetadataRepository
    private val storageRepository = StorageRepository

    private val _currentStep = MutableStateFlow(RegistrationStep.BASIC_INFO)
    val currentStep: StateFlow<RegistrationStep> = _currentStep.asStateFlow()

    private val _form = MutableStateFlow(RegistrationForm())
    val form: StateFlow<RegistrationForm> = _form.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Metadata flows
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _specialities = MutableStateFlow<List<String>>(emptyList())
    val specialities: StateFlow<List<String>> = _specialities.asStateFlow()

    private val _states = MutableStateFlow<List<String>>(emptyList())
    val states: StateFlow<List<String>> = _states.asStateFlow()

    private val _districts = MutableStateFlow<List<String>>(emptyList())
    val districts: StateFlow<List<String>> = _districts.asStateFlow()

    private val _languages = MutableStateFlow<List<String>>(emptyList())
    val languages: StateFlow<List<String>> = _languages.asStateFlow()

    private val _experienceLevels = MutableStateFlow<List<String>>(emptyList())
    val experienceLevels: StateFlow<List<String>> = _experienceLevels.asStateFlow()

    init {
        fetchMetadata()
        loadExistingProfile()
    }

    private fun fetchMetadata() {
        viewModelScope.launch {
            try {
                _categories.value = metadataRepository.getCategories()
                _specialities.value = metadataRepository.getSpecialities()
                _states.value = metadataRepository.getStates()
                _languages.value = metadataRepository.getLanguages()
                _experienceLevels.value = metadataRepository.getExperienceLevels()
            } catch (e: Exception) {
                Log.e("ArtistRegVM", "Metadata fetch failed", e)
                _error.value = "Failed to load registration options. Please check your connection."
            }
        }
    }

    private fun loadExistingProfile() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserProfile(uid).onSuccess { user ->
                user?.let {
                    _form.value = _form.value.copy(
                        name = it.name,
                        email = it.email,
                        phone = it.phone,
                        whatsapp = it.whatsapp,
                        category = it.category,
                        speciality = it.skill,
                        state = it.state,
                        district = it.district,
                        experience = it.experience,
                        language = it.languages,
                        aboutMe = it.aboutMe,
                        photoUrl = it.photoUrl,
                        idProofUrl = it.idProofUrl
                    )
                    if (it.state.isNotEmpty()) {
                        fetchDistricts(it.state)
                    }
                }
            }
        }
    }

    fun updateForm(updater: (RegistrationForm) -> RegistrationForm) {
        _form.value = updater(_form.value)
        if (_form.value.state.isNotEmpty()) {
            fetchDistricts(_form.value.state)
        }
    }

    private fun fetchDistricts(state: String) {
        viewModelScope.launch {
            _districts.value = metadataRepository.getDistricts(state)
        }
    }

    fun nextStep() {
        val steps = RegistrationStep.entries
        val nextIndex = _currentStep.value.ordinal + 1
        if (nextIndex < steps.size) {
            _currentStep.value = steps[nextIndex]
        }
    }

    fun previousStep() {
        val steps = RegistrationStep.entries
        val prevIndex = _currentStep.value.ordinal - 1
        if (prevIndex >= 0) {
            _currentStep.value = steps[prevIndex]
        }
    }

    fun submitRegistration(onSuccess: () -> Unit) {
        val uid = userRepository.getCurrentUserId()
        if (uid == null) {
            _error.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Upload Profile Photo if changed
                var finalPhotoUrl = _form.value.photoUrl
                _form.value.profilePhotoUri?.let { uri ->
                    val result = storageRepository.uploadImage(uri, "artist_profiles", "profile_$uid")
                    if (result.isSuccess) {
                        finalPhotoUrl = result.getOrThrow().url
                    } else {
                        throw result.exceptionOrNull() ?: Exception("Photo upload failed")
                    }
                }

                // 2. Upload ID Proof if changed
                var finalIdProofUrl = _form.value.idProofUrl
                _form.value.idProofUri?.let { uri ->
                    val result = storageRepository.uploadImage(uri, "artist_documents", "id_proof_$uid")
                    if (result.isSuccess) {
                        finalIdProofUrl = result.getOrThrow().url
                    } else {
                        throw result.exceptionOrNull() ?: Exception("ID Proof upload failed")
                    }
                }

                // 3. Generate Artist ID if not already present
                val currentProfile = userRepository.getUserProfile(uid).getOrNull()
                val artistId = if (currentProfile?.artistId.isNullOrEmpty()) {
                    userRepository.getNextArtistId()
                } else {
                    currentProfile!!.artistId
                }

                // 4. Save to Firestore
                val user = User(
                    uid = uid,
                    name = _form.value.name,
                    email = _form.value.email,
                    phone = _form.value.phone,
                    artistId = artistId,
                    whatsapp = _form.value.whatsapp,
                    category = _form.value.category,
                    skill = _form.value.speciality,
                    state = _form.value.state,
                    district = _form.value.district,
                    experience = _form.value.experience,
                    languages = _form.value.language,
                    aboutMe = _form.value.aboutMe,
                    photoUrl = finalPhotoUrl,
                    idProofUrl = finalIdProofUrl,
                    userType = "Artist",
                    role = "artist",
                    status = "active",
                    approvalStatus = "PENDING",
                    registrationCompleted = true,
                    profileCompleted = true
                )

                userRepository.saveUserProfile(user).onSuccess {
                    onSuccess()
                }.onFailure {
                    _error.value = "Failed to save profile: ${it.message}"
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
