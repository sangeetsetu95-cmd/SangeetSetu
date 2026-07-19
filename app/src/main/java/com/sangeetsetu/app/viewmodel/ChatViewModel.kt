package com.sangeetsetu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.Chat
import com.sangeetsetu.app.model.Message
import com.sangeetsetu.app.repository.ChatRepository
import com.sangeetsetu.app.repository.SystemLogRepository
import com.sangeetsetu.app.repository.UserRepository
import com.sangeetsetu.app.util.ChatFilterUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val repo = ChatRepository
    private val userRepo = UserRepository
    private val logRepo = SystemLogRepository

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    private val _unreadCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val unreadCounts: StateFlow<Map<String, Int>> = _unreadCounts

    private val _presenceMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val presenceMap: StateFlow<Map<String, String>> = _presenceMap

    private val _currentChat = MutableStateFlow<Chat?>(null)
    val currentChat: StateFlow<Chat?> = _currentChat

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId

    private val _participantInfo = MutableStateFlow<Map<String, Any?>>(emptyMap())
    val participantInfo: StateFlow<Map<String, Any?>> = _participantInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _violationMessage = MutableStateFlow<String?>(null)
    val violationMessage: StateFlow<String?> = _violationMessage

    private val _isChatDisabled = MutableStateFlow(false)
    val isChatDisabled: StateFlow<Boolean> = _isChatDisabled

    private val _participantPresence = MutableStateFlow<Map<String, Any>>(emptyMap())
    val participantPresence: StateFlow<Map<String, Any>> = _participantPresence

    private var presenceListener: com.google.firebase.database.ValueEventListener? = null
    private var isPrivacyFilterEnabled = true

    init {
        loadChats()
        checkChatStatus()
        fetchSettings()
    }

    private fun fetchSettings() {
        viewModelScope.launch {
            try {
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("settings").document("system").get().await()
                if (doc.exists()) {
                    val settings = doc.toObject(com.sangeetsetu.app.model.SystemSettings::class.java)
                    isPrivacyFilterEnabled = settings?.features?.get("chat_privacy_filter") == true
                }
            } catch (e: Exception) {
                isPrivacyFilterEnabled = true
            }
        }
    }

    private fun checkChatStatus() {
        val uid = userRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            val user = userRepo.getUser(uid).getOrNull()
            if (user != null) {
                if (user.chatDisabledUntil > System.currentTimeMillis()) {
                    _isChatDisabled.value = true
                }
            }
        }
    }

    fun clearViolationMessage() {
        _violationMessage.value = null
    }

    fun loadChats() {
        val uid = userRepo.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                repo.getChatsFlow(uid).collect { rawChats ->
                    val enrichedChats = rawChats.map { chat ->
                        async {
                            val otherId = chat.participants.find { it != uid } ?: ""
                            val info = repo.getParticipantInfo(otherId)
                            val unread = repo.getUnreadCount(chat.id, uid)
                            
                            // Track presence for each
                            if (otherId.isNotEmpty()) {
                                observePresenceForList(otherId)
                            }

                            chat.copy(
                                participantName = info["name"] as? String ?: "User",
                                participantPhoto = info["photo"] as? String ?: "",
                                isVerified = info["isVerified"] as? Boolean ?: false,
                                unreadCount = unread
                            )
                        }
                    }.awaitAll()
                    _chats.value = enrichedChats
                }
            }
        }
    }

    private val presenceListeners = mutableMapOf<String, com.google.firebase.database.ValueEventListener>()

    private fun observePresenceForList(uid: String) {
        if (presenceListeners.containsKey(uid)) return

        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val state = snapshot.child("state").value as? String ?: "offline"
                val currentMap = _presenceMap.value.toMutableMap()
                currentMap[uid] = state
                _presenceMap.value = currentMap
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        presenceListeners[uid] = listener
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").addValueEventListener(listener)
    }

    fun setActiveChat(chatId: String, otherId: String) {
        _activeChatId.value = chatId
        viewModelScope.launch {
            _participantInfo.value = repo.getParticipantInfo(otherId)
            
            // Get chat info
            val db = FirebaseFirestore.getInstance()
            val chatDoc = db.collection("chats").document(chatId).get().await()
            val chat = chatDoc.toObject<Chat>()
            _currentChat.value = chat
            
            // Disable if broadcast
            if (chat?.conversationType == "broadcast") {
                _isChatDisabled.value = true
            }

            repo.getMessagesFlow(chatId).collect { _messages.value = it }
        }
        
        // Mark as read
        val uid = userRepo.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                repo.markMessagesAsRead(chatId, uid)
            }
        }
        
        trackParticipantPresence(otherId)
    }

    private fun trackParticipantPresence(uid: String) {
        // Clean up old listener
        presenceListener?.let { 
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").removeEventListener(it)
        }
        
        presenceListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val data = snapshot.value as? Map<String, Any> ?: emptyMap()
                _participantPresence.value = data
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").addValueEventListener(presenceListener!!)
    }

    override fun onCleared() {
        super.onCleared()
        presenceListener?.let {
            // We don't have the UID here easily unless we store it, 
            // but we can just use the last used one or not worry too much if it's cleared with VM
        }
        // Clear all list listeners
        presenceListeners.forEach { (uid, listener) ->
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").removeEventListener(listener)
        }
        presenceListeners.clear()
    }

    fun sendMessage(receiverId: String, text: String) {
        val senderId = userRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            // Check if chat is disabled
            val user = userRepo.getUser(senderId).getOrNull()
            if (user != null && user.chatDisabledUntil > System.currentTimeMillis()) {
                val remainingHours = (user.chatDisabledUntil - System.currentTimeMillis()) / (3600 * 1000)
                _violationMessage.value = "सुरक्षा कारणों से आपका चैट ब्लॉक है। कृपया $remainingHours घंटे बाद प्रयास करें।"
                return@launch
            }

            // Privacy Filter - Backend driven logic (check if enabled in settings)
            if (isPrivacyFilterEnabled && ChatFilterUtil.containsRestrictedContent(text)) {
                handlePrivacyViolation(senderId, text)
                return@launch
            }

            val chatId = _activeChatId.value ?: repo.createOrGetChat(senderId, receiverId)
            if (_activeChatId.value == null) {
                _activeChatId.value = chatId
            }
            repo.sendMessage(chatId, senderId, receiverId, text)
        }
    }

    private suspend fun handlePrivacyViolation(uid: String, text: String) {
        _violationMessage.value = ChatFilterUtil.getRestrictedMessage()
        
        val user = userRepo.getUser(uid).getOrNull() ?: return
        val newWarningCount = user.chatWarningCount + 1
        val updates = mutableMapOf<String, Any>("chatWarningCount" to newWarningCount)
        
        var logMessage = "Privacy Violation by ${user.name} (${user.uid}): $text"
        
        if (newWarningCount >= 3) {
            val disabledUntil = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            updates["chatDisabledUntil"] = disabledUntil
            updates["chatWarningCount"] = 0 
            _isChatDisabled.value = true
            logMessage += " | BLOCK_24H_APPLIED"
        }
        
        userRepo.updateProfileFields(uid, updates)
        logRepo.logAction(logMessage)
    }
    
    fun createChatAndNavigate(otherId: String, onReady: (String) -> Unit) {
        val uid = userRepo.getCurrentUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val chatId = repo.createOrGetChat(uid, otherId)
            _isLoading.value = false
            onReady(chatId)
        }
    }
}
