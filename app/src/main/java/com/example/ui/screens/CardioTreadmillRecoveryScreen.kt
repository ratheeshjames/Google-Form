package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoiceCoachManager
import com.example.audio.WorkoutCoachNarration
import com.example.data.AthleteProfile
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.WorkoutLogEntity
import com.example.model.CalibratedCardioRecoveryPlan
import com.example.model.CardioRecoveryBlock
import com.example.model.CardioRecoveryPillar
import com.example.model.CardioRecoveryProtocolRepository
import com.example.ui.theme.PillShape
import com.example.ui.util.celebrationHaptic
import com.example.ui.util.confirmHaptic
import com.example.ui.util.tickHaptic
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

// Distinct Biometric Telemetry & Active Recovery Palette (Decoupled from Dumbbell Strength UI)
private val TelemetryBgDeep = Color(0xFF041317)
private val TelemetryCardSurface = Color(0xFF0A2128)
private val TelemetryTileSurface = Color(0xFF102D36)
private val TelemetryCyan = Color(0xFF00E5FF)
private val TelemetryEmerald = Color(0xFF10B981)
private val TelemetryLavender = Color(0xFFA78BFA)
private val TelemetryCoral = Color(0xFFFF8A65)
private val TelemetryBorder = Color(0xFF1C4654)

/**
 * Completely Decoupled Sunday Cardio, Treadmill & Active Recovery Console.
 * Uses a Continuous Multi-Pillar Telemetry & Regeneration methodology rather than dumbbell sets/reps.
 */
