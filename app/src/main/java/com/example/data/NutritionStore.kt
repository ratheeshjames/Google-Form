package com.example.data

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class MealSlot(
  val id: String,
  val label: String,
  val shortLabel: String,
  val typicalCalorieShare: Float,
) {
  BREAKFAST("breakfast", "Breakfast", "Breakfast", 0.24f),
  LUNCH("lunch", "Lunch", "Lunch", 0.30f),
  PRE_POST_WORKOUT("workout", "Pre / Post Workout", "Gym Fuel", 0.16f),
  DINNER("dinner", "Dinner", "Dinner", 0.22f),
  SNACK("snack", "Snacks & Drinks", "Snack", 0.08f);

  companion object {
    fun fromId(id: String?): MealSlot {
      return entries.find { it.id == id } ?: LUNCH
    }

    fun autoDetectForCurrentHour(): MealSlot {
      val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
      return when (hour) {
        in 5..10 -> BREAKFAST
        in 11..15 -> LUNCH
        in 16..18 -> PRE_POST_WORKOUT
        in 19..22 -> DINNER
        else -> SNACK
      }
    }
  }
}

enum class NutritionGoalMode(
  val id: String,
  val title: String,
  val shortBadge: String,
  val subtitle: String,
  val calorieAdjustmentKcal: Int,
  val proteinPerKgLeanMass: Float,
) {
  PRESCRIBED_AUTO(
    id = "prescribed_auto",
    title = "Auto Prescribed (3-Biometric)",
    shortBadge = "Auto Prescribed",
    subtitle = "Scientifically calibrated from your Height, Weight, Waist RFM% & today's training burn.",
    calorieAdjustmentKcal = 0,
    proteinPerKgLeanMass = 2.35f,
  ),
  AGGRESSIVE_CUT(
    id = "aggressive_cut",
    title = "Accelerated Fat Shred",
    shortBadge = "Fat Shred (-150)",
    subtitle = "Steeper calorie deficit with elevated protein (2.5g/kg LBM) to strip waist fat while sparing muscle.",
    calorieAdjustmentKcal = -150,
    proteinPerKgLeanMass = 2.50f,
  ),
  BODY_RECOMP(
    id = "body_recomp",
    title = "Lean Body Recomposition",
    shortBadge = "Recomp (+140)",
    subtitle = "Moderate deficit near maintenance to build dense dumbbell hypertrophy while trimming waist circumference.",
    calorieAdjustmentKcal = 140,
    proteinPerKgLeanMass = 2.35f,
  ),
  LEAN_SURPLUS(
    id = "lean_surplus",
    title = "Clean Muscle Hypertrophy",
    shortBadge = "Lean Bulk (+420)",
    subtitle = "Controlled caloric surplus above TDEE to maximize strength progression and muscular fullness.",
    calorieAdjustmentKcal = 420,
    proteinPerKgLeanMass = 2.15f,
  );

  companion object {
    fun fromId(id: String?): NutritionGoalMode {
      return entries.find { it.id == id } ?: PRESCRIBED_AUTO
    }
  }
}

data class FoodQuickPreset(
  val id: String,
  val name: String,
  val servingDesc: String,
  val caloriesKcal: Int,
  val proteinG: Int,
  val carbsG: Int,
  val fatsG: Int,
  val defaultMealSlot: MealSlot = MealSlot.LUNCH,
  val badgeEmoji: String = "🍽️",
  val isCustom: Boolean = false,
  val isFavorite: Boolean = false,
  val usageCount: Int = 0,
) {
  val proteinCaloriePct: Int
    get() = (((proteinG * 4f) / caloriesKcal.coerceAtLeast(1)) * 100f).roundToInt().coerceIn(0, 100)

  val isHighProtein: Boolean
    get() = proteinG >= 18 || proteinCaloriePct >= 30
}

data class LoggedFoodEntry(
  val id: String,
  val presetId: String?,
  val name: String,
  val servingDesc: String,
  val multiplier: Float,
  val caloriesKcal: Int,
  val proteinG: Int,
  val carbsG: Int,
  val fatsG: Int,
  val mealSlot: MealSlot,
  val badgeEmoji: String,
  val timestampEpochMs: Long,
  val dateKey: String,
) {
  val formattedTime: String
    get() = SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestampEpochMs))

  val formattedMultiplier: String
    get() = when {
      abs(multiplier - 1.0f) < 0.05f -> "1×"
      abs(multiplier - 0.5f) < 0.05f -> "½×"
      abs(multiplier - 1.5f) < 0.05f -> "1.5×"
      abs(multiplier - 2.0f) < 0.05f -> "2×"
      else -> String.format(Locale.US, "%.1f×", multiplier)
    }
}

data class NutritionInsightTip(
  val id: String,
  val categoryBadge: String,
  val title: String,
  val body: String,
  val actionHighlight: String,
  val accentType: Int, // 0 = Blue/Primary, 1 = Emerald/Success, 2 = Amber/Warning, 3 = Cyan/Hydration
)

