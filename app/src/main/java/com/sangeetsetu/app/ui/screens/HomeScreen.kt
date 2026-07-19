package com.sangeetsetu.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicExternalOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.R
import com.sangeetsetu.app.model.Banner
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.theme.LocalSystemSettings
import com.sangeetsetu.app.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val BgColor = Color(0xFF0B0B0F)
private val CardBg = Color(0xFF121217)
private val GoldAccent = Color(0xFFD4AF37)
private val TextWhite = Color(0xFFFFFFFF)
private val TextGray = Color(0xFF8E8E93)
private val VerifiedBlue = Color(0xFF2196F3)

@Composable
fun HomeScreen(
    onArtistClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSeeAllArtists: () -> Unit,
    onProfileClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onChatClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchClick: (String?) -> Unit,
    onAnnouncementsClick: () -> Unit,
    onSubscriptionClick: () -> Unit,
    onSupportClick: () -> Unit,
    onLogout: () -> Unit,
    onAboutClick: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val banners by viewModel.banners.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val verifiedArtists by viewModel.verifiedArtists.collectAsState()
    val vipArtists by viewModel.vipArtists.collectAsState()
    val currentUser by viewModel.currentUserData.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    val unreadAnnouncements by viewModel.unreadAnnouncementsCount.collectAsState()
    val systemSettings = LocalSystemSettings.current
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
    
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        com.sangeetsetu.app.ui.components.FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApply = { options ->
                viewModel.fetchArtists("All", options.state, options.district)
                Toast.makeText(context, "Filters Applied", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Scaffold(
        bottomBar = {
            ModernBottomNav(
                currentRoute = "home",
                onHome = {},
                onAnnouncements = onAnnouncementsClick,
                onBookNow = { onCategoryClick("All") },
                onBookings = onBookingsClick,
                onProfile = onProfileClick,
                unreadAnnouncements = unreadAnnouncements
            )
        },
        containerColor = BgColor
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }
            
            // 1. Top Bar
            item {
                HomeTopBar(
                    userName = currentUser?.name ?: "Deepak Kumar",
                    userPhotoUrl = currentUser?.photoUrl ?: "",
                    onNotificationClick = onNotificationClick,
                    onMenuClick = { onProfileClick() },
                    unreadCount = unreadCount,
                    onLocationClick = { 
                        Toast.makeText(context, "Location selection coming soon!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // 2. Search Bar & Filter
            item {
                SearchBarWithFilter(
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
                    },
                    onFilterClick = { 
                        showFilterSheet = true
                    },
                    onHistoryClick = { onSearchClick(null) }
                )
            }

            // 3. Popular Services (Horizontal)
            if (showQuickCategories) {
                item {
                    SectionHeader(title = stringResource(R.string.popular_services), onSeeAll = { onCategoryClick("All") })
                    HorizontalPopularServices(onCategoryClick = onCategoryClick)
                }
            }

            // 4. Featured Banner Slider
            item {
                FeaturedBannerSlider(
                    banners = banners,
                    onBannerClick = { banner ->
                        when (banner.clickActionType) {
                            "Artist" -> onArtistClick(banner.actionUrl)
                            "Category" -> onCategoryClick(banner.actionUrl)
                            else -> {
                                Toast.makeText(context, "Banner: ${banner.title}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            // 5. Popular Services Grid
            item {
                SectionHeader(title = stringResource(R.string.popular_services), onSeeAll = { onCategoryClick("All") })
                PopularServicesGrid(categories = categories, onCategoryClick = { id -> 
                    val cat = categories.find { it.id == id || it.name == id }
                    onCategoryClick(cat?.id ?: id)
                })
            }

            // 6. Verified Artists Section
            if (verifiedArtists.isNotEmpty()) {
                item {
                    PremiumArtistSection(
                        title = stringResource(R.string.verified_artists),
                        artists = verifiedArtists,
                        onArtistClick = onArtistClick,
                        onSeeAll = onSeeAllArtists,
                        badgeIcon = Icons.Rounded.Verified,
                        badgeColor = VerifiedBlue
                    )
                }
            }

            // 7. VIP Artists Section
            if (vipArtists.isNotEmpty()) {
                item {
                    PremiumArtistSection(
                        title = stringResource(R.string.vip_artists),
                        artists = vipArtists,
                        onArtistClick = onArtistClick,
                        onSeeAll = onSeeAllArtists,
                        badgeIcon = Icons.Rounded.Stars,
                        badgeColor = GoldAccent
                    )
                }
            }

            // 8. Featured Artists Section (Standard)
            item {
                PremiumArtistSection(
                    title = stringResource(R.string.featured_artists),
                    artists = artists,
                    onArtistClick = onArtistClick,
                    onSeeAll = onSeeAllArtists
                )
            }

            // 9. Referral Banner
            item {
                ReferralBanner(
                    referralCount = currentUser?.referralCount ?: 0,
                    onInviteClick = {
                        val referralCode = currentUser?.uid ?: ""
                        val shareText = "Hey! Join Sangeet Setu and book your favorite artists. Use my code: $referralCode or link: https://sangeetsetu.com/invite/$referralCode"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

@Composable
fun HomeTopBar(
    userName: String,
    userPhotoUrl: String,
    onNotificationClick: () -> Unit,
    onMenuClick: () -> Unit,
    unreadCount: Int,
    onLocationClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Rounded.Menu, "Menu", tint = GoldAccent, modifier = Modifier.size(28.dp))
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clickable { onLocationClick() },
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.vrindavan_mathura), color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Rounded.KeyboardArrowDown, null, tint = TextGray, modifier = Modifier.size(16.dp))
            }
            Text(stringResource(R.string.welcome_user, userName), color = TextGray, fontSize = 12.sp)
        }

        Box {
            IconButton(onClick = onNotificationClick) {
                Icon(Icons.Rounded.Notifications, null, tint = TextWhite, modifier = Modifier.size(28.dp))
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .background(Color.Red, CircleShape)
                        .border(1.5.dp, BgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(unreadCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, GoldAccent)
        ) {
            AsyncImage(
                model = userPhotoUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                contentDescription = "Profile",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun SearchBarWithFilter(
    onSearchClick: () -> Unit,
    onMicClick: () -> Unit,
    onFilterClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clickable { onSearchClick() },
            shape = RoundedCornerShape(28.dp),
            color = CardBg,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.Search, null, tint = TextGray)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.search_placeholder), color = TextGray, fontSize = 14.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onHistoryClick) {
                    Icon(Icons.Rounded.History, null, tint = TextGray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onMicClick) {
                    Icon(Icons.Rounded.Mic, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        
        YellowActionIcon(icon = Icons.Rounded.Tune, onClick = onFilterClick)
    }
}

@Composable
fun YellowActionIcon(icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(48.dp),
        shape = CircleShape,
        color = GoldAccent
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, null, tint = Color.Black, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun FeaturedBannerSlider(
    banners: List<Banner>,
    onBannerClick: (Banner) -> Unit
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (banners.isNotEmpty()) {
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
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
                color = CardBg
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .weight(1.2f)
                            .padding(20.dp)
                    ) {
                        Surface(
                            color = GoldAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(banner.bannerType, color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(banner.title, color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(banner.subtitle, color = TextGray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            onClick = { onBannerClick(banner) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(stringResource(R.string.book_now_button), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AsyncImage(
                            model = banner.imageUrl.ifEmpty { "https://img.freepik.com/free-photo/vocalist-singing-into-microphone-studio_23-2149301211.jpg" },
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val width by animateDpAsState(targetValue = if (pagerState.currentPage == index) 20.dp else 6.dp)
                val alpha by animateFloatAsState(targetValue = if (pagerState.currentPage == index) 1f else 0.3f)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width, 6.dp)
                        .clip(CircleShape)
                        .background(GoldAccent.copy(alpha = alpha))
                        .clickable {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                )
            }
        }
    }
}

@Composable
fun HorizontalPopularServices(onCategoryClick: (String) -> Unit) {
    val items = listOf(
        stringResource(R.string.cat_singer) to Icons.Rounded.Mic,
        stringResource(R.string.cat_female_singer) to Icons.Rounded.MicExternalOn,
        stringResource(R.string.cat_male_singer) to Icons.Rounded.Mic,
        stringResource(R.string.cat_harmonium) to Icons.Rounded.MusicNote,
        stringResource(R.string.cat_tabla) to Icons.Rounded.Album,
        stringResource(R.string.cat_keyboard) to Icons.Rounded.Piano,
        stringResource(R.string.cat_katha) to Icons.Rounded.MenuBook,
        stringResource(R.string.cat_dj) to Icons.Rounded.GraphicEq
    )
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items) { (name, icon) ->
            Surface(
                modifier = Modifier
                    .clickable { onCategoryClick(name) }
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = GoldAccent.copy(alpha = 0.5f)
                    ),
                shape = RoundedCornerShape(20.dp),
                color = CardBg,
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = name,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PopularServicesGrid(categories: List<Category>, onCategoryClick: (String) -> Unit) {
    val displayCategories = if (categories.isEmpty()) {
        listOf(
            Category(name = stringResource(R.string.cat_harmonium), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063851.png"),
            Category(name = stringResource(R.string.cat_keyboard), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063840.png"),
            Category(name = stringResource(R.string.cat_female_singer), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063825.png"),
            Category(name = stringResource(R.string.cat_male_singer), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063822.png"),
            Category(name = stringResource(R.string.cat_tabla), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063836.png"),
            Category(name = stringResource(R.string.cat_dholak), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063831.png"),
            Category(name = stringResource(R.string.cat_dj), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063840.png"),
            Category(name = stringResource(R.string.cat_more), imageUrl = "https://cdn-icons-png.flaticon.com/512/3063/3063847.png")
        )
    } else categories.take(8)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        displayCategories.chunked(4).forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { category ->
                    Column(
                        modifier = Modifier.weight(1f).clickable { onCategoryClick(category.id) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.aspectRatio(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = CardBg,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                AsyncImage(
                                    model = category.displayImage, 
                                    contentDescription = null, 
                                    modifier = Modifier.fillMaxSize(),
                                    placeholder = painterResource(id = com.sangeetsetu.app.R.drawable.ic_launcher_background),
                                    error = painterResource(id = com.sangeetsetu.app.R.drawable.ic_launcher_background)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(category.name, color = TextWhite, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
                if (row.size < 4) {
                    repeat(4 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun PremiumArtistSection(
    title: String,
    artists: List<User>,
    onArtistClick: (String) -> Unit,
    onSeeAll: () -> Unit,
    badgeIcon: ImageVector? = null,
    badgeColor: Color = GoldAccent
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        SectionHeader(title = title, onSeeAll = onSeeAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(artists) { artist ->
                PremiumArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist.uid) },
                    badgeIcon = badgeIcon,
                    badgeColor = badgeColor
                )
            }
        }
    }
}

@Composable
fun PremiumArtistCard(
    artist: User,
    onClick: () -> Unit,
    badgeIcon: ImageVector? = null,
    badgeColor: Color = GoldAccent
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = CardBg,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp)) {
                AsyncImage(
                    model = artist.photoUrl.ifEmpty { "https://img.freepik.com/free-photo/handsome-confident-smiling-man-with-hands-crossed-chest_176420-18743.jpg" },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val finalBadgeIcon = badgeIcon ?: if (artist.isVerified) Icons.Rounded.Verified else null
                    val finalBadgeColor = if (badgeIcon != null) badgeColor else VerifiedBlue
                    
                    if (finalBadgeIcon != null) {
                        Icon(
                            finalBadgeIcon, 
                            null, 
                            tint = finalBadgeColor, 
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White, CircleShape)
                                .padding(2.dp)
                        )
                    } else {
                        Spacer(Modifier.size(24.dp))
                    }
                    
                    Icon(
                        Icons.Rounded.FavoriteBorder, 
                        null, 
                        tint = Color.White, 
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black.copy(0.3f), CircleShape)
                            .padding(4.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(artist.name, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(artist.category, color = TextGray, fontSize = 12.sp, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    Icon(Icons.Rounded.Star, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                    Text(" ${if (artist.rating > 0) artist.rating else 4.5}", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(" (${(10..200).random()})", color = TextGray, fontSize = 10.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Rounded.LocationOn, null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(artist.city.ifEmpty { "Mathura" }, color = TextGray, fontSize = 11.sp, maxLines = 1)
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("From", color = TextGray, fontSize = 10.sp)
                        Text(
                            "₹${if (artist.experience.isNotEmpty() && artist.experience.toIntOrNull() != null) artist.experience.toInt() * 1000 else 5000}", 
                            color = TextWhite, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(stringResource(R.string.book_now), color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralBanner(referralCount: Int, onInviteClick: () -> Unit) {
    val progress = (referralCount / 5f).coerceIn(0f, 1f)
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(GoldAccent.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CardGiftcard, null, tint = GoldAccent, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.invite_get_verified), color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.invite_friends_desc), color = TextGray, fontSize = 13.sp)
                }
                AsyncImage(
                    model = "https://cdn-icons-png.flaticon.com/512/4213/4213958.png",
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = GoldAccent,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("$referralCount/5 Invites", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${(progress * 100).toInt()}% Complete", color = TextGray, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = onInviteClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Rounded.Share, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.invite_now), color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextWhite, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.see_all_button), color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { onSeeAll() })
    }
}

@Composable
fun ModernBottomNav(
    currentRoute: String,
    onHome: () -> Unit,
    onAnnouncements: () -> Unit,
    onBookNow: () -> Unit,
    onBookings: () -> Unit,
    onProfile: () -> Unit,
    unreadAnnouncements: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgColor)
            .navigationBarsPadding()
            .padding(bottom = 20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(72.dp),
            shape = RoundedCornerShape(36.dp),
            color = Color(0xFF1C1C1E),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Rounded.Home, stringResource(R.string.home), currentRoute == "home", onHome)
                BottomNavItem(
                    Icons.Rounded.ChatBubbleOutline, 
                    "Messages", 
                    currentRoute == "announcements", 
                    onAnnouncements,
                    badgeCount = unreadAnnouncements
                )
                
                Column(
                    modifier = Modifier
                        .offset(y = (-15).dp)
                        .clickable { onBookNow() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = GoldAccent,
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Add, null, tint = Color.Black, modifier = Modifier.size(28.dp))
                        }
                    }
                    Text(stringResource(R.string.explore), color = GoldAccent, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 4.dp))
                }

                BottomNavItem(Icons.Rounded.EventNote, stringResource(R.string.bookings), currentRoute == "bookings", onBookings)
                BottomNavItem(Icons.Rounded.Person, stringResource(R.string.profile), currentRoute == "profile", onProfile)
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector, 
    label: String, 
    isSelected: Boolean, 
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Column(
        modifier = Modifier.clickable { onClick() },
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
                icon,
                contentDescription = label,
                tint = if (isSelected) GoldAccent else TextGray,
                modifier = Modifier.size(26.dp)
            )
        }
        Text(label, color = if (isSelected) GoldAccent else TextGray, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
