package com.sangeetsetu.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.ui.theme.AdminBackground
import com.sangeetsetu.app.ui.theme.AdminCardBackground
import com.sangeetsetu.app.ui.theme.AdminSecondaryText
import com.sangeetsetu.app.ui.theme.GoldenGradient
import com.sangeetsetu.app.ui.theme.NotoSansDevanagariFamily
import com.sangeetsetu.app.ui.theme.PoppinsFamily
import com.sangeetsetu.app.ui.theme.PremiumGold
import com.sangeetsetu.app.ui.theme.PremiumWhite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sangeetsetu.app.util.formatAmount

@Composable
fun AdminMessageDialog(recipientName: String, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AdminCardBackground,
        title = { Text("Message to $recipientName", color = PremiumGold, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Type your message here...", color = AdminSecondaryText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PremiumWhite,
                    unfocusedTextColor = PremiumWhite,
                    focusedBorderColor = PremiumGold,
                    unfocusedBorderColor = Color.White.copy(0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { if (message.isNotBlank()) onSend(message) },
                colors = ButtonDefaults.buttonColors(containerColor = PremiumGold),
                shape = RoundedCornerShape(12.dp),
                enabled = message.isNotBlank()
            ) {
                Text("Send Message", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AdminSecondaryText) }
        }
    )
}

@Composable
fun EarningsOverviewChart() {
    Surface(
        modifier = Modifier.fillMaxWidth(), 
        color = AdminCardBackground, 
        shape = RoundedCornerShape(28.dp), 
        border = BorderStroke(1.dp, Color.White.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("बुकिंग के रुझान", color = AdminSecondaryText, fontSize = 13.sp, fontFamily = NotoSansDevanagariFamily, fontWeight = FontWeight.Bold)
                    Text("Booking Trends", color = PremiumWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
                Surface(color = AdminBackground, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, PremiumGold.copy(0.3f))) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("This Month ", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.8f)
                    lineTo(size.width * 0.15f, size.height * 0.6f)
                    lineTo(size.width * 0.3f, size.height * 0.7f)
                    lineTo(size.width * 0.45f, size.height * 0.4f)
                    lineTo(size.width * 0.6f, size.height * 0.5f)
                    lineTo(size.width * 0.75f, size.height * 0.2f)
                    lineTo(size.width * 0.9f, size.height * 0.25f)
                    lineTo(size.width, size.height * 0.1f)
                }
                drawPath(
                    path = path,
                    color = PremiumGold,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(listOf(PremiumGold.copy(alpha = 0.2f), Color.Transparent))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("1 May", "11 May", "21 May", "31 May").forEach { date ->
                    Text(date, color = AdminSecondaryText, fontSize = 10.sp)
                }
            }
        }
    }
}