data class PrescribedNutritionAnalysis(
  val goalMode: NutritionGoalMode,
  val bmrKcal: Int,
  val trainingBurnKcal: Int,
  val tdeeKcal: Int,
  val prescribedCaloriesKcal: Int,
  val prescribedProteinG: Int,
  val prescribedCarbsG: Int,
  val prescribedFatsG: Int,
  val consumedCaloriesKcal: Int,
  val consumedProteinG: Int,
  val consumedCarbsG: Int,
  val consumedFatsG: Int,
  val remainingCaloriesKcal: Int,
  val remainingProteinG: Int,
  val remainingCarbsG: Int,
  val remainingFatsG: Int,
  val calorieProgress: Float,
  val proteinProgress: Float,
  val carbsProgress: Float,
  val fatsProgress: Float,
  val waterMlToday: Int,
  val targetWaterMl: Int,
  val waterGlassesToday: Int,
  val targetWaterGlasses: Int,
  val waterProgress: Float,
  val estimatedRemainingMealsKcal: Int,
  val isUsingManualPredictionOverride: Boolean,
  val predictedEndOfDayCaloriesKcal: Int,
  val predictedDeltaFromPrescribedKcal: Int,
  val predictedDailyNetDeficitKcal: Int,
  val predictedWeeklyWeightChangeKg: Float,
  val predicted30DayWeightKg: Float,
  val isWithinPrescribedWindow: Boolean,
  val complianceStatusTitle: String,
  val complianceStatusSubtitle: String,
  val complianceZoneCode: Int, // 0 = Building/On-Track, 1 = Optimal Prescribed Window, 2 = Over Prescribed Target
  val dailyNutritionScorePct: Int,
  val unloggedMealSlots: List<MealSlot>,
  val insights: List<NutritionInsightTip>,
)

data class NutritionState(
  val presets: List<FoodQuickPreset> = emptyList(),
  val todayEntries: List<LoggedFoodEntry> = emptyList(),
  val waterMlToday: Int = 0,
  val goalMode: NutritionGoalMode = NutritionGoalMode.PRESCRIBED_AUTO,
  val manualRemainingCaloriesOverride: Int? = null,
  val currentDateKey: String = "",
)

object NutritionStore {
  private const val PREFS_NAME = "google_form_nutrition_store_v2"
  private var prefs: SharedPreferences? = null

  private val defaultStarterPresets = listOf(
    FoodQuickPreset(
      id = "preset_whey_isolate",
      name = "Whey Protein Shake",
      servingDesc = "1 scoop (32g) in water",
      caloriesKcal = 130,
      proteinG = 25,
      carbsG = 3,
      fatsG = 2,
      defaultMealSlot = MealSlot.PRE_POST_WORKOUT,
      badgeEmoji = "🥤",
      isCustom = false,
      isFavorite = true,
      usageCount = 5,
    ),
    FoodQuickPreset(
      id = "preset_boiled_eggs_3",
      name = "3 Whole Boiled Eggs",
      servingDesc = "3 large eggs (150g)",
      caloriesKcal = 215,
      proteinG = 18,
      carbsG = 1,
      fatsG = 15,
      defaultMealSlot = MealSlot.BREAKFAST,
      badgeEmoji = "🥚",
      isCustom = false,
      isFavorite = true,
      usageCount = 4,
    ),
    FoodQuickPreset(
      id = "preset_grilled_chicken_breast",
      name = "Grilled Chicken Breast",
      servingDesc = "160g cooked breast",
      caloriesKcal = 265,
      proteinG = 48,
      carbsG = 0,
      fatsG = 6,
      defaultMealSlot = MealSlot.LUNCH,
      badgeEmoji = "🍗",
      isCustom = false,
      isFavorite = true,
      usageCount = 4,
    ),
    FoodQuickPreset(
      id = "preset_oats_banana_bowl",
      name = "Rolled Oats & Banana Bowl",
      servingDesc = "60g oats + 1 banana + skim milk",
      caloriesKcal = 360,
      proteinG = 14,
      carbsG = 64,
      fatsG = 6,
      defaultMealSlot = MealSlot.BREAKFAST,
      badgeEmoji = "🥣",
      isCustom = false,
      isFavorite = false,
      usageCount = 3,
    ),
    FoodQuickPreset(
      id = "preset_rice_dal_chicken",
      name = "Rice, Dal & Chicken Plate",
      servingDesc = "150g rice + 1 bowl dal + 100g chicken",
      caloriesKcal = 520,
      proteinG = 38,
      carbsG = 62,
      fatsG = 12,
      defaultMealSlot = MealSlot.LUNCH,
      badgeEmoji = "🍛",
      isCustom = false,
      isFavorite = true,
      usageCount = 3,
    ),
    FoodQuickPreset(
      id = "preset_greek_yogurt_berries",
      name = "High-Protein Greek Yogurt",
      servingDesc = "200g cup + berries",
      caloriesKcal = 170,
      proteinG = 20,
      carbsG = 14,
      fatsG = 3,
      defaultMealSlot = MealSlot.SNACK,
      badgeEmoji = "🍨",
      isCustom = false,
      isFavorite = false,
      usageCount = 2,
    ),
    FoodQuickPreset(
      id = "preset_chapati_paneer_bhurji",
      name = "2 Multigrain Rotis + Paneer",
      servingDesc = "2 rotis + 120g low-fat paneer bhurji",
      caloriesKcal = 430,
      proteinG = 26,
      carbsG = 44,
      fatsG = 16,
      defaultMealSlot = MealSlot.DINNER,
      badgeEmoji = "🫓",
      isCustom = false,
      isFavorite = false,
      usageCount = 2,
    ),
    FoodQuickPreset(
      id = "preset_tuna_egg_salad",
      name = "Lean Tuna / Egg White Salad",
      servingDesc = "1 large bowl (280g)",
      caloriesKcal = 240,
      proteinG = 34,
      carbsG = 9,
      fatsG = 7,
      defaultMealSlot = MealSlot.DINNER,
      badgeEmoji = "🥗",
      isCustom = false,
      isFavorite = false,
      usageCount = 1,
    ),
    FoodQuickPreset(
      id = "preset_almonds_walnuts",
      name = "Roasted Almonds & Walnuts",
      servingDesc = "1 handful (28g)",
      caloriesKcal = 175,
      proteinG = 6,
      carbsG = 5,
      fatsG = 15,
      defaultMealSlot = MealSlot.SNACK,
      badgeEmoji = "🥜",
      isCustom = false,
      isFavorite = false,
      usageCount = 1,
    ),
    FoodQuickPreset(
      id = "preset_black_coffee_banana",
      name = "Pre-Workout Coffee + Banana",
      servingDesc = "1 espresso + 1 medium banana",
      caloriesKcal = 105,
      proteinG = 1,
      carbsG = 27,
      fatsG = 0,
      defaultMealSlot = MealSlot.PRE_POST_WORKOUT,
      badgeEmoji = "☕",
      isCustom = false,
      isFavorite = false,
      usageCount = 2,
    ),
  )

