package com.sangeetsetu.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Banner
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.theme.LocalSystemSettings
import com.sangeetsetu.app.ui.theme.LocalStrings
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.viewmodel.HomeViewModel
import com.sangeetsetu.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- PREMIUM COLOR PALETTE ---
private val PremiumDarkNavy = Color(0xFF0B1020)
private val PremiumGold = Color(0xFFD4AF37)
private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.05f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.1f)
private val PremiumTextWhite = Color(0xFFFFFFFF)
private val PremiumTextGray = Color(0xFFB8C1CC)

@Composable
fun PremiumHomeScreen(
    onArtistClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllArtists: () -> Unit,
    onProfileClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onSearchClick: (String?) -> Unit,
    onNotificationClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onChatClick: () -> Unit = {},
    userViewModel: UserViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.uiState.collectAsState()
    val banners = homeUiState.banners
    val categories = homeUiState.categories
    val artists = homeUiState.featuredArtists
    val verifiedArtists = artists.filter { it.isVerified || it.verificationStatus == "VERIFIED" }
    val vipArtists = homeUiState.vipArtists
    val currentUser by userViewModel.currentUserData.collectAsState()
    val unreadCount by userViewModel.unreadNotificationsCount.collectAsState()
    val unreadAnnouncements by userViewModel.unreadAnnouncementsCount.collectAsState()
    val isLoading = homeUiState.isLoading
    val systemSettings = LocalSystemSettings.current
    val strings = LocalStrings.current
    val showQuickCategories = systemSettings.features["quick_categories"] ?: false
    val context = LocalContext.current

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                onSearchClick(spokenText)
            }
        }
    }

    Scaffold(
        bottomBar = {
            PremiumBottomNavigation(
                currentRoute = "home",
                onHome = {},
                onAnnouncements = onAnnouncementsClick,
                onChat = onChatClick,
                onBookNow = { onCategoryClick("All") },
                onBookings = onBookingsClick,
                onProfile = onProfileClick,
                unreadAnnouncements = unreadAnnouncements
            )
        },
        containerColor = PremiumDarkNavy
    ) { padding ->
        if (isLoading && banners.isEmpty() && categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                LinearProgressIndicator(color = PremiumGold, modifier = Modifier.fillMaxWidth(0.5f))
            }
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }
            // Top Header
            item {
                PremiumHomeHeader(
                    userName = currentUser?.name ?: "User",
                    location = currentUser?.district ?: "Location",
                    userPhotoUrl = currentUser?.photoUrl ?: "",
                    unreadCount = unreadCount,
                    onProfileClick = onProfileClick,
                    onNotificationClick = onNotificationClick,
                    onMenuClick = { onProfileClick() },
                    onLocationClick = { Toast.makeText(context, "Location selection coming soon", Toast.LENGTH_SHORT).show() }
                )
            }

            // Search Bar
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    PremiumSearchBar(
                        onSearchClick = { onSearchClick(null) },
                        onMicClick = { 
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "कलाकार, सेवा का नाम बोलें...")
                            }
                            try {
                                voiceLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Voice Search not available", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // Horizontal Categories
            if (showQuickCategories) {
                item {
                    PremiumQuickCategories(categories = categories, onCategoryClick = onCategoryClick)
                }
            }

            // 1. Hero Banner Slider
            item {
                if (banners.isNotEmpty()) {
                    PremiumHeroBannerSlider(
                        banners = banners,
                        onBannerClick = { banner ->
                            when (banner.clickActionType) {
                                "Artist" -> onArtistClick(banner.actionUrl)
                                "Category" -> onCategoryClick(banner.actionUrl)
                                "Custom" -> { /* Open URL */ }
                                else -> { /* Do nothing */ }
                            }
                        }
                    )
                }
            }

            // 2. Popular Services (Hero Banner के तुरंत नीचे)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PremiumSectionHeader(title = strings.categories, onSeeAll = { onCategoryClick("All") })
                PremiumPopularCategoriesGrid(categories = categories, onCategoryClick = { 
                    android.util.Log.d("HomeScreen", "Popular Service Clicked: Name='${it.name}', ID='${it.id}'")
                    onCategoryClick(it.id) 
                })
            }

            // 3. Verified Artists (3 Profiles)
            if (verifiedArtists.isNotEmpty() || (isLoading && artists.isEmpty())) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumSectionHeader(title = strings.verifiedArtists, onSeeAll = onSeeAllArtists)
                    if (isLoading && verifiedArtists.isEmpty()) {
                        PremiumArtistSkeletonRow()
                    } else {
                        PremiumFeaturedArtistsRow(artists = verifiedArtists, onArtistClick = onArtistClick)
                    }
                }
            }

            // 4. VIP Artists (3 Profiles)
            if (vipArtists.isNotEmpty() || (isLoading && artists.isEmpty())) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumSectionHeader(title = strings.vipArtists, onSeeAll = onSeeAllArtists)
                    if (isLoading && vipArtists.isEmpty()) {
                        PremiumArtistSkeletonRow()
                    } else {
                        PremiumFeaturedArtistsRow(artists = vipArtists, onArtistClick = onArtistClick)
                    }
                }
            }

            // 5. Invite & Get Verified
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PremiumInviteFriendsCard(
                    referralCount = currentUser?.referralCount ?: 0,
                    onInviteClick = {
                        val referralCode = currentUser?.uid ?: ""
                        val shareText = "Join Sangeet Setu and book your favorite artists. Use my code: $referralCode or link: https://sangeetsetu.com/invite/$referralCode"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(100.dp)) } // Enough padding for floating bottom bar
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(24.dp),
        color = GlassWhite,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun PremiumHomeHeader(
    userName: String,
    location: String,
    userPhotoUrl: String,
    unreadCount: Int,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp), // Reduced from 12.dp
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Hamburger Menu
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(44.dp) // Slightly larger touch target
                .clip(CircleShape)
                .background(GlassWhite)
        ) {
            Icon(Icons.Rounded.Menu, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
        }

        // Center Left: Location & Welcome Section
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clickable { onLocationClick() },
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocationOn,
                    null,
                    tint = PremiumGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = location,
                    color = PremiumTextWhite,
                    fontSize = 15.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (userName.isNotEmpty()) "Welcome, $userName" else "Welcome, Vrindavan TV",
                color = PremiumTextGray,
                fontSize = 11.sp,
                fontFamily = PoppinsFamily,
                fontWeight = FontWeight.Medium
            )
        }

        // Right: Notification Bell
        Box(modifier = Modifier.padding(end = 12.dp)) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GlassWhite)
            ) {
                Icon(Icons.Rounded.Notifications, null, tint = PremiumTextWhite, modifier = Modifier.size(24.dp))
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Red, CircleShape)
                        .border(1.5.dp, PremiumDarkNavy, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        unreadCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right End: Profile Image
        Surface(
            modifier = Modifier
                .size(44.dp)
                .clickable { onProfileClick() },
            shape = CircleShape,
            border = BorderStroke(1.5.dp, PremiumGold)
        ) {
            AsyncImage(
                model = userPhotoUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = com.sangeetsetu.app.R.drawable.ic_launcher_background)
            )
        }
    }
}

