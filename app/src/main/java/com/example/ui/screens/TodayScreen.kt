package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.example.data.AthleteProfileStore
import com.example.data.MealSlot
import com.example.data.NutritionStore
import com.example.model.ExerciseProtocol
import com.example.model.WorkoutProtocolRepository
import com.example.ui.components.DashboardChickenCompanionCard
import com.example.ui.components.DashboardNutritionMiniCard
import com.example.ui.components.WeeklyQuestAndBiometricTrendCard
import com.example.ui.components.workspaceShimmer
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.GoogleYellow40
import com.example.ui.theme.PillShape
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape
import com.example.ui.viewmodels.TodayUiState
import com.example.ui.viewmodels.TodayViewModel
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
  onStartWorkout: () -> Unit = {},
  onStartSpecificWorkout: (Int) -> Unit = { onStartWorkout() },
  onNavigateToTransformation: () -> Unit = {},
  onNavigateToNutrition: () -> Unit = {},
  onOpenBiometricsClick: () -> Unit = {},
  contentPadding: PaddingValues = PaddingValues(0.dp),
  viewModel: TodayViewModel = viewModel(),
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
  val nutritionState by NutritionStore.state.collectAsState()
  val nutritionAnalysis = remember(activeProfile, nutritionState) {
    NutritionStore.analyzeForProfile(activeProfile, nutritionState)
  }
  val autoDetectedDay = remember { AthleteProfileStore.getAutoDetectedWorkoutDay() }
  var selectedDay by remember { mutableIntStateOf(autoDetectedDay) }
  var selectedExerciseForSheet by remember { mutableStateOf<ExerciseProtocol?>(null) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  val scaledDayPlan = remember(
    selectedDay,
    activeProfile.sessionDurationMinutes,
    activeProfile.dumbbellWeightKg,
    activeProfile.exerciseWeightOverrides,
    activeProfile.workoutDifficultyLevel,
  ) {
    WorkoutProtocolRepository.getScaledWorkoutPlan(
      day = selectedDay,
      targetDurationMinutes = activeProfile.sessionDurationMinutes,
      customDumbbellWeightKg = activeProfile.dumbbellWeightKg,
      exerciseWeightOverrides = activeProfile.exerciseWeightOverrides,
      difficultyLevel = activeProfile.workoutDifficultyLevel,
    )
  }

  when (uiState) {
    is TodayUiState.Loading -> {
      TodayShimmerSkeleton(contentPadding = contentPadding, modifier = modifier)
    }
    is TodayUiState.Error -> {
      Box(
        modifier = modifier
          .fillMaxSize()
          .padding(contentPadding),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = "Unable to load today's routine",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    }
    is TodayUiState.Success -> {
      LazyColumn(
        modifier = modifier
          .fillMaxSize()
          .testTag("today_screen_lazy_column"),
        contentPadding = PaddingValues(
          top = contentPadding.calculateTopPadding() + 8.dp,
          bottom = contentPadding.calculateBottomPadding() + 110.dp,
          start = 16.dp,
          end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Weekday Split Selector Strip (Days 1..6 Strength + Sunday Cardio & Recovery Studio)
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            for (dayNum in 1..7) {
              val isSelected = selectedDay == dayNum
              val isToday = dayNum == autoDetectedDay
              FilterChip(
                selected = isSelected,
                onClick = { selectedDay = dayNum },
                label = {
                  val baseLabel = if (dayNum == 7) "Sun • Cardio & Recovery" else AthleteProfileStore.getWeekdayLabel(dayNum)
                  Text(
                    text = if (isToday) "★ $baseLabel" else baseLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                  )
                },
                shape = PillShape,
              )
            }
          }
        }

        // If Day 7 (Sunday Cardio & Active Recovery) is selected: Completely Decoupled Telemetry & Regeneration Studio UI!
        if (selectedDay == 7) {
          item {
            DecoupledCardioRecoveryStudioHub(
              activeProfile = activeProfile,
              onLaunchCardioConsole = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onStartSpecificWorkout(7)
              },
            )
          }
        } else {
          // Card 1: Days 1–6 Dumbbell Strength Hero Card
          item {
            Card(
              shape = WorkspaceCardShape,
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              ),
              elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("today_hero_workout_card"),
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                      )
                      Text(
                        text = "Exact ${scaledDayPlan.targetDurationMinutes}:00 Fit • ${activeProfile.difficultyShortBadge}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                      )
                    }
                  }

                  Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                  ) {
                    Text(
                      text = if (selectedDay == autoDetectedDay) {
                        "Today (${AthleteProfileStore.getWeekdayLabel(selectedDay)})"
                      } else {
                        "${AthleteProfileStore.getWeekdayLabel(selectedDay)} • Preview"
                      },
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      softWrap = false,
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                  }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Text(
                    text = scaledDayPlan.dayProtocol.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                  )
                  Text(
                    text = "${scaledDayPlan.exercises.size} exercises • ${scaledDayPlan.totalScaledSets} scaled sets (${scaledDayPlan.workSecondsPerSet}s work / ${scaledDayPlan.restSecondsPerSet}s rest) calibrated to finish 100% at 00:00 (${activeProfile.difficultyTitle}).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }

                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  BadgeTag(text = "${scaledDayPlan.exercises.size} Exercises")
                  BadgeTag(text = "${scaledDayPlan.totalScaledSets} Scaled Sets")
                  BadgeTag(text = activeProfile.difficultyShortBadge)
                  BadgeTag(text = "${String.format(Locale.US, "%.1f", activeProfile.dumbbellWeightKg)} kg Base")
                  if (activeProfile.exerciseWeightOverrides.isNotEmpty()) {
                    BadgeTag(text = "${activeProfile.exerciseWeightOverrides.size} Custom Loads")
                  }
                  BadgeTag(text = "No Pull-Up Bar")
                }

                // Time-Constrained Execution Plan
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.surfaceContainerLowest,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "Time-Scaled Execution Plan",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                      Text(
                        text = "${scaledDayPlan.secondsPerSetSlot}s / Set Slot → Exact ${scaledDayPlan.targetDurationMinutes}:00",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false,
                        color = MaterialTheme.colorScheme.primary,
                      )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                      TimeBudgetSegment(
                        label = "Prep",
                        duration = "${scaledDayPlan.prepSecondsPerSet}s/set",
                        color = GoogleYellow40,
                        modifier = Modifier.weight(0.25f),
                      )
                      TimeBudgetSegment(
                        label = "Active Work",
                        duration = "${scaledDayPlan.workSecondsPerSet}s/set",
                        color = GoogleBlue40,
                        modifier = Modifier.weight(0.45f),
                      )
                      TimeBudgetSegment(
                        label = "Recovery",
                        duration = "${scaledDayPlan.restSecondsPerSet}s/set",
                        color = GoogleGreen40,
                        modifier = Modifier.weight(0.30f),
                      )
                    }
                  }
                }

                Button(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStartSpecificWorkout(selectedDay)
                  },
                  shape = PillShape,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("begin_session_button"),
                ) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "Begin Day $selectedDay • ${scaledDayPlan.targetDurationMinutes}-Min Hands-Free Session",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                  )
                }
              }
            }
          }
        }

        // Card 2: Sunday Special Cardio + Treadmill + Active Recovery Banner (ONLY VISIBLE ON SUNDAYS when viewing another day)
        val isSundayToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
        if (isSundayToday && selectedDay != 7) {
          item {
            Card(
              shape = WorkspaceCardShape,
              colors = CardDefaults.cardColors(
                containerColor = Color(0xFF0A2128),
              ),
              border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(SquircleFabShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center,
                ) {
                  Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp),
                  )
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "SUNDAY RECOVERY & TREADMILL STUDIO",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00E5FF),
                  )
                  Text(
                    text = "Zone-2 Treadmill & Active Recovery",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                  )
                  Text(
                    text = "${activeProfile.cardioDurationMin}m • ${activeProfile.cardioDifficultyTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White.copy(alpha = 0.78f),
                  )
                }

                OutlinedButton(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedDay = 7
                  },
                  shape = PillShape,
                  border = BorderStroke(1.dp, Color(0xFF10B981)),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                  Text(
                    text = "Open Studio",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    maxLines = 1,
                    softWrap = false,
                  )
                }
              }
            }
          }
        }

        // Card 3: Main Dashboard Nutrition, Macro & Hydration Mini-Summary Card
        item {
          DashboardNutritionMiniCard(
            analysis = nutritionAnalysis,
            topQuickPresets = nutritionState.presets.take(3),
            onQuickLogPreset = { preset ->
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              val entry = NutritionStore.quickLogPreset(
                preset = preset,
                multiplier = 1.0f,
                mealSlot = MealSlot.autoDetectForCurrentHour(),
                context = context,
              )
              Toast.makeText(
                context,
                "Logged ${preset.name} (+${entry.caloriesKcal} kcal • ${entry.proteinG}g P)",
                Toast.LENGTH_SHORT,
              ).show()
            },
            onAddWaterGlass = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              NutritionStore.addWaterMl(250, context)
              Toast.makeText(context, "+250ml Water Logged", Toast.LENGTH_SHORT).show()
            },
            onOpenNutritionScreen = onNavigateToNutrition,
          )
        }

        // Card 4: Integrated 2D Warrior Rooster Evolution, Evolving Dojo & Interactive Actions
        item {
          DashboardChickenCompanionCard(
            activeProfile = activeProfile,
            onOpenBiometricsClick = onOpenBiometricsClick,
          )
        }

        // Card 5: v2 Weekly Biometric Trend Comparison & Quest Board + Streak Freeze Shield
        item {
          WeeklyQuestAndBiometricTrendCard(
            activeProfile = activeProfile,
            onOpenBiometricsClick = onOpenBiometricsClick,
          )
        }

        // Section: Dumbbell Exercise Queue Preview (ONLY for Days 1..6 Strength Days!)
        if (selectedDay != 7) {
          item {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Exercise Queue Preview",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Day $selectedDay • ${scaledDayPlan.exercises.size} Exercises • Tap ⋮ to customize weight",
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Spacer(modifier = Modifier.width(8.dp))

              Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
              ) {
                Text(
                  text = "${String.format(Locale.US, "%.1f", activeProfile.dumbbellWeightKg)} kg Base",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  softWrap = false,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
              }
            }
          }

          itemsIndexed(scaledDayPlan.exercises) { index, exercise ->
            val hasCustomLoad = activeProfile.exerciseWeightOverrides.containsKey(exercise.name)
            Card(
              shape = WorkspaceCardShape,
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                  selectedExerciseForSheet = exercise
                }
                .testTag("exercise_item_$index"),
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
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
                    text = "${exercise.sets} sets × ${exercise.reps} reps • ${exercise.displayWeight}${if (hasCustomLoad) " ★" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    color = if (hasCustomLoad) GoogleGreen40 else MaterialTheme.colorScheme.primary,
                  )
                }

                Surface(
                  shape = PillShape,
                  color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                  Text(
                    text = exercise.targetMuscle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }

                IconButton(
                  onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    selectedExerciseForSheet = exercise
                  },
                  modifier = Modifier
                    .size(34.dp)
                    .testTag("exercise_overflow_$index"),
                ) {
                  Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options for ${exercise.name}",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }

        // Card 5: Recent 3-Biometric Transformation Snapshot
        item {
          Card(
            shape = WorkspaceCardShape,
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onNavigateToTransformation() }
              .testTag("transformation_snapshot_card"),
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "3-Biometric & Auto Goal Snapshot",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = "View all measurements",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                SnapshotMetric(
                  label = "Current Weight",
                  value = String.format(Locale.US, "%.1f kg", activeProfile.weightKg),
                  delta = String.format(Locale.US, "BMI %.1f • %.1f%% RFM", activeProfile.bmi, activeProfile.rfmBodyFatPct),
                  isGreen = true,
                  modifier = Modifier.weight(1f),
                )
                SnapshotMetric(
                  label = "Auto Goal Weight",
                  value = String.format(Locale.US, "%.1f kg", activeProfile.autoGoalWeightKg),
                  delta = "ETA: ~${activeProfile.autoWeeksToGoal} Weeks (${activeProfile.autoProjectedDateString})",
                  isGreen = true,
                  modifier = Modifier.weight(1f),
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Surface(
                shape = PillShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(GoogleGreen40),
                  )
                  Text(
                    text = String.format(
                      Locale.US,
                      "Height: %.0f cm • Waist: %.1f cm → Auto Target %.1f cm (WHtR %.2f)",
                      activeProfile.heightCm,
                      activeProfile.waistCm,
                      activeProfile.autoGoalWaistCm,
                      activeProfile.whtr,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Exercise Quick-Action Modal Bottom Sheet with Per-Exercise Working Dumbbell Load Adjuster
  selectedExerciseForSheet?.let { sheetExercise ->
    val liveExercise = scaledDayPlan.exercises.find { it.name == sheetExercise.name } ?: sheetExercise
    val currentLoadKg = (liveExercise.weightKg ?: activeProfile.dumbbellWeightKg.toDouble()).toFloat()

    ModalBottomSheet(
      onDismissRequest = { selectedExerciseForSheet = null },
      sheetState = sheetState,
      shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
      modifier = Modifier.testTag("exercise_bottom_sheet"),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(SquircleFabShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.FitnessCenter,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(26.dp),
            )
          }

          Column {
            Text(
              text = liveExercise.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${liveExercise.sets} sets × ${liveExercise.reps} reps • ${liveExercise.displayWeight} • ${liveExercise.targetMuscle}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // Interactive Per-Exercise Working Dumbbell Load Adjuster (-0.5 kg / +0.5 kg)
        if (!liveExercise.isBodyweight) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Per-Exercise Working Dumbbell Load",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Saved automatically for ${liveExercise.name}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                IconButton(
                  onClick = {
                    val nextWeight = (currentLoadKg - 0.5f).coerceAtLeast(1.0f)
                    AthleteProfileStore.setExerciseWeightOverride(liveExercise.name, nextWeight, context)
                  },
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                ) {
                  Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease 0.5 kg")
                }

                Text(
                  text = String.format(Locale.US, "%.1f kg", currentLoadKg),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Black,
                  color = MaterialTheme.colorScheme.primary,
                )

                IconButton(
                  onClick = {
                    val nextWeight = (currentLoadKg + 0.5f).coerceAtMost(60.0f)
                    AthleteProfileStore.setExerciseWeightOverride(liveExercise.name, nextWeight, context)
                  },
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest),
                ) {
                  Icon(imageVector = Icons.Default.Add, contentDescription = "Increase 0.5 kg")
                }
              }
            }
          }
        }

        SheetActionItem(
          icon = Icons.Default.SwapHoriz,
          title = "Dumbbell & Bench Compatible",
          subtitle = liveExercise.alternative ?: "Zero pull-up bar required—optimized for home dumbbells & bench",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedExerciseForSheet = null
          },
        )

        SheetActionItem(
          icon = Icons.Default.Info,
          title = "View Technique & Biomechanics Guide",
          subtitle = "Starting position, 2s eccentric tempo, tucked elbows, and breathing cadence.",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedExerciseForSheet = null
          },
        )

        SheetActionItem(
          icon = Icons.Default.History,
          title = "Time-Scaled Performance",
          subtitle = "Scaled to ${liveExercise.sets} sets × ${liveExercise.reps} reps so your ${scaledDayPlan.targetDurationMinutes}-min session finishes on time.",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            selectedExerciseForSheet = null
          },
        )

        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
