package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.AppSettings
import com.sangeetsetu.app.AppTheme
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentTheme by AppSettings.theme

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            "डिस्प्ले थीम (Theme)", 
                            color = PremiumGold, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBackIosNew, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                SettingsCard(title = "ऐप थीम चुनें") {
                    ThemeOption("लाइट मोड (Light)", currentTheme == AppTheme.LIGHT) { 
                        AppSettings.setTheme(context, AppTheme.LIGHT) 
                    }
                    ThemeOption("डार्क मोड (Dark)", currentTheme == AppTheme.DARK) { 
                        AppSettings.setTheme(context, AppTheme.DARK) 
                    }
                    ThemeOption("सिस्टम डिफ़ॉल्ट", currentTheme == AppTheme.SYSTEM) { 
                        AppSettings.setTheme(context, AppTheme.SYSTEM) 
                    }
                }
            }
        }
    }
}
