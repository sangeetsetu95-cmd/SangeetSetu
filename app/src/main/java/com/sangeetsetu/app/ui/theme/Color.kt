package com.sangeetsetu.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

/**
 * Sangeet Setu - Ultra Premium Design System
 * Reference: #0B0F1A Background, #D4AF37 Gold
 */

val AdminBackground = Color(0xFF0B0F1A)    // Matte Black
val AdminSecondaryBackground = Color(0xFF121826)
val AdminCardBackground = Color(0xFF1A2235)
val AdminSecondaryText = Color(0xFFB8C1CC)

val AppBackground = AdminBackground     // Changed to Matte Black for Premium feel
val CardBackground = AdminCardBackground  // Changed to Dark for Premium feel
val PremiumGold = Color(0xFFD4AF37)      // Royal Gold
val AccentGold = Color(0xFFF4D35E)       // Light Gold
val PremiumWhite = Color(0xFFFFFFFF)
val PrimaryText = Color(0xFFFFFFFF)      // Primary Text (White for Dark Theme)
val SecondaryText = AdminSecondaryText    // Secondary Text
val GoldAccent = Color(0xFFD4AF37)       // Gold Accent
val BorderColor = Color(0xFFFFFFFF).copy(alpha = 0.1f) // Subtle White Border
val GoldenGlow = Color(0xFFD4AF37).copy(alpha = 0.3f)
val DeepNavy = Color(0xFF0B0F1A)

// Gold Variants
val PremiumGoldDark = Color(0xFFB68D2A)
val PremiumGoldLight = Color(0xFFF4D35E)

// Neon Accents for Categories
val NeonPurple = Color(0xFFA855F7)
val NeonBlue = Color(0xFF3B82F6)
val NeonOrange = Color(0xFFF97316)
val NeonPink = Color(0xFFEC4899)
val NeonCyan = Color(0xFF06B6D4)
val NeonGreen = Color(0xFF10B981)

// Luxury Gradients
val GoldenGradient = Brush.verticalGradient(listOf(PremiumGold, PremiumGoldDark))
val VerifiedBlueGradient = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
val PurpleGoldGradient = Brush.linearGradient(listOf(Color(0xFF6B21A8), Color(0xFFD4AF37)))
val BlueNeonGradient = Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
val OrangeNeonGradient = Brush.linearGradient(listOf(Color(0xFF9A3412), Color(0xFFF97316)))

// Status Colors
val SuccessColor = Color(0xFF22C55E)
val WarningColor = Color(0xFFF59E0B)
val ErrorColor = Color(0xFFEF4444)
val VerifiedBlue = Color(0xFF2196F3)

// Legacy / Support for existing screens
val PremiumNavy = AppBackground
val PremiumNavyDark = Color(0xFF0B0F1A)
val PremiumNavyLight = Color(0xFF121826)
val PremiumNavyDeep = Color(0xFF04070D)
val PremiumGray = SecondaryText
val CardBorder = BorderColor
val SurfaceVariant = Color(0xFF121826)
