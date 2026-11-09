package com.example.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AthleteProfile
import com.example.data.AthleteProfileStore
import com.example.data.GoogleFormDatabase
import com.example.data.entity.BodyMeasurementEntity
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherSheet(
    onDismiss: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profiles by AthleteProfileStore.profiles.collectAsState()
    val activeProfileId by AthleteProfileStore.activeProfileId.collectAsState()
    val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

    // Modes: 0 = Overview/Switch, 1 = Edit Active Biometrics (Height, Weight, Waist ONLY), 2 = Create New Profile
    var sheetMode by remember { mutableIntStateOf(0) }

    // Form State — strictly Height, Weight, Waist Length + Dumbbell & Session Duration
    var nameInput by remember(activeProfile, sheetMode) {
        mutableStateOf(if (sheetMode == 2) "" else activeProfile.name)
    }
    var isMaleInput by remember(activeProfile, sheetMode) {
        mutableStateOf(if (sheetMode == 2) true else activeProfile.isMale)
    }
    var heightInput by remember(activeProfile, sheetMode) {
        mutableStateOf(String.format(Locale.US, "%.0f", activeProfile.heightCm))
    }
    var weightInput by remember(activeProfile, sheetMode) {
        mutableStateOf(String.format(Locale.US, "%.1f", activeProfile.weightKg))
    }
    var waistInput by remember(activeProfile, sheetMode) {
        mutableStateOf(String.format(Locale.US, "%.1f", activeProfile.waistCm))
    }
    var dumbbellInput by remember(activeProfile, sheetMode) {
        mutableStateOf(String.format(Locale.US, "%.1f", activeProfile.dumbbellWeightKg))
    }
    var durationInput by remember(activeProfile, sheetMode) {
        mutableIntStateOf(activeProfile.sessionDurationMin)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (sheetMode) {
                            1 -> "Edit 3-Biometric Profile"
                            2 -> "Create Athlete Profile"
                            else -> "Athlete Biometric Profile"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = when (sheetMode) {
                            1 -> "Height, Weight & Waist Length • Auto-calculates Goal & RFM"
                            2 -> "Set up a new profile with Weight, Height & Waist Length"
                            else -> "Manage profiles, 3-biometric inputs & automatic goals"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (sheetMode != 0) {
                    TextButton(onClick = { sheetMode = 0 }) {
                        Text("Back")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sheetMode == 0) {
                // Active Profile Card with Live Auto-Calculated Goal Weight & Time
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeProfile.initials,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = activeProfile.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "LVL ${activeProfile.level} • ${activeProfile.evolutionStage.badge}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                Text(
                                    text = "${activeProfile.heightCm.toInt()} cm • ${String.format(Locale.US, "%.1f", activeProfile.weightKg)} kg • ${String.format(Locale.US, "%.1f", activeProfile.waistCm)} cm Waist",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "BMI ${String.format(Locale.US, "%.1f", activeProfile.bmi)} • RFM ${String.format(Locale.US, "%.1f%%", activeProfile.rfmBodyFatPct)} • WHtR ${String.format(Locale.US, "%.2f", activeProfile.whtr)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledTonalIconButton(
                                onClick = { sheetMode = 1 },
                                modifier = Modifier.testTag("edit_biometrics_button")
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Biometrics")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Automatic Goal Weight & Time-to-Goal Banner
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "AUTOMATIC GOAL PROJECTION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF2E7D32).copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "ETA ~${activeProfile.autoWeeksToGoal} Weeks",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Target Weight: ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWeightKg)} kg • Target Waist: ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWaistCm)} cm (${String.format(Locale.US, "%.1f%%", activeProfile.autoGoalBodyFatPct)} RFM)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Recommended Intake: ${activeProfile.autoDailyCaloriesKcal} kcal/day • ${activeProfile.autoDailyProteinG}g Protein",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { sheetMode = 1 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_biometric_editor_btn"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Biometrics",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            OutlinedButton(
                                onClick = { sheetMode = 2 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("create_new_profile_btn"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "New Profile",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Profile Switcher List
                Text(
                    text = "Switch Athlete Profile (${profiles.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    profiles.forEach { profile ->
                        val isSelected = profile.id == activeProfileId
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    AthleteProfileStore.switchProfile(profile.id, context)
                                }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = profile.initials,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", profile.weightKg)} kg • ${profile.heightCm.toInt()} cm • Waist ${String.format(Locale.US, "%.1f", profile.waistCm)} cm",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Active Profile",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Mode 1 (Edit Biometrics) or Mode 2 (Create New Profile) — ONLY Height, Weight, Waist
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Athlete Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = isMaleInput,
                                onClick = { isMaleInput = true },
                                label = { Text("Male (RFM)") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !isMaleInput,
                                onClick = { isMaleInput = false },
                                label = { Text("Female (RFM)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                label = { Text("Weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("Height (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = waistInput,
                                onValueChange = { waistInput = it },
                                label = { Text("Waist Length (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = dumbbellInput,
                                onValueChange = { dumbbellInput = it },
                                label = { Text("Dumbbell (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Constrained Workout Duration",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$durationInput Min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = durationInput.toFloat(),
                            onValueChange = { raw ->
                                durationInput = (Math.round(raw / 5f) * 5).coerceIn(15, 90)
                            },
                            valueRange = 15f..90f,
                            steps = 14,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = {
                                val parsedHeight = heightInput.toFloatOrNull()?.coerceIn(120f, 230f) ?: activeProfile.heightCm
                                val parsedWeight = weightInput.toFloatOrNull()?.coerceIn(35f, 220f) ?: activeProfile.weightKg
                                val parsedWaist = waistInput.toFloatOrNull()?.coerceIn(50f, 180f) ?: activeProfile.waistCm
                                val parsedDumbbell = dumbbellInput.toFloatOrNull()?.coerceIn(2f, 50f) ?: activeProfile.dumbbellWeightKg

                                if (sheetMode == 2) {
                                    AthleteProfileStore.createNewProfile(
                                        name = nameInput.ifBlank { "New Athlete" },
                                        isMale = isMaleInput,
                                        heightCm = parsedHeight,
                                        weightKg = parsedWeight,
                                        waistCm = parsedWaist,
                                        dumbbellWeightKg = parsedDumbbell,
                                        sessionDurationMin = durationInput,
                                        context = context
                                    )
                                } else {
                                    AthleteProfileStore.updateActiveProfile(context) { current ->
                                        current.copy(
                                            name = nameInput.ifBlank { current.name },
                                            initials = nameInput.trim().split(" ")
                                                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                                .take(2)
                                                .joinToString("")
                                                .ifEmpty { current.initials },
                                            isMale = isMaleInput,
                                            heightCm = parsedHeight,
                                            weightKg = parsedWeight,
                                            waistCm = parsedWaist,
                                            dumbbellWeightKg = parsedDumbbell,
                                            sessionDurationMin = durationInput
                                        )
                                    }
                                }
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
                                }
                                sheetMode = 0
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_biometrics_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                if (sheetMode == 2) "Create Profile & Calculate Goal"
                                else "Save 3 Biometrics & Recalculate Goal",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
