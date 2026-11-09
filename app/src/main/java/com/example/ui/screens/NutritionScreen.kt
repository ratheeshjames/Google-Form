package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfileStore
import com.example.data.FoodQuickPreset
import com.example.data.LoggedFoodEntry
import com.example.data.MealSlot
import com.example.data.NutritionInsightTip
import com.example.data.NutritionStore
import com.example.ui.components.CalorieIntakePredictorCard
import com.example.ui.components.HydrationWaveGaugeCard
import com.example.ui.components.PrescribedCalorieAndMacroGaugeHeroCard
import com.example.ui.theme.GoogleBlue40
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.PillShape
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape
import java.util.Locale

private val AccentEmerald = Color(0xFF10B981)
private val AccentCyan = Color(0xFF00E5FF)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentCoral = Color(0xFFEF4444)

private enum class PresetFilterTab(val label: String) {
  ALL("All Presets"),
  CUSTOM("★ My Custom"),
  FAVORITES("Favorites"),
  HIGH_PROTEIN("High Protein"),
  BREAKFAST("Breakfast"),
  MAIN_MEALS("Lunch & Dinner"),
  GYM_FUEL("Gym Fuel"),
  SNACKS("Snacks"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NutritionScreen(
  contentPadding: PaddingValues = PaddingValues(0.dp),
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  LaunchedEffect(context) {
    NutritionStore.initialize(context)
  }

  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()
  val nutritionState by NutritionStore.state.collectAsState()

  val analysis = remember(activeProfile, nutritionState) {
    NutritionStore.analyzeForProfile(activeProfile, nutritionState)
  }

  var activeMealSlot by remember { mutableStateOf(MealSlot.autoDetectForCurrentHour()) }
  var selectedFilterTab by remember { mutableStateOf(PresetFilterTab.ALL) }
  var searchQuery by remember { mutableStateOf("") }
  var showPresetCreatorDialog by remember { mutableStateOf(false) }
  var editingPreset by remember { mutableStateOf<FoodQuickPreset?>(null) }

  val filteredPresets = remember(nutritionState.presets, selectedFilterTab, searchQuery) {
    nutritionState.presets.filter { preset ->
      val matchesSearch = searchQuery.isBlank() ||
        preset.name.contains(searchQuery.trim(), ignoreCase = true) ||
        preset.servingDesc.contains(searchQuery.trim(), ignoreCase = true)
      val matchesTab = when (selectedFilterTab) {
        PresetFilterTab.ALL -> true
        PresetFilterTab.CUSTOM -> preset.isCustom
        PresetFilterTab.FAVORITES -> preset.isFavorite
        PresetFilterTab.HIGH_PROTEIN -> preset.isHighProtein
        PresetFilterTab.BREAKFAST -> preset.defaultMealSlot == MealSlot.BREAKFAST
        PresetFilterTab.MAIN_MEALS -> preset.defaultMealSlot == MealSlot.LUNCH || preset.defaultMealSlot == MealSlot.DINNER
        PresetFilterTab.GYM_FUEL -> preset.defaultMealSlot == MealSlot.PRE_POST_WORKOUT
        PresetFilterTab.SNACKS -> preset.defaultMealSlot == MealSlot.SNACK
      }
      matchesSearch && matchesTab
    }
  }

  val customPresetsCount = remember(nutritionState.presets) {
    nutritionState.presets.count { it.isCustom }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("nutrition_screen"),
    contentPadding = PaddingValues(
      top = contentPadding.calculateTopPadding() + 8.dp,
      bottom = contentPadding.calculateBottomPadding() + 112.dp,
      start = 16.dp,
      end = 16.dp,
    ),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Header + Daily Nutrition Precision Score Badge
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Prescribed Nutrition Studio",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "3-Biometric Calorie & Macro Gauges • Custom Food Presets • EOD Predictor",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = PillShape,
          color = AccentEmerald.copy(alpha = 0.16f),
          border = BorderStroke(1.dp, AccentEmerald.copy(alpha = 0.45f)),
        ) {
          Text(
            text = "Score ${analysis.dailyNutritionScorePct}%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = AccentEmerald,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }
      }
    }

    // 1. Prescribed Calorie & Triple Macro Radial Gauges Hero Card
    item {
      PrescribedCalorieAndMacroGaugeHeroCard(
        analysis = analysis,
        onSelectGoalMode = { mode ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          NutritionStore.setGoalMode(mode, context)
        },
      )
    }

    // 2. Custom Quick Food Presets Studio Card (Core Feature)
    item {
      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("custom_food_presets_section"),
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          // Section Title + "+ Add Custom Preset" Primary Button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = "Quick Food Presets",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                  shape = PillShape,
                  color = AccentAmber.copy(alpha = 0.2f),
                ) {
                  Text(
                    text = "$customPresetsCount Custom",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentAmber,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
              }
              Text(
                text = "Create your own custom meals & tap +½×, +1×, or +2× to log instantly",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          // Prominent Primary CTA to Create Custom Food Preset
          Button(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              editingPreset = null
              showPresetCreatorDialog = true
            },
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("add_custom_food_preset_button"),
          ) {
            Icon(
              imageVector = Icons.Default.BookmarkAdd,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "+ Create Custom Food Preset",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              softWrap = false,
            )
          }

          // Active Target Meal Slot Strip (Where 1-tap logs will be recorded)
          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "LOGGING INTO MEAL SLOT: ${activeMealSlot.label.uppercase(Locale.US)}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              MealSlot.entries.forEach { slot ->
                val isSelected = slot == activeMealSlot
                FilterChip(
                  selected = isSelected,
                  onClick = { activeMealSlot = slot },
                  label = {
                    Text(
                      text = slot.shortLabel,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                  },
                  shape = PillShape,
                )
              }
            }
          }

          // Search & Category Filter Row
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                text = "Search custom or starter food presets...",
                style = MaterialTheme.typography.bodySmall,
              )
            },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { searchQuery = "" }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear search",
                    modifier = Modifier.size(18.dp),
                  )
                }
              }
            },
            singleLine = true,
            shape = PillShape,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("preset_search_input"),
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            PresetFilterTab.entries.forEach { tab ->
              val isSelected = selectedFilterTab == tab
              FilterChip(
                selected = isSelected,
                onClick = { selectedFilterTab = tab },
                label = {
                  val labelText = if (tab == PresetFilterTab.CUSTOM) {
                    "★ My Custom ($customPresetsCount)"
                  } else {
                    tab.label
                  }
                  Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                  )
                },
                shape = PillShape,
              )
            }
          }

          // Preset Items List
          if (filteredPresets.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLowest,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.RestaurantMenu,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(32.dp),
                )
                Text(
                  text = if (selectedFilterTab == PresetFilterTab.CUSTOM) {
                    "No custom food presets created yet"
                  } else {
                    "No matching food presets found"
                  },
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "Tap '+ Create Custom Food Preset' above to save your favorite homemade meals, shakes, or snacks for 1-tap logging.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              filteredPresets.forEach { preset ->
                QuickFoodPresetItemCard(
                  preset = preset,
                  targetMealSlot = activeMealSlot,
                  onLogWithMultiplier = { mult ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val entry = NutritionStore.quickLogPreset(
                      preset = preset,
                      multiplier = mult,
                      mealSlot = activeMealSlot,
                      context = context,
                    )
                    Toast.makeText(
                      context,
                      "Logged ${entry.formattedMultiplier} ${preset.name} (+${entry.caloriesKcal} kcal • ${entry.proteinG}g P)",
                      Toast.LENGTH_SHORT,
                    ).show()
                  },
                  onToggleFavorite = {
                    NutritionStore.toggleFavoritePreset(preset.id, context)
                  },
                  onEditPreset = {
                    editingPreset = preset
                    showPresetCreatorDialog = true
                  },
                  onDeletePreset = {
                    NutritionStore.deleteFoodPreset(preset.id, context)
                    Toast.makeText(context, "Removed ${preset.name}", Toast.LENGTH_SHORT).show()
                  },
                )
              }
            }
          }
        }
      }
    }

    // 3. Interactive End-of-Day Calorie & Weight-Trajectory Predictor Card
    item {
      CalorieIntakePredictorCard(
        profile = activeProfile,
        analysis = analysis,
        onSetManualRemainingKcal = { overrideKcal ->
          NutritionStore.setManualRemainingCaloriesOverride(overrideKcal)
        },
      )
    }

    // 4. Hydration Wave & Radial Gauge Card
    item {
      HydrationWaveGaugeCard(
        analysis = analysis,
        onAddWaterMl = { deltaMl ->
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          NutritionStore.addWaterMl(deltaMl, context)
        },
      )
    }

    // 5. Today's Logged Meals Timeline
    item {
      Card(
        shape = WorkspaceCardShape,
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("today_logged_meals_card"),
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Today's Logged Intake (${nutritionState.todayEntries.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = "${analysis.consumedCaloriesKcal} kcal • ${analysis.consumedProteinG}g P • ${analysis.consumedCarbsG}g C • ${analysis.consumedFatsG}g F",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            if (nutritionState.todayEntries.isNotEmpty()) {
              TextButton(
                onClick = {
                  NutritionStore.clearTodayEntries(context)
                },
              ) {
                Text(
                  text = "Clear Day",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }

          if (nutritionState.todayEntries.isEmpty()) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceContainerLowest,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(
                text = "No meals logged yet today. Tap +½×, +1×, or +2× on any Quick Food Preset above to log your intake.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(14.dp),
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              nutritionState.todayEntries.forEach { entry ->
                LoggedFoodEntryRow(
                  entry = entry,
                  onRemove = {
                    NutritionStore.removeLoggedEntry(entry.id, context)
                  },
                )
              }
            }
          }
        }
      }
    }

    // 6. Smart Prescribed Nutrition Insights & Actionable Tips
    item {
      Text(
        text = "Prescribed Nutrition Insights & Tips",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }

    items(analysis.insights, key = { it.id }) { tip ->
      NutritionInsightCard(tip = tip)
    }
  }

  // Custom Food Preset Creator / Editor Modal Dialog
  if (showPresetCreatorDialog) {
    CustomFoodPresetDialog(
      initialPreset = editingPreset,
      defaultSlot = activeMealSlot,
      onDismiss = {
        showPresetCreatorDialog = false
        editingPreset = null
      },
      onSavePreset = { name, serving, kcal, prot, carbs, fats, slot, emoji, logNow ->
        val existing = editingPreset
        if (existing != null) {
          val updated = existing.copy(
            name = name.ifBlank { existing.name },
            servingDesc = serving.ifBlank { existing.servingDesc },
            caloriesKcal = kcal,
            proteinG = prot,
            carbsG = carbs,
            fatsG = fats,
            defaultMealSlot = slot,
            badgeEmoji = emoji,
            isCustom = true,
          )
          NutritionStore.updateFoodPreset(updated, context)
          if (logNow) {
            NutritionStore.quickLogPreset(updated, 1.0f, slot, context)
          }
          Toast.makeText(context, "Updated preset: ${updated.name}", Toast.LENGTH_SHORT).show()
        } else {
          val created = NutritionStore.addCustomFoodPreset(
            name = name,
            servingDesc = serving,
            caloriesKcal = kcal,
            proteinG = prot,
            carbsG = carbs,
            fatsG = fats,
            defaultMealSlot = slot,
            badgeEmoji = emoji,
            alsoLogImmediately = logNow,
            context = context,
          )
          Toast.makeText(
            context,
            if (logNow) "Saved & logged ${created.name} (+${created.caloriesKcal} kcal)" else "Saved custom preset: ${created.name}",
            Toast.LENGTH_SHORT,
          ).show()
        }
        showPresetCreatorDialog = false
        editingPreset = null
      },
    )
  }
}

