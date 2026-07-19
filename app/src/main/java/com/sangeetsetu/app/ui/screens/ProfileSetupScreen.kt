package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.components.PremiumButton
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ProfileViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    onProfileComplete: () -> Unit,
    onArtistSelected: () -> Unit = {}, // Keeping for compatibility in NavGraph
    viewModel: ProfileViewModel = hiltViewModel(),
    configViewModel: ConfigViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val states by configViewModel.states.collectAsStateWithLifecycle()

    // Form State
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Artist") } 
    var selectedCategory by remember { mutableStateOf("") }
    
    var showCategoryDialog by remember { mutableStateOf(false) }
    
    val artistCategories = listOf(
        "Bhajan Singer", "Kathavachak", "Music Band", "Dholak Player", "Tabla Player",
        "Harmonium Player", "Flute Player", "Keyboard Player", "Guitar Player", "DJ",
        "Band Party", "Sound System", "Cameraman", "Videographer", "Decoration", "Other"
    )
    
    // Artist Specific fields are now handled by DynamicRegistrationScreen
    var whatsapp by remember { mutableStateOf("") }
    
    // Organizer Specific
    var organizationName by remember { mutableStateOf("") }
    var aboutOrganizer by remember { mutableStateOf("") }

    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }
    var acceptedTerms by remember { mutableStateOf(false) }

    // Prefill data if available
    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (name.isEmpty()) name = it.name
            if (phone.isEmpty()) phone = it.phone
            if (whatsapp.isEmpty()) whatsapp = it.whatsapp
            if (state.isEmpty()) state = it.state
            if (district.isEmpty()) district = it.district
        }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = PremiumNavyDeep
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))
                
                Text(
                    "Complete Your Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PremiumGold,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NotoSansDevanagariFamily
                )
                Text(
                    "Please fill all details to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PremiumGray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Basic Details Section
                SectionHeader(title = "Basic Information", icon = Icons.Default.Info)
                
                PremiumField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    icon = Icons.Default.Person
                )

                PremiumField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = "Mobile Number",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )

                LocationSelector(
                    selectedState = state,
                    selectedDistrict = district,
                    onStateSelected = { state = it },
                    onDistrictSelected = { district = it },
                    states = states.map { it.name },
                    districts = configViewModel.getDistrictsForState(state)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Role Selection
                SectionHeader(title = "I am a...", icon = Icons.Default.RecentActors)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoleCard(
                        title = "Artist",
                        subtitle = "I provide services as an artist",
                        icon = Icons.Default.MusicNote,
                        isSelected = userType == "Artist",
                        onClick = { userType = "Artist" },
                        modifier = Modifier.weight(1f)
                    )
                    RoleCard(
                        title = "Organizer",
                        subtitle = "I book artists for events",
                        icon = Icons.Default.Event,
                        isSelected = userType == "Organizer",
                        onClick = { userType = "Organizer" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Artist Category Selection
                AnimatedVisibility(
                    visible = userType == "Artist",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        SectionHeader(title = "Which category are you?", icon = Icons.Default.Category)
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategoryDialog = true },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, if (selectedCategory.isNotEmpty()) PremiumGold else PremiumGold.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = PremiumGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                                        color = if (selectedCategory.isEmpty()) PremiumGray else Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = PremiumGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                if (showCategoryDialog) {
                    androidx.compose.ui.window.Dialog(onDismissRequest = { showCategoryDialog = false }) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight(0.7f),
                            color = PremiumNavyDeep,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, PremiumGold.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Select Category",
                                    color = PremiumGold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(artistCategories) { category ->
                                        val isSelected = selectedCategory == category
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedCategory = category
                                                    showCategoryDialog = false
                                                },
                                            color = if (isSelected) PremiumGold.copy(alpha = 0.1f) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                width = 1.dp,
                                                color = if (isSelected) PremiumGold.copy(alpha = 0.5f) else Color.Transparent
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = category,
                                                    color = if (isSelected) PremiumGold else PremiumWhite,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(
                                    onClick = { showCategoryDialog = false },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("CANCEL", color = PremiumGray)
                                }
                            }
                        }
                    }
                }

                // Dynamic Role-Specific Fields (Artist fields removed as they are now in DynamicRegistrationScreen)
                AnimatedVisibility(
                    visible = userType == "Organizer",
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeader(title = "Organizer Details", icon = Icons.Default.Business)
                        
                        PremiumField(
                            value = organizationName,
                            onValueChange = { organizationName = it },
                            label = "Organization Name",
                            icon = Icons.Default.Domain
                        )

                        PremiumField(
                            value = aboutOrganizer,
                            onValueChange = { aboutOrganizer = it },
                            label = "About Organizer",
                            icon = Icons.Default.Description,
                            singleLine = false,
                            modifier = Modifier.height(120.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Terms and Conditions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = acceptedTerms,
                        onCheckedChange = { acceptedTerms = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PremiumGold,
                            uncheckedColor = PremiumGray
                        )
                    )
                    Text(
                        "I agree to the Terms & Conditions and Privacy Policy",
                        color = PremiumGray,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { acceptedTerms = !acceptedTerms }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue Button
                PremiumButton(
                    text = if (userType == "Artist") "CONTINUE TO ARTIST REGISTRATION" else "COMPLETE REGISTRATION",
                    isLoading = isLoading,
                    enabled = !isLoading && acceptedTerms && name.isNotEmpty() && phone.isNotEmpty() && state.isNotEmpty() && district.isNotEmpty() && (userType != "Artist" || selectedCategory.isNotEmpty()),
                    onClick = {
                        if (userType == "Artist") {
                            viewModel.updateProfile(
                                com.sangeetsetu.app.model.User(
                                    uid = viewModel.getCurrentUserId() ?: "",
                                    name = name,
                                    phone = phone,
                                    state = state,
                                    district = district,
                                    userType = userType,
                                    category = selectedCategory,
                                    role = "artist"
                                ),
                                onSuccess = {
                                    onArtistSelected()
                                }
                            )
                        } else {
                            viewModel.completeProfile(
                                name = name,
                                phone = phone,
                                state = state,
                                district = district,
                                userType = userType,
                                organizationName = organizationName,
                                aboutMe = aboutOrganizer,
                                photoUri = profilePhotoUri,
                                onSuccess = {
                                    onProfileComplete()
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )

                Spacer(modifier = Modifier.height(50.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = PremiumWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PremiumField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(20.dp)) },
        modifier = modifier.fillMaxWidth().padding(bottom = 16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.05f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
            focusedBorderColor = PremiumGold,
            unfocusedBorderColor = PremiumGold.copy(alpha = 0.2f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = PremiumGold,
            unfocusedLabelColor = PremiumGray,
            cursorColor = PremiumGold
        )
    )
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderBrush = if (isSelected) {
        Brush.linearGradient(listOf(PremiumGold, PremiumGoldDark))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(0.1f), Color.White.copy(0.05f)))
    }

    Surface(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() }
            .shadow(if (isSelected) 8.dp else 0.dp, RoundedCornerShape(20.dp), spotColor = PremiumGold),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PremiumNavyLight else Color.White.copy(0.03f),
        border = BorderStroke(1.5.dp, borderBrush)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null, 
                tint = if (isSelected) PremiumGold else PremiumGray,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title, 
                color = if (isSelected) PremiumGold else PremiumWhite, 
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                subtitle, 
                color = PremiumGray, 
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}
