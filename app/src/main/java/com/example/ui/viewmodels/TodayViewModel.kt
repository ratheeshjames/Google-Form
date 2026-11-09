package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.ReadinessEntity
import com.example.domain.OptimizedSessionPlan
import com.example.domain.WorkoutScheduler
import com.example.model.WorkoutDayProtocol
import com.example.model.WorkoutProtocolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed interface TodayUiState {
  object Loading : TodayUiState
  data class Success(
    val activeDayProtocol: WorkoutDayProtocol,
    val scheduledPlan: OptimizedSessionPlan,
    val readiness: ReadinessEntity?,
    val latestMeasurement: BodyMeasurementEntity?,
    val weightMovingAvg: Float,
  ) : TodayUiState
  data class Error(val message: String) : TodayUiState
}

class TodayViewModel(application: Application) : AndroidViewModel(application) {
  private val database = GoogleFormDatabase.getInstance(application)
  private val measurementDao = database.measurementDao()
  private val readinessDao = database.readinessDao()

  private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
  val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

  init {
    loadTodayData()
  }

  fun loadTodayData() {
    viewModelScope.launch {
      try {
        val profile = AthleteProfileStore.activeProfile.value
        val dayProtocol = WorkoutProtocolRepository.getWorkout(1)
        val plan = WorkoutScheduler.optimizeSession(dayProtocol, profile.sessionDurationMin)

        val latestReadiness = readinessDao.getLatestReadiness().firstOrNull()
        val latestMeasurement = measurementDao.getLatestMeasurement().firstOrNull()

        _uiState.value = TodayUiState.Success(
          activeDayProtocol = dayProtocol,
          scheduledPlan = plan,
          readiness = latestReadiness,
          latestMeasurement = latestMeasurement,
          weightMovingAvg = latestMeasurement?.weightKg ?: profile.weightKg,
        )
      } catch (e: Exception) {
        _uiState.value = TodayUiState.Error(e.localizedMessage ?: "Failed to load session data")
      }
    }
  }
}
