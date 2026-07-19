package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.components.VerifiedBadge
import com.sangeetsetu.app.ui.components.VipBadge
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ArtistDetailsViewModel

@OptIn(ExperimentalLayoutApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun ArtistDetailsScreen(
    artistId: String,
    onBack: () -> Unit,
    onBook: (String) -> Unit,
    onChat: () -> Unit = {},
    onCall: () -> Unit = {},
    viewModel: ArtistDetailsViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val artist by viewModel.artist.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val presence by viewModel.presence.collectAsState()

    var showUnlockSheet by remember { mutableStateOf(false) }

    LaunchedEffect(artistId) {
        viewModel.loadArtist(artistId)
    }

    if (showUnlockSheet) {
        com.sangeetsetu.app.ui.components.ContactUnlockSheet(
            artistName = artist?.name ?: "",
            onDismiss = { showUnlockSheet = false },
            onPaymentSuccess = { txId ->
                artist?.let {
                    viewModel.unlockContact(it.uid, it.name, txId) {
                        showUnlockSheet = false
                        android.widget.Toast.makeText(context, "Contact Unlocked Successfully!", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (artist == null && isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(AppBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PremiumGold)
        }
        return
    }

    val currentArtist = artist ?: return
    
    val presenceState = presence["state"] as? String ?: "offline"
    val lastChanged = (presence["last_changed"] as? Long) ?: 0L
    
    val presenceText = if (currentArtist.presencePrivacy == "Nobody") "" 
                       else if (presenceState == "online") "🟢 Online" 
                       else if (lastChanged > 0) "🔴 ${com.sangeetsetu.app.util.formatLastSeen(lastChanged)}"
                       else "🔴 Offline"

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            Surface(
                color = CardBackground,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // WhatsApp Button
                    IconButton(
                        onClick = {
                            if (isUnlocked) {
                                val cleanPhone = currentArtist.phone.replace("+", "").replace(" ", "").removePrefix("91")
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/91$cleanPhone?text=नमस्ते ${currentArtist.name}, मैं आपसे संगेत सेतु के माध्यम से संपर्क कर रहा हूँ।"))
                                context.startActivity(intent)
                            } else {
                                showUnlockSheet = true
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isUnlocked) Color(0xFF25D366).copy(0.1f) else Color.White.copy(0.05f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_send), 
                            null, 
                            tint = if (isUnlocked) Color(0xFF25D366) else PremiumGray
                        )
                    }

                    // Call Button
                    IconButton(
                        onClick = {
                            if (isUnlocked) {
                                val cleanPhone = currentArtist.phone.replace("+", "").replace(" ", "")
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$cleanPhone"))
                                context.startActivity(intent)
                            } else {
                                showUnlockSheet = true
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isUnlocked) PremiumGold.copy(0.1f) else Color.White.copy(0.05f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Call, 
                            null, 
                            tint = if (isUnlocked) PremiumGold else PremiumGray
                        )
                    }

                    IconButton(
                        onClick = onChat,
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(0.05f), RoundedCornerShape(16.dp))
                    ) {
                        Icon(Icons.Default.Message, null, tint = PremiumGold)
                    }

                    PremiumButton(
                        text = if (isUnlocked) "अभी बुक करें" else "Unlock Contact – ₹11",
                        onClick = { 
                            if (isUnlocked) onBook(currentArtist.uid)
                            else showUnlockSheet = true
                        },
                        modifier = Modifier.weight(1f).height(56.dp)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image Section
            Box(modifier = Modifier.fillMaxWidth().height(480.dp)) {
                AsyncImage(
                    model = currentArtist.photoUrl,
                    contentDescription = currentArtist.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(0.5f), Color.Black)
                            )
                        )
                )

                // Top Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconButton(
                            onClick = {
                                val sendIntent: android.content.Intent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Check out ${currentArtist.name} on Sangeet Setu: https://sangeetsetu.app/artist/${currentArtist.uid}")
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.Share, null, tint = PremiumWhite)
                        }
                        IconButton(
                            onClick = { viewModel.toggleFavorite(artistId) },
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                null,
                                tint = if (isFavorite) Color.Red else PremiumWhite
                            )
                        }
                    }
                }

                // Info Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentArtist.verificationStatus == "VERIFIED") {
                            VerifiedBadge(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        if (currentArtist.isVip) {
                            VipBadge(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        
                        if (presenceText.isNotEmpty()) {
                            Surface(
                                color = Color.Black.copy(0.4f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, Color.White.copy(0.2f))
                            ) {
                                Text(
                                    presenceText, 
                                    color = if (presenceState == "online") Color(0xFF4CAF50) else Color.White.copy(0.7f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            currentArtist.name,
                            color = PremiumWhite,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CinzelFamily,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = { /* Follow Logic */ },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Follow", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Text(
                        currentArtist.category,
                        color = PremiumGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NotoSansDevanagariFamily
                    )

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                            Text(" ${currentArtist.rating} (${currentArtist.reviewsCount} समीक्षाएं)", color = Color.White, fontSize = 14.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=${currentArtist.city}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.LocationOn, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                            Text(" ${currentArtist.city}", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Tabs / Sections
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
                
                // Biography
                DetailSection("जीवनी") {
                    Text(
                        currentArtist.aboutMe.ifEmpty { "एक अनुभवी और प्रसिद्ध कलाकार जो अपनी कला के माध्यम से दर्शकों को मंत्रमुग्ध कर देते हैं।" },
                        color = Color.White.copy(0.7f),
                        fontSize = 15.sp,
                        lineHeight = 24.sp,
                        fontFamily = NotoSansDevanagariFamily
                    )
                }

                // Experience & Languages
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatBox(modifier = Modifier.weight(1f), label = "अनुभव", value = "${currentArtist.experience ?: 10}+ वर्ष")
                    StatBox(modifier = Modifier.weight(1f), label = "भाषाएं", value = "हिन्दी, अंग्रेजी")
                }

                // Contact Unlock Section
                androidx.compose.animation.AnimatedContent(
                    targetState = isUnlocked,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) with
                        fadeOut(animationSpec = tween(500))
                    },
                    label = "unlockAnimation"
                ) { unlocked ->
                    if (!unlocked) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = PremiumNavyLight,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, PremiumGold.copy(0.2f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val infiniteTransition = rememberInfiniteTransition()
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.15f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "lockScale"
                                )
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PremiumGold,
                                    modifier = Modifier.size(40.dp).scale(scale)
                                )
                                Text(
                                    "Contact Locked",
                                    color = PremiumGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CinzelFamily
                                )
                                
                                // Masked Phone Number Display
                                Surface(
                                    color = Color.Black.copy(0.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color.White.copy(0.05f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = com.sangeetsetu.app.util.maskPhoneNumber(currentArtist.phone),
                                            color = PremiumWhite.copy(0.5f),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Icons.Default.Lock, null, tint = PremiumGold.copy(0.5f), modifier = Modifier.size(16.dp))
                                    }
                                }
                                
                                Text(
                                    "सीधे कॉल या WhatsApp करने के लिए ₹11 देकर Contact Unlock करें।",
                                    color = PremiumWhite.copy(0.7f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    fontFamily = NotoSansDevanagariFamily
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                PremiumButton(
                                    text = "Unlock Mobile Number – ₹11",
                                    onClick = { showUnlockSheet = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SuccessColor.copy(0.1f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, SuccessColor.copy(0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = SuccessColor,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.Verified, null, tint = PremiumNavy, modifier = Modifier.padding(10.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Contact Unlocked", color = SuccessColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    Text(currentArtist.phone, color = PremiumWhite, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            val cleanPhone = currentArtist.phone.replace("+", "").replace(" ", "")
                                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:$cleanPhone"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.background(SuccessColor.copy(0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Call, null, tint = SuccessColor)
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val cleanPhone = currentArtist.phone.replace("+", "").replace(" ", "").removePrefix("91")
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/91$cleanPhone"))
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.background(Color(0xFF25D366).copy(0.1f), CircleShape)
                                    ) {
                                        Icon(androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_send), null, tint = Color(0xFF25D366))
                                    }
                                }
                            }
                        }
                    }
                }


                // Availability Calendar
                DetailSection("उपलब्धता") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(0.05f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, null, tint = PremiumGold)
                                Spacer(Modifier.width(12.dp))
                                Text("आगामी उपलब्धता", color = PremiumWhite, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(16.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(10) { i ->
                                    val isBooked = i % 3 == 0
                                    Surface(
                                        color = if (isBooked) Color.Red.copy(0.1f) else SuccessColor.copy(0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.width(50.dp),
                                        border = BorderStroke(1.dp, if (isBooked) Color.Red.copy(0.3f) else SuccessColor.copy(0.3f))
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally, 
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        ) {
                                            Text("${15 + i}", color = PremiumWhite, fontWeight = FontWeight.Bold)
                                            Text("Oct", color = SecondaryText, fontSize = 10.sp)
                                            Spacer(Modifier.height(4.dp))
                                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(if (isBooked) Color.Red else SuccessColor))
                                        }
                                    }
                                }
                            }
                            Text(
                                "लाल: बुक, हरा: उपलब्ध", 
                                color = SecondaryText, 
                                fontSize = 10.sp, 
                                modifier = Modifier.padding(top = 12.dp),
                                fontFamily = NotoSansDevanagariFamily
                            )
                        }
                    }
                }

                // Gallery
                DetailSection("गैलरी") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(listOf(0,1,2,3,4)) { i ->
                            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-${1500000000000 + i}?q=80&w=200",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Audio Samples
                DetailSection("ऑडियो सैंपल") {
                    repeat(2) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            color = Color.White.copy(0.05f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = PremiumGold)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("सूफी और भक्ति संगीत - नमूना ${it+1}", color = Color.White, fontSize = 14.sp)
                                    Text("04:20", color = SecondaryText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Video Performance
                DetailSection("वीडियो परफॉरमेंस") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .clickable { /* Play Video */ }
                    ) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1514525253344-f8500071367c?q=80&w=600",
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.6f
                        )
                        Icon(
                            Icons.Default.PlayCircle,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp).align(Alignment.Center)
                        )
                        Surface(
                            color = Color.Red,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                        ) {
                            Text("NEW", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }

                // Services & Packages
                DetailSection("सेवाएं और पैकेज") {
                    val packages = listOf("शादी", "जन्मदिन", "कॉर्पोरेट", "भजन संध्या")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        packages.forEach { pkg ->
                            Surface(
                                color = Color.White.copy(0.1f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Text(pkg, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Reviews
                DetailSection("समीक्षाएं") {
                    repeat(2) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            color = CardBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.05f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Gray))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text("राहुल शर्मा", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Row { repeat(5) { Icon(Icons.Default.Star, null, tint = PremiumGold, modifier = Modifier.size(10.dp)) } }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("अद्भुत प्रस्तुति! हमारे कार्यक्रम में जान फूंक दी।", color = Color.White.copy(0.7f), fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Report
                TextButton(onClick = { /* Report Action */ }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("रिपोर्ट कलाकार", color = Color.Red.copy(0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, color = PremiumWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
        content()
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, label: String, value: String) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(0.05f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = SecondaryText, fontSize = 12.sp)
            Text(value, color = PremiumGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = SecondaryText, fontSize = 12.sp)
        Text(value, color = PremiumGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
