package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.repository.UserRepository
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.util.formatTimeAgo
import com.sangeetsetu.app.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chatId: String,
    receiverId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val participantInfo by viewModel.participantInfo.collectAsState()
    val participantPresence by viewModel.participantPresence.collectAsState()
    val currentUserId = UserRepository.getCurrentUserId() ?: ""
    val listState = rememberLazyListState()

    val violationMessage by viewModel.violationMessage.collectAsState()
    val isChatDisabled by viewModel.isChatDisabled.collectAsState()

    val presenceState = participantPresence["state"] as? String ?: "offline"
    val lastChanged = (participantPresence["last_changed"] as? Long) ?: 0L
    
    val participantPrivacy = participantInfo["presencePrivacy"] as? String ?: "Everyone"
    
    val presenceText = if (participantPrivacy == "Nobody") "" 
                       else if (presenceState == "online") "Online" 
                       else com.sangeetsetu.app.util.formatLastSeen(lastChanged)

    val presenceColor = if (presenceState == "online") Color(0xFF4CAF50) else SecondaryText

    LaunchedEffect(chatId) {
        viewModel.setActiveChat(chatId, receiverId)
    }

    if (violationMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearViolationMessage() },
            title = { Text("सुरक्षा अलर्ट", color = PremiumGold, fontWeight = FontWeight.Bold) },
            text = { Text(violationMessage!!, color = PremiumWhite) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearViolationMessage() }) {
                    Text("समझ गया", color = PremiumGold)
                }
            },
            containerColor = PremiumNavyLight,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = (participantInfo["photo"] as? String)?.ifEmpty { "https://via.placeholder.com/150" },
                            contentDescription = null,
                            modifier = Modifier.size(36.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = participantInfo["name"] as? String ?: "Artist",
                                    color = PremiumWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (participantInfo["isVerified"] == true) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.CheckCircle, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                                }
                            }
                            if (presenceText.isNotEmpty()) {
                                Text(presenceText, color = presenceColor, fontSize = 11.sp)
                            }
                        }
                    }
                },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(
                        message = msg.message,
                        isMe = msg.senderId == currentUserId,
                        timestamp = msg.timestamp,
                        isRead = msg.read,
                        type = msg.type,
                        imageUrl = msg.imageUrl,
                        actionLink = msg.actionLink,
                        actionLabel = msg.actionLabel
                    )
                }
            }

            // Input Area
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PremiumNavyDark,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { if (!isChatDisabled) messageText = it },
                        placeholder = { 
                            Text(
                                if (isChatDisabled) "चैट ब्लॉक है" else "Type a message...", 
                                color = if (isChatDisabled) Color.Red else SecondaryText
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isChatDisabled,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PremiumWhite,
                            unfocusedTextColor = PremiumWhite,
                            focusedContainerColor = PremiumNavy,
                            unfocusedContainerColor = PremiumNavy,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = CardBorder,
                            disabledBorderColor = Color.Red.copy(0.3f),
                            disabledPlaceholderColor = Color.Red.copy(0.5f)
                        ),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank() && !isChatDisabled) {
                                viewModel.sendMessage(receiverId, messageText)
                                messageText = ""
                            }
                        },
                        enabled = !isChatDisabled && messageText.isNotBlank(),
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isChatDisabled) Color.Gray else PremiumGold)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = PremiumNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: String, 
    isMe: Boolean, 
    timestamp: Long, 
    isRead: Boolean,
    type: String = "text",
    imageUrl: String? = null,
    actionLink: String? = null,
    actionLabel: String? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val backgroundColor = if (type == "broadcast") PremiumNavyLight else if (isMe) PremiumGold else PremiumNavyLight
    val contentColor = if (isMe && type != "broadcast") PremiumNavy else PremiumWhite

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = backgroundColor,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 300.dp),
            border = if (type == "broadcast") androidx.compose.foundation.BorderStroke(1.dp, PremiumGold.copy(0.5f)) else null
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (type == "broadcast") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 4.dp)) {
                        Icon(Icons.Default.Notifications, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Official Update", color = PremiumGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)).padding(vertical = 4.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = message,
                    color = contentColor,
                    fontSize = 14.sp
                )

                if (!actionLink.isNullOrEmpty()) {
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(actionLink))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(actionLabel ?: "View More", color = PremiumNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = com.sangeetsetu.app.util.formatTimeAgo(timestamp),
                        color = if (isMe && type != "broadcast") PremiumNavy.copy(0.6f) else SecondaryText,
                        fontSize = 10.sp
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isRead) (if (type == "broadcast") PremiumGold else PremiumNavy) else (if (type == "broadcast") PremiumWhite.copy(0.3f) else PremiumNavy.copy(0.3f)),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
