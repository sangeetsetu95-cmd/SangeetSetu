package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IMainRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.BadgeConfig
import com.sangeetsetu.app.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val mainRepository: IMainRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userPermissions = MutableStateFlow<List<String>>(emptyList())
    val userPermissions: StateFlow<List<String>> = _userPermissions.asStateFlow()

    private val _verifiedBadge = MutableStateFlow<BadgeConfig?>(null)
    val verifiedBadge: StateFlow<BadgeConfig?> = _verifiedBadge.asStateFlow()

    private val _vipBadge = MutableStateFlow<BadgeConfig?>(null)
    val vipBadge: StateFlow<BadgeConfig?> = _vipBadge.asStateFlow()

    companion object {
        const val ADMIN_EMAIL = "sadhvi.rashmi9119@gmail.com"
    }
    
    val adminEmail = ADMIN_EMAIL

    private var userListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        listenToBadges()
        startAuthSync()
    }

    private fun listenToBadges() {
        viewModelScope.launch {
            mainRepository.getBadgesFlow().collect { badges ->
                _verifiedBadge.value = badges.find { it.id == "verified" }
                _vipBadge.value = badges.find { it.id == "vip" }
            }
        }
    }

    private fun startAuthSync() {
        val uid = userRepository.getCurrentUserId()
        Log.d("StartupTrace", "startAuthSync() - UID: $uid")
        
        if (uid != null) {
            _authState.value = AuthState.Loading
            userListener?.remove()
            userListener = userRepository.listenToUserProfile(uid) { user ->
                Log.d("StartupTrace", "Auth sync update received for: $uid")
                if (user == null) {
                    _authState.value = AuthState.ProfileIncomplete
                } else {
                    _currentUser.value = user
                    processUserRole(user)
                }
            }
        } else {
            _authState.value = AuthState.Unauthenticated
            _currentUser.value = null
        }
    }

    private fun processUserRole(user: User) {
        fetchPermissions(user)
        
        when {
            user.email == ADMIN_EMAIL || user.role == "admin" -> {
                _authState.value = AuthState.AuthenticatedAdmin
            }
            user.role == "organizer" || user.userType == "Organizer" -> {
                _authState.value = AuthState.AuthenticatedOrganizer
            }
            user.userType == "Artist" || user.registrationCompleted || user.role == "artist" -> {
                _authState.value = AuthState.AuthenticatedArtist
            }
            user.profileCompleted -> {
                _authState.value = AuthState.AuthenticatedUser
            }
            else -> {
                _authState.value = AuthState.ProfileIncomplete
            }
        }
    }

    fun checkAuthState() {
        startAuthSync()
    }

    private fun fetchPermissions(user: User) {
        viewModelScope.launch {
            val roleName = when {
                user.email == ADMIN_EMAIL || user.role == "admin" -> "admin"
                user.role == "organizer" || user.userType == "Organizer" -> "organizer"
                user.userType == "Artist" || user.registrationCompleted || user.role == "artist" -> "artist"
                else -> "user"
            }
            
            userRepository.getRole(roleName).onSuccess { role ->
                _userPermissions.value = role?.permissions ?: emptyList()
                Log.d("RBAC", "Permissions loaded for $roleName: ${_userPermissions.value}")
            }
        }
    }

    fun hasPermission(permission: String): Boolean {
        return _userPermissions.value.contains(permission) || _authState.value is AuthState.AuthenticatedAdmin
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    object AuthenticatedUser : AuthState()
    object AuthenticatedArtist : AuthState()
    object AuthenticatedOrganizer : AuthState()
    object AuthenticatedAdmin : AuthState()
    object ProfileIncomplete : AuthState()
    data class Error(val message: String) : AuthState()
}
