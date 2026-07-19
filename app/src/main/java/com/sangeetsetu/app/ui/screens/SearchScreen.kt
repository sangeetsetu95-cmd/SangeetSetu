package com.sangeetsetu.app.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.LocationData
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.components.SearchableListDialog
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.SearchUIState
import com.sangeetsetu.app.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onArtistClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit = {},
    initialQuery: String = "",
    searchViewModel: SearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val query by searchViewModel.query.collectAsState()
    val selectedState by searchViewModel.selectedState.collectAsState()
    val selectedDistrict by searchViewModel.selectedDistrict.collectAsState()
    val selectedCategory by searchViewModel.selectedCategory.collectAsState()
    val allCategories by searchViewModel.allCategories.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val recentSearches by searchViewModel.recentSearches.collectAsState()
    val popularArtists by searchViewModel.popularArtists.collectAsState()
    val popularCategories by searchViewModel.popularCategories.collectAsState()

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrEmpty()) {
                searchViewModel.onQueryChange(spokenText)
            }
        }
    }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            searchViewModel.onQueryChange(initialQuery)
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            Column(modifier = Modifier.background(AppBackground)) {
                PremiumHeader(
                    title = strings.searchOptions,
                    onBackClick = onBack
                )
                
                OutlinedTextField(
                    value = query,
                    onValueChange = { searchViewModel.onQueryChange(it) },
                    placeholder = { Text(strings.searchPlaceholder, color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold) },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { searchViewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, null, tint = SecondaryText)
                                }
                            }
                            IconButton(onClick = { 
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "कलाकार, सेवा का नाम बोलें...")
                                }
                                try {
                                    voiceLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Voice Search not available", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.Mic, null, tint = PremiumGold)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.05f)
                    )
                )

                // Search Filters
                SearchFilters(
                    selectedState = selectedState,
                    selectedDistrict = selectedDistrict,
                    selectedCategory = selectedCategory,
                    categories = allCategories,
                    onStateChange = { searchViewModel.onStateChange(it) },
                    onDistrictChange = { searchViewModel.onDistrictChange(it) },
                    onCategoryChange = { searchViewModel.onCategoryChange(it) },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PremiumBackgroundDecoration()
            
            AnimatedContent(
                targetState = searchResults,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "search_transition"
            ) { state ->
                when (state) {
                    is SearchUIState.Empty -> {
                        EmptySearchContent(
                            recentSearches = recentSearches,
                            popularCategories = popularCategories,
                            onSearchSelect = { searchViewModel.onQueryChange(it) },
                            onCategoryClick = onCategoryClick
                        )
                    }
                    is SearchUIState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PremiumGold)
                        }
                    }
                    is SearchUIState.Success -> {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp)) {
                            items(state.artists) { artist ->
                                SearchArtistItem(artist, onArtistClick)
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun SearchFilters(
    selectedState: String,
    selectedDistrict: String,
    selectedCategory: String,
    categories: List<Category>,
    onStateChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    var showStateDialog by remember { mutableStateOf(false) }
    var showDistrictDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // State
            FilterItem(
                title = strings.selectState,
                value = selectedState.ifEmpty { strings.selectState },
                icon = Icons.Default.LocationOn,
                onClick = { showStateDialog = true },
                modifier = Modifier.weight(1f)
            )

            // District
            FilterItem(
                title = strings.selectDistrict,
                value = selectedDistrict.ifEmpty { strings.selectDistrict },
                icon = Icons.Default.Business,
                onClick = { if (selectedState.isNotEmpty()) showDistrictDialog = true },
                modifier = Modifier.weight(1f),
                enabled = selectedState.isNotEmpty()
            )

            // Category
            FilterItem(
                title = strings.categories,
                value = categories.find { it.id == selectedCategory }?.name ?: strings.categories,
                icon = Icons.Default.Sell,
                onClick = { showCategoryDialog = true },
                modifier = Modifier.weight(1.3f)
            )
        }
    }

    if (showStateDialog) {
        SearchableListDialog(
            title = strings.selectState,
            list = LocationData.states,
            onDismiss = { showStateDialog = false },
            onSelect = {
                onStateChange(it)
                showStateDialog = false
            }
        )
    }

    if (showDistrictDialog) {
        SearchableListDialog(
            title = strings.selectDistrict,
            list = LocationData.stateDistricts[selectedState] ?: emptyList(),
            onDismiss = { showDistrictDialog = false },
            onSelect = {
                onDistrictChange(it)
                showDistrictDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        SearchableListDialog(
            title = strings.categories,
            list = categories.map { it.name },
            onDismiss = { showCategoryDialog = false },
            onSelect = { name ->
                val id = categories.find { it.name == name }?.id ?: name
                onCategoryChange(id)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun FilterItem(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = if (enabled) PremiumWhite.copy(alpha = 0.8f) else SecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            maxLines = 1
        )
        Surface(
            onClick = onClick,
            enabled = enabled,
            color = AppBackground.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(icon, null, tint = if (enabled) PremiumGold else SecondaryText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = value,
                    color = if (enabled) PremiumWhite else SecondaryText,
                    fontSize = 11.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.KeyboardArrowDown, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun EmptySearchContent(
    recentSearches: List<String>,
    popularCategories: List<Category>,
    onSearchSelect: (String) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        if (recentSearches.isNotEmpty()) {
            Text("Recent Searches", color = PremiumWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(recentSearches) { search ->
                    Surface(
                        onClick = { onSearchSelect(search) },
                        color = CardBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Text(search, color = SecondaryText, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Text("Popular Categories", color = PremiumWhite, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(popularCategories) { category ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onCategoryClick(if (category.id.isNotEmpty()) category.id else category.name) }
                ) {
                    Surface(
                        modifier = Modifier.size(70.dp),
                        shape = CircleShape,
                        color = CardBackground,
                        border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.3f))
                    ) {
                        AsyncImage(
                            model = category.displayImage,
                            contentDescription = category.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(category.name, color = PremiumWhite, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SearchArtistItem(artist: User, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(artist.uid) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        color = CardBackground,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = artist.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(56.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(artist.name, color = PremiumWhite, fontWeight = FontWeight.Bold)
                    if (artist.isVip) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Stars, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                    }
                }
                Text(artist.category, color = PremiumGold, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = SecondaryText)
        }
    }
}
