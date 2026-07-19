package com.sangeetsetu.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.components.PremiumArtistCard
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryArtistsScreen(
    categoryId: String,
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUserData.collectAsState()
    var visible by remember { mutableStateOf(false) }

    val categoryObj = remember(categories, categoryId) {
        categories.find { it.id == categoryId }
    }
    val categoryName = categoryObj?.name ?: categoryId

    LaunchedEffect(categoryId) {
        android.util.Log.d("CategoryArtistsScreen", "Screen launched with Category ID: $categoryId")
        viewModel.fetchArtists(categoryId)
        visible = true
    }

    val filteredArtists = remember(artists, categoryId) {
        val cleanId = categoryId.trim()
        val result = if (cleanId.equals("All", ignoreCase = true) || cleanId.equals("trending", ignoreCase = true)) artists 
        else artists.filter { 
            it.categoryId.trim().equals(cleanId, ignoreCase = true) || it.category.trim().equals(cleanId, ignoreCase = true)
        }
        android.util.Log.d("CategoryArtistsScreen", "Filtering artists for '$cleanId'. Found: ${result.size} artists. Total artists in state: ${artists.size}")
        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = categoryName,
                            color = Color.White,
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "बेहतरीन कलाकार",
                            color = PremiumGold,
                            fontSize = 12.sp,
                            fontFamily = NotoSansDevanagariFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = PremiumGold
                )
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AppBackground, Color(0xFF0F172A).copy(alpha = 0.5f))
                        )
                    )
            )

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800))
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredArtists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Should be something like "PersonOff" but keeping it simple
                                contentDescription = null,
                                tint = SecondaryText,
                                modifier = Modifier.size(64.dp).alpha(0.3f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "कोई कलाकार नहीं मिला",
                                color = SecondaryText,
                                fontFamily = NotoSansDevanagariFamily
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredArtists) { artist ->
                            PremiumArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.uid) },
                                onFavoriteToggle = { viewModel.toggleFavorite(artist.uid) },
                                isFavorite = currentUser?.favorites?.contains(artist.uid) == true
                            )
                        }
                    }
                }
            }
        }
    }
}
