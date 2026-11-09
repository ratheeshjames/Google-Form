package com.example.model

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Decoupled Sunday Cardio, Treadmill & Active Recovery Methodology Engine.
 *
 * Unlike Days 1–6 (which use discrete Dumbbell Sets × Reps + Rest Intervals),
 * Sunday is engineered as a **Continuous Multi-Pillar Cardiovascular & Active Regeneration Session**
 * designed to:
 * 1. Oxidize visceral fat via Zone 2 Incline Treadmill work & calibrated VO2 intervals,
 * 2. Flush metabolic waste & DOMS (delayed-onset muscle soreness) from the week's 6 dumbbell days,
 * 3. Decompress the spine, hips, shoulders, and ankles via guided myofascial & mobility blocks,
 * 4. Reset the Central Nervous System (CNS) via parasympathetic breathwork so the athlete is 100%
 *    recovered and primed for Monday Day 1.
 */
enum class CardioRecoveryPillar(
  val pillarNumber: Int,
  val title: String,
  val shortTag: String,
  val subtitle: String,
) {
  PILLAR_1_JOINT_PREP(
    pillarNumber = 1,
    title = "Pillar I • Joint Decompression & Warm-Up",
    shortTag = "Joint Prep",
    subtitle = "Lubricates ankles, knees, hips & thoracic spine before stepping onto the belt",
  ),
  PILLAR_2_TREADMILL_AERO(
    pillarNumber = 2,
    title = "Pillar II • Treadmill Incline & Zone-2/VO2 Wave",
    shortTag = "Treadmill",
    subtitle = "Guided incline & speed telemetry for fat oxidation and capillary blood-flow flush",
  ),
  PILLAR_3_MYOFASCIAL_MOBILITY(
    pillarNumber = 3,
    title = "Pillar III • Myofascial Unwind & Muscle Recovery",
    shortTag = "Mobility",
    subtitle = "Lengthens tight pecs, lats, hamstrings & hip flexors from Days 1–6 lifting",
  ),
  PILLAR_4_CNS_PARASYMPATHETIC(
    pillarNumber = 4,
    title = "Pillar IV • Parasympathetic CNS & Vagus Reset",
    shortTag = "CNS Reset",
    subtitle = "Downregulates cortisol and lowers heart rate for peak Monday strength readiness",
  ),
}

data class CardioRecoveryBlock(
  val id: String,
  val blockNumber: Int,
  val pillar: CardioRecoveryPillar,
  val title: String,
  val subtitle: String,
  val durationSeconds: Int,
  val isTreadmillBlock: Boolean,
  val inclinePercent: Float,
  val speedKmh: Float,
  val targetHeartRateZone: String,
  val targetBpmRange: String,
  val cadenceOrTempoCue: String,
  val recoveryTargetMuscles: String,
  val primaryCoachDirective: String,
  val secondaryBreathingCue: String,
  val voiceStartAnnouncementEn: String,
  val voiceStartAnnouncementMl: String,
) {
  val durationMinutesFormatted: String
    get() {
      val m = durationSeconds / 60
      val s = durationSeconds % 60
      return if (s == 0) "${m}m" else String.format(Locale.US, "%d:%02d", m, s)
    }

  val paceMinPerKmFormatted: String
    get() {
      if (!isTreadmillBlock || speedKmh <= 0.5f) return "Floor / Mat"
      val totalMinPerKm = 60f / speedKmh
      val wholeMin = totalMinPerKm.toInt()
      val remSec = ((totalMinPerKm - wholeMin) * 60f).roundToInt().coerceIn(0, 59)
      return String.format(Locale.US, "%d'%02d\"/km", wholeMin, remSec)
    }
}

data class CalibratedCardioRecoveryPlan(
  val totalDurationMinutes: Int,
  val totalDurationSeconds: Int,
  val difficultyLevel: Int,
  val difficultyTitle: String,
  val blocks: List<CardioRecoveryBlock>,
  val treadmillMinutes: Int,
  val mobilityAndRecoveryMinutes: Int,
  val estimatedCaloriesBurned: Int,
  val recoveryReadinessBoostPct: Int,
  val peakInclinePercent: Float,
  val peakSpeedKmh: Float,
)

object CardioRecoveryProtocolRepository {

