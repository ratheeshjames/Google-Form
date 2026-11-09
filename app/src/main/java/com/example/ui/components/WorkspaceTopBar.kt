package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AthleteProfileStore
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.PillShape

@Composable
fun WorkspaceTopBar(
  onMenuClick: () -> Unit,
  onProfileClick: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
  onSearchClick: () -> Unit = onProfileClick,
) {
  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Surface(
      shape = PillShape,
      color = MaterialTheme.colorScheme.surfaceContainerHigh,
      tonalElevation = 3.dp,
      shadowElevation = 2.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .testTag("workspace_search_bar"),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Leading Hamburger Icon
        IconButton(
          onClick = onMenuClick,
          modifier = Modifier
            .size(44.dp)
            .testTag("drawer_menu_button"),
        ) {
          Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Open navigation drawer",
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Center Search / Profile Biometrics Prompt
        Row(
          modifier = Modifier
            .weight(1f)
            .clickable(onClick = onSearchClick)
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = query.ifEmpty { stringResource(R.string.search_placeholder) },
            style = MaterialTheme.typography.bodyLarge,
            color = if (query.isEmpty()) {
              MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
            } else {
              MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Trailing Athlete Profile Avatar with Active Status Dot
        Box(
          modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onProfileClick)
            .testTag("account_avatar_button"),
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = activeProfile.initials,
              color = MaterialTheme.colorScheme.onPrimary,
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
              ),
              maxLines = 1,
              softWrap = false,
            )
          }

          // Active Status Dot at bottom-end
          Box(
            modifier = Modifier
              .size(10.dp)
              .align(Alignment.BottomEnd)
              .clip(CircleShape)
              .background(GoogleGreen40)
              .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape),
          )
        }
      }
    }
  }
}
