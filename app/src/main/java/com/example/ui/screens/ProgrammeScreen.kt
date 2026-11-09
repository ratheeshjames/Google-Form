package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfileStore
import com.example.model.WorkoutProtocolRepository
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.PillShape
import com.example.ui.theme.WorkspaceCardShape
import java.util.Locale

@Composable
fun ProgrammeScreen(
  onStartSpecificWorkout: (Int) -> Unit = {},
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  var selectedDayIndex by remember { mutableIntStateOf(0) }
  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
  val workouts = WorkoutProtocolRepository.allWorkouts
  val selectedDayProtocol = workouts[selectedDayIndex]

  val scaledPlan = remember(
    selectedDayProtocol.day,
    activeProfile.sessionDurationMinutes,
    activeProfile.dumbbellWeightKg,
    activeProfile.exerciseWeightOverrides,
    activeProfile.workoutDifficultyLevel,
  ) {
    WorkoutProtocolRepository.getScaledWorkoutPlan(
      day = selectedDayProtocol.day,
      targetDurationMinutes = activeProfile.sessionDurationMinutes,
      customDumbbellWeightKg = activeProfile.dumbbellWeightKg,
      exerciseWeightOverrides = activeProfile.exerciseWeightOverrides,
      difficultyLevel = activeProfile.workoutDifficultyLevel,
    )
  }

  val estimatedTonnageKg = remember(scaledPlan) {
    scaledPlan.exercises.sumOf { ex ->
      val wt = ex.weightKg ?: 0.0
      (ex.sets * ex.reps * wt).toInt()
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("programme_screen"),
    contentPadding = PaddingValues(
      top = contentPadding.calculateTopPadding() + 8.dp,
      bottom = contentPadding.calculateBottomPadding() + 110.dp,
      start = 16.dp,
      end = 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Text(
        text = WorkoutProtocolRepository.programName,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "6-Day Dumbbell Split + Sunday Cardio • ${activeProfile.sessionDurationMinutes}m • ${activeProfile.difficultyTitle}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Day selection tabs / chips (Days 1..6 Strength + Sunday Cardio & Recovery Studio)
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        itemsIndexed(workouts) { index, day ->
          FilterChip(
            selected = selectedDayIndex == index,
            onClick = { selectedDayIndex = index },
            label = {
              Text(
                text = if (day.isCardioSpecial) "Sun • Cardio & Recovery" else "Day ${day.day}",
                fontWeight = if (selectedDayIndex == index) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                softWrap = false,
              )
            },
            shape = PillShape,
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = if (day.isCardioSpecial) Color(0xFF0A2128) else MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = if (day.isCardioSpecial) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          )
        }
      }
    }

    if (selectedDayProtocol.isCardioSpecial) {
      // Completely Decoupled Sunday Cardio, Treadmill & Active Recovery Studio UI
      item {
        DecoupledCardioRecoveryStudioHub(
          activeProfile = activeProfile,
          onLaunchCardioConsole = { onStartSpecificWorkout(7) },
        )
      }
    } else {
      // Active Strength Day Summary & Time-Scaled Metrics Card (Days 1..6)
      item {
        Card(
          shape = WorkspaceCardShape,
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "Day ${selectedDayProtocol.day}: ${selectedDayProtocol.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
              )
              Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
              ) {
                Text(
                  text = "${scaledPlan.exercises.size} Ex • ${scaledPlan.totalScaledSets} Sets",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              DayMetricBadge(
                label = "Scaled Sets",
                value = "${scaledPlan.totalScaledSets} Sets",
                modifier = Modifier.weight(1f),
              )
              DayMetricBadge(
                label = "Volume",
                value = "$estimatedTonnageKg kg",
                modifier = Modifier.weight(1f),
              )
              DayMetricBadge(
                label = "Set Timing",
                value = "${scaledPlan.workSecondsPerSet}s / ${scaledPlan.restSecondsPerSet}s",
                modifier = Modifier.weight(1f),
              )
            }

            Button(
              onClick = { onStartSpecificWorkout(selectedDayProtocol.day) },
              shape = PillShape,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
              )
              Spacer(modifier = Modifier.size(6.dp))
              Text(
                text = "Start Day ${selectedDayProtocol.day} Workout (Exact ${scaledPlan.targetDurationMinutes}m Fit)",
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
              )
            }
          }
        }
      }

      itemsIndexed(scaledPlan.exercises) { index, exercise ->
        Card(
          shape = WorkspaceCardShape,
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          ),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            // Numbered Sequence Badge
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = String.format(Locale.US, "%02d", index + 1),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
              Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${exercise.sets} sets × ${exercise.reps} reps • ${exercise.displayWeight}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary,
              )
              if (exercise.alternative != null) {
                Text(
                  text = "Option: ${exercise.alternative}",
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Surface(
              shape = PillShape,
              color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
              Text(
                text = exercise.targetMuscle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                maxLines = 1,
                softWrap = false,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DayMetricBadge(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        softWrap = false,
      )
    }
  }
}
