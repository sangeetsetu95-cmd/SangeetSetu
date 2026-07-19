package com.sangeetsetu.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.sangeetsetu.app.repository.StorageRepository
import com.sangeetsetu.app.ui.components.LocationSelector
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.ui.theme.SuccessColor
import com.sangeetsetu.app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistDetailsScreen(
    artistId: String,
    onBack: () -> Unit,
    viewModel: AdminViewModel= hiltViewModel()
) {
    val context = LocalContext.current
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val artist = remember(artists, artistId) { artists.find { it.uid == artistId } }
    
    var name by remember(artist) { mutableStateOf(artist?.name ?: "") }
    var category by remember(artist) { mutableStateOf(artist?.category ?: "") }
    var skill by remember(artist) { mutableStateOf(artist?.skill ?: "") }
    var age by remember(artist) { mutableStateOf(artist?.age ?: "") }
    var experience by remember(artist) { mutableStateOf(artist?.experience ?: "") }
    var phone by remember(artist) { mutableStateOf(artist?.phone ?: "") }
    var whatsapp by remember(artist) { mutableStateOf(artist?.whatsapp ?: "") }
    var email by remember(artist) { mutableStateOf(artist?.email ?: "") }
    var state by remember(artist) { mutableStateOf(artist?.state ?: "") }
    var district by remember(artist) { mutableStateOf(artist?.district ?: "") }
    var city by remember(artist) { mutableStateOf(artist?.city ?: "") }
    var aboutMe by remember(artist) { mutableStateOf(artist?.aboutMe ?: "") }
    var selectedVerificationStatus by remember(artist) { mutableStateOf<String>(artist?.verificationStatus ?: "PENDING") }
    var selectedApprovalStatus by remember(artist) { mutableStateOf<String>(artist?.approvalStatus ?: "PENDING") }
    var selectedAccountStatus by remember(artist) { mutableStateOf<String>(artist?.accountStatus ?: "ACTIVE") }
    var isPremium by remember(artist) { mutableStateOf(artist?.isPremium ?: false) }
    var isVip by remember(artist) { mutableStateOf(artist?.isVip ?: false) }
    var photoUrl by remember(artist) { mutableStateOf(artist?.photoUrl ?: "") }
    var publicId by remember(artist) { mutableStateOf(artist?.cloudinaryPublicId ?: "") }
    var isUploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            isUploading = true
            scope.launch {
                val oldId = publicId
                val result = StorageRepository.uploadImage(it, "artist_profiles", "${artistId}_${System.currentTimeMillis()}")
                result.onSuccess { resource ->
                    photoUrl = resource.url
                    publicId = resource.publicId
                    isUploading = false
                    if (oldId.isNotEmpty()) {
                        StorageRepository.deleteImage(oldId)
                    }
                }.onFailure {
                    isUploading = false
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Artist Profile", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.deleteUser(artistId) {
                            Toast.makeText(context, "Artist Profile Deleted", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }) {
                        Icon(Icons.Rounded.Delete, null, tint = Color.Red.copy(0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        if (artist == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PremiumGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    ArtistPhotoSection(
                        photoUrl = photoUrl,
                        isUploading = isUploading,
                        onUploadClick = { launcher.launch("image/*") },
                        onRemoveClick = { 
                            photoUrl = ""
                            publicId = ""
                            Toast.makeText(context, "Photo marked for removal. Update profile to save.", Toast.LENGTH_LONG).show()
                        }
                    )
                }

                item {
                    SectionCard("Basic Information") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminEditField("Full Name", name) { name = it }
                            AdminEditField("Age", age) { age = it }
                            AdminEditField("Category", category) { category = it }
                            AdminEditField("Skill / Speciality", skill) { skill = it }
                            AdminEditField("Experience (Years)", experience) { experience = it }
                        }
                    }
                }

                item {
                    SectionCard("Location Details") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            LocationSelector(
                                selectedState = state,
                                selectedDistrict = district,
                                onStateSelected = { state = it },
                                onDistrictSelected = { district = it }
                            )
                            AdminEditField("City / Town", city) { city = it }
                        }
                    }
                }

                item {
                    SectionCard("Contact Details") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminEditField("Mobile Number", phone) { phone = it }
                            AdminEditField("WhatsApp Number", whatsapp) { whatsapp = it }
                            AdminEditField("Email Address", email) { email = it }
                        }
                    }
                }

                item {
                    SectionCard("Artist Bio") {
                        OutlinedTextField(
                            value = aboutMe,
                            onValueChange = { aboutMe = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 14.sp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PremiumGold,
                                unfocusedBorderColor = BorderColor
                            ),
                            placeholder = { Text("Write a compelling bio for the artist...", color = SecondaryText, fontSize = 14.sp) }
                        )
                    }
                }

                item {
                    SectionCard("Artist Status & Verification") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Account Status", color = SecondaryText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("ACTIVE", "SUSPENDED").forEach { s ->
                                    val isSelected = selectedAccountStatus == s
                                    Surface(
                                        onClick = { selectedAccountStatus = s },
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected) {
                                            if (s == "ACTIVE") SuccessColor.copy(0.2f) else Color.Red.copy(0.2f)
                                        } else AppBackground.copy(0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isSelected) {
                                            if (s == "ACTIVE") SuccessColor else Color.Red
                                        } else Color.Transparent)
                                    ) {
                                        Text(
                                            s,
                                            color = if (isSelected) Color.White else SecondaryText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(0.05f))

                            // Compact Toggles for Verified and VIP
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Verified Artist (✓)", color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Toggle verification badge", color = SecondaryText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = selectedVerificationStatus == "VERIFIED",
                                    onCheckedChange = { 
                                        selectedVerificationStatus = if (it) "VERIFIED" else "PENDING"
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PremiumGold,
                                        checkedTrackColor = PremiumGold.copy(0.3f)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Premium Status (💎)", color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Unlock premium features for artist", color = SecondaryText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isPremium,
                                    onCheckedChange = { isPremium = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PremiumGold,
                                        checkedTrackColor = PremiumGold.copy(0.3f)
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("VIP Artist (⭐)", color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Feature in VIP section on Home", color = SecondaryText, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = isVip,
                                    onCheckedChange = { isVip = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PremiumGold,
                                        checkedTrackColor = PremiumGold.copy(0.3f)
                                    )
                                )
                            }

                            HorizontalDivider(color = Color.White.copy(0.05f))

                            Text("Verification Status (Advanced)", color = SecondaryText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("PENDING", "VERIFIED", "REJECTED").forEach { s ->
                                    val isSelected = selectedVerificationStatus == s
                                    Surface(
                                        onClick = { selectedVerificationStatus = s },
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected) PremiumGold.copy(0.2f) else AppBackground.copy(0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isSelected) PremiumGold else Color.Transparent)
                                    ) {
                                        Text(
                                            s,
                                            color = if (isSelected) Color.White else SecondaryText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color.White.copy(0.05f))

                            Text("Approval Status", color = SecondaryText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("PENDING", "APPROVED", "REJECTED").forEach { s ->
                                    val isSelected = selectedApprovalStatus == s
                                    Surface(
                                        onClick = { selectedApprovalStatus = s },
                                        modifier = Modifier.weight(1f),
                                        color = if (isSelected) PremiumGold.copy(0.2f) else AppBackground.copy(0.3f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (isSelected) PremiumGold else Color.Transparent)
                                    ) {
                                        Text(
                                            s,
                                            color = if (isSelected) Color.White else SecondaryText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val updatedUser = artist.copy(
                                name = name,
                                age = age,
                                category = category,
                                skill = skill,
                                experience = experience,
                                phone = phone,
                                whatsapp = whatsapp,
                                email = email,
                                state = state,
                                district = district,
                                city = city,
                                aboutMe = aboutMe,
                                verificationStatus = selectedVerificationStatus,
                                isVerified = selectedVerificationStatus == "VERIFIED",
                                approvalStatus = selectedApprovalStatus,
                                accountStatus = selectedAccountStatus,
                                isPremium = isPremium,
                                isVip = isVip,
                                status = selectedAccountStatus,
                                photoUrl = photoUrl,
                                cloudinaryPublicId = publicId
                            )
                            viewModel.updateUserProfile(updatedUser) {
                                Toast.makeText(context, "Profile Synchronized with Firebase", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading && !isUploading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = AppBackground, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Update Profile", color = AppBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun ArtistPhotoSection(
    photoUrl: String,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(CardBackground)
                        .border(2.dp, PremiumGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, PremiumGold, CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person),
                    error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.Person)
                )
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
                    .clickable { onUploadClick() },
                shape = CircleShape,
                color = PremiumGold,
                border = BorderStroke(2.dp, AppBackground)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CameraAlt, null, tint = AppBackground, modifier = Modifier.size(18.dp))
                }
            }
        }
        
        if (photoUrl.isNotEmpty()) {
            TextButton(onClick = onRemoveClick) {
                Text("Remove Profile Photo", color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardBackground,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
fun AdminEditField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, color = SecondaryText, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(color = PremiumWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = AppBackground.copy(0.3f),
                unfocusedContainerColor = AppBackground.copy(0.3f)
            )
        )
    }
}
