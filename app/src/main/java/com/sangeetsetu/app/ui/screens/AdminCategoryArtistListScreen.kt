package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryArtistListScreen(
    categoryId: String,
    onBack: () -> Unit,
    onEditArtist: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val categoryObj = remember(categories, categoryId) {
        categories.find { it.id == categoryId }
    }
    
    val categoryName = categoryObj?.name ?: categoryId

    val categoryArtists = remember(artists, categoryId, searchQuery, selectedFilter) {
        val cleanId = categoryId.trim()
        artists.filter { it.categoryId.trim().equals(cleanId, ignoreCase = true) }
            .filter { it.name.contains(searchQuery, ignoreCase = true) || it.uid.contains(searchQuery, ignoreCase = true) || it.artistId.contains(searchQuery, ignoreCase = true) }
            .filter {
                when (selectedFilter) {
                    "Verified" -> it.verificationStatus.equals("VERIFIED", ignoreCase = true)
                    "VIP" -> it.isPremium
                    "Active" -> it.accountStatus.equals("ACTIVE", ignoreCase = true)
                    "Inactive" -> it.accountStatus.equals("SUSPENDED", ignoreCase = true)
                    else -> true
                }
            }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(AppBackground)) {
                TopAppBar(
                    title = {
                        Column {
                            Text(categoryName, color = PremiumWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("${categoryArtists.size} Artists", color = PremiumGold, fontSize = 12.sp)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or ID...", color = Color.Gray, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = BorderColor
                    )
                )

                // Filters
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf("All", "Verified", "VIP", "Active", "Inactive")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = Color.Gray,
                                selectedLabelColor = Color.Black,
                                selectedContainerColor = PremiumGold
                            )
                        )
                    }
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        if (categoryArtists.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No artists found in this category", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(categoryArtists) { artist ->
                    AdminArtistCategoryCard(
                        artist = artist,
                        onEdit = { onEditArtist(artist.uid) },
                        onSuspend = { 
                            val newStatus = if (artist.accountStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
                            viewModel.updateAccountStatus(artist.uid, newStatus) {} 
                        },
                        onDelete = { viewModel.deleteUser(artist.uid, {}) },
                        onToggleVIP = { viewModel.updateUserProfile(artist.copy(isPremium = !artist.isPremium), {}) },
                        onToggleVerified = { 
                            val newStatus = if (artist.verificationStatus == "VERIFIED") "PENDING" else "VERIFIED"
                            viewModel.updateVerificationStatus(artist.uid, newStatus) {}
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminArtistCategoryCard(
    artist: User,
    onEdit: () -> Unit,
    onSuspend: () -> Unit,
    onDelete: () -> Unit,
    onToggleVIP: () -> Unit,
    onToggleVerified: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, if (artist.isPremium) PremiumGold.copy(0.3f) else BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = artist.photoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).clip(CircleShape).background(AppBackground),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(artist.name, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (artist.verificationStatus == "VERIFIED") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                        }
                        if (artist.isPremium) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Star, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("ID: ${artist.uid.take(8)}", color = SecondaryText, fontSize = 11.sp)
                    Text("📞 ${artist.phone}", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("${artist.district}, ${artist.state}", color = SecondaryText, fontSize = 12.sp)
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(status = artist.accountStatus)
                    if (artist.isPremium) {
                        Text("VIP", color = PremiumGold, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Icons
                AdminActionIcon(Icons.Rounded.Edit, "Edit", PremiumGold) { onEdit() }
                AdminActionIcon(
                    if (artist.verificationStatus == "VERIFIED") Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    "Featured",
                    if (artist.verificationStatus == "VERIFIED") PremiumGold else Color.Gray
                ) { onToggleVerified() }
                
                AdminActionIcon(
                    Icons.Rounded.WorkspacePremium,
                    "VIP",
                    if (artist.isPremium) PremiumGold else Color.Gray
                ) { onToggleVIP() }

                AdminActionIcon(
                    if (artist.accountStatus == "ACTIVE") Icons.Rounded.Block else Icons.Rounded.CheckCircle,
                    if (artist.accountStatus == "ACTIVE") "Suspend" else "Activate",
                    if (artist.accountStatus == "ACTIVE") Color.Red else Color.Green
                ) { onSuspend() }

                AdminActionIcon(Icons.Rounded.Delete, "Delete", Color.Red.copy(0.7f)) { onDelete() }
            }
        }
    }
}

@Composable
fun AdminActionIcon(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when(status.uppercase()) {
        "ACTIVE" -> Color.Green
        "SUSPENDED" -> Color.Red
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            status.uppercase(),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