  /**
   * Builds a 100% time-accurate, continuous 7-block Cardio, Treadmill & Active Recovery plan
   * constrained to exact `durationMinutes * 60` seconds and dynamically calibrated by `difficultyLevel` (1..5).
   *
   * Notice how Difficulty shifts the balance between intense treadmill conditioning and restorative recovery:
   * - Level 1 (Restorative Flush): 40% gentle treadmill walk, 60% joint prep + mobility + CNS breathwork
   * - Level 2 (Zone 2 & Recovery Base): 50% incline treadmill cruise, 50% active recovery & mobility
   * - Level 3 (Aerobic Incline Cruise): 56% incline + brisk tempo, 44% mobility & decompression
   * - Level 4 (VO2 Surge + Mobility): 62% steep power hike + speed surges, 38% lactic flush & stretch
   * - Level 5 (Peak Summit & CNS Reset): 68% peak incline & HIIT intervals, 32% concentrated recovery
   */
  fun buildCalibratedPlan(
    durationMinutes: Int = 35,
    difficultyLevel: Int = 2,
    athleteWeightKg: Float = 75f,
  ): CalibratedCardioRecoveryPlan {
    val clampedMins = durationMinutes.coerceIn(15, 90)
    val clampedDiff = difficultyLevel.coerceIn(1, 5)
    val totalSecs = clampedMins * 60

    // Share of session allocated to each of the 7 continuous phases based on difficulty level
    val shares = when (clampedDiff) {
      1 -> floatArrayOf(0.16f, 0.20f, 0.12f, 0.08f, 0.16f, 0.14f) // Remaining ~14% -> Block 7 CNS Reset
      2 -> floatArrayOf(0.14f, 0.22f, 0.16f, 0.10f, 0.14f, 0.12f) // Remaining ~12% -> Block 7 CNS Reset
      3 -> floatArrayOf(0.12f, 0.24f, 0.20f, 0.10f, 0.12f, 0.11f) // Remaining ~11% -> Block 7 CNS Reset
      4 -> floatArrayOf(0.11f, 0.24f, 0.26f, 0.10f, 0.10f, 0.10f) // Remaining ~9%  -> Block 7 CNS Reset
      else -> floatArrayOf(0.10f, 0.25f, 0.30f, 0.10f, 0.09f, 0.08f) // Remaining ~8% -> Block 7 CNS Reset
    }

    val b1Secs = ((totalSecs * shares[0] / 10f).roundToInt() * 10).coerceAtLeast(60)
    val b2Secs = ((totalSecs * shares[1] / 10f).roundToInt() * 10).coerceAtLeast(120)
    val b3Secs = ((totalSecs * shares[2] / 10f).roundToInt() * 10).coerceAtLeast(120)
    val b4Secs = ((totalSecs * shares[3] / 10f).roundToInt() * 10).coerceAtLeast(60)
    val b5Secs = ((totalSecs * shares[4] / 10f).roundToInt() * 10).coerceAtLeast(60)
    val b6Secs = ((totalSecs * shares[5] / 10f).roundToInt() * 10).coerceAtLeast(60)
    val b7Secs = (totalSecs - (b1Secs + b2Secs + b3Secs + b4Secs + b5Secs + b6Secs)).coerceAtLeast(60)

    // Calibrate Treadmill Incline (%) and Speed (km/h) per Difficulty Level (Lv.1..Lv.5)
    val b1Incline = 1.5f
    val b1Speed = when (clampedDiff) {
      1 -> 3.4f
      2 -> 3.8f
      3 -> 4.2f
      4 -> 4.5f
      else -> 4.8f
    }

    val b2Incline = when (clampedDiff) {
      1 -> 4.0f
      2 -> 6.5f
      3 -> 8.0f
      4 -> 9.5f
      else -> 11.0f
    }
    val b2Speed = when (clampedDiff) {
      1 -> 4.6f
      2 -> 5.4f
      3 -> 5.8f
      4 -> 6.2f
      else -> 6.6f
    }

    val b3Incline = when (clampedDiff) {
      1 -> 6.0f
      2 -> 9.5f
      3 -> 10.5f
      4 -> 8.0f
      else -> 9.0f
    }
    val b3Speed = when (clampedDiff) {
      1 -> 5.0f
      2 -> 5.6f
      3 -> 7.2f
      4 -> 9.6f
      else -> 11.5f
    }
    val b3Title = when (clampedDiff) {
      1 -> "Gentle Incline Glute & Calf Walk"
      2 -> "Steep Incline Power Hike (Zone 2/3)"
      3 -> "Aerobic Tempo Incline Surge"
      4 -> "VO2 Max Interval Surges"
      else -> "Apex Summit HIIT & Hill Sprints"
    }

    val b4Incline = 2.0f
    val b4Speed = when (clampedDiff) {
      1 -> 3.8f
      2 -> 4.3f
      3 -> 4.6f
      4 -> 4.8f
      else -> 5.0f
    }

    val blocks = listOf(
      CardioRecoveryBlock(
        id = "cr_block_1",
        blockNumber = 1,
        pillar = CardioRecoveryPillar.PILLAR_1_JOINT_PREP,
        title = "Dynamic Joint Prep & Easy Belt Walk",
        subtitle = "Ankle circles, hip airplanes, thoracic openers & easy treadmill warm-up stride",
        durationSeconds = b1Secs,
        isTreadmillBlock = true,
        inclinePercent = b1Incline,
        speedKmh = b1Speed,
        targetHeartRateZone = "Zone 1 • Warm-Up",
        targetBpmRange = "95–112 BPM",
        cadenceOrTempoCue = "100 SPM • Tall relaxed posture",
        recoveryTargetMuscles = "Ankles, Knees, Hip Capsule & Thoracic Spine",
        primaryCoachDirective = "Set incline to ${b1Incline}% and speed to ${b1Speed} km/h. Roll shoulders back and take long, fluid heel-to-toe steps.",
        secondaryBreathingCue = "Nasal inhale 3 steps • Smooth exhale 3 steps",
        voiceStartAnnouncementEn = "Starting Sunday Cardio and Recovery! Block 1: Dynamic joint decompression and warm-up walk at ${b1Incline} percent incline and ${b1Speed} kilometers per hour.",
        voiceStartAnnouncementMl = "ഞായറാഴ്ചത്തെ കാർഡിയോ ആൻഡ് റിക്കവറി സെഷൻ ആരംഭിക്കുന്നു! സന്ധികൾക്ക് അയവു വരുത്താൻ കുറഞ്ഞ വേഗതയിൽ നടന്നു തുടങ്ങാം.",
      ),
      CardioRecoveryBlock(
        id = "cr_block_2",
        blockNumber = 2,
        pillar = CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO,
        title = "Zone 2 Capillary Flush Incline Cruise",
        subtitle = "Steady incline walk that oxidizes visceral fat and flushes soreness from Days 1–6",
        durationSeconds = b2Secs,
        isTreadmillBlock = true,
        inclinePercent = b2Incline,
        speedKmh = b2Speed,
        targetHeartRateZone = "Zone 2 • Fat Oxidation & Flush",
        targetBpmRange = "118–136 BPM",
        cadenceOrTempoCue = "116 SPM • Do not hold handrails",
        recoveryTargetMuscles = "Capillary Flush for Quads, Glutes, Back & Core",
        primaryCoachDirective = "Raise treadmill incline to ${b2Incline}% and speed to ${b2Speed} km/h. Swing arms naturally without gripping the rails.",
        secondaryBreathingCue = "Rhythmic nasal breathing • Conversational Zone 2 effort",
        voiceStartAnnouncementEn = "Block 2: Zone 2 Capillary Flush. Increase treadmill incline to ${b2Incline} percent and speed to ${b2Speed} kilometers per hour. Keep hands off the rails.",
        voiceStartAnnouncementMl = "രണ്ടാം ഘട്ടം: സോൺ ടു ഇൻക്ലൈൻ വാക്കിംഗ്. ട്രെഡ്മിൽ ഇൻക്ലൈൻ ${b2Incline} ശതമാനവും വേഗത ${b2Speed} കിലോമീറ്ററുമായി ക്രമീകരിക്കുക.",
      ),
      CardioRecoveryBlock(
        id = "cr_block_3",
        blockNumber = 3,
        pillar = CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO,
        title = b3Title,
        subtitle = "Peak cardiovascular output calibrated to Difficulty Level $clampedDiff",
        durationSeconds = b3Secs,
        isTreadmillBlock = true,
        inclinePercent = b3Incline,
        speedKmh = b3Speed,
        targetHeartRateZone = if (clampedDiff <= 2) "Zone 2/3 • Aerobic Power" else "Zone 3/4 • VO2 Conditioning",
        targetBpmRange = if (clampedDiff <= 2) "128–146 BPM" else "145–168 BPM",
        cadenceOrTempoCue = if (clampedDiff <= 2) "122 SPM • Drive through heels" else "148+ SPM • Powerful arm drive",
        recoveryTargetMuscles = "Posterior Chain, Calves, Glutes & Cardiorespiratory Capacity",
        primaryCoachDirective = "Set treadmill to ${b3Incline}% incline at ${b3Speed} km/h. Drive through your mid-foot and glutes with proud chest posture.",
        secondaryBreathingCue = "Deep belly inhale • Forceful mouth exhale on effort",
        voiceStartAnnouncementEn = "Block 3: $b3Title. Adjust treadmill incline to ${b3Incline} percent and speed to ${b3Speed} kilometers per hour. Drive strong through your glutes and calves!",
        voiceStartAnnouncementMl = "മൂന്നാം ഘട്ടം: ഉയർന്ന ഇൻക്ലൈൻ പവർ ഹൈക്ക്. ശരീര നില തെറ്റാതെ ശക്തമായി മുന്നോട്ട് നീങ്ങുക!",
      ),
      CardioRecoveryBlock(
        id = "cr_block_4",
        blockNumber = 4,
        pillar = CardioRecoveryPillar.PILLAR_2_TREADMILL_AERO,
        title = "Heart-Rate Taper & Lactic Flush Walk",
        subtitle = "Gradual belt step-down to clear lactate and prevent lower-extremity pooling",
        durationSeconds = b4Secs,
        isTreadmillBlock = true,
        inclinePercent = b4Incline,
        speedKmh = b4Speed,
        targetHeartRateZone = "Zone 1/2 • Active Flush",
        targetBpmRange = "105–120 BPM",
        cadenceOrTempoCue = "104 SPM • Shake out arms & shoulders",
        recoveryTargetMuscles = "Calves, Tibialis Anterior & Venous Return",
        primaryCoachDirective = "Lower incline to ${b4Incline}% and speed to ${b4Speed} km/h. Let your heart rate step down smoothly while shaking tension out of your arms.",
        secondaryBreathingCue = "Slow 4-second inhale • Extended 6-second exhale",
        voiceStartAnnouncementEn = "Block 4: Treadmill Heart Rate Taper. Lower incline to ${b4Incline} percent and speed to ${b4Speed} kilometers per hour to flush lactic acid.",
        voiceStartAnnouncementMl = "നാലാം ഘട്ടം: വേഗതയും ഇൻക്ലൈനും കുറച്ച് ഹൃദയമിടിപ്പ് സാധാരണ നിലയിലേക്ക് കൊണ്ടുവരാം.",
      ),
      CardioRecoveryBlock(
        id = "cr_block_5",
        blockNumber = 5,
        pillar = CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY,
        title = "Posterior Chain, Hip Flexor & Calf Decompression",
        subtitle = "Off-treadmill couch stretch, deep squat hold & downward-dog calf pedaling",
        durationSeconds = b5Secs,
        isTreadmillBlock = false,
        inclinePercent = 0f,
        speedKmh = 0f,
        targetHeartRateZone = "Recovery • Mobility Flow",
        targetBpmRange = "90–105 BPM",
        cadenceOrTempoCue = "30s hold per side • Slow sink on exhale",
        recoveryTargetMuscles = "Hip Flexors, Hamstrings, Glutes & Gastrocnemius (Days 3 & 6 Recovery)",
        primaryCoachDirective = "Step off the treadmill onto your mat. Alternate 30-second kneeling hip-flexor lunges, hamstring hinges, and deep calf stretches.",
        secondaryBreathingCue = "Sink 1 cm deeper into each stretch on every exhale",
        voiceStartAnnouncementEn = "Block 5: Step off the treadmill for Lower Body Decompression. Perform kneeling hip flexor lunges and deep calf stretches to recover from leg day.",
        voiceStartAnnouncementMl = "അഞ്ചാം ഘട്ടം: ട്രെഡ്മില്ലിൽ നിന്നിറങ്ങി കാലുകൾക്കും അരക്കെട്ടിനും അയവു വരുത്തുന്ന സ്ട്രെച്ചിംഗ് ചെയ്യാം.",
      ),
      CardioRecoveryBlock(
        id = "cr_block_6",
        blockNumber = 6,
        pillar = CardioRecoveryPillar.PILLAR_3_MYOFASCIAL_MOBILITY,
        title = "Pectoral, Lat & Thoracic Spine Unwind",
        subtitle = "Doorway/floor pec opener, child's pose lat reach & open-book thoracic rotations",
        durationSeconds = b6Secs,
        isTreadmillBlock = false,
        inclinePercent = 0f,
        speedKmh = 0f,
        targetHeartRateZone = "Recovery • Upper Body Unwind",
        targetBpmRange = "82–96 BPM",
        cadenceOrTempoCue = "Slow 5-second open-book thoracic sweeps",
        recoveryTargetMuscles = "Pectorals, Lats, Rotator Cuffs & Thoracic Spine (Days 1, 2, 4, 5 Recovery)",
        primaryCoachDirective = "Open tight chest and lat fibers from the week's dumbbell presses and rows. Perform thread-the-needle thoracic rotations and child's pose lat reaches.",
        secondaryBreathingCue = "Expand ribcage 360° on inhale • Soften shoulders on exhale",
        voiceStartAnnouncementEn = "Block 6: Upper Body and Thoracic Unwind. Stretch your chest, lats, and shoulders so your upper body is fresh for Monday's dumbbell press session.",
        voiceStartAnnouncementMl = "ആറാം ഘട്ടം: നെഞ്ചിനും തോളുകൾക്കും നട്ടെല്ലിനും വിശ്രമം നൽകുന്ന മൊബിലിറ്റി വ്യായാമങ്ങൾ ചെയ്യാം.",
      ),
      CardioRecoveryBlock(
        id = "cr_block_7",
        blockNumber = 7,
        pillar = CardioRecoveryPillar.PILLAR_4_CNS_PARASYMPATHETIC,
        title = "90/90 Diaphragmatic Breath & Vagus Nerve Reset",
        subtitle = "Supine spinal decompression with 4-4-6 parasympathetic breathing cadence",
        durationSeconds = b7Secs,
        isTreadmillBlock = false,
        inclinePercent = 0f,
        speedKmh = 0f,
        targetHeartRateZone = "Parasympathetic • CNS Reset",
        targetBpmRange = "65–82 BPM",
        cadenceOrTempoCue = "4s Inhale • 4s Hold • 6s Slow Exhale",
        recoveryTargetMuscles = "Central Nervous System, Lower Back & Diaphragm",
        primaryCoachDirective = "Lie on your back with legs elevated or knees bent. Follow the breathing ring: inhale 4 seconds through the nose, hold 4 seconds, exhale 6 seconds.",
        secondaryBreathingCue = "4s Nasal Inhale → 4s Hold → 6s Pursed-Lip Exhale",
        voiceStartAnnouncementEn = "Final Block: Parasympathetic Central Nervous System Reset. Lie back, relax your spine, and follow the 4-4-6 breathing cadence until the timer reaches zero.",
        voiceStartAnnouncementMl = "അവസാന ഘട്ടം: ശാന്തമായി കിടന്ന് ദീർഘശ്വാസം എടുക്കാം. അടുത്ത ആഴ്ചയിലെ വ്യായാമങ്ങൾക്കായി ശരീരത്തെയും മനസ്സിനെയും പൂർണ്ണമായി സജ്ജമാക്കാം.",
      ),
    )

    val treadmillSecs = blocks.filter { it.isTreadmillBlock }.sumOf { it.durationSeconds }
    val recoverySecs = totalSecs - treadmillSecs

    // Calorie burn & recovery readiness calculation
    val metMultiplier = when (clampedDiff) {
      1 -> 5.2f
      2 -> 6.8f
      3 -> 8.1f
      4 -> 9.4f
      else -> 10.8f
    }
    val estKcal = ((metMultiplier * 3.5f * athleteWeightKg / 200f) * clampedMins).roundToInt().coerceAtLeast(90)
    val readinessBoost = when (clampedDiff) {
      1 -> 98
      2 -> 96
      3 -> 92
      4 -> 88
      else -> 85
    }

    val diffTitle = when (clampedDiff) {
      1 -> "Lv.1 • Restorative Flush"
      2 -> "Lv.2 • Zone 2 & Recovery Base"
      3 -> "Lv.3 • Aerobic Incline Cruise"
      4 -> "Lv.4 • VO2 Surge + Mobility"
      else -> "Lv.5 • Peak Summit & CNS Reset"
    }

    return CalibratedCardioRecoveryPlan(
      totalDurationMinutes = clampedMins,
      totalDurationSeconds = totalSecs,
      difficultyLevel = clampedDiff,
      difficultyTitle = diffTitle,
      blocks = blocks,
      treadmillMinutes = (treadmillSecs / 60f).roundToInt().coerceAtLeast(1),
      mobilityAndRecoveryMinutes = (recoverySecs / 60f).roundToInt().coerceAtLeast(1),
      estimatedCaloriesBurned = estKcal,
      recoveryReadinessBoostPct = readinessBoost,
      peakInclinePercent = maxOf(b2Incline, b3Incline),
      peakSpeedKmh = maxOf(b2Speed, b3Speed),
    )
  }
}
