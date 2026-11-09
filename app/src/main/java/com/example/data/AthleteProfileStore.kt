package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.widget.GoogleFormStreakWidgetProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 12 Granular Evolution Stages of the 2D Illustrated Warrior Rooster:
 * Progresses gradually across the entire transformation timeline from a Sanctuary Egg (Stage 1)
 * through pipping, hatching, fledgling, cockerel, and conditioning phases into an
 * Apex Muscular Warrior Rooster (Stage 12) when the user reaches their goal.
 */
enum class ChickenEvolutionStage(
  val stageIndex: Int,
  val stageNumber: Int,
  val badge: String,
  val shortLabel: String,
  val title: String,
  val subtitle: String,
  val minProgress: Float,
  val maxProgress: Float,
) {
  STAGE_01_SANCTUARY_EGG(
    stageIndex = 0,
    stageNumber = 1,
    badge = "Stage 1/12 • Fresh Egg",
    shortLabel = "Egg",
    title = "Stage 1 • Sanctuary Egg",
    subtitle = "Warm farm-fresh egg resting in the sanctuary nest as your journey begins",
    minProgress = 0.00f,
    maxProgress = 0.06f,
  ),
  STAGE_02_INCUBATING_EGG(
    stageIndex = 1,
    stageNumber = 2,
    badge = "Stage 2/12 • Incubating",
    shortLabel = "Warming",
    title = "Stage 2 • Incubating Egg",
    subtitle = "Steady training builds inner warmth; first subtle hairline seam appears",
    minProgress = 0.06f,
    maxProgress = 0.13f,
  ),
  STAGE_03_PIPPING_SHELL(
    stageIndex = 2,
    stageNumber = 3,
    badge = "Stage 3/12 • Pipping Shell",
    shortLabel = "Pipping",
    title = "Stage 3 • Pipping Shell",
    subtitle = "Star-shaped pip fracture forms on the upper shell as life stirs within",
    minProgress = 0.13f,
    maxProgress = 0.20f,
  ),
  STAGE_04_HATCHING_CROWN(
    stageIndex = 3,
    stageNumber = 4,
    badge = "Stage 4/12 • Hatching",
    shortLabel = "Hatching",
    title = "Stage 4 • Hatching Chick",
    subtitle = "Top shell cap lifts as a golden chick peeks out into the light",
    minProgress = 0.20f,
    maxProgress = 0.28f,
  ),
  STAGE_05_DOWNY_HATCHLING(
    stageIndex = 4,
    stageNumber = 5,
    badge = "Stage 5/12 • Hatchling",
    shortLabel = "Hatchling",
    title = "Stage 5 • Downy Hatchling",
    subtitle = "Newly hatched golden-yellow chick standing proudly out of the shell",
    minProgress = 0.28f,
    maxProgress = 0.36f,
  ),
  STAGE_06_FEATHERED_CHICK(
    stageIndex = 5,
    stageNumber = 6,
    badge = "Stage 6/12 • Fledgling",
    shortLabel = "Fledgling",
    title = "Stage 6 • Feathered Fledgling",
    subtitle = "First wing covert feathers and tiny crimson comb bud begin sprouting",
    minProgress = 0.36f,
    maxProgress = 0.45f,
  ),
  STAGE_07_AGILE_COCKEREL(
    stageIndex = 6,
    stageNumber = 7,
    badge = "Stage 7/12 • Cockerel",
    shortLabel = "Cockerel",
    title = "Stage 7 • Agile Cockerel",
    subtitle = "Taller juvenile rooster frame with sleek neck plumage and tail buds",
    minProgress = 0.45f,
    maxProgress = 0.54f,
  ),
  STAGE_08_STRENGTH_ROOSTER(
    stageIndex = 7,
    stageNumber = 8,
    badge = "Stage 8/12 • Conditioning",
    shortLabel = "Rooster",
    title = "Stage 8 • Conditioning Rooster",
    subtitle = "Broader breastbone, sturdy feathered thighs, and full serrated comb",
    minProgress = 0.54f,
    maxProgress = 0.64f,
  ),
  STAGE_09_IRON_WING_STRIKER(
    stageIndex = 8,
    stageNumber = 9,
    badge = "Stage 9/12 • Striker",
    shortLabel = "Striker",
    title = "Stage 9 • Iron-Wing Striker",
    subtitle = "Thickened golden neck hackles, strong wings, and arching tail sickles",
    minProgress = 0.64f,
    maxProgress = 0.74f,
  ),
  STAGE_10_HEAVY_BROILER(
    stageIndex = 9,
    stageNumber = 10,
    badge = "Stage 10/12 • Broiler",
    shortLabel = "Broiler",
    title = "Stage 10 • Heavyweight Broiler",
    subtitle = "Deep sculpted pectoral plumage, muscular drumsticks, and proud posture",
    minProgress = 0.74f,
    maxProgress = 0.84f,
  ),
  STAGE_11_VANGUARD_WARRIOR(
    stageIndex = 10,
    stageNumber = 11,
    badge = "Stage 11/12 • Vanguard",
    shortLabel = "Vanguard",
    title = "Stage 11 • Vanguard War-Rooster",
    subtitle = "Athletic V-taper frame, dense layered hackle armor, and powerful wings",
    minProgress = 0.84f,
    maxProgress = 0.93f,
  ),
  STAGE_12_APEX_WARRIOR_ROOSTER(
    stageIndex = 11,
    stageNumber = 12,
    badge = "Stage 12/12 • Apex Warrior",
    shortLabel = "Apex",
    title = "Stage 12 • Apex Warrior Rooster",
    subtitle = "Goal Reached! Majestic, heavily muscled Champion Warrior Rooster",
    minProgress = 0.93f,
    maxProgress = 1.00f,
  );

  val dojoTierNumber: Int
    get() = when (stageNumber) {
      in 1..4 -> 1
      in 5..7 -> 2
      in 8..10 -> 3
      else -> 4
    }

  val dojoTierName: String
    get() = when (dojoTierNumber) {
      1 -> "Sanctuary Barn"
      2 -> "Timber Courtyard"
      3 -> "Iron Foundry Gym"
      else -> "Champion Colosseum"
    }

  companion object {
    fun fromProgress(progress: Float): ChickenEvolutionStage {
      val clamped = progress.coerceIn(0f, 1f)
      return entries.lastOrNull { clamped >= it.minProgress } ?: STAGE_01_SANCTUARY_EGG
    }
  }
}

