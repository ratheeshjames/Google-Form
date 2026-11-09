package com.example.audio

object WorkoutCoachNarration {

  /**
   * Spoken immediately when entering PREPARING (10s window: 10s down to 6s = 5 full seconds for speech,
   * leaving 5, 4, 3, 2, 1 completely clear for the countdown).
   */
  fun getPrepStartAnnouncement(
    isFirstSetOfSession: Boolean,
    dayNumber: Int,
    exerciseIndex: Int,
    totalExercises: Int,
    exerciseName: String,
    setNumber: Int,
    totalSets: Int,
    reps: Int,
    weightStr: String,
  ): Pair<String, String> {
    val en = if (isFirstSetOfSession) {
      "Day $dayNumber workout starting! Exercise 1: $exerciseName. Set 1 of $totalSets, $reps reps at $weightStr."
    } else if (setNumber == 1) {
      "Next exercise: $exerciseName! Set 1 of $totalSets. Target: $reps reps at $weightStr."
    } else {
      "Get ready for $exerciseName, Set $setNumber of $totalSets. Target: $reps reps at $weightStr."
    }

    val ml = if (isFirstSetOfSession) {
      "വ്യായാമം തുടങ്ങുന്നു! ഒന്നാമത്തേത്: $exerciseName. സെറ്റ് 1, $reps റെപ്സ്."
    } else if (setNumber == 1) {
      "അടുത്ത വ്യായാമം: $exerciseName! സെറ്റ് 1, $reps റെപ്സ്."
    } else {
      "$exerciseName, സെറ്റ് $setNumber. $reps റെപ്സ്."
    }

    return en to ml
  }

  /**
   * Spoken right when ACTIVE_WORK begins at 40s (window: 40s down to 33s).
   */
  fun getWorkStartAnnouncement(
    exerciseName: String,
    setNumber: Int,
    reps: Int,
  ): Pair<String, String> {
    val (formEn, formMl) = getShortFormCue(exerciseName)
    val en = "Begin Set $setNumber! $exerciseName, $reps reps. $formEn"
    val ml = "സെറ്റ് $setNumber ആരംഭിക്കുക! $exerciseName, $reps റെപ്സ്. $formMl"
    return en to ml
  }

  fun getShortFormCue(exerciseName: String): Pair<String, String> {
    val lower = exerciseName.lowercase()
    return when {
      lower.contains("incline bench press") ->
        "Pinch your shoulder blades and drive through the upper chest!" to
        "തോളുകൾ പിന്നിലേക്ക് വലിച്ച് ഉയർത്തുക!"

      lower.contains("bench press") ->
        "Plant your feet firm, lower smoothly and press strong!" to
        "കാലുകൾ ഉറപ്പിച്ചു നിർത്തി ശക്തിയോടെ ഉയർത്തുക!"

      lower.contains("fly") ->
        "Slight bend in your elbows, feel the chest stretch and squeeze!" to
        "കൈമുട്ടുകൾ ചെറുതായി മടക്കി നെഞ്ച് വിടർത്തുക!"

      lower.contains("french press") ->
        "Keep elbows locked in place and extend through your triceps!" to
        "കൈമുട്ടുകൾ അനക്കാതെ ട്രൈസെപ്സ് ഉപയോഗിച്ച് ഉയർത്തുക!"

      lower.contains("kickback") ->
        "Elbows pinned high, kick back and squeeze the tricep peak!" to
        "കൈമുട്ടുകൾ ഉയർത്തി പിന്നിലേക്ക് നീട്ടി മുറുക്കുക!"

      lower.contains("pull-up") || lower.contains("pullover") ->
        "Chest proud, drive your elbows down and squeeze your lats!" to
        "നെഞ്ച് ഉയർത്തി പുറത്തെ പേശികൾ മുറുക്കുക!"

      lower.contains("row") ->
        "Flat back, pull the dumbbells to your hips and squeeze!" to
        "നട്ടെല്ല് നിവർത്തി വെയ്റ്റ് ഇടുപ്പിലേക്ക് വലിക്കുക!"

      lower.contains("curl") ->
        "Elbows pinned to your sides, zero swinging, squeeze the biceps!" to
        "കൈകൾ ശരീരത്തോട് ചേർത്തുനിർത്തി ബൈസെപ്സ് മുറുക്കുക!"

      lower.contains("squat") ->
        "Chest tall, hit full depth and drive hard through your heels!" to
        "നെഞ്ച് ഉയർത്തി ഇരുന്ന് ശക്തമായി ഉയരുക!"

      lower.contains("deadlift") ->
        "Flat back, hinge at the hips and drive your hips forward!" to
        "നട്ടെല്ല് നിവർത്തി ഇടുപ്പ് മുന്നിലേക്ക് തള്ളി നിവരുക!"

      lower.contains("press") ->
        "Brace your core tight and press straight overhead!" to
        "വയർ മുറുക്കിപ്പിടിച്ച് മുകളിലേക്ക് ഉയർത്തുക!"

      lower.contains("raise") || lower.contains("crunch") ->
        "Exhale as you lift and squeeze your abs hard at the top!" to
        "ശ്വാസം വിട്ടു വയറിന്റെ പേശികൾ മുറുക്കുക!"

      else ->
        "Controlled tempo down, explosive power on the drive!" to
        "കൃത്യമായ വേഗതയിൽ ശ്രദ്ധയോടെ ചെയ്യുക!"
    }
  }

