package com.sangeetsetu.app.util

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val rtdb: FirebaseDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var connectionListener: ValueEventListener? = null

    fun startTracking() {
        val uid = auth.currentUser?.uid ?: return
        
        val userStatusDatabaseRef = rtdb.getReference("/status/$uid")
        val connectedRef = rtdb.getReference(".info/connected")

        connectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    val status = mapOf(
                        "state" to "online",
                        "last_changed" to ServerValue.TIMESTAMP
                    )

                    // When I disconnect, update the status to offline
                    userStatusDatabaseRef.onDisconnect().setValue(
                        mapOf(
                            "state" to "offline",
                            "last_changed" to ServerValue.TIMESTAMP
                        )
                    ).continueWith {
                        // The onDisconnect is set, now set the current status to online
                        userStatusDatabaseRef.setValue(status)
                        updateFirestorePresence(uid, true)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("PresenceManager", "Listener cancelled", error.toException())
            }
        }
        connectedRef.addValueEventListener(connectionListener!!)
    }

    fun stopTracking() {
        val uid = auth.currentUser?.uid ?: return
        connectionListener?.let {
            rtdb.getReference(".info/connected").removeEventListener(it)
        }
        
        val userStatusDatabaseRef = rtdb.getReference("/status/$uid")
        userStatusDatabaseRef.setValue(
            mapOf(
                "state" to "offline",
                "last_changed" to ServerValue.TIMESTAMP
            )
        ).continueWith {
            updateFirestorePresence(uid, false)
        }
    }

    private fun updateFirestorePresence(uid: String, isOnline: Boolean) {
        scope.launch {
            try {
                val updates = mutableMapOf<String, Any>(
                    "isOnline" to isOnline,
                    "lastSeen" to System.currentTimeMillis()
                )
                if (isOnline) {
                    updates["lastOnlineTimestamp"] = System.currentTimeMillis()
                }
                
                firestore.collection("users").document(uid).update(updates).await()
            } catch (e: Exception) {
                Log.e("PresenceManager", "Failed to update Firestore presence", e)
            }
        }
    }
}