/**
 * Unlockable 2D Gift Accessories awarded on reaching key milestones during the journey.
 */
data class ChickenAccessoryGift(
  val id: String,
  val name: String,
  val milestoneRequirement: String,
  val description: String,
  val unlockProgressThreshold: Float,
  val unlockWorkoutsThreshold: Int,
) {
  val title: String
    get() = name
  val milestoneLabel: String
    get() = milestoneRequirement
}

val allChickenAccessoryGifts = listOf(
  ChickenAccessoryGift(
    id = "headband_crimson",
    name = "Crimson Dojo Band",
    milestoneRequirement = "Starter Gift (Unlocked)",
    description = "Woven crimson martial-arts band worn across the crest",
    unlockProgressThreshold = 0.0f,
    unlockWorkoutsThreshold = 0,
  ),
  ChickenAccessoryGift(
    id = "wrist_bracers",
    name = "Golden Wing Bands",
    milestoneRequirement = "3 Workouts or 15% Goal",
    description = "Forged golden bands worn on both lower wings",
    unlockProgressThreshold = 0.15f,
    unlockWorkoutsThreshold = 3,
  ),
  ChickenAccessoryGift(
    id = "golden_dumbbells",
    name = "Sanctuary Dumbbells",
    milestoneRequirement = "8 Workouts or 30% Goal",
    description = "Pair of iron dumbbells gripped by your warrior rooster",
    unlockProgressThreshold = 0.30f,
    unlockWorkoutsThreshold = 8,
  ),
  ChickenAccessoryGift(
    id = "spartan_pauldrons",
    name = "Bronze Shoulder Guards",
    milestoneRequirement = "16 Workouts or 50% Goal",
    description = "Sculpted bronze & gold shoulder guards over the upper wings",
    unlockProgressThreshold = 0.50f,
    unlockWorkoutsThreshold = 16,
  ),
  ChickenAccessoryGift(
    id = "champion_belt",
    name = "Champion Sash & Belt",
    milestoneRequirement = "28 Workouts or 72% Goal",
    description = "Gold-buckled championship belt around the lower torso",
    unlockProgressThreshold = 0.72f,
    unlockWorkoutsThreshold = 28,
  ),
  ChickenAccessoryGift(
    id = "warrior_cape_crown",
    name = "Royal Mantle & Crest",
    milestoneRequirement = "90% Goal or 45 Workouts",
    description = "Regal crimson mantle draped from the shoulders",
    unlockProgressThreshold = 0.90f,
    unlockWorkoutsThreshold = 45,
  ),
)

/**
 * Unlockable 2D Plumage Colour Palettes & Elemental Aura Effects (v2 Release).
 */
data class RoosterPlumagePalette(
  val id: String,
  val name: String,
  val auraName: String,
  val unlockRequirement: String,
  val unlockWorkouts: Int,
  val unlockProgress: Float,
)

val allRoosterPlumagePalettes = listOf(
  RoosterPlumagePalette(
    id = "golden_amber",
    name = "Golden Amber",
    auraName = "Solar Gold Aura",
    unlockRequirement = "Starter Palette",
    unlockWorkouts = 0,
    unlockProgress = 0.0f,
  ),
  RoosterPlumagePalette(
    id = "crimson_phoenix",
    name = "Crimson Phoenix",
    auraName = "Ember Flame Aura",
    unlockRequirement = "5 Workouts or 25% Goal",
    unlockWorkouts = 5,
    unlockProgress = 0.25f,
  ),
  RoosterPlumagePalette(
    id = "emerald_jade",
    name = "Emerald Jade",
    auraName = "Jade Wind Aura",
    unlockRequirement = "14 Workouts or 50% Goal",
    unlockWorkouts = 14,
    unlockProgress = 0.50f,
  ),
  RoosterPlumagePalette(
    id = "obsidian_shadow",
    name = "Obsidian Titan",
    auraName = "Lightning Arc Aura",
    unlockRequirement = "25 Workouts or 75% Goal",
    unlockWorkouts = 25,
    unlockProgress = 0.75f,
  ),
)

/**
 * Weekly Quest item for the v2 Quest Board & Streak Freeze Shield system.
 */