  private val _state = MutableStateFlow(
    NutritionState(
      presets = defaultStarterPresets,
      todayEntries = emptyList(),
      waterMlToday = 0,
      goalMode = NutritionGoalMode.PRESCRIBED_AUTO,
      manualRemainingCaloriesOverride = null,
      currentDateKey = todayDateKey(),
    )
  )
  val state: StateFlow<NutritionState> = _state.asStateFlow()

  fun todayDateKey(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
  }

  fun initialize(context: Context) {
    val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs = sp
    val today = todayDateKey()
    val savedDate = sp.getString("last_active_date_key", today) ?: today

    val loadedPresets = deserializePresets(sp.getString("saved_food_presets_v2", null))
      .ifEmpty { defaultStarterPresets }

    val allEntries = deserializeEntries(sp.getString("logged_food_entries_v2", null))
    val todayEntries = allEntries.filter { it.dateKey == today }

    val waterMl = if (savedDate == today) {
      sp.getInt("water_ml_today", AthleteProfileStore.activeProfile.value.waterGlassesToday * 250)
    } else {
      0
    }

    val goalMode = NutritionGoalMode.fromId(sp.getString("nutrition_goal_mode", null))

    _state.value = NutritionState(
      presets = sortPresets(loadedPresets),
      todayEntries = todayEntries.sortedByDescending { it.timestampEpochMs },
      waterMlToday = waterMl.coerceIn(0, 6000),
      goalMode = goalMode,
      manualRemainingCaloriesOverride = null,
      currentDateKey = today,
    )

    if (savedDate != today) {
      sp.edit()
        .putString("last_active_date_key", today)
        .putInt("water_ml_today", 0)
        .apply()
      AthleteProfileStore.updateActiveProfile(context) { it.copy(waterGlassesToday = 0) }
    }
  }

  private fun sortPresets(list: List<FoodQuickPreset>): List<FoodQuickPreset> {
    return list.sortedWith(
      compareByDescending<FoodQuickPreset> { it.isCustom }
        .thenByDescending { it.isFavorite }
        .thenByDescending { it.usageCount }
        .thenBy { it.name }
    )
  }

