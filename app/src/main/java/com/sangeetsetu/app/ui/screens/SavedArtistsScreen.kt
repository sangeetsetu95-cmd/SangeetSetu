package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.components.FloatingGlassBottomBar
import com.sangeetsetu.app.ui.components.PremiumArtistCard
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SangeetSetuDesign
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedArtistsScreen(
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val savedArtists by viewModel.favoriteArtists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUserData.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredArtists = remember(savedArtists, searchQuery) {
        if (searchQuery.isEmpty()) {
            savedArtists
        } else {
            savedArtists.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.city.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PremiumHeader(
                    title = "पसंदीदा कलाकार",
                    onBackClick = onBack,
                    actions = {
                        IconButton(onClick = { 
                            viewModel.fetchCurrentUser()
                        }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = PremiumGold)
                        }
                    }
                )
            },
            bottomBar = {
                FloatingGlassBottomBar(
                    currentRoute = "favorites",
                    onHome = onBack,
                    onCategories = { },
                    onBookings = { },
                    onChat = { },
                    onProfile = { }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("कलाकार, श्रेणी या शहर खोजें...", color = SecondaryText) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        cursorColor = PremiumGold,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedContainerColor = CardBackground.copy(alpha = 0.6f),
                        unfocusedContainerColor = CardBackground.copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )

                if (isLoading && savedArtists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredArtists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (searchQuery.isEmpty()) Icons.Default.Favorite else Icons.Default.Search, 
                                null, 
                                tint = PremiumGold.copy(alpha = 0.15f), 
                                modifier = Modifier.size(100.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isEmpty()) "अभी कोई कलाकार पसंद नहीं किया गया है" else "कोई कलाकार नहीं मिला", 
                                color = SecondaryText, 
                                fontSize = 16.sp,
                                fontFamily = NotoSansDevanagariFamily
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredArtists, key = { it.uid }) { artist ->
                            PremiumArtistCard(
                                artist = artist, 
                                onClick = { onArtistClick(artist.uid) },
                                onFavoriteToggle = { viewModel.toggleFavorite(artist.uid) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}