data class WeeklyQuest(
  val id: String,
  val title: String,
  val subtitle: String,
  val currentCount: Int,
  val targetCount: Int,
  val xpReward: Int,
  val awardsStreakShield: Boolean,
  val isClaimed: Boolean,
) {
  val isCompleted: Boolean get() = currentCount >= targetCount
  val progressFraction: Float get() = (currentCount.toFloat() / targetCount.coerceAtLeast(1)).coerceIn(0f, 1f)
}

/**
 * Streamlined 3-Biometric Athlete Profile:
 * Uses ONLY Height (cm), Weight (kg), and Waist Length (cm).
 * Zero mock measurements—populated directly by the user during Onboarding.
 */
data class AthleteProfile(
  val id: String,
  val name: String,
  val initials: String = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.let { parts ->
    when {
      parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
      parts.size == 1 -> parts.first().take(2).uppercase()
      else -> "AT"
    }
  },
  val isMale: Boolean = true,
  val heightCm: Float,
  val weightKg: Float,
  val waistCm: Float,
  val baselineWeightKg: Float = weightKg,
  val baselineWaistCm: Float = waistCm,
  val previousWeightKg: Float = weightKg,
  val previousWaistCm: Float = waistCm,
  val lastBiometricCheckInEpochMs: Long = System.currentTimeMillis(),
  val dumbbellWeightKg: Float = 8.0f,
  val sessionDurationMin: Int = 30,
  val workoutDifficultyLevel: Int = 2,
  val cardioDurationMin: Int = 35,
  val cardioDifficultyLevel: Int = 2,
  val xp: Int = 0,
  val completedWorkouts: Int = 0,
  val streakDays: Int = 0,
  val streakFreezeShields: Int = 1,
  val waterGlassesToday: Int = 0,
  val equippedAccessories: Set<String> = emptySet(),
  val selectedPlumagePalette: String = "golden_amber",
  val exerciseWeightOverrides: Map<String, Float> = emptyMap(),
  val claimedQuestIds: Set<String> = emptySet(),
  val hasCompletedOnboarding: Boolean = false,
) {
  val sessionDurationMinutes: Int
    get() = sessionDurationMin

  val difficultyTitle: String
    get() = when (workoutDifficultyLevel.coerceIn(1, 5)) {
      1 -> "Level 1 • Foundation"
      2 -> "Level 2 • Standard (Baseline)"
      3 -> "Level 3 • Athletic Conditioning"
      4 -> "Level 4 • Spartan High-Density"
      5 -> "Level 5 • Apex Warrior Overload"
      else -> "Level 2 • Standard"
    }

  val difficultyShortBadge: String
    get() = "Lv.${workoutDifficultyLevel.coerceIn(1, 5)} ${
      when (workoutDifficultyLevel.coerceIn(1, 5)) {
        1 -> "Foundation"
        2 -> "Standard"
        3 -> "Athletic"
        4 -> "Spartan"
        else -> "Apex"
      }
    }"

  val difficultySubtitle: String
    get() = when (workoutDifficultyLevel.coerceIn(1, 5)) {
      1 -> "Gentler rep cadence (-14% reps/set) & longer recovery rest within your constrained time."
      2 -> "Balanced baseline hypertrophy sets, rep tempo, and recovery intervals."
      3 -> "Increased set density & faster rep tempo (+14% reps/set) with tighter rest."
      4 -> "High-volume Spartan sets (+26% rep pace & extra sets) calibrated to your duration."
      else -> "Maximum set & rep density (+38% explosive cadence & minimum rest) for peak overload."
    }

  val cardioDifficultyTitle: String
    get() = when (cardioDifficultyLevel.coerceIn(1, 5)) {
      1 -> "Level 1 • Restorative Flush"
      2 -> "Level 2 • Zone 2 & Recovery (Base)"
      3 -> "Level 3 • Aerobic Incline Cruise"
      4 -> "Level 4 • VO2 Surge + Mobility"
      5 -> "Level 5 • Peak Summit & CNS Reset"
      else -> "Level 2 • Zone 2 & Recovery"
    }

  val cardioDifficultyShortBadge: String
    get() = "Cardio Lv.${cardioDifficultyLevel.coerceIn(1, 5)}"

  val cardioDifficultySubtitle: String
    get() = when (cardioDifficultyLevel.coerceIn(1, 5)) {
      1 -> "Low-impact Zone 1-2 treadmill walk (4% incline) + extended 60% joint mobility & spinal decompression."
      2 -> "Optimal Sunday recovery: Zone 2 incline walk (6.5-9.5%) to flush soreness + deep hip/thoracic mobility & breathwork."
      3 -> "Elevated incline tempo (8-10.5%) & brisk aerobic surges balanced with myofascial release for Monday readiness."
      4 -> "High-output incline hikes & VO2 speed intervals paired with active lactic flush and full-body stretch."
      else -> "Maximum treadmill incline & VO2 intervals followed by parasympathetic vagus-nerve cooldown."
    }

  val heightMeters: Float
    get() = (heightCm / 100f).coerceAtLeast(1.2f)

  val bmi: Float
    get() = ((weightKg / (heightMeters * heightMeters)) * 10f).roundToInt() / 10f

  val bmiCategory: String
    get() = when {
      bmi < 18.5f -> "Underweight"
      bmi < 25.0f -> "Optimal"
      bmi < 27.5f -> "Athletic / Muscular"
      else -> "Elevated"
    }

  /**
   * Relative Fat Mass (RFM) Index — Cedars-Sinai validated scientific body fat equation
   * requiring ONLY Height (cm) and Waist Circumference (cm).
   */
  val rfmBodyFatPct: Float
    get() {
      val safeWaist = waistCm.coerceAtLeast(45f)
      val raw = if (isMale) {
        64f - (20f * (heightCm / safeWaist))
      } else {
        76f - (20f * (heightCm / safeWaist))
      }
      return ((raw.coerceIn(6f, 45f)) * 10f).roundToInt() / 10f
    }

  val previousRfmBodyFatPct: Float
    get() {
      val safeWaist = previousWaistCm.coerceAtLeast(45f)
      val raw = if (isMale) {
        64f - (20f * (heightCm / safeWaist))
      } else {
        76f - (20f * (heightCm / safeWaist))
      }
      return ((raw.coerceIn(6f, 45f)) * 10f).roundToInt() / 10f
    }

  val weightDeltaFromPreviousKg: Float
    get() = ((weightKg - previousWeightKg) * 10f).roundToInt() / 10f

  val waistDeltaFromPreviousCm: Float
    get() = ((waistCm - previousWaistCm) * 10f).roundToInt() / 10f

  val rfmDeltaFromPreviousPct: Float
    get() = ((rfmBodyFatPct - previousRfmBodyFatPct) * 10f).roundToInt() / 10f

  val daysSinceLastBiometricCheckIn: Int
    get() {
      val diffMs = (System.currentTimeMillis() - lastBiometricCheckInEpochMs).coerceAtLeast(0L)
      return (diffMs / (1000L * 60L * 60L * 24L)).toInt()
    }

  val isWeeklyCheckInDue: Boolean
    get() = daysSinceLastBiometricCheckIn >= 7

  val fatMassKg: Float
    get() = ((weightKg * (rfmBodyFatPct / 100f)) * 10f).roundToInt() / 10f

  val leanBodyMassKg: Float
    get() = (((weightKg - fatMassKg).coerceAtLeast(28f)) * 10f).roundToInt() / 10f

  val ffmi: Float
    get() = ((leanBodyMassKg / (heightMeters * heightMeters)) * 10f).roundToInt() / 10f

  val whtr: Float
    get() = ((waistCm / heightCm.coerceAtLeast(120f)) * 100f).roundToInt() / 100f

  /**
   * Automatic Scientific Goal Calculations from Height, Weight, and Waist Length:
   */
  val autoGoalWaistCm: Float
    get() = ((heightCm * 0.45f) * 10f).roundToInt() / 10f

  val autoGoalBodyFatPct: Float
    get() = if (isMale) 13.0f else 21.0f

  val autoGoalWeightKg: Float
    get() {
      val targetLeanMass = leanBodyMassKg + 0.6f
      val recompWeight = targetLeanMass / (1f - (autoGoalBodyFatPct / 100f))
      val minHealthyWeight = 20.5f * heightMeters * heightMeters
      val maxAthleticWeight = 23.5f * heightMeters * heightMeters
      val clamped = recompWeight.coerceIn(minHealthyWeight, maxAthleticWeight)
      return (clamped * 10f).roundToInt() / 10f
    }

  val autoGoalBmi: Float
    get() = ((autoGoalWeightKg / (heightMeters * heightMeters)) * 10f).roundToInt() / 10f

  val autoWeeklyRateKg: Float
    get() {
      val diff = autoGoalWeightKg - weightKg
      return when {
        diff < -0.2f -> -((weightKg * 0.0055f * 100f).roundToInt() / 100f)
        diff > 0.2f -> +0.22f
        else -> 0.0f
      }
    }

  val autoWeeksToGoal: Int
    get() {
      val weightGap = abs(weightKg - autoGoalWeightKg)
      val waistGap = abs(waistCm - autoGoalWaistCm)
      val weeksByWeight = if (abs(autoWeeklyRateKg) > 0.05f) (weightGap / abs(autoWeeklyRateKg)).roundToInt() else 1
      val weeksByWaist = (waistGap / 0.45f).roundToInt()
      return max(max(weeksByWeight, weeksByWaist), 1).coerceAtMost(52)
    }

  val autoProjectedDateString: String
    get() {
      val cal = Calendar.getInstance()
      cal.add(Calendar.WEEK_OF_YEAR, autoWeeksToGoal)
      return SimpleDateFormat("MMM dd, yyyy", Locale.US).format(cal.time)
    }

  val autoDailyCaloriesKcal: Int
    get() {
      val bmr = (370f + 21.6f * leanBodyMassKg).roundToInt()
      val tdee = (bmr * 1.48f).roundToInt()
      return when {
        weightKg > autoGoalWeightKg + 0.8f || waistCm > autoGoalWaistCm + 1.0f -> (tdee - 380).coerceAtLeast(1550)
        weightKg < autoGoalWeightKg - 0.8f -> tdee + 250
        else -> tdee
      }
    }

  val autoDailyProteinG: Int
    get() = (leanBodyMassKg * 2.35f).roundToInt().coerceIn(115, 230)

  val totalTimelineWorkoutsToGoal: Int
    get() {
      val baselineWeightGap = abs(baselineWeightKg - autoGoalWeightKg)
      val baselineWaistGap = abs(baselineWaistCm - autoGoalWaistCm)
      val estTotalWeeks = max(
        (baselineWeightGap / 0.45f).roundToInt(),
        (baselineWaistGap / 0.50f).roundToInt()
      ).coerceIn(8, 52)
      return (estTotalWeeks * 6).coerceAtLeast(48)
    }

  /**
   * Gradual, continuous Journey Progress (0.000f..1.000f) across all 12 stages:
   * - 70% weighted by actual Weight & Waist reduction toward Auto Goal
   * - 30% weighted by cumulative workouts & check-in XP across the full multi-week timeline
   */
  val goalJourneyProgress: Float
    get() {
      val initialWeightGap = abs(baselineWeightKg - autoGoalWeightKg)
      val currentWeightGap = abs(weightKg - autoGoalWeightKg)
      val weightProgress = if (initialWeightGap > 0.3f) {
        ((initialWeightGap - currentWeightGap) / initialWeightGap).coerceIn(0f, 1f)
      } else {
        0f
      }

      val initialWaistGap = abs(baselineWaistCm - autoGoalWaistCm)
      val currentWaistGap = abs(waistCm - autoGoalWaistCm)
      val waistProgress = if (initialWaistGap > 0.5f) {
        ((initialWaistGap - currentWaistGap) / initialWaistGap).coerceIn(0f, 1f)
      } else {
        0f
      }

      val biometricProgress = (weightProgress * 0.5f) + (waistProgress * 0.5f)
      val totalTargetWorkouts = totalTimelineWorkoutsToGoal.toFloat()
      val totalTargetXp = totalTargetWorkouts * 150f

      val workoutTimelineProgress = (completedWorkouts.toFloat() / totalTargetWorkouts).coerceIn(0f, 1f)
      val xpTimelineProgress = (xp.toFloat() / totalTargetXp).coerceIn(0f, 1f)
      val consistencyProgress = maxOf(workoutTimelineProgress, xpTimelineProgress)

      return (biometricProgress * 0.70f + consistencyProgress * 0.30f).coerceIn(0f, 1f)
    }

  val evolutionStage: ChickenEvolutionStage
    get() = ChickenEvolutionStage.fromProgress(goalJourneyProgress)

  val nextEvolutionStage: ChickenEvolutionStage?
    get() = ChickenEvolutionStage.entries.getOrNull(evolutionStage.stageIndex + 1)

  val currentStageProgressFraction: Float
    get() {
      val st = evolutionStage
      val span = (st.maxProgress - st.minProgress).coerceAtLeast(0.01f)
      return ((goalJourneyProgress - st.minProgress) / span).coerceIn(0f, 1f)
    }

  val growthMicroLevel: Int
    get() = (goalJourneyProgress * 100f).roundToInt().coerceIn(1, 100)

  val avatarLevel: Int
    get() = (xp / 250) + 1

  val level: Int
    get() = avatarLevel

  val xpInCurrentLevel: Int
    get() = xp % 250

  val levelProgressFraction: Float
    get() = ((xp % 250).toFloat() / 250f).coerceIn(0.02f, 1f)

  val avatarTierTitle: String
    get() = evolutionStage.title

  fun isAccessoryUnlocked(gift: ChickenAccessoryGift): Boolean {
    return goalJourneyProgress >= gift.unlockProgressThreshold ||
      completedWorkouts >= gift.unlockWorkoutsThreshold
  }

  fun isPaletteUnlocked(palette: RoosterPlumagePalette): Boolean {
    return goalJourneyProgress >= palette.unlockProgress ||
      completedWorkouts >= palette.unlockWorkouts
  }

  val weeklyQuests: List<WeeklyQuest>
    get() {
      val workoutsInCycle = completedWorkouts % 4
      val q1Progress = if (completedWorkouts > 0 && workoutsInCycle == 0) 4 else workoutsInCycle
      val hasRecentCheckIn = daysSinceLastBiometricCheckIn <= 7
      return listOf(
        WeeklyQuest(
          id = "quest_iron_consistency",
          title = "Iron Consistency • 4 Sessions",
          subtitle = "Complete 4 time-scaled dumbbell workouts (+120 XP & +1 Streak Shield)",
          currentCount = q1Progress,
          targetCount = 4,
          xpReward = 120,
          awardsStreakShield = true,
          isClaimed = claimedQuestIds.contains("quest_iron_consistency"),
        ),
        WeeklyQuest(
          id = "quest_biometric_precision",
          title = "3-Biometric Precision Check-In",
          subtitle = "Keep Height, Weight & Waist updated within 7 days (+75 XP)",
          currentCount = if (hasRecentCheckIn) 1 else 0,
          targetCount = 1,
          xpReward = 75,
          awardsStreakShield = false,
          isClaimed = claimedQuestIds.contains("quest_biometric_precision"),
        ),
        WeeklyQuest(
          id = "quest_progressive_overload",
          title = "Custom Load Mastery",
          subtitle = "Customize working weight on any exercise or complete 2 sessions (+90 XP)",
          currentCount = max(if (exerciseWeightOverrides.isNotEmpty()) 2 else 0, completedWorkouts.coerceAtMost(2)),
          targetCount = 2,
          xpReward = 90,
          awardsStreakShield = false,
          isClaimed = claimedQuestIds.contains("quest_progressive_overload"),
        ),
      )
    }
}

