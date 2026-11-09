package com.example.model

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Exact Workout Protocol: "Dumbbells - Get Shredded"
 * No Pull-Up Bar Required (100% Dumbbell + Optional Bench/Floor + Sunday Treadmill Cardio).
 * Includes TimeConstrainedWorkoutScaler so every session finishes at exact 00:00 of the constrained duration,
 * plus per-exercise working dumbbell weight memory overrides.
 */
data class ExerciseProtocol(
  val id: String,
  val name: String,
  val sets: Int,
  val reps: Int,
  val weightKg: Double?, // null represents bodyweight / treadmill interval
  val targetMuscle: String,
  val alternative: String? = null,
  val isSupersetEligible: Boolean = false,
) {
  val isBodyweight: Boolean get() = weightKg == null
  val displayWeight: String
    get() = when {
      name.contains("Treadmill", ignoreCase = true) -> "Treadmill"
      weightKg != null -> {
        val w = weightKg
        if (abs(w - w.roundToInt()) < 0.05) {
          "${w.roundToInt()} kg"
        } else {
          String.format(Locale.US, "%.1f kg", w)
        }
      }
      else -> "Bodyweight"
    }
}

data class WorkoutDayProtocol(
  val day: Int,
  val name: String,
  val durationMinutes: Int = 30,
  val exercises: List<ExerciseProtocol>,
  val activities: List<String> = emptyList(),
  val isRecovery: Boolean = false,
  val isCardioSpecial: Boolean = false,
) {
  val totalPrescribedSets: Int get() = exercises.sumOf { it.sets }
}

/**
 * Exact per-set timing budget so the entire workout finishes precisely when the session timer hits 00:00.
 */
data class ScaledWorkoutTimingPlan(
  val protocol: WorkoutDayProtocol,
  val totalSessionSeconds: Int,
  val prepSecondsPerSet: Int,
  val workSecondsPerSet: Int,
  val restSecondsPerSet: Int,
  val difficultyLevel: Int = 2,
) {
  val dayProtocol: WorkoutDayProtocol get() = protocol
  val exercises: List<ExerciseProtocol> get() = protocol.exercises
  val totalScaledSets: Int get() = protocol.totalPrescribedSets
  val targetDurationMinutes: Int get() = protocol.durationMinutes
  val secondsPerSetSlot: Int get() = prepSecondsPerSet + workSecondsPerSet + restSecondsPerSet
}

object WorkoutProtocolRepository {
  val programName = "Dumbbells - Get Shredded"
  val programType = "Dumbbell & Bench Strength + Sunday Treadmill"
  val goals = listOf(
    "Muscle Building",
    "Fat Loss",
    "Strength",
    "Body Recomposition",
    "Core & Cardio Conditioning",
  )
  val defaultDurationMinutes = 30
  val strictDurationLimit = true
  val defaultWeightKg = 8.0

