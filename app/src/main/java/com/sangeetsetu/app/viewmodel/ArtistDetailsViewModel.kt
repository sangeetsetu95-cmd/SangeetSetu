package com.sangeetsetu.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.repository.UserRepository
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ArtistDetailsViewModel : ViewModel() {
    private val userRepository = UserRepository
    private val db = FirebaseFirestore.getInstance()

    private val _artist = MutableStateFlow<User?>(null)
    val artist: StateFlow<User?> = _artist.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _presence = MutableStateFlow<Map<String, Any>>(emptyMap())
    val presence: StateFlow<Map<String, Any>> = _presence.asStateFlow()
    
    private var trackedUid: String? = null
    private var presenceListener: com.google.firebase.database.ValueEventListener? = null

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Try loading by UID first (Standard)
                val doc = db.collection("users").document(artistId).get(Source.SERVER).await()
                var user = doc.toObject(User::class.java)

                // If not found by UID, or if the input looks like a Sequential Artist ID (SSA...), search by field
                if (user == null || artistId.startsWith("SSA")) {
                    val query = db.collection("users")
                        .whereEqualTo("artistId", artistId)
                        .limit(1)
                        .get(Source.SERVER)
                        .await()
                    
                    if (!query.isEmpty) {
                        user = query.documents[0].toObject(User::class.java)
                    }
                }

                _artist.value = user
                
                if (user == null) {
                    _error.value = "Artist not found"
                } else {
                    checkIfFavorite(user.uid)
                    checkIfUnlocked(user.uid)
                    trackPresence(user.uid)
                }
            } catch (e: Exception) {
                Log.e("BackendAudit", "loadArtist failed for ID: $artistId", e)
                _error.value = "Failed to load artist details"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun trackPresence(uid: String) {
        if (trackedUid == uid) return
        
        // Remove old listener
        presenceListener?.let { 
            trackedUid?.let { oldUid ->
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$oldUid").removeEventListener(it)
            }
        }
        
        trackedUid = uid
        presenceListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val data = snapshot.value as? Map<String, Any> ?: emptyMap()
                _presence.value = data
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").addValueEventListener(presenceListener!!)
    }

    private suspend fun checkIfUnlocked(artistId: String) {
        val currentUserId = userRepository.getCurrentUserId() ?: return
        try {
            val unlockDoc = db.collection("unlockedContacts")
                .document(currentUserId)
                .collection("artists")
                .document(artistId)
                .get()
                .await()
            
            _isUnlocked.value = unlockDoc.exists()
        } catch (e: Exception) {
            Log.e("ArtistDetailsVM", "checkIfUnlocked failed", e)
        }
    }

    fun unlockContact(artistId: String, artistName: String, transactionId: String, amount: Double = 11.0, onSuccess: () -> Unit) {
        val userId = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                val userName = userDoc.getString("name") ?: "Unknown User"

                val unlockData = mapOf(
                    "artistId" to artistId,
                    "artistName" to artistName,
                    "userId" to userId,
                    "userName" to userName,
                    "amount" to amount,
                    "transactionId" to transactionId,
                    "paymentStatus" to "success",
                    "unlockedAt" to System.currentTimeMillis()
                )

                // Save in user-specific collection for fast lookup
                db.collection("unlockedContacts")
                    .document(userId)
                    .collection("artists")
                    .document(artistId)
                    .set(unlockData)
                    .await()

                // Save in global collection for Admin Panel
                db.collection("contactUnlockPayments")
                    .document()
                    .set(unlockData)
                    .await()

                _isUnlocked.value = true
                onSuccess()
            } catch (e: Exception) {
                Log.e("ArtistDetailsVM", "unlockContact failed", e)
            }
        }
    }

    private suspend fun checkIfFavorite(uid: String) {
        val currentUserId = userRepository.getCurrentUserId() ?: return
        try {
            val userDoc = db.collection("users").document(currentUserId).get(Source.SERVER).await()
            val user = userDoc.toObject(User::class.java)
            _isFavorite.value = user?.favorites?.contains(uid) ?: false
        } catch (e: Exception) {
            Log.e("BackendAudit", "checkIfFavorite failed", e)
        }
    }

    fun toggleFavorite(artistId: String) {
        val uid = userRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val currentFav = _isFavorite.value
            _isFavorite.value = !currentFav
            try {
                val userDoc = db.collection("users").document(uid)
                val user = userDoc.get(Source.SERVER).await().toObject(User::class.java) ?: return@launch
                
                val updatedFavorites = user.favorites.toMutableList()
                if (currentFav) {
                    updatedFavorites.remove(artistId)
                } else {
                    updatedFavorites.add(artistId)
                }
                
                FirestoreAudit.verifiedWrite("users", uid) {
                    userDoc.update("favorites", updatedFavorites).await()
                }.onFailure { e -> Log.e("BackendAudit", "Toggle favorite failed", e) }
            } catch (e: Exception) {
                _isFavorite.value = currentFav // Revert on failure
                Log.e("BackendAudit", "toggleFavorite verified write failed", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        presenceListener?.let { 
            trackedUid?.let { uid ->
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("status/$uid").removeEventListener(it)
            }
        }
    }
}
