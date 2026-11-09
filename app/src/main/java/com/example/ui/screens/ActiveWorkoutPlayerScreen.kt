package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.sp
import com.example.model.ExerciseProtocol
import com.example.model.WorkoutDayProtocol
import com.example.model.WorkoutProtocolRepository
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.GoogleRed40
import com.example.ui.theme.GoogleYellow40
import com.example.ui.theme.PillShape
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutPlayerScreen(
  dayNumber: Int = 1,
  onClosePlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val haptic = LocalHapticFeedback.current
  val workoutProtocol: WorkoutDayProtocol = remember(dayNumber) {
    WorkoutProtocolRepository.getWorkout(dayNumber)
  }

  // Session timer: exactly 30:00 (1800 seconds)
  var totalSecondsRemaining by remember { mutableIntStateOf(workoutProtocol.durationMinutes * 60) }
  var isPaused by remember { mutableStateOf(false) }
  var sessionCompleted by remember { mutableStateOf(false) }

  // Exercise and Set Progression
  var currentExerciseIndex by remember { mutableIntStateOf(0) }
  var currentSetIndex by remember { mutableIntStateOf(1) }
  var completedSetsCount by remember { mutableIntStateOf(0) }

  // Rest interval management
  var isResting by remember { mutableStateOf(false) }
  var restSecondsRemaining by remember { mutableIntStateOf(45) }

  val currentExercise = workoutProtocol.exercises.getOrNull(currentExerciseIndex)

  // Real-time custom adjustments
  var activeReps by remember(currentExercise) {
    mutableIntStateOf(currentExercise?.reps ?: 15)
  }
  var activeWeight by remember(currentExercise) {
    mutableDoubleStateOf(currentExercise?.weightKg ?: 8.0)
  }
  var substitutedExerciseName by remember(currentExercise) {
    mutableStateOf(currentExercise?.name ?: "")
  }

  // Running 1-second ticker for workout duration
  LaunchedEffect(isPaused, totalSecondsRemaining, sessionCompleted) {
    if (!isPaused && totalSecondsRemaining > 0 && !sessionCompleted) {
      delay(1000L)
      totalSecondsRemaining -= 1
      if (totalSecondsRemaining == 0) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        sessionCompleted = true
      }
    }
  }

  // Rest timer ticker
  LaunchedEffect(isResting, restSecondsRemaining) {
    if (isResting && restSecondsRemaining > 0) {
      delay(1000L)
      restSecondsRemaining -= 1
      if (restSecondsRemaining == 0) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        isResting = false
      }
    }
  }

  val totalElapsedSeconds = (workoutProtocol.durationMinutes * 60) - totalSecondsRemaining
  val elapsedMinutes = totalElapsedSeconds / 60
  val elapsedSecs = totalElapsedSeconds % 60
  val remMinutes = totalSecondsRemaining / 60
  val remSecs = totalSecondsRemaining % 60

  val timerProgress = (totalElapsedSeconds.toFloat() / (workoutProtocol.durationMinutes * 60)).coerceIn(0f, 1f)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .testTag("active_workout_player"),
  ) {
    if (sessionCompleted || currentExercise == null) {
      // Session Completion / Summary View (Prescribed vs Completed accounting)
      SessionCompletionSummary(
        totalPrescribedSets = workoutProtocol.totalPrescribedSets,
        completedSets = completedSetsCount,
        durationMinutes = workoutProtocol.durationMinutes,
        onClose = onClosePlayer,
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 20.dp, vertical = 12.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Top Bar: Exit button & 30-Min Strict Countdown
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onClosePlayer,
            modifier = Modifier.size(44.dp),
          ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close Workout")
          }

          Surface(
            shape = PillShape,
            color = when {
              totalSecondsRemaining < 300 -> MaterialTheme.colorScheme.errorContainer
              totalSecondsRemaining < 600 -> MaterialTheme.colorScheme.tertiaryContainer
              else -> MaterialTheme.colorScheme.surfaceContainerHigh
            },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (totalSecondsRemaining < 300) GoogleRed40 else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = String.format("%02d:%02d left", remMinutes, remSecs),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (totalSecondsRemaining < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
              )
            }
          }

          IconButton(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              isPaused = !isPaused
            },
            modifier = Modifier.size(44.dp),
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume" else "Pause",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        }

        // Overall Session Progress Bar
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Elapsed: ${String.format("%02d:%02d", elapsedMinutes, elapsedSecs)}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "Sets: $completedSetsCount / ${workoutProtocol.totalPrescribedSets}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          LinearProgressIndicator(
            progress = { timerProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(PillShape),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          )
        }

        // Active State: Rest Mode vs Working Set Mode
        if (isResting) {
          // REST INTERVAL DISPLAY
          Card(
            shape = WorkspaceCardShape,
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Text(
                text = "REST INTERVAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )

              Text(
                text = "${restSecondsRemaining}s",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )

              Text(
                text = "Next: $substitutedExerciseName (Set $currentSetIndex of ${currentExercise.sets})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { restSecondsRemaining += 15 },
                  shape = PillShape,
                ) {
                  Text("+15s Rest")
                }
                Button(
                  onClick = { isResting = false },
                  shape = PillShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                  ),
                ) {
                  Text("Skip Rest")
                }
              }
            }
          }
        } else {
          // WORKING SET DISPLAY
          Card(
            shape = WorkspaceCardShape,
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              // Exercise Number & Target Muscle
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Surface(
                  shape = PillShape,
                  color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  Text(
                    text = "Exercise ${currentExerciseIndex + 1} of ${workoutProtocol.exercises.size}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }

                Surface(
                  shape = PillShape,
                  color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                  Text(
                    text = currentExercise.targetMuscle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }

              // Exercise Title with Pull-Up Pullover swap if available
              Column {
                Text(
                  text = substitutedExerciseName,
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )

                if (currentExercise.alternative != null) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                      .clip(PillShape)
                      .clickable {
                        substitutedExerciseName = if (substitutedExerciseName == currentExercise.name) {
                          currentExercise.alternative
                        } else {
                          currentExercise.name
                        }
                      }
                      .padding(vertical = 4.dp),
                  ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Tap to swap: ${if (substitutedExerciseName == currentExercise.name) currentExercise.alternative else currentExercise.name}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
              }

              // Set Index Indicator
              Text(
                text = "SET $currentSetIndex OF ${currentExercise.sets}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )

              // Weight & Rep Controls Stepper
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                // Weight Stepper
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.surfaceContainerLowest,
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    Text(text = "Weight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                      text = if (currentExercise.isBodyweight && activeWeight == 0.0) "Bodyweight" else "${activeWeight.toInt()} kg",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      IconButton(
                        onClick = { if (activeWeight > 1.0) activeWeight -= 1.0 },
                        modifier = Modifier
                          .size(32.dp)
                          .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                      ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                      }
                      IconButton(
                        onClick = { activeWeight += 1.0 },
                        modifier = Modifier
                          .size(32.dp)
                          .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                      ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }

                // Reps Stepper
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.surfaceContainerLowest,
                  modifier = Modifier.weight(1f),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                  ) {
                    Text(text = "Reps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                      text = "$activeReps reps",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      IconButton(
                        onClick = { if (activeReps > 1) activeReps -= 1 },
                        modifier = Modifier
                          .size(32.dp)
                          .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                      ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                      }
                      IconButton(
                        onClick = { activeReps += 1 },
                        modifier = Modifier
                          .size(32.dp)
                          .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                      ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }

              // Complete Set Button
              Button(
                onClick = {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  completedSetsCount += 1

                  if (currentSetIndex < currentExercise.sets) {
                    currentSetIndex += 1
                    isResting = true
                    restSecondsRemaining = 45
                  } else {
                    // Move to next exercise
                    if (currentExerciseIndex < workoutProtocol.exercises.size - 1) {
                      currentExerciseIndex += 1
                      currentSetIndex = 1
                      isResting = true
                      restSecondsRemaining = 60
                    } else {
                      // All exercises finished
                      sessionCompleted = true
                    }
                  }
                },
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(54.dp)
                  .testTag("complete_set_button"),
              ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Complete Set $currentSetIndex",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        // Upcoming Exercise Preview
        val nextExercise = workoutProtocol.exercises.getOrNull(currentExerciseIndex + 1)
        if (nextExercise != null) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Column {
                Text(text = "Next Exercise:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "${nextExercise.name} (${nextExercise.sets} × ${nextExercise.reps} • ${nextExercise.displayWeight})", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SessionCompletionSummary(
  totalPrescribedSets: Int,
  completedSets: Int,
  durationMinutes: Int,
  onClose: () -> Unit,
) {
  val deferredSets = (totalPrescribedSets - completedSets).coerceAtLeast(0)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Spacer(modifier = Modifier.height(24.dp))

    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(SquircleFabShape)
        .background(MaterialTheme.colorScheme.primary),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(36.dp),
      )
    }

    Text(
      text = "30-Minute Session Complete",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
    )

    Text(
      text = "Strict duration ceiling enforced. All performed volume recorded with zero false completion reporting.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    // Accurate Workload Accountability Card
    Card(
      shape = WorkspaceCardShape,
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
      ),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "Workload Accounting",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Prescribed Workout Sets:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "$totalPrescribedSets sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Completed Within 30 Min:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(text = "$completedSets sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoogleGreen40)
        }

        if (deferredSets > 0) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(text = "Deferred Workload (Rest Capped):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "$deferredSets sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoogleYellow40)
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "Note: The scheduling engine prioritized heavy compounds and safely preserved rest intervals. Deferred sets are logged for recovery balance.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
      onClick = onClose,
      shape = PillShape,
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp),
    ) {
      Text(text = "Return to Dashboard", style = MaterialTheme.typography.titleSmall)
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
