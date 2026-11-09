package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AthleteProfile
import com.example.data.allChickenAccessoryGifts
import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.WorkoutLogEntity
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun MotivationAndGamificationHub(
    activeProfile: AthleteProfile,
    measurementLogs: List<BodyMeasurementEntity>,
    workoutLogs: List<WorkoutLogEntity>
) {
    // Compute real 28-day consistency map (4 weeks x 7 days) from actual workoutLogs & measurementLogs
    val dayActivityGrid = remember(workoutLogs, measurementLogs) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStartMs = cal.timeInMillis
        val dayMs = 24L * 60L * 60L * 1000L

        // For each of the last 28 days (index 0 = 27 days ago, index 27 = Today):
        // 0 = No logged activity, 1 = Biometric Check-in logged, 2 = Workout completed
        (27 downTo 0).map { daysAgo ->
            val windowStart = todayStartMs - daysAgo * dayMs
            val windowEnd = windowStart + dayMs
            val hasWorkout = workoutLogs.any { it.timestamp in windowStart until windowEnd }
            val hasCheckIn = measurementLogs.any { it.timestamp in windowStart until windowEnd }
            when {
                hasWorkout -> 2
                hasCheckIn -> 1
                else -> 0
            }
        }
    }

    val unlockedGiftsCount = remember(activeProfile) {
        allChickenAccessoryGifts.count { activeProfile.isAccessoryUnlocked(it) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with Real Level, XP, and Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFF6D00).copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalFireDepartment,
                            contentDescription = "Consistency & Milestones",
                            tint = Color(0xFFFF6D00)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONSISTENCY & MILESTONES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFF6D00),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Lvl ${activeProfile.level} • Stage ${activeProfile.evolutionStage.stageNumber}/12 • ${activeProfile.evolutionStage.title}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF6D00).copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "${activeProfile.streakDays}d • ${activeProfile.xp} XP",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF6D00),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Journey Progress toward Warrior Chicken (100% Goal)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Egg → Warrior Rooster: ${(activeProfile.goalJourneyProgress * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$unlockedGiftsCount/${allChickenAccessoryGifts.size} Gifts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { activeProfile.goalJourneyProgress.coerceIn(0.03f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFFFF6D00),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real 28-Day Activity Consistency Map (Zero fake days!)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "28-Day Activity Map",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${workoutLogs.size} Workouts • ${measurementLogs.size} Logs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        dayActivityGrid.chunked(7).forEachIndexed { weekIdx, weekDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (weekIdx == 3) "Now" else "W-${3 - weekIdx}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(34.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                weekDays.forEach { state ->
                                    val cellColor = when (state) {
                                        2 -> Color(0xFF2E7D32) // Completed workout
                                        1 -> MaterialTheme.colorScheme.primary // Logged Weight/Waist check-in
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellColor)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendDot(color = Color(0xFF2E7D32), label = "Workout Done")
                        LegendDot(color = MaterialTheme.colorScheme.primary, label = "Check-In Logged")
                        LegendDot(color = MaterialTheme.colorScheme.surfaceVariant, label = "No Log")
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false
        )
    }
}
