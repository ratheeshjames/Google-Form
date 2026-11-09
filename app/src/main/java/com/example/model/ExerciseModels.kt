package com.example.model

data class PreloadedExerciseItem(
  val name: String,
  val setsReps: String,
  val weight: String,
  val targetMuscle: String,
  val alternative: String? = null,
)

val day1Exercises: List<PreloadedExerciseItem> = WorkoutProtocolRepository.getWorkout(1).exercises.map { ep ->
  PreloadedExerciseItem(
    name = ep.name,
    setsReps = "${ep.sets} sets × ${ep.reps} reps",
    weight = ep.displayWeight,
    targetMuscle = ep.targetMuscle,
    alternative = ep.alternative,
  )
}
