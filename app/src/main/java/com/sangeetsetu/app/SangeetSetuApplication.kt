package com.sangeetsetu.app

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import com.cloudinary.android.MediaManager
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

import com.sangeetsetu.app.ai.AISettings
import com.sangeetsetu.app.util.NotificationSettings

@HiltAndroidApp
class SangeetSetuApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        Log.d("StartupTrace", "Application.onCreate started")
        
        // 1. Initialize Firebase FIRST (Essential for many other components)
        try {
            initializeFirebase()
        } catch (t: Throwable) {
            Log.e("StartupTrace", "FATAL Firebase initialization failure in Application", t)
        }

        // 2. Initialize App Settings
        try {
            Log.d("StartupTrace", "Initializing settings...")
            AppSettings.init(this)
            AISettings.init(this)
            NotificationSettings.init(this)
        } catch (t: Throwable) {
            Log.e("StartupTrace", "Failed to initialize settings", t)
        }
        
        // 3. Initialize Cloudinary
        try {
            initializeCloudinary()
        } catch (t: Throwable) {
            Log.e("StartupTrace", "Failed to initialize Cloudinary", t)
        }
    }

    private fun initializeFirebase() {
        // Force Firebase App initialization
        if (FirebaseApp.getApps(this).isEmpty()) {
            try {
                FirebaseApp.initializeApp(this)
                Log.d("StartupTrace", "FirebaseApp.initializeApp successful")
                
                // Configure Firestore for better production performance and offline support
                val firestore = FirebaseFirestore.getInstance()
                val settings = FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                        .setSizeBytes(100 * 1024 * 1024) // 100MB cache
                        .build())
                    .build()
                firestore.firestoreSettings = settings
                Log.d("StartupTrace", "Firestore Settings initialized with Persistence")
            } catch (e: Exception) {
                Log.e("StartupTrace", "FirebaseApp.initializeApp FAILED", e)
                return // Stop further Firebase init if this fails
            }
        }

        // Initialize App Check safely
        try {
            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            if (BuildConfig.DEBUG) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d("StartupTrace", "App Check (Debug) installed")
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d("StartupTrace", "App Check (Play Integrity) installed")
            }
        } catch (e: Exception) {
            Log.e("StartupTrace", "App Check initialization failed", e)
        }
    }

    private fun initializeCloudinary() {
        try {
            val config = mapOf(
                "cloud_name" to "dz2xrf9q",
                "secure" to true
            )
            
            try {
                MediaManager.init(this, config)
                Log.i("SangeetSetuApp", "Cloudinary initialized")
            } catch (e: IllegalStateException) {
                Log.w("SangeetSetuApp", "Cloudinary already initialized")
            }
        } catch (e: Exception) {
            Log.e("SangeetSetuApp", "Cloudinary init error", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }
}