@Composable
private fun QuickFoodPresetItemCard(
  preset: FoodQuickPreset,
  targetMealSlot: MealSlot,
  onLogWithMultiplier: (Float) -> Unit,
  onToggleFavorite: () -> Unit,
  onEditPreset: () -> Unit,
  onDeletePreset: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    border = BorderStroke(
      1.dp,
      if (preset.isCustom) {
        AccentAmber.copy(alpha = 0.50f)
      } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
      },
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("preset_card_${preset.id}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Emoji Badge
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (preset.isCustom) {
                AccentAmber.copy(alpha = 0.16f)
              } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
              }
            ),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = preset.badgeEmoji,
            fontSize = 22.sp,
          )
        }

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text(
              text = preset.name,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
            )
            if (preset.isCustom) {
              Surface(
                shape = PillShape,
                color = AccentAmber.copy(alpha = 0.20f),
              ) {
                Text(
                  text = "★ CUSTOM",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  fontWeight = FontWeight.ExtraBold,
                  color = AccentAmber,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                )
              }
            }
          }
          Text(
            text = "${preset.servingDesc} • ${preset.defaultMealSlot.shortLabel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        // Favorite + Edit / Delete icons
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              imageVector = if (preset.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
              contentDescription = "Favorite",
              tint = if (preset.isFavorite) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp),
            )
          }
          IconButton(
            onClick = onEditPreset,
            modifier = Modifier.size(32.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit preset",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(17.dp),
            )
          }
          if (preset.isCustom) {
            IconButton(
              onClick = onDeletePreset,
              modifier = Modifier.size(32.dp),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Delete custom preset",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }

      // Macro Badges + 1-Tap Multiplier Log Buttons Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        // Kcal & P/C/F summary pills
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          MacroMiniBadge(text = "${preset.caloriesKcal} kcal", color = AccentEmerald)
          MacroMiniBadge(text = "${preset.proteinG}g P", color = AccentCyan)
          MacroMiniBadge(text = "${preset.carbsG}g C", color = AccentBlue)
          MacroMiniBadge(text = "${preset.fatsG}g F", color = AccentAmber)
        }

        Spacer(modifier = Modifier.width(6.dp))

        // 1-Tap Quick Multipliers: +½×, +1×, +2×
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          MultiplierQuickChip(
            label = "+½×",
            isPrimary = false,
            onClick = { onLogWithMultiplier(0.5f) },
          )
          MultiplierQuickChip(
            label = "+1× Log",
            isPrimary = true,
            onClick = { onLogWithMultiplier(1.0f) },
          )
          MultiplierQuickChip(
            label = "+2×",
            isPrimary = false,
            onClick = { onLogWithMultiplier(2.0f) },
          )
        }
      }
    }
  }
}

