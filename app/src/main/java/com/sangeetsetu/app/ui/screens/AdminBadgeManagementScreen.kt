package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.BadgeConfig
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBadgeManagementScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel
) {
    val badges by viewModel.badges.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val verifiedBadge = badges.find { it.id == "verified" } ?: BadgeConfig(id = "verified")
    val vipBadge = badges.find { it.id == "vip" } ?: BadgeConfig(id = "vip")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Badge Management", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = PremiumGold,
                    trackColor = Color.Transparent
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    BadgeManagementCard(
                        title = "Verified Badge",
                        description = "Displayed on verified artist profiles and cards.",
                        badge = verifiedBadge,
                        onUpdate = { uri, isEnabled ->
                            viewModel.saveBadge(verifiedBadge.copy(isEnabled = isEnabled), uri, {
                                Toast.makeText(context, "Verified badge updated", Toast.LENGTH_SHORT).show()
                            }, { Toast.makeText(context, it, Toast.LENGTH_LONG).show() })
                        },
                        onDelete = {
                            viewModel.deleteBadge("verified", verifiedBadge.cloudinaryPublicId) {
                                Toast.makeText(context, "Verified badge deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                item {
                    BadgeManagementCard(
                        title = "VIP Badge",
                        description = "Displayed on VIP artist profiles and premium listings.",
                        badge = vipBadge,
                        onUpdate = { uri, isEnabled ->
                            viewModel.saveBadge(vipBadge.copy(isEnabled = isEnabled), uri, {
                                Toast.makeText(context, "VIP badge updated", Toast.LENGTH_SHORT).show()
                            }, { Toast.makeText(context, it, Toast.LENGTH_LONG).show() })
                        },
                        onDelete = {
                            viewModel.deleteBadge("vip", vipBadge.cloudinaryPublicId) {
                                Toast.makeText(context, "VIP badge deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeManagementCard(
    title: String,
    description: String,
    badge: BadgeConfig,
    onUpdate: (Uri?, Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReplaceDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            if (badge.imageUrl.isNotEmpty()) {
                showReplaceDialog = true
            } else {
                onUpdate(uri, badge.isEnabled)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Badge?") },
            text = { Text("Are you sure you want to delete this badge? It will be removed from all profiles immediately.") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = CardBackground,
            titleContentColor = PremiumWhite,
            textContentColor = SecondaryText
        )
    }

    if (showReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showReplaceDialog = false; selectedUri = null },
            title = { Text("Replace Badge?") },
            text = { Text("This will replace the current badge image. Continue?") },
            confirmButton = {
                TextButton(onClick = { 
                    onUpdate(selectedUri, badge.isEnabled)
                    showReplaceDialog = false
                    selectedUri = null
                }) {
                    Text("Replace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReplaceDialog = false; selectedUri = null }) { Text("Cancel") }
            },
            containerColor = CardBackground,
            titleContentColor = PremiumWhite,
            textContentColor = SecondaryText
        )
    }

    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(description, color = SecondaryText, fontSize = 12.sp)
                }
                
                Switch(
                    checked = badge.isEnabled,
                    onCheckedChange = { onUpdate(null, it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PremiumGold,
                        checkedTrackColor = PremiumGold.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppBackground)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (badge.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = badge.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(32.dp)
                            .background(PremiumGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.CloudUpload, null, tint = PremiumGold.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload Badge Image", color = SecondaryText, fontSize = 14.sp)
                    }
                }
            }

            if (badge.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Badge")
                }
            }
        }
    }
}