  private fun persistState(context: Context? = null) {
    if (prefs == null && context != null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    val current = _state.value
    val today = todayDateKey()
    prefs?.edit()?.apply {
      putString("last_active_date_key", today)
      putString("saved_food_presets_v2", serializePresets(current.presets))
      putString("logged_food_entries_v2", serializeEntries(current.todayEntries))
      putInt("water_ml_today", current.waterMlToday)
      putString("nutrition_goal_mode", current.goalMode.id)
      apply()
    }
  }

  fun addCustomFoodPreset(
    name: String,
    servingDesc: String,
    caloriesKcal: Int,
    proteinG: Int,
    carbsG: Int,
    fatsG: Int,
    defaultMealSlot: MealSlot,
    badgeEmoji: String,
    alsoLogImmediately: Boolean = false,
    context: Context? = null,
  ): FoodQuickPreset {
    val cleanName = name.trim().replace("|", " ").replace("~", " ").ifBlank { "Custom Meal" }
    val cleanServing = servingDesc.trim().replace("|", " ").replace("~", " ").ifBlank { "1 serving" }
    val cleanProtein = proteinG.coerceIn(0, 250)
    val cleanCarbs = carbsG.coerceIn(0, 400)
    val cleanFats = fatsG.coerceIn(0, 200)
    val macroDerivedCalories = cleanProtein * 4 + cleanCarbs * 4 + cleanFats * 9
    val cleanCalories = if (caloriesKcal > 0) caloriesKcal.coerceIn(10, 3500) else macroDerivedCalories.coerceAtLeast(15)

    val newPreset = FoodQuickPreset(
      id = "custom_${System.currentTimeMillis()}",
      name = cleanName,
      servingDesc = cleanServing,
      caloriesKcal = cleanCalories,
      proteinG = cleanProtein,
      carbsG = cleanCarbs,
      fatsG = cleanFats,
      defaultMealSlot = defaultMealSlot,
      badgeEmoji = badgeEmoji.ifBlank { "🍲" },
      isCustom = true,
      isFavorite = true,
      usageCount = if (alsoLogImmediately) 1 else 0,
    )

    val updatedPresets = sortPresets(listOf(newPreset) + _state.value.presets)
    _state.value = _state.value.copy(presets = updatedPresets)
    persistState(context)

    if (alsoLogImmediately) {
      quickLogPreset(
        preset = newPreset,
        multiplier = 1.0f,
        mealSlot = defaultMealSlot,
        context = context,
      )
    } else {
      AthleteProfileStore.awardWorkoutXp(xpAmount = 20, incrementWorkout = false, context = context)
    }

    return newPreset
  }

  fun updateFoodPreset(
    updatedPreset: FoodQuickPreset,
    context: Context? = null,
  ) {
    val list = _state.value.presets.map { existing ->
      if (existing.id == updatedPreset.id) updatedPreset else existing
    }
    _state.value = _state.value.copy(presets = sortPresets(list))
    persistState(context)
  }

  fun deleteFoodPreset(
    presetId: String,
    context: Context? = null,
  ) {
    val filtered = _state.value.presets.filterNot { it.id == presetId }
    _state.value = _state.value.copy(presets = sortPresets(filtered))
    persistState(context)
  }

  fun toggleFavoritePreset(
    presetId: String,
    context: Context? = null,
  ) {
    val updated = _state.value.presets.map { preset ->
      if (preset.id == presetId) preset.copy(isFavorite = !preset.isFavorite) else preset
    }
    _state.value = _state.value.copy(presets = sortPresets(updated))
    persistState(context)
  }

  fun quickLogPreset(
    preset: FoodQuickPreset,
    multiplier: Float = 1.0f,
    mealSlot: MealSlot = preset.defaultMealSlot,
    context: Context? = null,
  ): LoggedFoodEntry {
    val safeMult = multiplier.coerceIn(0.25f, 5.0f)
    val entry = LoggedFoodEntry(
      id = "entry_${System.currentTimeMillis()}_${(100..999).random()}",
      presetId = preset.id,
      name = preset.name,
      servingDesc = preset.servingDesc,
      multiplier = safeMult,
      caloriesKcal = (preset.caloriesKcal * safeMult).roundToInt(),
      proteinG = (preset.proteinG * safeMult).roundToInt(),
      carbsG = (preset.carbsG * safeMult).roundToInt(),
      fatsG = (preset.fatsG * safeMult).roundToInt(),
      mealSlot = mealSlot,
      badgeEmoji = preset.badgeEmoji,
      timestampEpochMs = System.currentTimeMillis(),
      dateKey = todayDateKey(),
    )

    val updatedPresets = _state.value.presets.map {
      if (it.id == preset.id) it.copy(usageCount = it.usageCount + 1) else it
    }
    val updatedEntries = listOf(entry) + _state.value.todayEntries

    _state.value = _state.value.copy(
      presets = sortPresets(updatedPresets),
      todayEntries = updatedEntries,
    )
    persistState(context)

    AthleteProfileStore.awardWorkoutXp(xpAmount = 15, incrementWorkout = false, context = context)
    return entry
  }

  fun removeLoggedEntry(
    entryId: String,
    context: Context? = null,
  ) {
    val updated = _state.value.todayEntries.filterNot { it.id == entryId }
    _state.value = _state.value.copy(todayEntries = updated)
    persistState(context)
  }

  fun clearTodayEntries(context: Context? = null) {
    _state.value = _state.value.copy(todayEntries = emptyList())
    persistState(context)
  }

  fun addWaterMl(
    deltaMl: Int,
    context: Context? = null,
  ) {
    val updatedMl = (_state.value.waterMlToday + deltaMl).coerceIn(0, 6000)
    val glasses = (updatedMl / 250f).roundToInt()
    _state.value = _state.value.copy(waterMlToday = updatedMl)
    persistState(context)

    AthleteProfileStore.updateActiveProfile(context) { current ->
      current.copy(
        waterGlassesToday = glasses,
        xp = if (deltaMl > 0) current.xp + 5 else current.xp,
      )
    }
  }

  fun setGoalMode(
    mode: NutritionGoalMode,
    context: Context? = null,
  ) {
    _state.value = _state.value.copy(goalMode = mode)
    persistState(context)
  }

  fun setManualRemainingCaloriesOverride(overrideKcal: Int?) {
    _state.value = _state.value.copy(
      manualRemainingCaloriesOverride = overrideKcal?.coerceIn(0, 2200)
    )
  }

  /**
   * Comprehensive Prescribed Calorie, Macro, Hydration & End-of-Day Weight Impact Analysis
   */
  fun analyzeForProfile(
    profile: AthleteProfile,
    nutritionState: NutritionState = _state.value,
  ): PrescribedNutritionAnalysis {
    val bmr = (370f + 21.6f * profile.leanBodyMassKg).roundToInt()
    val todayDay = AthleteProfileStore.getAutoDetectedWorkoutDay()
    val trainingBurn = if (todayDay == 7) {
      (profile.cardioDurationMin * (5.8f + profile.cardioDifficultyLevel * 0.95f) * (profile.weightKg / 75f)).roundToInt()
    } else {
      (profile.sessionDurationMin * (5.2f + profile.workoutDifficultyLevel * 0.85f) * (profile.weightKg / 75f)).roundToInt()
    }

    val tdee = (bmr * 1.38f + trainingBurn).roundToInt()

    val basePrescribedKcal = profile.autoDailyCaloriesKcal + (trainingBurn - 180).coerceAtLeast(0)
    val prescribedCalories = (basePrescribedKcal + nutritionState.goalMode.calorieAdjustmentKcal).coerceIn(1350, 4200)

    val prescribedProtein = (profile.leanBodyMassKg * nutritionState.goalMode.proteinPerKgLeanMass)
      .roundToInt()
      .coerceIn(110, 260)
    val prescribedFats = (profile.weightKg * 0.82f).roundToInt().coerceIn(45, 120)
    val remainingCalsForCarbs = (prescribedCalories - (prescribedProtein * 4 + prescribedFats * 9)).coerceAtLeast(360)
    val prescribedCarbs = (remainingCalsForCarbs / 4f).roundToInt()

    val entries = nutritionState.todayEntries
    val consumedCalories = entries.sumOf { it.caloriesKcal }
    val consumedProtein = entries.sumOf { it.proteinG }
    val consumedCarbs = entries.sumOf { it.carbsG }
    val consumedFats = entries.sumOf { it.fatsG }

    val remainingCalories = prescribedCalories - consumedCalories
    val remainingProtein = (prescribedProtein - consumedProtein).coerceAtLeast(0)
    val remainingCarbs = (prescribedCarbs - consumedCarbs).coerceAtLeast(0)
    val remainingFats = (prescribedFats - consumedFats).coerceAtLeast(0)

    val calProgress = (consumedCalories.toFloat() / prescribedCalories.coerceAtLeast(1)).coerceIn(0f, 1.5f)
    val protProgress = (consumedProtein.toFloat() / prescribedProtein.coerceAtLeast(1)).coerceIn(0f, 1.5f)
    val carbProgress = (consumedCarbs.toFloat() / prescribedCarbs.coerceAtLeast(1)).coerceIn(0f, 1.5f)
    val fatProgress = (consumedFats.toFloat() / prescribedFats.coerceAtLeast(1)).coerceIn(0f, 1.5f)

    // Hydration target: 42 ml per kg body weight + extra sweat replacement for longer sessions
    val workoutWaterBonusMl = ((if (todayDay == 7) profile.cardioDurationMin else profile.sessionDurationMin) * 8)
    val targetWaterMl = ((profile.weightKg * 40f).roundToInt() + workoutWaterBonusMl).coerceIn(2250, 4750)
    val targetGlasses = (targetWaterMl / 250f).roundToInt().coerceAtLeast(9)
    val waterMl = nutritionState.waterMlToday
    val waterGlasses = (waterMl / 250f).roundToInt()
    val waterProgress = (waterMl.toFloat() / targetWaterMl.coerceAtLeast(1)).coerceIn(0f, 1.25f)

    // Detect which major meal slots have not been logged yet today
    val loggedSlots = entries.map { it.mealSlot }.toSet()
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val unloggedFutureSlots = MealSlot.entries.filter { slot ->
      !loggedSlots.contains(slot) && when (slot) {
        MealSlot.BREAKFAST -> currentHour < 11
        MealSlot.LUNCH -> currentHour < 16
        MealSlot.PRE_POST_WORKOUT -> currentHour < 20
        MealSlot.DINNER -> currentHour < 22
        MealSlot.SNACK -> currentHour < 21
      }
    }

    // Auto-estimate remaining intake if the user hasn't manually moved the What-If simulator slider
    val autoEstimatedRemainingKcal = if (entries.isEmpty()) {
      prescribedCalories
    } else {
      val shareLeft = unloggedFutureSlots.sumOf { it.typicalCalorieShare.toDouble() }.toFloat()
      val avgCalPerLoggedMeal = (consumedCalories.toFloat() / entries.size.coerceAtLeast(1))
      val paceEstimate = (prescribedCalories * shareLeft * 0.95f).roundToInt()
      if (shareLeft <= 0.01f) {
        0
      } else {
        ((paceEstimate * 0.7f) + (avgCalPerLoggedMeal * unloggedFutureSlots.size.coerceAtMost(2) * 0.3f))
          .roundToInt()
          .coerceIn(0, 1800)
      }
    }

    val plannedRemainingKcal = nutritionState.manualRemainingCaloriesOverride ?: autoEstimatedRemainingKcal
    val predictedEndOfDayKcal = consumedCalories + plannedRemainingKcal
    val predictedDeltaFromPrescribed = predictedEndOfDayKcal - prescribedCalories
    val predictedNetDeficit = tdee - predictedEndOfDayKcal
    val predictedWeeklyWeightChangeKg = -((predictedNetDeficit * 7f) / 7700f * 100f).roundToInt() / 100f
    val predicted30DayWeightKg = ((profile.weightKg + predictedWeeklyWeightChangeKg * 4.28f) * 10f).roundToInt() / 10f

    val isWithinPrescribed = predictedDeltaFromPrescribed in -260..140 && consumedCalories <= prescribedCalories + 140

    val (statusTitle, statusSubtitle, zoneCode) = when {
      consumedCalories > prescribedCalories + 140 -> Triple(
        "Over Prescribed Target (+${consumedCalories - prescribedCalories} kcal)",
        "You have exceeded today's ${prescribedCalories} kcal prescription. Prioritize hydration and a brisk 15-min walk.",
        2,
      )
      consumedCalories in (prescribedCalories - 220)..(prescribedCalories + 140) && plannedRemainingKcal == 0 -> Triple(
        "Within Prescribed Target Window ✅",
        "Spot-on precision! You are right inside your ${prescribedCalories} kcal transformation window.",
        1,
      )
      predictedDeltaFromPrescribed > 150 -> Triple(
        "Projected +${predictedDeltaFromPrescribed} kcal Over Target",
        "At your current meal pace (${predictedEndOfDayKcal} kcal EOD), trim ~${predictedDeltaFromPrescribed} kcal from your next meal to stay in zone.",
        2,
      )
      entries.isEmpty() -> Triple(
        "Ready to Fuel • ${prescribedCalories} kcal Prescribed",
        "Tap any Custom Quick Preset below to log your first meal and activate live EOD calorie prediction.",
        0,
      )
      else -> Triple(
        "On Track • ${remainingCalories.coerceAtLeast(0)} kcal Remaining",
        "Projected EOD: ${predictedEndOfDayKcal} kcal (${if (predictedNetDeficit >= 0) "-$predictedNetDeficit kcal deficit" else "+${abs(predictedNetDeficit)} kcal surplus"} vs TDEE).",
        if (isWithinPrescribed) 1 else 0,
      )
    }

    // Composite Daily Nutrition Score (0..100%)
    val calorieAccuracyScore = when {
      entries.isEmpty() -> 25f
      abs(consumedCalories - prescribedCalories) <= 150 -> 100f
      consumedCalories < prescribedCalories -> (consumedCalories.toFloat() / prescribedCalories * 95f).coerceIn(25f, 95f)
      else -> (100f - ((consumedCalories - prescribedCalories) / 12f)).coerceIn(30f, 90f)
    }
    val proteinScore = (protProgress.coerceAtMost(1.0f) * 100f)
    val waterScore = (waterProgress.coerceAtMost(1.0f) * 100f)
    val dailyScore = (calorieAccuracyScore * 0.45f + proteinScore * 0.35f + waterScore * 0.20f).roundToInt().coerceIn(10, 100)

    // Generate context-aware insights & tips
    val insights = buildDynamicInsights(
      profile = profile,
      todayDay = todayDay,
      prescribedCalories = prescribedCalories,
      consumedCalories = consumedCalories,
      remainingCalories = remainingCalories,
      prescribedProtein = prescribedProtein,
      consumedProtein = consumedProtein,
      remainingProtein = remainingProtein,
      waterMl = waterMl,
      targetWaterMl = targetWaterMl,
      predictedWeeklyWeightChangeKg = predictedWeeklyWeightChangeKg,
    )

    return PrescribedNutritionAnalysis(
      goalMode = nutritionState.goalMode,
      bmrKcal = bmr,
      trainingBurnKcal = trainingBurn,
      tdeeKcal = tdee,
      prescribedCaloriesKcal = prescribedCalories,
      prescribedProteinG = prescribedProtein,
      prescribedCarbsG = prescribedCarbs,
      prescribedFatsG = prescribedFats,
      consumedCaloriesKcal = consumedCalories,
      consumedProteinG = consumedProtein,
      consumedCarbsG = consumedCarbs,
      consumedFatsG = consumedFats,
      remainingCaloriesKcal = remainingCalories,
      remainingProteinG = remainingProtein,
      remainingCarbsG = remainingCarbs,
      remainingFatsG = remainingFats,
      calorieProgress = calProgress,
      proteinProgress = protProgress,
      carbsProgress = carbProgress,
      fatsProgress = fatProgress,
      waterMlToday = waterMl,
      targetWaterMl = targetWaterMl,
      waterGlassesToday = waterGlasses,
      targetWaterGlasses = targetGlasses,
      waterProgress = waterProgress,
      estimatedRemainingMealsKcal = plannedRemainingKcal,
      isUsingManualPredictionOverride = nutritionState.manualRemainingCaloriesOverride != null,
      predictedEndOfDayCaloriesKcal = predictedEndOfDayKcal,
      predictedDeltaFromPrescribedKcal = predictedDeltaFromPrescribed,
      predictedDailyNetDeficitKcal = predictedNetDeficit,
      predictedWeeklyWeightChangeKg = predictedWeeklyWeightChangeKg,
      predicted30DayWeightKg = predicted30DayWeightKg,
      isWithinPrescribedWindow = isWithinPrescribed,
      complianceStatusTitle = statusTitle,
      complianceStatusSubtitle = statusSubtitle,
      complianceZoneCode = zoneCode,
      dailyNutritionScorePct = dailyScore,
      unloggedMealSlots = unloggedFutureSlots,
      insights = insights,
    )
  }

  private fun buildDynamicInsights(
    profile: AthleteProfile,
    todayDay: Int,
    prescribedCalories: Int,
    consumedCalories: Int,
    remainingCalories: Int,
    prescribedProtein: Int,
    consumedProtein: Int,
    remainingProtein: Int,
    waterMl: Int,
    targetWaterMl: Int,
    predictedWeeklyWeightChangeKg: Float,
  ): List<NutritionInsightTip> {
    val tips = mutableListOf<NutritionInsightTip>()

    // 1. Protein Synthesis & Muscle Sparing Tip
    if (remainingProtein > 35) {
      val scoopsNeeded = max(1, (remainingProtein / 25f).roundToInt())
      tips.add(
        NutritionInsightTip(
          id = "tip_protein_gap",
          categoryBadge = "PROTEIN SYNTHESIS • ${remainingProtein}g LEFT",
          title = "Hit Your ${prescribedProtein}g Lean-Mass Protection Target",
          body = "With ${String.format(Locale.US, "%.1f", profile.leanBodyMassKg)} kg Lean Body Mass, eating ${prescribedProtein}g protein prevents muscle catabolism while trimming waist fat.",
          actionHighlight = "Quick Fix: Log ${scoopsNeeded}× Whey Shake or 160g Grilled Chicken Breast to close ${remainingProtein}g gap.",
          accentType = 0,
        )
      )
    } else {
      tips.add(
        NutritionInsightTip(
          id = "tip_protein_locked",
          categoryBadge = "ANABOLIC SHIELD • ON TARGET",
          title = "Muscle Protein Synthesis Fully Primed",
          body = "You have already locked in ${consumedProtein}g / ${prescribedProtein}g protein today, protecting your FFMI (${profile.ffmi}) during fat loss.",
          actionHighlight = "Spread remaining protein evenly before sleep for overnight myofibrillar repair.",
          accentType = 1,
        )
      )
    }

    // 2. Calorie Density & Prescribed Budget Tip
    if (remainingCalories > 250) {
      val proteinRatioNeeded = if (remainingCalories > 0) ((remainingProtein * 4f / remainingCalories) * 100f).roundToInt() else 30
      tips.add(
        NutritionInsightTip(
          id = "tip_calorie_budget",
          categoryBadge = "CALORIE BUDGET • ${remainingCalories} KCAL OPEN",
          title = if (proteinRatioNeeded >= 35) "High-Protein Density Required for Remaining Meals" else "Balanced Satiety Window Open",
          body = "You have ${remainingCalories} kcal and ${remainingProtein}g protein left inside your ${prescribedCalories} kcal prescription.",
          actionHighlight = if (proteinRatioNeeded >= 35) {
            "Choose lean presets (>35% protein calories like Egg Whites, Tuna, Chicken, or Greek Yogurt) to hit protein without overshooting kcal."
          } else {
            "At this pace, your projected weekly weight change is ${String.format(Locale.US, "%+.2f", predictedWeeklyWeightChangeKg)} kg/week."
          },
          accentType = if (proteinRatioNeeded >= 40) 2 else 1,
        )
      )
    } else if (remainingCalories < -120) {
      tips.add(
        NutritionInsightTip(
          id = "tip_calorie_over",
          categoryBadge = "SURPLUS ALERT • +${abs(remainingCalories)} KCAL",
          title = "Offset Surplus with NEAT & Zero-Calorie Hydration",
          body = "You are ${abs(remainingCalories)} kcal above today's prescribed target. Avoid liquid calories for the rest of the evening.",
          actionHighlight = "A 20-minute brisk incline walk oxidizes ~140 kcal and restores your daily net deficit.",
          accentType = 2,
        )
      )
    }

    // 3. Training-Day Specific Fueling Insight
    val workoutTip = when (todayDay) {
      3, 6 -> NutritionInsightTip(
        id = "tip_leg_day_glycogen",
        categoryBadge = "DAY $todayDay LEG & CORE FUELING",
        title = "Prioritize Complex Carbs 60–90 Min Around Leg Volume",
        body = "Squats, Lunges, and Deadlifts deplete quadriceps and glute glycogen faster than upper-body days.",
        actionHighlight = "Place 40–60g of your prescribed carbs (Oats, Rice, or Banana) around your workout window.",
        accentType = 0,
      )
      7 -> NutritionInsightTip(
        id = "tip_sunday_recovery_nutrition",
        categoryBadge = "SUNDAY CARDIO & CNS RECOVERY",
        title = "Anti-Inflammatory Fats & Electrolyte Flush",
        body = "Sunday's Zone-2 Treadmill & Myofascial session clears inter-fiber edema accumulated from Mon–Sat lifting.",
        actionHighlight = "Include Omega-3 fats (Walnuts/Fish) and 500ml extra water to accelerate Monday strength readiness.",
        accentType = 3,
      )
      else -> NutritionInsightTip(
        id = "tip_upper_hypertrophy",
        categoryBadge = "DAY $todayDay HYPERTROPHY WINDOW",
        title = "Leucine Trigger Post-Dumbbell Session",
        body = "Your ${profile.sessionDurationMin}-min ${profile.difficultyShortBadge} dumbbell session elevates muscle protein synthesis for 24–36 hours.",
        actionHighlight = "Aim for 30–40g fast-digesting protein within 90 minutes of finishing your final set.",
        accentType = 0,
      )
    }
    tips.add(workoutTip)

    // 4. Hydration & Lipolysis Insight
    val waterRemainingMl = (targetWaterMl - waterMl).coerceAtLeast(0)
    tips.add(
      NutritionInsightTip(
        id = "tip_hydration_lipolysis",
        categoryBadge = "HYDRATION • ${waterMl} / ${targetWaterMl} ML",
        title = if (waterRemainingMl > 600) "Hepatic Fat Oxidation Needs ${waterRemainingMl} ml More Water" else "Optimal Cellular Hydration Achieved",
        body = "Even 2% dehydration reduces dumbbell rep endurance by ~8% and slows hepatic lipolysis (fat mobilization).",
        actionHighlight = if (waterRemainingMl > 0) {
          "Drink ${(waterRemainingMl / 250f).roundToInt().coerceAtLeast(1)} more 250ml glasses spaced across the next 4 hours."
        } else {
          "Hydration goal complete! Your cellular volume and joint lubrication are at 100%."
        },
        accentType = 3,
      )
    )

    return tips
  }

  private fun serializePresets(presets: List<FoodQuickPreset>): String {
    return presets.joinToString("||") { p ->
      listOf(
        p.id,
        p.name.replace("~", " ").replace("|", " "),
        p.servingDesc.replace("~", " ").replace("|", " "),
        p.caloriesKcal.toString(),
        p.proteinG.toString(),
        p.carbsG.toString(),
        p.fatsG.toString(),
        p.defaultMealSlot.id,
        p.badgeEmoji,
        p.isCustom.toString(),
        p.isFavorite.toString(),
        p.usageCount.toString(),
      ).joinToString("~")
    }
  }

  private fun deserializePresets(raw: String?): List<FoodQuickPreset> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.split("||").mapNotNull { token ->
      val p = token.split("~")
      if (p.size >= 12) {
        FoodQuickPreset(
          id = p[0],
          name = p[1],
          servingDesc = p[2],
          caloriesKcal = p[3].toIntOrNull() ?: 150,
          proteinG = p[4].toIntOrNull() ?: 10,
          carbsG = p[5].toIntOrNull() ?: 15,
          fatsG = p[6].toIntOrNull() ?: 5,
          defaultMealSlot = MealSlot.fromId(p[7]),
          badgeEmoji = p[8].ifBlank { "🍽️" },
          isCustom = p[9].toBooleanStrictOrNull() ?: false,
          isFavorite = p[10].toBooleanStrictOrNull() ?: false,
          usageCount = p[11].toIntOrNull() ?: 0,
        )
      } else {
        null
      }
    }
  }

