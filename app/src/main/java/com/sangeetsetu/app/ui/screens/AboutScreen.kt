package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import com.sangeetsetu.app.ui.theme.SecondaryText
import com.sangeetsetu.app.util.AppConstants

import androidx.compose.ui.res.stringResource
import com.sangeetsetu.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_sangeet_setu), style = MaterialTheme.typography.titleLarge, color = PremiumWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.sangeet_setu_logo),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.app_name),
                color = PremiumGold,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold
            )
            
            Text(
                stringResource(R.string.hero_subtitle),
                color = PremiumWhite.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            AboutSection("Our Mission", "Sangeet Setu is dedicated to preserving and promoting Indian traditional and contemporary arts by providing a premium platform for artists and organizers to connect.")
            
            AboutSection(stringResource(R.string.version), AppConstants.APP_VERSION)
            
            AboutSection("Developer", AppConstants.DEVELOPER_TEAM)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "© 2024 Sangeet Setu. All rights reserved.",
                color = SecondaryText,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AboutSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(title, color = PremiumGold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(content, color = PremiumWhite, style = MaterialTheme.typography.bodyMedium)
    }
}
