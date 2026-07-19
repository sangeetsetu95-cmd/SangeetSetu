package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.components.EarningsOverviewChart
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(onBack: () -> Unit, viewModel: AdminViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()

    PremiumAdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Analytics", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground),
                )
            },
            containerColor = AdminBackground
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnalyticsCard("Total Users", stats.totalUsers.toString(), SuccessColor)
                AnalyticsCard("Total Artists", stats.totalArtists.toString(), Color(0xFF8BC34A))
                AnalyticsCard("Total Bookings", stats.totalBookings.toString(), PremiumGold)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Booking Performance", color = PremiumWhite, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AnalyticsSmallCard("Completed", stats.completedBookings.toString(), SuccessColor, Modifier.weight(1f))
                    AnalyticsSmallCard("Cancelled", stats.cancelledBookings.toString(), ErrorColor, Modifier.weight(1f))
                }
                
                Text("Activity Overview", color = PremiumWhite, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                
                EarningsOverviewChart()
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AnalyticsCard(label: String, value: String, color: Color) {
    Surface(
        color = AdminCardBackground,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(label, color = AdminSecondaryText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier.size(50.dp).background(color.copy(0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Rounded.TrendingUp, null, tint = color, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
fun AnalyticsSmallCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = AdminCardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(label, color = AdminSecondaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