@Composable
fun PremiumSearchBar(onSearchClick: () -> Unit, onMicClick: () -> Unit) {
    val strings = LocalStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(28.dp),
            color = GlassWhite,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, null, tint = PremiumTextGray, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                    strings.searchPlaceholder,
                    color = PremiumTextGray, 
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f), 
                    fontFamily = PoppinsFamily
                )
                IconButton(onClick = onMicClick) {
                    Icon(Icons.Rounded.Mic, null, tint = PremiumGold, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

@Composable
fun PremiumQuickCategories(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    // If we have categories from Firestore, we try to match the hardcoded names to their IDs
    // If not found, we use the name as fallback (which will be handled by the repository)
    val items = listOf("गायक", "हारमोनियम", "तबला", "कथा", "DJ", "महिला गायिका", "पुरुष गायक", "कीबोर्ड", "ढोलक")
    var selectedCategory by remember { mutableStateOf("गायक") }
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
                items(items) { catName ->
                    val isSelected = selectedCategory == catName
                    val categoryId = categories.find { it.name.equals(catName, ignoreCase = true) }?.id ?: catName
                    
                    Surface(
                        modifier = Modifier
                            .height(42.dp)
                            .clickable { 
                                selectedCategory = catName
                                android.util.Log.d("HomeScreen", "Quick Category Clicked: Name='$catName', ResolvedID='$categoryId'")
                                onCategoryClick(categoryId)
                            },
                        shape = RoundedCornerShape(21.dp),
                        color = if (isSelected) PremiumGold else GlassWhite,
                        border = BorderStroke(1.dp, if (isSelected) PremiumGold else GlassBorder)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catName,
                                color = if (isSelected) PremiumDarkNavy else PremiumTextWhite,
                        fontSize = 14.sp,
                        fontFamily = PoppinsFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumHeroBannerSlider(banners: List<Banner>, onBannerClick: (Banner) -> Unit) {
    if (banners.isEmpty()) return
    
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        while(true) {
            delay(5000)
            if (banners.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.padding(top = 4.dp, bottom = 0.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val banner = banners[page]
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onBannerClick(banner) },
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF151929),
                border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.2f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xFF0A0E21).copy(alpha = 0.7f))
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = banner.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = banner.subtitle,
                            color = PremiumGold,
                            fontSize = 13.sp,
                            fontFamily = PoppinsFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        
        // Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val width by animateDpAsState(targetValue = if (index == pagerState.currentPage) 20.dp else 8.dp)
                val alpha by animateFloatAsState(targetValue = if (index == pagerState.currentPage) 1f else 0.3f)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width, 6.dp)
                        .clip(CircleShape)
                        .background(PremiumGold.copy(alpha = alpha))
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                )
            }
        }
    }
}

