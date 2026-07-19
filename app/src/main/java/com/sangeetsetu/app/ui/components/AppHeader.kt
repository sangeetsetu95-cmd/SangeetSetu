package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import com.sangeetsetu.app.AppSettings
import com.sangeetsetu.app.AppTheme
import com.sangeetsetu.app.model.LocationData
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.ui.theme.PremiumGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    userPhotoUrl: String = "",
    unreadNotifications: Int = 0,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var showThemeSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var showLocationSheet by remember { mutableStateOf(false) }

    val currentTheme = AppSettings.theme.value
    val currentLanguage = AppSettings.language.value
    val currentLocation = AppSettings.location.value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 📍 Location Button (Outlined Style)
            Surface(
                onClick = { showLocationSheet = true },
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = PremiumGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        currentLocation,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = NotoSansDevanagariFamily,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
            }

            // Logo in Center
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { /* Logo Click */ }
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.sangeetsetu.app.R.drawable.sangeet_setu_logo),
                    contentDescription = "Sangeet Setu Logo",
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    "Sangeet Setu",
                    color = PremiumGold,
                    fontSize = 12.sp,
                    fontFamily = PoppinsFamily,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // 🌐 Language
                Surface(
                    onClick = { showLanguageSheet = true },
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Language, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentLanguage, color = Color.White, fontSize = 11.sp)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🔍 Search
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Search, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🌙 Theme
                IconButton(
                    onClick = { showThemeSheet = true },
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = when (currentTheme) {
                            AppTheme.LIGHT -> Icons.Default.LightMode
                            AppTheme.DARK -> Icons.Default.DarkMode
                            AppTheme.SYSTEM -> Icons.Default.SettingsSuggest
                            AppTheme.RED -> Icons.Default.Palette
                        },
                        contentDescription = "Theme",
                        tint = PremiumGold,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🔔 Notifications
                Box {
                    IconButton(onClick = onNotificationClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    if (unreadNotifications > 0) {
                        Surface(
                            color = Color.Red,
                            shape = CircleShape,
                            modifier = Modifier.size(14.dp).align(Alignment.TopEnd)
                        ) {
                            Text(
                                if (unreadNotifications > 99) "99+" else unreadNotifications.toString(),
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 👤 Profile
                Surface(
                    modifier = Modifier.size(36.dp).clip(CircleShape).clickable { onProfileClick() },
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    if (userPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.padding(6.dp),
                            tint = PremiumGold
                        )
                    }
                }
            }
        }
    }

    // Theme Selection Sheet
    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showThemeSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            ThemeSelectionContent(
                currentTheme = currentTheme,
                onThemeSelected = {
                    AppSettings.setTheme(context, it)
                    showThemeSheet = false
                }
            )
        }
    }

    // Language Selection Sheet
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            LanguageSelectionContent(
                currentLanguage = currentLanguage,
                onLanguageSelected = {
                    AppSettings.setLanguage(context, it)
                    showLanguageSheet = false
                }
            )
        }
    }

    // Location Selection Sheet
    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            LocationSelectionContent(
                onLocationSelected = { loc ->
                    AppSettings.setLocation(context, loc)
                    showLocationSheet = false
                }
            )
        }
    }
}

@Composable
fun ThemeSelectionContent(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "थीम चुनें",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontFamily = NotoSansDevanagariFamily
            )
            IconButton(onClick = { /* Handle Close */ }) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ThemeOptionItem("लाइट मोड", Icons.Default.LightMode, currentTheme == AppTheme.LIGHT) {
            onThemeSelected(AppTheme.LIGHT)
        }
        ThemeOptionItem("डार्क मोड", Icons.Default.DarkMode, currentTheme == AppTheme.DARK) {
            onThemeSelected(AppTheme.DARK)
        }
        ThemeOptionItem("सिस्टम डिफ़ॉल्ट", Icons.Default.SettingsSuggest, currentTheme == AppTheme.SYSTEM) {
            onThemeSelected(AppTheme.SYSTEM)
        }
        ThemeOptionItem("रेड मोड", Icons.Default.Palette, currentTheme == AppTheme.RED) {
            onThemeSelected(AppTheme.RED)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onThemeSelected(currentTheme) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("सेव करें", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
        }
    }
}

@Composable
fun ThemeOptionItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PremiumGold.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) border(1.dp, PremiumGold, RoundedCornerShape(12.dp)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) PremiumGold else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                label,
                fontSize = 16.sp,
                color = if (isSelected) PremiumGold else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PremiumGold)
            }
        }
    }
}

@Composable
fun LanguageSelectionContent(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf("English", "हिंदी", "मराठी", "ગુજરાતી", "বাংলা")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "अपनी भाषा चुनें",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = NotoSansDevanagariFamily,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        languages.forEach { lang ->
            LanguageOptionItem(lang, currentLanguage == lang) {
                onLanguageSelected(lang)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LanguageOptionItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PremiumGold.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) border(1.dp, PremiumGold, RoundedCornerShape(12.dp)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 16.sp,
                color = if (isSelected) PremiumGold else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PremiumGold)
            }
        }
    }
}

@Composable
fun LocationSelectionContent(
    onLocationSelected: (String) -> Unit
) {
    var selectedState by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .heightIn(max = 600.dp)
    ) {
        Text(
            if (selectedState == null) "अपना राज्य चुनें" 
            else "अपना जिला चुनें (${selectedState})",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontFamily = NotoSansDevanagariFamily,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Premium Search bar
        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("खोजें...", color = Color.Gray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = PremiumGold, modifier = Modifier.size(20.dp)) },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = PremiumGold,
                unfocusedBorderColor = Color.White.copy(0.1f),
                focusedContainerColor = Color.White.copy(0.05f),
                unfocusedContainerColor = Color.White.copy(0.02f)
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (selectedState == null) {
                val filteredStates = LocationData.states.filter { it.contains(searchQuery, ignoreCase = true) }
                items(filteredStates) { state ->
                    LocationItem(
                        label = state,
                        onMainClick = { onLocationSelected(state) },
                        onArrowClick = { 
                            selectedState = state
                            searchQuery = ""
                        },
                        showArrow = true
                    )
                }
            } else {
                val districts = LocationData.stateDistricts[selectedState!!] ?: emptyList()
                val filteredDistricts = districts.filter { it.contains(searchQuery, ignoreCase = true) }
                items(filteredDistricts) { district ->
                    LocationItem(
                        label = district,
                        onMainClick = { onLocationSelected(district) },
                        onArrowClick = {},
                        showArrow = false
                    )
                }
            }
        }
        
        if (selectedState != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                onClick = { 
                    selectedState = null
                    searchQuery = ""
                },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(0.1f))
            ) {
                Text(
                    "राज्यों की सूची पर वापस जाएं", 
                    color = PremiumGold, 
                    fontFamily = NotoSansDevanagariFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LocationItem(
    label: String, 
    onMainClick: () -> Unit,
    onArrowClick: () -> Unit,
    showArrow: Boolean
) {
    Surface(
        onClick = onMainClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            
            if (showArrow) {
                IconButton(
                    onClick = { onArrowClick() },
                    modifier = Modifier.size(32.dp).background(PremiumGold.copy(0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Districts",
                        tint = PremiumGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Helper for border from HomeScreen.kt logic
private fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape) =
    BorderStroke(width, color)
