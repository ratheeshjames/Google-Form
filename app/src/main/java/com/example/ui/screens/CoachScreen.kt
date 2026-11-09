package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.AthleteProfileStore
import com.example.ui.components.DashboardChickenCompanionCard

/**
 * Legacy CoachScreen wrapper delegated to DashboardChickenCompanionCard.
 * Note: The separate Avatar RPG tab has been removed and integrated directly into TodayScreen.
 */
@Composable
fun CoachScreen(
    onOpenBiometricsClick: () -> Unit = {}
) {
    val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DashboardChickenCompanionCard(
                activeProfile = activeProfile,
                onOpenBiometricsClick = onOpenBiometricsClick
            )
        }
    }
}
