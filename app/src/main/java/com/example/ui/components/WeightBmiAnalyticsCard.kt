package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.entity.BodyMeasurementEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun WeightBmiAnalyticsSuite(
    currentWeightKg: Float,
    targetWeightKg: Float,
    heightCm: Float,
    waistCm: Float,
    targetWaistCm: Float,
    bmi: Float,
    rfmBodyFatPct: Float,
    leanMassKg: Float,
    fatMassKg: Float,
    ffmi: Float,
    whtr: Float,
    autoWeeksToGoal: Int = 12,
    recordedMeasurements: List<BodyMeasurementEntity>,
    onAdjustWeight: (Float) -> Unit,
    onAdjustWaist: (Float) -> Unit,
    onAdjustHeight: (Float) -> Unit
) {
    // 0 = Weight & 4-Week Goal Forecast, 1 = Waist & RFM Trajectory, 2 = BMI Spectrum
    var selectedChartMode by remember { mutableIntStateOf(0) }

    val heightM = (heightCm / 100f).coerceAtLeast(1.2f)
    val targetBmi = targetWeightKg / (heightM * heightM)

    // Real recorded points from Room DB (sorted chronological, oldest -> newest)
    val chronologicalLogs = remember(recordedMeasurements, currentWeightKg, waistCm, bmi) {
        val sorted = recordedMeasurements.sortedBy { it.timestamp }.takeLast(6)
        if (sorted.isEmpty()) {
            listOf(
                BodyMeasurementEntity(
                    timestamp = System.currentTimeMillis(),
                    weightKg = currentWeightKg,
                    heightCm = heightCm,
                    waistCm = waistCm,
                    bmi = bmi,
                    estimatedBodyFatPercent = rfmBodyFatPct
                )
            )
        } else {
            sorted
        }
    }

    val recordedWeights = remember(chronologicalLogs, currentWeightKg) {
        val list = chronologicalLogs.map { it.weightKg }.toMutableList()
        if (list.isNotEmpty()) {
            list[list.lastIndex] = currentWeightKg
        }
        list
    }

    val recordedWaists = remember(chronologicalLogs, waistCm) {
        val list = chronologicalLogs.map { it.waistCm }.toMutableList()
        if (list.isNotEmpty()) {
            list[list.lastIndex] = waistCm
        }
        list
    }

    // Weekly rate toward automatic goal
    val weightGap = targetWeightKg - currentWeightKg
    val weeklySlopeKg = when {
        abs(weightGap) < 0.2f -> 0f
        weightGap < 0f -> max(weightGap / 4f, -0.55f)
        else -> min(weightGap / 4f, 0.35f)
    }

    val waistGap = targetWaistCm - waistCm
    val weeklyWaistSlopeCm = when {
        abs(waistGap) < 0.2f -> 0f
        waistGap < 0f -> max(waistGap / 4f, -0.65f)
        else -> min(waistGap / 4f, 0.35f)
    }

    // 4-Week Forward Forecast from Today's Real Biometrics
    val projectedWeights = remember(currentWeightKg, targetWeightKg, weeklySlopeKg) {
        listOf(
            currentWeightKg + weeklySlopeKg * 1f,
            currentWeightKg + weeklySlopeKg * 2f,
            currentWeightKg + weeklySlopeKg * 3f,
            currentWeightKg + weeklySlopeKg * 4f
        ).map {
            if (targetWeightKg <= currentWeightKg) it.coerceAtLeast(targetWeightKg - 0.3f)
            else it.coerceAtMost(targetWeightKg + 0.3f)
        }
    }

    val projectedWaists = remember(waistCm, targetWaistCm, weeklyWaistSlopeCm) {
        listOf(
            waistCm + weeklyWaistSlopeCm * 1f,
            waistCm + weeklyWaistSlopeCm * 2f,
            waistCm + weeklyWaistSlopeCm * 3f,
            waistCm + weeklyWaistSlopeCm * 4f
        ).map {
            if (targetWaistCm <= waistCm) it.coerceAtLeast(targetWaistCm - 0.3f)
            else it.coerceAtMost(targetWaistCm + 0.3f)
        }
    }

    val weeksToTarget = autoWeeksToGoal.coerceAtLeast(1)

    val avgWeight = remember(recordedWeights) {
        recordedWeights.average().toFloat()
    }

    val bmiCategory = when {
        bmi < 18.5f -> "Underweight"
        bmi < 25.0f -> "Optimal Range"
        bmi < 30.0f -> "Overweight"
        else -> "High Mass"
    }
    val bmiBadgeColor = when {
        bmi < 18.5f -> Color(0xFF1976D2)
        bmi < 25.0f -> Color(0xFF2E7D32)
        bmi < 30.0f -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }

    var selectedPointIndex by remember(recordedWeights.size) {
        mutableIntStateOf(recordedWeights.lastIndex)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Top Header: Weight + Height + Waist Summary & BMI Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonitorWeight,
                            contentDescription = "3-Biometric Tracker",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "3-BIOMETRIC ENGINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f kg • %.1f cm", currentWeightKg, waistCm),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Height ${heightCm.toInt()} cm • ${recordedMeasurements.size} check-in(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bmiBadgeColor.copy(alpha = 0.14f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = String.format(Locale.US, "BMI %.1f", bmi),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = bmiBadgeColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = bmiCategory,
                            style = MaterialTheme.typography.labelSmall,
                            color = bmiBadgeColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Adjust Steppers for the 3 Core Biometrics (Weight, Waist, Height)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactBiometricAdjustPill(
                        label = "Weight",
                        valueText = String.format(Locale.US, "%.1f kg", currentWeightKg),
                        onMinus = { onAdjustWeight(-0.2f) },
                        onPlus = { onAdjustWeight(0.2f) },
                        modifier = Modifier.weight(1f)
                    )
                    CompactBiometricAdjustPill(
                        label = "Waist",
                        valueText = String.format(Locale.US, "%.1f cm", waistCm),
                        onMinus = { onAdjustWaist(-0.5f) },
                        onPlus = { onAdjustWaist(0.5f) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CompactBiometricAdjustPill(
                        label = "Height",
                        valueText = String.format(Locale.US, "%.0f cm", heightCm),
                        onMinus = { onAdjustHeight(-1f) },
                        onPlus = { onAdjustHeight(1f) },
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Auto Goal Weight & Waist",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f kg • %.1f cm", targetWeightKg, targetWaistCm),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Mode Switcher Pills
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            0 to "Weight Trend",
                            1 to "Waist & RFM",
                            2 to "BMI Spectrum"
                        ).forEach { (idx, title) ->
                            val isSelected = selectedChartMode == idx
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedChartMode = idx },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Text(
                                    text = title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (selectedChartMode) {
                        0 -> {
                            WeightTrajectoryAndForecastChart(
                                recordedSeries = recordedWeights,
                                projectedSeries = projectedWeights,
                                targetValue = targetWeightKg,
                                unit = "kg",
                                selectedIndex = selectedPointIndex,
                                onSelectPoint = { selectedPointIndex = it },
                                weeksToTarget = weeksToTarget
                            )
                        }
                        1 -> {
                            WeightTrajectoryAndForecastChart(
                                recordedSeries = recordedWaists,
                                projectedSeries = projectedWaists,
                                targetValue = targetWaistCm,
                                unit = "cm",
                                selectedIndex = selectedPointIndex,
                                onSelectPoint = { selectedPointIndex = it },
                                weeksToTarget = weeksToTarget
                            )
                        }
                        2 -> {
                            BmiSpectrumVisual(
                                currentBmi = bmi,
                                targetBmi = targetBmi,
                                heightCm = heightCm,
                                currentWeightKg = currentWeightKg
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Derived Body Composition Cards (Purely from Weight, Height & Waist Length!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BiometricMiniStatCard(
                    title = "RFM Body Fat",
                    value = String.format(Locale.US, "%.1f%%", rfmBodyFatPct),
                    subtitle = "Fat Mass ${String.format(Locale.US, "%.1f", fatMassKg)} kg",
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                BiometricMiniStatCard(
                    title = "Lean Body Mass",
                    value = String.format(Locale.US, "%.1f kg", leanMassKg),
                    subtitle = "FFMI ${String.format(Locale.US, "%.1f", ffmi)}",
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BiometricMiniStatCard(
                    title = "Waist-to-Height",
                    value = String.format(Locale.US, "%.2f", whtr),
                    subtitle = if (whtr < 0.48f) "Optimal (<0.48)" else "Target: 0.45 (${String.format(Locale.US, "%.1f", targetWaistCm)} cm)",
                    accentColor = if (whtr < 0.50f) Color(0xFF2E7D32) else Color(0xFFEF6C00),
                    modifier = Modifier.weight(1f)
                )
                BiometricMiniStatCard(
                    title = "Auto Goal ETA",
                    value = "~$weeksToTarget Weeks",
                    subtitle = "Goal ${String.format(Locale.US, "%.1f", targetWeightKg)} kg",
                    accentColor = Color(0xFF7B1FA2),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactBiometricAdjustPill(
    label: String,
    valueText: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onMinus,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(14.dp))
                }
                FilledTonalIconButton(
                    onClick = onPlus,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase $label", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun BiometricMiniStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeightTrajectoryAndForecastChart(
    recordedSeries: List<Float>,
    projectedSeries: List<Float>,
    targetValue: Float,
    unit: String,
    selectedIndex: Int,
    onSelectPoint: (Int) -> Unit,
    weeksToTarget: Int
) {
    val combined = recordedSeries + projectedSeries
    val minY = (min(combined.minOrNull() ?: 60f, targetValue) - 1.2f)
    val maxY = (max(combined.maxOrNull() ?: 80f, targetValue) + 1.2f)
    val yRange = (maxY - minY).coerceAtLeast(1f)

    val safeSelectedIdx = selectedIndex.coerceIn(0, combined.lastIndex)
    val selectedValue = combined[safeSelectedIdx]
    val isForecastSelected = safeSelectedIdx >= recordedSeries.size

    val primaryColor = MaterialTheme.colorScheme.primary
    val forecastColor = Color(0xFF2E7D32)
    val goalLineColor = Color(0xFFFF6D00)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isForecastSelected) {
                        "PROJECTED • WEEK +${safeSelectedIdx - recordedSeries.size + 1}"
                    } else if (safeSelectedIdx == recordedSeries.lastIndex) {
                        "RECORDED • CURRENT"
                    } else {
                        "RECORDED • CHECK-IN #${safeSelectedIdx + 1}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isForecastSelected) forecastColor else primaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = String.format(Locale.US, "%.1f %s (Goal: %.1f %s)", selectedValue, unit, targetValue, unit),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ) {
                Text(
                    text = "ETA: ~$weeksToTarget Wks",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
        ) {
            val w = size.width
            val h = size.height
            val padX = 10.dp.toPx()
            val padTop = 14.dp.toPx()
            val padBottom = 22.dp.toPx()
            val chartW = (w - padX * 2f).coerceAtLeast(1f)
            val chartH = h - padTop - padBottom
            val totalPoints = combined.size
            val stepX = if (totalPoints > 1) chartW / (totalPoints - 1).toFloat() else chartW

            fun xFor(idx: Int): Float = padX + idx * stepX

            fun yFor(v: Float): Float {
                val norm = ((v - minY) / yRange).coerceIn(0f, 1f)
                return padTop + chartH * (1f - norm)
            }

            // Grid lines
            for (g in 0..3) {
                val gy = padTop + chartH * (g / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gy),
                    end = Offset(w, gy),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Dashed Goal Line
            val goalY = yFor(targetValue)
            drawLine(
                color = goalLineColor,
                start = Offset(0f, goalY),
                end = Offset(w, goalY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )

            // Recorded Path (if > 1 point)
            if (recordedSeries.size > 1) {
                val recPath = Path()
                recordedSeries.forEachIndexed { idx, v ->
                    val px = xFor(idx)
                    val py = yFor(v)
                    if (idx == 0) recPath.moveTo(px, py) else recPath.lineTo(px, py)
                }
                drawPath(
                    path = recPath,
                    color = primaryColor,
                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 4-Week Forward Forecast Path from Current Recorded Value
            val forecastPath = Path()
            val startIdx = recordedSeries.lastIndex
            forecastPath.moveTo(xFor(startIdx), yFor(recordedSeries.last()))
            projectedSeries.forEachIndexed { offsetIdx, v ->
                val globalIdx = recordedSeries.size + offsetIdx
                forecastPath.lineTo(xFor(globalIdx), yFor(v))
            }
            drawPath(
                path = forecastPath,
                color = forecastColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f)
                )
            )

            // Draw nodes
            combined.forEachIndexed { idx, v ->
                val px = xFor(idx)
                val py = yFor(v)
                val isForecast = idx >= recordedSeries.size
                val dotColor = if (isForecast) forecastColor else primaryColor
                drawCircle(
                    color = dotColor,
                    radius = if (idx == safeSelectedIdx) 7.dp.toPx() else 5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        // Point Selector Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (recordedSeries.size == 1) "Baseline" else "${recordedSeries.size} Logs",
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "+1 Wk",
                style = MaterialTheme.typography.labelSmall,
                color = forecastColor,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "+2 Wk",
                style = MaterialTheme.typography.labelSmall,
                color = forecastColor,
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = "+4 Wk Goal",
                style = MaterialTheme.typography.labelSmall,
                color = forecastColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun BmiSpectrumVisual(
    currentBmi: Float,
    targetBmi: Float,
    heightCm: Float,
    currentWeightKg: Float
) {
    val heightM = (heightCm / 100f).coerceAtLeast(1.2f)
    val minHealthyKg = 18.5f * heightM * heightM
    val maxHealthyKg = 24.9f * heightM * heightM

    Column {
        Text(
            text = "BMI SPECTRUM • OPTIMAL CORRIDOR: ${String.format(Locale.US, "%.1f - %.1f kg", minHealthyKg, maxHealthyKg)}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            val w = size.width
            val barH = 14.dp.toPx()
            val barY = 10.dp.toPx()

            fun xForBmi(b: Float): Float {
                val norm = ((b - 15f) / 20f).coerceIn(0f, 1f)
                return norm * w
            }

            // Spectrum bar
            drawRoundRect(
                color = Color(0xFF2E7D32).copy(alpha = 0.3f),
                topLeft = Offset(0f, barY),
                size = Size(w, barH),
                cornerRadius = CornerRadius(7.dp.toPx())
            )
            // Current BMI Marker
            drawCircle(
                color = Color(0xFF1565C0),
                radius = 9.dp.toPx(),
                center = Offset(xForBmi(currentBmi), barY + barH / 2f)
            )
            // Target BMI Marker
            drawCircle(
                color = Color(0xFFFF6D00),
                radius = 7.dp.toPx(),
                center = Offset(xForBmi(targetBmi), barY + barH / 2f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Current BMI: ${String.format(Locale.US, "%.1f", currentBmi)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Auto Goal BMI: ${String.format(Locale.US, "%.1f", targetBmi)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6D00)
            )
        }
    }
}
