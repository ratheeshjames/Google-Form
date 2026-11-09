package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfile
import com.example.data.FoodQuickPreset
import com.example.data.NutritionGoalMode
import com.example.data.PrescribedNutritionAnalysis
import com.example.ui.theme.PillShape
import com.example.ui.theme.SquircleFabShape
import com.example.ui.theme.WorkspaceCardShape
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val NutritionEmerald = Color(0xFF10B981)
private val NutritionCyan = Color(0xFF00E5FF)
private val NutritionBlue = Color(0xFF3B82F6)
private val NutritionAmber = Color(0xFFF59E0B)
private val NutritionCoral = Color(0xFFEF4444)
private val NutritionPurple = Color(0xFFA855F7)
private val NutritionDarkCardBg = Color(0xFF0A1622)
private val NutritionInnerTileBg = Color(0xFF112233)

/**
 * 1. Primary Prescribed Calorie & Triple Macro Radial Telemetry Hero Card
 */
@Composable
fun PrescribedCalorieAndMacroGaugeHeroCard(
  analysis: PrescribedNutritionAnalysis,
  onSelectGoalMode: (NutritionGoalMode) -> Unit,
  modifier: Modifier = Modifier,
) {
  val animatedCalorieProgress by animateFloatAsState(
    targetValue = analysis.calorieProgress.coerceIn(0f, 1.35f),
    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
    label = "calorie_arc_progress",
  )

  val statusColor = when (analysis.complianceZoneCode) {
    1 -> NutritionEmerald
    2 -> NutritionAmber
    else -> NutritionCyan
  }

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(containerColor = NutritionDarkCardBg),
    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.38f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("nutrition_hero_gauge_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top Goal Mode Selector Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        NutritionGoalMode.entries.forEach { mode ->
          val isSelected = mode == analysis.goalMode
          FilterChip(
            selected = isSelected,
            onClick = { onSelectGoalMode(mode) },
            label = {
              Text(
                text = mode.shortBadge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
              )
            },
            shape = PillShape,
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NutritionEmerald.copy(alpha = 0.24f),
              selectedLabelColor = NutritionEmerald,
              containerColor = NutritionInnerTileBg,
              labelColor = Color.White.copy(alpha = 0.78f),
            ),
          )
        }
      }

      // Main 240° Radial Calorie Gauge + BMR / Training Burn / Remaining Readout
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // 240° Sweeping Calorie Speedometer Gauge
        Box(
          modifier = Modifier.size(164.dp),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 14.dp.toPx()
            val pad = strokeW / 2f + 8.dp.toPx()
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            val topLeft = Offset(pad, pad)

            // Background Track (150° start, 240° sweep)
            drawArc(
              color = Color.White.copy(alpha = 0.10f),
              startAngle = 150f,
              sweepAngle = 240f,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )

            // Optimal Prescribed Window Zone Highlight (88% to 102% of arc)
            drawArc(
              color = NutritionEmerald.copy(alpha = 0.30f),
              startAngle = 150f + (240f * 0.85f),
              sweepAngle = 240f * 0.15f,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeW + 4.dp.toPx(), cap = StrokeCap.Butt),
            )

            // Tick Marks around the 240° dial
            val cx = size.width / 2f
            val cy = size.height / 2f
            val outerR = (size.width / 2f) - 2.dp.toPx()
            val innerR = outerR - 5.dp.toPx()
            for (i in 0..12) {
              val deg = 150f + (i / 12f) * 240f
              val rad = deg * (PI / 180.0)
              val sx = cx + innerR * cos(rad).toFloat()
              val sy = cy + innerR * sin(rad).toFloat()
              val ex = cx + outerR * cos(rad).toFloat()
              val ey = cy + outerR * sin(rad).toFloat()
              drawLine(
                color = if (i in 10..12) NutritionEmerald.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.22f),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = if (i % 3 == 0) 2.2.dp.toPx() else 1.2.dp.toPx(),
                cap = StrokeCap.Round,
              )
            }

            // Active Consumed Calorie Sweep Arc
            val clampedProgress = animatedCalorieProgress.coerceIn(0f, 1.0f)
            val sweep = 240f * clampedProgress
            val activeColors = if (animatedCalorieProgress > 1.06f) {
              listOf(NutritionAmber, NutritionCoral)
            } else {
              listOf(NutritionCyan, NutritionEmerald)
            }
            if (sweep > 1f) {
              drawArc(
                brush = Brush.sweepGradient(activeColors),
                startAngle = 150f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeW, cap = StrokeCap.Round),
              )
            }
          }

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp),
          ) {
            Icon(
              imageVector = Icons.Default.LocalFireDepartment,
              contentDescription = null,
              tint = statusColor,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "${analysis.consumedCaloriesKcal}",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              softWrap = false,
            )
            Text(
              text = "/ ${analysis.prescribedCaloriesKcal} kcal",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = Color.White.copy(alpha = 0.72f),
              maxLines = 1,
              softWrap = false,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
              shape = PillShape,
              color = statusColor.copy(alpha = 0.18f),
            ) {
              val rem = analysis.remainingCaloriesKcal
              Text(
                text = if (rem >= 0) "$rem left" else "+${abs(rem)} over",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                fontWeight = FontWeight.ExtraBold,
                color = statusColor,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
          }
        }

        // Right Column: Prescribed Energy Breakdown & Status
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = statusColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "PRESCRIBED STATUS",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                fontWeight = FontWeight.ExtraBold,
                color = statusColor,
              )
              Text(
                text = analysis.complianceStatusTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }

          // Energy Equation Breakdown (BMR + Training Burn = TDEE -> Prescribed)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = NutritionInnerTileBg,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              EnergyEquationRow(label = "Basal BMR (LBM)", value = "${analysis.bmrKcal} kcal", color = Color.White.copy(alpha = 0.8f))
              EnergyEquationRow(label = "Today Workout Burn", value = "+${analysis.trainingBurnKcal} kcal", color = NutritionCyan)
              EnergyEquationRow(label = "Total Daily Burn (TDEE)", value = "${analysis.tdeeKcal} kcal", color = NutritionAmber)
              HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 2.dp))
              EnergyEquationRow(
                label = "Prescribed Target",
                value = "${analysis.prescribedCaloriesKcal} kcal",
                color = NutritionEmerald,
                isBold = true,
              )
            }
          }
        }
      }

      // 3 Radial Macro Gauges Row (Protein, Carbs, Healthy Fats)
      Text(
        text = "PRESCRIBED MACRO GAUGES (PROTEIN • CARBS • HEALTHY FATS)",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        fontWeight = FontWeight.ExtraBold,
        color = NutritionCyan,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        RadialMacroGaugeTile(
          title = "Protein",
          roleLabel = "Muscle Shield",
          consumedG = analysis.consumedProteinG,
          prescribedG = analysis.prescribedProteinG,
          progress = analysis.proteinProgress,
          accentColor = NutritionCyan,
          kcalPerGram = 4,
          modifier = Modifier.weight(1f),
        )
        RadialMacroGaugeTile(
          title = "Carbs",
          roleLabel = "Training Fuel",
          consumedG = analysis.consumedCarbsG,
          prescribedG = analysis.prescribedCarbsG,
          progress = analysis.carbsProgress,
          accentColor = NutritionEmerald,
          kcalPerGram = 4,
          modifier = Modifier.weight(1f),
        )
        RadialMacroGaugeTile(
          title = "Fats",
          roleLabel = "Hormonal Core",
          consumedG = analysis.consumedFatsG,
          prescribedG = analysis.prescribedFatsG,
          progress = analysis.fatsProgress,
          accentColor = NutritionAmber,
          kcalPerGram = 9,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun EnergyEquationRow(
  label: String,
  value: String,
  color: Color,
  isBold: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = Color.White.copy(alpha = if (isBold) 0.95f else 0.68f),
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
      color = color,
      maxLines = 1,
      softWrap = false,
    )
  }
}

