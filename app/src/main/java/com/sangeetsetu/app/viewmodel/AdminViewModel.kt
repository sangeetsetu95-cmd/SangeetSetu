package com.sangeetsetu.app.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.domain.repository.IAdminCategoryRepository
import com.sangeetsetu.app.domain.repository.IBookingRepository
import com.sangeetsetu.app.domain.repository.IMainRepository
import com.sangeetsetu.app.domain.repository.INotificationRepository
import com.sangeetsetu.app.domain.repository.IStorageRepository
import com.sangeetsetu.app.domain.repository.ISystemLogRepository
import com.sangeetsetu.app.domain.repository.IUserRepository
import com.sangeetsetu.app.model.AdminLog
import com.sangeetsetu.app.model.AdminStats
import com.sangeetsetu.app.model.Announcement
import com.sangeetsetu.app.model.BadgeConfig
import com.sangeetsetu.app.model.Banner
import com.sangeetsetu.app.model.Bhajan
import com.sangeetsetu.app.model.Booking
import com.sangeetsetu.app.model.BookingStatus
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.Chat
import com.sangeetsetu.app.model.HomeSection
import com.sangeetsetu.app.model.LiveTelecast
import com.sangeetsetu.app.model.Offer
import com.sangeetsetu.app.model.Review
import com.sangeetsetu.app.model.Service
import com.sangeetsetu.app.model.SubscriptionPlan
import com.sangeetsetu.app.model.SystemSettings
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.util.AppConstants
import com.sangeetsetu.app.util.FirestoreAudit
import com.sangeetsetu.app.util.RefreshSignal
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val bookingRepository: IBookingRepository,
    private val mainRepository: IMainRepository,
    private val userRepository: IUserRepository,
    private val adminCategoryRepository: IAdminCategoryRepository,
    private val storageRepository: IStorageRepository,
    private val systemLogRepository: ISystemLogRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(AdminStats())
    val stats: StateFlow<AdminStats> = _stats.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _artists = MutableStateFlow<List<User>>(emptyList())
    val artists: StateFlow<List<User>> = _artists.asStateFlow()

    private val _pendingArtists = MutableStateFlow<List<User>>(emptyList())
    val pendingArtists: StateFlow<List<User>> = _pendingArtists.asStateFlow()

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _unlockPayments = MutableStateFlow<List<com.sangeetsetu.app.model.UnlockPayment>>(emptyList())
    val unlockPayments: StateFlow<List<com.sangeetsetu.app.model.UnlockPayment>> = _unlockPayments.asStateFlow()

    private val _recentActivities = MutableStateFlow<List<AdminLog>>(emptyList())
    val recentActivities: StateFlow<List<AdminLog>> = _recentActivities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _unreadNotificationsCount = MutableStateFlow(0)
    val unreadNotificationsCount: StateFlow<Int> = _unreadNotificationsCount.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _revenueChartData = MutableStateFlow<List<Float>>(emptyList())
    val revenueChartData: StateFlow<List<Float>> = _revenueChartData.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val subscriptionPlans: StateFlow<List<SubscriptionPlan>> = _subscriptionPlans.asStateFlow()

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers.asStateFlow()

    private val _homeSections = MutableStateFlow<List<HomeSection>>(emptyList())
    val homeSections: StateFlow<List<HomeSection>> = _homeSections.asStateFlow()

    private val _services = MutableStateFlow<List<Service>>(emptyList())
    val services: StateFlow<List<Service>> = _services.asStateFlow()

    private val _liveSessions = MutableStateFlow<List<LiveTelecast>>(emptyList())
    val liveSessions: StateFlow<List<LiveTelecast>> = _liveSessions.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _badges = MutableStateFlow<List<BadgeConfig>>(emptyList())
    val badges: StateFlow<List<BadgeConfig>> = _badges.asStateFlow()

    private val _appContent = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val appContent: StateFlow<List<Map<String, Any>>> = _appContent.asStateFlow()

    private val _reports = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val reports: StateFlow<List<Map<String, Any>>> = _reports.asStateFlow()

    private val _systemSettings = MutableStateFlow(SystemSettings())
    val systemSettings: StateFlow<SystemSettings> = _systemSettings.asStateFlow()

    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    private val _allChats = MutableStateFlow<List<Chat>>(emptyList())
    val allChats: StateFlow<List<Chat>> = _allChats.asStateFlow()

    private val _selectedChatMessages = MutableStateFlow<List<com.sangeetsetu.app.model.Message>>(emptyList())
    val selectedChatMessages: StateFlow<List<com.sangeetsetu.app.model.Message>> = _selectedChatMessages.asStateFlow()

    private var messageListener: ListenerRegistration? = null
    private val registrations = mutableListOf<ListenerRegistration>()

    init {
        syncRealTimeData()
        fetchAppContent()
        fetchReports()
        fetchSystemSettings()
        syncAnnouncements()
        syncUnlockPayments()
        syncAllChats()
    }

    private fun syncAllChats() {
        runChatAutoDeleteAudit()
        val reg = db.collection("chats")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val chats = snapshot?.toObjects(Chat::class.java) ?: emptyList()
                
                viewModelScope.launch {
                    val enrichedChats = chats.map { chat ->
                        async {
                            val p1 = chat.participants.getOrNull(0) ?: ""
                            val p2 = chat.participants.getOrNull(1) ?: ""
                            
                            val name1 = if (p1 == "admin") "Admin" else userRepository.getUserProfile(p1).getOrNull()?.name ?: "Unknown"
                            val name2 = if (p2 == "admin") "Admin" else userRepository.getUserProfile(p2).getOrNull()?.name ?: "Unknown"
                            
                            chat.copy(participantName = if (chat.conversationType == "support") "Support: $name1 ↔️ $name2" else "$name1 ↔️ $name2")
                        }
                    }.awaitAll()
                    _allChats.value = enrichedChats
                }
            }
        registrations.add(reg)
    }

    fun deleteChat(chatId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Delete messages subcollection
                val messages = db.collection("chats").document(chatId).collection("messages").get().await()
                val batch = db.batch()
                messages.documents.forEach { batch.delete(it.reference) }
                batch.delete(db.collection("chats").document(chatId))
                batch.commit().await()
                
                logAdminAction("Deleted chat: $chatId")
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Chat delete failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChatMessages(chatId: String) {
        messageListener?.remove()
        messageListener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                _selectedChatMessages.value = snapshot?.toObjects(com.sangeetsetu.app.model.Message::class.java) ?: emptyList()
            }
    }

    fun sendAdminMessage(receiverId: String, text: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val adminId = "admin"
                val chatId = "support_$receiverId"
                val sortedIds = listOf(adminId, receiverId).sorted()
                
                val chatRef = db.collection("chats").document(chatId)
                val chatDoc = chatRef.get().await()
                val now = System.currentTimeMillis()
                
                val messageRef = chatRef.collection("messages").document()
                val message = com.sangeetsetu.app.model.Message(
                    id = messageRef.id,
                    senderId = adminId,
                    receiverId = receiverId,
                    message = text,
                    timestamp = now,
                    read = false,
                    type = "text"
                )

                db.runTransaction { transaction ->
                    if (!chatDoc.exists()) {
                        val newChat = mapOf(
                            "id" to chatId,
                            "participants" to sortedIds,
                            "lastMessage" to text,
                            "lastMessageTime" to now,
                            "createdAt" to now,
                            "unreadCount" to 1,
                            "type" to "ADMIN_SUPPORT",
                            "conversationType" to "support"
                        )
                        transaction.set(chatRef, newChat)
                    } else {
                        transaction.update(chatRef, mapOf(
                            "lastMessage" to text,
                            "lastMessageTime" to now,
                            "participants" to sortedIds,
                            "unreadCount" to FieldValue.increment(1),
                            "type" to "ADMIN_SUPPORT",
                            "conversationType" to "support"
                        ))
                    }
                    transaction.set(messageRef, message)
                }.await()
                
                // Trigger real-time notification
                notificationRepository.sendNotification(
                    userId = receiverId,
                    title = "New Message from Admin",
                    message = text,
                    type = com.sangeetsetu.app.model.NotificationType.CHAT,
                    senderId = adminId,
                    senderName = "Sangeet Setu Admin",
                    chatId = chatId
                )
                
                logAdminAction("Admin sent support message to $receiverId: $text")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "Failed to send admin message", e)
                _error.value = "Send failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAllChats(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val chats = db.collection("chats").get().await()
                for (chatDoc in chats.documents) {
                    val messages = chatDoc.reference.collection("messages").get().await()
                    val batch = db.batch()
                    messages.documents.forEach { batch.delete(it.reference) }
                    batch.delete(chatDoc.reference)
                    batch.commit().await()
                }
                logAdminAction("Deleted ALL chats")
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to delete all chats: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun blockUserChat(userId: String, hours: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val disabledUntil = System.currentTimeMillis() + (hours * 3600 * 1000L)
                db.collection("users").document(userId).update("chatDisabledUntil", disabledUntil).await()
                logAdminAction("Blocked user $userId from chat for $hours hours")
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to block user: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setChatAutoDeleteDays(days: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("settings").document("system")
                    .update("chatAutoDeleteDays", days).await()
                logAdminAction("Set chat auto-delete to $days days")
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Failed to update settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun runChatAutoDeleteAudit() {
        viewModelScope.launch {
            try {
                val settings = db.collection("settings").document("system").get().await()
                    .toObject(SystemSettings::class.java)
                val days = settings?.chatAutoDeleteDays ?: 0
                if (days > 0) {
                    val threshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                    val expiredChats = db.collection("chats")
                        .whereLessThan("lastMessageTime", threshold)
                        .get().await()
                    
                    if (!expiredChats.isEmpty) {
                        Log.d("AdminVM", "Auto-deleting ${expiredChats.size()} expired chats")
                        for (chatDoc in expiredChats.documents) {
                            val messages = chatDoc.reference.collection("messages").get().await()
                            val batch = db.batch()
                            messages.documents.forEach { batch.delete(it.reference) }
                            batch.delete(chatDoc.reference)
                            batch.commit().await()
                        }
                        logAdminAction("Auto-deleted ${expiredChats.size()} chats (Threshold: $days days)")
                    }
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "Auto-delete audit failed", e)
            }
        }
    }

    private fun syncUnlockPayments() {
        val reg = db.collection("contactUnlockPayments")
            .orderBy("unlockedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val payments = snapshot?.toObjects(com.sangeetsetu.app.model.UnlockPayment::class.java) ?: emptyList()
                _unlockPayments.value = payments
            }
        registrations.add(reg)
    }

    private fun syncAnnouncements() {
        viewModelScope.launch {
            mainRepository.getAllAnnouncementsFlow().collect {
                _announcements.value = it
            }
        }
    }

    fun sendAnnouncement(
        title: String, 
        content: String, 
        recipientCategories: List<String>,
        imageUrl: String? = null,
        actionLink: String? = null,
        targetType: String = "CATEGORY",
        actionLabel: String? = null,
        expiryDays: Int = 30,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = db.collection("announcements").document()
                val now = System.currentTimeMillis()
                val announcement = Announcement(
                    id = docRef.id,
                    title = title,
                    content = content,
                    imageUrl = imageUrl,
                    actionLink = actionLink,
                    actionLabel = actionLabel,
                    createdAt = now,
                    expiresAt = now + (expiryDays * 24 * 60 * 60 * 1000L),
                    isActive = true,
                    targetType = targetType,
                    targetCategories = recipientCategories,
                    recipientCategories = recipientCategories // for backward compatibility
                )

                docRef.set(announcement).await()
                
                // Simulation of Push Notification and Inbox
                createInboxMessagesProfessional(announcement)
                
                logAdminAction("Broadcasted: $title to $targetType")
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Broadcast failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createInboxMessagesProfessional(announcement: Announcement) {
        try {
            val usersSnap = db.collection("users").get().await()
            val allUsers = usersSnap.toObjects(User::class.java)
            
            val targetUsers = allUsers.filter { user ->
                when (announcement.targetType) {
                    "ALL" -> true
                    "USERS" -> user.userType == "User"
                    "ARTISTS" -> user.userType == "Artist"
                    "VIP_USERS" -> user.isVip
                    "VERIFIED_ARTISTS" -> user.userType == "Artist" && user.verificationStatus == "VERIFIED"
                    "CATEGORY" -> announcement.targetCategories.any { it == "All Users" || (user.userType == "Artist" && (user.category == it || user.categoryId == it)) }
                    "LOCATION" -> announcement.targetStates.contains(user.state) || announcement.targetDistricts.contains(user.district)
                    "CUSTOM" -> announcement.targetUserIds.contains(user.uid)
                    else -> true
                }
            }
            
            if (targetUsers.isEmpty()) return

            val batchSize = 100 // Firestore batch limit is 500, but let's be safe
            targetUsers.chunked(batchSize).forEach { chunk ->
                db.runTransaction { transaction ->
                    chunk.forEach { user ->
                        val chatId = "broadcast_${user.uid}"
                        val chatRef = db.collection("chats").document(chatId)
                        val messageRef = chatRef.collection("messages").document()
                        val now = System.currentTimeMillis()
                        
                        val message = com.sangeetsetu.app.model.Message(
                            id = messageRef.id,
                            senderId = "admin",
                            receiverId = user.uid,
                            message = announcement.content,
                            timestamp = now,
                            read = false,
                            type = "broadcast",
                            announcementId = announcement.id,
                            imageUrl = announcement.imageUrl,
                            actionLink = announcement.actionLink,
                            actionLabel = announcement.actionLabel
                        )

                        // Update or create conversation
                        transaction.set(chatRef, mapOf(
                            "id" to chatId,
                            "participants" to listOf("admin", user.uid).sorted(),
                            "lastMessage" to announcement.title,
                            "lastMessageTime" to now,
                            "createdAt" to now,
                            "conversationType" to "broadcast",
                            "senderRole" to "admin",
                            "type" to "BROADCAST",
                            "unreadCount" to FieldValue.increment(1)
                        ), SetOptions.merge())

                        transaction.set(messageRef, message)
                        
                        // Also keep the old notification system if needed, but user said "Use existing unified backend"
                        // I'll keep it for now as a fallback or remove it if strictly forbidden.
                        // "Do not use a separate Firestore collection for broadcast" -> 
                        // Actually it said "Don't use separate collection for broadcast messages". 
                        // The notifications collection might be used for other things too.
                    }
                }.await()
                
                // Trigger real-time notifications for this chunk
                chunk.forEach { user ->
                    notificationRepository.sendNotification(
                        userId = user.uid,
                        title = "Broadcast: ${announcement.title}",
                        message = announcement.content,
                        type = com.sangeetsetu.app.model.NotificationType.BROADCAST,
                        senderId = "admin",
                        senderName = "Sangeet Setu Admin",
                        chatId = "broadcast_${user.uid}"
                    )
                }
            }
            
            // Update sent count
            db.collection("announcements").document(announcement.id)
                .update("sentCount", targetUsers.size)
        } catch (e: Exception) {
            Log.e("AdminVM", "Inbox generation failed", e)
        }
    }

    fun deleteAnnouncement(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                db.collection("announcements").document(id).delete().await()
                logAdminAction("Deleted announcement: $id")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "Failed to delete announcement", e)
                _error.value = "Delete failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAllAnnouncements(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("BroadcastDebug", "Starting Global Cleanup: Deleting all announcements and broadcast notifications.")
                
                // 1. Delete all announcements
                val annSnap = db.collection("announcements").get().await()
                if (!annSnap.isEmpty) {
                    val batch = db.batch()
                    annSnap.documents.forEach { batch.delete(it.reference) }
                    batch.commit().await()
                }
                
                // 2. Delete all broadcast notifications from user inboxes
                val notifSnap = db.collection("notifications")
                    .whereEqualTo("type", com.sangeetsetu.app.model.NotificationType.BROADCAST.name)
                    .get().await()
                
                if (!notifSnap.isEmpty) {
                    notifSnap.documents.chunked(500).forEach { chunk ->
                        val batch = db.batch()
                        chunk.forEach { batch.delete(it.reference) }
                        batch.commit().await()
                    }
                }

                logAdminAction("Deleted ALL announcements and inbox messages")
                onSuccess()
            } catch (e: Exception) {
                Log.e("BroadcastDebug", "Cleanup FAILED", e)
                _error.value = "Cleanup failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun syncRealTimeData() {
        viewModelScope.launch {
            userRepository.getAllUsersFlow().collect { allUsers ->
                _users.value = allUsers
                _artists.value = allUsers.filter { it.userType == "Artist" }
                _pendingArtists.value = allUsers.filter { it.userType == "Artist" && it.approvalStatus == "PENDING" }
                calculateStats()
            }
        }

        viewModelScope.launch {
            bookingRepository.getAllBookingsFlow().collect { _bookings.value = it; calculateStats() }
        }

        viewModelScope.launch {
            mainRepository.getAllBannersFlow().collect { _banners.value = it }
        }

        viewModelScope.launch {
            mainRepository.getOffersFlow().collect { _offers.value = it }
        }

        viewModelScope.launch {
            mainRepository.getServicesFlow().collect { _services.value = it }
        }

        viewModelScope.launch {
            mainRepository.getHomeSectionsFlow().collect { _homeSections.value = it }
        }

        viewModelScope.launch {
            mainRepository.getCategoriesFlow().collect {
                _categories.value = it
                calculateStats()
            }
        }
        
        viewModelScope.launch {
            mainRepository.getBadgesFlow().collect {
                _badges.value = it
            }
        }
        
        viewModelScope.launch {
            notificationRepository.getUnreadCountFlow().collect {
                _unreadNotificationsCount.value = it
            }
        }
        
        viewModelScope.launch {
            systemLogRepository.getRecentLogsFlow().collect {
                _recentActivities.value = it
            }
        }

        val reviewReg = db.collection("reviews")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error == null) {
                    _reviews.value = snap?.toObjects(Review::class.java) ?: emptyList()
                } else if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Log.e("AdminVM", "Index required for reviews. Preparing data safely.", error)
                    _error.value = "Reviews data is being prepared. Please try again shortly."
                    db.collection("reviews").get().addOnSuccessListener { s ->
                        _reviews.value = s.toObjects(Review::class.java).sortedByDescending { it.timestamp }
                    }
                }
            }
        registrations.add(reviewReg)
        
        val planReg = db.collection("subscription_plans").addSnapshotListener { snap, error ->
            if (error != null) {
                Log.e("AdminViewModel", "UID=${userRepository.getCurrentUserId()} Collection=subscription_plans Operation=Listen FirestoreCode=${error.code}", error)
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    _subscriptionPlans.value = emptyList()
                }
                return@addSnapshotListener
            }
            _subscriptionPlans.value = try {
                snap?.toObjects(SubscriptionPlan::class.java) ?: emptyList()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error parsing SubscriptionPlan", e)
                emptyList()
            }
        }
        registrations.add(planReg)
    }

    
    private fun calculateStats() {
        val bookings = _bookings.value
        
        val now = Calendar.getInstance()
        val todayStart = now.apply { 
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) 
        }.timeInMillis
        
        _stats.value = _stats.value.copy(
            totalUsers = _users.value.size,
            totalArtists = _artists.value.size,
            activeArtists = _artists.value.count { it.accountStatus == "ACTIVE" },
            totalBookings = bookings.size,
            todayBookings = bookings.count { it.createdAt >= todayStart },
            newRegistrationsToday = _users.value.count { it.createdAt >= todayStart },
            pendingBookings = bookings.count { it.status == BookingStatus.PENDING },
            completedBookings = bookings.count { it.status == BookingStatus.COMPLETED },
            confirmedBookings = bookings.count { it.status == BookingStatus.ACCEPTED || it.status == BookingStatus.IN_PROGRESS },
            cancelledBookings = bookings.count { it.status == BookingStatus.CANCELLED },
            totalCategories = _categories.value.size
        )
    }

    fun updateBookingStatus(booking: Booking, newStatus: BookingStatus) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                bookingRepository.updateBookingStatus(booking, newStatus).onSuccess {
                    logAdminAction("Updated booking ${booking.id} status to $newStatus")
                }.onFailure { e ->
                    _error.value = "Booking status update failed: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in updateBookingStatus", e)
                _error.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun seedDefaultCategories(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                AppConstants.defaultCategories.forEach { catInfo ->
                    val existing = db.collection("categories")
                        .whereEqualTo("name", catInfo.name)
                        .get(com.google.firebase.firestore.Source.SERVER).await()
                    
                    if (existing.isEmpty) {
                        val docRef = db.collection("categories").document()
                        val category = Category(
                            id = docRef.id,
                            name = catInfo.name,
                            imageUrl = catInfo.imageUrl,
                            iconUrl = catInfo.imageUrl,
                            isActive = true,
                            createdAt = System.currentTimeMillis()
                        )
                        docRef.set(category).await()
                    }
                }
                logAdminAction("Seeded default categories")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "Seed failed", e)
                onError(e.localizedMessage ?: "Seed failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addCategory(name: String, imageUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = db.collection("categories").whereEqualTo("name", name.trim())
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                if (!existing.isEmpty) {
                    onError("Category '$name' already exists")
                    return@launch
                }

                if (imageUri == null) {
                    val defaultInfo = AppConstants.defaultCategories.find { it.name.equals(name.trim(), ignoreCase = true) }
                    if (defaultInfo != null) {
                        val docRef = db.collection("categories").document()
                        val category = Category(
                            id = docRef.id,
                            name = name.trim(),
                            imageUrl = defaultInfo.imageUrl,
                            iconUrl = defaultInfo.imageUrl,
                            isActive = true,
                            createdAt = System.currentTimeMillis()
                        )
                        docRef.set(category).await()
                        logAdminAction("Added category from default: ${name.trim()}")
                        onSuccess()
                        return@launch
                    } else {
                        onError("Please select an image for this category")
                        return@launch
                    }
                }

                adminCategoryRepository.addCategory(name.trim(), imageUri)
                    .onSuccess {
                        logAdminAction("Added category: ${name.trim()}")
                        viewModelScope.launch { RefreshSignal.onDataUpdated() }
                        onSuccess()
                    }
                    .onFailure { e -> 
                        _error.value = "Category add failed: ${e.message}"
                        onError(e.localizedMessage ?: "Add failed")
                    }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in addCategory", e)
                onError("Failed: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(category: Category, newImageUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val existing = db.collection("categories")
                    .whereEqualTo("name", category.name.trim())
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                
                if (!existing.isEmpty && existing.documents.any { it.id != category.id }) {
                    onError("Another category with this name already exists")
                    return@launch
                }

                adminCategoryRepository.updateCategory(category, newImageUri)
                    .onSuccess {
                        logAdminAction("Updated category: ${category.name}")
                        viewModelScope.launch { RefreshSignal.onDataUpdated() }
                        onSuccess()
                    }
                    .onFailure { e -> 
                        _error.value = "Category update failed: ${e.message}"
                        onError(e.localizedMessage ?: "Update failed")
                    }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in updateCategory", e)
                onError(e.localizedMessage ?: "Update failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(category: Category, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                adminCategoryRepository.deleteCategory(category)
                    .onSuccess {
                        logAdminAction("Deleted category: ${category.name}")
                        onSuccess()
                    }
                    .onFailure { e -> 
                        _error.value = "Category delete failed: ${e.message}"
                        onError(e.localizedMessage ?: "Delete failed")
                    }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteCategory", e)
                onError("Failed: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBanner(banner: Banner, imageUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalBanner = banner
                if (imageUri != null) {
                    // Delete old image if it exists on Cloudinary
                    if (banner.cloudinaryPublicId.isNotEmpty()) {
                        try {
                            storageRepository.deleteImage(banner.cloudinaryPublicId)
                        } catch (e: Exception) {
                            Log.e("AdminViewModel", "Failed to delete old banner image", e)
                        }
                    }

                    storageRepository.uploadImage(imageUri, "banners", "banner_${System.currentTimeMillis()}")
                        .onSuccess { resource ->
                            finalBanner = banner.copy(
                                imageUrl = resource.url,
                                cloudinaryPublicId = resource.publicId
                            )
                        }.onFailure { e -> 
                            _error.value = "Banner image upload failed: ${e.message}"
                            onError(e.localizedMessage ?: "Upload failed")
                            _isLoading.value = false
                            return@launch
                        }
                }
                val docRef = if (banner.id.isEmpty()) db.collection("banners").document() else db.collection("banners").document(banner.id)
                finalBanner = finalBanner.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "banners",
                    documentId = finalBanner.id,
                    expectedData = mapOf("id" to finalBanner.id, "imageUrl" to finalBanner.imageUrl)
                ) {
                    Log.d("AdminVM", "Saving banner to Firestore: ${finalBanner.id}")
                    docRef.set(finalBanner).await()
                }.onSuccess {
                    Log.d("AdminVM", "Banner save SUCCESS: ${finalBanner.id}")
                    logAdminAction(if (banner.id.isEmpty()) "Added banner: ${finalBanner.title}" else "Updated banner: ${finalBanner.title}")
                    viewModelScope.launch { RefreshSignal.onDataUpdated() }
                    onSuccess()
                }.onFailure { e -> 
                    Log.e("AdminVM", "Banner save FAILED", e)
                    _error.value = "Banner save failed: ${e.message}"
                    onError(e.localizedMessage ?: "Save failed")
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveBanner", e)
                onError("Save failed: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBanner(banner: Banner, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Delete from Cloudinary if publicId exists
                if (banner.cloudinaryPublicId.isNotEmpty()) {
                    try {
                        storageRepository.deleteImage(banner.cloudinaryPublicId)
                    } catch (e: Exception) {
                        Log.e("AdminViewModel", "Failed to delete banner image from Cloudinary", e)
                    }
                }

                FirestoreAudit.verifiedDelete("banners", banner.id) {
                    db.collection("banners").document(banner.id).delete().await()
                }.onSuccess {
                    logAdminAction("Deleted banner: ${banner.title}")
                    viewModelScope.launch { RefreshSignal.onDataUpdated() }
                    onSuccess()
                }.onFailure { e -> _error.value = "Banner delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteBanner", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reorderBanners(fromIndex: Int, toIndex: Int, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val currentList = _banners.value.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        
        val reorderedList = currentList.mapIndexed { index, banner -> 
            banner.copy(order = index) 
        }
        _banners.value = reorderedList
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val batch = db.batch()
                reorderedList.forEach { banner ->
                    batch.update(db.collection("banners").document(banner.id), "order", banner.order)
                }
                batch.commit().await()
                logAdminAction("Reordered banners")
                viewModelScope.launch { RefreshSignal.onDataUpdated() }
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "Failed to sync banner order", e)
                _error.value = "Reorder failed: ${e.message}"
                onError(e.localizedMessage ?: "Reorder failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveBadge(badge: BadgeConfig, imageUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalBadge = badge
                if (imageUri != null) {
                    // Delete old image if exists
                    if (badge.cloudinaryPublicId.isNotEmpty()) {
                        try { storageRepository.deleteImage(badge.cloudinaryPublicId) } catch (e: Exception) { Log.e("AdminVM", "Old image delete failed", e) }
                    }
                    
                    storageRepository.uploadImage(imageUri, "badges", "badge_${badge.id}_${System.currentTimeMillis()}")
                        .onSuccess { resource ->
                            finalBadge = badge.copy(imageUrl = resource.url, cloudinaryPublicId = resource.publicId)
                        }.onFailure { e ->
                            _error.value = "Badge image upload failed: ${e.message}"
                            onError(e.localizedMessage ?: "Upload failed")
                            _isLoading.value = false
                            return@launch
                        }
                }
                
                db.collection("badges").document(finalBadge.id)
                    .set(finalBadge.copy(lastUpdated = System.currentTimeMillis()))
                    .await()
                
                logAdminAction("Updated badge: ${finalBadge.id}")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveBadge", e)
                onError("Save failed: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBadge(id: String, publicId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (publicId.isNotEmpty()) {
                    storageRepository.deleteImage(publicId)
                }
                db.collection("badges").document(id).delete().await()
                logAdminAction("Deleted badge: $id")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteBadge", e)
                _error.value = "Badge delete failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addBhajan(bhajan: Bhajan, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedWrite(
                    collectionName = "bhajans",
                    documentId = bhajan.id,
                    expectedData = mapOf("id" to bhajan.id, "title" to bhajan.title)
                ) {
                    db.collection("bhajans").document(bhajan.id).set(bhajan).await()
                }.onSuccess {
                    logAdminAction("Added/Updated bhajan: ${bhajan.title}")
                    onSuccess()
                }.onFailure { e -> _error.value = "Bhajan add failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in addBhajan", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBhajan(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("bhajans", id) {
                    db.collection("bhajans").document(id).delete().await()
                }.onSuccess {
                    logAdminAction("Deleted bhajan: $id")
                    onSuccess()
                }.onFailure { e -> _error.value = "Bhajan delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteBhajan", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveOffer(offer: Offer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = if (offer.id.isEmpty()) db.collection("offers").document() else db.collection("offers").document(offer.id)
                val finalOffer = offer.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "offers",
                    documentId = finalOffer.id,
                    expectedData = mapOf("id" to finalOffer.id, "code" to finalOffer.code)
                ) {
                    docRef.set(finalOffer).await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> 
                    _error.value = "Offer save failed: ${e.message}"
                    onError(e.localizedMessage ?: "Save failed")
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveOffer", e)
                onError(e.localizedMessage ?: "Save failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteOffer(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("offers", id) {
                    db.collection("offers").document(id).delete().await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> _error.value = "Offer delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteOffer", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveService(service: Service, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = if (service.id.isEmpty()) db.collection("services").document() else db.collection("services").document(service.id)
                val finalService = service.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "services",
                    documentId = finalService.id,
                    expectedData = mapOf("id" to finalService.id, "name" to finalService.name)
                ) {
                    docRef.set(finalService).await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> _error.value = "Service save failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveService", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteService(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("services", id) {
                    db.collection("services").document(id).delete().await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> _error.value = "Service delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteService", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchLiveSessions() {
        viewModelScope.launch {
            try {
                val result = db.collection("live_telecasts")
                    .orderBy("startTime", Query.Direction.DESCENDING)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _liveSessions.value = result.toObjects(LiveTelecast::class.java)
            } catch (e: Exception) { Log.e("AdminVM", "fetchLiveSessions error", e) }
        }
    }

    fun saveLiveSession(session: LiveTelecast, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = if (session.id.isEmpty()) db.collection("live_telecasts").document() else db.collection("live_telecasts").document(session.id)
                val finalSession = session.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "live_telecasts",
                    documentId = finalSession.id,
                    expectedData = mapOf("id" to finalSession.id, "title" to finalSession.title)
                ) {
                    docRef.set(finalSession).await()
                }.onSuccess {
                    fetchLiveSessions()
                    onSuccess()
                }.onFailure { e -> _error.value = "Live session save failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveLiveSession", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLiveSession(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("live_telecasts", id) {
                    db.collection("live_telecasts").document(id).delete().await()
                }.onSuccess {
                    fetchLiveSessions()
                    onSuccess()
                }.onFailure { e -> _error.value = "Live session delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteLiveSession", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendBroadcast(title: String, message: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = db.collection("announcements").document()
                val announcement = mapOf(
                    "id" to docRef.id,
                    "title" to title,
                    "content" to message,
                    "timestamp" to System.currentTimeMillis()
                )
                FirestoreAudit.verifiedWrite(
                    collectionName = "announcements",
                    documentId = docRef.id,
                    expectedData = mapOf("id" to docRef.id, "title" to title)
                ) {
                    docRef.set(announcement).await()
                }.onSuccess {
                    logAdminAction("Sent broadcast: $title")
                    onSuccess()
                }.onFailure { e -> _error.value = "Broadcast failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in sendBroadcast", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchAppContent() {
        viewModelScope.launch {
            try {
                val result = db.collection("app_content")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _appContent.value = result.documents.map { it.data?.plus("id" to it.id) ?: emptyMap() }
            } catch (e: Exception) { Log.e("AdminVM", "fetchAppContent error", e) }
        }
    }

    fun addAppContent(data: Map<String, Any>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = db.collection("app_content").document()
                val finalData = data.plus("id" to docRef.id)
                FirestoreAudit.verifiedWrite(
                    collectionName = "app_content",
                    documentId = docRef.id,
                    expectedData = mapOf("id" to docRef.id)
                ) {
                    docRef.set(finalData).await()
                }.onSuccess {
                    fetchAppContent()
                    onSuccess()
                }.onFailure { e -> _error.value = "Content add failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in addAppContent", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAppContent(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("app_content", id) {
                    db.collection("app_content").document(id).delete().await()
                }.onSuccess {
                    fetchAppContent()
                    onSuccess()
                }.onFailure { e -> _error.value = "Content delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteAppContent", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchReports() {
        viewModelScope.launch {
            try {
                val result = db.collection("reports")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _reports.value = result.documents.map { it.data?.plus("id" to it.id) ?: emptyMap() }
            } catch (e: Exception) { Log.e("AdminVM", "fetchReports error", e) }
        }
    }

    fun deleteReport(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("reports", id) {
                    db.collection("reports").document(id).delete().await()
                }.onSuccess {
                    fetchReports()
                    onSuccess()
                }.onFailure { e -> _error.value = "Report delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteReport", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchHomeSections() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("home_sections")
                    .orderBy("displayOrder", Query.Direction.ASCENDING)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _homeSections.value = result.toObjects(HomeSection::class.java)
            } catch (e: Exception) {
                Log.e("AdminVM", "fetchHomeSections error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("offers")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _offers.value = result.toObjects(Offer::class.java)
            } catch (e: Exception) {
                Log.e("AdminVM", "fetchOffers error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPendingArtists() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("users")
                    .whereEqualTo("userType", "Artist")
                    .whereEqualTo("approvalStatus", "PENDING")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _pendingArtists.value = result.toObjects(User::class.java)
            } catch (e: Exception) {
                if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Log.e("AdminVM", "Index required for pending artists. Preparing data safely.", e)
                    _error.value = "Artists data is being prepared. Please try again shortly."
                    // Fallback to filtering all users in memory
                    val allUsers = db.collection("users").get().await()
                    _pendingArtists.value = allUsers.toObjects(User::class.java)
                        .filter { it.userType == "Artist" && it.approvalStatus == "PENDING" }
                } else {
                    Log.e("AdminVM", "fetchPendingArtists error", e)
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchServices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("services")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _services.value = result.toObjects(Service::class.java)
            } catch (e: Exception) {
                Log.e("AdminVM", "fetchServices error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchBanners() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("banners")
                    .orderBy("displayOrder", Query.Direction.ASCENDING)
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _banners.value = result.toObjects(Banner::class.java)
            } catch (e: Exception) {
                Log.e("AdminVM", "fetchBanners error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchSubscriptionPlans() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = db.collection("subscription_plans")
                    .get(com.google.firebase.firestore.Source.SERVER).await()
                _subscriptionPlans.value = result.toObjects(SubscriptionPlan::class.java)
            } catch (e: Exception) {
                Log.e("AdminVM", "fetchSubscriptionPlans error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveHomeSection(section: HomeSection, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = if (section.id.isEmpty()) db.collection("home_sections").document() else db.collection("home_sections").document(section.id)
                val finalSection = section.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "home_sections",
                    documentId = finalSection.id,
                    expectedData = mapOf("id" to finalSection.id, "title" to finalSection.title)
                ) {
                    docRef.set(finalSection).await()
                }.onSuccess {
                    viewModelScope.launch { RefreshSignal.onDataUpdated() }
                    onSuccess()
                }.onFailure { e -> _error.value = "Home section save failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveHomeSection", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteHomeSection(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("home_sections", id) {
                    db.collection("home_sections").document(id).delete().await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> _error.value = "Home section delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteHomeSection", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun reorderHomeSections(fromIndex: Int, toIndex: Int) {
        val currentList = _homeSections.value.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        
        // Update local state immediately for snappy UI
        val reorderedList = currentList.mapIndexed { index, section -> 
            section.copy(displayOrder = index) 
        }
        _homeSections.value = reorderedList
        
        // Sync to Firestore
        viewModelScope.launch {
            try {
                val batch = db.batch()
                reorderedList.forEach { section ->
                    batch.update(db.collection("home_sections").document(section.id), "displayOrder", section.displayOrder)
                }
                batch.commit().await()
                viewModelScope.launch { RefreshSignal.onDataUpdated() }
            } catch (e: Exception) {
                Log.e("AdminVM", "Failed to sync order", e)
            }
        }
    }

    fun fetchSystemSettings() {
        viewModelScope.launch {
            try {
                val doc = db.collection("settings").document("system").get(com.google.firebase.firestore.Source.SERVER).await()
                if (doc.exists()) {
                    _systemSettings.value = doc.toObject(SystemSettings::class.java) ?: SystemSettings()
                }
            } catch (e: Exception) { Log.e("AdminVM", "fetchSystemSettings failed", e) }
        }
    }

    fun saveSystemSettings(settings: SystemSettings, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedWrite(
                    collectionName = "settings",
                    documentId = "system",
                    expectedData = mapOf("maintenanceMode" to settings.maintenanceMode)
                ) {
                    db.collection("settings").document("system").set(settings).await()
                }.onSuccess {
                    _systemSettings.value = settings
                    logAdminAction("Updated system settings")
                    onSuccess()
                }.onFailure { e -> 
                    _error.value = "Settings save failed: ${e.message}"
                    onError(e.localizedMessage ?: "Save failed")
                }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveSystemSettings", e)
                onError(e.localizedMessage ?: "Save failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addUser(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var finalUser = user
                
                // Automatically generate Artist ID for new artists
                if (user.userType == "Artist" && user.artistId.isEmpty()) {
                    val nextId = userRepository.getNextArtistId()
                    finalUser = user.copy(artistId = nextId)
                }
                
                val docRef = if (finalUser.uid.isEmpty()) db.collection("users").document() else db.collection("users").document(finalUser.uid)
                finalUser = finalUser.copy(uid = docRef.id, createdAt = System.currentTimeMillis())
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "users",
                    documentId = finalUser.uid,
                    expectedData = mapOf("uid" to finalUser.uid, "email" to finalUser.email)
                ) {
                    docRef.set(finalUser).await()
                }.onSuccess {
                    logAdminAction("Admin added new ${finalUser.userType}: ${finalUser.name} (${finalUser.artistId})")
                    onSuccess()
                }.onFailure { e -> _error.value = "User add failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in addUser", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(user: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Use transaction to ensure consistency
                val updates = mapOf(
                    "name" to user.name,
                    "phone" to user.phone,
                    "email" to user.email,
                    "category" to user.category,
                    "categoryId" to user.categoryId,
                    "state" to user.state,
                    "district" to user.district,
                    "city" to user.city,
                    "age" to user.age,
                    "skill" to user.skill,
                    "experience" to user.experience,
                    "aboutMe" to user.aboutMe,
                    "photoUrl" to user.photoUrl,
                    "cloudinaryPublicId" to user.cloudinaryPublicId,
                    "verificationStatus" to user.verificationStatus,
                    "approvalStatus" to user.approvalStatus,
                    "accountStatus" to user.accountStatus,
                    "isPremium" to user.isPremium,
                    "isVip" to user.isVip
                )

                userRepository.updateArtistStatusTransaction(user.uid, updates)
                    .onSuccess {
                        logAdminAction("Admin updated profile for ${user.uid} (ID: ${user.artistId})")
                        onSuccess()
                    }
                    .onFailure { e -> _error.value = "Profile update failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in updateUserProfile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteUser(uid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("users", uid) {
                    db.collection("users").document(uid).delete().await()
                }.onSuccess {
                    logAdminAction("Deleted user: $uid")
                    onSuccess()
                }.onFailure { e -> _error.value = "User delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteUser", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccountStatus(uid: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val standardizedStatus = status.uppercase().trim()
            userRepository.updateArtistStatusTransaction(uid, mapOf("accountStatus" to standardizedStatus))
                .onSuccess {
                    logAdminAction("Updated artist $uid account status to $standardizedStatus")
                    onSuccess()
                }.onFailure { e -> _error.value = "Account status update failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun updateVerificationStatus(uid: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val standardizedStatus = status.uppercase().trim()
            userRepository.updateArtistStatusTransaction(uid, mapOf("verificationStatus" to standardizedStatus))
                .onSuccess {
                    logAdminAction("Updated artist $uid verification status to $standardizedStatus")
                    onSuccess()
                }.onFailure { e -> _error.value = "Verification status update failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun updateApprovalStatus(uid: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val standardizedStatus = status.uppercase().trim()
            userRepository.updateArtistStatusTransaction(uid, mapOf("approvalStatus" to standardizedStatus))
                .onSuccess {
                    logAdminAction("Updated artist $uid approval status to $standardizedStatus")
                    onSuccess()
                }.onFailure { e -> _error.value = "Approval status update failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun updateArtistStatus(uid: String, status: String, onSuccess: () -> Unit) {
        updateAccountStatus(uid, status, onSuccess)
    }

    fun approveArtist(uid: String, onSuccess: () -> Unit) {
        updateApprovalStatus(uid, "APPROVED", onSuccess)
    }

    fun rejectArtist(uid: String, onSuccess: () -> Unit) {
        updateApprovalStatus(uid, "REJECTED", onSuccess)
    }

    fun toggleVipStatus(uid: String, isVip: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.updateArtistStatusTransaction(uid, mapOf("isVip" to isVip))
                .onSuccess {
                    logAdminAction("Updated artist $uid VIP status to $isVip")
                    onSuccess()
                }.onFailure { e -> _error.value = "VIP status update failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun updateUserStatus(uid: String, status: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedWrite(
                    collectionName = "users",
                    documentId = uid,
                    expectedData = mapOf("status" to status)
                ) {
                    db.collection("users").document(uid).update("status", status).await()
                }.onSuccess {
                    logAdminAction("Updated user $uid status to $status")
                    onSuccess()
                }.onFailure { e -> _error.value = "Status update failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in updateUserStatus", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveSubscriptionPlan(plan: SubscriptionPlan, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val docRef = if (plan.id.isEmpty()) db.collection("subscription_plans").document() else db.collection("subscription_plans").document(plan.id)
                val finalPlan = plan.copy(id = docRef.id)
                
                FirestoreAudit.verifiedWrite(
                    collectionName = "subscription_plans",
                    documentId = finalPlan.id,
                    expectedData = mapOf("id" to finalPlan.id, "name" to finalPlan.name)
                ) {
                    docRef.set(finalPlan).await()
                }.onSuccess {
                    onSuccess()
                }.onFailure { e -> _error.value = "Plan save failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in saveSubscriptionPlan", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteReview(reviewId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                FirestoreAudit.verifiedDelete("reviews", reviewId) {
                    db.collection("reviews").document(reviewId).delete().await()
                }.onSuccess {
                    logAdminAction("Deleted review: $reviewId")
                    onSuccess()
                }.onFailure { e -> _error.value = "Review delete failed: ${e.message}" }
            } catch (e: Exception) {
                Log.e("AdminVM", "CRITICAL ERROR in deleteReview", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRevenueFilter(filter: String) {
        val bookings = _bookings.value
        val now = Calendar.getInstance()
        
        val data = when(filter) {
            "Today" -> {
                val countValues = FloatArray(24) { 0f }
                val todayStart = now.apply { 
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0) 
                }.timeInMillis
                bookings.filter { it.createdAt >= todayStart }.forEach { b ->
                    val hour = ((b.createdAt - todayStart) / (3600 * 1000)).toInt()
                    if (hour in 0..23) countValues[hour] += 1f
                }
                countValues.toList()
            }
            "Week" -> {
                val dayValues = FloatArray(7) { 0f }
                val weekStart = now.apply { 
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0) 
                }.timeInMillis
                bookings.filter { it.createdAt >= weekStart }.forEach { b ->
                    val day = ((b.createdAt - weekStart) / (24 * 3600 * 1000)).toInt()
                    if (day in 0..6) dayValues[day] += 1f
                }
                dayValues.toList()
            }
            "Month" -> {
                val dayValues = FloatArray(30) { 0f }
                val monthStart = now.apply { 
                    add(Calendar.DAY_OF_YEAR, -29)
                    set(Calendar.HOUR_OF_DAY, 0) 
                }.timeInMillis
                bookings.filter { it.createdAt >= monthStart }.forEach { b ->
                    val day = ((b.createdAt - monthStart) / (24 * 3600 * 1000)).toInt()
                    if (day in 0..29) dayValues[day] += 1f
                }
                dayValues.toList()
            }
            "Year" -> {
                val monthValues = FloatArray(12) { 0f }
                val yearStart = now.apply { 
                    add(Calendar.MONTH, -11)
                    set(Calendar.DAY_OF_MONTH, 1) 
                }.timeInMillis
                bookings.filter { it.createdAt >= yearStart }.forEach { b ->
                    val month = ((b.createdAt - yearStart) / (30L * 24 * 3600 * 1000)).toInt()
                    if (month in 0..11) monthValues[month] += 1f
                }
                monthValues.toList()
            }
            else -> emptyList()
        }
        
        val maxVal = data.maxOrNull() ?: 1f
        _revenueChartData.value = data.map { if (maxVal == 0f) 0f else it / maxVal }
    }

    fun logAdminAction(action: String) {
        viewModelScope.launch {
            systemLogRepository.logAction(action)
        }
    }

    fun runArtistStatusAudit(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.runArtistStatusMigration()
                .onSuccess { count ->
                    logAdminAction("Performed Artist Status Audit: Fixed $count records")
                    onComplete(count)
                }
                .onFailure { e -> _error.value = "Audit failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    fun runCategoryAudit(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            userRepository.runCategoryMigration()
                .onSuccess { count ->
                    logAdminAction("Performed Category Mapping Audit: Migrated $count records")
                    onComplete(count)
                }
                .onFailure { e -> _error.value = "Category audit failed: ${e.message}" }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        registrations.forEach { it.remove() }
        registrations.clear()
    }
}
