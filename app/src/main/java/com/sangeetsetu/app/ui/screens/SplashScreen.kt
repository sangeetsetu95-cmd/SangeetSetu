package com.sangeetsetu.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sangeetsetu.app.R
import com.sangeetsetu.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAdminAccess: () -> Unit, onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.9f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(1500))
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(1500, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        // Subtle Golden Ambient Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PremiumGold.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Logo Glow
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .shadow(40.dp, CircleShape, spotColor = PremiumGold),
                    color = Color.Transparent,
                    shape = CircleShape
                ) {}
                
                Image(
                    painter = painterResource(id = R.drawable.sangeet_setu_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Sangeet Setu",
                color = PremiumGold,
                style = Typography.displayLarge,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Jode Kalakar, Banaye Yaadgar Pal",
                color = PremiumWhite.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontFamily = PoppinsFamily,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Hidden Admin Trigger
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(60.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAdminAccess() }
        )
        
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(28.dp),
            color = PremiumGold,
            strokeWidth = 2.dp
        )
    }
}