  /**
   * Spoken during ACTIVE_WORK at 30s, 20s, and 10s.
   * Each cue is concise (~2.5 seconds) so it finishes well before the next cue or countdown.
   */
  fun getWorkPacingCue(secondsRemaining: Int, exerciseName: String): Pair<String, String> {
    return when (secondsRemaining) {
      30 -> "Thirty seconds left! Breathe in going down, exhale as you drive up!" to
            "മുപ്പത് സെക്കൻഡ് ബാക്കി! ശ്വാസം കൃത്യമായി നിലനിർത്തുക!"

      20 -> "Halfway through the set! Twenty seconds left. Keep your form tight!" to
            "പകുതി സമയം കഴിഞ്ഞു! ഇരുപത് സെക്കൻഡ് ബാക്കി!"

      10 -> "Final ten seconds of $exerciseName! Give me two more strong reps!" to
            "അവസാന പത്ത് സെക്കൻഡ്! രണ്ട് തവണ കൂടി ശക്തമായി ചെയ്യൂ!"

      else -> "" to ""
    }
  }

  /**
   * Spoken immediately when REST starts at 30s (window: 30s down to 22s).
   */
  fun getRestStartAnnouncement(
    restSeconds: Int,
    completedExerciseName: String,
    completedSetNumber: Int,
    nextExerciseName: String,
    nextSetNumber: Int,
  ): Pair<String, String> {
    val en = "Set $completedSetNumber complete! Rest for $restSeconds seconds. Next up: $nextExerciseName, Set $nextSetNumber."
    val ml = "സെറ്റ് $completedSetNumber കഴിഞ്ഞു! $restSeconds സെക്കൻഡ് വിശ്രമം. അടുത്തത്: $nextExerciseName, സെറ്റ് $nextSetNumber."
    return en to ml
  }

  /**
   * Spoken during REST at 20s and 10s.
   */
  fun getRestMidCue(secondsRemaining: Int, nextExerciseName: String, nextSetNumber: Int): Pair<String, String> {
    return when (secondsRemaining) {
      20 -> "Twenty seconds of rest left. Deep recovery breaths, shake out your arms." to
            "ഇരുപത് സെക്കൻഡ് വിശ്രമം ബാക്കി. ദീർഘമായി ശ്വാസമെടുക്കുക."

      10 -> "Ten seconds left in rest! Pick up your dumbbells for $nextExerciseName, Set $nextSetNumber." to
            "പത്ത് സെക്കൻഡ് ബാക്കി! $nextExerciseName ചെയ്യാൻ തയ്യാറാവുക."

      else -> "" to ""
    }
  }

  /**
   * Single-word spoken numbers for exact 1-second synchronization (5, 4, 3, 2, 1).
   */
  fun getSpokenCountdownWord(second: Int): Pair<String, String> {
    return when (second) {
      5 -> "Five" to "അഞ്ച്"
      4 -> "Four" to "നാല്"
      3 -> "Three" to "മൂന്ന്"
      2 -> "Two" to "രണ്ട്"
      1 -> "One" to "ഒന്ന്"
      else -> "$second" to "$second"
    }
  }
}