object AthleteProfileStore {
  private const val PREFS_NAME = "google_form_athlete_prefs_v2"
  private var prefs: SharedPreferences? = null

  private val uninitializedProfile = AthleteProfile(
    id = "profile_primary",
    name = "",
    isMale = true,
    heightCm = 175.0f,
    weightKg = 75.0f,
    waistCm = 84.0f,
    baselineWeightKg = 75.0f,
    baselineWaistCm = 84.0f,
    previousWeightKg = 75.0f,
    previousWaistCm = 84.0f,
    dumbbellWeightKg = 8.0f,
    sessionDurationMin = 30,
    xp = 0,
    completedWorkouts = 0,
    streakDays = 0,
    streakFreezeShields = 1,
    waterGlassesToday = 0,
    equippedAccessories = emptySet(),
    selectedPlumagePalette = "golden_amber",
    exerciseWeightOverrides = emptyMap(),
    claimedQuestIds = emptySet(),
    hasCompletedOnboarding = false,
  )

  private val _profiles = MutableStateFlow(listOf(uninitializedProfile))
  val profiles: StateFlow<List<AthleteProfile>> = _profiles.asStateFlow()

  private val _activeProfileId = MutableStateFlow(uninitializedProfile.id)
  val activeProfileId: StateFlow<String> = _activeProfileId.asStateFlow()

