package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IMainRepository
import com.sangeetsetu.app.domain.repository.INotificationRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.util.FirestoreAudit
import com.sangeetsetu.app.util.RefreshSignal
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: IUserRepository,
    private val mainRepository: IMainRepository,
    private val notificationRepository: INotificationRepository,
    private val configRepository: com.sangeetsetu.app.domain.repository.IConfigRepository
) : ViewModel() {

    private val _artists = MutableStateFlow<List<User>>(emptyList())
    val artists: StateFlow<List<User>> = _artists

    private val _verifiedArtists = MutableStateFlow<List<User>>(emptyList())
    val verifiedArtists: StateFlow<List<User>> = _verifiedArtists

    private val _vipArtists = MutableStateFlow<List<User>>(emptyList())
    val vipArtists: StateFlow<List<User>> = _vipArtists

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _banners = MutableStateFlow<List<com.sangeetsetu.app.model.Banner>>(emptyList())
    val banners: StateFlow<List<com.sangeetsetu.app.model.Banner>> = _banners

    private val _homeSections = MutableStateFlow<List<com.sangeetsetu.app.model.HomeSection>>(emptyList())
    val homeSections: StateFlow<List<com.sangeetsetu.app.model.HomeSection>> = _homeSections

    private val _systemSettings = MutableStateFlow(com.sangeetsetu.app.model.SystemSettings())
    val systemSettings: StateFlow<com.sangeetsetu.app.model.SystemSettings> = _systemSettings

    private val _navigationItems = MutableStateFlow<List<com.sangeetsetu.app.model.NavigationItem>>(emptyList())
    val navigationItems: StateFlow<List<com.sangeetsetu.app.model.NavigationItem>> = _navigationItems

    private val _popUps = MutableStateFlow<List<com.sangeetsetu.app.model.PopUpConfig>>(emptyList())
    val popUps: StateFlow<List<com.sangeetsetu.app.model.PopUpConfig>> = _popUps

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData: StateFlow<User?> = _currentUserData

    private val _favoriteArtists = MutableStateFlow<List<User>>(emptyList())
    val favoriteArtists: StateFlow<List<User>> = _favoriteArtists

    private var artistCollectionJob: kotlinx.coroutines.Job? = null
    private var favoriteArtistsJob: kotlinx.coroutines.Job? = null

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount

    private val _announcements = MutableStateFlow<List<com.sangeetsetu.app.model.Announcement>>(emptyList())
    val announcements: StateFlow<List<com.sangeetsetu.app.model.Announcement>> = _announcements

    private val _unreadAnnouncementsCount = MutableStateFlow(0)
    val unreadAnnouncementsCount: StateFlow<Int> = _unreadAnnouncementsCount

    private val _reviews = MutableStateFlow<List<com.sangeetsetu.app.model.Review>>(emptyList())
    val reviews: StateFlow<List<com.sangeetsetu.app.model.Review>> = _reviews

    private var userProfileListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    private val prefs = db.app.applicationContext.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            try {
                RefreshSignal.refreshEvent.collect { refreshAllData() }
            } catch (e: Exception) { Log.e("UserViewModel", "Refresh signal error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getVipArtistsFlow().collect { _vipArtists.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "VIP Artists flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getCategoriesFlow().collect { _categories.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "Categories flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getBannersFlow().collect { _banners.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "Banners flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getHomeSectionsFlow().collect { _homeSections.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "HomeSections flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getReviewsFlow().collect { _reviews.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "Reviews flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getSystemSettingsFlow().collect { _systemSettings.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "SystemSettings flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getNavigationItemsFlow().collect { _navigationItems.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "NavItems flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getPopUpsFlow().collect { _popUps.value = it }
            } catch (e: Exception) { Log.e("UserViewModel", "Popups flow error", e) }
        }

        viewModelScope.launch {
            try {
                mainRepository.getAnnouncementsFlow().collect { list ->
                    Log.d("UserInbox", "Received ${list.size} announcements in UserViewModel")
                    _announcements.value = list
                    // Removed separate unread count for announcements as it's unified with chats
                }
            } catch (e: Exception) { 
                Log.e("UserInbox", "Announcements flow error", e) 
            }
        }

        try {
            val uid = userRepository.getCurrentUserId()
            if (uid != null) {
                userProfileListener = userRepository.listenToUserProfile(uid) { user ->
                    _currentUserData.value = user
                    user?.let {
                        fetchArtists("All", it.state, it.district)
                        fetchFavoriteArtists(it.favorites)
                    }
                }
                
                viewModelScope.launch {
                    try {
                        com.sangeetsetu.app.repository.ChatRepository.getTotalUnreadCountFlow(uid).collect {
                            _unreadAnnouncementsCount.value = it
                        }
                    } catch (e: Exception) { Log.e("UserViewModel", "Chat unread count error", e) }
                }

                viewModelScope.launch {
                    try {
                        notificationRepository.getUnreadCountFlow().collect {
                            _unreadNotificationsCount.value = it
                        }
                    } catch (e: Exception) { Log.e("UserViewModel", "Notifications count error", e) }
                }
            } else {
                fetchArtists("All")
            }
        } catch (e: Exception) { Log.e("UserViewModel", "User init error", e) }
    }

    fun markAnnouncementsAsRead() {
        val latestTime = _announcements.value.maxOfOrNull { it.createdAt } ?: 0L
        if (latestTime > 0) {
            prefs.edit().putLong("last_read_announcement_time", latestTime).apply()
            _unreadAnnouncementsCount.value = 0
        }
        
        // Update read count in Firestore for all visible announcements that haven't been read by this user before in this session
        viewModelScope.launch {
            _announcements.value.forEach { announcement ->
                try {
                    db.collection("announcements").document(announcement.id)
                        .update("readCount", com.google.firebase.firestore.FieldValue.increment(1))
                } catch (e: Exception) {
                    // Ignore error for read count update
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        userProfileListener?.remove()
        artistCollectionJob?.cancel()
        favoriteArtistsJob?.cancel()
    }

    fun fetchFavoriteArtists(ids: List<String>) {
        favoriteArtistsJob?.cancel()
        if (ids.isEmpty()) {
            _favoriteArtists.value = emptyList()
            return
        }
        favoriteArtistsJob = viewModelScope.launch {
            try {
                mainRepository.getArtistsByIdsFlow(ids).collect {
                    _favoriteArtists.value = it
                }
            } catch (e: Exception) { Log.e("UserViewModel", "fetchFavoriteArtists error", e) }
        }
    }

    fun fetchCurrentUser() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            userRepository.getUserProfile(uid)
                .onSuccess { user ->
                    _currentUserData.value = user
                    user?.let { fetchArtists("All", it.state, it.district) }
                }
                .onFailure { it.printStackTrace() }
        }
    }

    fun fetchArtists(categoryId: String = "All", state: String? = null, district: String? = null) {
        artistCollectionJob?.cancel()
        artistCollectionJob = viewModelScope.launch {
            try {
                mainRepository.getArtistsFlow(categoryId, state, district).collect { artists -> 
                    _artists.value = artists 
                    _verifiedArtists.value = artists.filter { a -> a.verificationStatus == "VERIFIED" }.take(5)
                }
            } catch (e: Exception) { Log.e("UserViewModel", "fetchArtists error", e) }
        }
    }

    fun toggleFavorite(artistId: String) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid)
                val user = userDoc.get().await().toObject(User::class.java) ?: return@launch
                val updatedFavorites = user.favorites.toMutableList()
                if (updatedFavorites.contains(artistId)) updatedFavorites.remove(artistId)
                else updatedFavorites.add(artistId)
                
                FirestoreAudit.verifiedWrite("users", uid) {
                    userDoc.update("favorites", updatedFavorites).await()
                }
            } catch (e: Exception) { Log.e("BackendAudit", "toggleFavorite failed", e) }
        }
    }

    fun claimReferralReward() {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid)
                FirestoreAudit.verifiedWrite("users", uid) {
                    userDoc.update(mapOf("isPremium" to true, "rewardClaimed" to true)).await()
                }
            } catch (e: Exception) { Log.e("Referral", "claimReward failed", e) }
        }
    }

    fun addAddress(address: String) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid)
                val user = _currentUserData.value ?: return@launch
                val updatedAddresses = user.addresses.toMutableList().apply { add(address) }
                FirestoreAudit.verifiedWrite("users", uid) {
                    userDoc.update("addresses", updatedAddresses).await()
                }
            } catch (e: Exception) { Log.e("UserViewModel", "addAddress failed", e) }
        }
    }

    fun removeAddress(address: String) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(uid)
                val user = _currentUserData.value ?: return@launch
                val updatedAddresses = user.addresses.toMutableList().apply { remove(address) }
                FirestoreAudit.verifiedWrite("users", uid) {
                    userDoc.update("addresses", updatedAddresses).await()
                }
            } catch (e: Exception) { Log.e("UserViewModel", "removeAddress failed", e) }
        }
    }

    fun updateLocation(state: String? = null, district: String? = null) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>().apply {
                    state?.let { put("state", it) }
                    district?.let { put("district", it) }
                }
                if (updates.isNotEmpty()) userRepository.updateProfileFields(uid, updates)
            } catch (e: Exception) { Log.e("UserViewModel", "updateLocation failed", e) }
        }
    }

    fun refreshAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val categoriesResult = db.collection("categories").whereEqualTo("isActive", true).get(com.google.firebase.firestore.Source.SERVER).await()
                _categories.value = categoriesResult.toObjects(Category::class.java)

                val bannersResult = db.collection("banners").whereEqualTo("active", true).orderBy("displayOrder").get(com.google.firebase.firestore.Source.SERVER).await()
                _banners.value = bannersResult.toObjects(com.sangeetsetu.app.model.Banner::class.java).filter { it.isActive }.sortedBy { it.order }

                val currentUser = _currentUserData.value
                fetchArtists("All", currentUser?.state, currentUser?.district)
            } catch (e: Exception) { Log.e("UserViewModel", "Manual refresh failed", e) }
            finally { _isLoading.value = false }
        }
    }
}
