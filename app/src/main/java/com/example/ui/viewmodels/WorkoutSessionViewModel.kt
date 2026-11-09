package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GoogleFormDatabase
import com.example.data.entity.SetLogEntity
import com.example.data.entity.WorkoutLogEntity
import com.example.model.ExerciseProtocol
import com.example.model.WorkoutDayProtocol
import com.example.model.WorkoutProtocolRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionState(
  val dayProtocol: WorkoutDayProtocol,
  val totalSecondsRemaining: Int = 1800, // 30 minutes
  val isPaused: Boolean = false,
  val currentExerciseIndex: Int = 0,
  val currentSetIndex: Int = 1,
  val completedSetsCount: Int = 0,
  val isResting: Boolean = false,
  val restSecondsRemaining: Int = 45,
  val activeWeightKg: Double = 8.0,
  val activeReps: Int = 15,
  val isCompleted: Boolean = false,
)

class WorkoutSessionViewModel(application: Application) : AndroidViewModel(application) {
  private val database = GoogleFormDatabase.getInstance(application)
  private val workoutDao = database.workoutDao()

  private val _sessionState = MutableStateFlow(
    SessionState(dayProtocol = WorkoutProtocolRepository.getWorkout(1)),
  )
  val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

  private var timerJob: Job? = null
  private var restJob: Job? = null
  private val loggedSets = mutableListOf<SetLogEntity>()

  fun startSession(dayNumber: Int) {
    val protocol = WorkoutProtocolRepository.getWorkout(dayNumber)
    val firstEx = protocol.exercises.firstOrNull()

    _sessionState.value = SessionState(
      dayProtocol = protocol,
      totalSecondsRemaining = protocol.durationMinutes * 60,
      activeWeightKg = firstEx?.weightKg ?: 8.0,
      activeReps = firstEx?.reps ?: 15,
    )
    loggedSets.clear()
    startTimerTicker()
  }

  private fun startTimerTicker() {
    timerJob?.cancel()
    timerJob = viewModelScope.launch {
      while (_sessionState.value.totalSecondsRemaining > 0 && !_sessionState.value.isCompleted) {
        delay(1000L)
        if (!_sessionState.value.isPaused) {
          val newRem = _sessionState.value.totalSecondsRemaining - 1
          _sessionState.value = _sessionState.value.copy(
            totalSecondsRemaining = newRem,
            isCompleted = newRem <= 0,
          )
          if (newRem <= 0) {
            persistWorkoutCompletion()
          }
        }
      }
    }
  }

  fun togglePause() {
    _sessionState.value = _sessionState.value.copy(isPaused = !_sessionState.value.isPaused)
  }

  fun adjustWeight(delta: Double) {
    val current = _sessionState.value.activeWeightKg
    _sessionState.value = _sessionState.value.copy(activeWeightKg = (current + delta).coerceAtLeast(0.0))
  }

  fun adjustReps(delta: Int) {
    val current = _sessionState.value.activeReps
    _sessionState.value = _sessionState.value.copy(activeReps = (current + delta).coerceAtLeast(1))
  }

  fun completeCurrentSet() {
    val current = _sessionState.value
    val ex = current.dayProtocol.exercises.getOrNull(current.currentExerciseIndex) ?: return

    loggedSets.add(
      SetLogEntity(
        workoutLogId = 0,
        exerciseName = ex.name,
        setIndex = current.currentSetIndex,
        actualWeightKg = current.activeWeightKg,
        actualReps = current.activeReps,
        rpeScore = 8.0,
      ),
    )

    val newCompleted = current.completedSetsCount + 1

    if (current.currentSetIndex < ex.sets) {
      _sessionState.value = current.copy(
        currentSetIndex = current.currentSetIndex + 1,
        completedSetsCount = newCompleted,
        isResting = true,
        restSecondsRemaining = 45,
      )
      startRestCountdown(45)
    } else {
      if (current.currentExerciseIndex < current.dayProtocol.exercises.size - 1) {
        val nextIdx = current.currentExerciseIndex + 1
        val nextEx = current.dayProtocol.exercises[nextIdx]
        _sessionState.value = current.copy(
          currentExerciseIndex = nextIdx,
          currentSetIndex = 1,
          completedSetsCount = newCompleted,
          activeWeightKg = nextEx.weightKg ?: 8.0,
          activeReps = nextEx.reps,
          isResting = true,
          restSecondsRemaining = 60,
        )
        startRestCountdown(60)
      } else {
        _sessionState.value = current.copy(
          completedSetsCount = newCompleted,
          isCompleted = true,
        )
        persistWorkoutCompletion()
      }
    }
  }

  private fun startRestCountdown(seconds: Int) {
    restJob?.cancel()
    restJob = viewModelScope.launch {
      var rem = seconds
      while (rem > 0 && _sessionState.value.isResting) {
        delay(1000L)
        rem -= 1
        _sessionState.value = _sessionState.value.copy(restSecondsRemaining = rem)
      }
      _sessionState.value = _sessionState.value.copy(isResting = false)
    }
  }

  fun skipRest() {
    restJob?.cancel()
    _sessionState.value = _sessionState.value.copy(isResting = false)
  }

  fun addRest(seconds: Int) {
    _sessionState.value = _sessionState.value.copy(
      restSecondsRemaining = _sessionState.value.restSecondsRemaining + seconds,
    )
  }

  private fun persistWorkoutCompletion() {
    viewModelScope.launch {
      val state = _sessionState.value
      val totalPrescribed = state.dayProtocol.totalPrescribedSets
      val completed = state.completedSetsCount
      val deferred = (totalPrescribed - completed).coerceAtLeast(0)
      val totalVol = loggedSets.sumOf { it.actualWeightKg * it.actualReps }

      val logId = workoutDao.insertWorkoutLog(
        WorkoutLogEntity(
          dayNumber = state.dayProtocol.day,
          workoutName = state.dayProtocol.name,
          durationSeconds = (state.dayProtocol.durationMinutes * 60) - state.totalSecondsRemaining,
          totalVolumeKg = totalVol,
          setsCompleted = completed,
          setsDeferred = deferred,
        ),
      )

      val boundSets = loggedSets.map { it.copy(workoutLogId = logId) }
      workoutDao.insertSetLogs(boundSets)
    }
  }

  override fun onCleared() {
    super.onCleared()
    timerJob?.cancel()
    restJob?.cancel()
  }
}
