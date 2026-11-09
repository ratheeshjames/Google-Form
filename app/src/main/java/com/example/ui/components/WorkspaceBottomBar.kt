package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.navigation.Screen

@Composable
fun WorkspaceBottomBar(
  currentRoute: String,
  onNavigateToScreen: (Screen) -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationBar(
    modifier = modifier.testTag("workspace_bottom_bar"),
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation = 2.dp,
    windowInsets = WindowInsets.navigationBars,
  ) {
    Screen.bottomNavItems.filterNotNull().forEach { screen ->
      val selected = currentRoute == screen.route

      NavigationBarItem(
        selected = selected,
        onClick = { onNavigateToScreen(screen) },
        icon = {
          Icon(
            imageVector = if (selected) screen.activeIcon else screen.icon,
            contentDescription = stringResource(screen.titleRes),
          )
        },
        label = {
          Text(
            text = stringResource(screen.titleRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
          )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          selectedTextColor = MaterialTheme.colorScheme.onSurface,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
          indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        modifier = Modifier.testTag("nav_item_${screen.route}"),
      )
    }
  }
}
