package com.sangeetsetu.app.ui.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sangeetsetu.app.model.User
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val userProfile by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadProfilePhoto(it) { newUrl ->
                photoUrl = newUrl
                scope.launch { snackbarHostState.showSnackbar("Photo uploaded successfully") }
            }
        }
    }

    var isInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            if (!isInitialized) {
                name = it.name
                phone = it.phone
                state = it.state
                district = it.district
                photoUrl = it.photoUrl
                isInitialized = true
                Log.d("EditProfile", "Screen initialized with profile: ${it.uid}")
            } else {
                // Background sync for photo only
                if (it.photoUrl != photoUrl && it.photoUrl.isNotEmpty()) {
                    photoUrl = it.photoUrl
                }
            }
        }
    }

    LaunchedEffect(error) {
        error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PremiumNavyDark)
            )
        },
        containerColor = PremiumNavy
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading && !isInitialized) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PremiumGold
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Photo
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clickable { if (!isLoading) launcher.launch("image/*") },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(2.dp, PremiumGold, CircleShape),
                            contentScale = ContentScale.Crop,
                            error = rememberVectorPainter(Icons.Default.Person),
                            placeholder = rememberVectorPainter(Icons.Default.Person)
                        )
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PremiumGold, modifier = Modifier.size(24.dp))
                            }
                        }
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = PremiumGold,
                            contentColor = PremiumNavy
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                null,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    if (photoUrl.isNotEmpty()) {
                        TextButton(
                            onClick = { 
                                viewModel.removeProfilePhoto()
                                photoUrl = ""
                                scope.launch { snackbarHostState.showSnackbar("Photo removed") }
                            },
                            enabled = !isLoading
                        ) {
                            Text("Remove Photo", color = Color.Red, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name", color = PremiumGray) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PremiumWhite,
                            unfocusedTextColor = PremiumWhite,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone", color = PremiumGray) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PremiumWhite,
                            unfocusedTextColor = PremiumWhite,
                            focusedBorderColor = PremiumGold,
                            unfocusedBorderColor = CardBorder
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LocationSelector(
                        selectedState = state,
                        selectedDistrict = district,
                        onStateSelected = { 
                            state = it
                            district = "" // Reset district locally
                            userProfile?.let { profile ->
                                viewModel.updateProfile(profile.copy(state = it, district = "")) {}
                            }
                        },
                        onDistrictSelected = { 
                            district = it
                            userProfile?.let { profile ->
                                viewModel.updateProfile(profile.copy(district = it)) {}
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            val updatedUser = User(
                                name = name.trim(),
                                phone = phone.trim(),
                                state = state,
                                district = district,
                                photoUrl = photoUrl
                            )
                            viewModel.updateProfile(updatedUser) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Profile details saved!")
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !isLoading && name.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading && isInitialized) {
                            CircularProgressIndicator(color = PremiumNavy, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PremiumNavy)
                        }
                    }
                }
            }
        }
    }
}
