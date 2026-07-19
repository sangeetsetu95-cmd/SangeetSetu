package com.sangeetsetu.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.sangeetsetu.app.domain.repository.IAuthRepository
import com.sangeetsetu.app.navigation.NavGraph
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.MyApplicationTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject lateinit var authRepository: IAuthRepository
    @Inject lateinit var presenceManager: com.sangeetsetu.app.util.PresenceManager
    private lateinit var googleSignInClient: GoogleSignInClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("StartupTrace", "MainActivity.onCreate started")
        
        try {
            AppSettings.init(this)

            if (FirebaseApp.getApps(this).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(this)
                } catch (e: Exception) {
                    Log.e("StartupTrace", "Firebase initialization failed", e)
                }
            }

            enableEdgeToEdge()
            
            val webClientId = try { getString(R.string.default_web_client_id) } catch (e: Exception) { "" }
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .apply { if (webClientId.isNotEmpty()) requestIdToken(webClientId) }
                .build()
            googleSignInClient = GoogleSignIn.getClient(this, gso)

            askNotificationPermission()
            initializeFCM()

            lifecycleScope.launch {
                com.sangeetsetu.app.util.AppAuditManager.runFinalAudit(this@MainActivity)
            }

            setContent {
                val userViewModel: com.sangeetsetu.app.viewmodel.UserViewModel = hiltViewModel()
                val systemSettings by userViewModel.systemSettings.collectAsState(com.sangeetsetu.app.model.SystemSettings())
                
                MyApplicationTheme(systemSettings = systemSettings) {
                    MainContent(
                        authRepository = authRepository,
                        googleSignInClient = googleSignInClient,
                        intent = intent
                    )
                }
            }
            
        } catch (t: Throwable) {
            Log.e("StartupTrace", "FATAL CRASH in MainActivity.onCreate", t)
            setContent {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Startup Error: ${t.localizedMessage}", color = Color.White)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        presenceManager.startTracking()
    }

    override fun onStop() {
        super.onStop()
        presenceManager.stopTracking()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initializeFCM() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveTokenToFirestore(task.result)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "FCM not available", e)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        lifecycleScope.launch {
            try {
                db.collection("users").document(uid).update("fcmToken", token).await()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to sync FCM token", e)
            }
        }
    }
}

@Composable
fun MainContent(
    authRepository: IAuthRepository,
    googleSignInClient: GoogleSignInClient,
    intent: Intent
) {
    val navController = rememberNavController()
    
    val targetScreen = intent.getStringExtra("TARGET_SCREEN") ?: "splash"
    val chatId = intent.getStringExtra("CHAT_ID")
    val receiverId = intent.getStringExtra("RECEIVER_ID")

    val startDestination = when(targetScreen) {
        "message_detail" -> if (chatId != null && receiverId != null) Screen.MessageDetail.createRoute(chatId, receiverId) else Screen.Home.route
        "announcements" -> Screen.Announcements.route
        else -> Screen.Splash.route
    }

    NavGraph(navController, authRepository, googleSignInClient, startDestination)
}
