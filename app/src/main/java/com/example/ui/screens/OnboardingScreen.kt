package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onOnboardingCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }

    // Only the 3 required biometrics: Weight, Height, Waist Length
    var heightText by remember { mutableStateOf("175") }
    var weightText by remember { mutableStateOf("75.0") }
    var waistText by remember { mutableStateOf("84.0") }

    var dumbbellWeightText by remember { mutableStateOf("8.0") }
    var selectedDurationMin by remember { mutableIntStateOf(30) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val parsedHeight = heightText.toFloatOrNull()?.coerceIn(120f, 230f) ?: 175f
    val parsedWeight = weightText.toFloatOrNull()?.coerceIn(35f, 220f) ?: 75f
    val parsedWaist = waistText.toFloatOrNull()?.coerceIn(50f, 180f) ?: 84f
    val parsedDumbbell = dumbbellWeightText.toFloatOrNull()?.coerceIn(2f, 50f) ?: 8f

    // Live Biometric & Automatic Goal Preview Calculations (derived purely from Height, Weight, Waist)
    val heightM = (parsedHeight / 100f).coerceAtLeast(1.2f)
    val previewBmi = parsedWeight / (heightM * heightM)
    val previewWhtr = parsedWaist / parsedHeight
    val previewRfmBf = if (isMale) {
        (64f - 20f * (parsedHeight / parsedWaist)).coerceIn(5f, 50f)
    } else {
        (76f - 20f * (parsedHeight / parsedWaist)).coerceIn(10f, 55f)
    }
    val previewLeanMass = parsedWeight * (1f - previewRfmBf / 100f)
    val goalWaist = (parsedHeight * 0.45f).coerceIn(64f, 92f)
    val goalBf = if (isMale) 13.5f else 20.5f
    val goalWeight = if (previewBmi > 23.0f || previewWhtr > 0.48f) {
        (previewLeanMass / (1f - goalBf / 100f)).coerceIn(heightM * heightM * 19.5f, parsedWeight - 0.5f)
    } else if (previewBmi < 19.5f) {
        (heightM * heightM * 21.5f).coerceAtLeast(parsedWeight + 1.5f)
    } else {
        (previewLeanMass / (1f - goalBf / 100f)).coerceIn(heightM * heightM * 20.5f, heightM * heightM * 23.5f)
    }
    val weightDelta = kotlin.math.abs(parsedWeight - goalWeight)
    val waistDelta = kotlin.math.abs(parsedWaist - goalWaist)
    val estWeeks = max(
        (weightDelta / 0.5f).roundToInt(),
        (waistDelta / 0.65f).roundToInt()
    ).coerceIn(4, 28)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Button(
                        onClick = {
                            val h = heightText.toFloatOrNull()
                            val w = weightText.toFloatOrNull()
                            val wst = waistText.toFloatOrNull()
                            if (h == null || h < 120f || h > 230f) {
                                errorMessage = "Please enter a valid Height between 120 cm and 230 cm."
                                return@Button
                            }
                            if (w == null || w < 35f || w > 220f) {
                                errorMessage = "Please enter a valid Weight between 35 kg and 220 kg."
                                return@Button
                            }
                            if (wst == null || wst < 50f || wst > 180f) {
                                errorMessage = "Please enter a valid Waist Length between 50 cm and 180 cm."
                                return@Button
                            }
                            errorMessage = null
                            val cleanName = nameInput.trim().ifEmpty { "Athlete" }
                            AthleteProfileStore.completeOnboarding(
                                context = context,
                                name = cleanName,
                                isMale = isMale,
                                heightCm = h,
                                weightKg = w,
                                waistCm = wst,
                                dumbbellWeightKg = parsedDumbbell,
                                sessionDurationMin = selectedDurationMin
                            )
                            val updatedProfile = AthleteProfileStore.activeProfile.value
                            scope.launch {
                                val db = GoogleFormDatabase.getInstance(context)
                                db.measurementDao().clearAllMeasurements()
                                db.measurementDao().insertMeasurement(
                                    BodyMeasurementEntity(
                                        timestamp = System.currentTimeMillis(),
                                        weightKg = updatedProfile.weightKg,
                                        heightCm = updatedProfile.heightCm,
                                        waistCm = updatedProfile.waistCm,
                                        bmi = updatedProfile.bmi,
                                        estimatedBodyFatPercent = updatedProfile.rfmBodyFatPct
                                    )
                                )
                                onOnboardingCompleted()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("complete_onboarding_button"),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Egg, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Hatch My Journey & Save Biometrics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Hero Banner with Animated 3D Sacred Egg Preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Animated 3D Egg Badge
                    OnboardingSacredEggPreview(
                        modifier = Modifier.size(92.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "ZERO MOCK DATA • 3-BIOMETRIC ENGINE",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Welcome to Your Transformation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Enter only your Weight, Height & Waist Length. Your 3D Warrior Chicken starts as an Egg today and cracks open & grows muscular as you reach your goal!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Step 1: Athlete Identity & Biological Sex (for RFM Formula)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "1. Athlete Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name") },
                        placeholder = { Text("e.g., Alex") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_name_input")
                    )

                    Text(
                        "Biological Sex (Used for Cedars-Sinai Relative Fat Mass equation)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = isMale,
                            onClick = { isMale = true },
                            label = { Text("Male", fontWeight = FontWeight.Bold) },
                            leadingIcon = if (isMale) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !isMale,
                            onClick = { isMale = false },
                            label = { Text("Female", fontWeight = FontWeight.Bold) },
                            leadingIcon = if (!isMale) {
                                { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Step 2: The 3 Core Biometrics (Weight, Height, Waist Length ONLY)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "2. Core 3 Biometrics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "Only 3 Inputs Needed",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    BiometricStepperInputRow(
                        title = "Current Weight (kg)",
                        subtitle = "Morning fasted weight",
                        valueText = weightText,
                        onValueTextChange = { weightText = it },
                        onStep = { delta ->
                            val next = (parsedWeight + delta).coerceIn(35f, 220f)
                            weightText = String.format(java.util.Locale.US, "%.1f", next)
                        },
                        stepDelta = 0.5f,
                        unit = "kg",
                        testTag = "onboarding_weight_input"
                    )

                    BiometricStepperInputRow(
                        title = "Height (cm)",
                        subtitle = "Standing barefoot height",
                        valueText = heightText,
                        onValueTextChange = { heightText = it },
                        onStep = { delta ->
                            val next = (parsedHeight + delta).coerceIn(120f, 230f)
                            heightText = String.format(java.util.Locale.US, "%.0f", next)
                        },
                        stepDelta = 1f,
                        unit = "cm",
                        testTag = "onboarding_height_input"
                    )

                    BiometricStepperInputRow(
                        title = "Waist Length (cm)",
                        subtitle = "Measured horizontally at navel level",
                        valueText = waistText,
                        onValueTextChange = { waistText = it },
                        onStep = { delta ->
                            val next = (parsedWaist + delta).coerceIn(50f, 180f)
                            waistText = String.format(java.util.Locale.US, "%.1f", next)
                        },
                        stepDelta = 0.5f,
                        unit = "cm",
                        testTag = "onboarding_waist_input"
                    )
                }
            }

            // Step 3: Equipment & Session Time Constraint
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "3. Dumbbell & Session Time Setup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    BiometricStepperInputRow(
                        title = "Dumbbell Base Weight (kg)",
                        subtitle = "Single dumbbell load (No pull-up bar required)",
                        valueText = dumbbellWeightText,
                        onValueTextChange = { dumbbellWeightText = it },
                        onStep = { delta ->
                            val next = (parsedDumbbell + delta).coerceIn(2f, 50f)
                            dumbbellWeightText = String.format(java.util.Locale.US, "%.1f", next)
                        },
                        stepDelta = 1.0f,
                        unit = "kg",
                        testTag = "onboarding_dumbbell_input"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Hands-Free Workout Duration",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (selectedDurationMin == 30) "30 Min (Default)" else "$selectedDurationMin Min",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = selectedDurationMin.toFloat(),
                        onValueChange = { raw ->
                            selectedDurationMin = ((raw / 5f).roundToInt() * 5).coerceIn(15, 90)
                        },
                        valueRange = 15f..90f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(15, 30, 45, 60, 75, 90).forEach { mins ->
                            Text(
                                text = if (mins == 30) "30m★" else "${mins}m",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedDurationMin == mins) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (selectedDurationMin == mins) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live Automatic Biometric & Goal Calculation Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "AUTO-CALCULATED GOAL & RFM",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF2E7D32).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "ETA ~$estWeeks Weeks",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreviewMetricPill(
                            label = "BMI",
                            value = String.format(java.util.Locale.US, "%.1f", previewBmi),
                            sub = "Height & Weight",
                            modifier = Modifier.weight(1f)
                        )
                        PreviewMetricPill(
                            label = "RFM Body Fat",
                            value = String.format(java.util.Locale.US, "%.1f%%", previewRfmBf),
                            sub = "Height & Waist",
                            modifier = Modifier.weight(1f)
                        )
                        PreviewMetricPill(
                            label = "Waist/Height",
                            value = String.format(java.util.Locale.US, "%.2f", previewWhtr),
                            sub = "Goal: 0.45",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PreviewMetricPill(
                            label = "Auto Goal Weight",
                            value = String.format(java.util.Locale.US, "%.1f kg", goalWeight),
                            sub = "From ${String.format(java.util.Locale.US, "%.1f", parsedWeight)} kg",
                            modifier = Modifier.weight(1f)
                        )
                        PreviewMetricPill(
                            label = "Auto Goal Waist",
                            value = String.format(java.util.Locale.US, "%.1f cm", goalWaist),
                            sub = "From ${String.format(java.util.Locale.US, "%.1f", parsedWaist)} cm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricStepperInputRow(
    title: String,
    subtitle: String,
    valueText: String,
    onValueTextChange: (String) -> Unit,
    onStep: (Float) -> Unit,
    stepDelta: Float,
    unit: String,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalIconButton(
                    onClick = { onStep(-stepDelta) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease $title", modifier = Modifier.size(18.dp))
                }
                OutlinedTextField(
                    value = valueText,
                    onValueChange = onValueTextChange,
                    singleLine = true,
                    suffix = { Text(unit, style = MaterialTheme.typography.labelSmall) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .width(105.dp)
                        .testTag(testTag)
                )
                FilledTonalIconButton(
                    onClick = { onStep(stepDelta) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase $title", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviewMetricPill(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OnboardingSacredEggPreview(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "egg_pulse")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "egg_spin"
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val rad = Math.toRadians(angle.toDouble())

        // Warm sanctuary nest ring beneath the egg
        drawOval(
            color = Color(0xFF8D5B2B).copy(alpha = 0.55f),
            topLeft = Offset(cx - size.width * 0.42f, cy + size.height * 0.24f),
            size = Size(size.width * 0.84f, size.height * 0.20f)
        )
        drawOval(
            color = Color(0xFFFFB300).copy(alpha = 0.55f),
            topLeft = Offset(cx - size.width * 0.42f, cy + size.height * 0.24f),
            size = Size(size.width * 0.84f, size.height * 0.20f),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Realistic natural cream-terracotta ovoid egg with soft specular highlight
        val eggW = size.width * 0.56f
        val eggH = size.height * 0.72f
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFDF8),
                    Color(0xFFF6E6CA),
                    Color(0xFFDFC092),
                    Color(0xFFB38856)
                ),
                center = Offset(
                    cx - eggW * 0.16f + (sin(rad) * 3f).toFloat(),
                    cy - eggH * 0.18f
                ),
                radius = eggH * 0.72f
            ),
            topLeft = Offset(cx - eggW / 2f, cy - eggH / 2f),
            size = Size(eggW, eggH)
        )

        // Soft studio light reflection
        drawOval(
            color = Color.White.copy(alpha = 0.45f),
            topLeft = Offset(cx - eggW * 0.24f, cy - eggH * 0.34f),
            size = Size(eggW * 0.16f, eggH * 0.24f)
        )
    }
}

