package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import com.example.ui.components.MotivationAndGamificationHub
import com.example.ui.components.WeightBmiAnalyticsSuite
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransformationScreen(
    onOpenQuickLogDialog: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { GoogleFormDatabase.getInstance(context) }
    val allMeasurements by db.measurementDao().getAllMeasurements().collectAsState(initial = emptyList())
    val recentWorkoutLogs by db.workoutDao().getRecentWorkoutLogs().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
    var saveFeedbackMessage by remember { mutableStateOf<String?>(null) }

    fun adjustBiometricAndPersist(
        newWeight: Float = activeProfile.weightKg,
        newWaist: Float = activeProfile.waistCm,
        newHeight: Float = activeProfile.heightCm,
        saveToHistoryLog: Boolean = false
    ) {
        val clampedW = newWeight.coerceIn(35f, 220f)
        val clampedWaist = newWaist.coerceIn(50f, 180f)
        val clampedH = newHeight.coerceIn(120f, 230f)

        AthleteProfileStore.updateActiveProfile(context) { current ->
            current.copy(
                weightKg = clampedW,
                waistCm = clampedWaist,
                heightCm = clampedH
            )
        }
        if (saveToHistoryLog) {
            val updated = AthleteProfileStore.activeProfile.value
            coroutineScope.launch {
                db.measurementDao().insertMeasurement(
                    BodyMeasurementEntity(
                        timestamp = System.currentTimeMillis(),
                        weightKg = updated.weightKg,
                        heightCm = updated.heightCm,
                        waistCm = updated.waistCm,
                        bmi = updated.bmi,
                        estimatedBodyFatPercent = updated.rfmBodyFatPct
                    )
                )
                AthleteProfileStore.recordBiometricCheckIn(
                    weightKg = updated.weightKg,
                    waistCm = updated.waistCm,
                    heightCm = updated.heightCm,
                    context = context
                )
                saveFeedbackMessage = "Saved ${String.format(Locale.US, "%.1f", updated.weightKg)} kg • Waist ${String.format(Locale.US, "%.1f", updated.waistCm)} cm (+35 XP)"
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("transformation_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 104.dp)
    ) {
        // 1. Header with Save Check-In Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "3-Biometric Transform",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Weight • Height • Waist Length → Auto Goal & RFM Engine",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = {
                        adjustBiometricAndPersist(saveToHistoryLog = true)
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("save_transformation_snapshot_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save Check-In",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Check-In", fontWeight = FontWeight.Bold)
                }
            }

            if (saveFeedbackMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF2E7D32).copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = saveFeedbackMessage!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Weight, Waist & BMI Analytics Suite (Real Room DB logs + 4-Week Goal Forecast)
        item {
            WeightBmiAnalyticsSuite(
                currentWeightKg = activeProfile.weightKg,
                targetWeightKg = activeProfile.autoGoalWeightKg,
                heightCm = activeProfile.heightCm,
                waistCm = activeProfile.waistCm,
                targetWaistCm = activeProfile.autoGoalWaistCm,
                bmi = activeProfile.bmi,
                rfmBodyFatPct = activeProfile.rfmBodyFatPct,
                leanMassKg = activeProfile.leanBodyMassKg,
                fatMassKg = activeProfile.fatMassKg,
                ffmi = activeProfile.ffmi,
                whtr = activeProfile.whtr,
                autoWeeksToGoal = activeProfile.autoWeeksToGoal,
                recordedMeasurements = allMeasurements,
                onAdjustWeight = { delta ->
                    adjustBiometricAndPersist(newWeight = activeProfile.weightKg + delta)
                },
                onAdjustWaist = { delta ->
                    adjustBiometricAndPersist(newWaist = activeProfile.waistCm + delta)
                },
                onAdjustHeight = { delta ->
                    adjustBiometricAndPersist(newHeight = activeProfile.heightCm + delta)
                }
            )
        }

        // 3. Automatic Goal & Nutrition Calibrator Card (Derived strictly from Weight, Height & Waist)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoGraph,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Automatic Recomposition Blueprint",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Calculated automatically from your Height (${activeProfile.heightCm.toInt()} cm), Weight (${String.format(Locale.US, "%.1f", activeProfile.weightKg)} kg) & Waist (${String.format(Locale.US, "%.1f", activeProfile.waistCm)} cm)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target: ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWeightKg)} kg • ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWaistCm)} cm Waist",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "~${activeProfile.autoWeeksToGoal} Weeks",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                text = "Daily Nutrition Target: ${activeProfile.autoDailyCaloriesKcal} kcal • ${activeProfile.autoDailyProteinG}g Protein • ${(activeProfile.weightKg * 0.042f).let { String.format(Locale.US, "%.1f", it) }}L Water",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Real Consistency & Milestones Hub
        item {
            MotivationAndGamificationHub(
                activeProfile = activeProfile,
                measurementLogs = allMeasurements,
                workoutLogs = recentWorkoutLogs
            )
        }

        // 5. Real Recorded 3-Biometric Check-In Log History (Room SQLite)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recorded Check-Ins (${allMeasurements.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = onOpenQuickLogDialog,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "New Check-In",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (allMeasurements.isEmpty()) {
                        Text(
                            text = "No biometric logs yet. Tap 'Log Check-In' above to record your Weight, Height, and Waist Length.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allMeasurements.take(6).forEachIndexed { idx, entry ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (idx == 0) "Latest Check-In" else dateFormat.format(Date(entry.timestamp)),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Height ${entry.heightCm.toInt()} cm • BMI ${String.format(Locale.US, "%.1f", entry.bmi)} • RFM ${String.format(Locale.US, "%.1f%%", entry.estimatedBodyFatPercent)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", entry.weightKg)} kg • ${String.format(Locale.US, "%.1f", entry.waistCm)} cm",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
