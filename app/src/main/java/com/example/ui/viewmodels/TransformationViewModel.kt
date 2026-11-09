package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TransformationUiState(
  val isLoading: Boolean = false,
  val currentWeight: Float = 75.0f,
  val sevenDayMovingAverage: Float = 75.0f,
  val heightCm: Float = 175.0f,
  val waistCm: Float = 84.0f,
  val waistToHeightRatio: Float = 0.48f,
  val bodyFatPercent: Float = 18.2f,
  val history: List<BodyMeasurementEntity> = emptyList(),
)

class TransformationViewModel(application: Application) : AndroidViewModel(application) {
  private val database = GoogleFormDatabase.getInstance(application)
  private val measurementDao = database.measurementDao()

  private val _uiState = MutableStateFlow(TransformationUiState(isLoading = true))
  val uiState: StateFlow<TransformationUiState> = _uiState.asStateFlow()

  init {
    observeMeasurements()
  }

  private fun observeMeasurements() {
    viewModelScope.launch {
      measurementDao.getAllMeasurements().collect { list ->
        val profile = AthleteProfileStore.activeProfile.value
        if (list.isNotEmpty()) {
          val latest = list.first()
          val avg = list.take(7).map { it.weightKg }.average().toFloat()
          val whtr = latest.waistCm / latest.heightCm.coerceAtLeast(100f)

          _uiState.value = TransformationUiState(
            isLoading = false,
            currentWeight = latest.weightKg,
            sevenDayMovingAverage = if (avg.isNaN()) latest.weightKg else avg,
            heightCm = latest.heightCm,
            waistCm = latest.waistCm,
            waistToHeightRatio = whtr,
            bodyFatPercent = latest.estimatedBodyFatPercent,
            history = list,
          )
        } else {
          _uiState.value = TransformationUiState(
            isLoading = false,
            currentWeight = profile.weightKg,
            sevenDayMovingAverage = profile.weightKg,
            heightCm = profile.heightCm,
            waistCm = profile.waistCm,
            waistToHeightRatio = profile.whtr,
            bodyFatPercent = profile.rfmBodyFatPct,
            history = emptyList(),
          )
        }
      }
    }
  }

  fun addMeasurement(
    weightKg: Float,
    heightCm: Float,
    waistCm: Float,
  ) {
    viewModelScope.launch {
      val profile = AthleteProfileStore.activeProfile.value
      val heightM = (heightCm / 100f).coerceAtLeast(1.2f)
      val bmi = weightKg / (heightM * heightM)
      val rfm = if (profile.isMale) {
        (64f - 20f * (heightCm / waistCm.coerceAtLeast(50f))).coerceIn(5f, 50f)
      } else {
        (76f - 20f * (heightCm / waistCm.coerceAtLeast(50f))).coerceIn(10f, 55f)
      }

      val entity = BodyMeasurementEntity(
        weightKg = weightKg,
        heightCm = heightCm,
        waistCm = waistCm,
        bmi = bmi,
        estimatedBodyFatPercent = rfm,
      )
      measurementDao.insertMeasurement(entity)
    }
  }
}
