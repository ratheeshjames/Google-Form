package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.TransformationExporter
import com.example.model.CardioRecoveryProtocolRepository
import com.example.model.WorkoutProtocolRepository
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.PillShape
import kotlin.math.roundToInt
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape
import java.util.Locale
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun ExerciseLibraryScreen(
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  val muscleGroups = listOf(
    "Chest (Pectoralis Major & Minor)" to "Incline Bench Press, Bench Press, Incline Bench Fly, Bench Fly, Decline Press",
    "Back (Latissimus Dorsi & Rhomboids)" to "Dumbbell Pullover, Single-Arm Bench Lat Row, Bent-Over Dumbbell Row, Wide Row",
    "Triceps (Long, Lateral & Medial Heads)" to "Lying French Press, French Press, Triceps Kickback",
    "Biceps & Forearms (Brachii & Brachialis)" to "Bicep Curl, Hammer Curl, Alternate Bicep Curl, Alternate Hammer Curl",
    "Shoulders (Deltoids & Trapezius)" to "Overhead Press, Arnold Press, Lateral Raises, Front Raises, Reverse Fly, Shrugs",
    "Legs & Cardio (Quads, Hamstrings & Treadmill)" to "Squats, Lunges, Deadlift, Calf Raises, Sunday Treadmill HIIT & Incline Walk",
    "Core (Rectus Abdominis & Obliques)" to "Bench Lying Leg Raises, Dumbbell V-Up & Leg Tuck, Lying Leg Raises, Crunches, Bicycle Crunches",
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("exercise_library_screen"),
    contentPadding = PaddingValues(
      top = contentPadding.calculateTopPadding() + 8.dp,
      bottom = contentPadding.calculateBottomPadding() + 96.dp,
      start = 16.dp,
      end = 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = "Anatomical Muscle Map",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Explore Target Muscle Groups & Movement Directory",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    item {
      com.example.ui.components.MuscleMapComposable()
    }

    items(muscleGroups.size) { index ->
      val (muscle, exercises) = muscleGroups[index]
      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(SquircleFabShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp),
              )
            }

            Text(
              text = muscle,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = exercises,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun SettingsScreen(
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
  var voiceGuidanceEnabled by remember { mutableStateOf(true) }
  var selectedLanguage by remember { mutableStateOf("English (US)") }
  var useMetric by remember { mutableStateOf(true) }

  var showExportModal by remember { mutableStateOf(false) }
  var exportedCsvContent by remember { mutableStateOf("") }
  var exportedJsonContent by remember { mutableStateOf("") }
  var selectedExportFormat by remember { mutableStateOf("CSV") }

  fun prepareAndShowExport(format: String) {
    scope.launch {
      val db = GoogleFormDatabase.getInstance(context)
      val workouts = db.workoutDao().getRecentWorkoutLogs().firstOrNull() ?: emptyList()
      val measurements = db.measurementDao().getAllMeasurements().firstOrNull() ?: emptyList()

      val workoutsCsv = TransformationExporter.exportWorkoutsToCsv(workouts)
      val measurementsCsv = TransformationExporter.exportMeasurementsToCsv(measurements)
      val profileHeader = """
        # Google Form v2 • Athlete Transformation Backup
        # Athlete: ${activeProfile.name} | Stage: ${activeProfile.evolutionStage.title} | XP: ${activeProfile.xp} | Streak: ${activeProfile.streakDays}d
        # Current 3-Biometrics: Height ${activeProfile.heightCm} cm | Weight ${activeProfile.weightKg} kg | Waist ${activeProfile.waistCm} cm | RFM ${activeProfile.rfmBodyFatPct}%
      """.trimIndent()

      exportedCsvContent = "$profileHeader\n\n--- WORKOUT SESSIONS ---\n$workoutsCsv\n--- 3-BIOMETRIC CHECK-INS ---\n$measurementsCsv"
      exportedJsonContent = TransformationExporter.exportToJson(
        workouts = workouts,
        sets = emptyList(),
        measurements = measurements,
      )
      selectedExportFormat = format
      showExportModal = true
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen"),
    contentPadding = PaddingValues(
      top = contentPadding.calculateTopPadding() + 8.dp,
      bottom = contentPadding.calculateBottomPadding() + 96.dp,
      start = 16.dp,
      end = 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = "Settings & 2D Stage Preview",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "12-Stage 2D Rooster & Dojo Previewer • Duration Budget • CSV/JSON Backup",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Exclusive Settings-Only 12-Stage 2D Chicken Evolution Previewer
    item {
      com.example.ui.components.SettingsChickenStagePreviewCard(
        activeProfile = activeProfile,
      )
    }

    // 1. Dumbbell Strength Duration Slider (15-90m, Default 30m) & Difficulty Calibration (Lv.1-5, Default Lv.2)
    item {
      val autoDay = remember { AthleteProfileStore.getAutoDetectedWorkoutDay().coerceIn(1, 6) }
      val calibratedPreviewPlan = remember(
        autoDay,
        activeProfile.sessionDurationMin,
        activeProfile.workoutDifficultyLevel,
        activeProfile.dumbbellWeightKg,
        activeProfile.exerciseWeightOverrides,
      ) {
        WorkoutProtocolRepository.getScaledWorkoutPlan(
          day = autoDay,
          targetDurationMinutes = activeProfile.sessionDurationMin,
          customDumbbellWeightKg = activeProfile.dumbbellWeightKg,
          exerciseWeightOverrides = activeProfile.exerciseWeightOverrides,
          difficultyLevel = activeProfile.workoutDifficultyLevel,
        )
      }
      val avgRepsPerSet = remember(calibratedPreviewPlan) {
        calibratedPreviewPlan.exercises
          .filter { !it.isBodyweight || it.reps < 30 }
          .map { it.reps }
          .average()
          .takeIf { !it.isNaN() }
          ?.roundToInt() ?: 12
      }

      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_duration_difficulty_card"),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
              text = "Strength Duration & Difficulty (Days 1–6)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Calibrate your Dumbbell Strength duration (15–90 min, default 30m) and difficulty level (Lv.1–Lv.5, default Lv.2). Sets, reps, and work/rest intervals dynamically scale to finish 100% at 00:00.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(modifier = Modifier.height(14.dp))

          // 1. Session Duration Slider (15m to 90m, Default 30m)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "1. Strength Session Duration",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.weight(1f),
            )
            Surface(
              shape = PillShape,
              color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
              Text(
                text = if (activeProfile.sessionDurationMin == 30) "30m (Default)" else "${activeProfile.sessionDurationMin} Minutes",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Slider(
            value = activeProfile.sessionDurationMin.toFloat(),
            onValueChange = { rawValue ->
              val snappedMins = ((rawValue / 5f).roundToInt() * 5).coerceIn(15, 90)
              if (snappedMins != activeProfile.sessionDurationMin) {
                AthleteProfileStore.updateActiveProfile(context) {
                  it.copy(sessionDurationMin = snappedMins)
                }
              }
            },
            valueRange = 15f..90f,
            steps = 14,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_duration_slider"),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            listOf(15, 30, 45, 60, 75, 90).forEach { mins ->
              val isSelected = activeProfile.sessionDurationMin == mins
              Text(
                text = if (mins == 30) "30m★" else "${mins}m",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // 2. Difficulty Slider Header (Lv.1 to Lv.5, Default Lv.2)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "2. Strength Difficulty Level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }

            Surface(
              shape = PillShape,
              color = MaterialTheme.colorScheme.primaryContainer,
            ) {
              Text(
                text = activeProfile.difficultyTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = activeProfile.difficultySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          Spacer(modifier = Modifier.height(6.dp))

          Slider(
            value = activeProfile.workoutDifficultyLevel.toFloat(),
            onValueChange = { rawValue ->
              val newLevel = rawValue.roundToInt().coerceIn(1, 5)
              if (newLevel != activeProfile.workoutDifficultyLevel) {
                AthleteProfileStore.updateActiveProfile(context) {
                  it.copy(workoutDifficultyLevel = newLevel)
                }
              }
            },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_difficulty_slider"),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            listOf(
              1 to "Lv.1 Light",
              2 to "Lv.2 Base",
              3 to "Lv.3 Push",
              4 to "Lv.4 Hard",
              5 to "Lv.5 Titan",
            ).forEach { (lvl, shortText) ->
              val isSelected = activeProfile.workoutDifficultyLevel == lvl
              Text(
                text = shortText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Live Constrained Output Summary Box
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "Calibrated Strength Plan (${AthleteProfileStore.getWeekdayLabel(autoDay)})",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.weight(1f),
                )
                Text(
                  text = "Exact ${activeProfile.sessionDurationMin}:00 Fit",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoogleGreen40,
                  maxLines = 1,
                  softWrap = false,
                )
              }

              Text(
                text = "${calibratedPreviewPlan.totalScaledSets} Total Sets • ~$avgRepsPerSet Reps/Set • ${calibratedPreviewPlan.workSecondsPerSet}s Work / ${calibratedPreviewPlan.restSecondsPerSet}s Rest (${calibratedPreviewPlan.prepSecondsPerSet}s Prep)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }

    // 2. Decoupled Sunday Cardio, Treadmill & Active Recovery Calibration Card
    item {
      val cardioPreview = remember(
        activeProfile.cardioDurationMin,
        activeProfile.cardioDifficultyLevel,
        activeProfile.weightKg,
      ) {
        CardioRecoveryProtocolRepository.buildCalibratedPlan(
          durationMinutes = activeProfile.cardioDurationMin,
          difficultyLevel = activeProfile.cardioDifficultyLevel,
          athleteWeightKg = activeProfile.weightKg,
        )
      }

      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = androidx.compose.ui.graphics.Color(0xFF0A2128),
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("settings_cardio_recovery_card"),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "Sunday Cardio, Treadmill & Recovery",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = androidx.compose.ui.graphics.Color.White,
              modifier = Modifier.weight(1f),
            )
            Surface(
              shape = PillShape,
              color = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.22f),
            ) {
              Text(
                text = "Decoupled Engine",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = androidx.compose.ui.graphics.Color(0xFF10B981),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Independent duration & intensity calibration for Sunday's 4-Pillar Zone-2 Treadmill, Myofascial Mobility, and Parasympathetic CNS Recovery session.",
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f),
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Cardio + Recovery Duration",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = androidx.compose.ui.graphics.Color.White,
            )
            Text(
              text = "${activeProfile.cardioDurationMin} Minutes",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = androidx.compose.ui.graphics.Color(0xFF00E5FF),
            )
          }

          Slider(
            value = activeProfile.cardioDurationMin.toFloat(),
            onValueChange = { raw ->
              val snapped = ((raw / 5f).roundToInt() * 5).coerceIn(15, 90)
              if (snapped != activeProfile.cardioDurationMin) {
                AthleteProfileStore.updateActiveProfile(context) {
                  it.copy(cardioDurationMin = snapped)
                }
              }
            },
            valueRange = 15f..90f,
            steps = 14,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_cardio_duration_slider"),
          )

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Cardio & Recovery Intensity",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = androidx.compose.ui.graphics.Color.White,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = activeProfile.cardioDifficultyTitle,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = androidx.compose.ui.graphics.Color(0xFF10B981),
              maxLines = 1,
              softWrap = false,
            )
          }

          Slider(
            value = activeProfile.cardioDifficultyLevel.toFloat(),
            onValueChange = { raw ->
              val lvl = raw.roundToInt().coerceIn(1, 5)
              if (lvl != activeProfile.cardioDifficultyLevel) {
                AthleteProfileStore.updateActiveProfile(context) {
                  it.copy(cardioDifficultyLevel = lvl)
                }
              }
            },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("settings_cardio_difficulty_slider"),
          )

          Text(
            text = activeProfile.cardioDifficultySubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.78f),
          )

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = androidx.compose.ui.graphics.Color(0xFF102D36),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "Split: ${cardioPreview.treadmillMinutes}m Treadmill (Peak ${cardioPreview.peakInclinePercent}% @ ${cardioPreview.peakSpeedKmh} km/h) + ${cardioPreview.mobilityAndRecoveryMinutes}m Mobility & CNS Reset • ~${cardioPreview.estimatedCaloriesBurned} kcal • +${cardioPreview.recoveryReadinessBoostPct}% Readiness",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = androidx.compose.ui.graphics.Color(0xFF00E5FF),
              modifier = Modifier.padding(12.dp),
            )
          }
        }
      }
    }

    // Voice Coaching Options
    item {
      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(
                text = "Voice Guidance Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }

            Switch(
              checked = voiceGuidanceEnabled,
              onCheckedChange = { voiceGuidanceEnabled = it },
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "Speaks cadence, breathing cues, and 3-2-1 audio countdowns during sessions.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          if (voiceGuidanceEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(
                selected = selectedLanguage == "English (US)",
                onClick = { selectedLanguage = "English (US)" },
                label = { Text("English (US)") },
                shape = PillShape,
              )
              FilterChip(
                selected = selectedLanguage == "Malayalam (IN)",
                onClick = { selectedLanguage = "Malayalam (IN)" },
                label = { Text("Malayalam (IN)") },
                shape = PillShape,
              )
            }
          }
        }
      }
    }

    // Units & Real CSV/JSON Database Export
    item {
      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "Unit System",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              FilterChip(
                selected = useMetric,
                onClick = { useMetric = true },
                label = { Text("kg / cm") },
                shape = PillShape,
              )
              FilterChip(
                selected = !useMetric,
                onClick = { useMetric = false },
                label = { Text("lb / in") },
                shape = PillShape,
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Button(
              onClick = { prepareAndShowExport("CSV") },
              shape = PillShape,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("export_csv_button"),
            ) {
              Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text(text = "Export CSV", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
              onClick = { prepareAndShowExport("JSON") },
              shape = PillShape,
              modifier = Modifier
                .weight(1f)
                .testTag("export_json_button"),
            ) {
              Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.size(6.dp))
              Text(text = "Export JSON", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }

  if (showExportModal) {
    val activeContent = if (selectedExportFormat == "CSV") exportedCsvContent else exportedJsonContent
    AlertDialog(
      onDismissRequest = { showExportModal = false },
      title = {
        Text(
          text = "Room Database Export ($selectedExportFormat)",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedExportFormat == "CSV",
              onClick = { selectedExportFormat = "CSV" },
              label = { Text("CSV Report") },
              shape = PillShape,
            )
            FilterChip(
              selected = selectedExportFormat == "JSON",
              onClick = { selectedExportFormat = "JSON" },
              label = { Text("JSON Backup") },
              shape = PillShape,
            )
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
              .fillMaxWidth()
              .height(220.dp),
          ) {
            Column(
              modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            ) {
              Text(
                text = activeContent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
              type = if (selectedExportFormat == "CSV") "text/csv" else "application/json"
              putExtra(Intent.EXTRA_SUBJECT, "Google Form v2 Transformation Backup ($selectedExportFormat)")
              putExtra(Intent.EXTRA_TEXT, activeContent)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share $selectedExportFormat Export"))
          },
          shape = PillShape,
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.size(6.dp))
          Text("Share File")
        }
      },
      dismissButton = {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          TextButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
              clipboard?.setPrimaryClip(ClipData.newPlainText("GoogleForm_$selectedExportFormat", activeContent))
              Toast.makeText(context, "$selectedExportFormat copied to clipboard", Toast.LENGTH_SHORT).show()
            },
          ) {
            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.size(4.dp))
            Text("Copy")
          }
          TextButton(onClick = { showExportModal = false }) {
            Text("Close")
          }
        }
      },
    )
  }
}