@Composable
fun CardioTreadmillRecoveryScreen(
  onClosePlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val view = LocalView.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

  DisposableEffect(view, context) {
    val prev = view.keepScreenOn
    view.keepScreenOn = true
    val window = (context as? Activity)?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      view.keepScreenOn = prev
      window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  val voiceCoach = remember { VoiceCoachManager(context, languageCode = "en", speechRate = 1.03f) }
  var liveTelemetrySpeech by remember {
    mutableStateOf("Treadmill & Active Recovery Telemetry ready.")
  }

  DisposableEffect(Unit) {
    voiceCoach.onCoachSpoke = { speech ->
      liveTelemetrySpeech = speech
    }
    onDispose {
      voiceCoach.shutdown()
    }
  }

  // Customizable Cardio Duration (15..90 min, default 35m) & Cardio Difficulty (Lv.1..5, default Lv.2)
  var cardioMinutes by remember(activeProfile.cardioDurationMin) {
    mutableIntStateOf(activeProfile.cardioDurationMin.coerceIn(15, 90))
  }
  var cardioDifficulty by remember(activeProfile.cardioDifficultyLevel) {
    mutableIntStateOf(activeProfile.cardioDifficultyLevel.coerceIn(1, 5))
  }
  var showCalibrationDrawer by remember { mutableStateOf(false) }

  val calibratedPlan: CalibratedCardioRecoveryPlan = remember(
    cardioMinutes,
    cardioDifficulty,
    activeProfile.weightKg,
  ) {
    CardioRecoveryProtocolRepository.buildCalibratedPlan(
      durationMinutes = cardioMinutes,
      difficultyLevel = cardioDifficulty,
      athleteWeightKg = activeProfile.weightKg,
    )
  }

  var currentBlockIndex by remember(cardioMinutes, cardioDifficulty) { mutableIntStateOf(0) }
  val currentBlock: CardioRecoveryBlock = calibratedPlan.blocks.getOrElse(currentBlockIndex) {
    calibratedPlan.blocks.first()
  }

  var blockSecondsRemaining by remember(cardioMinutes, cardioDifficulty, currentBlockIndex) {
    mutableIntStateOf(currentBlock.durationSeconds)
  }
  var isPaused by remember { mutableStateOf(false) }
  var isMuted by remember { mutableStateOf(false) }
  var isSessionFinished by remember { mutableStateOf(false) }

  // Total elapsed and remaining seconds across the entire 7-block continuous flow
  val elapsedBeforeCurrentBlock = remember(calibratedPlan, currentBlockIndex) {
    calibratedPlan.blocks.take(currentBlockIndex).sumOf { it.durationSeconds }
  }
  val elapsedInCurrentBlock = (currentBlock.durationSeconds - blockSecondsRemaining).coerceAtLeast(0)
  val totalElapsedSeconds = (elapsedBeforeCurrentBlock + elapsedInCurrentBlock).coerceIn(0, calibratedPlan.totalDurationSeconds)
  val totalSecondsRemaining = (calibratedPlan.totalDurationSeconds - totalElapsedSeconds).coerceAtLeast(0)

  // Live accumulated distance (km) during treadmill blocks
  val accumulatedTreadmillKm = remember(calibratedPlan, currentBlockIndex, elapsedInCurrentBlock) {
    var distKm = 0f
    calibratedPlan.blocks.forEachIndexed { idx, blk ->
      if (blk.isTreadmillBlock) {
        val secsSpent = when {
          idx < currentBlockIndex -> blk.durationSeconds
          idx == currentBlockIndex -> elapsedInCurrentBlock
          else -> 0
        }
        distKm += (blk.speedKmh * (secsSpent / 3600f))
      }
    }
    distKm
  }

  LaunchedEffect(isMuted) {
    voiceCoach.isEnabled = !isMuted
  }

  fun announceCurrentBlock(block: CardioRecoveryBlock) {
    voiceCoach.speakCoach(
      block.voiceStartAnnouncementEn,
      block.voiceStartAnnouncementMl,
      flush = true,
    )
  }

  fun completeCardioRecoverySession() {
    if (isSessionFinished) return
    isSessionFinished = true
    AthleteProfileStore.awardWorkoutXp(xpAmount = 160, incrementWorkout = true, context = context)
    kotlinx.coroutines.MainScope().launch {
      GoogleFormDatabase.getInstance(context).workoutDao().insertWorkoutLog(
        WorkoutLogEntity(
          timestamp = System.currentTimeMillis(),
          dayNumber = 7,
          workoutName = "Sunday Cardio, Treadmill & Recovery (${calibratedPlan.difficultyTitle})",
          durationSeconds = calibratedPlan.totalDurationSeconds,
          totalVolumeKg = calibratedPlan.estimatedCaloriesBurned.toDouble(),
          setsCompleted = calibratedPlan.blocks.size,
          setsDeferred = 0,
          sessionRating = 5,
        )
      )
    }
    haptic.celebrationHaptic(scope)
    voiceCoach.speakCoach(
      "Sunday Cardio, Treadmill, and Active Recovery complete! You burned ${calibratedPlan.estimatedCaloriesBurned} calories and boosted Monday muscle readiness to ${calibratedPlan.recoveryReadinessBoostPct} percent!",
      "ഞായറാഴ്ചത്തെ കാർഡിയോ ആൻഡ് റിക്കവറി സെഷൻ പൂർത്തിയായി! അഭിനന്ദനങ്ങൾ!",
      flush = true,
    )
  }

  fun advanceToNextBlock() {
    if (currentBlockIndex < calibratedPlan.blocks.lastIndex) {
      voiceCoach.playCountdownBeep(isFinalGo = true)
      haptic.confirmHaptic()
      val nextIdx = currentBlockIndex + 1
      currentBlockIndex = nextIdx
      val nextBlk = calibratedPlan.blocks[nextIdx]
      blockSecondsRemaining = nextBlk.durationSeconds
      announceCurrentBlock(nextBlk)
    } else {
      completeCardioRecoverySession()
    }
  }

  LaunchedEffect(cardioMinutes, cardioDifficulty) {
    isSessionFinished = false
    announceCurrentBlock(calibratedPlan.blocks.first())
  }

  LaunchedEffect(isPaused, isSessionFinished, currentBlockIndex, cardioMinutes, cardioDifficulty) {
    if (isPaused || isSessionFinished) return@LaunchedEffect
    while (isActive && !isSessionFinished) {
      delay(1000L)
      if (blockSecondsRemaining > 1) {
        blockSecondsRemaining -= 1
        val sec = blockSecondsRemaining
        if (sec in 1..5) {
          haptic.tickHaptic()
          voiceCoach.playCountdownBeep(isFinalGo = false)
          val (wordEn, wordMl) = WorkoutCoachNarration.getSpokenCountdownWord(sec)
          voiceCoach.speakCoach(wordEn, wordMl, flush = true, isCountdownTick = true)
          liveTelemetrySpeech = "Next phase in $sec... ($wordEn)"
        } else if (sec == currentBlock.durationSeconds / 2 && sec > 20) {
          val midCue = if (currentBlock.isTreadmillBlock) {
            "Halfway through ${currentBlock.title}. Hold ${currentBlock.inclinePercent}% incline at ${currentBlock.speedKmh} kilometers per hour. ${currentBlock.secondaryBreathingCue}."
          } else {
            "Halfway through ${currentBlock.title}. ${currentBlock.secondaryBreathingCue}."
          }
          voiceCoach.speakCoach(midCue, midCue, flush = true)
        }
      } else {
        advanceToNextBlock()
      }
    }
  }

  val pillarAccentColor by animateColorAsState(
    targetValue = when (currentBlock.pillar) {
      CardioRecoveryPillar.PILLAR_1_JOINT_PREP -> TelemetryEmerald
      CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO -> TelemetryCyan
      CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY -> TelemetryCoral
      CardioRecoveryPillar.PILLAR_4_CNS_PARASYMPATHETIC -> TelemetryLavender
    },
    animationSpec = tween(450),
    label = "pillarAccentColor",
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            pillarAccentColor.copy(alpha = 0.16f),
            TelemetryBgDeep,
            TelemetryBgDeep,
          )
        )
      )
      .testTag("cardio_treadmill_recovery_screen"),
  ) {
    if (isSessionFinished) {
      CardioRecoveryCompletionReceipt(
        plan = calibratedPlan,
        distanceKm = accumulatedTreadmillKm,
        onFinish = onClosePlayer,
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // 1. Top Telemetry Bar (Exit, Constrained Clock, Calibrate Button, Voice Toggle)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onClosePlayer,
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(TelemetryCardSurface),
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Exit Cardio Console",
              tint = Color.White,
            )
          }

          Surface(
            shape = PillShape,
            color = TelemetryCardSurface,
            border = BorderStroke(1.dp, pillarAccentColor.copy(alpha = 0.5f)),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = pillarAccentColor,
                modifier = Modifier.size(16.dp),
              )
              val remM = totalSecondsRemaining / 60
              val remS = totalSecondsRemaining % 60
              Text(
                text = String.format(Locale.US, "%02d:%02d TOTAL • Lv.%d", remM, remS, cardioDifficulty),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                softWrap = false,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
              onClick = { showCalibrationDrawer = !showCalibrationDrawer },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (showCalibrationDrawer) pillarAccentColor.copy(alpha = 0.28f) else TelemetryCardSurface),
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Calibrate Duration & Difficulty",
                tint = if (showCalibrationDrawer) pillarAccentColor else Color.White,
              )
            }

            IconButton(
              onClick = { isMuted = !isMuted },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(TelemetryCardSurface),
            ) {
              Icon(
                imageVector = if (isMuted) Icons.Default.VoiceOverOff else Icons.Default.RecordVoiceOver,
                contentDescription = "Toggle Voice Telemetry",
                tint = if (isMuted) Color.White.copy(alpha = 0.5f) else pillarAccentColor,
              )
            }
          }
        }

        // 2. Collapsible In-Session Cardio Duration (15-90m) & Difficulty (Lv.1-5) Calibration Drawer
        AnimatedVisibility(visible = showCalibrationDrawer) {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = TelemetryCardSurface,
            border = BorderStroke(1.dp, TelemetryCyan.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "LIVE CARDIO & RECOVERY CALIBRATION",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = TelemetryCyan,
                )
                Text(
                  text = "${cardioMinutes}m • Lv.$cardioDifficulty",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Black,
                  color = Color.White,
                )
              }

              // Cardio Duration Slider (15m to 90m in 5m increments)
              Text(
                text = "Cardio + Recovery Duration: $cardioMinutes Minutes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
              Slider(
                value = cardioMinutes.toFloat(),
                onValueChange = { raw ->
                  val snapped = ((raw / 5f).roundToInt() * 5).coerceIn(15, 90)
                  if (snapped != cardioMinutes) {
                    cardioMinutes = snapped
                    AthleteProfileStore.updateActiveProfile(context) {
                      it.copy(cardioDurationMin = snapped)
                    }
                  }
                },
                valueRange = 15f..90f,
                steps = 14,
                colors = SliderDefaults.colors(
                  thumbColor = TelemetryCyan,
                  activeTrackColor = TelemetryCyan,
                ),
              )

              // Cardio Difficulty Slider (Level 1 to Level 5)
              Text(
                text = "Intensity & Recovery Balance: ${calibratedPlan.difficultyTitle}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
              Slider(
                value = cardioDifficulty.toFloat(),
                onValueChange = { raw ->
                  val lvl = raw.roundToInt().coerceIn(1, 5)
                  if (lvl != cardioDifficulty) {
                    cardioDifficulty = lvl
                    AthleteProfileStore.updateActiveProfile(context) {
                      it.copy(cardioDifficultyLevel = lvl)
                    }
                  }
                },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                  thumbColor = TelemetryEmerald,
                  activeTrackColor = TelemetryEmerald,
                ),
              )
            }
          }
        }

        // 3. 4-Pillar Active Regeneration Header & Continuous Elevation Wave Timeline
        Surface(
          shape = RoundedCornerShape(24.dp),
          color = TelemetryCardSurface,
          border = BorderStroke(1.dp, TelemetryBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                shape = PillShape,
                color = pillarAccentColor.copy(alpha = 0.18f),
              ) {
                Text(
                  text = currentBlock.pillar.title,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = pillarAccentColor,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }

              Text(
                text = "Block ${currentBlock.blockNumber} of ${calibratedPlan.blocks.size}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                softWrap = false,
              )
            }

            Text(
              text = currentBlock.title,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = Color.White,
            )

            Text(
              text = currentBlock.subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.78f),
            )

            // Interactive 7-Block Elevation & Recovery Profile Graph
            CardioElevationWaveVisualizer(
              blocks = calibratedPlan.blocks,
              activeBlockIndex = currentBlockIndex,
              blockProgress = 1f - (blockSecondsRemaining.toFloat() / currentBlock.durationSeconds.coerceAtLeast(1)),
              onSelectBlock = { idx ->
                currentBlockIndex = idx
                val blk = calibratedPlan.blocks[idx]
                blockSecondsRemaining = blk.durationSeconds
                announceCurrentBlock(blk)
              },
            )
          }
        }

        // 4. Primary Telemetry Cockpit:
        //    - If Treadmill Block: Dual-Dial Incline (%) & Speed (km/h) Cockpit + Cadence/HR Zone
        //    - If Mobility / CNS Block: Guided Myofascial Decompression & 4-4-6 Breath Pacer Ring
        if (currentBlock.isTreadmillBlock) {
          TreadmillDualDialCockpitCard(
            block = currentBlock,
            blockSecondsRemaining = blockSecondsRemaining,
            distanceKm = accumulatedTreadmillKm,
            accentColor = pillarAccentColor,
          )
        } else {
          ActiveRecoveryAndBreathworkCockpitCard(
            block = currentBlock,
            blockSecondsRemaining = blockSecondsRemaining,
            accentColor = pillarAccentColor,
          )
        }

        // 5. Muscle Recovery Target & Live Voice Telemetry Strip
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = TelemetryCardSurface,
          border = BorderStroke(1.dp, TelemetryBorder),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = TelemetryEmerald,
                modifier = Modifier.size(18.dp),
              )
              Text(
                text = "Recovering: ${currentBlock.recoveryTargetMuscles}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TelemetryEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }

            Text(
              text = currentBlock.primaryCoachDirective,
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f),
            )

            Surface(
              shape = RoundedCornerShape(12.dp),
              color = TelemetryTileSurface,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Air,
                  contentDescription = null,
                  tint = pillarAccentColor,
                  modifier = Modifier.size(16.dp),
                )
                Text(
                  text = currentBlock.secondaryBreathingCue,
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White.copy(alpha = 0.85f),
                )
              }
            }
          }
        }

        // 6. Transport Controls (Pause/Resume + Skip to Next Phase)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Button(
            onClick = { isPaused = !isPaused },
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isPaused) TelemetryEmerald else pillarAccentColor,
              contentColor = Color(0xFF031418),
            ),
            modifier = Modifier
              .weight(1f)
              .height(54.dp),
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isPaused) "Resume Flow" else "Pause Flow",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              softWrap = false,
            )
          }

          OutlinedButton(
            onClick = { advanceToNextBlock() },
            shape = PillShape,
            border = BorderStroke(1.dp, pillarAccentColor.copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            modifier = Modifier.height(54.dp),
          ) {
            Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (currentBlockIndex < calibratedPlan.blocks.lastIndex) "Next Block" else "Finish",
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TreadmillDualDialCockpitCard(
  block: CardioRecoveryBlock,
  blockSecondsRemaining: Int,
  distanceKm: Float,
  accentColor: Color,
) {
  val animatedIncline by animateFloatAsState(
    targetValue = block.inclinePercent,
    animationSpec = tween(600, easing = FastOutSlowInEasing),
    label = "inclineAnim",
  )
  val animatedSpeed by animateFloatAsState(
    targetValue = block.speedKmh,
    animationSpec = tween(600, easing = FastOutSlowInEasing),
    label = "speedAnim",
  )

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = TelemetryCardSurface,
    border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.45f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // Top Phase Countdown & Heart Rate Zone Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "TREADMILL TELEMETRY TARGET",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
          )
          val m = blockSecondsRemaining / 60
          val s = blockSecondsRemaining % 60
          Text(
            text = String.format(Locale.US, "%02d:%02d Block Remaining", m, s),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
          )
        }

        Surface(
          shape = PillShape,
          color = TelemetryEmerald.copy(alpha = 0.18f),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Favorite,
              contentDescription = null,
              tint = TelemetryEmerald,
              modifier = Modifier.size(14.dp),
            )
            Text(
              text = block.targetBpmRange,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = TelemetryEmerald,
              maxLines = 1,
              softWrap = false,
            )
          }
        }
      }

      // Dual Circular Telemetry Gauges: INCLINE (%) & BELT SPEED (KM/H)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        TelemetryArcGauge(
          label = "INCLINE GRADE",
          valueText = String.format(Locale.US, "%.1f%%", animatedIncline),
          subText = if (animatedIncline >= 8f) "Steep Glute/Calf" else "Zone-2 Grade",
          fraction = (animatedIncline / 15f).coerceIn(0.05f, 1f),
          gaugeColor = TelemetryEmerald,
          modifier = Modifier.weight(1f),
        )

        TelemetryArcGauge(
          label = "BELT SPEED",
          valueText = String.format(Locale.US, "%.1f km/h", animatedSpeed),
          subText = "Pace ${block.paceMinPerKmFormatted}",
          fraction = (animatedSpeed / 14f).coerceIn(0.08f, 1f),
          gaugeColor = TelemetryCyan,
          modifier = Modifier.weight(1f),
        )
      }

      // Bottom Telemetry Strip: HR Zone, Step Cadence, Live Distance
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        TelemetryMiniTile(
          label = "Target Zone",
          value = block.targetHeartRateZone.substringBefore("•").trim(),
          color = TelemetryEmerald,
          modifier = Modifier.weight(1f),
        )
        TelemetryMiniTile(
          label = "Step Cadence",
          value = block.cadenceOrTempoCue.substringBefore("•").trim(),
          color = TelemetryCyan,
          modifier = Modifier.weight(1f),
        )
        TelemetryMiniTile(
          label = "Belt Distance",
          value = String.format(Locale.US, "%.2f km", distanceKm),
          color = Color.White,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun ActiveRecoveryAndBreathworkCockpitCard(
  block: CardioRecoveryBlock,
  blockSecondsRemaining: Int,
  accentColor: Color,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "breathRing")
  val breathScale by infiniteTransition.animateFloat(
    initialValue = 0.72f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "breathScale",
  )

  val cycleSec = (block.durationSeconds - blockSecondsRemaining).coerceAtLeast(0) % 14
  val breathPhaseLabel = when {
    cycleSec < 4 -> "INHALE SLOWLY (4s)"
    cycleSec < 8 -> "HOLD & EXPAND RIBCAGE (4s)"
    else -> "LONG EXHALE & SINK DEEPER (6s)"
  }

  Surface(
    shape = RoundedCornerShape(26.dp),
    color = TelemetryCardSurface,
    border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.45f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "ACTIVE RECOVERY & CNS DECOMPRESSION",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.ExtraBold,
          color = accentColor,
        )
        Text(
          text = block.targetBpmRange,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = TelemetryLavender,
        )
      }

      // Animated Breath & Mobility Decompression Ring
      Box(
        modifier = Modifier.size(168.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val maxRadius = size.minDimension / 2f - 10.dp.toPx()
          drawCircle(
            color = accentColor.copy(alpha = 0.14f),
            radius = maxRadius * breathScale,
            center = center,
          )
          drawCircle(
            color = accentColor.copy(alpha = 0.30f),
            radius = maxRadius * (breathScale * 0.82f),
            center = center,
          )
          drawCircle(
            color = accentColor,
            radius = maxRadius,
            center = center,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round),
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          val m = blockSecondsRemaining / 60
          val s = blockSecondsRemaining % 60
          Text(
            text = String.format(Locale.US, "%02d:%02d", m, s),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
          )
          Text(
            text = "Off-Treadmill Mat",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = accentColor,
          )
        }
      }

      Surface(
        shape = PillShape,
        color = accentColor.copy(alpha = 0.18f),
      ) {
        Text(
          text = breathPhaseLabel,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = accentColor,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        )
      }

      Text(
        text = block.cadenceOrTempoCue,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.White.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun TelemetryArcGauge(
  label: String,
  valueText: String,
  subText: String,
  fraction: Float,
  gaugeColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = TelemetryTileSurface,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White.copy(alpha = 0.7f),
        maxLines = 1,
        softWrap = false,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Box(
        modifier = Modifier.size(104.dp),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val strokeW = 9.dp.toPx()
          val arcSize = Size(size.width - strokeW, size.height - strokeW)
          val topLeft = Offset(strokeW / 2f, strokeW / 2f)
          drawArc(
            color = Color.White.copy(alpha = 0.12f),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
          )
          drawArc(
            color = gaugeColor,
            startAngle = 140f,
            sweepAngle = 260f * fraction.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeW, cap = StrokeCap.Round),
          )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = valueText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
          )
          Text(
            text = subText,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = gaugeColor,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

@Composable
private fun TelemetryMiniTile(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = TelemetryTileSurface,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.65f),
        maxLines = 1,
        softWrap = false,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        color = color,
        maxLines = 1,
        softWrap = false,
      )
    }
  }
}

