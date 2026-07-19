package com.sangeetsetu.app.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.sangeetsetu.app.model.PaymentRequest
import com.sangeetsetu.app.repository.PaymentRepository
import com.sangeetsetu.app.repository.UserRepository
import com.sangeetsetu.app.ui.theme.BorderColor
import com.sangeetsetu.app.ui.theme.CardBackground
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPaymentSheet(
    note: String,
    onDismiss: () -> Unit,
    onPaymentStarted: () -> Unit,
    onPaymentSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var isConfirming by remember { mutableStateOf(false) }
    
    val upiId = "sadhvi.rashmi9119@okhdfcbank"
    val displayName = "Sadhvi Rashmi Shastri Ji"
    
    // UPI Deep Link
    val upiUri = "upi://pay?pa=$upiId&pn=${Uri.encode(displayName)}&cu=INR&tn=${Uri.encode(note)}"
    
    // QR Code URL (using qrserver.com)
    val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${Uri.encode(upiUri)}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderColor) },
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "भुगतान विवरण",
                color = PremiumGold,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NotoSansDevanagariFamily,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // QR Code Section - Premium Glass Card Effect
            Surface(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp)),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                AsyncImage(
                    model = qrUrl,
                    contentDescription = "UPI QR Code",
                    modifier = Modifier.padding(16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                displayName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PoppinsFamily
            )

            Text(
                upiId,
                color = PremiumGold,
                fontSize = 14.sp,
                fontFamily = PoppinsFamily,
                modifier = Modifier.padding(top = 4.dp),
                letterSpacing = 0.5.sp
            )
            
            // Copy UPI ID Button - Premium Outlined Style
            OutlinedButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(upiId))
                    Toast.makeText(context, "UPI ID Copied", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.padding(vertical = 16.dp),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumGold),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy UPI ID", color = Color.White, fontSize = 13.sp, fontFamily = PoppinsFamily)
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = BorderColor.copy(alpha = 0.5f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action: Open any UPI App - Gold Premium Button
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                        val chooser = Intent.createChooser(intent, "Pay using")
                        context.startActivity(chooser)
                        onPaymentStarted()
                    } catch (_: Exception) {
                        Toast.makeText(context, "No UPI app found", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PremiumGold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Open in any UPI App",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = PoppinsFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Action: Confirm Payment After Paying
            OutlinedButton(
                onClick = {
                    val uid = UserRepository.getCurrentUserId() ?: return@OutlinedButton
                    isConfirming = true
                    scope.launch {
                        val request = PaymentRequest(
                            userId = uid,
                            note = note,
                            upiIdUsed = upiId
                        )
                        PaymentRepository.createPaymentRequest(request)
                            .onSuccess {
                                Toast.makeText(context, "Payment confirmation request sent!", Toast.LENGTH_LONG).show()
                                isConfirming = false
                                onPaymentSuccess()
                                onDismiss()
                            }
                            .onFailure {
                                Toast.makeText(context, "Failed to send request. Try again.", Toast.LENGTH_SHORT).show()
                                isConfirming = false
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                border = BorderStroke(1.dp, PremiumGold),
                shape = RoundedCornerShape(16.dp),
                enabled = !isConfirming
            ) {
                if (isConfirming) {
                    CircularProgressIndicator(color = PremiumGold, modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirm Payment (Done Already)", color = PremiumGold, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Support Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Supported: ", color = SecondaryText, fontSize = 12.sp, fontFamily = PoppinsFamily)
                Text("GPay • PhonePe • Paytm • BHIM", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = PoppinsFamily)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "भुगतान के बाद कृपया ऐप पर वापस आएं।",
                color = SecondaryText.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = NotoSansDevanagariFamily
            )
        }
    }
}
