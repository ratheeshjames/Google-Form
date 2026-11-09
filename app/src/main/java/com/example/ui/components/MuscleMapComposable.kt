package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.PillShape
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape

data class AnatomicalMuscle(
  val id: String,
  val name: String,
  val anatomyRegion: String, // Anterior / Posterior
  val weeklySetsLogged: Int,
  val targetWeeklySets: Int,
  val splitExercises: List<String>,
)

val defaultMuscles = listOf(
  AnatomicalMuscle("pecs", "Pectorals (Chest)", "Anterior", 14, 16, listOf("Incline Bench Press", "Bench Press", "Incline Bench Fly", "Bench Fly", "Decline Bench Press")),
  AnatomicalMuscle("lats", "Latissimus Dorsi & Rhomboids", "Posterior", 16, 16, listOf("Dumbbell Pullover", "Single-Arm Bench Lat Row", "Bent-Over Dumbbell Row", "Wide Row")),
  AnatomicalMuscle("delts", "Deltoids (Shoulders)", "Anterior / Lateral", 14, 16, listOf("Overhead Press", "Arnold Press", "Lateral Raises", "Front Raises", "Reverse Fly")),
  AnatomicalMuscle("biceps", "Biceps & Brachialis", "Anterior", 16, 16, listOf("Bicep Curl", "Hammer Curl", "Alternate Bicep Curl", "Alternate Hammer Curl")),
  AnatomicalMuscle("triceps", "Triceps (Long & Lateral)", "Posterior", 14, 16, listOf("Lying French Press", "French Press", "Triceps Kickback")),
  AnatomicalMuscle("quads", "Quadriceps & Glutes", "Anterior", 16, 16, listOf("Squats", "Lunges", "Incline Treadmill Power Hike")),
  AnatomicalMuscle("posterior", "Hamstrings & Lower Back", "Posterior", 12, 16, listOf("Deadlift", "Calf Raises", "Treadmill HIIT Sprints")),
  AnatomicalMuscle("core", "Rectus Abdominis & Obliques", "Anterior", 16, 16, listOf("Bench Lying Leg Raises", "Dumbbell V-Up & Leg Tuck", "Lying Leg Raises", "Crunches", "Bicycle Crunches")),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleMapComposable(
  modifier: Modifier = Modifier,
  muscles: List<AnatomicalMuscle> = defaultMuscles,
) {
  val haptic = LocalHapticFeedback.current
  var selectedMuscleId by remember { mutableStateOf("pecs") }
  val activeMuscle = muscles.find { it.id == selectedMuscleId } ?: muscles.first()

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("muscle_map_composable"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "Anatomical Muscle Activation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Interactive load & volume distribution",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Surface(
          shape = PillShape,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Text(
            text = "${activeMuscle.weeklySetsLogged} / ${activeMuscle.targetWeeklySets} Sets",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      // Interactive Muscle Selection Chips with Dynamic Highlight
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
      ) {
        muscles.forEach { muscle ->
          val isSelected = muscle.id == selectedMuscleId
          val chipBg by animateColorAsState(
            targetValue = if (isSelected) GoogleBlue40 else MaterialTheme.colorScheme.surfaceContainerLowest,
            label = "muscle_chip_bg",
          )
          val chipTextColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

          Surface(
            shape = PillShape,
            color = chipBg,
            modifier = Modifier
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                selectedMuscleId = muscle.id
              }
              .testTag("muscle_chip_${muscle.id}"),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(14.dp),
                )
              }
              Text(
                text = muscle.name.split(" ")[0], // First word (Pectorals, Lats, etc.)
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = chipTextColor,
              )
            }
          }
        }
      }

      // Active Muscle Card Focus
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(SquircleFabShape)
                .background(GoogleGreen40),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
              )
            }

            Column {
              Text(
                text = activeMuscle.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "Region: ${activeMuscle.anatomyRegion} Chain",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Preloaded Split Exercises:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
          )

          Spacer(modifier = Modifier.height(4.dp))

          activeMuscle.splitExercises.forEach { exName ->
            Text(
              text = "• $exName",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 1.dp),
            )
          }
        }
      }
    }
  }
}
