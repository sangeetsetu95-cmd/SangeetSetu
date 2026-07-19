package com.sangeetsetu.app.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUnlockSheet(
    artistName: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (String) -> Unit // Passes transaction ID
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isProcessing by remember { mutableStateOf(false) }
    var transactionId by remember { mutableStateOf("") }
    var paymentStatusMessage by remember { mutableStateOf<String?>(null) }
    
    val upiId = "sangeetsetu95@oksbi"
    val displayName = "Sangeet Setu"
    val amount = "11.00"
    val note = "Unlock $artistName Contact"
    
    // Generate a unique transaction reference
    val txnRef = remember { "SS${System.currentTimeMillis()}" }
    
    // UPI Deep Link with more parameters for better tracking
    val upiUri = "upi://pay?pa=$upiId&pn=${Uri.encode(displayName)}&mc=&tid=$txnRef&tr=$txnRef&tn=${Uri.encode(note)}&am=$amount&cu=INR"
    
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${Uri.encode(upiUri)}"

    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val response = data?.getStringExtra("response") ?: ""
            handleUpiResponse(response, onPaymentSuccess, { msg -> paymentStatusMessage = msg })
        } else {
            paymentStatusMessage = "Payment Cancelled or Failed"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PremiumNavyDark,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = PremiumGold.copy(0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = PremiumGold,
                modifier = Modifier.size(48.dp)
            )
            
            Text(
                "Secure Payment",
                color = PremiumGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = CinzelFamily
            )
            
            Text(
                "Unlock $artistName's contact details",
                color = PremiumWhite.copy(0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Payment Amount Display
            Surface(
                color = Color.White.copy(0.05f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, PremiumGold.copy(0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Payable Amount:", color = PremiumWhite.copy(0.6f), fontSize = 14.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("₹11", color = PremiumGold, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // QR Code Section
            Surface(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, PremiumGold.copy(0.2f), RoundedCornerShape(24.dp)),
                color = Color.White
            ) {
                AsyncImage(
                    model = qrUrl,
                    contentDescription = "UPI QR Code",
                    modifier = Modifier.padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Text(
                "Scan to Pay with any UPI App",
                color = PremiumWhite.copy(0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Payment Action
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                        val chooser = Intent.createChooser(intent, "Pay with")
                        upiLauncher.launch(chooser)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null, tint = PremiumNavy)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Pay with UPI App", color = PremiumNavy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            if (paymentStatusMessage != null) {
                Text(
                    text = paymentStatusMessage!!,
                    color = if (paymentStatusMessage!!.contains("Success")) SuccessColor else Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(color = Color.White.copy(0.1f))
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                "Manual Verification (If payment successful but not unlocked)",
                color = PremiumWhite.copy(0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = transactionId,
                onValueChange = { transactionId = it },
                label = { Text("Transaction ID / Ref No.", color = PremiumWhite.copy(0.5f)) },
                placeholder = { Text("Enter 12 digit Ref No.", color = PremiumWhite.copy(0.3f)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PremiumWhite,
                    unfocusedTextColor = PremiumWhite,
                    focusedBorderColor = PremiumGold,
                    unfocusedBorderColor = PremiumGold.copy(0.3f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (transactionId.length < 8) {
                        Toast.makeText(context, "Please enter a valid Transaction ID", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isProcessing = true
                    onPaymentSuccess(transactionId)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessColor.copy(0.8f)),
                shape = RoundedCornerShape(16.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = PremiumWhite, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.LockOpen, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Verify & Unlock Now", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Payment, null, tint = PremiumGold.copy(0.5f), modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "100% Secure & Direct Payment",
                    color = PremiumWhite.copy(0.5f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

private fun handleUpiResponse(
    response: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    // UPI response format is usually: txnId=...&responseCode=...&Status=SUCCESS&txnRef=...
    val map = response.split("&").associate {
        val parts = it.split("=")
        if (parts.size == 2) parts[0].lowercase() to parts[1]
        else parts[0].lowercase() to ""
    }

    val status = map["status"]?.lowercase() ?: ""
    val txnId = map["txnid"] ?: map["tr"] ?: "TXN-${System.currentTimeMillis()}"

    if (status == "success") {
        onSuccess(txnId)
    } else if (status == "submitted" || status == "pending") {
        onError("Payment is Pending. Please wait or enter ID manually.")
    } else {
        onError("Payment Failed. If amount deducted, enter ID manually.")
    }
}