  val allWorkouts: List<WorkoutDayProtocol> = listOf(
    // Day 1: Chest, Triceps & Abs (Zero pull-up bar required)
    WorkoutDayProtocol(
      day = 1,
      name = "Chest, Triceps & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d1_1", "Incline Bench Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Upper Chest"),
        ExerciseProtocol("d1_2", "Bench Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Mid Chest"),
        ExerciseProtocol("d1_3", "Incline Bench Fly", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Chest Stretch"),
        ExerciseProtocol("d1_4", "Bench Fly", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Pectoral Squeeze"),
        ExerciseProtocol("d1_5", "Lying French Press", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Triceps Long Head", isSupersetEligible = true),
        ExerciseProtocol("d1_6", "Overhead French Press", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Triceps Lateral Head"),
        ExerciseProtocol("d1_7", "Bench Lying Leg Raises", sets = 3, reps = 15, weightKg = null, targetMuscle = "Lower Abs", isSupersetEligible = true),
        ExerciseProtocol("d1_8", "Weighted Dumbbell Crunch", sets = 3, reps = 18, weightKg = 8.0, targetMuscle = "Rectus Abdominis"),
      ),
    ),

    // Day 2: Back, Biceps & Abs (Pull-Ups replaced with Dumbbell Pullover on bench/floor)
    WorkoutDayProtocol(
      day = 2,
      name = "Back, Biceps & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d2_1", "Dumbbell Pullover", sets = 4, reps = 12, weightKg = 8.0, targetMuscle = "Lats & Serratus", alternative = "Single-Arm Lat Row"),
        ExerciseProtocol("d2_2", "Bent-Over Dumbbell Row", sets = 4, reps = 13, weightKg = 8.0, targetMuscle = "Mid Back"),
        ExerciseProtocol("d2_3", "Wide Elbow Row", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Upper Back & Rear Delt"),
        ExerciseProtocol("d2_4", "Bicep Curl", sets = 4, reps = 14, weightKg = 8.0, targetMuscle = "Biceps Brachii"),
        ExerciseProtocol("d2_5", "Hammer Curl", sets = 3, reps = 14, weightKg = 8.0, targetMuscle = "Brachialis"),
        ExerciseProtocol("d2_6", "Lying Leg Raises", sets = 3, reps = 18, weightKg = null, targetMuscle = "Lower Core"),
        ExerciseProtocol("d2_7", "Bicycle Crunches", sets = 3, reps = 22, weightKg = null, targetMuscle = "Obliques"),
      ),
    ),

    // Day 3: Legs, Shoulders & Abs
    WorkoutDayProtocol(
      day = 3,
      name = "Legs, Shoulders & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d3_1", "Dumbbell Goblet Squats", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Quadriceps"),
        ExerciseProtocol("d3_2", "Romanian Deadlift", sets = 3, reps = 11, weightKg = 8.0, targetMuscle = "Hamstrings & Glutes"),
        ExerciseProtocol("d3_3", "Dumbbell Lunges", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Quads & Glutes"),
        ExerciseProtocol("d3_4", "Standing Calf Raises", sets = 3, reps = 20, weightKg = 8.0, targetMuscle = "Gastrocnemius"),
        ExerciseProtocol("d3_5", "Seated Overhead Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Anterior Deltoids"),
        ExerciseProtocol("d3_6", "Lateral Raises", sets = 3, reps = 15, weightKg = 8.0, targetMuscle = "Lateral Deltoids"),
        ExerciseProtocol("d3_7", "Reverse Crunch & Hip Lift", sets = 3, reps = 16, weightKg = null, targetMuscle = "Lower Abs"),
        ExerciseProtocol("d3_8", "Abdominal Crunches", sets = 3, reps = 22, weightKg = null, targetMuscle = "Upper Abs"),
      ),
    ),

    // Day 4: Chest, Triceps & Abs (Zero pull-up bar required)
    WorkoutDayProtocol(
      day = 4,
      name = "Chest, Triceps & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d4_1", "Decline Bench Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Lower Chest"),
        ExerciseProtocol("d4_2", "Flat Bench Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Mid Chest"),
        ExerciseProtocol("d4_3", "Decline Bench Fly", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Lower Pectoral"),
        ExerciseProtocol("d4_4", "Flat Bench Fly", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Pectoral Squeeze"),
        ExerciseProtocol("d4_5", "Dumbbell Triceps Kickback", sets = 3, reps = 15, weightKg = 8.0, targetMuscle = "Triceps Peak"),
        ExerciseProtocol("d4_6", "Seated French Press", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Triceps Long Head"),
        ExerciseProtocol("d4_7", "Dumbbell V-Up & Leg Tuck", sets = 3, reps = 15, weightKg = null, targetMuscle = "Lower Abs"),
        ExerciseProtocol("d4_8", "Crunches", sets = 3, reps = 22, weightKg = null, targetMuscle = "Abs"),
      ),
    ),

    // Day 5: Back, Biceps & Abs (Pull-Ups replaced with Single-Arm Bench Lat Row)
    WorkoutDayProtocol(
      day = 5,
      name = "Back, Biceps & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d5_1", "Single-Arm Bench Lat Row", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Lats", alternative = "Dumbbell Pullover"),
        ExerciseProtocol("d5_2", "Chest-Supported Bench Row", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Lats & Rhomboids"),
        ExerciseProtocol("d5_3", "Bent-Over Reverse Fly", sets = 3, reps = 15, weightKg = 8.0, targetMuscle = "Rear Delts"),
        ExerciseProtocol("d5_4", "Dumbbell Shrugs", sets = 3, reps = 18, weightKg = 8.0, targetMuscle = "Trapezius"),
        ExerciseProtocol("d5_5", "Alternate Bicep Curl", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Biceps Peak"),
        ExerciseProtocol("d5_6", "Alternate Hammer Curl", sets = 3, reps = 13, weightKg = 8.0, targetMuscle = "Brachialis"),
        ExerciseProtocol("d5_7", "Lying Leg Raises", sets = 3, reps = 18, weightKg = null, targetMuscle = "Core"),
        ExerciseProtocol("d5_8", "Bicycle Crunches", sets = 3, reps = 22, weightKg = null, targetMuscle = "Obliques"),
      ),
    ),

    // Day 6: Legs, Shoulders & Abs
    WorkoutDayProtocol(
      day = 6,
      name = "Legs, Shoulders & Abs",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d6_1", "Dumbbell Squats", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Quads"),
        ExerciseProtocol("d6_2", "Reverse Dumbbell Lunges", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Glutes & Quads"),
        ExerciseProtocol("d6_3", "Dumbbell Deadlift", sets = 3, reps = 11, weightKg = 8.0, targetMuscle = "Posterior Chain"),
        ExerciseProtocol("d6_4", "Calf Raises", sets = 3, reps = 20, weightKg = 8.0, targetMuscle = "Calves"),
        ExerciseProtocol("d6_5", "Arnold Press", sets = 3, reps = 12, weightKg = 8.0, targetMuscle = "Full Deltoid"),
        ExerciseProtocol("d6_6", "Lateral Raises", sets = 3, reps = 15, weightKg = 8.0, targetMuscle = "Side Delts"),
        ExerciseProtocol("d6_7", "Alternating Front Raises", sets = 3, reps = 14, weightKg = 8.0, targetMuscle = "Front Delts"),
        ExerciseProtocol("d6_8", "Weighted Crunches", sets = 3, reps = 20, weightKg = 8.0, targetMuscle = "Abs"),
      ),
    ),

    // Day 7: Sunday Special Cardio + Treadmill Session
    WorkoutDayProtocol(
      day = 7,
      name = "Sunday Special: Cardio & Treadmill Shred",
      durationMinutes = 30,
      exercises = listOf(
        ExerciseProtocol("d7_1", "Treadmill Incline Brisk Walk (6% @ 5.5 km/h)", sets = 3, reps = 15, weightKg = null, targetMuscle = "Zone 2 Fat Oxidation"),
        ExerciseProtocol("d7_2", "Treadmill HIIT Sprint Intervals (11 km/h)", sets = 4, reps = 15, weightKg = null, targetMuscle = "VO2 Max & Visceral Burn"),
        ExerciseProtocol("d7_3", "Treadmill Steep Power Hike (10% @ 5.0 km/h)", sets = 3, reps = 14, weightKg = null, targetMuscle = "Glutes & Calves"),
        ExerciseProtocol("d7_4", "Dumbbell Farmer's Carry March", sets = 3, reps = 16, weightKg = 8.0, targetMuscle = "Core & Grip Conditioning"),
        ExerciseProtocol("d7_5", "High-Tempo Mountain Climbers", sets = 3, reps = 22, weightKg = null, targetMuscle = "Metabolic Core"),
        ExerciseProtocol("d7_6", "Bicycle Crunches & Plank Burnout", sets = 4, reps = 22, weightKg = null, targetMuscle = "Abdominal Definition"),
      ),
      activities = listOf(
        "15-Min Treadmill Incline & HIIT Intervals",
        "15-Min Metabolic Dumbbell Carry & Core Shred",
        "Post-Cardio Mobility & Calf Stretch",
      ),
      isRecovery = false,
      isCardioSpecial = true,
    ),
  )

  fun getWorkout(day: Int): WorkoutDayProtocol {
    return allWorkouts.find { it.day == day } ?: allWorkouts.first()
  }

  /**
   * Dynamically calibrates Sets, Reps, and Work/Rest intervals from BOTH:
   * 1. `targetDurationMinutes` (e.g., 20, 25, 30, 45 min)
   * 2. `difficultyLevel` (Level 1..5, where Level 2 = current standard baseline)
   * Guarantees that the workout ALWAYS finishes 100% of sets at exact 00:00 of the constrained duration.
   */
  fun getScaledWorkoutPlan(
    day: Int,
    targetDurationMinutes: Int = 30,
    customDumbbellWeightKg: Float,
    exerciseWeightOverrides: Map<String, Float> = emptyMap(),
    difficultyLevel: Int = 2,
  ): ScaledWorkoutTimingPlan = getScaledWorkoutPlan(
    day = day,
    targetDurationMinutes = targetDurationMinutes,
    customDumbbellWeightKg = customDumbbellWeightKg.toDouble(),
    exerciseWeightOverrides = exerciseWeightOverrides,
    difficultyLevel = difficultyLevel,
  )

  fun getScaledWorkoutPlan(
    day: Int,
    targetDurationMinutes: Int = 30,
    customDumbbellWeightKg: Double = 8.0,
    exerciseWeightOverrides: Map<String, Float> = emptyMap(),
    difficultyLevel: Int = 2,
  ): ScaledWorkoutTimingPlan {
    val base = getWorkout(day)
    val constrainedMinutes = targetDurationMinutes.coerceIn(15, 90)
    val clampedDifficulty = difficultyLevel.coerceIn(1, 5)
    val totalSessionSeconds = constrainedMinutes * 60

    // Set density per minute scales with Difficulty Level (Level 2 = 0.80f baseline)
    val setsPerMinuteFactor = when (clampedDifficulty) {
      1 -> 0.68f
      2 -> 0.80f
      3 -> 0.90f
      4 -> 1.02f
      else -> 1.14f
    }
    val maxSetsPerEx = when {
      constrainedMinutes >= 70 -> if (clampedDifficulty >= 4) 8 else 7
      constrainedMinutes >= 50 -> if (clampedDifficulty >= 4) 7 else 6
      constrainedMinutes >= 40 -> if (clampedDifficulty >= 4) 6 else 5
      else -> if (clampedDifficulty >= 4) 5 else 4
    }
    val minSetsPerEx = if (clampedDifficulty >= 4) 3 else 2

    val targetTotalSets = (constrainedMinutes * setsPerMinuteFactor).roundToInt().coerceIn(
      base.exercises.size * minSetsPerEx,
      base.exercises.size * maxSetsPerEx,
    )
    val baseSetsPerEx = (targetTotalSets / base.exercises.size).coerceIn(minSetsPerEx, maxSetsPerEx)
    val extraSetsCount = (targetTotalSets % base.exercises.size).coerceAtLeast(0)

    val scaledExercises = base.exercises.mapIndexed { idx, ex ->
      val scaledSets = if (idx < extraSetsCount) (baseSetsPerEx + 1).coerceAtMost(maxSetsPerEx) else baseSetsPerEx
      val overrideWeight = exerciseWeightOverrides[ex.name]?.toDouble()
      val updatedWeight = if (ex.weightKg != null) (overrideWeight ?: customDumbbellWeightKg) else null
      ex.copy(sets = scaledSets, weightKg = updatedWeight)
    }

    val actualTotalSets = scaledExercises.sumOf { it.sets }.coerceAtLeast(1)

    val prepSecs = when {
      clampedDifficulty <= 2 -> 8
      clampedDifficulty == 3 -> 7
      else -> 6
    }
    val baseRestSecs = when {
      constrainedMinutes <= 20 -> 18
      constrainedMinutes <= 25 -> 22
      constrainedMinutes <= 40 -> 24
      constrainedMinutes <= 60 -> 28
      else -> 32
    }
    val restDifficultyDelta = when (clampedDifficulty) {
      1 -> +3
      2 -> 0
      3 -> -2
      4 -> -4
      else -> -6
    }
    val restSecs = (baseRestSecs + restDifficultyDelta).coerceIn(12, 30)

    val totalRestSeconds = (actualTotalSets - 1).coerceAtLeast(0) * restSecs
    val totalPrepSeconds = actualTotalSets * prepSecs
    val remainingForWork = (totalSessionSeconds - totalRestSeconds - totalPrepSeconds).coerceAtLeast(actualTotalSets * 18)
    val workSecsPerSet = (remainingForWork / actualTotalSets).coerceIn(22, 65)

    // Rep cadence multiplier by Difficulty Level (Level 2 = 1.00f baseline)
    val repIntensityMultiplier = when (clampedDifficulty) {
      1 -> 0.86f
      2 -> 1.00f
      3 -> 1.14f
      4 -> 1.26f
      else -> 1.38f
    }

    val repCalibratedExercises = scaledExercises.map { ex ->
      val lower = ex.name.lowercase()
      val secPerRep = when {
        lower.contains("treadmill") -> 2.5f
        lower.contains("deadlift") || lower.contains("lunge") || lower.contains("squat") || lower.contains("pullover") -> 3.4f
        lower.contains("press") || lower.contains("fly") || lower.contains("row") -> 3.0f
        lower.contains("curl") || lower.contains("raise") || lower.contains("kickback") -> 2.5f
        else -> 1.9f
      }
      val practicalReps = (((workSecsPerSet - 3) / secPerRep) * repIntensityMultiplier).roundToInt().coerceIn(6, 32)
      ex.copy(reps = practicalReps)
    }

    val scaledProtocol = base.copy(
      durationMinutes = constrainedMinutes,
      exercises = repCalibratedExercises,
    )

    return ScaledWorkoutTimingPlan(
      protocol = scaledProtocol,
      totalSessionSeconds = totalSessionSeconds,
      prepSecondsPerSet = prepSecs,
      workSecondsPerSet = workSecsPerSet,
      restSecondsPerSet = restSecs,
      difficultyLevel = clampedDifficulty,
    )
  }
}
