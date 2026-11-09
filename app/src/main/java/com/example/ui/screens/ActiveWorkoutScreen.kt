package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import kotlinx.coroutines.launch
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.VoiceCoachManager
import com.example.audio.WorkoutCoachNarration
import com.example.data.AthleteProfileStore
import com.example.model.ExerciseProtocol
import com.example.model.ScaledWorkoutTimingPlan
import com.example.model.WorkoutProtocolRepository
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.GoogleYellow40
import com.example.ui.theme.PillShape
import com.example.ui.theme.WorkspaceCardShape
import com.example.ui.util.celebrationHaptic
import com.example.ui.util.confirmHaptic
import com.example.ui.util.tickHaptic
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class WorkoutSessionPhase {
  PREPARING,   // Setup: exercise announcement + 5-4-3-2-1 countdown
  ACTIVE_WORK, // Timed active set: start cue, pacing cues, 5-4-3-2-1 countdown
  REST,        // Rest interval: rest & next exercise announcement, cues, 5-4-3-2-1 countdown
  FINISHED,    // Completed workout (exact 00:00 synchronization)
}

data class PracticalExercisePrescription(
  val practicalReps: Int,
  val secondsPerRep: Float,
  val setupSeconds: Int,
  val difficultyLabel: String,
  val eccentricCue: String,
  val holdCue: String,
  val concentricCue: String,
  val setupDirective: String,
)

object PracticalRepCalculator {
  fun evaluate(
    exercise: ExerciseProtocol,
    setNumber: Int,
    workDurationSeconds: Int,
    totalSecondsRemaining: Int,
    difficultyLevel: Int = 2,
  ): PracticalExercisePrescription {
    val lower = exercise.name.lowercase()

    val (baseSecPerRep, setupSec, diffLabel, eccCue, holdCue, concCue, setupDir) = when {
      lower.contains("treadmill") && lower.contains("sprint") ->
        BiomechanicalSpec(
          secPerRep = 2.0f,
          setupSec = 2,
          difficulty = "HIIT Sprint • Treadmill",
          eccentric = "Long explosive stride",
          hold = "Drive knees high",
          concentric = "Pump arms fast • Max pace",
          setup = "Set treadmill 10-12 km/h • 2% incline",
        )

      lower.contains("treadmill") ->
        BiomechanicalSpec(
          secPerRep = 2.2f,
          setupSec = 2,
          difficulty = "Incline Cardio • Treadmill",
          eccentric = "Heel-to-toe incline step",
          hold = "Posture tall • Do not hold rails",
          concentric = "Drive through glutes & calves",
          setup = "Set treadmill 8-12% incline • Brisk walk",
        )

      lower.contains("farmer") ->
        BiomechanicalSpec(
          secPerRep = 2.0f,
          setupSec = 2,
          difficulty = "Loaded Conditioning",
          eccentric = "Controlled heel-toe step",
          hold = "Shoulders pulled back & down",
          concentric = "Crush grip on dumbbells",
          setup = "Hold dumbbells firmly at sides • Tall spine",
        )

      lower.contains("mountain") || lower.contains("plank") ->
        BiomechanicalSpec(
          secPerRep = 1.6f,
          setupSec = 2,
          difficulty = "High Cadence • Core Shred",
          eccentric = "Extend leg back smoothly",
          hold = "Hips level with shoulders",
          concentric = "Drive knee to chest fast",
          setup = "High plank stance • Hands under shoulders",
        )

      lower.contains("pullover") ->
        BiomechanicalSpec(
          secPerRep = 3.6f,
          setupSec = 3,
          difficulty = "High Stretch • Lats & Serratus",
          eccentric = "Arc dumbbell behind head (2.2s)",
          hold = "Deep lat stretch at bottom",
          concentric = "Pull over chest engaging lats",
          setup = "Upper back on bench • Soft elbow bend",
        )

      lower.contains("deadlift") ->
        BiomechanicalSpec(
          secPerRep = 3.6f,
          setupSec = 3,
          difficulty = "High Difficulty • Posterior",
          eccentric = "Hinge hips back (2s)",
          hold = "Hamstring stretch • Flat back",
          concentric = "Drive hips forward to stand",
          setup = "Dumbbells close to shins • Brace core",
        )

      lower.contains("lunge") ->
        BiomechanicalSpec(
          secPerRep = 3.4f,
          setupSec = 3,
          difficulty = "High Difficulty • Unilateral",
          eccentric = "Step & lower back knee (2s)",
          hold = "90° depth control",
          concentric = "Drive through front heel",
          setup = "Dumbbells at sides • Tall upright torso",
        )

      lower.contains("squat") ->
        BiomechanicalSpec(
          secPerRep = 3.2f,
          setupSec = 3,
          difficulty = "High Difficulty • Compound",
          eccentric = "Sit hips back & down (2s)",
          hold = "Pause at parallel depth",
          concentric = "Drive up through midfoot",
          setup = "Shoulder-width stance • Chest proud",
        )

      lower.contains("incline bench press") || lower.contains("decline bench press") ->
        BiomechanicalSpec(
          secPerRep = 3.1f,
          setupSec = 3,
          difficulty = "Hard • Upper/Lower Press",
          eccentric = "Lower dumbbells slowly (2s)",
          hold = "Deep chest stretch at bottom",
          concentric = "Press up & exhale strong",
          setup = "Retract shoulder blades • Plant feet",
        )

      lower.contains("bench press") || lower.contains("arnold press") || lower.contains("overhead press") ->
        BiomechanicalSpec(
          secPerRep = 3.0f,
          setupSec = 3,
          difficulty = "Hard • Compound Press",
          eccentric = "Control descent (2s)",
          hold = "Keep wrists stacked",
          concentric = "Press smoothly to top",
          setup = "Core braced tight • Elbows at 45°",
        )

      lower.contains("v-up") || lower.contains("leg raise") ->
        BiomechanicalSpec(
          secPerRep = 2.4f,
          setupSec = 2,
          difficulty = "Moderate • Lower Abs",
          eccentric = "Lower heels slowly (1.5s)",
          hold = "Hover above bench/mat",
          concentric = "Lift legs & curl pelvis up",
          setup = "Grip bench edge or mat • Lower back flat",
        )

      lower.contains("fly") ->
        BiomechanicalSpec(
          secPerRep = 3.0f,
          setupSec = 3,
          difficulty = "Moderate • Stretch Isolation",
          eccentric = "Open wide arc slowly (2s)",
          hold = "Feel pectoral stretch",
          concentric = "Bring dumbbells together",
          setup = "Soft 15° bend in elbows • Chest up",
        )

      lower.contains("row") ->
        BiomechanicalSpec(
          secPerRep = 2.8f,
          setupSec = 2,
          difficulty = "Moderate • Back Pull",
          eccentric = "Extend lats smoothly",
          hold = "Squeeze shoulder blades (1s)",
          concentric = "Pull weight to hip crease",
          setup = "Flat lower back • Brace on bench/knee",
        )

      lower.contains("french press") ->
        BiomechanicalSpec(
          secPerRep = 2.7f,
          setupSec = 2,
          difficulty = "Moderate • Triceps Isolation",
          eccentric = "Hinge forearms back (1.5s)",
          hold = "Full tricep stretch",
          concentric = "Extend arms straight up",
          setup = "Keep upper arms locked vertical",
        )

      lower.contains("curl") ->
        BiomechanicalSpec(
          secPerRep = 2.5f,
          setupSec = 2,
          difficulty = "Moderate • Arm Isolation",
          eccentric = "Slow lowering phase (1.5s)",
          hold = "Peak bicep squeeze",
          concentric = "Curl up smoothly",
          setup = "Elbows pinned to ribcage • Zero swing",
        )

      lower.contains("lateral raise") || lower.contains("front raise") || lower.contains("kickback") ->
        BiomechanicalSpec(
          secPerRep = 2.4f,
          setupSec = 2,
          difficulty = "Moderate • Isolation",
          eccentric = "Resist gravity on way down",
          hold = "Brief pause at peak",
          concentric = "Lift to shoulder height",
          setup = "Slight forward lean • Relax traps",
        )

      lower.contains("calf") || lower.contains("shrug") ->
        BiomechanicalSpec(
          secPerRep = 1.9f,
          setupSec = 2,
          difficulty = "Fast Cadence • Short ROM",
          eccentric = "Full stretch at bottom",
          hold = "1s peak hold at top",
          concentric = "Drive up high",
          setup = "Hold dumbbells firmly at sides",
        )

      lower.contains("bicycle") || lower.contains("crunch") ->
        BiomechanicalSpec(
          secPerRep = 1.7f,
          setupSec = 2,
          difficulty = "Fast Cadence • Abs",
          eccentric = "Control back down",
          hold = "Exhale & contract abs",
          concentric = "Curl ribs toward pelvis",
          setup = "Fingertips by ears • Do not pull neck",
        )

      else ->
        BiomechanicalSpec(
          secPerRep = 2.8f,
          setupSec = 2,
          difficulty = "Moderate • Hypertrophy",
          eccentric = "Controlled lowering (2s)",
          hold = "Pause & stabilize",
          concentric = "Drive & exhale (1s)",
          setup = "Brace core • Controlled full range",
        )
    }

    val usableSetWindow = minOf(workDurationSeconds, totalSecondsRemaining.coerceAtLeast(12))
    val fatiguePenaltySec = (setNumber - 1).coerceAtLeast(0) * 0.14f
    val loadPenaltySec = if ((exercise.weightKg ?: 0.0) > 8.0) 0.12f else 0f
    val difficultyRepMultiplier = when (difficultyLevel.coerceIn(1, 5)) {
      1 -> 0.86f
      2 -> 1.00f
      3 -> 1.14f
      4 -> 1.26f
      5 -> 1.38f
      else -> 1.00f
    }
    val effectiveSecondsPerRep = ((baseSecPerRep + fatiguePenaltySec + loadPenaltySec) / difficultyRepMultiplier).coerceAtLeast(1.1f)

    val netRepWindowSeconds = (usableSetWindow - setupSec).coerceAtLeast(8)
    val rawPracticalReps = (netRepWindowSeconds / effectiveSecondsPerRep).roundToInt()
    val maxRepCap = when (difficultyLevel.coerceIn(1, 5)) {
      1 -> 22
      2 -> 30
      3 -> 34
      4 -> 38
      5 -> 42
      else -> 30
    }
    val boundedReps = rawPracticalReps.coerceIn(5, maxRepCap)

    return PracticalExercisePrescription(
      practicalReps = boundedReps,
      secondsPerRep = (effectiveSecondsPerRep * 10f).roundToInt() / 10f,
      setupSeconds = setupSec,
      difficultyLabel = "$diffLabel • Lv.${difficultyLevel.coerceIn(1, 5)}",
      eccentricCue = eccCue,
      holdCue = holdCue,
      concentricCue = concCue,
      setupDirective = setupDir,
    )
  }

