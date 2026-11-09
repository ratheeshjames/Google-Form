package com.example.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

sealed class Screen(
  val route: String,
  @StringRes val titleRes: Int,
  val icon: ImageVector,
  val activeIcon: ImageVector,
) {
  // Primary Bottom Navigation Destinations
  object Today : Screen(
    route = "today",
    titleRes = R.string.nav_today,
    icon = Icons.Outlined.FitnessCenter,
    activeIcon = Icons.Filled.FitnessCenter,
  )

  object Programme : Screen(
    route = "programme",
    titleRes = R.string.nav_programme,
    icon = Icons.AutoMirrored.Outlined.List,
    activeIcon = Icons.AutoMirrored.Filled.List,
  )

  object Transformation : Screen(
    route = "transformation",
    titleRes = R.string.nav_transformation,
    icon = Icons.Outlined.Straighten,
    activeIcon = Icons.Filled.Straighten,
  )

  object Analytics : Screen(
    route = "analytics",
    titleRes = R.string.nav_analytics,
    icon = Icons.Outlined.Analytics,
    activeIcon = Icons.Filled.Analytics,
  )

  object Coach : Screen(
    route = "coach",
    titleRes = R.string.nav_coach,
    icon = Icons.Outlined.AutoAwesome,
    activeIcon = Icons.Filled.AutoAwesome,
  )

  // Drawer-Only Secondary Destinations
  object Nutrition : Screen(
    route = "nutrition",
    titleRes = R.string.nav_nutrition,
    icon = Icons.Outlined.Restaurant,
    activeIcon = Icons.Filled.Restaurant,
  )

  object ExerciseLibrary : Screen(
    route = "exercises",
    titleRes = R.string.nav_exercises,
    icon = Icons.Outlined.Explore,
    activeIcon = Icons.Filled.Explore,
  )

  object Settings : Screen(
    route = "settings",
    titleRes = R.string.nav_settings,
    icon = Icons.Outlined.Settings,
    activeIcon = Icons.Filled.Settings,
  )

  companion object {
    val bottomNavItems: List<Screen>
      get() = listOf(
        Today,
        Programme,
        Nutrition,
        Transformation,
        Analytics,
      )
  }
}