  private fun serializeEntries(entries: List<LoggedFoodEntry>): String {
    return entries.joinToString("||") { e ->
      listOf(
        e.id,
        e.presetId ?: "",
        e.name.replace("~", " ").replace("|", " "),
        e.servingDesc.replace("~", " ").replace("|", " "),
        e.multiplier.toString(),
        e.caloriesKcal.toString(),
        e.proteinG.toString(),
        e.carbsG.toString(),
        e.fatsG.toString(),
        e.mealSlot.id,
        e.badgeEmoji,
        e.timestampEpochMs.toString(),
        e.dateKey,
      ).joinToString("~")
    }
  }

  private fun deserializeEntries(raw: String?): List<LoggedFoodEntry> {
    if (raw.isNullOrBlank()) return emptyList()
    return raw.split("||").mapNotNull { token ->
      val p = token.split("~")
      if (p.size >= 13) {
        LoggedFoodEntry(
          id = p[0],
          presetId = p[1].ifBlank { null },
          name = p[2],
          servingDesc = p[3],
          multiplier = p[4].toFloatOrNull() ?: 1.0f,
          caloriesKcal = p[5].toIntOrNull() ?: 0,
          proteinG = p[6].toIntOrNull() ?: 0,
          carbsG = p[7].toIntOrNull() ?: 0,
          fatsG = p[8].toIntOrNull() ?: 0,
          mealSlot = MealSlot.fromId(p[9]),
          badgeEmoji = p[10].ifBlank { "🍽️" },
          timestampEpochMs = p[11].toLongOrNull() ?: System.currentTimeMillis(),
          dateKey = p[12],
        )
      } else {
        null
      }
    }
  }
}