private fun SheetActionItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(SquircleFabShape)
          .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentAlignment = Alignment.Center,
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
private fun TimeBudgetSegment(
  label: String,
  duration: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = duration,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        color = color,
      )
    }
    Spacer(modifier = Modifier.height(6.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(PillShape)
        .background(color),
    )
  }
}

@Composable
private fun SnapshotMetric(
  label: String,
  value: String,
  delta: String,
  isGreen: Boolean,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(text = label, style = MaterialTheme.typography.labelSmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
      Text(text = delta, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (isGreen) GoogleGreen40 else MaterialTheme.colorScheme.primary)
    }
  }
}

@Composable
private fun BadgeTag(text: String) {
  Surface(
    shape = PillShape,
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      maxLines = 1,
      softWrap = false,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun TodayShimmerSkeleton(
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(
        top = contentPadding.calculateTopPadding() + 8.dp,
        bottom = contentPadding.calculateBottomPadding() + 110.dp,
        start = 16.dp,
        end = 16.dp,
      ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
        .clip(WorkspaceCardShape)
        .workspaceShimmer(),
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(110.dp)
        .clip(WorkspaceCardShape)
        .workspaceShimmer(),
    )

    repeat(3) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
          .clip(WorkspaceCardShape)
          .workspaceShimmer(),
      )
    }
  }
}
