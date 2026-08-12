package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

// Google Font Provider
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val LoraFont = GoogleFont("Lora")
val RobotoFont = GoogleFont("Roboto")

// Lora Font Family with Serif as standard local fallback
val LoraFontFamily = FontFamily(
    Font(googleFont = LoraFont, fontProvider = provider),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Black)
)

// Roboto Font Family with Sans-Serif as standard local fallback
val RobotoFontFamily = FontFamily(
    Font(googleFont = RobotoFont, fontProvider = provider),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = RobotoFont, fontProvider = provider, weight = FontWeight.SemiBold)
)

// Set of Material typography styles using Lora for display/headers and Roboto for body text
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = LoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 1.2.sp
    )
)

