package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockOpen
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
import com.sangeetsetu.app.viewmodel.AdminViewModel
import com.sangeetsetu.app.util.formatTimeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUnlockPaymentsScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val payments by viewModel.unlockPayments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Unlock Payments", color = PremiumGold, fontWeight = FontWeight.ExtraBold) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PremiumGold)
                }
            } else if (payments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No unlock payments found", color = AdminSecondaryText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(payments) { payment ->
                        UnlockPaymentItem(payment)
                    }
                }
            }
        }
    }
}

@Composable
fun UnlockPaymentItem(payment: com.sangeetsetu.app.model.UnlockPayment) {
    Surface(
        color = AdminCardBackground,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(PremiumGold.copy(0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LockOpen, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(payment.userName, color = PremiumWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Unlocked: ${payment.artistName}", color = PremiumGold, fontSize = 13.sp)
                }
                Text("₹${payment.amount.toInt()}", color = SuccessColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(0.05f))
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Transaction ID", color = AdminSecondaryText, fontSize = 10.sp)
                    Text(payment.transactionId, color = PremiumWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", color = AdminSecondaryText, fontSize = 10.sp)
                    Text(payment.paymentStatus.uppercase(), color = SuccessColor, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatTimeAgo(payment.unlockedAt),
                color = AdminSecondaryText.copy(0.7f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
