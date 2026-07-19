package com.sangeetsetu.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Announcement
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.UserViewModel
import com.sangeetsetu.app.util.formatTimeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsScreen(
    onBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val announcements by viewModel.announcements.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.markAnnouncementsAsRead()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Announcements", color = PremiumWhite, fontWeight = FontWeight.Bold) },
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
        if (isLoading && announcements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (announcements.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, null, tint = PremiumGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("कोई नया अपडेट नहीं है", color = PremiumGray, fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(announcements) { item ->
                    UserAnnouncementItem(item) { link ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("Announcement", "Failed to open link: $link", e)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserAnnouncementItem(announcement: Announcement, onActionClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column {
            if (!announcement.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = announcement.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        announcement.title,
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        formatTimeAgo(announcement.createdAt),
                        color = PremiumGray,
                        fontSize = 10.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    announcement.content,
                    color = PremiumWhite,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                
                if (!announcement.actionLink.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onActionClick(announcement.actionLink) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Launch, null, tint = PremiumNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(announcement.actionLabel ?: "Learn More", color = PremiumNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
