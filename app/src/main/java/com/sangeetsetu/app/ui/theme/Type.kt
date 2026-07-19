package com.sangeetsetu.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

/**
 * The [GoogleFont.Provider] configuration for fetching downloadable fonts.
 */
val provider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.sangeetsetu.app.R.array.com_google_android_gms_fonts_certs
)

val CinzelFont = GoogleFont("Cinzel")
val PoppinsFont = GoogleFont("Poppins")
val NotoSansDevanagariFont = GoogleFont("Noto Sans Devanagari")

val CinzelFamily = FontFamily(
    Font(googleFont = CinzelFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = CinzelFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val PoppinsFamily = FontFamily(
    Font(googleFont = PoppinsFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = PoppinsFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = PoppinsFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = PoppinsFont, fontProvider = provider, weight = FontWeight.Bold)
)

val NotoSansDevanagariFamily = FontFamily(
    Font(googleFont = NotoSansDevanagariFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = NotoSansDevanagariFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = NotoSansDevanagariFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSansDevanagariFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NotoSansDevanagariFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PoppinsFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