@Composable
private fun CardioElevationWaveVisualizer(
  blocks: List<CardioRecoveryBlock>,
  activeBlockIndex: Int,
  blockProgress: Float,
  onSelectBlock: (Int) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(58.dp),
      horizontalArrangement = Arrangement.spacedBy(5.dp),
      verticalAlignment = Alignment.Bottom,
    ) {
      blocks.forEachIndexed { idx, blk ->
        val isCurrent = idx == activeBlockIndex
        val isCompleted = idx < activeBlockIndex
        val barColor = when (blk.pillar) {
          CardioRecoveryPillar.PILLAR_1_JOINT_PREP -> TelemetryEmerald
          CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO -> TelemetryCyan
          CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY -> TelemetryCoral
          CardioRecoveryPillar.PILLAR_4_CNS_PARASYMPATHETIC -> TelemetryLavender
        }
        val heightFraction = when {
          blk.isTreadmillBlock -> ((blk.inclinePercent + blk.speedKmh * 0.7f) / 18f).coerceIn(0.38f, 1.0f)
          blk.pillar == CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY -> 0.42f
          else -> 0.30f
        }

        Column(
          modifier = Modifier
            .weight(1f)
            .clickable { onSelectBlock(idx) },
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Bottom,
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height((42 * heightFraction).dp)
              .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
              .background(
                when {
                  isCurrent -> barColor
                  isCompleted -> barColor.copy(alpha = 0.55f)
                  else -> barColor.copy(alpha = 0.22f)
                }
              ),
          )
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = "B${idx + 1}",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }

    LinearProgressIndicator(
      progress = {
        ((activeBlockIndex + blockProgress.coerceIn(0f, 1f)) / blocks.size.coerceAtLeast(1)).coerceIn(0f, 1f)
      },
      modifier = Modifier
        .fillMaxWidth()
        .height(5.dp)
        .clip(PillShape),
      color = TelemetryCyan,
      trackColor = TelemetryTileSurface,
    )
  }
}

