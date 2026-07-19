package com.sangeetsetu.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.LiveTelecast
import com.sangeetsetu.app.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTelecastScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var liveSessions by remember { mutableStateOf<List<LiveTelecast>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("live_telecasts")
            .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                liveSessions = result.toObjects(LiveTelecast::class.java)
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Telecast", fontWeight = FontWeight.Bold, color = PremiumWhite) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(liveSessions) { session ->
                    LiveSessionCard(session) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.youtubeLink))
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveSessionCard(session: LiveTelecast, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(16.dp).fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = PremiumNavyLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = session.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.height(200.dp).fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
                if (session.isLive) {
                    Surface(
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("LIVE", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.PlayCircle, null, tint = PremiumGold.copy(alpha = 0.8f), modifier = Modifier.size(64.dp).align(Alignment.Center))
            }
            Column(modifier = Modifier.padding(20.dp)) {
                Text(session.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PremiumWhite)
                Spacer(modifier = Modifier.height(8.dp))
                Text(session.description, fontSize = 14.sp, color = PremiumGray, maxLines = 2)
            }
        }
    }
}
