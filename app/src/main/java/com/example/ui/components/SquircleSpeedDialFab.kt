package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SquircleSpeedDialFab(
    onStartWorkout: () -> Unit,
    onLogWeightTape: () -> Unit,
    onPreWorkoutCheckIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    Box(modifier = modifier) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 96.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    SpeedDialPillItem(
                        label = "Biometric Check-in",
                        icon = Icons.Default.Straighten,
                        tag = "speed_dial_tape",
                        onClick = {
                            expanded = false
                            onPreWorkoutCheckIn()
                        }
                    )
                    SpeedDialPillItem(
                        label = "Log Weight & Waist",
                        icon = Icons.Default.MonitorWeight,
                        tag = "speed_dial_weight",
                        onClick = {
                            expanded = false
                            onLogWeightTape()
                        }
                    )
                    SpeedDialPillItem(
                        label = "Start Hands-Free Workout",
                        icon = Icons.Default.FitnessCenter,
                        tag = "speed_dial_workout",
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        onClick = {
                            expanded = false
                            onStartWorkout()
                        }
                    )
                }
            }

            LargeFloatingActionButton(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(22.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(58.dp)
                    .testTag("main_speed_dial_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "Close quick actions" else "Open quick actions",
                    modifier = Modifier
                        .size(26.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun SpeedDialPillItem(
    label: String,
    icon: ImageVector,
    tag: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 6.dp,
        shadowElevation = 4.dp,
        modifier = Modifier
            .testTag(tag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
