package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.rounded.ViewQuilt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.util.formatAmount
import com.sangeetsetu.app.util.formatTimeAgo
import com.sangeetsetu.app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val activities by viewModel.recentActivities.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    PremiumAdminTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AdminSideDrawer(
                    onNavigate = onNavigate,
                    onLogout = onLogout
                ) { scope.launch { drawerState.close() } }
            },
            gesturesEnabled = true
        ) {
            Scaffold(
                topBar = {
                    AdminHeader(
                        unreadCount = unreadCount,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onNotificationClick = { onNavigate(Screen.Notifications.route) },
                        onProfileClick = { onNavigate(Screen.AdminProfile.route) }
                    )
                },
                bottomBar = {
                    AdminBottomNav(
                        currentRoute = Screen.AdminDashboard.route,
                        onNavigate = onNavigate
                    )
                },
                containerColor = AdminBackground
            ) { paddingValues ->
                if (isLoading && stats.totalUsers == 0) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        item { WelcomeGreeting() }
                        item { QuickStatsGrid(stats) }
                        item { PendingActionsSection(stats, onNavigate) }
                        item { QuickActionsSection(onNavigate, viewModel) }
                        item { RecentActivitySection(activities, onNavigate) }
                        item { SystemStatusSection() }
                        item { Spacer(modifier = Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeGreeting() {
    val greeting = when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..16 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }

    Column {
        Text(text = greeting, color = AdminSecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Text(text = stringResource(R.string.welcome_back_admin), color = PremiumWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHeader(unreadCount: Int, onMenuClick: () -> Unit, onNotificationClick: () -> Unit, onProfileClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.sangeetsetu.app.R.drawable.sangeet_setu_logo),
                    contentDescription = null,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Sangeet Setu", color = PremiumGold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = PremiumGold) }
        },
        actions = {
            Box(modifier = Modifier.padding(end = 8.dp)) {
                IconButton(onClick = onNotificationClick) {
                    Icon(Icons.Rounded.Notifications, null, tint = PremiumGold)
                }
                if (unreadCount > 0) {
                    Surface(
                        color = ErrorColor,
                        shape = CircleShape,
                        modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center, modifier = Modifier.padding(top = 1.dp)
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier.padding(end = 12.dp).size(38.dp).clickable { onProfileClick() },
                shape = CircleShape, color = AdminCardBackground, border = BorderStroke(1.dp, PremiumGold.copy(0.4f))
            ) {
                Icon(Icons.Rounded.Person, null, tint = PremiumGold, modifier = Modifier.padding(6.dp))
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AdminBackground)
    )
}

@Composable
fun QuickStatsGrid(stats: com.sangeetsetu.app.model.AdminStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.users), stats.totalUsers.toString(), "+${stats.newRegistrationsToday} Today", Icons.Rounded.People, Color(0xFF6366F1), Modifier.weight(1f))
            StatCard(stringResource(R.string.artists), stats.totalArtists.toString(), "${stats.activeArtists} Active", Icons.Rounded.Mic, SuccessColor, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(stringResource(R.string.bookings), stats.totalBookings.toString(), "+${stats.todayBookings} Today", Icons.Rounded.Bookmark, Color(0xFF3B82F6), Modifier.weight(1f))
            StatCard(stringResource(R.string.categories), stats.totalCategories.toString(), "Active", Icons.Rounded.Category, WarningColor, Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(label: String, value: String, trend: String, icon: ImageVector, iconColor: Color, modifier: Modifier) {
    Surface(
        modifier = modifier, color = AdminCardBackground, shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(modifier = Modifier.size(40.dp).background(iconColor.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(label, color = AdminSecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = PremiumWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(trend, color = SuccessColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PendingActionsSection(stats: com.sangeetsetu.app.model.AdminStats, onNavigate: (String) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.pending_actions), color = PremiumWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.view_all), color = PremiumGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigate(Screen.AdminRequests.route) })
        }
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(), color = AdminCardBackground, shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.08f))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                PendingActionItem(stringResource(R.string.booking_approvals), stats.pendingBookings, Color(0xFF3B82F6), Icons.Rounded.Event) { onNavigate(Screen.AdminRequests.route) }
                PendingActionItem(stringResource(R.string.reported_users), 1, ErrorColor, Icons.Rounded.Flag, isLast = true) { onNavigate(Screen.ManageReports.route) }
            }
        }
    }
}

@Composable
fun PendingActionItem(label: String, count: Int, color: Color, icon: ImageVector, isLast: Boolean = false, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(color.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, color = PremiumWhite, fontSize = 15.sp, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        if (count > 0) {
            Surface(color = ErrorColor, shape = CircleShape, modifier = Modifier.size(22.dp)) {
                Text(count.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = AdminSecondaryText, modifier = Modifier.size(14.dp))
    }
    if (!isLast) HorizontalDivider(color = Color.White.copy(0.08f), modifier = Modifier.padding(horizontal = 14.dp))
}

@Composable
fun QuickActionsSection(onNavigate: (String) -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var isAuditing by remember { mutableStateOf(false) }
    var isCategoryMigrating by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.quick_actions), color = PremiumWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isCategoryMigrating) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PremiumGold, strokeWidth = 2.5.dp)
                } else {
                    Text(
                        stringResource(R.string.fix_categories), 
                        color = PremiumGold, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isCategoryMigrating = true
                            viewModel.runCategoryAudit { count ->
                                isCategoryMigrating = false
                                android.widget.Toast.makeText(context, "Mapping Complete: Updated $count artists", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }

                if (isAuditing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = SuccessColor, strokeWidth = 2.5.dp)
                } else {
                    Text(
                        stringResource(R.string.status_audit), 
                        color = SuccessColor, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            isAuditing = true
                            viewModel.runArtistStatusAudit { fixedCount ->
                                isAuditing = false
                                android.widget.Toast.makeText(context, "Audit Complete: Fixed $fixedCount artists", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        val actions = listOf(
            QuickActionData(stringResource(R.string.add_artist), Icons.Rounded.PersonAdd, Color(0xFF6366F1), Screen.AdminArtistList.route),
            QuickActionData("Unlock History", Icons.Rounded.LockOpen, SuccessColor, Screen.AdminUnlockPayments.route),
            QuickActionData(stringResource(R.string.add_banner), Icons.Rounded.Image, SuccessColor, Screen.Banners.route),
            QuickActionData(stringResource(R.string.categories), Icons.Rounded.Category, WarningColor, Screen.AdminCategories.route),
            QuickActionData(stringResource(R.string.app_info), Icons.Rounded.Info, Color(0xFFEC4899), Screen.AdminAppSettings.route),
            QuickActionData(stringResource(R.string.states), Icons.Rounded.Map, Color(0xFFFF5722), Screen.AdminConfig.createRoute("states", "States")),
            QuickActionData(stringResource(R.string.districts), Icons.Rounded.LocationCity, Color(0xFF3B82F6), Screen.AdminConfig.createRoute("districts", "Districts")),
            QuickActionData(stringResource(R.string.specialities), Icons.Rounded.Star, Color(0xFF8B5CF6), Screen.AdminConfig.createRoute("specialities", "Specialities")),
            QuickActionData(stringResource(R.string.instruments), Icons.Rounded.MusicNote, Color(0xFF00ACC1), Screen.AdminConfig.createRoute("instruments", "Instruments")),
            QuickActionData(stringResource(R.string.event_types), Icons.Rounded.Event, WarningColor, Screen.AdminConfig.createRoute("event_types", "Event Types")),
            QuickActionData(stringResource(R.string.languages), Icons.Rounded.Language, Color(0xFF6366F1), Screen.AdminConfig.createRoute("languages", "Languages")),
            QuickActionData(stringResource(R.string.exp_levels), Icons.Rounded.TrendingUp, SuccessColor, Screen.AdminConfig.createRoute("experience_levels", "Experience Levels")),
            QuickActionData(stringResource(R.string.home_layout), Icons.AutoMirrored.Rounded.ViewQuilt, Color(0xFF6366F1), Screen.AdminHomeSections.route),
            QuickActionData(stringResource(R.string.badges), Icons.Rounded.Verified, WarningColor, Screen.AdminBadgeManagement.route),
            QuickActionData(stringResource(R.string.form_builder), Icons.Rounded.ListAlt, Color(0xFF00ACC1), Screen.AdminFormBuilder.route),
            QuickActionData(stringResource(R.string.live_sessions), Icons.Rounded.LiveTv, ErrorColor, Screen.ManageLive.route),
            QuickActionData(stringResource(R.string.offers), Icons.Rounded.LocalOffer, SuccessColor, Screen.AdminOffers.route),
            QuickActionData(stringResource(R.string.services), Icons.Rounded.Build, Color(0xFF8B5CF6), Screen.AdminServices.route),
            QuickActionData(stringResource(R.string.nav_menu), Icons.Rounded.MenuOpen, Color(0xFF3B82F6), Screen.AdminConfig.createRoute("navigation", "App Navigation")),
            QuickActionData(stringResource(R.string.popups), Icons.Rounded.AddAlert, Color(0xFFEC4899), Screen.AdminConfig.createRoute("popups", "App Popups"))
        )
        actions.chunked(4).forEach { rowActions ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { 
                rowActions.forEach { QuickActionItem(it, onNavigate, Modifier.weight(1f)) } 
                if (rowActions.size < 4) {
                    repeat(4 - rowActions.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun QuickActionItem(action: QuickActionData, onNavigate: (String) -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier.clickable { onNavigate(action.route) }, color = AdminCardBackground,
        shape = RoundedCornerShape(20.dp), border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(44.dp).background(action.color.copy(0.12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, null, tint = action.color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(action.label, color = AdminSecondaryText, fontSize = 11.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<com.sangeetsetu.app.model.AdminLog>, onNavigate: (String) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.recent_activity), color = PremiumWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.view_all), color = PremiumGold, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigate(Screen.AdminLogs.route) })
        }
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(), color = AdminCardBackground, shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (activities.isEmpty()) Text(stringResource(R.string.no_recent_activity), color = AdminSecondaryText, modifier = Modifier.padding(8.dp))
                else activities.take(4).forEachIndexed { index, log -> TimelineItem(formatTimeAgo(log.timestamp), log.action, index == activities.take(4).size - 1) }
            }
        }
    }
}

@Composable
fun TimelineItem(time: String, action: String, isLast: Boolean) {
    Row(verticalAlignment = Alignment.Top) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(10.dp).background(PremiumGold, CircleShape))
            if (!isLast) Box(modifier = Modifier.width(2.dp).height(44.dp).background(PremiumGold.copy(0.25f)))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            Text(time, color = AdminSecondaryText, fontSize = 11.sp)
            Text(action, color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SystemStatusSection() {
    Column {
        Text(stringResource(R.string.system_status), color = PremiumWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(), color = AdminCardBackground, shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                StatusProgressItem(stringResource(R.string.storage), 0.82f, "82%", WarningColor)
                StatusProgressItem(stringResource(R.string.firebase), 1.0f, "100%", SuccessColor)
                StatusProgressItem(stringResource(R.string.server), 1.0f, "100%", SuccessColor)
            }
        }
    }
}

@Composable
fun StatusProgressItem(label: String, progress: Float, percentage: String, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(percentage, color = AdminSecondaryText, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = color, trackColor = Color.White.copy(0.08f))
    }
}

@Composable
fun AdminSideDrawer(onNavigate: (String) -> Unit, onLogout: () -> Unit, closeDrawer: () -> Unit) {
    ModalDrawerSheet(drawerContainerColor = AdminBackground, drawerContentColor = PremiumWhite, modifier = Modifier.width(310.dp)) {
        Spacer(modifier = Modifier.height(56.dp))
        Row(modifier = Modifier.padding(26.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.sangeetsetu.app.R.drawable.sangeet_setu_logo),
                contentDescription = null,
                modifier = Modifier.size(54.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text("Sangeet Setu", color = PremiumGold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.admin_panel), color = AdminSecondaryText, fontSize = 13.sp, letterSpacing = 1.sp)
            }
        }
        HorizontalDivider(color = Color.White.copy(0.1f), modifier = Modifier.padding(horizontal = 26.dp))
        Spacer(modifier = Modifier.height(20.dp))
        val items = listOf(
            AdminDrawerItem(stringResource(R.string.dashboard), Icons.Rounded.Dashboard, Screen.AdminDashboard.route),
            AdminDrawerItem(stringResource(R.string.artists), Icons.Rounded.Mic, Screen.AdminArtistList.route),
            AdminDrawerItem(stringResource(R.string.users), Icons.Rounded.People, Screen.AdminUserList.route),
            AdminDrawerItem(stringResource(R.string.bookings), Icons.Rounded.Bookmark, Screen.AdminBookings.route),
            AdminDrawerItem(stringResource(R.string.subscriptions), Icons.Rounded.Star, Screen.AdminSubscriptions.route),
            AdminDrawerItem(stringResource(R.string.activity_logs), Icons.Rounded.History, Screen.AdminLogs.route)
        )
        LazyColumn {
            items(items) { item ->
                NavigationDrawerItem(
                    label = { Text(item.label, fontWeight = FontWeight.SemiBold) },
                    selected = false,
                    onClick = { onNavigate(item.route); closeDrawer() },
                    icon = { Icon(item.icon, null, tint = PremiumGold) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent, 
                        unselectedTextColor = PremiumWhite,
                        selectedContainerColor = PremiumGold.copy(alpha = 0.15f),
                        selectedTextColor = PremiumGold
                    ),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.logout), color = ErrorColor, fontWeight = FontWeight.Bold) }, selected = false,
                    onClick = { onLogout(); closeDrawer() },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ErrorColor) },
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

data class AdminDrawerItem(val label: String, val icon: ImageVector, val route: String)
data class QuickActionData(val label: String, val icon: ImageVector, val color: Color, val route: String)