@Composable
private fun MacroMiniBadge(
  text: String,
  color: Color,
) {
  Surface(
    shape = PillShape,
    color = color.copy(alpha = 0.14f),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      fontWeight = FontWeight.ExtraBold,
      color = color,
      maxLines = 1,
      softWrap = false,
      modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
    )
  }
}

@Composable
private fun MultiplierQuickChip(
  label: String,
  isPrimary: Boolean,
  onClick: () -> Unit,
) {
  Surface(
    shape = PillShape,
    color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
    modifier = Modifier
      .clip(PillShape)
      .clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.ExtraBold,
      color = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer,
      maxLines = 1,
      softWrap = false,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
    )
  }
}

@Composable
private fun LoggedFoodEntryRow(
  entry: LoggedFoodEntry,
  onRemove: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(text = entry.badgeEmoji, fontSize = 20.sp)

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "${entry.formattedMultiplier} ${entry.name}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        Text(
          text = "${entry.mealSlot.shortLabel} • ${entry.formattedTime} • ${entry.proteinG}g P / ${entry.carbsG}g C / ${entry.fatsG}g F",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Surface(
        shape = PillShape,
        color = AccentEmerald.copy(alpha = 0.16f),
      ) {
        Text(
          text = "${entry.caloriesKcal} kcal",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.ExtraBold,
          color = AccentEmerald,
          maxLines = 1,
          softWrap = false,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
      }

      IconButton(
        onClick = onRemove,
        modifier = Modifier.size(30.dp),
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete logged item",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun NutritionInsightCard(
  tip: NutritionInsightTip,
) {
  val accentColor = when (tip.accentType) {
    1 -> AccentEmerald
    2 -> AccentAmber
    3 -> AccentCyan
    else -> GoogleBlue40
  }

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp),
          )
        }
        Text(
          text = tip.categoryBadge,
          style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
          fontWeight = FontWeight.ExtraBold,
          color = accentColor,
        )
      }

      Text(
        text = tip.title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = tip.body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Surface(
        shape = RoundedCornerShape(10.dp),
        color = accentColor.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(
          text = "💡 ${tip.actionHighlight}",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(10.dp),
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomFoodPresetDialog(
  initialPreset: FoodQuickPreset?,
  defaultSlot: MealSlot,
  onDismiss: () -> Unit,
  onSavePreset: (
    name: String,
    servingDesc: String,
    caloriesKcal: Int,
    proteinG: Int,
    carbsG: Int,
    fatsG: Int,
    mealSlot: MealSlot,
    badgeEmoji: String,
    alsoLogImmediately: Boolean,
  ) -> Unit,
) {
  var nameInput by remember { mutableStateOf(initialPreset?.name ?: "") }
  var servingInput by remember { mutableStateOf(initialPreset?.servingDesc ?: "1 serving") }
  var selectedEmoji by remember { mutableStateOf(initialPreset?.badgeEmoji ?: "🍲") }
  var selectedSlot by remember { mutableStateOf(initialPreset?.defaultMealSlot ?: defaultSlot) }

  var proteinInput by remember { mutableStateOf((initialPreset?.proteinG ?: 28).toString()) }
  var carbsInput by remember { mutableStateOf((initialPreset?.carbsG ?: 32).toString()) }
  var fatsInput by remember { mutableStateOf((initialPreset?.fatsG ?: 8).toString()) }
  var autoCalcCalories by remember { mutableStateOf(initialPreset == null) }
  var manualCaloriesInput by remember { mutableStateOf((initialPreset?.caloriesKcal ?: 312).toString()) }

  val parsedP = proteinInput.toIntOrNull()?.coerceIn(0, 250) ?: 0
  val parsedC = carbsInput.toIntOrNull()?.coerceIn(0, 400) ?: 0
  val parsedF = fatsInput.toIntOrNull()?.coerceIn(0, 200) ?: 0
  val macroCalculatedCalories = (parsedP * 4) + (parsedC * 4) + (parsedF * 9)
  val effectiveCalories = if (autoCalcCalories) {
    macroCalculatedCalories.coerceAtLeast(10)
  } else {
    manualCaloriesInput.toIntOrNull()?.coerceIn(10, 3500) ?: macroCalculatedCalories.coerceAtLeast(10)
  }

  val emojiChoices = listOf("🍲", "🥤", "🥚", "🍗", "🍛", "🥣", "🥗", "🫓", "🥩", "🐟", "🍌", "🥜", "🍨", "☕", "🥪", "🥑")
  val quickNameIdeas = listOf(
    Triple("Homemade Protein Shake", "1 shaker (350ml)", listOf(30, 12, 4)),
    Triple("4 Egg White + 2 Whole Omelette", "1 plate", listOf(26, 2, 11)),
    Triple("Chicken & Rice Prep Box", "250g bowl", listOf(42, 50, 9)),
    Triple("2 Rotis + Dal Tadka", "1 meal plate", listOf(18, 56, 10)),
    Triple("Paneer Bhurji / Tofu Bowl", "180g serving", listOf(24, 10, 16)),
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = if (initialPreset == null) "Create Custom Food Preset" else "Edit Food Preset",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
        )
        Text(
          text = "Save your regular meals for 1-tap calorie & macro logging",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Quick Template Ideas if creating new
        if (initialPreset == null) {
          Text(
            text = "1-Tap Quick Templates (Optional)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            quickNameIdeas.forEach { (ideaName, ideaServing, macros) ->
              FilterChip(
                selected = nameInput == ideaName,
                onClick = {
                  nameInput = ideaName
                  servingInput = ideaServing
                  proteinInput = macros[0].toString()
                  carbsInput = macros[1].toString()
                  fatsInput = macros[2].toString()
                  autoCalcCalories = true
                },
                label = {
                  Text(
                    text = ideaName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  )
                },
                shape = PillShape,
              )
            }
          }
        }

        OutlinedTextField(
          value = nameInput,
          onValueChange = { nameInput = it },
          label = { Text("Food / Meal Name") },
          placeholder = { Text("e.g., Post-Gym Protein Oats") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_preset_name_input"),
        )

        OutlinedTextField(
          value = servingInput,
          onValueChange = { servingInput = it },
          label = { Text("Serving Size Description") },
          placeholder = { Text("e.g., 1 bowl (250g) or 2 pieces") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_preset_serving_input"),
        )

        // Emoji Icon Picker
        Text(
          text = "Choose Icon Badge",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          emojiChoices.forEach { emoji ->
            val selected = emoji == selectedEmoji
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
              border = BorderStroke(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
              ),
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { selectedEmoji = emoji },
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 18.sp)
              }
            }
          }
        }

        // Default Meal Slot Picker
        Text(
          text = "Default Meal Slot",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          MealSlot.entries.forEach { slot ->
            FilterChip(
              selected = selectedSlot == slot,
              onClick = { selectedSlot = slot },
              label = {
                Text(
                  text = slot.shortLabel,
                  style = MaterialTheme.typography.labelSmall,
                )
              },
              shape = PillShape,
            )
          }
        }

        // Macros Row (Protein, Carbs, Fats)
        Text(
          text = "Macros per Serving (Grams)",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = proteinInput,
            onValueChange = { proteinInput = it.filter { ch -> ch.isDigit() }.take(3) },
            label = { Text("Protein (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("custom_preset_protein_input"),
          )
          OutlinedTextField(
            value = carbsInput,
            onValueChange = { carbsInput = it.filter { ch -> ch.isDigit() }.take(3) },
            label = { Text("Carbs (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("custom_preset_carbs_input"),
          )
          OutlinedTextField(
            value = fatsInput,
            onValueChange = { fatsInput = it.filter { ch -> ch.isDigit() }.take(3) },
            label = { Text("Fats (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("custom_preset_fats_input"),
          )
        }

        // Auto-Calculate Calories Toggle + Calorie Box
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceContainerLowest,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Auto-Calculate kcal from Macros",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "4×Protein + 4×Carbs + 9×Fats = $macroCalculatedCalories kcal",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Switch(
                checked = autoCalcCalories,
                onCheckedChange = {
                  autoCalcCalories = it
                  if (!it) {
                    manualCaloriesInput = macroCalculatedCalories.toString()
                  }
                },
              )
            }

            if (!autoCalcCalories) {
              OutlinedTextField(
                value = manualCaloriesInput,
                onValueChange = { manualCaloriesInput = it.filter { ch -> ch.isDigit() }.take(4) },
                label = { Text("Custom Calories (kcal)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )
            } else {
              Surface(
                shape = PillShape,
                color = AccentEmerald.copy(alpha = 0.16f),
              ) {
                Text(
                  text = "Prescribed Preset Energy: $effectiveCalories kcal / serving",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = AccentEmerald,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            onSavePreset(
              nameInput.ifBlank { "Custom Meal" },
              servingInput.ifBlank { "1 serving" },
              effectiveCalories,
              parsedP,
              parsedC,
              parsedF,
              selectedSlot,
              selectedEmoji,
              false,
            )
          },
          shape = PillShape,
          modifier = Modifier.testTag("save_preset_only_button"),
        ) {
          Text("Save Preset", fontWeight = FontWeight.Bold)
        }
        Button(
          onClick = {
            onSavePreset(
              nameInput.ifBlank { "Custom Meal" },
              servingInput.ifBlank { "1 serving" },
              effectiveCalories,
              parsedP,
              parsedC,
              parsedF,
              selectedSlot,
              selectedEmoji,
              true,
            )
          },
          shape = PillShape,
          modifier = Modifier.testTag("save_and_log_preset_button"),
        ) {
          Text("Save & Log 1×", fontWeight = FontWeight.ExtraBold)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