  private data class BiomechanicalSpec(
    val secPerRep: Float,
    val setupSec: Int,
    val difficulty: String,
    val eccentric: String,
    val hold: String,
    val concentric: String,
    val setup: String,
  )
}

private data class AccessiblePhaseTheme(
  val ambientTopGlow: Color,
  val screenBackground: Color = Color(0xFF0A0E14),
  val cardSurface: Color = Color(0xFF121822),
  val tileSurface: Color = Color(0xFF1A2230),
  val subtleHairline: Color = Color(0xFF283244),
  val phaseAccent: Color,
  val bannerContainer: Color,
  val bannerContent: Color,
)

@Composable
fun ActiveWorkoutScreen(
  dayNumber: Int = 1,
  onClosePlayer: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val view = LocalView.current
  val haptic = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

  // Keep Screen Always ON while on Active Workout Screen
  DisposableEffect(view, context) {
    val previousKeepScreenOn = view.keepScreenOn
    view.keepScreenOn = true
    val window = (context as? Activity)?.window
    window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    onDispose {
      view.keepScreenOn = previousKeepScreenOn
      window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
  }

  val voiceCoach = remember { VoiceCoachManager(context, languageCode = "en", speechRate = 1.05f) }

  var liveCoachSpeech by remember {
    mutableStateOf("Initializing voice coach...")
  }

  DisposableEffect(Unit) {
    voiceCoach.onCoachSpoke = { speech ->
      liveCoachSpeech = speech
    }
    onDispose {
      voiceCoach.shutdown()
    }
  }

  // User-constrained target session duration (20 / 25 / 30 minutes)
  var constrainedMinutes by remember(activeProfile.sessionDurationMinutes) {
    mutableIntStateOf(activeProfile.sessionDurationMinutes)
  }

  // Scaled Workout Plan (dynamically scales Sets and Reps so 100% of sets fit inside constrainedMinutes)
  val scaledPlan: ScaledWorkoutTimingPlan = remember(
    dayNumber,
    constrainedMinutes,
    activeProfile.dumbbellWeightKg,
    activeProfile.exerciseWeightOverrides,
    activeProfile.workoutDifficultyLevel,
  ) {
    WorkoutProtocolRepository.getScaledWorkoutPlan(
      day = dayNumber,
      targetDurationMinutes = constrainedMinutes,
      customDumbbellWeightKg = activeProfile.dumbbellWeightKg,
      exerciseWeightOverrides = activeProfile.exerciseWeightOverrides,
      difficultyLevel = activeProfile.workoutDifficultyLevel,
    )
  }

  val workoutProtocol = scaledPlan.dayProtocol

  // Global Constrained Clock (in seconds)
  var totalSecondsRemaining by remember(constrainedMinutes) {
    mutableIntStateOf(constrainedMinutes * 60)
  }
  var isPaused by remember { mutableStateOf(false) }
  var autoPilotEnabled by remember { mutableStateOf(true) }
  var isMuted by remember { mutableStateOf(false) }
  var selectedLanguage by remember { mutableStateOf("en") }

  // Phase & Progression State
  var sessionPhase by remember { mutableStateOf(WorkoutSessionPhase.PREPARING) }
  var currentExerciseIndex by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(0) }
  var currentSetIndex by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(1) }
  var completedSetsCount by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(0) }

  // Dynamic per-set phase timings calibrated from remaining time & remaining sets
  var prepDurationSecs by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(scaledPlan.prepSecondsPerSet) }
  var activeSetDurationSecs by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(scaledPlan.workSecondsPerSet) }
  var restDurationSecs by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(scaledPlan.restSecondsPerSet) }
  var phaseSecondsRemaining by remember(dayNumber, constrainedMinutes) { mutableIntStateOf(scaledPlan.prepSecondsPerSet) }

  /**
   * Re-calibrates prep, work, and rest seconds for the upcoming set based on exact
   * `totalSecondsRemaining` and `remainingSetsCount` so the final set finishes at 00:00.
   */
  fun recalibrateTimingsForRemainingSets(setsAlreadyDone: Int) {
    val remainingSets = (scaledPlan.totalScaledSets - setsAlreadyDone).coerceAtLeast(1)
    if (remainingSets == 1) {
      // Final set of the workout: allocate all remaining seconds to Prep + Active Work (0 post-set rest)
      val prep = 6.coerceAtMost((totalSecondsRemaining / 4).coerceAtLeast(3))
      val work = (totalSecondsRemaining - prep).coerceAtLeast(10)
      prepDurationSecs = prep
      activeSetDurationSecs = work
      restDurationSecs = 0
    } else {
      // Distribute remaining seconds across remaining sets (Note: final set has no trailing rest)
      val slotSec = (totalSecondsRemaining / remainingSets).coerceIn(24, 95)
      val prep = if (slotSec >= 65) 7 else 6
      val work = (slotSec * 0.57f).roundToInt().coerceIn(18, 52)
      val rest = (slotSec - prep - work).coerceAtLeast(8)
      prepDurationSecs = prep
      activeSetDurationSecs = work
      restDurationSecs = rest
    }
  }

  val currentExercise: ExerciseProtocol? = workoutProtocol.exercises.getOrNull(currentExerciseIndex)

  // Dynamically computed practical rep prescription based on exercise difficulty, set fatigue & scaled work window
  val currentPrescription: PracticalExercisePrescription? = remember(
    currentExercise,
    currentSetIndex,
    activeSetDurationSecs,
    totalSecondsRemaining / 20,
    activeProfile.workoutDifficultyLevel,
  ) {
    currentExercise?.let { ex ->
      PracticalRepCalculator.evaluate(
        exercise = ex,
        setNumber = currentSetIndex,
        workDurationSeconds = activeSetDurationSecs,
        totalSecondsRemaining = totalSecondsRemaining,
        difficultyLevel = activeProfile.workoutDifficultyLevel,
      )
    }
  }

  // Subsequent exercise info
  val followingExerciseAndSet: Pair<ExerciseProtocol?, Int> = remember(
    currentExerciseIndex,
    currentSetIndex,
    sessionPhase,
    workoutProtocol,
  ) {
    val ex = currentExercise
    if (ex == null) {
      Pair<ExerciseProtocol?, Int>(null, 1)
    } else if (currentSetIndex < ex.sets) {
      val nextSet = if (sessionPhase == WorkoutSessionPhase.ACTIVE_WORK) currentSetIndex + 1 else currentSetIndex
      Pair<ExerciseProtocol?, Int>(ex, nextSet)
    } else {
      Pair<ExerciseProtocol?, Int>(workoutProtocol.exercises.getOrNull(currentExerciseIndex + 1), 1)
    }
  }

  val nextUpDescription: String = remember(followingExerciseAndSet, sessionPhase) {
    val (nextEx, nextSet) = followingExerciseAndSet
    if (nextEx == null) {
      "Final Set • Finishes at 00:00"
    } else {
      "${nextEx.name} • Set $nextSet of ${nextEx.sets}"
    }
  }

  val nextUpMetaBadge: String = remember(followingExerciseAndSet, activeSetDurationSecs, activeProfile.workoutDifficultyLevel) {
    val (nextEx, nextSet) = followingExerciseAndSet
    if (nextEx == null) {
      "Final"
    } else {
      val nextPresc = PracticalRepCalculator.evaluate(
        exercise = nextEx,
        setNumber = nextSet,
        workDurationSeconds = activeSetDurationSecs,
        totalSecondsRemaining = totalSecondsRemaining,
        difficultyLevel = activeProfile.workoutDifficultyLevel,
      )
      val wt = if (nextEx.isBodyweight) "BW" else "${nextEx.weightKg?.toInt() ?: 8}kg"
      "$wt × ${nextPresc.practicalReps}"
    }
  }

  LaunchedEffect(isMuted, selectedLanguage) {
    voiceCoach.isEnabled = !isMuted
    voiceCoach.setLanguage(selectedLanguage)
  }

  fun announcePrepPhase(isFirstSetOfSession: Boolean, exIndex: Int, setIdx: Int) {
    val ex = workoutProtocol.exercises.getOrNull(exIndex) ?: return
    val presc = PracticalRepCalculator.evaluate(
      exercise = ex,
      setNumber = setIdx,
      workDurationSeconds = activeSetDurationSecs,
      totalSecondsRemaining = totalSecondsRemaining,
      difficultyLevel = activeProfile.workoutDifficultyLevel,
    )
    val weightStr = if (ex.isBodyweight) "bodyweight" else "${ex.weightKg?.toInt() ?: 8} kilos"
    val (prepEn, prepMl) = WorkoutCoachNarration.getPrepStartAnnouncement(
      isFirstSetOfSession = isFirstSetOfSession,
      dayNumber = dayNumber,
      exerciseIndex = exIndex + 1,
      totalExercises = workoutProtocol.exercises.size,
      exerciseName = ex.name,
      setNumber = setIdx,
      totalSets = ex.sets,
      reps = presc.practicalReps,
      weightStr = weightStr,
    )
    voiceCoach.speakCoach(prepEn, prepMl, flush = true)
  }

  fun completeWorkoutSession() {
    completedSetsCount = scaledPlan.totalScaledSets
    totalSecondsRemaining = 0
    sessionPhase = WorkoutSessionPhase.FINISHED
    AthleteProfileStore.awardWorkoutXp(xpAmount = 150, incrementWorkout = true, context = context)
    val estVolumeKg = scaledPlan.exercises.sumOf { ex ->
      val wt = (ex.weightKg ?: 0.0)
      wt * ex.sets * ex.reps
    }
    kotlinx.coroutines.MainScope().launch {
      com.example.data.GoogleFormDatabase.getInstance(context).workoutDao().insertWorkoutLog(
        com.example.data.entity.WorkoutLogEntity(
          timestamp = System.currentTimeMillis(),
          dayNumber = dayNumber,
          workoutName = workoutProtocol.name,
          durationSeconds = scaledPlan.targetDurationMinutes * 60,
          totalVolumeKg = estVolumeKg,
          setsCompleted = scaledPlan.totalScaledSets,
          setsDeferred = 0,
          sessionRating = 5,
        )
      )
    }
    haptic.celebrationHaptic(scope)
    voiceCoach.speakCoach(
      "Workout complete right on schedule! All ${scaledPlan.totalScaledSets} scaled sets finished in ${scaledPlan.targetDurationMinutes} minutes! Plus 150 Avatar XP earned!",
      "വ്യായാമം കൃത്യസമയത്ത് പൂർത്തിയായി! എല്ലാ സെറ്റുകളും മികച്ച രീതിയിൽ പൂർത്തിയാക്കി. അഭിനന്ദനങ്ങൾ!",
      flush = true,
    )
  }

  fun advanceToNextPhase() {
    val ex = workoutProtocol.exercises.getOrNull(currentExerciseIndex) ?: return
    val presc = PracticalRepCalculator.evaluate(
      exercise = ex,
      setNumber = currentSetIndex,
      workDurationSeconds = activeSetDurationSecs,
      totalSecondsRemaining = totalSecondsRemaining,
      difficultyLevel = activeProfile.workoutDifficultyLevel,
    )
    when (sessionPhase) {
      WorkoutSessionPhase.PREPARING -> {
        voiceCoach.playCountdownBeep(isFinalGo = true)
        haptic.confirmHaptic()
        sessionPhase = WorkoutSessionPhase.ACTIVE_WORK
        // If this is the very last set of the workout, match active work duration to remaining global clock
        val remainingSets = (scaledPlan.totalScaledSets - completedSetsCount).coerceAtLeast(1)
        if (remainingSets == 1 && totalSecondsRemaining in 8..75) {
          activeSetDurationSecs = totalSecondsRemaining
        }
        phaseSecondsRemaining = activeSetDurationSecs
        val (workEn, workMl) = WorkoutCoachNarration.getWorkStartAnnouncement(
          exerciseName = ex.name,
          setNumber = currentSetIndex,
          reps = presc.practicalReps,
        )
        voiceCoach.speakCoach(workEn, workMl, flush = true)
      }

      WorkoutSessionPhase.ACTIVE_WORK -> {
        voiceCoach.playCountdownBeep(isFinalGo = true)
        haptic.confirmHaptic()
        val newCompletedSets = completedSetsCount + 1
        completedSetsCount = newCompletedSets

        val totalSetsForExercise = ex.sets
        if (currentSetIndex < totalSetsForExercise) {
          val completedSet = currentSetIndex
          val nextSet = currentSetIndex + 1
          currentSetIndex = nextSet
          recalibrateTimingsForRemainingSets(newCompletedSets)
          sessionPhase = WorkoutSessionPhase.REST
          phaseSecondsRemaining = restDurationSecs.coerceAtLeast(8)

          val (restEn, restMl) = WorkoutCoachNarration.getRestStartAnnouncement(
            restSeconds = phaseSecondsRemaining,
            completedExerciseName = ex.name,
            completedSetNumber = completedSet,
            nextExerciseName = ex.name,
            nextSetNumber = nextSet,
          )
          voiceCoach.speakCoach(restEn, restMl, flush = true)
        } else if (currentExerciseIndex < workoutProtocol.exercises.size - 1) {
          val completedSet = currentSetIndex
          val nextExIdx = currentExerciseIndex + 1
          val nextEx = workoutProtocol.exercises[nextExIdx]
          currentExerciseIndex = nextExIdx
          currentSetIndex = 1
          recalibrateTimingsForRemainingSets(newCompletedSets)
          sessionPhase = WorkoutSessionPhase.REST
          phaseSecondsRemaining = restDurationSecs.coerceAtLeast(8)

          val (restEn, restMl) = WorkoutCoachNarration.getRestStartAnnouncement(
            restSeconds = phaseSecondsRemaining,
            completedExerciseName = ex.name,
            completedSetNumber = completedSet,
            nextExerciseName = nextEx.name,
            nextSetNumber = 1,
          )
          voiceCoach.speakCoach(restEn, restMl, flush = true)
        } else {
          completeWorkoutSession()
        }
      }

      WorkoutSessionPhase.REST -> {
        voiceCoach.playCountdownBeep(isFinalGo = true)
        haptic.confirmHaptic()
        recalibrateTimingsForRemainingSets(completedSetsCount)
        sessionPhase = WorkoutSessionPhase.PREPARING
        phaseSecondsRemaining = prepDurationSecs
        announcePrepPhase(
          isFirstSetOfSession = false,
          exIndex = currentExerciseIndex,
          setIdx = currentSetIndex,
        )
      }

      WorkoutSessionPhase.FINISHED -> {}
    }
  }

  LaunchedEffect(dayNumber, constrainedMinutes) {
    recalibrateTimingsForRemainingSets(0)
    phaseSecondsRemaining = prepDurationSecs
    announcePrepPhase(
      isFirstSetOfSession = true,
      exIndex = 0,
      setIdx = 1,
    )
  }

  // UNIFIED SINGLE-LOOP DETERMINISTIC WORKOUT ENGINE
  LaunchedEffect(isPaused, autoPilotEnabled, sessionPhase) {
    if (isPaused || !autoPilotEnabled || sessionPhase == WorkoutSessionPhase.FINISHED) return@LaunchedEffect

    while (isActive && sessionPhase != WorkoutSessionPhase.FINISHED) {
      delay(1000L)

      if (totalSecondsRemaining > 0) {
        totalSecondsRemaining -= 1
        if (totalSecondsRemaining == 0) {
          completeWorkoutSession()
          break
        }
      }

      if (phaseSecondsRemaining > 1) {
        phaseSecondsRemaining -= 1
        val sec = phaseSecondsRemaining
        val ex = workoutProtocol.exercises.getOrNull(currentExerciseIndex)

        if (sec in 1..5) {
          haptic.tickHaptic()
          voiceCoach.playCountdownBeep(isFinalGo = false)
          val (wordEn, wordMl) = WorkoutCoachNarration.getSpokenCountdownWord(sec)
          val phaseActionLabel = when (sessionPhase) {
            WorkoutSessionPhase.PREPARING -> "Starting set in $sec..."
            WorkoutSessionPhase.ACTIVE_WORK -> "Finishing set in $sec..."
            WorkoutSessionPhase.REST -> "Rest ending in $sec..."
            WorkoutSessionPhase.FINISHED -> "$sec"
          }
          voiceCoach.speakCoach(wordEn, wordMl, flush = true, isCountdownTick = true)
          liveCoachSpeech = "$phaseActionLabel ($wordEn!)"
        } else {
          when (sessionPhase) {
            WorkoutSessionPhase.PREPARING -> {}

            WorkoutSessionPhase.ACTIVE_WORK -> {
              if (sec in listOf(25, 15, 10) && ex != null) {
                val (cueEn, cueMl) = WorkoutCoachNarration.getWorkPacingCue(sec, ex.name)
                if (cueEn.isNotBlank()) {
                  voiceCoach.speakCoach(cueEn, cueMl, flush = true)
                }
              }
            }

            WorkoutSessionPhase.REST -> {
              if (sec in listOf(15, 10)) {
                val nextExName = ex?.name ?: "next exercise"
                val (cueEn, cueMl) = WorkoutCoachNarration.getRestMidCue(sec, nextExName, currentSetIndex)
                if (cueEn.isNotBlank()) {
                  voiceCoach.speakCoach(cueEn, cueMl, flush = true)
                }
              }
            }

            WorkoutSessionPhase.FINISHED -> {}
          }
        }
      } else {
        advanceToNextPhase()
      }
    }
  }

  val remMinutes = totalSecondsRemaining / 60
  val remSecs = totalSecondsRemaining % 60
  val globalProgress = (1f - (totalSecondsRemaining.toFloat() / (constrainedMinutes * 60).coerceAtLeast(1))).coerceIn(0f, 1f)
  val setsProgress = (completedSetsCount.toFloat() / scaledPlan.totalScaledSets.coerceAtLeast(1)).coerceIn(0f, 1f)

  val isInCountdownWindow = phaseSecondsRemaining in 1..5

  val targetTheme = remember(sessionPhase, isInCountdownWindow, isPaused) {
    when {
      isPaused -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF2B2110),
        phaseAccent = Color(0xFFFBBF24),
        bannerContainer = Color(0xFFF59E0B),
        bannerContent = Color(0xFF140D02),
      )

      isInCountdownWindow -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF331014),
        phaseAccent = Color(0xFFFF6B6B),
        bannerContainer = Color(0xFFEF4444),
        bannerContent = Color.White,
      )

      sessionPhase == WorkoutSessionPhase.ACTIVE_WORK -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF0C291C),
        phaseAccent = Color(0xFF34D399),
        bannerContainer = Color(0xFF10B981),
        bannerContent = Color(0xFF041E12),
      )

      sessionPhase == WorkoutSessionPhase.REST -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF0E223D),
        phaseAccent = Color(0xFF60A5FA),
        bannerContainer = Color(0xFF2563EB),
        bannerContent = Color.White,
      )

      sessionPhase == WorkoutSessionPhase.PREPARING -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF291F0C),
        phaseAccent = Color(0xFFFBBF24),
        bannerContainer = Color(0xFFF59E0B),
        bannerContent = Color(0xFF140D02),
      )

      else -> AccessiblePhaseTheme(
        ambientTopGlow = Color(0xFF0C291C),
        phaseAccent = Color(0xFF34D399),
        bannerContainer = Color(0xFF10B981),
        bannerContent = Color(0xFF041E12),
      )
    }
  }

  val ambientTopColor by animateColorAsState(targetTheme.ambientTopGlow, tween(450), label = "ambientTop")
  val accentColor by animateColorAsState(targetTheme.phaseAccent, tween(350), label = "accentColor")
  val bannerBgColor by animateColorAsState(targetTheme.bannerContainer, tween(350), label = "bannerBg")
  val bannerTextColor by animateColorAsState(targetTheme.bannerContent, tween(350), label = "bannerText")

  val cardSurface = targetTheme.cardSurface
  val tileSurface = targetTheme.tileSurface
  val subtleHairline = targetTheme.subtleHairline
  val mutedLabelColor = Color(0xFF94A3B8)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colorStops = arrayOf(
            0.0f to ambientTopColor,
            0.42f to targetTheme.screenBackground,
            1.0f to targetTheme.screenBackground,
          ),
        ),
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("active_workout_screen"),
  ) {
    if (sessionPhase == WorkoutSessionPhase.FINISHED || currentExercise == null || currentPrescription == null) {
      val totalVolumeMovedKg = scaledPlan.exercises.sumOf { ex ->
        (ex.weightKg ?: 0.0) * ex.sets * ex.reps
      }.roundToInt()
      WorkoutFinishedCelebration(
        workoutName = workoutProtocol.name,
        totalPrescribedSets = scaledPlan.totalScaledSets,
        completedSets = completedSetsCount,
        durationMinutes = constrainedMinutes,
        totalVolumeKg = totalVolumeMovedKg,
        activeProfile = activeProfile,
        onApplyOverload = {
          AthleteProfileStore.applyProgressiveOverload(deltaKg = 1.0f, context = context)
          haptic.confirmHaptic()
        },
        onClose = onClosePlayer,
      )
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        // ─────────────────────────────────────────────────────────────────────
        // 1. TOP BAR + SYNCHRONIZED TIME-CONSTRAINED SESSION GAUGE
        // ─────────────────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onClosePlayer,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(cardSurface),
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Workout",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
              )
            }

            // Constrained Session Duration Selector (20m / 25m / 30m) - scales Sets & Reps to finish at 00:00
            Surface(
              shape = PillShape,
              color = cardSurface,
              border = BorderStroke(1.dp, subtleHairline),
              modifier = Modifier.clickable {
                val nextDuration = when {
                  constrainedMinutes < 25 -> 30
                  constrainedMinutes < 35 -> 45
                  constrainedMinutes < 50 -> 60
                  constrainedMinutes < 65 -> 75
                  constrainedMinutes < 80 -> 90
                  else -> 20
                }
                constrainedMinutes = nextDuration
                AthleteProfileStore.updateActiveProfile(context) { it.copy(sessionDurationMin = nextDuration) }
                haptic.confirmHaptic()
              },
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor),
                )
                Text(
                  text = "${constrainedMinutes}m Fit • ${scaledPlan.totalScaledSets} Sets (${activeSetDurationSecs}s/set)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  softWrap = false,
                  color = Color.White,
                )
              }
            }

            // Voice Accent & Mute Controls
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Surface(
                shape = PillShape,
                color = cardSurface,
                border = BorderStroke(1.dp, subtleHairline),
                modifier = Modifier.clickable {
                  selectedLanguage = if (selectedLanguage == "en") "ml" else "en"
                },
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp),
                  )
                  Text(
                    text = if (selectedLanguage == "en") "EN-IN" else "മലയാളം",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    color = Color.White,
                  )
                }
              }

              IconButton(
                onClick = { isMuted = !isMuted },
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(cardSurface),
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Default.VoiceOverOff else Icons.Default.RecordVoiceOver,
                  contentDescription = "Toggle Voice Coach",
                  tint = if (isMuted) mutedLabelColor else accentColor,
                  modifier = Modifier.size(20.dp),
                )
              }
            }
          }

          // LARGE TOTAL SESSION GAUGE (Synchronized Sets & Time Remaining)
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, subtleHairline),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                  )
                  Column {
                    Text(
                      text = "TIME-SCALED ${constrainedMinutes}-MIN PROTOCOL",
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      fontWeight = FontWeight.Bold,
                      color = mutedLabelColor,
                    )
                    Text(
                      text = "$completedSetsCount of ${scaledPlan.totalScaledSets} Scaled Sets Done",
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                    )
                  }
                }

                Row(
                  verticalAlignment = Alignment.Bottom,
                  horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text = String.format("%02d:%02d", remMinutes, remSecs),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                  )
                  Text(
                    text = "LEFT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = mutedLabelColor,
                    modifier = Modifier.padding(bottom = 3.dp),
                  )
                }
              }

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(12.dp)
                  .clip(PillShape)
                  .background(tileSurface),
              ) {
                val gaugeFill = maxOf(globalProgress, setsProgress, 0.04f).coerceIn(0f, 1f)
                Box(
                  modifier = Modifier
                    .fillMaxWidth(gaugeFill)
                    .fillMaxHeight()
                    .clip(PillShape)
                    .background(accentColor),
                )
              }
            }
          }
        }

        // ─────────────────────────────────────────────────────────────────────
        // 2. CENTER WORKOUT COCKPIT
        // ─────────────────────────────────────────────────────────────────────
        Column(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Card(
            shape = WorkspaceCardShape,
            colors = CardDefaults.cardColors(
              containerColor = cardSurface,
            ),
            border = BorderStroke(
              width = 1.dp,
              color = accentColor.copy(alpha = 0.35f),
            ),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              val topPhaseBannerText = when {
                isPaused -> "WORKOUT PAUSED"
                isInCountdownWindow && sessionPhase == WorkoutSessionPhase.PREPARING -> "STARTING SET IN $phaseSecondsRemaining..."
                isInCountdownWindow && sessionPhase == WorkoutSessionPhase.ACTIVE_WORK -> "FINAL REPS • $phaseSecondsRemaining..."
                isInCountdownWindow && sessionPhase == WorkoutSessionPhase.REST -> "REST ENDING • GRAB WEIGHTS ($phaseSecondsRemaining)"
                sessionPhase == WorkoutSessionPhase.PREPARING -> "GET READY • EXERCISE ${currentExerciseIndex + 1} OF ${workoutProtocol.exercises.size}"
                sessionPhase == WorkoutSessionPhase.ACTIVE_WORK -> "ACTIVE WORK • SET $currentSetIndex OF ${currentExercise.sets}"
                sessionPhase == WorkoutSessionPhase.REST -> "RECOVERY REST • PREPARE FOR SET $currentSetIndex"
                else -> "SESSION COMPLETE"
              }

              Surface(
                shape = PillShape,
                color = bannerBgColor,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(
                  text = topPhaseBannerText,
                  style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                  fontWeight = FontWeight.Black,
                  letterSpacing = 0.6.sp,
                  textAlign = TextAlign.Center,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = bannerTextColor,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                )
              }

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                if (sessionPhase == WorkoutSessionPhase.REST) {
                  Text(
                    text = "RESTING • UP NEXT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = accentColor,
                  )
                }
                Text(
                  text = currentExercise.name,
                  style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                  ),
                  fontWeight = FontWeight.Black,
                  textAlign = TextAlign.Center,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                  color = Color.White,
                  modifier = Modifier.fillMaxWidth(),
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                AccessibleMetricTile(
                  label = if (sessionPhase == WorkoutSessionPhase.REST) "NEXT SET" else "SCALED SET",
                  primaryValue = "$currentSetIndex of ${currentExercise.sets}",
                  subLabel = currentExercise.targetMuscle,
                  tileSurface = tileSurface,
                  valueColor = Color.White,
                  labelColor = mutedLabelColor,
                  modifier = Modifier.weight(1f),
                )

                AccessibleMetricTile(
                  label = "SCALED REPS",
                  primaryValue = "${currentPrescription.practicalReps} Reps",
                  subLabel = "${currentPrescription.secondsPerRep}s/rep • ${activeSetDurationSecs}s",
                  tileSurface = tileSurface,
                  valueColor = accentColor,
                  labelColor = mutedLabelColor,
                  modifier = Modifier.weight(1.15f),
                )

                AccessibleMetricTile(
                  label = "LOAD",
                  primaryValue = currentExercise.displayWeight,
                  subLabel = if (currentExercise.isBodyweight) "Treadmill/BW" else "Tap ±0.5kg below",
                  tileSurface = tileSurface,
                  valueColor = Color.White,
                  labelColor = mutedLabelColor,
                  modifier = Modifier.weight(1f),
                )
              }

              // Per-Exercise Quick Working Dumbbell Weight Memory Controls (-0.5 kg / +0.5 kg)
              if (!currentExercise.isBodyweight) {
                val exWeightFloat = (currentExercise.weightKg ?: activeProfile.dumbbellWeightKg.toDouble()).toFloat()
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    shape = PillShape,
                    color = tileSurface,
                    border = BorderStroke(1.dp, subtleHairline),
                    modifier = Modifier.clickable {
                      val updated = (exWeightFloat - 0.5f).coerceAtLeast(1.0f)
                      AthleteProfileStore.setExerciseWeightOverride(currentExercise.name, updated, context)
                      haptic.confirmHaptic()
                    },
                  ) {
                    Text(
                      text = "−0.5 kg",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                  }

                  Text(
                    text = "  Working Load: ${currentExercise.displayWeight}  ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                  )

                  Surface(
                    shape = PillShape,
                    color = tileSurface,
                    border = BorderStroke(1.dp, subtleHairline),
                    modifier = Modifier.clickable {
                      val updated = (exWeightFloat + 0.5f).coerceAtMost(60.0f)
                      AthleteProfileStore.setExerciseWeightOverride(currentExercise.name, updated, context)
                      haptic.confirmHaptic()
                    },
                  ) {
                    Text(
                      text = "+0.5 kg",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    )
                  }
                }
              }

              val phaseMaxDuration = when (sessionPhase) {
                WorkoutSessionPhase.PREPARING -> prepDurationSecs
                WorkoutSessionPhase.ACTIVE_WORK -> activeSetDurationSecs
                WorkoutSessionPhase.REST -> restDurationSecs
                WorkoutSessionPhase.FINISHED -> 1
              }

              val phaseProgress = (phaseSecondsRemaining.toFloat() / phaseMaxDuration.coerceAtLeast(1)).coerceIn(0f, 1f)
              val animatedPhaseProgress by animateFloatAsState(
                targetValue = phaseProgress,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "phaseProgress",
              )

              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .size(162.dp)
                  .padding(vertical = 2.dp),
              ) {
                CircularProgressIndicator(
                  progress = { animatedPhaseProgress },
                  modifier = Modifier.fillMaxSize(),
                  strokeWidth = 12.dp,
                  color = accentColor,
                  trackColor = tileSurface,
                  strokeCap = StrokeCap.Round,
                )

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center,
                ) {
                  Text(
                    text = "$phaseSecondsRemaining",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                  )
                  Text(
                    text = when {
                      isInCountdownWindow -> "COUNTDOWN"
                      sessionPhase == WorkoutSessionPhase.ACTIVE_WORK -> "WORK SECS"
                      sessionPhase == WorkoutSessionPhase.REST -> "REST SECS"
                      else -> "PREP SECS"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = mutedLabelColor,
                  )
                }
              }

              LiveBiomechanicsPacerHud(
                sessionPhase = sessionPhase,
                prescription = currentPrescription,
                activeSetDurationSecs = activeSetDurationSecs,
                phaseSecondsRemaining = phaseSecondsRemaining,
                currentSetIndex = currentSetIndex,
                totalSetsForExercise = currentExercise.sets,
                tileSurface = tileSurface,
                accentColor = accentColor,
                mutedLabelColor = mutedLabelColor,
                nextUpDescription = nextUpDescription,
                nextUpMetaBadge = nextUpMetaBadge,
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardSurface,
            border = BorderStroke(1.dp, subtleHairline),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                val weightStr = if (currentExercise.isBodyweight) "bodyweight" else "${currentExercise.weightKg?.toInt() ?: 8} kilos"
                voiceCoach.speakCoach(
                  "${currentExercise.name}, Set $currentSetIndex of ${currentExercise.sets}. Target: ${currentPrescription.practicalReps} reps at $weightStr. $phaseSecondsRemaining seconds remaining.",
                  "${currentExercise.name}, സെറ്റ് $currentSetIndex, ${currentPrescription.practicalReps} റെപ്സ്. $phaseSecondsRemaining സെക്കൻഡ് ബാക്കി.",
                  flush = true,
                )
              },
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(tileSurface),
                contentAlignment = Alignment.Center,
              ) {
                if (isInCountdownWindow) {
                  Text(
                    text = "$phaseSecondsRemaining",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                  )
                } else {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Coach Speaking",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isInCountdownWindow) "LIVE AUDIO COUNTDOWN" else "VOICE COACH • TAP TO REPEAT",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  color = mutedLabelColor,
                )
                Text(
                  text = "\"$liveCoachSpeech\"",
                  style = MaterialTheme.typography.bodySmall,
                  fontStyle = FontStyle.Italic,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = Color.White,
                )
              }
            }
          }
        }

        // ─────────────────────────────────────────────────────────────────────
        // 3. PINNED BOTTOM CONTROL DOCK
        // ─────────────────────────────────────────────────────────────────────
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Button(
            onClick = {
              isPaused = !isPaused
              if (isPaused) {
                voiceCoach.speakCoach("Workout paused.", "പരിശീലനം നിർത്തിവെച്ചു.", flush = true)
              } else {
                voiceCoach.speakCoach("Resuming workout! Let's go!", "പരിശീലനം തുടരുന്നു!", flush = true)
              }
            },
            shape = PillShape,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isPaused) GoogleYellow40 else tileSurface,
              contentColor = if (isPaused) Color.Black else Color.White,
            ),
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
          ) {
            Icon(
              imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
              contentDescription = if (isPaused) "Resume" else "Pause",
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isPaused) "Resume" else "Pause",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false,
            )
          }

          OutlinedButton(
            onClick = {
              phaseSecondsRemaining += 15
              if (sessionPhase == WorkoutSessionPhase.ACTIVE_WORK) {
                activeSetDurationSecs += 10
              }
              haptic.confirmHaptic()
              voiceCoach.speakCoach(
                "Added 15 seconds. Remaining sets automatically scaled.",
                "15 സെക്കൻഡ് കൂടി ചേർത്തു.",
                flush = true,
              )
            },
            shape = PillShape,
            border = BorderStroke(1.dp, subtleHairline),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = cardSurface,
              contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
            modifier = Modifier
              .weight(0.85f)
              .height(50.dp),
          ) {
            Text(
              text = "+15s Time",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false,
            )
          }

          Button(
            onClick = {
              advanceToNextPhase()
            },
            shape = PillShape,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = bannerBgColor,
              contentColor = bannerTextColor,
            ),
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
          ) {
            Icon(
              imageVector = Icons.Default.FastForward,
              contentDescription = "Next Phase",
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = when (sessionPhase) {
                WorkoutSessionPhase.PREPARING -> "Start Set"
                WorkoutSessionPhase.ACTIVE_WORK -> "Finish Set"
                WorkoutSessionPhase.REST -> "Skip Rest"
                WorkoutSessionPhase.FINISHED -> "Close"
              },
              style = MaterialTheme.typography.labelLarge,
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
private fun LiveBiomechanicsPacerHud(
  sessionPhase: WorkoutSessionPhase,
  prescription: PracticalExercisePrescription,
  activeSetDurationSecs: Int,
  phaseSecondsRemaining: Int,
  currentSetIndex: Int,
  totalSetsForExercise: Int,
  tileSurface: Color,
  accentColor: Color,
  mutedLabelColor: Color,
  nextUpDescription: String,
  nextUpMetaBadge: String,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = tileSurface,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      when (sessionPhase) {
        WorkoutSessionPhase.ACTIVE_WORK -> {
          val elapsedSecs = (activeSetDurationSecs - phaseSecondsRemaining).coerceAtLeast(0)
          val netElapsed = (elapsedSecs - prescription.setupSeconds).coerceAtLeast(0)
          val currentRepEstimate = if (elapsedSecs < prescription.setupSeconds) {
            1
          } else {
            ((netElapsed / prescription.secondsPerRep).toInt() + 1).coerceIn(1, prescription.practicalReps)
          }

          val repCyclePos = if (elapsedSecs < prescription.setupSeconds) {
            -1f
          } else {
            (netElapsed % prescription.secondsPerRep) / prescription.secondsPerRep
          }

          val liveMovementCue = when {
            repCyclePos < 0f -> "Get into position & brace core"
            repCyclePos < 0.50f -> "⬇ ${prescription.eccentricCue}"
            repCyclePos < 0.68f -> "⏸ ${prescription.holdCue}"
            else -> "⬆ ${prescription.concentricCue}"
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "LIVE CADENCE • ${prescription.difficultyLabel.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = mutedLabelColor,
            )

            Surface(
              shape = PillShape,
              color = accentColor.copy(alpha = 0.18f),
            ) {
              Text(
                text = "PACED REP $currentRepEstimate OF ${prescription.practicalReps}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
              )
            }
          }

          Text(
            text = liveMovementCue,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        WorkoutSessionPhase.REST -> {
          val inhalePhase = (phaseSecondsRemaining % 6) >= 3
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "UP NEXT • ${nextUpDescription.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              color = mutedLabelColor,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = nextUpMetaBadge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = accentColor,
            )
          }

          Text(
            text = if (inhalePhase) "Deep nasal inhale (3s) • Lower heart rate" else "Slow mouth exhale (3s) • Shake out tension",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        else -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "FORM & SETUP • ${prescription.difficultyLabel.uppercase()}",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
              fontWeight = FontWeight.Bold,
              color = mutedLabelColor,
            )
            Text(
              text = nextUpMetaBadge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = accentColor,
            )
          }

          Text(
            text = prescription.setupDirective,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        for (setNum in 1..totalSetsForExercise) {
          val isCompleted = setNum < currentSetIndex
          val isCurrent = setNum == currentSetIndex
          val blockBg = when {
            isCompleted -> accentColor.copy(alpha = 0.22f)
            isCurrent -> accentColor
            else -> Color(0xFF10151E)
          }
          val blockTextColor = when {
            isCompleted -> accentColor
            isCurrent -> Color(0xFF06131E)
            else -> mutedLabelColor
          }
          val blockText = when {
            isCompleted -> "✓ Set $setNum"
            else -> "Set $setNum"
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = blockBg,
            modifier = Modifier.weight(1f),
          ) {
            Text(
              text = blockText,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center,
              maxLines = 1,
              softWrap = false,
              color = blockTextColor,
              modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AccessibleMetricTile(
  label: String,
  primaryValue: String,
  subLabel: String,
  tileSurface: Color,
  valueColor: Color,
  labelColor: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = tileSurface,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        color = labelColor,
      )
      Text(
        text = primaryValue,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
        fontWeight = FontWeight.Black,
        maxLines = 1,
        softWrap = false,
        color = valueColor,
      )
      Text(
        text = subLabel,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = labelColor,
      )
    }
  }
}

@Composable
private fun WorkoutFinishedCelebration(
  workoutName: String,
  totalPrescribedSets: Int,
  completedSets: Int,
  durationMinutes: Int,
  totalVolumeKg: Int,
  activeProfile: com.example.data.AthleteProfile,
  onApplyOverload: () -> Unit,
  onClose: () -> Unit,
) {
  var overloadApplied by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp)
      .verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Spacer(modifier = Modifier.height(10.dp))

    Image(
      painter = painterResource(id = R.drawable.ic_session_celebration),
      contentDescription = "Celebration",
      modifier = Modifier.size(100.dp),
    )

    Text(
      text = "$durationMinutes-Minute Scaled Session Complete!",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = Color.White,
      textAlign = TextAlign.Center,
    )

    Text(
      text = "100% of scaled sets & exercises completed right on the $durationMinutes:00 time constraint. +150 Avatar XP awarded!",
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF94A3B8),
      textAlign = TextAlign.Center,
    )

    // v2 Post-Workout Celebration Receipt Card
    Card(
      shape = WorkspaceCardShape,
      colors = CardDefaults.cardColors(containerColor = Color(0xFF121822)),
      border = BorderStroke(1.dp, Color(0xFF283244)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("workout_celebration_receipt_card"),
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp),
      ) {
        Text(
          text = "SESSION RECEIPT • $workoutName",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Constrained Time Budget:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
          Text(text = "$durationMinutes:00 Exact Fit (100%)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Scaled Sets Completed:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
          Text(text = "$completedSets / $totalPrescribedSets sets", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoogleGreen40)
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Total Volume Lifted:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
          Text(text = "$totalVolumeKg kg Moved", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "2D Warrior Rooster Growth:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
          Text(
            text = "${activeProfile.evolutionStage.badge} • Lv.${activeProfile.growthMicroLevel}/100",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = GoogleYellow40,
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(text = "Active Streak & Shields:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF94A3B8))
          Text(
            text = "🔥 ${activeProfile.streakDays}d Streak • 🛡️ ${activeProfile.streakFreezeShields} Shield${if (activeProfile.streakFreezeShields == 1) "" else "s"}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        }
      }
    }

    // v2 Smart Progressive Overload Prompt Card
    Card(
      shape = WorkspaceCardShape,
      colors = CardDefaults.cardColors(containerColor = Color(0xFF172334)),
      border = BorderStroke(1.dp, GoogleYellow40.copy(alpha = 0.45f)),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("progressive_overload_prompt_card"),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "⚡ SMART PROGRESSIVE OVERLOAD",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Black,
          color = GoogleYellow40,
        )
        Text(
          text = if (overloadApplied) {
            "Progressive overload applied! Base working dumbbell load increased to ${String.format(java.util.Locale.US, "%.1f", activeProfile.dumbbellWeightKg)} kg (+40 Bonus XP earned)."
          } else {
            "You completed 100% of prescribed sets on schedule. Ready to increase your working dumbbell load by +1.0 kg for your next session?"
          },
          style = MaterialTheme.typography.bodySmall,
          color = Color(0xFFCBD5E1),
        )
        if (!overloadApplied) {
          OutlinedButton(
            onClick = {
              onApplyOverload()
              overloadApplied = true
            },
            shape = PillShape,
            border = BorderStroke(1.dp, GoogleYellow40),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoogleYellow40),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("apply_progressive_overload_button"),
          ) {
            Text(
              text = "Apply +1.0 kg Overload for Next Session (+40 XP)",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.weight(1f))

    Button(
      onClick = onClose,
      shape = PillShape,
      colors = ButtonDefaults.buttonColors(
        containerColor = GoogleGreen40,
        contentColor = Color.White,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(54.dp),
    ) {
      Text(text = "Return to Dashboard", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
