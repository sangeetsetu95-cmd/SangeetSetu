package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.components.VerifiedBadge
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val userViewModel: com.sangeetsetu.app.viewmodel.UserViewModel = hiltViewModel()
    val navigationItems by userViewModel.navigationItems.collectAsState()
    val moreItems = navigationItems.filter { it.type == "MoreScreen" && it.isVisible }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf<String?>(null) }

    // Logout Confirmation
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirm_title), color = PremiumGold, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.logout_confirm_msg), color = PremiumWhite) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGold, contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel), color = PremiumWhite)
                }
            },
            containerColor = Color(0xFF162D4D),
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Coming Soon Dialog
    if (showComingSoonDialog != null) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = null },
            title = { Text(stringResource(R.string.coming_soon_title), color = PremiumGold, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.feature_coming_soon_msg, showComingSoonDialog!!), color = PremiumWhite) },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = null }) {
                    Text(stringResource(R.string.got_it), color = PremiumGold, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF162D4D),
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            PremiumHeader(
                title = stringResource(R.string.profile),
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            SangeetSetuDesign.PremiumBackgroundDecoration()
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                ) {
                    // Profile Header
                    PremiumProfileHeader(userProfile) { onNavigate(Screen.EditProfile.route) }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        stringResource(R.string.account_settings_label),
                        color = PremiumGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (moreItems.isEmpty()) {
                        // 1. My Dashboard / Become an Artist
                        if (userProfile?.userType == "Artist") {
                            ProfileMenuItem(Icons.Outlined.Dashboard, stringResource(R.string.my_dashboard)) {
                                onNavigate(Screen.ArtistDashboard.route)
                            }
                        } else {
                            ProfileMenuItem(Icons.Outlined.Star, stringResource(R.string.become_an_artist)) {
                                onNavigate(Screen.DynamicRegistration.route)
                            }
                        }

                        // 2. My Bookings
                        ProfileMenuItem(Icons.Outlined.Event, stringResource(R.string.my_bookings)) {
                            onNavigate(Screen.MyBookings.route)
                        }

                        // 3. Payments
                        ProfileMenuItem(Icons.Outlined.Payments, stringResource(R.string.payments)) {
                            onNavigate(Screen.MyPayments.route)
                        }

                        // 4. Favorite Artists
                        ProfileMenuItem(Icons.Outlined.FavoriteBorder, stringResource(R.string.favorite_artists)) {
                            onNavigate(Screen.SavedArtists.route)
                        }

                        // 5. Saved Addresses
                        ProfileMenuItem(Icons.Outlined.LocationOn, stringResource(R.string.saved_addresses)) {
                            onNavigate(Screen.Addresses.route)
                        }

                        // 6. Language
                        ProfileMenuItem(Icons.Outlined.Language, stringResource(R.string.language)) {
                            onNavigate(Screen.Language.route)
                        }

                        // 7. Display
                        ProfileMenuItem(Icons.Outlined.Palette, stringResource(R.string.display_options)) {
                            onNavigate(Screen.Theme.route)
                        }

                        // 8. Settings
                        ProfileMenuItem(Icons.Outlined.Settings, stringResource(R.string.settings_title)) {
                            onNavigate(Screen.Settings.route)
                        }

                        // 9. Help & Support
                        ProfileMenuItem(Icons.Outlined.HelpOutline, stringResource(R.string.help_support)) {
                            onNavigate(Screen.AISupport.route)
                        }

                        // 10. Contact Us
                        ProfileMenuItem(Icons.Outlined.Call, stringResource(R.string.contact_us)) {
                            onNavigate(Screen.ContactUs.route)
                        }

                        // 11. Privacy Policy
                        ProfileMenuItem(Icons.Outlined.PrivacyTip, stringResource(R.string.privacy_policy)) {
                            onNavigate(Screen.About.route)
                        }

                        // 12. Terms & Conditions
                        ProfileMenuItem(Icons.Outlined.Description, stringResource(R.string.terms_conditions)) {
                            onNavigate(Screen.About.route)
                        }

                        // 13. About Sangeet Setu
                        ProfileMenuItem(Icons.Outlined.Info, stringResource(R.string.about_sangeet_setu)) {
                            onNavigate(Screen.About.route)
                        }
                    } else {
                        moreItems.forEach { item ->
                            ProfileMenuItem(mapOutlinedIcon(item.iconName), item.label) {
                                onNavigate(item.route)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Logout Button in Gold Theme
                    Button(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PremiumGold, 
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumProfileHeader(user: com.sangeetsetu.app.model.User?, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF162D4D))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.sangeetsetu.app.R.drawable.sangeet_setu_logo),
                contentDescription = null,
                modifier = Modifier.size(30.dp).align(Alignment.TopEnd)
            )
        }

        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                border = BorderStroke(2.dp, PremiumGold),
                color = AppBackground
            ) {
                AsyncImage(
                    model = user?.photoUrl?.takeIf { it.isNotEmpty() },
                    contentDescription = "Profile Photo",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.AccountCircle)
                )
            }
            if (user?.verificationStatus == "VERIFIED") {
                Surface(
                    modifier = Modifier.size(28.dp),
                    color = PremiumGold,
                    shape = CircleShape,
                    border = BorderStroke(2.dp, Color(0xFF162D4D))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        VerifiedBadge(modifier = Modifier.size(14.dp), tint = Color.Black)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            user?.name ?: stringResource(R.string.guest_user), 
            color = PremiumWhite, 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Bold
        )
        
        Text(
            user?.email ?: user?.phone ?: "", 
            color = SecondaryText, 
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedButton(
            onClick = onEditClick,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGold)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.edit_profile), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = Color(0xFF162D4D),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title, 
                color = PremiumWhite,
                modifier = Modifier.weight(1f), 
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Icon(Icons.Default.ChevronRight, null, tint = SecondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

fun mapOutlinedIcon(name: String): ImageVector {
    return when(name.lowercase()) {
        "dashboard" -> Icons.Outlined.Dashboard
        "star" -> Icons.Outlined.Star
        "event", "calendar" -> Icons.Outlined.Event
        "payments" -> Icons.Outlined.Payments
        "favorite" -> Icons.Outlined.FavoriteBorder
        "location" -> Icons.Outlined.LocationOn
        "language" -> Icons.Outlined.Language
        "palette" -> Icons.Outlined.Palette
        "settings" -> Icons.Outlined.Settings
        "help" -> Icons.Outlined.HelpOutline
        "call" -> Icons.Outlined.Call
        "privacy" -> Icons.Outlined.PrivacyTip
        "description" -> Icons.Outlined.Description
        "info" -> Icons.Outlined.Info
        else -> Icons.Outlined.Circle
    }
}
