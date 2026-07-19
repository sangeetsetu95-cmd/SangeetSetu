package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.Banner
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SangeetSetuDesign
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBannerManagerScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBanner by remember { mutableStateOf<Banner?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Banner Manager", style = MaterialTheme.typography.titleLarge, color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        selectedBanner = null
                        showEditDialog = true 
                    }) {
                        Icon(Icons.Rounded.Add, null, tint = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            AdminBottomNav(currentRoute = Screen.Banners.route, onNavigate = onNavigate)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground
    ) { padding ->
        if (isLoading && banners.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else if (banners.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Image, 
                        null, 
                        tint = SecondaryText.copy(0.2f), 
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        "No banners configured", 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = SecondaryText,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Button(
                        onClick = { showEditDialog = true }, 
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = SangeetSetuDesign.Shapes.medium,
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text("Add New Banner", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(SangeetSetuDesign.HorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(banners.sortedBy { it.order }, key = { it.id }) { banner ->
                    val index = banners.sortedBy { it.order }.indexOf(banner)
                    BannerAdminItem(
                        banner = banner,
                        onEdit = {
                            selectedBanner = banner
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteBanner(banner) {
                                Toast.makeText(context, "Banner Deleted", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onToggleActive = {
                            viewModel.saveBanner(banner.copy(isActive = !banner.isActive), null, 
                                onSuccess = {
                                    Toast.makeText(context, "Banner status updated", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onMoveUp = if (index > 0) {
                            {
                                viewModel.reorderBanners(index, index - 1,
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        } else null,
                        onMoveDown = if (index < banners.size - 1) {
                            {
                                viewModel.reorderBanners(index, index + 1,
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        BannerEditDialog(
            banner = selectedBanner,
            onDismiss = { 
                showEditDialog = false
                selectedBanner = null 
            },
            onSave = { banner, uri ->
                viewModel.saveBanner(banner, uri, 
                    onSuccess = {
                        Toast.makeText(context, "Banner synchronized successfully", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                        selectedBanner = null
                    },
                    onError = {
                        Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun BannerAdminItem(
    banner: Banner,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = SangeetSetuDesign.Shapes.medium,
        border = BorderStroke(1.dp, if (banner.isActive) PremiumGold.copy(0.3f) else BorderColor),
        onClick = onEdit,
        tonalElevation = 2.dp
    ) {
        Column {
            Box {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(SangeetSetuDesign.Shapes.medium.copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))),
                    contentScale = ContentScale.Crop
                )
                
                // Status Badge
                Surface(
                    color = if (banner.isActive) Color(0xFF2E7D32).copy(0.9f) else Color.Gray.copy(0.9f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
                ) {
                    Text(
                        if (banner.isActive) "Active" else "Inactive",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color.Black.copy(0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        banner.bannerType, 
                        color = PremiumGold, 
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        banner.title, 
                        style = MaterialTheme.typography.titleMedium,
                        color = PremiumWhite
                    )
                    if (banner.subtitle.isNotEmpty()) {
                        Text(
                            banner.subtitle, 
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryText, 
                            maxLines = 1
                        )
                    }
                    Text(
                        "Order: ${banner.order} • Action: ${banner.clickActionType}", 
                        style = MaterialTheme.typography.labelSmall,
                        color = PremiumGold.copy(0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onMoveUp != null) {
                        IconButton(onClick = onMoveUp) {
                            Icon(Icons.Rounded.ArrowUpward, null, tint = PremiumGold.copy(0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(onClick = onMoveDown) {
                            Icon(Icons.Rounded.ArrowDownward, null, tint = PremiumGold.copy(0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Switch(
                        checked = banner.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PremiumGold, 
                            checkedTrackColor = PremiumGold.copy(0.3f)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.7f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerEditDialog(
    banner: Banner?,
    onDismiss: () -> Unit,
    onSave: (Banner, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(banner?.title ?: "") }
    var subtitle by remember { mutableStateOf(banner?.subtitle ?: "") }
    var actionUrl by remember { mutableStateOf(banner?.actionUrl ?: "") }
    var displayOrder by remember { mutableStateOf(banner?.order?.toString() ?: "0") }
    var bannerType by remember { mutableStateOf(banner?.bannerType ?: "General") }
    var clickActionType by remember { mutableStateOf(banner?.clickActionType ?: "Category") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isActive by remember { mutableStateOf(banner?.isActive ?: true) }
    
    var startDate by remember { mutableStateOf(banner?.startDate ?: System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(banner?.endDate ?: (System.currentTimeMillis() + 2592000000L)) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    val bannerTypes = listOf("Featured", "Verified", "RisingStars", "MostBooked", "VIP", "Festival", "General")
    val clickActions = listOf("Category", "Artist", "Custom", "No Action")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedImageUri = it
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) { Text("OK", color = PremiumGold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK", color = PremiumGold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppBackground,
        modifier = Modifier.fillMaxWidth(0.95f),
        title = { 
            Text(
                if (banner == null) "Create New Banner" else "Edit Banner Details", 
                style = MaterialTheme.typography.headlineSmall,
                color = PremiumGold, 
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 📱 App Preview Logic
                Column {
                    Text(
                        "Live App Preview", 
                        style = MaterialTheme.typography.labelLarge,
                        color = PremiumWhite, 
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable { launcher.launch("image/*") },
                        shape = RoundedCornerShape(20.dp),
                        color = CardBackground,
                        border = BorderStroke(1.dp, PremiumGold.copy(0.2f))
                    ) {
                        Box {
                            if (selectedImageUri != null) {
                                AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else if (banner?.imageUrl?.isNotEmpty() == true) {
                                AsyncImage(model = banner.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Rounded.AddPhotoAlternate, null, tint = PremiumGold.copy(0.5f), modifier = Modifier.size(32.dp))
                                    Text("Add Image", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                                }
                            }
                            
                            // Mock UI Overlay
                            Box(modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Black.copy(0.8f), Color.Transparent))))
                            Column(modifier = Modifier.padding(20.dp).align(Alignment.CenterStart)) {
                                Text(
                                    title.ifEmpty { "Main Display Title" }, 
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PremiumGold, 
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    subtitle.ifEmpty { "Captivating subtitle here" }, 
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(0.9f)
                                )
                            }
                        }
                    }
                    Text(
                        "Tap image to change (Uploaded to Cloudinary)", 
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText, 
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                HorizontalDivider(color = BorderColor.copy(0.5f), thickness = 1.dp)

                // Basic Info
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it }, 
                    label = { Text("Main Title") },
                    placeholder = { Text("e.g. Festival Special") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SangeetSetuDesign.Shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White, 
                        focusedBorderColor = PremiumGold
                    )
                )

                OutlinedTextField(
                    value = subtitle, 
                    onValueChange = { subtitle = it }, 
                    label = { Text("Subtitle") },
                    placeholder = { Text("e.g. 50% Off on Bookings") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = SangeetSetuDesign.Shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White, 
                        focusedBorderColor = PremiumGold
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Type Selection
                    var typeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = bannerType, 
                            onValueChange = {}, 
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.menuAnchor(),
                            shape = SangeetSetuDesign.Shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            bannerTypes.forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = { bannerType = type; typeExpanded = false })
                            }
                        }
                    }

                    // Order
                    OutlinedTextField(
                        value = displayOrder, 
                        onValueChange = { displayOrder = it }, 
                        label = { Text("Priority") },
                        modifier = Modifier.weight(0.5f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = SangeetSetuDesign.Shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }

                // Click Action
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var actionExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = actionExpanded,
                        onExpandedChange = { actionExpanded = !actionExpanded },
                        modifier = if (clickActionType == "No Action") Modifier.fillMaxWidth() else Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = clickActionType, 
                            onValueChange = {}, 
                            readOnly = true,
                            label = { Text("Action") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier.menuAnchor(),
                            shape = SangeetSetuDesign.Shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        ExposedDropdownMenu(expanded = actionExpanded, onDismissRequest = { actionExpanded = false }) {
                            clickActions.forEach { action ->
                                DropdownMenuItem(text = { Text(action) }, onClick = { clickActionType = action; actionExpanded = false })
                            }
                        }
                    }

                    if (clickActionType != "No Action") {
                        OutlinedTextField(
                            value = actionUrl, 
                            onValueChange = { actionUrl = it }, 
                            label = { Text("Value (ID/Cat)") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("e.g. Bhajan", fontSize = 12.sp) },
                            shape = SangeetSetuDesign.Shapes.small,
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(startDate)), 
                        onValueChange = { }, 
                        readOnly = true,
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                        enabled = false,
                        shape = SangeetSetuDesign.Shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = BorderColor,
                            disabledLabelColor = SecondaryText
                        )
                    )
                    OutlinedTextField(
                        value = dateFormat.format(Date(endDate)), 
                        onValueChange = { }, 
                        readOnly = true,
                        label = { Text("End Date") },
                        modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                        enabled = false,
                        shape = SangeetSetuDesign.Shapes.small,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White, 
                            disabledBorderColor = BorderColor,
                            disabledLabelColor = SecondaryText
                        )
                    )
                }

                Surface(
                    color = PremiumGold.copy(0.05f),
                    shape = SangeetSetuDesign.Shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            "Live Status", 
                            style = MaterialTheme.typography.bodyMedium,
                            color = PremiumWhite, 
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = isActive, 
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PremiumGold, 
                                checkedTrackColor = PremiumGold.copy(0.3f)
                            ),
                            modifier = Modifier.scale(0.85f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotEmpty()) {
                        if (clickActionType != "No Action" && actionUrl.isBlank()) {
                            return@Button
                        }
                        
                        val finalBanner = (banner ?: Banner()).copy(
                            title = title,
                            subtitle = subtitle,
                            bannerType = bannerType,
                            clickActionType = clickActionType,
                            actionUrl = if (clickActionType == "No Action") "" else actionUrl,
                            order = displayOrder.toIntOrNull() ?: 0,
                            isActive = isActive,
                            startDate = startDate,
                            endDate = endDate
                        )
                        onSave(finalBanner, selectedImageUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = SangeetSetuDesign.Shapes.medium
            ) { 
                Text("Sync with App", color = Color.Black, fontWeight = FontWeight.Bold) 
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text("Cancel", color = SecondaryText, style = MaterialTheme.typography.labelLarge) 
            }
        }
    )
}
