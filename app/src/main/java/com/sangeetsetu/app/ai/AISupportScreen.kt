package com.sangeetsetu.app.ai

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISupportScreen(
    onBack: () -> Unit,
    userViewModel: com.sangeetsetu.app.viewmodel.UserViewModel = hiltViewModel()
) {
    val viewModel: AISupportViewModel = viewModel()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    
    val systemSettings by userViewModel.systemSettings.collectAsState()
    val isAIEnabled = systemSettings.features["ai_assistant"] ?: true

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        // Luxury Background Decoration
        SangeetSetuDesign.PremiumBackgroundDecoration()
        
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.ai_assistant_title), 
                                color = PremiumWhite, 
                                style = Typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isGenerating) PremiumGold else Color.Green, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.6.dp))
                                Text(
                                    if (isGenerating) stringResource(R.string.typing) else stringResource(R.string.online), 
                                    color = if (isGenerating) PremiumGold else Color.Green, 
                                    fontSize = 10.sp, 
                                    fontFamily = NotoSansDevanagariFamily
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.clearChat() }) {
                            Icon(Icons.Default.Refresh, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (isAIEnabled) {
                    // AI Chat Interface
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(messages) { (text, isMe) ->
                                SupportChatBubble(text, isMe)
                            }
                        }
                    }

                    // Chat Input Section
                    Surface(
                        color = CardBackground.copy(alpha = 0.95f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        shadowElevation = 20.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = { 
                                    Text(
                                        stringResource(R.string.ask_ai_hint), 
                                        color = SecondaryText, 
                                        fontFamily = NotoSansDevanagariFamily,
                                        fontSize = 14.sp
                                    ) 
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = AppBackground.copy(alpha = 0.5f),
                                    unfocusedContainerColor = AppBackground.copy(alpha = 0.5f),
                                    focusedTextColor = PremiumWhite,
                                    unfocusedTextColor = PremiumWhite,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = PremiumGold
                                ),
                                shape = RoundedCornerShape(24.dp),
                                maxLines = 4
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Send Button with Neon Glow
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (textInput.isNotBlank() && !isGenerating) 
                                            GoldenGradient
                                        else 
                                            Brush.linearGradient(listOf(Color.White.copy(0.05f), Color.White.copy(0.05f)))
                                    )
                                    .clickable(enabled = !isGenerating && textInput.isNotBlank()) {
                                        viewModel.sendMessage(textInput)
                                        textInput = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send, 
                                    null, 
                                    tint = if (textInput.isNotBlank() && !isGenerating) Color.Black else PremiumWhite.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Disabled State UI (Luxury Version)
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                color = Color.White.copy(0.03f),
                                border = BorderStroke(1.dp, PremiumGold.copy(0.1f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.SmartToy, 
                                        null, 
                                        tint = PremiumGold.copy(alpha = 0.4f), 
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(32.dp))
                            Text(
                                stringResource(R.string.ai_disabled_title), 
                                color = PremiumWhite, 
                                style = Typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                fontFamily = NotoSansDevanagariFamily
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                stringResource(R.string.ai_disabled_msg),
                                color = SecondaryText,
                                style = Typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                fontFamily = NotoSansDevanagariFamily,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(54.dp).fillMaxWidth(0.6f),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                            ) {
                                Text(
                                    stringResource(R.string.go_back),
                                    color = Color.Black, 
                                    fontWeight = FontWeight.Black,
                                    fontFamily = NotoSansDevanagariFamily,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupportChatBubble(message: String, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMe) PremiumGold else CardBackground,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isMe) 20.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 20.dp
            ),
            border = if (isMe) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier.widthIn(max = 280.dp),
            shadowElevation = if (isMe) 8.dp else 2.dp
        ) {
            Text(
                message,
                color = if (isMe) Color.Black else PremiumWhite,
                style = Typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                lineHeight = 22.sp,
                fontFamily = if (message.any { it.code > 127 }) NotoSansDevanagariFamily else PoppinsFamily
            )
        }
    }
}
