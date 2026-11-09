package com.example.data

import com.example.data.entity.BodyMeasurementEntity
import com.example.data.entity.SetLogEntity
import com.example.data.entity.WorkoutLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TransformationExporter {
  private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

  fun exportWorkoutsToCsv(workouts: List<WorkoutLogEntity>): String {
    val sb = StringBuilder()
    sb.append("LogID,Date,DayNumber,WorkoutName,DurationMinutes,TotalVolumeKg,SetsCompleted,SetsDeferred,SessionRating\n")
    for (w in workouts) {
      val formattedDate = dateFormat.format(Date(w.timestamp))
      val durationMins = String.format(Locale.US, "%.1f", w.durationSeconds / 60.0)
      sb.append("${w.id},\"$formattedDate\",${w.dayNumber},\"${w.workoutName}\",$durationMins,${w.totalVolumeKg},${w.setsCompleted},${w.setsDeferred},${w.sessionRating}\n")
    }
    return sb.toString()
  }

  /**
   * Exports 3-Biometric (Weight, Height, Waist Length) & derived RFM/BMI measurements to CSV format.
   */
  fun exportMeasurementsToCsv(measurements: List<BodyMeasurementEntity>): String {
    val sb = StringBuilder()
    sb.append("ID,Date,WeightKg,HeightCm,WaistCm,BMI,RFMBodyFatPercent\n")
    for (m in measurements) {
      val formattedDate = dateFormat.format(Date(m.timestamp))
      sb.append("${m.id},\"$formattedDate\",${m.weightKg},${m.heightCm},${m.waistCm},${m.bmi},${m.estimatedBodyFatPercent}\n")
    }
    return sb.toString()
  }

  fun exportToJson(
    workouts: List<WorkoutLogEntity>,
    sets: List<SetLogEntity>,
    measurements: List<BodyMeasurementEntity>,
  ): String {
    val sb = StringBuilder()
    sb.append("{\n")
    sb.append("  \"app\": \"Google Form\",\n")
    sb.append("  \"version\": \"2.0\",\n")
    sb.append("  \"export_timestamp\": ${System.currentTimeMillis()},\n")
    sb.append("  \"workouts\": [\n")
    workouts.forEachIndexed { index, w ->
      val comma = if (index < workouts.size - 1) "," else ""
      val formattedDate = dateFormat.format(Date(w.timestamp))
      sb.append("    {\n")
      sb.append("      \"id\": ${w.id},\n")
      sb.append("      \"date\": \"$formattedDate\",\n")
      sb.append("      \"day_number\": ${w.dayNumber},\n")
      sb.append("      \"name\": \"${w.workoutName}\",\n")
      sb.append("      \"duration_seconds\": ${w.durationSeconds},\n")
      sb.append("      \"total_volume_kg\": ${w.totalVolumeKg},\n")
      sb.append("      \"sets_completed\": ${w.setsCompleted},\n")
      sb.append("      \"sets_deferred\": ${w.setsDeferred},\n")
      sb.append("      \"session_rating\": ${w.sessionRating}\n")
      sb.append("    }$comma\n")
    }
    sb.append("  ],\n")
    sb.append("  \"measurements\": [\n")
    measurements.forEachIndexed { index, m ->
      val comma = if (index < measurements.size - 1) "," else ""
      val formattedDate = dateFormat.format(Date(m.timestamp))
      sb.append("    {\n")
      sb.append("      \"id\": ${m.id},\n")
      sb.append("      \"date\": \"$formattedDate\",\n")
      sb.append("      \"weight_kg\": ${m.weightKg},\n")
      sb.append("      \"height_cm\": ${m.heightCm},\n")
      sb.append("      \"waist_cm\": ${m.waistCm},\n")
      sb.append("      \"bmi\": ${m.bmi},\n")
      sb.append("      \"rfm_body_fat_percent\": ${m.estimatedBodyFatPercent}\n")
      sb.append("    }$comma\n")
    }
    sb.append("  ]\n")
    sb.append("}")
    return sb.toString()
  }

  fun generateAthleteSummary(
    workoutsCount: Int,
    currentWeightKg: Float,
    bodyFatPercent: Float,
    totalVolumeTons: Float,
  ): String {
    return """
      Google Form • Athlete Transformation Summary
      ────────────────────────────────────────
      Sessions Logged: $workoutsCount workouts
      Current Weight: ${String.format(Locale.US, "%.1f", currentWeightKg)} kg
      Estimated RFM Body Fat: ${String.format(Locale.US, "%.1f", bodyFatPercent)}%
      Lifetime Tonnage: ${String.format(Locale.US, "%.2f", totalVolumeTons)} tons moved
      Protocol: Dumbbells - Get Shredded (Zero Pull-Up Bar)
    """.trimIndent()
  }
}
