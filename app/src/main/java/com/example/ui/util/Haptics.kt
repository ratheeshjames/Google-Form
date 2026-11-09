package com.example.ui.util

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Tactile micro-interaction extensions on HapticFeedback.
 */
fun HapticFeedback.tickHaptic() {
  performHapticFeedback(HapticFeedbackType.TextHandleMove)
}

fun HapticFeedback.confirmHaptic() {
  performHapticFeedback(HapticFeedbackType.LongPress)
}

fun HapticFeedback.warningHaptic() {
  performHapticFeedback(HapticFeedbackType.LongPress)
}

fun HapticFeedback.celebrationHaptic(scope: CoroutineScope) {
  scope.launch(Dispatchers.Main) {
    performHapticFeedback(HapticFeedbackType.LongPress)
    delay(120L)
    performHapticFeedback(HapticFeedbackType.TextHandleMove)
    delay(120L)
    performHapticFeedback(HapticFeedbackType.LongPress)
  }
}
