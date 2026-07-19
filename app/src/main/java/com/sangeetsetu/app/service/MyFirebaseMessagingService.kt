package com.sangeetsetu.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sangeetsetu.app.MainActivity
import com.sangeetsetu.app.R
import com.sangeetsetu.app.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.HttpURLConnection
import java.net.URL

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Priority 1: Data payload
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        } 
        // Priority 2: Notification payload
        else {
            remoteMessage.notification?.let {
                sendBasicNotification(it.title ?: "Sangeet Setu", it.body ?: "")
            }
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: "GENERIC"
        val title = data["title"] ?: "New Message"
        val body = data["body"] ?: ""
        val senderId = data["senderId"] ?: ""
        val senderName = data["senderName"] ?: "Someone"
        val senderPhoto = data["senderPhoto"] ?: ""
        val chatId = data["chatId"] ?: ""
        val receiverId = data["receiverId"] ?: "" // My ID, used for navigation

        serviceScope.launch {
            val user = getCurrentUser()
            val settings = user?.notificationSettings ?: com.sangeetsetu.app.model.NotificationSettings()

            when (type) {
                "CHAT" -> {
                    if (settings.chatNotificationsEnabled) {
                        showChatNotification(body, senderName, senderPhoto, chatId, senderId, settings.soundEnabled, settings.vibrationEnabled)
                    }
                }
                "BROADCAST" -> {
                    if (settings.broadcastNotificationsEnabled) {
                        showBroadcastNotification(title, body, settings.soundEnabled, settings.vibrationEnabled)
                    }
                }
                else -> {
                    sendBasicNotification(title, body)
                }
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
        return try {
            FirebaseFirestore.getInstance().collection("users").document(uid).get().await().toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("FCM", "Error fetching user settings", e)
            null
        }
    }

    private fun showChatNotification(
        body: String, 
        senderName: String, 
        senderPhoto: String, 
        chatId: String, 
        senderId: String,
        sound: Boolean,
        vibration: Boolean
    ) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_SCREEN", "message_detail")
            putExtra("CHAT_ID", chatId)
            putExtra("RECEIVER_ID", senderId) // In message_detail, the other person is the receiver
        }

        val pendingIntent = PendingIntent.getActivity(
            this, chatId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "chat_notifications"
        createNotificationChannel(
            channelId, 
            "Chat Messages", 
            "Notifications for new chat messages",
            NotificationManager.IMPORTANCE_HIGH,
            sound,
            vibration
        )

        val largeIcon = if (senderPhoto.isNotEmpty()) getBitmapFromUrl(senderPhoto) else null

        val person = androidx.core.app.Person.Builder()
            .setName(senderName)
            .setIcon(largeIcon?.let { androidx.core.graphics.drawable.IconCompat.createWithBitmap(it) })
            .build()

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_sangeet_setu_logo)
            .setLargeIcon(largeIcon)
            .setContentTitle(senderName)
            .setContentText(body)
            .setStyle(NotificationCompat.MessagingStyle(androidx.core.app.Person.Builder().setName("Me").build())
                .addMessage(body, System.currentTimeMillis(), person)
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setGroup("chat_group_$chatId")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).notify(chatId.hashCode(), notificationBuilder.build())
        }
    }

    private fun showBroadcastNotification(title: String, body: String, sound: Boolean, vibration: Boolean) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_SCREEN", "announcements")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "broadcast_notifications"
        createNotificationChannel(
            channelId, 
            "Broadcasts", 
            "Important announcements from Sangeet Setu",
            NotificationManager.IMPORTANCE_DEFAULT,
            sound,
            vibration
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_sangeet_setu_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    private fun sendBasicNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel"
        createNotificationChannel(channelId, "General", "General notifications", NotificationManager.IMPORTANCE_DEFAULT, true, true)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        }
    }

    private fun createNotificationChannel(id: String, name: String, description: String, importance: Int, sound: Boolean, vibration: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance).apply {
                this.description = description
                if (!sound) setSound(null, null)
                enableVibration(vibration)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .update("fcmToken", token)
            .addOnSuccessListener { Log.d("FCM", "Token updated on server") }
            .addOnFailureListener { Log.e("FCM", "Failed to update token", it) }
    }

    private fun getBitmapFromUrl(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("FCM", "Error downloading image: ${e.message}")
            null
        }
    }
}
