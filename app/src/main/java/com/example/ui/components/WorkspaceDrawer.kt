package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfileStore
import com.example.navigation.Screen
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.GoogleRed40
import com.example.ui.theme.GoogleYellow40
import java.util.Locale

@Composable
fun WorkspaceDrawer(
    currentRoute: String,
    onNavigateToScreen: (Screen) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

    val primaryItems = listOf(
        Screen.Today,
        Screen.Programme,
        Screen.Transformation,
        Screen.Analytics
    )

    val secondaryItems = listOf(
        Screen.Nutrition,
        Screen.ExerciseLibrary,
        Screen.Settings
    )

    ModalDrawerSheet(
        modifier = Modifier
            .width(316.dp)
            .testTag("workspace_drawer"),
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Google Form Brand Wordmark Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = GoogleBlue40, fontWeight = FontWeight.Bold)) { append("G") }
                        withStyle(SpanStyle(color = GoogleRed40, fontWeight = FontWeight.Bold)) { append("o") }
                        withStyle(SpanStyle(color = GoogleYellow40, fontWeight = FontWeight.Bold)) { append("o") }
                        withStyle(SpanStyle(color = GoogleBlue40, fontWeight = FontWeight.Bold)) { append("g") }
                        withStyle(SpanStyle(color = GoogleGreen40, fontWeight = FontWeight.Bold)) { append("l") }
                        withStyle(SpanStyle(color = GoogleRed40, fontWeight = FontWeight.Bold)) { append("e") }
                        withStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        ) {
                            append(" Form")
                        }
                    },
                    fontSize = 22.sp
                )
            }

            // Dynamic Athlete Summary Pill inside Drawer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeProfile.initials,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = activeProfile.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${activeProfile.heightCm.toInt()} cm • ${String.format(Locale.US, "%.1f", activeProfile.weightKg)} kg • ${activeProfile.evolutionStage.badge}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TRAINING HUB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            primaryItems.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationDrawerItem(
                    label = { Text(stringResource(screen.titleRes), fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selected) screen.activeIcon else screen.icon,
                            contentDescription = stringResource(screen.titleRes)
                        )
                    },
                    selected = selected,
                    onClick = {
                        onNavigateToScreen(screen)
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_item_${screen.route}")
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )

            Text(
                text = "RESOURCES & 3D PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            secondaryItems.forEach { screen ->
                val selected = currentRoute == screen.route
                val labelText = if (screen == Screen.Settings) {
                    "Settings & 3D Stage Preview"
                } else {
                    stringResource(screen.titleRes)
                }
                NavigationDrawerItem(
                    label = { Text(labelText, fontWeight = FontWeight.Medium) },
                    icon = {
                        Icon(
                            imageVector = if (selected) screen.activeIcon else screen.icon,
                            contentDescription = labelText
                        )
                    },
                    selected = selected,
                    onClick = {
                        onNavigateToScreen(screen)
                        onCloseDrawer()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .testTag("drawer_item_${screen.route}")
                )
            }
        }
    }
}
