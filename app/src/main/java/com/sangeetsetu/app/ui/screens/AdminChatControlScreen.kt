package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetsetu.app.model.Chat
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.util.formatTimeAgo
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatControlScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allChats by viewModel.allChats.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val systemSettings by viewModel.systemSettings.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var showAutoDeleteDialog by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf<String?>(null) } // UID of user to block
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    val messages by viewModel.selectedChatMessages.collectAsStateWithLifecycle()

    val filteredChats = remember(allChats, searchQuery) {
        if (searchQuery.isEmpty()) allChats
        else allChats.filter { 
            it.participantName.contains(searchQuery, ignoreCase = true) || 
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    if (selectedChat != null) {
        AdminReplyDialog(
            chat = selectedChat!!,
            messages = messages,
            onDismiss = { selectedChat = null },
            onSend = { text ->
                val otherId = selectedChat!!.participants.find { it != "admin" } ?: ""
                if (otherId.isNotEmpty()) {
                    viewModel.sendAdminMessage(otherId, text)
                }
            }
        )
    }

    if (showAutoDeleteDialog) {
        ChatAutoDeleteDialog(
            currentDays = systemSettings.chatAutoDeleteDays,
            onDismiss = { showAutoDeleteDialog = false },
            onConfirm = { days ->
                viewModel.setChatAutoDeleteDays(days) {
                    Toast.makeText(context, "Auto-delete updated to $days days", Toast.LENGTH_SHORT).show()
                    showAutoDeleteDialog = false
                }
            }
        )
    }

    if (showBlockDialog != null) {
        BlockChatDialog(
            userId = showBlockDialog!!,
            onDismiss = { showBlockDialog = null },
            onConfirm = { hours ->
                viewModel.blockUserChat(showBlockDialog!!, hours) {
                    Toast.makeText(context, "User blocked for $hours hours", Toast.LENGTH_SHORT).show()
                    showBlockDialog = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat Control Center", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showAutoDeleteDialog = true }) {
                        Icon(Icons.Default.Settings, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        containerColor = PremiumNavy
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Stats Row
            ChatReportsRow(allChats)

            // Search and Delete All
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search User/Artist Chat...", color = PremiumGray.copy(0.5f)) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = CardBorder
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.deleteAllChats {
                            Toast.makeText(context, "All Chats Wiped Clean!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Chat List
            if (isLoading && allChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else if (filteredChats.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Chats Found", color = PremiumGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredChats) { chat ->
                        AdminChatItem(
                            chat = chat,
                            onClick = {
                                selectedChat = chat
                                viewModel.fetchChatMessages(chat.id)
                            },
                            onDelete = {
                                viewModel.deleteChat(chat.id) {
                                    Toast.makeText(context, "Chat Deleted for Everyone", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onBlockUser = { uid -> showBlockDialog = uid }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatReportsRow(chats: List<Chat>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportCard("Total Chats", chats.size.toString(), Icons.Default.ChatBubble, Modifier.weight(1f))
        ReportCard("Active (24h)", chats.count { it.lastMessageTime > System.currentTimeMillis() - 86400000 }.toString(), Icons.Default.FlashOn, Modifier.weight(1f))
        ReportCard("Participants", (chats.flatMap { it.participants }.distinct().size).toString(), Icons.Default.People, Modifier.weight(1f))
    }
}

@Composable
fun ReportCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
        border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(title, color = PremiumGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun AdminChatItem(chat: Chat, onClick: () -> Unit, onDelete: () -> Unit, onBlockUser: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = PremiumNavyLight),
        border = BorderStroke(0.5.dp, CardBorder.copy(0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(chat.participantName, color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (chat.conversationType == "support") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = SuccessColor.copy(0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    " SUPPORT ", 
                                    color = SuccessColor, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = PremiumGold.copy(0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    " BOOKING ", 
                                    color = PremiumGold, 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(chat.lastMessage, color = PremiumWhite, fontSize = 13.sp, maxLines = 1)
                }
                Text(formatTimeAgo(chat.lastMessageTime), color = PremiumGray, fontSize = 11.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder.copy(0.3f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row {
                    chat.participants.forEach { uid ->
                        IconButton(onClick = { onBlockUser(uid) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Block, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("Block Participant", color = PremiumGray, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(0.8f))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete for Everyone", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReplyDialog(
    chat: Chat,
    messages: List<com.sangeetsetu.app.model.Message>,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f).heightIn(max = 600.dp),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = PremiumNavyLight,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(chat.participantName, color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = PremiumWhite) }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        val isMe = msg.senderId == "admin"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                color = if (isMe) PremiumGold else AppBackground,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.widthIn(max = 240.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(msg.message, color = if (isMe) Color.Black else PremiumWhite, fontSize = 13.sp)
                                    Text(
                                        formatTimeAgo(msg.timestamp),
                                        color = if (isMe) Color.Black.copy(0.6f) else PremiumGray,
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Type reply...", color = PremiumGray) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PremiumWhite,
                            unfocusedTextColor = PremiumWhite,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSend(replyText)
                                replyText = ""
                            }
                        },
                        modifier = Modifier.background(PremiumGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, null, tint = PremiumNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatAutoDeleteDialog(currentDays: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val options = listOf(
        "24 Hours" to 1,
        "3 Days" to 3,
        "7 Days" to 7,
        "30 Days" to 30,
        "Never" to 0
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-Delete Settings", color = PremiumGold) },
        text = {
            Column {
                Text("Select duration after which chats will be automatically deleted.", color = PremiumWhite, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { (label, days) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentDays == days,
                            onClick = { onConfirm(days) },
                            colors = RadioButtonDefaults.colors(selectedColor = PremiumGold, unselectedColor = PremiumGray)
                        )
                        Text(label, color = PremiumWhite, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = PremiumGray) }
        },
        containerColor = PremiumNavyLight
    )
}

@Composable
fun BlockChatDialog(userId: String, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    val options = listOf(
        "24 Hours" to 24,
        "3 Days" to 72,
        "7 Days" to 168,
        "Permanent" to 87600 // ~10 years
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block Chat Access", color = PremiumGold) },
        text = {
            Column {
                Text("User ID: $userId", color = PremiumGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("How long should this user be blocked from sending messages?", color = PremiumWhite, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { (label, hours) ->
                    Button(
                        onClick = { onConfirm(hours) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumNavy),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(label, color = PremiumWhite)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = PremiumGray) }
        },
        containerColor = PremiumNavyLight
    )
}
