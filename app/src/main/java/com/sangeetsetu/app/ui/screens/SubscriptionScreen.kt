package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.components.UpiPaymentSheet
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.ui.theme.PremiumGold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.sangeetsetu.app.viewmodel.ConfigViewModel
import com.sangeetsetu.app.model.SubscriptionPlan

@Composable
fun SubscriptionScreen(
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var showPaymentSheet by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    val plans by viewModel.subscriptionPlans.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // App Diamond Icon
        Icon(
            imageVector = Icons.Default.Diamond,
            contentDescription = null,
            tint = PremiumGold,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sangeet Setu Premium",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = PoppinsFamily,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "अपनी कला को दें Premium पहचान",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp),
            fontFamily = NotoSansDevanagariFamily,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        // Subscription Plans
        plans.forEach { plan ->
            SubscriptionPlanCard(
                plan = plan,
                onSelect = { 
                    selectedPlan = it
                    showPaymentSheet = true 
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (plans.isEmpty()) {
            Text("No subscription plans available", color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Benefits Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "✨ Premium के लाभ",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NotoSansDevanagariFamily,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            BenefitItem("आपकी प्रोफ़ाइल को Premium Badge मिलेगा।", Icons.Default.Stars)
            BenefitItem("अधिक ग्राहकों तक पहुँचने का अवसर।", Icons.Default.Groups)
            BenefitItem("ग्राहक आपका मोबाइल नंबर सीधे देख सकेंगे।", Icons.Default.Call)
            BenefitItem("डायरेक्ट कॉल और चैट की सुविधा।", Icons.AutoMirrored.Rounded.Chat)
            BenefitItem("जल्दी बुकिंग पाने का मौका बढ़ाएँ।", Icons.Default.Speed)
            BenefitItem("खोज (Search) में बेहतर पहचान।", Icons.Default.Search)
            BenefitItem("प्राथमिकता सहायता (Priority Support)।", Icons.Default.VerifiedUser)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Trust Info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("100% सुरक्षित भुगतान", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = NotoSansDevanagariFamily)
                Text("आपका भुगतान पूरी तरह सुरक्षित और विश्वसनीय है।", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontFamily = NotoSansDevanagariFamily)
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showPaymentSheet && selectedPlan != null) {
        UpiPaymentSheet(
            note = "Sangeet Setu ${selectedPlan!!.name} Subscription",
            onDismiss = { showPaymentSheet = false },
            onPaymentStarted = {}
        )
    }
}

@Composable
fun SubscriptionPlanCard(plan: SubscriptionPlan, onSelect: (SubscriptionPlan) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                plan.name,
                color = Color.Black,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFamily
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = plan.description,
                color = Color.Black.copy(alpha = 0.7f),
                fontSize = 12.sp,
                fontFamily = NotoSansDevanagariFamily,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onSelect(plan) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("शुरू करें", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BenefitItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = PremiumGold.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text, 
            color = Color.White.copy(alpha = 0.9f), 
            fontSize = 15.sp, 
            fontFamily = NotoSansDevanagariFamily,
            lineHeight = 20.sp
        )
    }
}
