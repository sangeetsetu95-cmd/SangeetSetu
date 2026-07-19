package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPaymentsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    viewModel: com.sangeetsetu.app.viewmodel.AdminViewModel = hiltViewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val payments = remember(bookings) {
        bookings
    }

    PremiumAdminTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Payment History", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AdminBackground)
                )
            },
            bottomBar = {
                AdminBottomNav(currentRoute = Screen.AdminPayments.route, onNavigate = onNavigate)
            },
            containerColor = AdminBackground
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (isLoading && payments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold)
                    }
                } else if (payments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No payment history found", color = AdminSecondaryText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(payments) { payment ->
                            PaymentItem(payment)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentItem(booking: com.sangeetsetu.app.model.Booking) {
    val artistName = booking.artistName
    val userName = booking.userName
    val date = booking.eventDate
    val status = booking.status.name

    Surface(
        color = AdminCardBackground,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(SuccessColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Payments, null, tint = SuccessColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Booking by $userName", color = PremiumWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("for $artistName", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(date, color = AdminSecondaryText, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    status, 
                    color = if (status == "COMPLETED") SuccessColor else WarningColor, 
                    fontWeight = FontWeight.ExtraBold, 
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
