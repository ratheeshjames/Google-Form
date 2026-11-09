package com.example.domain

import com.example.model.ExerciseProtocol
import com.example.model.WorkoutDayProtocol

data class PlannedExerciseSlot(
  val exercise: ExerciseProtocol,
  val plannedSets: Int,
  val deferredSets: Int,
  val estimatedSeconds: Int,
  val isSuperset: Boolean = false,
  val supersetPartnerName: String? = null,
)

data class OptimizedSessionPlan(
  val totalBudgetSeconds: Int,
  val allocatedSeconds: Int,
  val plannedWarmupSeconds: Int = 180, // 3 minutes
  val plannedCooldownSeconds: Int = 180, // 3 minutes
  val totalPrescribedSets: Int,
  val totalPlannedSets: Int,
  val totalDeferredSets: Int,
  val exerciseSlots: List<PlannedExerciseSlot>,
  val supersetPairsCount: Int,
)

object WorkoutScheduler {
  private const val WORKING_SET_SECONDS = 45
  private const val STANDARD_REST_SECONDS = 50
  private const val SUPERSET_REST_SECONDS = 30
  private const val EXERCISE_TRANSITION_SECONDS = 30
  private const val WARMUP_SECONDS = 180
  private const val COOLDOWN_SECONDS = 180

  fun optimizeSession(
    dayProtocol: WorkoutDayProtocol,
    targetDurationMinutes: Int = 30,
  ): OptimizedSessionPlan {
    val totalBudgetSeconds = targetDurationMinutes * 60
    val hypertrophyBudgetSeconds = (totalBudgetSeconds - WARMUP_SECONDS - COOLDOWN_SECONDS).coerceAtLeast(300)

    val exercises = dayProtocol.exercises
    val totalPrescribedSets = dayProtocol.totalPrescribedSets

    if (exercises.isEmpty()) {
      return OptimizedSessionPlan(
        totalBudgetSeconds = totalBudgetSeconds,
        allocatedSeconds = 0,
        totalPrescribedSets = 0,
        totalPlannedSets = 0,
        totalDeferredSets = 0,
        exerciseSlots = emptyList(),
        supersetPairsCount = 0,
      )
    }

    var remainingTime = hypertrophyBudgetSeconds
    val slots = mutableListOf<PlannedExerciseSlot>()
    var plannedSetsSum = 0
    var supersetPairsCount = 0

    // Priority 1: Primary Compound exercises (first 4) get full priority
    exercises.forEachIndexed { index, exercise ->
      remainingTime -= EXERCISE_TRANSITION_SECONDS

      val isEligibleSuperset = exercise.isSupersetEligible
      val restPerSet = if (isEligibleSuperset) SUPERSET_REST_SECONDS else STANDARD_REST_SECONDS
      val timePerSet = WORKING_SET_SECONDS + restPerSet

      // How many sets fit into remaining budget?
      val maxPossibleSets = (remainingTime / timePerSet).coerceAtLeast(0)
      val actualPlannedSets = minOf(exercise.sets, maxPossibleSets)
      val deferredSets = exercise.sets - actualPlannedSets

      val slotSeconds = actualPlannedSets * timePerSet
      remainingTime -= slotSeconds
      plannedSetsSum += actualPlannedSets

      if (isEligibleSuperset) {
        supersetPairsCount++
      }

      slots.add(
        PlannedExerciseSlot(
          exercise = exercise,
          plannedSets = actualPlannedSets,
          deferredSets = deferredSets,
          estimatedSeconds = slotSeconds,
          isSuperset = isEligibleSuperset,
          supersetPartnerName = if (isEligibleSuperset) "Antagonist Pairing" else null,
        ),
      )
    }

    val totalDeferredSets = totalPrescribedSets - plannedSetsSum
    val allocatedHypertrophySeconds = hypertrophyBudgetSeconds - remainingTime
    val totalAllocatedSeconds = WARMUP_SECONDS + COOLDOWN_SECONDS + allocatedHypertrophySeconds

    return OptimizedSessionPlan(
      totalBudgetSeconds = totalBudgetSeconds,
      allocatedSeconds = totalAllocatedSeconds,
      plannedWarmupSeconds = WARMUP_SECONDS,
      plannedCooldownSeconds = COOLDOWN_SECONDS,
      totalPrescribedSets = totalPrescribedSets,
      totalPlannedSets = plannedSetsSum,
      totalDeferredSets = totalDeferredSets,
      exerciseSlots = slots,
      supersetPairsCount = supersetPairsCount,
    )
  }
}
