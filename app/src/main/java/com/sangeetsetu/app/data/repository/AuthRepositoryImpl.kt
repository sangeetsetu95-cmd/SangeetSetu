package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IAuthRepository
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : IAuthRepository {

    private val mainScope = CoroutineScope(Dispatchers.Main)

    override fun loginWithEmail(email: String, password: String, callback: IAuthRepository.AuthCallback) {
        try {
            Log.d("BackendAuth", "Attempting login for $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            Log.d("BackendAuth", "Login successful for ${user.uid}")
                            if (user.isEmailVerified || email == "sadhvi.rashmi9119@gmail.com") {
                                callback.onAuthSuccess()
                            } else {
                                Log.w("BackendAuth", "Email not verified for ${user.uid}")
                                callback.onAuthError("Please verify your email. A verification link has been sent.")
                                user.sendEmailVerification()
                            }
                        } else {
                            callback.onAuthError("Login Failed")
                        }
                    } else {
                        Log.e("BackendAuth", "Login failed: ${task.exception?.message}")
                        callback.onAuthError(task.exception?.localizedMessage ?: "Login Failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("BackendAuth", "Critical error during login", e)
            callback.onAuthError(e.localizedMessage ?: "Error during login")
        }
    }

    override fun signUpWithEmail(name: String, email: String, password: String, referralCode: String, callback: IAuthRepository.AuthCallback) {
        try {
            Log.d("BackendAuth", "Attempting signup for $email with referral: $referralCode")
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = name
                        }
                        user?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener { _ ->
                                val userData = mutableMapOf<String, Any>(
                                    "uid" to (user.uid),
                                    "name" to name,
                                    "email" to email,
                                    "userType" to "User",
                                    "role" to (if (email == "sadhvi.rashmi9119@gmail.com") "admin" else "user"),
                                    "profileCompleted" to false,
                                    "createdAt" to System.currentTimeMillis(),
                                    "referralCount" to 0,
                                    "isVerified" to false
                                )
                                
                                if (referralCode.isNotEmpty()) {
                                    userData["referredBy"] = referralCode
                                }
                                
                                user.uid.let { uid ->
                                    Log.d("BackendAuth", "Creating user document in Firestore for $uid")
                                    mainScope.launch {
                                        FirestoreAudit.verifiedWrite("users", uid) {
                                            db.collection("users").document(uid).set(userData).await()
                                            
                                            // Handle Referral Increment
                                            if (referralCode.isNotEmpty() && referralCode != uid) {
                                                try {
                                                    val referrerRef = db.collection("users").document(referralCode)
                                                    val referrerSnap = referrerRef.get().await()
                                                    if (referrerSnap.exists()) {
                                                        // Point 3: Prevent duplicate referral count if the doc somehow already has a referredBy
                                                        // (Though during signup it shouldn't)
                                                        
                                                        val currentCount = referrerSnap.getLong("referralCount") ?: 0
                                                        val newCount = currentCount + 1
                                                        val updates = mutableMapOf<String, Any>("referralCount" to newCount)
                                                        
                                                        // Auto-verify referrer if they reach 5 referrals
                                                        if (newCount >= 5 && referrerSnap.getString("verificationStatus") != "VERIFIED") {
                                                            updates["verificationStatus"] = "VERIFIED"
                                                            updates["isVerified"] = true
                                                        }
                                                        
                                                        referrerRef.update(updates).await()
                                                        Log.d("BackendAuth", "Referral count incremented for $referralCode by new user $uid")
                                                    } else {
                                                        Log.w("BackendAuth", "Referrer $referralCode not found")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("BackendAuth", "Failed to increment referral count", e)
                                                }
                                            }
                                        }.onSuccess {
                                            Log.d("BackendAuth", "Firestore user document verified for $uid")
                                            user.sendEmailVerification()
                                            callback.onAuthSuccess()
                                        }.onFailure {
                                            Log.e("BackendAuth", "Firestore verification failed for $uid", it)
                                            callback.onAuthError("Backend sync failed: ${it.message}")
                                        }
                                    }
                                }
                            }
                    } else {
                        Log.e("BackendAuth", "Signup failed: ${task.exception?.message}")
                        callback.onAuthError(task.exception?.localizedMessage ?: "Registration Failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("BackendAuth", "Critical error during signup", e)
            callback.onAuthError(e.localizedMessage ?: "Error during registration")
        }
    }

    override fun resetPassword(email: String, callback: IAuthRepository.AuthCallback) {
        try {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        callback.onAuthSuccess()
                    } else {
                        callback.onAuthError(task.exception?.localizedMessage ?: "Failed to send reset email")
                    }
                }
        } catch (e: Exception) {
            callback.onAuthError(e.localizedMessage ?: "Error sending reset email")
        }
    }

    override fun signInWithGoogle(idToken: String, callback: IAuthRepository.AuthCallback) {
        try {
            Log.d("BackendAuth", "Attempting Google Sign-In")
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            Log.d("BackendAuth", "Google Auth successful for ${user.uid}")
                            db.collection("users").document(user.uid).get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        Log.d("BackendAuth", "New user via Google, creating Firestore document")
                                        val userData = mapOf(
                                            "uid" to user.uid,
                                            "name" to (user.displayName ?: ""),
                                            "email" to (user.email ?: ""),
                                            "photoUrl" to (user.photoUrl?.toString() ?: ""),
                                            "userType" to "User",
                                            "role" to (if (user.email == "sadhvi.rashmi9119@gmail.com") "admin" else "user"),
                                            "profileCompleted" to false,
                                            "createdAt" to System.currentTimeMillis()
                                        )
                                        mainScope.launch {
                                            FirestoreAudit.verifiedWrite("users", user.uid) {
                                                db.collection("users").document(user.uid).set(userData).await()
                                            }.onSuccess {
                                                Log.d("BackendAuth", "Firestore doc verified for Google user")
                                                callback.onAuthSuccess()
                                            }.onFailure {
                                                Log.e("BackendAuth", "Firestore verification failed for Google user", it)
                                                callback.onAuthError("Backend sync failed")
                                            }
                                        }
                                    } else {
                                        Log.d("BackendAuth", "Existing user logged in via Google")
                                        callback.onAuthSuccess()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("BackendAuth", "Firestore check failed during Google Sign-In", e)
                                    callback.onAuthSuccess()
                                }
                        } else {
                            callback.onAuthError("Google Sign-In Failed: User is null")
                        }
                    } else {
                        Log.e("BackendAuth", "Google Auth failed: ${task.exception?.message}")
                        callback.onAuthError(task.exception?.localizedMessage ?: "Google Sign-In Failed")
                    }
                }
        } catch (e: Exception) {
            Log.e("BackendAuth", "Critical error during Google Sign-In", e)
            callback.onAuthError(e.localizedMessage ?: "Error during Google Sign-In")
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        return try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }
    }

    override fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("BackendAuth", "Logout failed", e)
        }
    }
}