@Composable
private fun CardioRecoveryCompletionReceipt(
  plan: CalibratedCardioRecoveryPlan,
  distanceKm: Float,
  onFinish: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = TelemetryCardSurface,
      border = BorderStroke(1.5.dp, TelemetryEmerald.copy(alpha = 0.6f)),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(TelemetryEmerald.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = TelemetryEmerald,
            modifier = Modifier.size(36.dp),
          )
        }

        Text(
          text = "SUNDAY CARDIO & RECOVERY COMPLETE",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.ExtraBold,
          color = TelemetryEmerald,
        )

        Text(
          text = "Monday Strength Readiness: ${plan.recoveryReadinessBoostPct}%",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Black,
          color = Color.White,
          textAlign = TextAlign.Center,
        )

        Text(
          text = "Your cardiovascular system, joints, and CNS are decompressed and primed for the upcoming week's dumbbell split (+160 Rooster XP earned).",
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.8f),
          textAlign = TextAlign.Center,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          TelemetryMiniTile(
            label = "Treadmill",
            value = "${plan.treadmillMinutes}m (${String.format(Locale.US, "%.2f", distanceKm)} km)",
            color = TelemetryCyan,
            modifier = Modifier.weight(1f),
          )
          TelemetryMiniTile(
            label = "Recovery & CNS",
            value = "${plan.mobilityAndRecoveryMinutes}m Unwind",
            color = TelemetryLavender,
            modifier = Modifier.weight(1f),
          )
          TelemetryMiniTile(
            label = "Oxidized",
            value = "${plan.estimatedCaloriesBurned} kcal",
            color = TelemetryEmerald,
            modifier = Modifier.weight(1f),
          )
        }

        Button(
          onClick = onFinish,
          shape = PillShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = TelemetryEmerald,
            contentColor = Color(0xFF031418),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        ) {
          Text(
            text = "Return to Dashboard",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
          )
        }
      }
    }
  }
}

