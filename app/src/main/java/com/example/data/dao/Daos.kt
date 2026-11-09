package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SetLogEntity
import com.example.data.entity.WorkoutLogEntity
import com.example.data.entity.WorkoutRoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
  @Query("SELECT * FROM workout_routines ORDER BY dayNumber ASC")
  fun getAllRoutines(): Flow<List<WorkoutRoutineEntity>>

  @Query("SELECT * FROM exercises WHERE dayId = :dayId ORDER BY originalOrder ASC")
  fun getExercisesForDay(dayId: Int): Flow<List<ExerciseEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRoutines(routines: List<WorkoutRoutineEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExercises(exercises: List<ExerciseEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkoutLog(log: WorkoutLogEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSetLogs(setLogs: List<SetLogEntity>)

  @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC LIMIT 50")
  fun getRecentWorkoutLogs(): Flow<List<WorkoutLogEntity>>

  @Query("SELECT * FROM set_logs WHERE exerciseName = :exerciseName ORDER BY id DESC LIMIT 20")
  fun getRecentSetsForExercise(exerciseName: String): Flow<List<SetLogEntity>>

  @Query("DELETE FROM workout_logs")
  suspend fun clearAllWorkoutLogs()
}

@Dao
interface MeasurementDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long

  @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC")
  fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>

  @Query("SELECT * FROM body_measurements ORDER BY timestamp DESC LIMIT 1")
  fun getLatestMeasurement(): Flow<BodyMeasurementEntity?>

  @Query("SELECT AVG(weightKg) FROM (SELECT weightKg FROM body_measurements ORDER BY timestamp DESC LIMIT 7)")
  fun getSevenDayWeightMovingAverage(): Flow<Double?>

  @Query("DELETE FROM body_measurements")
  suspend fun clearAllMeasurements()
}

@Dao
interface ReadinessDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReadiness(readiness: ReadinessEntity): Long

  @Query("SELECT * FROM readiness_entries ORDER BY timestamp DESC LIMIT 1")
  fun getLatestReadiness(): Flow<ReadinessEntity?>

  @Query("SELECT * FROM readiness_entries ORDER BY timestamp DESC LIMIT 7")
  fun getRecentReadinessEntries(): Flow<List<ReadinessEntity>>
}
