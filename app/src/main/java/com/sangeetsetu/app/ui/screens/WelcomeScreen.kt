package com.sangeetsetu.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sangeetsetu.app.ui.theme.*
import com.sangeetsetu.app.ui.components.PremiumButton

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        SangeetSetuDesign.PremiumBackgroundDecoration()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Hero Image with Premium Styling
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(32.dp)),
                color = CardBackground,
                shadowElevation = 12.dp
            ) {
                Box {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1514525253361-bee8718a74a2?q=80&w=800",
                        contentDescription = "Welcome",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    )
                    
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp),
                        color = PremiumGold,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "TRUSTED BY 10K+ ORGANIZERS",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "Experience the Magic of Live Music",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFamily,
                    lineHeight = 40.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Book world-class artists and traditional performers for your next grand celebration.",
                    color = PremiumGray,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = PoppinsFamily,
                    lineHeight = 24.sp
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                PremiumButton(
                    text = "Explore Artists",
                    onClick = onGetStarted
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onGetStarted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "Are you an Artist? Join Us", 
                        color = PremiumGold, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold,
                        fontFamily = PoppinsFamily
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
