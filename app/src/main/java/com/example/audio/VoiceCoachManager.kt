package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

sealed class CoachingCue {
  data class ExerciseAnnouncement(val exerciseName: String, val setNumber: Int, val targetReps: Int, val weightText: String) : CoachingCue()
  data class Countdown(val secondsRemaining: Int) : CoachingCue()
  object BreathingCue : CoachingCue()
  object HalfwayMark : CoachingCue()
  object SessionCompleted : CoachingCue()
  data class Custom(val textEn: String, val textMl: String) : CoachingCue()
}

class VoiceCoachManager(
  private val context: Context,
  var languageCode: String = "en", // "en" for English (India), "ml" for Malayalam
  var speechRate: Float = 1.05f,   // Crisp, natural athletic coaching pace
  var pitch: Float = 1.0f,
  var isEnabled: Boolean = true,
) : TextToSpeech.OnInitListener {

  private data class PendingSpeech(
    val textEn: String,
    val textMl: String,
    val flush: Boolean,
    val isCountdownTick: Boolean,
  )

  private var tts: TextToSpeech? = null
  private var isInitialized = false
  private var pendingSpeech: PendingSpeech? = null
  private var toneGenerator: ToneGenerator? = null

  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
  private var audioFocusRequest: AudioFocusRequest? = null

  var onCoachSpoke: ((text: String) -> Unit)? = null
  var isSpeakingState: Boolean = false

  init {
    try {
      tts = TextToSpeech(context.applicationContext, this)
    } catch (e: Exception) {
      Log.e("VoiceCoachManager", "Failed to initialize TTS engine: ${e.message}")
    }
    try {
      toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (e: Exception) {
      Log.w("VoiceCoachManager", "ToneGenerator unavailable: ${e.message}")
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      applyLanguageSettings()
      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          isSpeakingState = true
        }
        override fun onDone(utteranceId: String?) {
          isSpeakingState = false
          abandonAudioFocus()
        }
        override fun onError(utteranceId: String?) {
          isSpeakingState = false
          abandonAudioFocus()
        }
      })

      // Immediately speak any utterance that was requested while TTS was initializing
      pendingSpeech?.let { pending ->
        pendingSpeech = null
        speakInternal(
          textEn = pending.textEn,
          textMl = pending.textMl,
          flush = pending.flush,
          isCountdownTick = pending.isCountdownTick,
        )
      }
    } else {
      Log.w("VoiceCoachManager", "TTS initialization failed with status: $status")
    }
  }

  fun applyLanguageSettings() {
    if (!isInitialized) return

    val targetLocale = if (languageCode == "ml") {
      Locale.forLanguageTag("ml-IN")
    } else {
      Locale.forLanguageTag("en-IN")
    }

    var result = tts?.setLanguage(targetLocale)
    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
      result = tts?.setLanguage(if (languageCode == "ml") Locale.forLanguageTag("ml") else Locale.forLanguageTag("en-GB"))
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.language = Locale.US
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      try {
        val voices = tts?.voices
        if (!voices.isNullOrEmpty()) {
          val matchedVoice = voices.firstOrNull { voice: Voice ->
            if (languageCode == "ml") {
              voice.locale.language.equals("ml", ignoreCase = true) && !voice.isNetworkConnectionRequired
            } else {
              voice.locale.language.equals("en", ignoreCase = true) &&
                voice.locale.country.equals("IN", ignoreCase = true) &&
                !voice.isNetworkConnectionRequired
            }
          } ?: voices.firstOrNull { voice: Voice ->
            if (languageCode == "ml") {
              voice.locale.language.equals("ml", ignoreCase = true)
            } else {
              voice.locale.language.equals("en", ignoreCase = true) &&
                (voice.locale.country.equals("IN", ignoreCase = true) || voice.name.contains("in", ignoreCase = true))
            }
          }

          if (matchedVoice != null) {
            tts?.voice = matchedVoice
          }
        }
      } catch (e: Exception) {
        Log.w("VoiceCoachManager", "Voice selection lookup: ${e.message}")
      }
    }

    tts?.setSpeechRate(speechRate.coerceIn(0.9f, 1.3f))
    tts?.setPitch(pitch.coerceIn(0.9f, 1.2f))
  }

  fun setLanguage(code: String) {
    if (languageCode != code) {
      languageCode = code
      applyLanguageSettings()
    }
  }

  fun isSpeaking(): Boolean {
    return isSpeakingState || (tts?.isSpeaking == true)
  }

  fun playCountdownBeep(isFinalGo: Boolean = false) {
    if (!isEnabled) return
    try {
      if (isFinalGo) {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
      } else {
        toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
      }
    } catch (e: Exception) {
      Log.w("VoiceCoachManager", "Beep playback failed: ${e.message}")
    }
  }

  fun speak(textEn: String, textMl: String = textEn) {
    speakCoach(textEn, textMl, flush = true)
  }

  fun speakCoach(
    textEn: String,
    textMl: String = textEn,
    flush: Boolean = true,
    isCountdownTick: Boolean = false,
  ) {
    val displaySpeech = if (languageCode == "ml") textMl else textEn
    if (displaySpeech.isNotBlank()) {
      onCoachSpoke?.invoke(displaySpeech)
    }

    if (!isEnabled) return

    if (!isInitialized) {
      // Queue latest speech request until TTS finishes initializing
      pendingSpeech = PendingSpeech(textEn, textMl, flush, isCountdownTick)
      return
    }

    speakInternal(textEn, textMl, flush, isCountdownTick)
  }

  private fun speakInternal(
    textEn: String,
    textMl: String,
    flush: Boolean,
    isCountdownTick: Boolean,
  ) {
    val textToSpeak = if (languageCode == "ml") textMl else textEn
    if (textToSpeak.isBlank()) return

    requestAudioFocus()
    // Slightly faster speech rate on 1-second countdown numbers so they complete cleanly within ~450ms
    val rateToUse = if (isCountdownTick) 1.22f else speechRate.coerceIn(0.95f, 1.15f)
    tts?.setSpeechRate(rateToUse)

    val params = Bundle().apply {
      putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
    }
    val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
    tts?.speak(textToSpeak, queueMode, params, "coach_${System.nanoTime()}")
  }

  fun speakCue(cue: CoachingCue) {
    when (cue) {
      is CoachingCue.ExerciseAnnouncement ->
        speakCoach(
          "Next is ${cue.exerciseName}. Set ${cue.setNumber}. Target: ${cue.targetReps} reps at ${cue.weightText}.",
          flush = true,
        )
      is CoachingCue.Countdown -> {
        playCountdownBeep(isFinalGo = cue.secondsRemaining == 0)
        speakCoach("${cue.secondsRemaining}", flush = true, isCountdownTick = true)
      }
      is CoachingCue.BreathingCue ->
        speakCoach("Breathe in deeply on the descent, exhale on the drive.", flush = true)
      is CoachingCue.HalfwayMark ->
        speakCoach("15 minutes completed! Exactly halfway through our workout budget.", flush = false)
      is CoachingCue.SessionCompleted ->
        speakCoach("Workout completed! Excellent effort.", flush = true)
      is CoachingCue.Custom ->
        speakCoach(cue.textEn, cue.textMl, flush = true)
    }
  }

  private fun requestAudioFocus() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val playbackAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

      val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(playbackAttributes)
        .build()

      audioFocusRequest = request
      audioManager.requestAudioFocus(request)
    } else {
      @Suppress("DEPRECATION")
      audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
    }
  }

  private fun abandonAudioFocus() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
    } else {
      @Suppress("DEPRECATION")
      audioManager.abandonAudioFocus(null)
    }
  }

  fun stop() {
    pendingSpeech = null
    tts?.stop()
    isSpeakingState = false
    abandonAudioFocus()
  }

  fun shutdown() {
    stop()
    try {
      toneGenerator?.release()
    } catch (_: Exception) {}
    toneGenerator = null
    tts?.shutdown()
    tts = null
    isInitialized = false
  }
}
