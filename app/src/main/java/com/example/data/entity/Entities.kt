package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_routines")
data class WorkoutRoutineEntity(
  @PrimaryKey val dayNumber: Int,
  val name: String,
  val targetMuscleFocus: String,
  val isCustom: Boolean = false,
  val durationMinutes: Int = 30,
)

@Entity(
  tableName = "exercises",
  foreignKeys = [
    ForeignKey(
      entity = WorkoutRoutineEntity::class,
      parentColumns = ["dayNumber"],
      childColumns = ["dayId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("dayId")],
)
data class ExerciseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val dayId: Int,
  val exerciseName: String,
  val originalOrder: Int,
  val prescribedSets: Int,
  val prescribedReps: Int,
  val defaultWeightKg: Double = 8.0,
  val isBodyweight: Boolean = false,
  val primaryMuscle: String,
  val secondaryMuscle: String,
  val pullUpAlternativeName: String? = null,
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val dayNumber: Int,
  val workoutName: String,
  val durationSeconds: Int = 1800,
  val totalVolumeKg: Double,
  val setsCompleted: Int,
  val setsDeferred: Int,
  val sessionRating: Int = 5,
)

@Entity(
  tableName = "set_logs",
  foreignKeys = [
    ForeignKey(
      entity = WorkoutLogEntity::class,
      parentColumns = ["id"],
      childColumns = ["workoutLogId"],
      onDelete = ForeignKey.CASCADE,
    ),
  ],
  indices = [Index("workoutLogId")],
)
data class SetLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val workoutLogId: Long,
  val exerciseName: String,
  val setIndex: Int,
  val actualWeightKg: Double,
  val actualReps: Int,
  val rpeScore: Double = 8.0,
)

/**
 * Streamlined 3-Biometric Entity:
 * Stores ONLY user-provided Weight (kg), Height (cm), and Waist Length (cm),
 * along with the derived BMI and RFM Body Fat %.
 */
@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val weightKg: Float,
  val heightCm: Float,
  val waistCm: Float,
  val bmi: Float,
  val estimatedBodyFatPercent: Float,
)

@Entity(tableName = "readiness_entries")
data class ReadinessEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val sleepHours: Double,
  val sorenessLevel: Int,
  val energyLevel: Int,
  val readinessScore: Int,
)
