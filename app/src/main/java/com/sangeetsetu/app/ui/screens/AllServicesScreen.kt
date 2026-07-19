package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.components.PremiumHeader
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.UserViewModel

@Composable
fun AllServicesScreen(
    onCategoryClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val filteredCategories = remember(searchQuery, categories) {
        categories.filter {
            it.isActive && it.name.contains(searchQuery, ignoreCase = true) 
        }
    }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            PremiumHeader(
                title = "All Services",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            PremiumBackgroundDecoration()
            
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search services...", color = SecondaryText) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PremiumWhite,
                        unfocusedTextColor = PremiumWhite,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground,
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.05f)
                    )
                )

                if (isLoading && categories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(filteredCategories) { category ->
                            ServiceCategoryCard(category.id, category.name, category.displayImage, onCategoryClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCategoryCard(id: String, name: String, imageUrl: String, onClick: (String) -> Unit) {
    Surface(
        onClick = { 
            android.util.Log.d("AllServices", "Category clicked: $name (ID: $id)")
            onClick(id) 
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = PremiumGold.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            Text(
                text = name,
                color = PremiumWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}
