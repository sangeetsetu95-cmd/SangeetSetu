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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.AppSettings
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val currentLanguage by AppSettings.language

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            stringResource(R.string.language_screen_title), 
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
                SettingsCard(title = stringResource(R.string.choose_language)) {
                    ThemeOption(stringResource(R.string.english), currentLanguage == "English") { 
                        AppSettings.setLanguage(context, "English") 
                    }
                    ThemeOption(stringResource(R.string.hindi), currentLanguage == "Hindi") { 
                        AppSettings.setLanguage(context, "Hindi")
                    }
                    ThemeOption(stringResource(R.string.system_default), currentLanguage == "System") {
                        AppSettings.setLanguage(context, "System") 
                    }
                }
            }
        }
    }
}
