package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Chat
import com.sangeetsetu.app.ui.components.FloatingGlassBottomBar
import com.sangeetsetu.app.ui.components.PremiumToolbar
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBorder
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumNavy
import com.sangeetsetu.app.ui.theme.PremiumNavyLight
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SangeetSetuDesign
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.util.formatTimeAgo
import com.sangeetsetu.app.viewmodel.ChatViewModel
import com.sangeetsetu.app.repository.UserRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onHome: () -> Unit,
    onCategories: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    onChatClick: (String, String) -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val presenceMap by viewModel.presenceMap.collectAsState()
    val currentUserId = UserRepository.getCurrentUserId() ?: ""

    Box(modifier = Modifier.fillMaxSize().background(PremiumNavy)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PremiumToolbar(
                    title = "Messages",
                    onBack = onBack,
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Search, null, tint = PremiumWhite)
                        }
                    }
                )
            },
            bottomBar = {
                FloatingGlassBottomBar(
                    currentRoute = "chat",
                    onHome = onHome,
                    onCategories = onCategories,
                    onBookings = onBookings,
                    onChat = {},
                    onProfile = onProfile
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (chats.isEmpty()) {
                    EmptyChatState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(chats) { chat ->
                            val otherId = chat.participants.find { it != currentUserId } ?: ""
                            ChatItem(
                                chat = chat,
                                presenceState = presenceMap[otherId] ?: "offline",
                                onClick = { onChatClick(chat.id, otherId) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 24.dp),
                                color = CardBorder.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatItem(chat: Chat, presenceState: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Photo with Unread Badge
        Box {
            AsyncImage(
                model = chat.participantPhoto.ifEmpty { "https://via.placeholder.com/150" },
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(PremiumNavyLight),
                contentScale = ContentScale.Crop
            )
            
            // Presence Indicator
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .background(
                        if (presenceState == "online") Color(0xFF4CAF50) else Color.Gray,
                        CircleShape
                    )
                    .border(2.dp, PremiumNavy, CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.participantName,
                    color = PremiumWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily
                )
                if (chat.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = PremiumGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = formatTimeAgo(chat.lastMessageTime),
                    color = SecondaryText,
                    fontSize = 11.sp,
                    fontFamily = PoppinsFamily
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chat.lastMessage,
                    color = if (chat.unreadCount > 0) PremiumWhite else SecondaryText,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = PoppinsFamily,
                    modifier = Modifier.weight(1f)
                )
                
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PremiumGold,
                        shape = CircleShape,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = PremiumNavy,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Badge,
            contentDescription = null,
            tint = PremiumGold.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "अभी कोई चैट नहीं हुई",
            color = PremiumWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFamily
        )
        Text(
            "Start messaging artists to see them here",
            color = SecondaryText,
            fontSize = 14.sp,
            fontFamily = PoppinsFamily
        )
    }
}
