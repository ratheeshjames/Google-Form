package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Material 3 Expressive Heavily Rounded Shape Configurations (2026 Standard)
val Shapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp), // Squircle standard for cards & primary FABs
  large = RoundedCornerShape(24.dp), // Large container cards
  extraLarge = RoundedCornerShape(32.dp), // Hero containers & sheets
)

// Specific tokens for Google Workspace Expressive UI
val SquircleFabShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(999.dp)
val WorkspaceCardShape = RoundedCornerShape(24.dp)
val WorkspaceSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val WorkspaceSubActionShape = RoundedCornerShape(20.dp)
