package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.navigation.Screen
import com.sangeetsetu.app.ui.components.AdminMessageDialog
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.theme.AdminBackground
import com.sangeetsetu.app.ui.theme.AdminCardBackground
import com.sangeetsetu.app.ui.theme.AdminSecondaryText
import com.sangeetsetu.app.ui.theme.ErrorColor
import com.sangeetsetu.app.ui.theme.GoldenGradient
import com.sangeetsetu.app.ui.theme.PremiumAdminTheme
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SuccessColor
import com.sangeetsetu.app.ui.theme.WarningColor
import com.sangeetsetu.app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistListScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onArtistClick: (String) -> Unit = {},
    viewModel: AdminViewModel = hiltViewModel()
) {
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedArtistForEdit by remember { mutableStateOf<User?>(null) }
    var artistToDelete by remember { mutableStateOf<User?>(null) }
    var showAddArtistDialog by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var artistForQR by remember { mutableStateOf<User?>(null) }
    var artistToChat by remember { mutableStateOf<User?>(null) }
    
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }
    
    // Detailed Filters
    var filterVerified by remember { mutableStateOf(false) }
    var filterVIP by remember { mutableStateOf(false) }

    val tabs = listOf("All", "Active", "Inactive")

    val filteredArtists = remember(searchQuery, selectedTab, artists, filterVerified, filterVIP) {
        artists.filter { artist ->
            val matchesSearch = if (searchQuery.isEmpty()) true else {
                val q = searchQuery.lowercase()
                artist.artistId.lowercase().contains(q) ||
                artist.name.lowercase().contains(q) ||
                artist.phone.contains(q) ||
                artist.city.lowercase().contains(q) ||
                artist.district.lowercase().contains(q) ||
                artist.state.lowercase().contains(q) ||
                artist.category.lowercase().contains(q)
            }
            
            val matchesTab = when (selectedTab) {
                "Active" -> artist.accountStatus == "ACTIVE"
                "Inactive" -> artist.accountStatus == "SUSPENDED"
                else -> true
            }
            
            val matchesVerified = !filterVerified || artist.verificationStatus == "VERIFIED"
            val matchesVIP = !filterVIP || artist.isVip
            
            matchesSearch && matchesTab && matchesVerified && matchesVIP
        }
    }

    PremiumAdminTheme {
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = AdminCardBackground
            ) {
                Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                    Text("Advanced Filters", color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    FilterOption("Verified Artists", filterVerified) { filterVerified = it }
                    FilterOption("VIP / Premium Artists", filterVIP) { filterVIP = it }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Filters", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (selectedArtistForEdit != null) {
            EditArtistDialog(
                artist = selectedArtistForEdit!!,
                onDismiss = { selectedArtistForEdit = null },
                onSave = { updatedArtist ->
                    viewModel.updateUserProfile(updatedArtist) {
                        scope.launch { snackbarHostState.showSnackbar("Artist profile updated") }
                        selectedArtistForEdit = null
                    }
                }
            )
        }

        if (showAddArtistDialog) {
            AddArtistDialog(
                onDismiss = { showAddArtistDialog = false },
                onAdd = { newArtist ->
                    viewModel.addUser(newArtist) {
                        scope.launch { snackbarHostState.showSnackbar("Artist added successfully") }
                        showAddArtistDialog = false
                    }
                }
            )
        }

        if (artistToDelete != null) {
            AlertDialog(
                onDismissRequest = { artistToDelete = null },
                containerColor = AdminCardBackground,
                title = { Text("Delete Artist?", color = ErrorColor, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to delete ${artistToDelete?.name}? This cannot be undone.", color = PremiumWhite) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteUser(artistToDelete!!.uid) {
                            scope.launch { snackbarHostState.showSnackbar("Artist Deleted") }
                            artistToDelete = null
                        }
                    }) {
                        Text("DELETE", color = ErrorColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { artistToDelete = null }) {
                        Text("CANCEL", color = AdminSecondaryText)
                    }
                }
            )
        }

        if (artistForQR != null) {
            ArtistQRDialog(artist = artistForQR!!) { artistForQR = null }
        }

        if (artistToChat != null) {
            AdminMessageDialog(
                recipientName = artistToChat!!.name,
                onDismiss = { artistToChat = null },
                onSend = { message: String ->
                    viewModel.sendAdminMessage(artistToChat!!.uid, message) {
                        Toast.makeText(context, "Message sent to ${artistToChat!!.name}", Toast.LENGTH_SHORT).show()
                        artistToChat = null
                    }
                }
            )
        }

        Scaffold(
            topBar = {
                if (isSearchActive) {
                    TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search by ID, Name, Phone, Location...", color = AdminSecondaryText, fontSize = 14.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = PremiumWhite,
                                    unfocusedTextColor = PremiumWhite,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = PremiumGold
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                isSearchActive = false
                                searchQuery = ""
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                    )
                } else {
                    TopAppBar(
                        title = { Text("Manage Artists", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) { Icon(Icons.Default.Search, null, tint = PremiumGold) }
                            IconButton(onClick = { showFilterSheet = true }) { Icon(Icons.Rounded.FilterList, null, tint = PremiumGold) }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddArtistDialog = true },
                    containerColor = PremiumGold,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.Black)
                }
            },
            bottomBar = {
                AdminBottomNav(currentRoute = Screen.AdminArtistList.route, onNavigate = onNavigate)
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = AdminBackground
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Horizontal Scrollable Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tabs) { tab ->
                        val count = when(tab) {
                            "All" -> artists.size
                            "Active" -> artists.count { it.accountStatus == "ACTIVE" }
                            "Inactive" -> artists.count { it.accountStatus == "SUSPENDED" }
                            else -> 0
                        }
                        
                        Surface(
                            onClick = { selectedTab = tab },
                            color = if (selectedTab == tab) PremiumGold.copy(alpha = 0.15f) else AdminCardBackground,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (selectedTab == tab) PremiumGold else Color.White.copy(0.1f))
                        ) {
                            Text(
                                text = "$tab ($count)",
                                color = if (selectedTab == tab) PremiumGold else AdminSecondaryText,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (filteredArtists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Person, null, tint = AdminSecondaryText.copy(0.2f), modifier = Modifier.size(80.dp))
                            Text("No artists found", color = AdminSecondaryText)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredArtists, key = { it.uid }) { artist ->
                            LuxuryArtistAdminCard(
                                artist = artist,
                                onCopyId = { 
                                    clipboardManager.setText(AnnotatedString(artist.artistId))
                                    Toast.makeText(context, "Artist ID Copied: ${artist.artistId}", Toast.LENGTH_SHORT).show()
                                },
                                onQRClick = { artistForQR = artist },
                                onToggleStatus = {
                                    val newStatus = if (artist.accountStatus == "ACTIVE") "SUSPENDED" else "ACTIVE"
                                    viewModel.updateAccountStatus(artist.uid, newStatus) {
                                        Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEdit = { selectedArtistForEdit = artist },
                                onDelete = { artistToDelete = artist },
                                onChat = { artistToChat = artist },
                                onClick = { onArtistClick(artist.uid) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtistQRDialog(artist: User, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val qrData = "sangeetsetu://artist/${artist.artistId}"
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=$qrData"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Artist QR Pass", color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text(artist.name, color = PremiumWhite, fontSize = 14.sp)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(220.dp).padding(8.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    AsyncImage(
                        model = qrUrl,
                        contentDescription = "Artist QR Code",
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Surface(
                    color = AdminBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, PremiumGold.copy(0.3f)),
                    onClick = {
                        clipboardManager.setText(AnnotatedString(artist.artistId))
                        Toast.makeText(context, "ID Copied", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(artist.artistId, color = PremiumGold, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ContentCopy, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Scan to view profile instantly", color = AdminSecondaryText, fontSize = 11.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val shareIntent = android.content.Intent().apply {
                        action = android.content.Intent.ACTION_SEND
                        putExtra(android.content.Intent.EXTRA_TEXT, "Checkout Artist ${artist.name} on Sangeet Setu. ID: ${artist.artistId}\nDownload the app to scan QR!")
                        type = "text/plain"
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Artist ID"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Share, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share ID", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = AdminSecondaryText, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun FilterOption(label: String, isSelected: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onToggle(!isSelected) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = PremiumWhite, fontSize = 16.sp)
        Switch(
            checked = isSelected,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold, checkedTrackColor = PremiumGold.copy(0.4f))
        )
    }
}

@Composable
fun LuxuryArtistAdminCard(
    artist: User,
    onCopyId: () -> Unit,
    onQRClick: () -> Unit,
    onToggleStatus: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onChat: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        color = AdminCardBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(0.08f)),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box {
                    AsyncImage(
                        model = artist.photoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp).clip(CircleShape).border(1.5.dp, PremiumGold, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (artist.verificationStatus == "VERIFIED") {
                        Icon(
                            Icons.Rounded.Verified, null, tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).background(Color.White, CircleShape).padding(1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(artist.name, color = PremiumWhite, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        if (artist.isVip) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = PremiumGold, shape = RoundedCornerShape(6.dp)) {
                                Text(" VIP ", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val statusText = if (artist.isOnline) "🟢 Online" else "🔴 ${com.sangeetsetu.app.util.formatLastSeen(artist.lastSeen)}"
                        Text(statusText, color = if (artist.isOnline) SuccessColor else AdminSecondaryText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { if (artist.artistId.isNotEmpty()) onCopyId() }) {
                        Text(
                            text = artist.artistId.ifEmpty { "ID PENDING" },
                            color = if (artist.artistId.isNotEmpty()) PremiumGold else WarningColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        if (artist.artistId.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ContentCopy, null, tint = PremiumGold.copy(0.5f), modifier = Modifier.size(12.dp))
                        }
                    }
                    
                    Text(artist.category, color = AdminSecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("${artist.city}, ${artist.state}", color = AdminSecondaryText, fontSize = 11.sp)
                }
                
                IconButton(onClick = { onQRClick() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.QrCode, null, tint = PremiumWhite.copy(0.8f))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when(artist.approvalStatus) {
                        "APPROVED" -> SuccessColor
                        "REJECTED" -> ErrorColor
                        else -> WarningColor
                    }
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Status: ${artist.approvalStatus}", 
                        color = statusColor, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    if (artist.verificationStatus == "VERIFIED") {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(color = SuccessColor.copy(0.15f), shape = RoundedCornerShape(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                Icon(Icons.Rounded.Verified, null, tint = SuccessColor, modifier = Modifier.size(10.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("VERIFIED", color = SuccessColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (artist.verificationStatus == "PENDING") {
                        Spacer(modifier = Modifier.width(12.dp))
                        Surface(color = WarningColor.copy(0.2f), shape = RoundedCornerShape(4.dp)) {
                            Text(" ID PENDING ", color = WarningColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                        }
                    }
                    
                    if (artist.accountStatus == "SUSPENDED") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = ErrorColor.copy(0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text(" SUSPENDED ", color = ErrorColor, fontSize = 9.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(2.dp))
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onToggleStatus) {
                        Text(
                            if (artist.accountStatus == "ACTIVE") "Suspend" else "Activate",
                            color = if (artist.accountStatus == "ACTIVE") ErrorColor else SuccessColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        onClick = onClick,
                        color = PremiumGold.copy(0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, PremiumGold.copy(0.3f))
                    ) {
                        Text("GO TO PROFILE", color = PremiumGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    IconButton(onClick = {
                        val shareIntent = android.content.Intent().apply {
                            action = android.content.Intent.ACTION_SEND
                            putExtra(android.content.Intent.EXTRA_TEXT, "Artist: ${artist.name}\nID: ${artist.artistId}\nCategory: ${artist.category}\nLocation: ${artist.city}, ${artist.state}")
                            type = "text/plain"
                        }
                        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Artist ID"))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Share, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                    }
                    
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onChat, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = ErrorColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EditArtistDialog(
    artist: User,
    onDismiss: () -> Unit,
    onSave: (User) -> Unit
) {
    var name by remember { mutableStateOf(artist.name) }
    var category by remember { mutableStateOf(artist.category) }
    var phone by remember { mutableStateOf(artist.phone) }
    var state by remember { mutableStateOf(artist.state) }
    var district by remember { mutableStateOf(artist.district) }
    var selectedVerificationStatus by remember { mutableStateOf(artist.verificationStatus) }
    var selectedApprovalStatus by remember { mutableStateOf(artist.approvalStatus) }
    var isVIP by remember { mutableStateOf(artist.isVip) }

    val verificationOptions = listOf("PENDING", "VERIFIED", "REJECTED")
    val approvalOptions = listOf("PENDING", "APPROVED", "REJECTED")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = { Text("Edit Artist Profile", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                ArtistEditField("Artist Name", name) { name = it }
                ArtistEditField("Category", category) { category = it }
                ArtistEditField("Phone Number", phone) { phone = it }
                
                LocationSelector(
                    selectedState = state,
                    selectedDistrict = district,
                    onStateSelected = { state = it },
                    onDistrictSelected = { district = it }
                )
                
                Text("Verification Status", color = AdminSecondaryText, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(verificationOptions) { status ->
                        Surface(
                            onClick = { selectedVerificationStatus = status },
                            color = if (selectedVerificationStatus == status) PremiumGold.copy(0.2f) else AdminCardBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedVerificationStatus == status) PremiumGold else Color.White.copy(0.1f))
                        ) {
                            Text(status, color = if (selectedVerificationStatus == status) PremiumGold else AdminSecondaryText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 10.sp)
                        }
                    }
                }

                Text("Approval Status", color = AdminSecondaryText, fontSize = 14.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(approvalOptions) { status ->
                        Surface(
                            onClick = { selectedApprovalStatus = status },
                            color = if (selectedApprovalStatus == status) PremiumGold.copy(0.2f) else AdminCardBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (selectedApprovalStatus == status) PremiumGold else Color.White.copy(0.1f))
                        ) {
                            Text(status, color = if (selectedApprovalStatus == status) PremiumGold else AdminSecondaryText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 10.sp)
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("VIP / Premium", color = AdminSecondaryText, fontSize = 14.sp)
                    Checkbox(checked = isVIP, onCheckedChange = { isVIP = it }, colors = CheckboxDefaults.colors(checkedColor = PremiumGold))
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GoldenGradient)
                    .clickable { 
                        onSave(artist.copy(
                            name = name, 
                            category = category, 
                            phone = phone, 
                            state = state, 
                            district = district, 
                            verificationStatus = selectedVerificationStatus,
                            isVerified = selectedVerificationStatus == "VERIFIED",
                            approvalStatus = selectedApprovalStatus,
                            isApproved = selectedApprovalStatus == "APPROVED",
                            isVip = isVIP
                        )) 
                    }
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AdminSecondaryText)
            }
        }
    )
}

@Composable
fun AddArtistDialog(
    onDismiss: () -> Unit,
    onAdd: (User) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = { Text("Add New Artist", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ArtistEditField("Artist Name", name) { name = it }
                ArtistEditField("Email", email) { email = it }
                ArtistEditField("Category", category) { category = it }
                ArtistEditField("Phone Number", phone) { phone = it }
                
                LocationSelector(
                    selectedState = state,
                    selectedDistrict = district,
                    onStateSelected = { state = it },
                    onDistrictSelected = { district = it }
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (name.isNotBlank() && email.isNotBlank()) Color.Transparent else Color.Gray)
                    .let { if (name.isNotBlank() && email.isNotBlank()) it.background(GoldenGradient) else it }
                    .clickable(enabled = name.isNotBlank() && email.isNotBlank()) { 
                        onAdd(User(
                            name = name, 
                            email = email,
                            phone = phone, 
                            category = category,
                            userType = "Artist",
                            state = state, 
                            district = district,
                            accountStatus = "ACTIVE",
                            status = "ACTIVE"
                        )) 
                    }
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Add Artist", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AdminSecondaryText)
            }
        }
    )
}

@Composable
fun ArtistEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = AdminSecondaryText, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 14.sp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PremiumWhite,
                unfocusedTextColor = PremiumWhite,
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                cursorColor = PremiumGold
            )
        )
    }
}
