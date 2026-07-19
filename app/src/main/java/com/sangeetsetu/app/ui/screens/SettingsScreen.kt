package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.AppSettings
import com.sangeetsetu.app.AppTheme
import com.sangeetsetu.app.ai.AISettings
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.model.NotificationSettings

import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.viewmodel.ProfileViewModel

import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isAdmin: Boolean = false,
    onNavigate: (String) -> Unit = {},
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val error by profileViewModel.error.collectAsState()
    val userProfile by profileViewModel.userProfile.collectAsState()
    
    // States from persistent settings
    var aiSupportEnabled by remember { mutableStateOf(AISettings.isAIEnabled.value) }
    
    // Notification states from Firestore profile
    val currentNotifSettings = userProfile?.notificationSettings ?: NotificationSettings()

    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Who can see my Online Status?", color = PremiumGold) },
            text = {
                Column {
                    val currentPrivacy = userProfile?.presencePrivacy ?: "Everyone"
                    listOf("Everyone", "Nobody").forEach { option ->
                        ThemeOption(
                            label = option,
                            isSelected = currentPrivacy == option,
                            onClick = {
                                profileViewModel.updatePresencePrivacy(option)
                                showPrivacyDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Cancel", color = PremiumGray)
                }
            },
            containerColor = PremiumNavyLight,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.settings_title), 
                            color = PremiumGold, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                if (isAdmin) {
                    AdminBottomNav(currentRoute = Screen.Settings.route, onNavigate = onNavigate)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 1. Display & Language
                SettingsCard(title = "App Experience") {
                    ActionOption(Icons.Rounded.Palette, stringResource(R.string.theme_option)) {
                        onNavigate(Screen.Theme.route)
                    }
                    ActionOption(Icons.Rounded.Language, stringResource(R.string.language_option)) {
                        onNavigate(Screen.Language.route)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. AI Support Toggle
                SettingsCard(title = "Smart Features") {
                    ToggleOption(
                        label = "Enable AI Assistant", 
                        checked = aiSupportEnabled,
                        onCheckedChange = {
                            aiSupportEnabled = it
                            AISettings.setAIEnabled(context, it)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Notification Settings (Updated with Firestore Sync)
                SettingsCard(title = "Push Notifications") {
                    ToggleOption("Chat Notifications", currentNotifSettings.chatNotificationsEnabled) {
                        profileViewModel.updateNotificationSettings(currentNotifSettings.copy(chatNotificationsEnabled = it))
                    }
                    ToggleOption("Broadcasts & Offers", currentNotifSettings.broadcastNotificationsEnabled) {
                        profileViewModel.updateNotificationSettings(currentNotifSettings.copy(broadcastNotificationsEnabled = it))
                    }
                    ToggleOption("Notification Sound", currentNotifSettings.soundEnabled) {
                        profileViewModel.updateNotificationSettings(currentNotifSettings.copy(soundEnabled = it))
                    }
                    ToggleOption("Vibration", currentNotifSettings.vibrationEnabled) {
                        profileViewModel.updateNotificationSettings(currentNotifSettings.copy(vibrationEnabled = it))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Privacy & Security
                SettingsCard(title = stringResource(R.string.privacy_security_section)) {
                    ActionOption(Icons.Rounded.Visibility, "Online Status Presence") {
                        showPrivacyDialog = true
                    }
                    ActionOption(Icons.Rounded.Lock, stringResource(R.string.change_password)) {
                        onNavigate(Screen.ChangePassword.route)
                    }
                    ActionOption(Icons.Rounded.Logout, stringResource(R.string.logout), color = Color.Red.copy(alpha = 0.8f)) {
                        profileViewModel.logout()
                        onBack()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. About Section
                SettingsCard(title = stringResource(R.string.about_sangeet_setu)) {
                    InfoOption(stringResource(R.string.version), "1.0.0")
                    ActionOption(Icons.Rounded.Info, "Privacy Policy") { onNavigate(Screen.About.route) }
                    ActionOption(Icons.Rounded.Description, "Terms & Conditions") { onNavigate(Screen.About.route) }
                }

                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    "Made with ❤️ in Bharat",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = PremiumGray,
                    fontSize = 10.sp
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title, 
            color = PremiumGold, 
            fontSize = 14.sp, 
            fontWeight = FontWeight.Bold, 
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = CardBackground.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ThemeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PremiumGold, unselectedColor = PremiumGray)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = if (isSelected) PremiumGold else PremiumWhite, fontSize = 15.sp)
    }
}

@Composable
fun ToggleOption(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PremiumWhite, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PremiumGold,
                checkedTrackColor = PremiumGold.copy(alpha = 0.5f),
                uncheckedThumbColor = PremiumGray,
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
fun ActionOption(icon: ImageVector, label: String, color: Color = PremiumWhite, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (color == PremiumWhite) PremiumGold else color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, color = color, fontSize = 15.sp)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = PremiumGray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun InfoOption(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PremiumWhite, fontSize = 15.sp)
        Text(value, color = PremiumGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
