package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Google Sans & Google Sans Text Font Families (using system Sans-Serif font engine)
val GoogleSansFamily = FontFamily.SansSerif
val GoogleSansTextFamily = FontFamily.SansSerif

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  displaySmall = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = GoogleSansFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = GoogleSansTextFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)
