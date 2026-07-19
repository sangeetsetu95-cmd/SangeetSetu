package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.components.PremiumArtistCard
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.UserViewModel

@Composable
fun ArtistListScreen(
    categoryName: String, 
    onBack: () -> Unit, 
    onArtistClick: (String) -> Unit,
    onHome: () -> Unit,
    onBookings: () -> Unit,
    onChat: () -> Unit,
    onProfile: () -> Unit,
    onSearchClick: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filteredArtists = if (categoryName == "All") {
        artists
    } else {
        artists.filter { it.categoryId == categoryName }
    }.filter {
        when(selectedFilter) {
            "Verified" -> it.verificationStatus == "VERIFIED"
            "Top Rated" -> it.rating >= 4.5
            else -> true
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            PremiumHeader(
                title = if (categoryName == "All") "Top Artists" else categoryName,
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PremiumBackgroundDecoration()
            
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val filters = listOf("All", "Verified", "Top Rated", "Trending")
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = PremiumGold,
                                labelColor = SecondaryText,
                                selectedLabelColor = Color.Black
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.White.copy(alpha = 0.1f),
                                selectedBorderColor = PremiumGold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredArtists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No artists found", color = SecondaryText)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(filteredArtists) { artist ->
                            PremiumArtistCard(
                                artist = artist,
                                onClick = { onArtistClick(artist.uid) },
                                onFavoriteToggle = { viewModel.toggleFavorite(artist.uid) },
                                isFavorite = false // Should come from user data
                            )
                        }
                    }
                }
            }
        }
    }
}