/**
 * Decoupled Sunday Cardio, Treadmill & Active Recovery Studio Card for TodayScreen & ProgrammeScreen.
 * Uses a completely distinct Emerald/Cyan Telemetry design language with built-in Duration (15-90m)
 * and Difficulty/Recovery (Lv.1-5) sliders and a 4-Pillar Recovery Readiness breakdown.
 */
@Composable
fun DecoupledCardioRecoveryStudioHub(
  activeProfile: AthleteProfile,
  onLaunchCardioConsole: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val plan = remember(
    activeProfile.cardioDurationMin,
    activeProfile.cardioDifficultyLevel,
    activeProfile.weightKg,
  ) {
    CardioRecoveryProtocolRepository.buildCalibratedPlan(
      durationMinutes = activeProfile.cardioDurationMin,
      difficultyLevel = activeProfile.cardioDifficultyLevel,
      athleteWeightKg = activeProfile.weightKg,
    )
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Main Telemetry Studio Card
    Card(
      shape = RoundedCornerShape(26.dp),
      colors = CardDefaults.cardColors(containerColor = TelemetryCardSurface),
      border = BorderStroke(1.5.dp, TelemetryCyan.copy(alpha = 0.42f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("decoupled_cardio_recovery_hub"),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Header Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Surface(
            shape = PillShape,
            color = TelemetryEmerald.copy(alpha = 0.18f),
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = TelemetryEmerald,
                modifier = Modifier.size(16.dp),
              )
              Text(
                text = "SUNDAY CARDIO, TREADMILL & RECOVERY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TelemetryEmerald,
                maxLines = 1,
                softWrap = false,
              )
            }
          }

          Surface(
            shape = PillShape,
            color = TelemetryCyan.copy(alpha = 0.18f),
          ) {
            Text(
              text = "Readiness +${plan.recoveryReadinessBoostPct}%",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = TelemetryCyan,
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Zone-2 Treadmill & Active Regeneration",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
          )
          Text(
            text = "Decoupled continuous telemetry flow: ${plan.treadmillMinutes}m guided treadmill incline/speed + ${plan.mobilityAndRecoveryMinutes}m myofascial mobility & CNS breathwork to recover faster for Monday–Saturday.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
          )
        }

        // 3 Telemetry Summary Tiles
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          TelemetryMiniTile(
            label = "Treadmill Peak",
            value = "${plan.peakInclinePercent}% @ ${plan.peakSpeedKmh} km/h",
            color = TelemetryCyan,
            modifier = Modifier.weight(1f),
          )
          TelemetryMiniTile(
            label = "Recovery Split",
            value = "${plan.treadmillMinutes}m Belt / ${plan.mobilityAndRecoveryMinutes}m Mat",
            color = TelemetryLavender,
            modifier = Modifier.weight(1f),
          )
          TelemetryMiniTile(
            label = "Est. Oxidation",
            value = "${plan.estimatedCaloriesBurned} kcal",
            color = TelemetryEmerald,
            modifier = Modifier.weight(1f),
          )
        }

        // Interactive Cardio Duration Slider (15m to 90m) & Cardio Difficulty Slider (Lv.1 to Lv.5)
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = TelemetryTileSurface,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Cardio + Recovery Duration",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
              )
              Text(
                text = "${activeProfile.cardioDurationMin} Minutes",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TelemetryCyan,
              )
            }

            Slider(
              value = activeProfile.cardioDurationMin.toFloat(),
              onValueChange = { raw ->
                val snapped = ((raw / 5f).roundToInt() * 5).coerceIn(15, 90)
                if (snapped != activeProfile.cardioDurationMin) {
                  AthleteProfileStore.updateActiveProfile(context) {
                    it.copy(cardioDurationMin = snapped)
                  }
                }
              },
              valueRange = 15f..90f,
              steps = 14,
              colors = SliderDefaults.colors(
                thumbColor = TelemetryCyan,
                activeTrackColor = TelemetryCyan,
              ),
              modifier = Modifier.testTag("cardio_duration_slider"),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Cardio & Recovery Intensity",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
              )
              Text(
                text = activeProfile.cardioDifficultyTitle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TelemetryEmerald,
                maxLines = 1,
                softWrap = false,
              )
            }

            Slider(
              value = activeProfile.cardioDifficultyLevel.toFloat(),
              onValueChange = { raw ->
                val lvl = raw.roundToInt().coerceIn(1, 5)
                if (lvl != activeProfile.cardioDifficultyLevel) {
                  AthleteProfileStore.updateActiveProfile(context) {
                    it.copy(cardioDifficultyLevel = lvl)
                  }
                }
              },
              valueRange = 1f..5f,
              steps = 3,
              colors = SliderDefaults.colors(
                thumbColor = TelemetryEmerald,
                activeTrackColor = TelemetryEmerald,
              ),
              modifier = Modifier.testTag("cardio_difficulty_slider"),
            )

            Text(
              text = activeProfile.cardioDifficultySubtitle,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.75f),
            )
          }
        }

        Button(
          onClick = onLaunchCardioConsole,
          shape = PillShape,
          colors = ButtonDefaults.buttonColors(
            containerColor = TelemetryEmerald,
            contentColor = Color(0xFF031418),
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("launch_cardio_console_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
            contentDescription = null,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Launch ${activeProfile.cardioDurationMin}-Min Treadmill & Recovery Console",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }

    // 7-Block Continuous Regeneration & Treadmill Timeline Preview
    plan.blocks.forEach { blk ->
      val blkAccent = when (blk.pillar) {
        CardioRecoveryPillar.PILLAR_1_JOINT_PREP -> TelemetryEmerald
        CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO -> TelemetryCyan
        CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY -> TelemetryCoral
        CardioRecoveryPillar.PILLAR_4_CNS_PARASYMPATHETIC -> TelemetryLavender
      }
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = TelemetryCardSurface,
        border = BorderStroke(1.dp, blkAccent.copy(alpha = 0.30f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(blkAccent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = blk.durationMinutesFormatted,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Black,
              color = blkAccent,
              maxLines = 1,
              softWrap = false,
            )
          }

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = blk.pillar.shortTag.uppercase(Locale.US),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = blkAccent,
              )
              Text(
                text = "• ${blk.targetHeartRateZone}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Text(
              text = blk.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = if (blk.isTreadmillBlock) {
                "Treadmill: ${blk.inclinePercent}% Incline @ ${blk.speedKmh} km/h (${blk.paceMinPerKmFormatted}) • ${blk.recoveryTargetMuscles}"
              } else {
                "Recovery Target: ${blk.recoveryTargetMuscles} • ${blk.cadenceOrTempoCue}"
              },
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.78f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }
  }
}
