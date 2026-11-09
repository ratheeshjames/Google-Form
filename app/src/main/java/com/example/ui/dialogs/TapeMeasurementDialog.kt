package com.example.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TapeMeasurementDialog(
    onDismiss: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

    var weightText by remember(activeProfile.weightKg) {
        mutableStateOf(String.format(Locale.US, "%.1f", activeProfile.weightKg))
    }
    var waistText by remember(activeProfile.waistCm) {
        mutableStateOf(String.format(Locale.US, "%.1f", activeProfile.waistCm))
    }
    var heightText by remember(activeProfile.heightCm) {
        mutableStateOf(String.format(Locale.US, "%.0f", activeProfile.heightCm))
    }

    val parsedWeight = weightText.toFloatOrNull()?.coerceIn(35f, 220f) ?: activeProfile.weightKg
    val parsedWaist = waistText.toFloatOrNull()?.coerceIn(50f, 180f) ?: activeProfile.waistCm
    val parsedHeight = heightText.toFloatOrNull()?.coerceIn(120f, 230f) ?: activeProfile.heightCm

    val heightM = (parsedHeight / 100f).coerceAtLeast(1.2f)
    val liveBmi = parsedWeight / (heightM * heightM)
    val liveWhtr = parsedWaist / parsedHeight
    val liveRfm = if (activeProfile.isMale) {
        (64f - 20f * (parsedHeight / parsedWaist)).coerceIn(5f, 50f)
    } else {
        (76f - 20f * (parsedHeight / parsedWaist)).coerceIn(10f, 55f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Straighten,
                contentDescription = "3-Biometric Check-In",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "3-Biometric Check-In",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Update your Weight, Waist Length, and Height. BMI, Relative Fat Mass (RFM %), Waist-to-Height Ratio, and your 3D Warrior Chicken evolution update automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Live Preview Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("BMI", style = MaterialTheme.typography.labelSmall)
                            Text(
                                String.format(Locale.US, "%.1f", liveBmi),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            Text("RFM Body Fat", style = MaterialTheme.typography.labelSmall)
                            Text(
                                String.format(Locale.US, "%.1f%%", liveRfm),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text("Waist/Height", style = MaterialTheme.typography.labelSmall)
                            Text(
                                String.format(Locale.US, "%.2f", liveWhtr),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Weight Input with Steppers
                DialogBiometricRow(
                    label = "Weight (kg)",
                    value = weightText,
                    onValueChange = { weightText = it },
                    onStep = { delta ->
                        val next = (parsedWeight + delta).coerceIn(35f, 220f)
                        weightText = String.format(Locale.US, "%.1f", next)
                    },
                    step = 0.2f,
                    testTag = "input_weight"
                )

                // Waist Input with Steppers
                DialogBiometricRow(
                    label = "Waist Length (cm)",
                    value = waistText,
                    onValueChange = { waistText = it },
                    onStep = { delta ->
                        val next = (parsedWaist + delta).coerceIn(50f, 180f)
                        waistText = String.format(Locale.US, "%.1f", next)
                    },
                    step = 0.5f,
                    testTag = "input_waist"
                )

                // Height Input with Steppers
                DialogBiometricRow(
                    label = "Height (cm)",
                    value = heightText,
                    onValueChange = { heightText = it },
                    onStep = { delta ->
                        val next = (parsedHeight + delta).coerceIn(120f, 230f)
                        heightText = String.format(Locale.US, "%.0f", next)
                    },
                    step = 1.0f,
                    testTag = "input_height"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AthleteProfileStore.recordBiometricCheckIn(
                        weightKg = parsedWeight,
                        waistCm = parsedWaist,
                        heightCm = parsedHeight,
                        context = context
                    )
                    val updated = AthleteProfileStore.activeProfile.value
                    scope.launch {
                        GoogleFormDatabase.getInstance(context).measurementDao().insertMeasurement(
                            BodyMeasurementEntity(
                                timestamp = System.currentTimeMillis(),
                                weightKg = updated.weightKg,
                                heightCm = updated.heightCm,
                                waistCm = updated.waistCm,
                                bmi = updated.bmi,
                                estimatedBodyFatPercent = updated.rfmBodyFatPct
                            )
                        )
                        onSaveSuccess()
                    }
                },
                modifier = Modifier.testTag("save_measurements_button")
            ) {
                Text("Save Check-In (+35 XP)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DialogBiometricRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    onStep: (Float) -> Unit,
    step: Float,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalIconButton(
            onClick = { onStep(-step) },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Decrease $label", modifier = Modifier.size(18.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )
        FilledTonalIconButton(
            onClick = { onStep(step) },
            modifier = Modifier.size(38.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Increase $label", modifier = Modifier.size(18.dp))
        }
    }
}
