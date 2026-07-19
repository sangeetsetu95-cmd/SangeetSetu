package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.AppBackground
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizer Dashboard", color = PremiumWhite, fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = PremiumGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome, Organizer!",
                color = PremiumGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Manage your events and bookings here.",
                color = PremiumWhite,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { /* Navigate to create event */ },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold)
            ) {
                Text("Create New Event", color = AppBackground)
            }
        }
    }
}
