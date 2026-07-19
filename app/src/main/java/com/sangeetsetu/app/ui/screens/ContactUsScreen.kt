package com.sangeetsetu.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.sangeetsetu.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val phone = "+91 9258585074"
    val email = "sangeetsetu95@gmail.com"

    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Contact Us",
                            color = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = PoppinsFamily
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PremiumGold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Help & Support",
                    color = PremiumWhite,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = PoppinsFamily
                )

                Text(
                    "हम आपकी सहायता के लिए यहाँ हैं।",
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontFamily = NotoSansDevanagariFamily,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Call Option
                ContactCard(
                    icon = Icons.Default.Call,
                    title = "Call",
                    subtitle = phone,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, "tel:$phone".toUri())
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "कॉल करने में समस्या हुई", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp Option
                ContactCard(
                    icon = Icons.AutoMirrored.Filled.Message,
                    title = "WhatsApp",
                    subtitle = phone,
                    isWhatsApp = true,
                    onClick = {
                        try {
                            val cleanPhone = phone.replace("+", "").replace(" ", "")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = "https://wa.me/$cleanPhone?text=नमस्ते संगेत सेतु टीम, मुझे सहायता चाहिए।".toUri()
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // If WhatsApp is not installed, open in browser
                            try {
                                val cleanPhone = phone.replace("+", "").replace(" ", "")
                                val webIntent = Intent(Intent.ACTION_VIEW, "https://api.whatsapp.com/send?phone=$cleanPhone".toUri())
                                context.startActivity(webIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "WhatsApp खोलने में समस्या हुई", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Option
                ContactCard(
                    icon = Icons.Default.Email,
                    title = "Email",
                    subtitle = email,
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:$email".toUri()
                                putExtra(Intent.EXTRA_SUBJECT, "Support Request - Sangeet Setu")
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email"))
                        } catch (_: Exception) {
                            Toast.makeText(context, "Email App नहीं मिली", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(60.dp))

                Text(
                    "Business Hours: 10:00 AM - 7:00 PM",
                    color = PremiumGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isWhatsApp: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (isWhatsApp) Color(0xFF25D366).copy(0.1f) else PremiumGold.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isWhatsApp) Color(0xFF25D366) else PremiumGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = title,
                    color = PremiumWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PoppinsFamily
                )
                Text(
                    text = subtitle,
                    color = SecondaryText,
                    fontSize = 14.sp,
                    fontFamily = PoppinsFamily
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                painter = painterResource(id = android.R.drawable.ic_media_play), // Small arrow
                contentDescription = null,
                tint = PremiumGray,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
