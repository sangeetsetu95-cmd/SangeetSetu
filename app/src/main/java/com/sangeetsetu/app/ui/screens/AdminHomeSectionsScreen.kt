package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewQuilt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sangeetsetu.app.model.HomeSection
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.CardBorder
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumGray
import com.sangeetsetu.app.ui.theme.PremiumNavy
import com.sangeetsetu.app.ui.theme.PremiumNavyDark
import com.sangeetsetu.app.ui.theme.PremiumNavyLight
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeSectionsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val sections by viewModel.homeSections.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf<HomeSection?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchHomeSections() }

    if (showAddDialog || selectedSection != null) {
        HomeSectionEditDialog(
            section = selectedSection,
            onDismiss = { showAddDialog = false; selectedSection = null },
            onSave = { section ->
                viewModel.saveHomeSection(section) {
                    Toast.makeText(context, "Layout Updated", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                    selectedSection = null
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Page Layout", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Rounded.Add, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.AdminHomeSections.route, onNavigate = onNavigate)
        },
        containerColor = PremiumNavy
    ) { padding ->
        if (isLoading && sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Rounded.ViewQuilt, null, tint = PremiumGray.copy(0.2f), modifier = Modifier.size(80.dp))
                    Text("No home sections defined", color = PremiumGray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(sections) { index, section ->
                    HomeSectionAdminCard(
                        section = section,
                        index = index,
                        totalCount = sections.size,
                        onEdit = { selectedSection = section },
                        onDelete = {
                            viewModel.deleteHomeSection(section.id) {
                                Toast.makeText(context, "Section Removed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onMove = { from, to -> viewModel.reorderHomeSections(from, to) }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeSectionAdminCard(
    section: HomeSection,
    index: Int,
    totalCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMove: (Int, Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PremiumNavyLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { if (index > 0) onMove(index, index - 1) },
                    enabled = index > 0,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.KeyboardArrowUp, null, tint = if (index > 0) PremiumGold else PremiumGray.copy(0.3f))
                }
                
                Icon(Icons.Rounded.DragHandle, null, tint = PremiumGray.copy(0.5f), modifier = Modifier.size(20.dp))
                
                IconButton(
                    onClick = { if (index < totalCount - 1) onMove(index, index + 1) },
                    enabled = index < totalCount - 1,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Rounded.KeyboardArrowDown, null, tint = if (index < totalCount - 1) PremiumGold else PremiumGray.copy(0.3f))
                }
            }
            
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f).clickable { onEdit() }) {
                Text(section.title, color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${section.contentType} • ${section.type}", color = PremiumWhite, fontSize = 12.sp)
                Text("Order: ${section.displayOrder}", color = PremiumGray, fontSize = 11.sp)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.7f))
            }
        }
    }
}

@Composable
fun HomeSectionEditDialog(section: HomeSection?, onDismiss: () -> Unit, onSave: (HomeSection) -> Unit) {
    var title by remember { mutableStateOf(section?.title ?: "") }
    var subtitle by remember { mutableStateOf(section?.subtitle ?: "") }
    var contentType by remember { mutableStateOf(section?.contentType ?: "Artists") }
    var type by remember { mutableStateOf(section?.type ?: "HorizontalList") }
    var contentFilter by remember { mutableStateOf(section?.contentFilter ?: "All") }
    var imageUrl by remember { mutableStateOf(section?.imageUrl ?: "") }
    var order by remember { mutableStateOf(section?.displayOrder?.toString() ?: "0") }
    var isVisible by remember { mutableStateOf(section?.isVisible ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumNavyDark,
        title = { Text(if (section == null) "New Section" else "Edit Section", color = PremiumGold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Section Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Subtitle") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (HorizontalList, Grid, Banner, Promo, OfferZone, Referral)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contentType, onValueChange = { contentType = it }, label = { Text("Content Type (Artists, Categories, Events, Bhajans)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contentFilter, onValueChange = { contentFilter = it }, label = { Text("Content Filter (All, Verified, Trending, or Category Name)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Promo Image URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = order, onValueChange = { order = it }, label = { Text("Display Order") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isVisible, onCheckedChange = { isVisible = it })
                    Text("Is Visible", color = PremiumWhite)
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(section?.copy(
                    title = title, 
                    subtitle = subtitle,
                    type = type,
                    contentType = contentType, 
                    contentFilter = contentFilter,
                    imageUrl = imageUrl,
                    displayOrder = order.toIntOrNull() ?: 0,
                    isVisible = isVisible
                ) ?: HomeSection(
                    title = title, 
                    subtitle = subtitle,
                    type = type,
                    contentType = contentType, 
                    contentFilter = contentFilter,
                    imageUrl = imageUrl,
                    displayOrder = order.toIntOrNull() ?: 0,
                    isVisible = isVisible
                ))
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
