package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(onBack: () -> Unit) {
    PremiumAdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Admin Profile", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            containerColor = AdminBackground
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Admin Profile Settings", color = AdminSecondaryText)
            }
        }
    }
}
