package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.components.UpiPaymentSheet
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(onBack: () -> Unit) {
    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedAmount by remember { mutableStateOf("501") }
    val customAmount = remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Sangeet Setu", color = PremiumWhite, fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(64.dp))
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "आपका सहयोग हमारी शक्ति है",
                color = PremiumGold,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NotoSansDevanagariFamily,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "संगीत सेतु भारतीय कला और संस्कृति को बढ़ावा देने का एक छोटा सा प्रयास है। आपके द्वारा दिया गया दान इस मंच को बेहतर बनाने में मदद करेगा।",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                fontFamily = NotoSansDevanagariFamily,
                lineHeight = 22.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("सहयोग राशि चुनें", color = PremiumWhite, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("101", "501", "1100").forEach { amount ->
                    DonationAmountChip(
                        amount = amount,
                        isSelected = selectedAmount == amount,
                        onClick = { 
                            selectedAmount = amount
                            customAmount.value = ""
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = customAmount.value,
                onValueChange = { 
                    customAmount.value = it
                    selectedAmount = ""
                },
                label = { Text("अन्य राशि दर्ज करें", color = SecondaryText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Button(
                onClick = { 
                    val finalAmount = if (customAmount.value.isNotEmpty()) customAmount.value else selectedAmount
                    if (finalAmount.isNotEmpty()) {
                        showPaymentSheet = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("अभी सहयोग करें ✨", color = AppBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "सभी दान सीधे साध्वी रश्मि शास्त्री जी के खाते में जाएंगे।",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontFamily = NotoSansDevanagariFamily
            )
        }
    }

    if (showPaymentSheet) {
        UpiPaymentSheet(
            note = "Donation to Sangeet Setu",
            onDismiss = { showPaymentSheet = false },
            onPaymentStarted = { }
        )
    }
}

@Composable
fun DonationAmountChip(amount: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.height(48.dp).width(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PremiumGold else CardBackground,
        border = if (!isSelected) BorderStroke(1.dp, BorderColor) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(amount, color = if (isSelected) AppBackground else Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