  private val _activeProfile = MutableStateFlow(uninitializedProfile)
  val activeProfile: StateFlow<AthleteProfile> = _activeProfile.asStateFlow()

  /**
   * Maps the current calendar day of the week to the 7-day workout protocol:
   * Monday = Day 1, Tuesday = Day 2, Wednesday = Day 3, Thursday = Day 4,
   * Friday = Day 5, Saturday = Day 6, Sunday = Day 7 (Sunday Special Cardio + Treadmill Shred).
   */
  fun getAutoDetectedWorkoutDay(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
      Calendar.MONDAY -> 1
      Calendar.TUESDAY -> 2
      Calendar.WEDNESDAY -> 3
      Calendar.THURSDAY -> 4
      Calendar.FRIDAY -> 5
      Calendar.SATURDAY -> 6
      Calendar.SUNDAY -> 7
      else -> 1
    }
  }

  fun getWeekdayLabel(dayNumber: Int): String {
    return when (dayNumber) {
      1 -> "Mon • Day 1"
      2 -> "Tue • Day 2"
      3 -> "Wed • Day 3"
      4 -> "Thu • Day 4"
      5 -> "Fri • Day 5"
      6 -> "Sat • Day 6"
      7 -> "Sun • Day 7"
      else -> "Day $dayNumber"
    }
  }

  private fun serializeWeightOverrides(map: Map<String, Float>): String {
    return map.entries.joinToString("|") { "${it.key}=${it.value}" }
  }

  private fun deserializeWeightOverrides(raw: String?): Map<String, Float> {
    if (raw.isNullOrBlank()) return emptyMap()
    val result = mutableMapOf<String, Float>()
    raw.split("|").forEach { entry ->
      val parts = entry.split("=")
      if (parts.size == 2) {
        val k = parts[0].trim()
        val v = parts[1].toFloatOrNull()
        if (k.isNotEmpty() && v != null) {
          result[k] = v
        }
      }
    }
    return result
  }

  fun initialize(context: Context) {
    if (prefs != null) return
    val sp = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs = sp
    val onboarded = sp.getBoolean("has_completed_onboarding", false)
    if (onboarded) {
      val weightKg = sp.getFloat("weight_kg", 75f)
      val waistCm = sp.getFloat("waist_cm", 84f)
      val loaded = AthleteProfile(
        id = sp.getString("id", "profile_primary") ?: "profile_primary",
        name = sp.getString("name", "Athlete") ?: "Athlete",
        isMale = sp.getBoolean("is_male", true),
        heightCm = sp.getFloat("height_cm", 175f),
        weightKg = weightKg,
        waistCm = waistCm,
        baselineWeightKg = sp.getFloat("baseline_weight_kg", weightKg),
        baselineWaistCm = sp.getFloat("baseline_waist_cm", waistCm),
        previousWeightKg = sp.getFloat("previous_weight_kg", weightKg),
        previousWaistCm = sp.getFloat("previous_waist_cm", waistCm),
        lastBiometricCheckInEpochMs = sp.getLong("last_biometric_checkin_ms", System.currentTimeMillis()),
        dumbbellWeightKg = sp.getFloat("dumbbell_weight_kg", 8f),
        sessionDurationMin = sp.getInt("session_duration_min", 30).coerceIn(15, 90),
        workoutDifficultyLevel = sp.getInt("workout_difficulty_level", 2).coerceIn(1, 5),
        cardioDurationMin = sp.getInt("cardio_duration_min", 35).coerceIn(15, 90),
        cardioDifficultyLevel = sp.getInt("cardio_difficulty_level", 2).coerceIn(1, 5),
        xp = sp.getInt("xp", 0),
        completedWorkouts = sp.getInt("completed_workouts", 0),
        streakDays = sp.getInt("streak_days", 1),
        streakFreezeShields = sp.getInt("streak_freeze_shields", 1),
        waterGlassesToday = sp.getInt("water_glasses", 0),
        equippedAccessories = sp.getStringSet("equipped_accessories", emptySet()) ?: emptySet(),
        selectedPlumagePalette = sp.getString("selected_plumage_palette", "golden_amber") ?: "golden_amber",
        exerciseWeightOverrides = deserializeWeightOverrides(sp.getString("exercise_weight_overrides", "")),
        claimedQuestIds = sp.getStringSet("claimed_quest_ids", emptySet()) ?: emptySet(),
        hasCompletedOnboarding = true,
      )
      _activeProfile.value = loaded
      _activeProfileId.value = loaded.id
      _profiles.value = listOf(loaded)
    }
  }

  private fun persist(profile: AthleteProfile, context: Context? = null) {
    if (prefs == null && context != null) {
      prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    prefs?.edit()?.apply {
      putBoolean("has_completed_onboarding", profile.hasCompletedOnboarding)
      putString("id", profile.id)
      putString("name", profile.name)
      putBoolean("is_male", profile.isMale)
      putFloat("height_cm", profile.heightCm)
      putFloat("weight_kg", profile.weightKg)
      putFloat("waist_cm", profile.waistCm)
      putFloat("baseline_weight_kg", profile.baselineWeightKg)
      putFloat("baseline_waist_cm", profile.baselineWaistCm)
      putFloat("previous_weight_kg", profile.previousWeightKg)
      putFloat("previous_waist_cm", profile.previousWaistCm)
      putLong("last_biometric_checkin_ms", profile.lastBiometricCheckInEpochMs)
      putFloat("dumbbell_weight_kg", profile.dumbbellWeightKg)
      putInt("session_duration_min", profile.sessionDurationMin.coerceIn(15, 90))
      putInt("workout_difficulty_level", profile.workoutDifficultyLevel.coerceIn(1, 5))
      putInt("cardio_duration_min", profile.cardioDurationMin.coerceIn(15, 90))
      putInt("cardio_difficulty_level", profile.cardioDifficultyLevel.coerceIn(1, 5))
      putInt("xp", profile.xp)
      putInt("completed_workouts", profile.completedWorkouts)
      putInt("streak_days", profile.streakDays)
      putInt("streak_freeze_shields", profile.streakFreezeShields)
      putInt("water_glasses", profile.waterGlassesToday)
      putStringSet("equipped_accessories", profile.equippedAccessories)
      putString("selected_plumage_palette", profile.selectedPlumagePalette)
      putString("exercise_weight_overrides", serializeWeightOverrides(profile.exerciseWeightOverrides))
      putStringSet("claimed_quest_ids", profile.claimedQuestIds)
      apply()
    }
    if (context != null) {
      GoogleFormStreakWidgetProvider.updateAllWidgets(context)
    }
  }

  fun completeOnboarding(
    context: Context? = null,
    name: String,
    isMale: Boolean,
    heightCm: Float,
    weightKg: Float,
    waistCm: Float,
    dumbbellWeightKg: Float,
    sessionDurationMin: Int = 30,
  ): AthleteProfile {
    val cleanHeight = ((heightCm.coerceIn(120f, 230f)) * 10f).roundToInt() / 10f
    val cleanWeight = ((weightKg.coerceIn(35f, 220f)) * 10f).roundToInt() / 10f
    val cleanWaist = ((waistCm.coerceIn(45f, 170f)) * 10f).roundToInt() / 10f
    val profile = AthleteProfile(
      id = "profile_primary",
      name = name.trim().ifBlank { "Athlete" },
      isMale = isMale,
      heightCm = cleanHeight,
      weightKg = cleanWeight,
      waistCm = cleanWaist,
      baselineWeightKg = cleanWeight,
      baselineWaistCm = cleanWaist,
      previousWeightKg = cleanWeight,
      previousWaistCm = cleanWaist,
      lastBiometricCheckInEpochMs = System.currentTimeMillis(),
      dumbbellWeightKg = dumbbellWeightKg.coerceIn(2f, 45f),
      sessionDurationMin = sessionDurationMin.coerceIn(15, 90),
      cardioDurationMin = 35,
      cardioDifficultyLevel = 2,
      xp = 0,
      completedWorkouts = 0,
      streakDays = 1,
      streakFreezeShields = 1,
      waterGlassesToday = 0,
      equippedAccessories = emptySet(),
      selectedPlumagePalette = "golden_amber",
      exerciseWeightOverrides = emptyMap(),
      claimedQuestIds = emptySet(),
      hasCompletedOnboarding = true,
    )
    _activeProfile.value = profile
    _activeProfileId.value = profile.id
    _profiles.value = listOf(profile)
    persist(profile, context)
    return profile
  }

  fun updateActiveProfile(
    context: Context? = null,
    transform: (AthleteProfile) -> AthleteProfile,
  ) {
    val updated = transform(_activeProfile.value)
    _activeProfile.value = updated
    _activeProfileId.value = updated.id
    _profiles.value = _profiles.value.map { if (it.id == updated.id) updated else it }
    persist(updated, context)
  }

  fun recordBiometricCheckIn(
    weightKg: Float,
    waistCm: Float,
    heightCm: Float,
    context: Context? = null,
  ) {
    updateActiveProfile(context) { current ->
      current.copy(
        previousWeightKg = current.weightKg,
        previousWaistCm = current.waistCm,
        weightKg = weightKg.coerceIn(35f, 220f),
        waistCm = waistCm.coerceIn(45f, 180f),
        heightCm = heightCm.coerceIn(120f, 230f),
        lastBiometricCheckInEpochMs = System.currentTimeMillis(),
        xp = current.xp + 35,
        streakDays = max(current.streakDays, 1),
      )
    }
  }

  fun setExerciseWeightOverride(
    exerciseName: String,
    weightKg: Float,
    context: Context? = null,
  ) {
    val cleanWeight = ((weightKg.coerceIn(1.0f, 60.0f)) * 2f).roundToInt() / 2f
    updateActiveProfile(context) { current ->
      val updatedMap = current.exerciseWeightOverrides.toMutableMap()
      updatedMap[exerciseName] = cleanWeight
      current.copy(exerciseWeightOverrides = updatedMap)
    }
  }

  fun applyProgressiveOverload(
    deltaKg: Float = 1.0f,
    context: Context? = null,
  ) {
    updateActiveProfile(context) { current ->
      val newBase = (((current.dumbbellWeightKg + deltaKg).coerceIn(2f, 50f)) * 2f).roundToInt() / 2f
      val updatedOverrides = current.exerciseWeightOverrides.mapValues { (_, v) ->
        (((v + deltaKg).coerceIn(2f, 55f)) * 2f).roundToInt() / 2f
      }
      current.copy(
        dumbbellWeightKg = newBase,
        exerciseWeightOverrides = updatedOverrides,
        xp = current.xp + 40,
      )
    }
  }

  fun selectPlumagePalette(paletteId: String, context: Context? = null) {
    updateActiveProfile(context) { current ->
      current.copy(selectedPlumagePalette = paletteId)
    }
  }

  fun claimWeeklyQuest(
    quest: WeeklyQuest,
    context: Context? = null,
  ) {
    if (!quest.isCompleted || quest.isClaimed) return
    updateActiveProfile(context) { current ->
      val updatedClaimed = current.claimedQuestIds + quest.id
      val updatedShields = if (quest.awardsStreakShield) current.streakFreezeShields + 1 else current.streakFreezeShields
      current.copy(
        xp = current.xp + quest.xpReward,
        streakFreezeShields = updatedShields,
        claimedQuestIds = updatedClaimed,
      )
    }
  }

  fun toggleAccessory(accessoryId: String, context: Context? = null) {
    updateActiveProfile(context) { current ->
      val currentSet = current.equippedAccessories.toMutableSet()
      if (currentSet.contains(accessoryId)) {
        currentSet.remove(accessoryId)
      } else {
        currentSet.add(accessoryId)
      }
      current.copy(equippedAccessories = currentSet)
    }
  }

  fun createNewProfile(
    name: String,
    isMale: Boolean = true,
    heightCm: Float,
    weightKg: Float,
    waistCm: Float,
    dumbbellWeightKg: Float,
    sessionDurationMin: Int,
    context: Context? = null,
  ) {
    val cleanHeight = ((heightCm.coerceIn(120f, 230f)) * 10f).roundToInt() / 10f
    val cleanWeight = ((weightKg.coerceIn(35f, 220f)) * 10f).roundToInt() / 10f
    val cleanWaist = ((waistCm.coerceIn(45f, 170f)) * 10f).roundToInt() / 10f
    val newProfile = AthleteProfile(
      id = "profile_${System.currentTimeMillis()}",
      name = name.trim().ifBlank { "Athlete" },
      isMale = isMale,
      heightCm = cleanHeight,
      weightKg = cleanWeight,
      waistCm = cleanWaist,
      baselineWeightKg = cleanWeight,
      baselineWaistCm = cleanWaist,
      previousWeightKg = cleanWeight,
      previousWaistCm = cleanWaist,
      lastBiometricCheckInEpochMs = System.currentTimeMillis(),
      dumbbellWeightKg = dumbbellWeightKg.coerceIn(2f, 45f),
      sessionDurationMin = sessionDurationMin.coerceIn(15, 90),
      cardioDurationMin = 35,
      cardioDifficultyLevel = 2,
      xp = 0,
      completedWorkouts = 0,
      streakDays = 1,
      streakFreezeShields = 1,
      waterGlassesToday = 0,
      equippedAccessories = emptySet(),
      selectedPlumagePalette = "golden_amber",
      exerciseWeightOverrides = emptyMap(),
      claimedQuestIds = emptySet(),
      hasCompletedOnboarding = true,
    )
    _profiles.value = _profiles.value + newProfile
    _activeProfile.value = newProfile
    _activeProfileId.value = newProfile.id
    persist(newProfile, context)
  }

  fun switchProfile(profileId: String, context: Context? = null) {
    val found = _profiles.value.find { it.id == profileId } ?: return
    _activeProfile.value = found
    _activeProfileId.value = found.id
    persist(found, context)
  }

  fun awardWorkoutXp(
    xpAmount: Int,
    incrementWorkout: Boolean = false,
    context: Context? = null,
  ) {
    updateActiveProfile(context) { current ->
      val updatedWorkouts = if (incrementWorkout) current.completedWorkouts + 1 else current.completedWorkouts
      val updatedStreak = if (incrementWorkout) current.streakDays + 1 else current.streakDays
      val bonusShield = if (incrementWorkout && updatedWorkouts % 6 == 0) 1 else 0
      current.copy(
        xp = current.xp + xpAmount,
        completedWorkouts = updatedWorkouts,
        streakDays = updatedStreak,
        streakFreezeShields = current.streakFreezeShields + bonusShield,
      )
    }
  }
}
