package com.sangeetsetu.app.domain.repository

import com.google.firebase.auth.FirebaseUser

interface IAuthRepository {
    interface AuthCallback {
        fun onAuthSuccess()
        fun onAuthError(error: String)
    }

    fun loginWithEmail(email: String, password: String, callback: AuthCallback)
    fun signUpWithEmail(name: String, email: String, password: String, referralCode: String = "", callback: AuthCallback)
    fun resetPassword(email: String, callback: AuthCallback)
    fun signInWithGoogle(idToken: String, callback: AuthCallback)
    fun getCurrentUser(): FirebaseUser?
    fun logout()
}