@Composable
fun PremiumArtistSkeletonRow() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        userScrollEnabled = false
    ) {
        items(3) {
            Surface(
                modifier = Modifier.width(240.dp),
                shape = RoundedCornerShape(24.dp),
                color = GlassWhite,
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column {
                    Box(modifier = Modifier.height(240.dp).fillMaxWidth().background(Color.White.copy(alpha = 0.05f)))
                    Column(modifier = Modifier.padding(16.dp)) {
                        Box(modifier = Modifier.width(140.dp).height(20.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)))
                        Spacer(Modifier.height(8.dp))
                        Box(modifier = Modifier.width(100.dp).height(14.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)))
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Box(modifier = Modifier.width(60.dp).height(30.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp)))
                            Box(modifier = Modifier.width(80.dp).height(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)))
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PremiumSectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = PremiumTextWhite,
            fontSize = 32.sp, // As requested: 32sp
            fontWeight = FontWeight.Bold,
            fontFamily = PoppinsFamily
        )
        Text(
            text = "See All",
            color = PremiumGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onSeeAll() },
            fontFamily = PoppinsFamily
        )
    }
}

@Composable
fun PremiumFeaturedArtistsRow(artists: List<User>, onArtistClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(artists) { artist ->
            PremiumArtistCard(artist = artist, onClick = { onArtistClick(artist.uid) })
        }
    }
}

