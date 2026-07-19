package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPaymentsScreen(onBack: () -> Unit) {
    // Demo data for now, could be fetched from Firestore
    val payments = listOf(
        PaymentItem("Booking for Bhajan Sandhya", "Success", "12 May 2024"),
        PaymentItem("Artist Advance", "Pending", "10 May 2024"),
        PaymentItem("Subscription - Monthly", "Success", "01 May 2024")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Payments", color = PremiumWhite, fontWeight = FontWeight.Bold) },
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
        if (payments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No payment history found", color = PremiumGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(payments) { payment ->
                    PaymentCard(payment)
                }
            }
        }
    }
}

data class PaymentItem(val title: String, val status: String, val date: String)

@Composable
fun PaymentCard(payment: PaymentItem) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(PremiumGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Payment, null, tint = PremiumGold)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(payment.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = PoppinsFamily)
                Text(payment.date, color = PremiumGray, fontSize = 12.sp, fontFamily = PoppinsFamily)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    payment.status, 
                    color = if (payment.status == "Success") Color.Green else Color.Yellow, 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
