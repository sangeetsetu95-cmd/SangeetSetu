package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sangeetsetu.app.model.LiveTelecast
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.components.PremiumToolbar
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLiveScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val liveSessions by viewModel.liveSessions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ytLink by remember { mutableStateOf("") }
    var thumbUrl by remember { mutableStateOf("") }
    var isLive by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<LiveTelecast?>(null) }

    Scaffold(
        topBar = {
            PremiumToolbar(
                title = "Manage Live Sessions",
                onBack = onBack
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Form to Add/Edit - Redesigned with Premium Styling
            Surface(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (editingSession == null) "Add New Live Session" else "Edit Live Session",
                            fontWeight = FontWeight.Bold,
                            color = PremiumGold,
                            fontFamily = PoppinsFamily,
                            fontSize = 18.sp
                        )
                        if (editingSession != null) {
                            Text(
                                "Cancel",
                                color = Color.Red,
                                modifier = Modifier.clickable {
                                    editingSession = null
                                    title = ""; description = ""; ytLink = ""; thumbUrl = ""; isLive = false
                                },
                                fontFamily = PoppinsFamily,
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = title, 
                        onValueChange = { title = it }, 
                        label = { Text("Session Title", color = PremiumGray) }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = description, 
                        onValueChange = { description = it }, 
                        label = { Text("Description", color = PremiumGray) }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = ytLink, 
                        onValueChange = { ytLink = it }, 
                        label = { Text("YouTube Video ID or Link", color = PremiumGray) }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = thumbUrl, 
                        onValueChange = { thumbUrl = it }, 
                        label = { Text("Thumbnail Image URL", color = PremiumGray) }, 
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Checkbox(
                            checked = isLive, 
                            onCheckedChange = { isLive = it },
                            colors = CheckboxDefaults.colors(checkedColor = PremiumGold, uncheckedColor = PremiumGray)
                        )
                        Text("Mark as Live Session", color = Color.White, fontFamily = PoppinsFamily)
                    }

                    PremiumButton(
                        text = if (editingSession == null) "Create Session" else "Update Session",
                        onClick = {
                            val session = LiveTelecast(
                                id = editingSession?.id ?: "",
                                title = title,
                                description = description,
                                thumbnailUrl = thumbUrl,
                                youtubeLink = ytLink,
                                isLive = isLive,
                                startTime = editingSession?.startTime ?: System.currentTimeMillis()
                            )
                            viewModel.saveLiveSession(session) {
                                Toast.makeText(context, "Session Saved Successfully", Toast.LENGTH_SHORT).show()
                                title = ""; description = ""; ytLink = ""; thumbUrl = ""; isLive = false
                                editingSession = null
                            }
                        },
                        isLoading = isLoading,
                        enabled = title.isNotEmpty() && ytLink.isNotEmpty()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Active Sessions", 
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp), 
                color = PremiumGold, 
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFamily,
                fontSize = 18.sp
            )

            liveSessions.forEach { session ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .clickable {
                            editingSession = session
                            title = session.title
                            description = session.description
                            ytLink = session.youtubeLink
                            thumbUrl = session.thumbnailUrl
                            isLive = session.isLive
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = CardBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(session.title, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = PoppinsFamily)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (session.isLive) {
                                    Surface(
                                        color = Color.Red,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.size(width = 40.dp, height = 16.dp)
                                    ) {
                                        Text(
                                            "LIVE", 
                                            color = Color.White, 
                                            fontSize = 10.sp, 
                                            fontWeight = FontWeight.Bold, 
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    if (session.isLive) "Publicly visible as Live" else "Recorded Session", 
                                    color = PremiumGray, 
                                    fontSize = 12.sp,
                                    fontFamily = PoppinsFamily
                                )
                            }
                        }
                        IconButton(onClick = {
                            viewModel.deleteLiveSession(session.id) {
                                Toast.makeText(context, "Session Deleted", Toast.LENGTH_SHORT).show()
                            }
                        }) { Icon(Icons.Default.Delete, null, tint = Color.Red.copy(alpha = 0.7f)) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