@Composable
fun PremiumArtistCard(artist: User, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(240.dp) // Slightly wider for 16:9 feel
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = GlassWhite,
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f) // 16:9 Aspect Ratio as requested
            ) {
                AsyncImage(
                    model = artist.photoUrl.ifEmpty { "https://img.freepik.com/free-photo/handsome-confident-smiling-man-with-hands-crossed-chest_176420-18743.jpg" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Badges Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Verified Badge
                        if (artist.isVerified) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                    .border(1.dp, Color(0xFF2196F3).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Verified,
                                    null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // VIP/Premium Badge
                        if (artist.isVip) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color.White.copy(alpha = 0.9f), CircleShape)
                                    .border(1.dp, PremiumGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Stars,
                                    null,
                                    tint = PremiumGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Rating Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Star, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (artist.rating > 0) artist.rating.toString() else "4.5", 
                                color = Color.White, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = artist.name,
                    color = PremiumTextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = PoppinsFamily,
                    maxLines = 1, // Single line with ellipsis for consistency
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = artist.category.ifEmpty { "Artist" },
                    color = PremiumTextGray,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Starting Price", color = PremiumTextGray, fontSize = 10.sp)
                        Text(
                            text = if (artist.experience.isNotEmpty()) "₹${artist.experience}k+" else "₹5k+", 
                            color = PremiumGold, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 18.sp
                        )
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Book Now", color = PremiumDarkNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumPopularCategoriesGrid(categories: List<Category>, onCategoryClick: (Category) -> Unit) {
    val items = if (categories.isEmpty()) {
        listOf(
            Category(name = "Harmonium", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063851.png"),
            Category(name = "Keyboard", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063840.png"),
            Category(name = "Singer (Female)", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063825.png"),
            Category(name = "Singer (Male)", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063822.png"),
            Category(name = "Tabla", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063836.png"),
            Category(name = "कथा वाचक", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063837.png"),
            Category(name = "ढोलक वादक", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063831.png"),
            Category(name = "More", imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063847.png")
        )
    } else categories.take(8)

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        val rows = items.chunked(4)
        rows.forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = if (index == rows.size - 1) 2.dp else 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { category ->
                    PremiumCategoryItem(category, modifier = Modifier.weight(1f), onClick = { onCategoryClick(category) })
                }
                if (row.size < 4) {
                    repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun PremiumCategoryItem(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxWidth()
                .shadow(6.dp, CircleShape, spotColor = PremiumGold.copy(alpha = 0.2f)),
            shape = CircleShape,
            color = GlassWhite,
            border = BorderStroke(1.5.dp, PremiumGold.copy(alpha = 0.4f)) // Uniform border
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) { // Increased padding for smaller icon
                AsyncImage(
                    model = category.displayImage,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit, // Dynamic crop-free look
                    placeholder = painterResource(id = com.sangeetsetu.app.R.drawable.ic_launcher_background),
                    error = painterResource(id = com.sangeetsetu.app.R.drawable.ic_launcher_background)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = category.name,
            color = PremiumTextWhite,
            fontSize = 11.sp,
            fontFamily = PoppinsFamily,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun PremiumInviteFriendsCard(referralCount: Int, onInviteClick: () -> Unit) {
    val progress = (referralCount / 5f).coerceIn(0f, 1f)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        color = GlassWhite,
        border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(Color.Transparent, PremiumGold.copy(alpha = 0.05f))
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Invite & Get Verified", color = PremiumTextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = PoppinsFamily)
                    Text("Share with 5 friends to unlock badge", color = PremiumTextGray, fontSize = 14.sp, fontFamily = PoppinsFamily)
                }
                Icon(
                    Icons.Rounded.CardGiftcard,
                    null,
                    tint = PremiumGold,
                    modifier = Modifier.size(44.dp)
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Progress Bar (Full width)
            LinearProgressIndicator(
                progress = { progress }, 
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = PremiumGold,
                trackColor = Color.White.copy(alpha = 0.1f),
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$referralCount/5 Invites", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}% Complete", color = PremiumTextGray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onInviteClick,
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp) // M3 standard height
            ) {
                Icon(Icons.Rounded.Share, null, tint = PremiumDarkNavy, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share Now", color = PremiumDarkNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PremiumBottomNavigation(
    currentRoute: String,
    onHome: () -> Unit,
    onAnnouncements: () -> Unit,
    onChat: () -> Unit,
    onBookNow: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    unreadAnnouncements: Int
) {
    val strings = LocalStrings.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 32.dp), // Increased top padding to prevent FAB cut
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(36.dp),
            color = Color.Black.copy(alpha = 0.8f), // Premium Glass Dark
            border = BorderStroke(1.dp, GlassBorder),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumNavItem(Icons.Rounded.Home, strings.home, currentRoute == "home", onHome)
                PremiumNavItem(
                    Icons.Rounded.ChatBubbleOutline, 
                    strings.chat, 
                    currentRoute == "chat", 
                    onChat,
                    badgeCount = unreadAnnouncements
                )
                
                Spacer(modifier = Modifier.width(64.dp)) // Space for FAB

                PremiumNavItem(Icons.Rounded.ConfirmationNumber, strings.bookings, currentRoute == "bookings", onBookings)
                PremiumNavItem(Icons.Rounded.Person, strings.profile, currentRoute == "profile", onProfile)
            }
        }

        // Central Explore Button (Floating)
        Box(
            modifier = Modifier
                .offset(y = (-30).dp) // Centered relative to bar
                .size(64.dp)
                .shadow(12.dp, CircleShape, spotColor = PremiumGold)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(PremiumGold, Color(0xFFB8860B))) // Gold Gradient
                )
                .clickable { onBookNow() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Add, null, tint = PremiumDarkNavy, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun PremiumNavItem(
    icon: ImageVector, 
    label: String, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    IconButton(onClick = onClick) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(containerColor = Color.Red) {
                            Text(badgeCount.toString(), color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) PremiumGold else PremiumTextGray,
                    modifier = Modifier.size(26.dp)
                )
            }
            // dot की वजह से icon की position न बदले, इसलिए Box हमेशा रहेगा
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(4.dp)
                    .background(if (isSelected) PremiumGold else Color.Transparent, CircleShape)
            )
        }
    }
}

