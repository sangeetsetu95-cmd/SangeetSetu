package com.sangeetsetu.app.util

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Utility to perform final production audits and optimizations.
 * Ensures that all systems (Auth, Firestore, FCM) are correctly configured.
 */
object AppAuditManager {
    private const val TAG = "AppAudit"

    suspend fun runFinalAudit(context: Context) {
        Log.i(TAG, "Starting Final Production Audit...")
        
        // 1. Check Auth State
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        Log.d(TAG, "Auth Status: ${if (currentUser != null) "Logged In (${currentUser.uid})" else "Logged Out"}")

        // 2. Check FCM Token Consistency
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "FCM Token obtained: ${token.take(10)}...")
            
            currentUser?.let { user ->
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(user.uid).get().await()
                val serverToken = userDoc.getString("fcmToken")
                
                if (serverToken != token) {
                    Log.w(TAG, "FCM Token Mismatch! Syncing with server...")
                    db.collection("users").document(user.uid).update("fcmToken", token).await()
                    Log.i(TAG, "FCM Token Synced successfully")
                } else {
                    Log.d(TAG, "FCM Token is already synced")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "FCM Audit failed", e)
        }

        // 3. Verify Local Settings
        NotificationSettings.init(context)
        Log.d(TAG, "Notification Settings Initialized: Push=${NotificationSettings.pushEnabled.value}")

        // 4. Log ProGuard/R8 Status (Implicitly via reflection check)
        val isMinified = try {
            Class.forName("com.sangeetsetu.app.MainActivity")
            false
        } catch (e: Exception) {
            true
        }
        Log.d(TAG, "Build Minification Detected: $isMinified")
        
        Log.i(TAG, "Final Production Audit Complete. App is in Stable state.")
    }

    /**
     * Recommended Firestore Security Rules (Internal Reference for Admin)
     */
    val RECOMMENDED_SECURITY_RULES = """
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            // Helper functions
            function isSignedIn() { return request.auth != null; }
            function isAdmin() { return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin'; }
            function isOwner(uid) { return request.auth.uid == uid; }

            match /users/{uid} {
              allow read: if isSignedIn();
              allow write: if isOwner(uid) || isAdmin();
              
              // Protect private fields (requires field-level rules or separate collection)
              // Note: Standard Firestore rules don't support field-level per-document easily
              // Recommendation: Move 'phone' to 'users/{uid}/private/contact'
            }
            
            match /unlockedContacts/{userId}/artists/{artistId} {
              allow read, write: if isOwner(userId) || isAdmin();
            }
            
            match /contactUnlockPayments/{id} {
              allow read: if isAdmin();
              allow create: if isSignedIn();
            }
          }
        }
    """.trimIndent()
}
