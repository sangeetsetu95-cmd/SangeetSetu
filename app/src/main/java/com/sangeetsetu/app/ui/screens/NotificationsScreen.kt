package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

import com.sangeetsetu.app.model.Notification
import com.sangeetsetu.app.repository.UserRepository
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val db = remember { FirebaseFirestore.getInstance() }
    val currentUserId = UserRepository.getCurrentUserId() ?: ""
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        
        val listener = db.collection("notifications")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        android.util.Log.e("Notifications", "Index required. Data being prepared.", error)
                        db.collection("notifications").whereEqualTo("userId", currentUserId).get().addOnSuccessListener { s ->
                            notifications = s.toObjects(Notification::class.java).sortedByDescending { it.timestamp }
                            isLoading = false
                        }
                    } else {
                        isLoading = false
                    }
                    return@addSnapshotListener
                }
                notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                isLoading = false
                
                // Mark unread as read using a batch to avoid multiple listener triggers
                val unreadDocs = snapshot?.documents?.filter { it.getBoolean("isRead") == false }
                if (!unreadDocs.isNullOrEmpty()) {
                    val batch = db.batch()
                    unreadDocs.forEach { doc ->
                        batch.update(doc.reference, "isRead", true)
                    }
                    batch.commit().addOnFailureListener { e ->
                        android.util.Log.e("Notifications", "Failed to mark notifications as read", e)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No new notifications", color = PremiumGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { item ->
                    NotificationItem(item)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Notifications, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(notification.title, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (notification.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notification.message, color = PremiumGray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    com.sangeetsetu.app.util.formatTimeAgo(notification.timestamp),
                    color = PremiumGray.copy(0.6f),
                    fontSize = 11.sp
                )
            }
        }
    }
}
