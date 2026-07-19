package com.sangeetsetu.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

import androidx.compose.ui.graphics.Brush

/**
 * Sangeet Setu - Global Design System Guidelines
 */
object SangeetSetuDesign {

    // Rounded Corners Policy
    val CardCorner = 24.dp
    val ButtonCorner = 20.dp
    val ImageCorner = 24.dp
    
    val Shapes = androidx.compose.material3.Shapes(
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(CardCorner),
        large = RoundedCornerShape(32.dp)
    )

    // Spacing Policy
    val HorizontalPadding = 20.dp
    val SectionGap = 28.dp

    // Elevation Policy
    val CardElevation = 8.dp
    
    /**
     * Standardized Premium Button Colors
     */
    @Composable
    fun primaryButtonColors() = ButtonDefaults.buttonColors(
        containerColor = PremiumGold,
        contentColor = Color.Black
    )

    @Composable
    fun primaryButtonGradient() = Brush.linearGradient(
        colors = listOf(PremiumGold, Color(0xFFB68D2A))
    )

    fun Modifier.shimmerLoadingAnimation(): Modifier = composed {
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )

        val shimmerColors = listOf(
            CardBackground.copy(alpha = 0.9f),
            CardBackground.copy(alpha = 0.4f),
            CardBackground.copy(alpha = 0.9f),
        )

        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnim, y = translateAnim)
        )

        return@composed this.background(brush)
    }

    @Composable
    fun PremiumBackgroundDecoration() {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(AppBackground, Color(0xFF040D1A))
                )
            )

            // Curved Golden Lines with reference aesthetic
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.15f)
                quadraticTo(size.width * 0.5f, size.height * 0.05f, size.width, size.height * 0.25f)
            }
            drawPath(
                path = path1,
                brush = Brush.horizontalGradient(listOf(PremiumGold.copy(alpha = 0.0f), PremiumGold.copy(alpha = 0.15f), PremiumGold.copy(alpha = 0.0f))),
                style = Stroke(width = 2f)
            )

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.85f)
                quadraticTo(size.width * 0.5f, size.height * 0.95f, size.width, size.height * 0.75f)
            }
            drawPath(
                path = path2,
                brush = Brush.horizontalGradient(listOf(PremiumGold.copy(alpha = 0.0f), PremiumGold.copy(alpha = 0.15f), PremiumGold.copy(alpha = 0.0f))),
                style = Stroke(width = 2f)
            )
            
            // Decorative Dots
            val dotColor = PremiumGold.copy(alpha = 0.08f)
            val spacing = 45f
            for (x in 0 until (size.width / spacing).toInt()) {
                for (y in 0 until (size.height / spacing).toInt()) {
                    if ((x + y) % 7 == 0) {
                        drawCircle(
                            color = dotColor,
                            radius = 1.2f,
                            center = Offset(x * spacing, y * spacing)
                        )
                    }
                }
            }
        }
    }
}
