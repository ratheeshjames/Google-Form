package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.MeasurementDao
import com.example.data.dao.ReadinessDao
import com.example.data.dao.WorkoutDao
import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.ExerciseEntity
import com.example.data.entity.ReadinessEntity
import com.example.data.entity.SetLogEntity
import com.example.data.entity.WorkoutLogEntity
import com.example.data.entity.WorkoutRoutineEntity
import com.example.model.WorkoutProtocolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    WorkoutRoutineEntity::class,
    ExerciseEntity::class,
    WorkoutLogEntity::class,
    SetLogEntity::class,
    BodyMeasurementEntity::class,
    ReadinessEntity::class,
  ],
  version = 2,
  exportSchema = false,
)
abstract class GoogleFormDatabase : RoomDatabase() {
  abstract fun workoutDao(): WorkoutDao
  abstract fun measurementDao(): MeasurementDao
  abstract fun readinessDao(): ReadinessDao

  companion object {
    @Volatile
    private var INSTANCE: GoogleFormDatabase? = null

    fun getInstance(context: Context): GoogleFormDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          GoogleFormDatabase::class.java,
          "google_form.db",
        )
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              CoroutineScope(Dispatchers.IO).launch {
                seedExerciseProtocolOnly(getInstance(context))
              }
            }

            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
              super.onDestructiveMigration(db)
              CoroutineScope(Dispatchers.IO).launch {
                seedExerciseProtocolOnly(getInstance(context))
              }
            }
          })
          .fallbackToDestructiveMigration(dropAllTables = true)
          .build()

        INSTANCE = instance
        instance
      }
    }

    /**
     * Seeds ONLY the static workout split definitions (Exercises for Days 1..7).
     * Zero mock biometric measurements or fake workout logs are inserted.
     */
    private suspend fun seedExerciseProtocolOnly(database: GoogleFormDatabase) {
      val workoutDao = database.workoutDao()
      val routines = mutableListOf<WorkoutRoutineEntity>()
      val exercises = mutableListOf<ExerciseEntity>()

      WorkoutProtocolRepository.allWorkouts.forEach { dayProtocol ->
        routines.add(
          WorkoutRoutineEntity(
            dayNumber = dayProtocol.day,
            name = dayProtocol.name,
            targetMuscleFocus = dayProtocol.name,
            isCustom = false,
            durationMinutes = dayProtocol.durationMinutes,
          ),
        )

        dayProtocol.exercises.forEachIndexed { index, ep ->
          exercises.add(
            ExerciseEntity(
              dayId = dayProtocol.day,
              exerciseName = ep.name,
              originalOrder = index + 1,
              prescribedSets = ep.sets,
              prescribedReps = ep.reps,
              defaultWeightKg = ep.weightKg ?: 8.0,
              isBodyweight = ep.isBodyweight,
              primaryMuscle = ep.targetMuscle,
              secondaryMuscle = "Core Stabilization",
              pullUpAlternativeName = ep.alternative,
            ),
          )
        }
      }

      workoutDao.insertRoutines(routines)
      workoutDao.insertExercises(exercises)
    }
  }
}