@Composable
private fun RadialMacroGaugeTile(
  title: String,
  roleLabel: String,
  consumedG: Int,
  prescribedG: Int,
  progress: Float,
  accentColor: Color,
  kcalPerGram: Int,
  modifier: Modifier = Modifier,
) {
  val animatedSweep by animateFloatAsState(
    targetValue = progress.coerceIn(0f, 1.0f),
    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
    label = "macro_gauge_$title",
  )
  val pct = (progress * 100f).roundToInt().coerceIn(0, 999)
  val remainingG = (prescribedG - consumedG).coerceAtLeast(0)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = NutritionInnerTileBg,
    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f)),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
        maxLines = 1,
      )

      Box(
        modifier = Modifier.size(72.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeW = 7.dp.toPx()
          val pad = strokeW / 2f + 2.dp.toPx()
          val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
          val topLeft = Offset(pad, pad)

          drawArc(
            color = Color.White.copy(alpha = 0.10f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
          )

          if (animatedSweep > 0.01f) {
            drawArc(
              color = accentColor,
              startAngle = 135f,
              sweepAngle = 270f * animatedSweep,
              useCenter = false,
              topLeft = topLeft,
              size = arcSize,
              style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )
          }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "${consumedG}g",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
          )
          Text(
            text = "$pct%",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = accentColor,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      Text(
        text = "Target ${prescribedG}g",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.82f),
        maxLines = 1,
        softWrap = false,
      )
      Text(
        text = if (remainingG > 0) "${remainingG}g left" else "Goal Hit ✓",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        fontWeight = FontWeight.Bold,
        color = if (remainingG == 0) NutritionEmerald else accentColor,
        maxLines = 1,
        softWrap = false,
      )
      Text(
        text = "$roleLabel • ${consumedG * kcalPerGram} kcal",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = Color.White.copy(alpha = 0.56f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * 2. Interactive End-of-Day Calorie Intake & Weight-Impact Predictor Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalorieIntakePredictorCard(
  profile: AthleteProfile,
  analysis: PrescribedNutritionAnalysis,
  onSetManualRemainingKcal: (Int?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isWithinZone = analysis.isWithinPrescribedWindow
  val zoneAccent = when {
    analysis.predictedDeltaFromPrescribedKcal > 150 -> NutritionAmber
    isWithinZone -> NutritionEmerald
    else -> NutritionCyan
  }

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    border = BorderStroke(1.dp, zoneAccent.copy(alpha = 0.4f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("calorie_predictor_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(SquircleFabShape)
              .background(zoneAccent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.AutoGraph,
              contentDescription = null,
              tint = zoneAccent,
            )
          }
          Column {
            Text(
              text = "EOD Calorie & Weight Predictor",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Predicts End-of-Day total & checks if you stay within prescribed calories",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Surface(
          shape = PillShape,
          color = zoneAccent.copy(alpha = 0.18f),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = if (analysis.predictedDeltaFromPrescribedKcal <= 150) {
                Icons.Default.CheckCircle
              } else {
                Icons.Default.WarningAmber
              },
              contentDescription = null,
              tint = zoneAccent,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = if (analysis.predictedDeltaFromPrescribedKcal <= 150) "In Target" else "+${analysis.predictedDeltaFromPrescribedKcal} kcal",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = zoneAccent,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }

      // 3-Column EOD Forecast Summary Strip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        PredictorMetricBox(
          label = "Projected EOD",
          value = "${analysis.predictedEndOfDayCaloriesKcal} kcal",
          subtext = "Target: ${analysis.prescribedCaloriesKcal} kcal",
          accentColor = zoneAccent,
          modifier = Modifier.weight(1f),
        )
        PredictorMetricBox(
          label = "Net vs TDEE",
          value = if (analysis.predictedDailyNetDeficitKcal >= 0) {
            "-${analysis.predictedDailyNetDeficitKcal} kcal"
          } else {
            "+${abs(analysis.predictedDailyNetDeficitKcal)} kcal"
          },
          subtext = if (analysis.predictedDailyNetDeficitKcal >= 0) "Fat Loss Deficit" else "Calorie Surplus",
          accentColor = if (analysis.predictedDailyNetDeficitKcal >= 0) NutritionEmerald else NutritionAmber,
          modifier = Modifier.weight(1f),
        )
        PredictorMetricBox(
          label = "30-Day Forecast",
          value = "${String.format(Locale.US, "%.1f", analysis.predicted30DayWeightKg)} kg",
          subtext = "${String.format(Locale.US, "%+.2f", analysis.predictedWeeklyWeightChangeKg)} kg/wk",
          accentColor = NutritionBlue,
          modifier = Modifier.weight(1f),
        )
      }

      // Stacked Visual Bar: Consumed + Planned Remaining vs Prescribed Target
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Logged: ${analysis.consumedCaloriesKcal} kcal + Planned Next Meals: ${analysis.estimatedRemainingMealsKcal} kcal",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${((analysis.predictedEndOfDayCaloriesKcal.toFloat() / analysis.prescribedCaloriesKcal.coerceAtLeast(1)) * 100).roundToInt()}% of Prescribed",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = zoneAccent,
            )
          }

          // Custom Stacked Progress Canvas
          Canvas(
            modifier = Modifier
              .fillMaxWidth()
              .height(14.dp)
              .clip(PillShape),
          ) {
            val maxScale = maxOf(analysis.prescribedCaloriesKcal * 1.25f, analysis.predictedEndOfDayCaloriesKcal.toFloat(), 1f)
            val consumedW = (analysis.consumedCaloriesKcal / maxScale) * size.width
            val plannedW = (analysis.estimatedRemainingMealsKcal / maxScale) * size.width
            val targetX = (analysis.prescribedCaloriesKcal / maxScale) * size.width

            drawRect(color = Color.Gray.copy(alpha = 0.2f), size = size)

            if (consumedW > 0f) {
              drawRect(
                color = NutritionBlue,
                topLeft = Offset.Zero,
                size = Size(consumedW.coerceAtMost(size.width), size.height),
              )
            }
            if (plannedW > 0f) {
              drawRect(
                color = zoneAccent.copy(alpha = 0.65f),
                topLeft = Offset(consumedW, 0f),
                size = Size(plannedW.coerceAtMost(size.width - consumedW), size.height),
              )
            }
            // Prescribed Target Marker Line
            drawLine(
              color = NutritionEmerald,
              start = Offset(targetX.coerceIn(2f, size.width - 2f), 0f),
              end = Offset(targetX.coerceIn(2f, size.width - 2f), size.height),
              strokeWidth = 3.dp.toPx(),
            )
          }

          // What-If Simulator Slider for Planned Remaining Meals
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Simulate Next / Remaining Meals Today",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "+${analysis.estimatedRemainingMealsKcal} kcal",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          }

          Slider(
            value = analysis.estimatedRemainingMealsKcal.toFloat().coerceIn(0f, 1600f),
            onValueChange = { raw ->
              val snapped = (Math.round(raw / 25f) * 25).coerceIn(0, 1600)
              onSetManualRemainingKcal(snapped)
            },
            valueRange = 0f..1600f,
            steps = 31,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("predictor_remaining_meals_slider"),
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            val presets = listOf(
              "Auto Pace" to null,
              "Done Eating (+0)" to 0,
              "Light Meal (+350)" to 350,
              "Standard Dinner (+600)" to 600,
              "Big Feast (+950)" to 950,
            )
            presets.forEach { (label, kcalVal) ->
              val selected = (kcalVal == null && !analysis.isUsingManualPredictionOverride) ||
                (kcalVal != null && analysis.isUsingManualPredictionOverride && analysis.estimatedRemainingMealsKcal == kcalVal)
              FilterChip(
                selected = selected,
                onClick = { onSetManualRemainingKcal(kcalVal) },
                label = {
                  Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                  )
                },
                shape = PillShape,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PredictorMetricBox(
  label: String,
  value: String,
  subtext: String,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = accentColor,
        maxLines = 1,
        softWrap = false,
      )
      Text(
        text = subtext,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/**
 * 3. Interactive Hydration Wave & Radial Gauge Card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HydrationWaveGaugeCard(
  analysis: PrescribedNutritionAnalysis,
  onAddWaterMl: (Int) -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "hydration_wave")
  val wavePhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2600, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "wave_phase",
  )

  val animatedFill by animateFloatAsState(
    targetValue = analysis.waterProgress.coerceIn(0f, 1f),
    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
    label = "water_fill_level",
  )

  val waterLiters = analysis.waterMlToday / 1000f
  val targetLiters = analysis.targetWaterMl / 1000f
  val pct = (analysis.waterProgress * 100f).roundToInt().coerceIn(0, 200)

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(containerColor = Color(0xFF081B26)),
    border = BorderStroke(1.dp, NutritionCyan.copy(alpha = 0.38f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("hydration_gauge_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Animated Circular Liquid Tank + Radial Arc Gauge
        Box(
          modifier = Modifier.size(112.dp),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 8.dp.toPx()
            val pad = strokeW / 2f + 3.dp.toPx()
            val radius = (size.minDimension / 2f) - pad - 4.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer Radial Progress Ring
            drawArc(
              color = Color.White.copy(alpha = 0.12f),
              startAngle = -90f,
              sweepAngle = 360f,
              useCenter = false,
              topLeft = Offset(pad, pad),
              size = Size(size.width - pad * 2, size.height - pad * 2),
              style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )

            drawArc(
              brush = Brush.sweepGradient(listOf(NutritionCyan, NutritionBlue, NutritionEmerald)),
              startAngle = -90f,
              sweepAngle = 360f * animatedFill,
              useCenter = false,
              topLeft = Offset(pad, pad),
              size = Size(size.width - pad * 2, size.height - pad * 2),
              style = Stroke(width = strokeW, cap = StrokeCap.Round),
            )

            // Inner clipped circular liquid wave
            val circleClip = Path().apply {
              addOval(
                androidx.compose.ui.geometry.Rect(
                  center.x - radius,
                  center.y - radius,
                  center.x + radius,
                  center.y + radius,
                )
              )
            }

            clipPath(circleClip) {
              drawRect(color = Color(0xFF0E293B))
              val waterTopY = (center.y + radius) - (2f * radius * animatedFill)
              val wavePath = Path().apply {
                moveTo(center.x - radius, center.y + radius)
                lineTo(center.x - radius, waterTopY)
                val step = 6f
                var x = center.x - radius
                while (x <= center.x + radius + step) {
                  val normX = (x - (center.x - radius)) / (2f * radius)
                  val yOffset = sin(normX * 2f * PI + wavePhase).toFloat() * 4.dp.toPx()
                  lineTo(x, waterTopY + yOffset)
                  x += step
                }
                lineTo(center.x + radius, center.y + radius)
                close()
              }

              drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                  colors = listOf(
                    NutritionCyan.copy(alpha = 0.65f),
                    NutritionBlue.copy(alpha = 0.85f),
                  )
                ),
              )
            }
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.LocalDrink,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "$pct%",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
            )
            Text(
              text = "${String.format(Locale.US, "%.2f", waterLiters)}L",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = 0.9f),
            )
          }
        }

        // Right Column: Hydration Readout & Cellular Lipolysis Status
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "HYDRATION & LIPOLYSIS GAUGE",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.ExtraBold,
            color = NutritionCyan,
          )
          Text(
            text = "${analysis.waterMlToday} / ${analysis.targetWaterMl} ml",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
          )
          Text(
            text = "${analysis.waterGlassesToday} of ${analysis.targetWaterGlasses} glasses (250ml) • Target ${String.format(Locale.US, "%.1f", targetLiters)}L calibrated to your weight & workout sweat.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f),
          )
        }
      }

      // 1-Tap Vessel Logger Buttons
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        VesselQuickButton(
          label = "-250ml",
          sub = "Undo",
          accent = NutritionCoral,
          onClick = { onAddWaterMl(-250) },
          tag = "water_btn_minus_250",
        )
        VesselQuickButton(
          label = "+150ml",
          sub = "Small Cup",
          accent = NutritionCyan,
          onClick = { onAddWaterMl(150) },
          tag = "water_btn_plus_150",
        )
        VesselQuickButton(
          label = "+250ml",
          sub = "1 Glass",
          accent = NutritionCyan,
          onClick = { onAddWaterMl(250) },
          tag = "water_btn_plus_250",
        )
        VesselQuickButton(
          label = "+500ml",
          sub = "Gym Shaker",
          accent = NutritionEmerald,
          onClick = { onAddWaterMl(500) },
          tag = "water_btn_plus_500",
        )
        VesselQuickButton(
          label = "+750ml",
          sub = "Sipper Bottle",
          accent = NutritionBlue,
          onClick = { onAddWaterMl(750) },
          tag = "water_btn_plus_750",
        )
      }
    }
  }
}

@Composable
private fun VesselQuickButton(
  label: String,
  sub: String,
  accent: Color,
  onClick: () -> Unit,
  tag: String,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = accent.copy(alpha = 0.15f),
    border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag(tag),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
      )
      Text(
        text = sub,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
        color = accent,
      )
    }
  }
}

/**
 * 4. Main Dashboard Mini-Summary Card (Embedded directly on TodayScreen)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardNutritionMiniCard(
  analysis: PrescribedNutritionAnalysis,
  topQuickPresets: List<FoodQuickPreset>,
  onQuickLogPreset: (FoodQuickPreset) -> Unit,
  onAddWaterGlass: () -> Unit,
  onOpenNutritionScreen: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val statusColor = when (analysis.complianceZoneCode) {
    1 -> NutritionEmerald
    2 -> NutritionAmber
    else -> NutritionCyan
  }

  Card(
    shape = WorkspaceCardShape,
    colors = CardDefaults.cardColors(containerColor = NutritionDarkCardBg),
    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.38f)),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onOpenNutritionScreen() }
      .testTag("dashboard_nutrition_mini_card"),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.weight(1f),
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(SquircleFabShape)
              .background(NutritionEmerald.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = null,
              tint = NutritionEmerald,
            )
          }
          Column {
            Text(
              text = "NUTRITION & HYDRATION TELEMETRY",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.ExtraBold,
              color = NutritionCyan,
            )
            Text(
              text = "Prescribed Fuel & Custom Quick Presets",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
            )
          }
        }

        Surface(
          shape = PillShape,
          color = statusColor.copy(alpha = 0.18f),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Studio",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = statusColor,
            )
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Open Nutrition Studio",
              tint = statusColor,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }

      // Middle Row: Dual Concentric Mini Gauge (Calories + Hydration) + Macro Bars
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Dual Ring Mini Gauge
        Box(
          modifier = Modifier.size(86.dp),
          contentAlignment = Alignment.Center,
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val outerStroke = 8.dp.toPx()
            val innerStroke = 6.dp.toPx()
            val outerPad = outerStroke / 2f + 2.dp.toPx()
            val innerPad = outerPad + outerStroke + 4.dp.toPx()

            // Outer Calorie Ring
            drawArc(
              color = Color.White.copy(alpha = 0.12f),
              startAngle = 140f,
              sweepAngle = 260f,
              useCenter = false,
              topLeft = Offset(outerPad, outerPad),
              size = Size(size.width - outerPad * 2, size.height - outerPad * 2),
              style = Stroke(width = outerStroke, cap = StrokeCap.Round),
            )
            drawArc(
              color = statusColor,
              startAngle = 140f,
              sweepAngle = 260f * analysis.calorieProgress.coerceIn(0f, 1f),
              useCenter = false,
              topLeft = Offset(outerPad, outerPad),
              size = Size(size.width - outerPad * 2, size.height - outerPad * 2),
              style = Stroke(width = outerStroke, cap = StrokeCap.Round),
            )

            // Inner Hydration Ring
            drawArc(
              color = Color.White.copy(alpha = 0.10f),
              startAngle = 140f,
              sweepAngle = 260f,
              useCenter = false,
              topLeft = Offset(innerPad, innerPad),
              size = Size(size.width - innerPad * 2, size.height - innerPad * 2),
              style = Stroke(width = innerStroke, cap = StrokeCap.Round),
            )
            drawArc(
              color = NutritionCyan,
              startAngle = 140f,
              sweepAngle = 260f * analysis.waterProgress.coerceIn(0f, 1f),
              useCenter = false,
              topLeft = Offset(innerPad, innerPad),
              size = Size(size.width - innerPad * 2, size.height - innerPad * 2),
              style = Stroke(width = innerStroke, cap = StrokeCap.Round),
            )
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${analysis.consumedCaloriesKcal}",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Black,
              color = Color.White,
            )
            Text(
              text = "/${analysis.prescribedCaloriesKcal}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = Color.White.copy(alpha = 0.7f),
            )
          }
        }

        // Compact Macro & Hydration Readout
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = analysis.complianceStatusTitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = statusColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          MiniMacroBarRow(
            label = "Protein",
            value = "${analysis.consumedProteinG}/${analysis.prescribedProteinG}g",
            progress = analysis.proteinProgress,
            color = NutritionCyan,
          )
          MiniMacroBarRow(
            label = "Carbs",
            value = "${analysis.consumedCarbsG}/${analysis.prescribedCarbsG}g",
            progress = analysis.carbsProgress,
            color = NutritionEmerald,
          )
          MiniMacroBarRow(
            label = "Hydration",
            value = "${String.format(Locale.US, "%.2f", analysis.waterMlToday / 1000f)} / ${String.format(Locale.US, "%.1f", analysis.targetWaterMl / 1000f)}L",
            progress = analysis.waterProgress,
            color = NutritionBlue,
          )
        }
      }

      // Bottom Strip: 1-Tap Quick Food Presets + "+250ml Water" directly from Dashboard!
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = PillShape,
          color = NutritionCyan.copy(alpha = 0.16f),
          border = BorderStroke(1.dp, NutritionCyan.copy(alpha = 0.45f)),
          modifier = Modifier
            .clip(PillShape)
            .clickable { onAddWaterGlass() }
            .testTag("dashboard_quick_water_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.LocalDrink,
              contentDescription = null,
              tint = NutritionCyan,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = "+250ml Water",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = NutritionCyan,
              maxLines = 1,
              softWrap = false,
            )
          }
        }

        topQuickPresets.take(3).forEach { preset ->
          Surface(
            shape = PillShape,
            color = NutritionInnerTileBg,
            border = BorderStroke(
              1.dp,
              if (preset.isCustom) NutritionAmber.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.16f),
            ),
            modifier = Modifier
              .clip(PillShape)
              .clickable { onQuickLogPreset(preset) },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
              Text(text = preset.badgeEmoji, fontSize = 12.sp)
              Text(
                text = "+ ${preset.name} (${preset.caloriesKcal} kcal)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MiniMacroBarRow(
  label: String,
  value: String,
  progress: Float,
  color: Color,
) {
  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = Color.White.copy(alpha = 0.72f),
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        fontWeight = FontWeight.Bold,
        color = color,
      )
    }
    LinearProgressIndicator(
      progress = { progress.coerceIn(0f, 1f) },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(PillShape),
      color = color,
      trackColor = Color.White.copy(alpha = 0.12f),
    )
  }
}
