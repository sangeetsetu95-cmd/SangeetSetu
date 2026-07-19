package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewQuilt
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMoreScreen(onBack: () -> Unit, onNavigate: (String) -> Unit) {
    val strings = LocalStrings.current
    val menuItems = listOf(
        AdminMenuItem(stringResource(R.string.users), Icons.Rounded.People, Screen.AdminUserList.route, Color(0xFF6366F1)),
        AdminMenuItem(stringResource(R.string.payments), Icons.Rounded.Payments, Screen.AdminPayments.route, SuccessColor),
        AdminMenuItem("Contact Unlocks", Icons.Rounded.LockOpen, Screen.AdminUnlockPayments.route, PremiumGold),
        AdminMenuItem(stringResource(R.string.categories), Icons.Rounded.Category, Screen.AdminCategories.route, WarningColor),
        AdminMenuItem("Messages / Announcements", Icons.Rounded.Campaign, Screen.AdminBroadcast.route, Color(0xFF00ACC1)),
        AdminMenuItem("Chat Control", Icons.Rounded.ChatBubble, Screen.AdminChatControl.route, PremiumGold),
        AdminMenuItem(stringResource(R.string.services), Icons.Rounded.DesignServices, Screen.AdminServices.route, Color(0xFF00ACC1)),
        AdminMenuItem(stringResource(R.string.home_layout), Icons.AutoMirrored.Rounded.ViewQuilt, Screen.AdminHomeSections.route, Color(0xFFFF5722)),
        AdminMenuItem("Bhajans", Icons.Rounded.MusicNote, Screen.AdminBhajans.route, PremiumGold),
        AdminMenuItem(stringResource(R.string.notifications), Icons.Rounded.Notifications, Screen.Notifications.route, Color(0xFFEC4899)),
        AdminMenuItem("Analytics", Icons.Rounded.BarChart, Screen.AdminAnalytics.route, Color(0xFF3B82F6)),
        AdminMenuItem(stringResource(R.string.reported_users), Icons.Rounded.Assessment, Screen.ManageReports.route, ErrorColor),
        AdminMenuItem(stringResource(R.string.settings_title), Icons.Rounded.Settings, Screen.Settings.route, AdminSecondaryText),
        AdminMenuItem("RBAC", Icons.Rounded.Security, Screen.AdminRBAC.route, WarningColor),
        AdminMenuItem(stringResource(R.string.help_support), Icons.Rounded.SupportAgent, Screen.AISupport.route, SuccessColor),
        AdminMenuItem(stringResource(R.string.activity_logs), Icons.Rounded.History, Screen.AdminLogs.route, Color(0xFF8B5CF6))
    )

    PremiumAdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Controls", fontWeight = FontWeight.ExtraBold, color = PremiumGold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            bottomBar = {
                AdminBottomNav(currentRoute = Screen.AdminMore.route, onNavigate = onNavigate)
            },
            containerColor = AdminBackground
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                items(menuItems) { item ->
                    AdminMenuCard(item) { onNavigate(item.route) }
                }
            }
        }
    }
}

@Composable
fun AdminMenuCard(item: AdminMenuItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = AdminCardBackground,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(item.color.copy(alpha = 0.12f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, null, tint = item.color, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = item.title,
                color = PremiumWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class AdminMenuItem(val title: String, val icon: ImageVector, val route: String, val color: Color)
