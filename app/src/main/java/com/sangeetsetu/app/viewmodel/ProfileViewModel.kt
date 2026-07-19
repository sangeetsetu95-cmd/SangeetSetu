package com.sangeetsetu.app.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IStorageRepository
import com.sangeetsetu.app.domain.repository.ISystemLogRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val storageRepository: IStorageRepository,
    private val logRepository: ISystemLogRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var profileListener: ListenerRegistration? = null

    init {
        startProfileSync()
    }

    fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    private fun startProfileSync() {
        val uid = userRepository.getCurrentUserId() ?: return
        profileListener?.remove()
        
        profileListener = userRepository.listenToUserProfile(uid) { profile ->
            _userProfile.value = profile
            Log.d("ProfileVM", "Profile synced from Firestore. Photo: ${profile?.photoUrl}")
        }
    }

    fun loadProfile() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.getUserProfile(uid).onSuccess {
                _userProfile.value = it
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(updatedUser: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            Log.d("ProfileVM", "updateProfile() called for user: ${updatedUser.uid}")
            _isLoading.value = true
            _error.value = null
            
            try {
                val uid = updatedUser.uid.ifEmpty { getCurrentUserId() ?: "" }
                if (uid.isEmpty()) {
                    Log.w("ProfileVM", "Abort update: No UID found.")
                    _error.value = "User not authenticated"
                    return@launch
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to updatedUser.name,
                    "phone" to updatedUser.phone,
                    "mobile" to updatedUser.phone, // Added as requested
                    "state" to updatedUser.state,
                    "district" to updatedUser.district,
                    "photoUrl" to updatedUser.photoUrl,
                    "profileCompleted" to true,
                    "updatedAt" to System.currentTimeMillis(),
                    "registrationStatus" to "PROFILE_COMPLETED"
                )

                if (updatedUser.category.isNotEmpty()) {
                    updates["category"] = updatedUser.category
                    updates["artistCategory"] = updatedUser.category // Added as requested
                }

                if (updatedUser.userType.isNotEmpty()) {
                    updates["userType"] = updatedUser.userType
                    updates["role"] = updatedUser.userType.lowercase()
                }

                withTimeout(40000L) { // 40 second safety timeout
                    userRepository.updateProfileFields(uid, updates)
                        .onSuccess {
                            logRepository.logAction("User $uid completed profile setup")
                            onSuccess()
                        }
                        .onFailure { e ->
                            Log.e("ProfileVM", "Update failed", e)
                            _error.value = "Update failed: ${e.localizedMessage}"
                        }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e("ProfileVM", "Update timed out")
                _error.value = "Connection timeout. Please try again."
            } catch (e: Exception) {
                Log.e("ProfileVM", "Unexpected error", e)
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri, onSuccess: (String) -> Unit) {
        val uid = userRepository.getCurrentUserId() ?: return
        val oldPublicId = _userProfile.value?.cloudinaryPublicId
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                storageRepository.uploadImage(uri, "profile_photos/$uid", "profile_${System.currentTimeMillis()}.jpg")
                    .onSuccess { resource ->
                        val updates = mapOf(
                            "photoUrl" to resource.url,
                            "cloudinaryPublicId" to resource.publicId
                        )
                        
                        userRepository.updateProfileFields(uid, updates)
                            .onSuccess {
                                logRepository.logAction("User $uid updated profile photo")
                                onSuccess(resource.url)

                                if (!oldPublicId.isNullOrEmpty()) {
                                    storageRepository.deleteImage(oldPublicId)
                                }
                            }
                            .onFailure { e ->
                                _error.value = "Photo uploaded, but database update failed: ${e.localizedMessage}"
                            }
                    }
                    .onFailure { e ->
                        _error.value = "Cloudinary upload failed: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeProfilePhoto() {
        val user = _userProfile.value ?: return
        val publicId = user.cloudinaryPublicId
        
        viewModelScope.launch {
            _isLoading.value = true
            val updatedUser = user.copy(photoUrl = "", cloudinaryPublicId = "")
            
            userRepository.saveUserProfile(updatedUser)
                .onSuccess {
                    logRepository.logAction("User ${user.uid} removed profile photo")
                    if (!publicId.isNullOrEmpty()) {
                        storageRepository.deleteImage(publicId)
                    }
                }
            _isLoading.value = false
        }
    }

    fun completeProfile(
        name: String,
        phone: String,
        state: String,
        district: String,
        userType: String,
        organizationName: String = "",
        categories: List<String> = emptyList(),
        experience: String = "",
        whatsapp: String = "",
        aboutMe: String = "",
        photoUri: Uri? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val uid = getCurrentUserId() ?: throw Exception("User not authenticated")
                var finalPhotoUrl = _userProfile.value?.photoUrl ?: ""
                var finalPublicId = _userProfile.value?.cloudinaryPublicId ?: ""

                if (photoUri != null) {
                    val uploadResult = storageRepository.uploadImage(
                        photoUri, 
                        "profile_photos/$uid", 
                        "profile_${System.currentTimeMillis()}.jpg"
                    )
                    
                    if (uploadResult.isSuccess) {
                        val resource = uploadResult.getOrThrow()
                        finalPhotoUrl = resource.url
                        finalPublicId = resource.publicId
                        
                        val oldPublicId = _userProfile.value?.cloudinaryPublicId
                        if (!oldPublicId.isNullOrEmpty()) {
                            storageRepository.deleteImage(oldPublicId)
                        }
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Photo upload failed")
                    }
                }

                val updates = mutableMapOf<String, Any>(
                    "name" to name,
                    "phone" to phone,
                    "state" to state,
                    "district" to district,
                    "userType" to userType,
                    "organizationName" to organizationName,
                    "performanceTypes" to categories,
                    "experience" to experience,
                    "whatsapp" to whatsapp,
                    "aboutMe" to aboutMe,
                    "photoUrl" to finalPhotoUrl,
                    "cloudinaryPublicId" to finalPublicId,
                    "profileCompleted" to true,
                    "role" to userType.lowercase(),
                    "updatedAt" to System.currentTimeMillis(),
                    "registrationStatus" to "PROFILE_COMPLETED"
                )

                if (userType == "Artist" && categories.isNotEmpty()) {
                    updates["category"] = categories.first()
                    updates["artistCategory"] = categories.first()
                }

                withTimeout(60000L) { // 60s for full profile (includes potential large updates)
                    userRepository.updateProfileFields(uid, updates)
                        .onSuccess {
                            logRepository.logAction("User $uid completed premium profile setup as $userType")
                            onSuccess()
                        }
                        .onFailure { throw it }
                }

            } catch (e: TimeoutCancellationException) {
                _error.value = "Connection timeout. Profile may have been saved partially."
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        profileListener?.remove()
        userRepository.logout()
        _userProfile.value = null
    }

    fun updatePresencePrivacy(privacy: String) {
        val uid = getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.updateProfileFields(uid, mapOf("presencePrivacy" to privacy))
                .onSuccess {
                    logRepository.logAction("User $uid updated presence privacy to $privacy")
                }
                .onFailure { e ->
                    _error.value = "Privacy update failed: ${e.localizedMessage}"
                }
        }
    }

    fun updateNotificationSettings(settings: com.sangeetsetu.app.model.NotificationSettings) {
        val uid = getCurrentUserId() ?: return
        viewModelScope.launch {
            // Firestore can update nested objects by dot notation or by sending the whole object
            userRepository.updateProfileFields(uid, mapOf("notificationSettings" to settings))
                .onSuccess {
                    logRepository.logAction("User $uid updated notification settings")
                }
                .onFailure { e ->
                    _error.value = "Settings update failed: ${e.localizedMessage}"
                }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val uid = getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.updateProfileFields(uid, mapOf("status" to "deleted"))
                    .onSuccess {
                        logRepository.logAction("User $uid marked account for deletion")
                        auth.currentUser?.delete()?.addOnCompleteListener { 
                            if (it.isSuccessful) onSuccess()
                        }
                    }
                    .onFailure { e ->
                        _error.value = "Deletion failed: ${e.localizedMessage}"
                    }
            } catch (e: Exception) {
                _error.value = "Account deletion requires recent login. Please logout and login again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }

    fun clearError() {
        _error.value = null
    }
}
