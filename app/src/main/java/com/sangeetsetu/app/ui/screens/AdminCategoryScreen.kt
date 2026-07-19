package com.sangeetsetu.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.ui.theme.AdminBackground
import com.sangeetsetu.app.ui.theme.AdminCardBackground
import com.sangeetsetu.app.ui.theme.AdminSecondaryText
import com.sangeetsetu.app.ui.theme.ErrorColor
import com.sangeetsetu.app.ui.theme.GoldenGradient
import com.sangeetsetu.app.ui.theme.PremiumAdminTheme
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.viewmodel.AdminViewModel
import com.sangeetsetu.app.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryScreen(
    onBack: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val categories by userViewModel.categories.collectAsState()
    val artists by adminViewModel.artists.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    
    val categoryCounts = remember(artists) {
        artists.groupBy { it.categoryId }.mapValues { it.value.size }
    }

    var newCategoryName by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    PremiumAdminTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Manage Categories", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = { 
                        IconButton(onClick = onBack) { 
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold) 
                        } 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            containerColor = AdminBackground
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                
                // Add/Edit Form
                Surface(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = AdminCardBackground,
                    border = BorderStroke(1.dp, Color.White.copy(0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editingCategory != null) "Update Category" else "Add New Category",
                                color = PremiumGold,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                if (categories.isEmpty() && editingCategory == null) {
                                    TextButton(onClick = {
                                        adminViewModel.seedDefaultCategories(
                                            onSuccess = { scope.launch { snackbarHostState.showSnackbar("22 Categories Seeded!") } },
                                            onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                        )
                                    }) {
                                        Text("Seed 22 Categories", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (editingCategory != null) {
                                    IconButton(onClick = { 
                                        editingCategory = null
                                        newCategoryName = ""
                                        selectedImageUri = null
                                    }) {
                                        Icon(Icons.Default.Close, null, tint = ErrorColor, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(18.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(85.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AdminBackground)
                                    .clickable { if (!isLoading) launcher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (editingCategory != null && editingCategory!!.iconUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = editingCategory!!.iconUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = PremiumGold.copy(0.6f), modifier = Modifier.size(36.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(18.dp))
                            
                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                label = { Text("Name (e.g. 🥁 ढोलक)", color = AdminSecondaryText) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = !isLoading,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PremiumWhite,
                                    unfocusedTextColor = PremiumWhite,
                                    focusedBorderColor = PremiumGold,
                                    unfocusedBorderColor = Color.White.copy(0.1f),
                                    cursorColor = PremiumGold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Premium Gold Gradient Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isLoading) PremiumGold.copy(0.5f) else Color.Transparent)
                                .let { 
                                    if (!isLoading) it.background(GoldenGradient) else it 
                                }
                                .clickable(enabled = !isLoading) {
                                    if (newCategoryName.trim().isEmpty()) {
                                        scope.launch { snackbarHostState.showSnackbar("Category name is required") }
                                        return@clickable
                                    }
                                    
                                    if (editingCategory != null) {
                                        adminViewModel.updateCategory(
                                            category = editingCategory!!.copy(name = newCategoryName.trim()),
                                            newImageUri = selectedImageUri,
                                            onSuccess = {
                                                editingCategory = null
                                                newCategoryName = ""
                                                selectedImageUri = null
                                                scope.launch { snackbarHostState.showSnackbar("Category Updated Successfully!") }
                                            },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    } else {
                                        adminViewModel.addCategory(
                                            name = newCategoryName,
                                            imageUri = selectedImageUri,
                                            onSuccess = {
                                                newCategoryName = ""
                                                selectedImageUri = null
                                                scope.launch { snackbarHostState.showSnackbar("Category Created!") }
                                            },
                                            onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.Black, strokeWidth = 2.5.dp)
                            } else {
                                Text(
                                    if (editingCategory != null) "UPDATE CATEGORY" else "CREATE CATEGORY",
                                    color = Color.Black,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    "Active Categories (${categories.size})", 
                    color = PremiumWhite, 
                    fontSize = 19.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryAdminCard(
                            category = category,
                            count = categoryCounts[category.id] ?: 0,
                            isLoading = isLoading,
                            onClick = { onNavigateToCategory(category.id) },
                            onEdit = {
                                editingCategory = category
                                newCategoryName = category.name
                                selectedImageUri = null
                            },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            containerColor = AdminCardBackground,
            title = { Text("Delete Category?", color = ErrorColor, fontWeight = FontWeight.ExtraBold) },
            text = { Text("This will permanently remove '${categoryToDelete!!.name}' and its image.", color = PremiumWhite) },
            confirmButton = {
                TextButton(onClick = {
                    val cat = categoryToDelete!!
                    categoryToDelete = null
                    adminViewModel.deleteCategory(cat, 
                        onSuccess = { scope.launch { snackbarHostState.showSnackbar("Category Removed") } },
                        onError = { scope.launch { snackbarHostState.showSnackbar(it) } }
                    )
                }) { Text("DELETE", color = ErrorColor, fontWeight = FontWeight.ExtraBold) }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) { Text("CANCEL", color = AdminSecondaryText) }
            }
        )
    }
}

@Composable
fun CategoryAdminCard(
    category: Category,
    count: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = AdminCardBackground,
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp), 
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = category.displayImage,
                contentDescription = null,
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AdminBackground),
                contentScale = ContentScale.Crop,
                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Category)
            )
            
            Spacer(modifier = Modifier.width(18.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name, 
                    color = PremiumWhite, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 17.sp
                )
                Text(
                    text = "$count Artists Registered", 
                    color = PremiumGold, 
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            IconButton(
                onClick = onEdit, 
                enabled = !isLoading,
                modifier = Modifier.background(PremiumGold.copy(0.12f), CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDelete, 
                enabled = !isLoading,
                modifier = Modifier.background(ErrorColor.copy(0.12f), CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.Delete, null, tint = ErrorColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}
