package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AthleteProfile
import com.example.data.AthleteProfileStore
import com.example.data.ChickenAccessoryGift
import com.example.data.ChickenEvolutionStage
import com.example.data.RoosterPlumagePalette
import com.example.data.WeeklyQuest
import com.example.data.allChickenAccessoryGifts
import com.example.data.allRoosterPlumagePalettes
import com.example.ui.theme.GoogleGreen40
import com.example.ui.theme.GoogleYellow40
import com.example.ui.theme.PillShape
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Interactive 2D Rooster Action States (v2 Release).
 */
enum class RoosterActionState(
  val label: String,
  val badgeLabel: String,
  val icon: ImageVector,
) {
  IDLE("Idle", "2D • Sanctuary Stance", Icons.Filled.SelfImprovement),
  POWER_FLEX("Power Flex", "2D • Double Bicep Flex", Icons.Filled.Bolt),
  LIFT_IRON("Lift Iron", "2D • Dumbbell Curls", Icons.Filled.FitnessCenter),
  PROTEIN_FEAST("Protein Feast", "2D • Protein Recovery", Icons.Filled.Restaurant),
}

/**
 * Resolved 2D Color Tokens for the 4 Unlockable Plumage Palettes & Elemental Auras.
 */
private data class Resolved2DPlumageTheme(
  val plumageTop: Color,
  val plumageMid: Color,
  val plumageDeep: Color,
  val tailPrimary: Color,
  val tailSecondary: Color,
  val combBright: Color,
  val combDeep: Color,
  val auraInner: Color,
  val auraOuter: Color,
  val particleColor: Color,
)

private fun resolve2DPlumageTheme(paletteId: String, muscle: Float): Resolved2DPlumageTheme {
  return when (paletteId) {
    "crimson_phoenix" -> Resolved2DPlumageTheme(
      plumageTop = Color(0xFFFFAB91),
      plumageMid = lerp2DColor(Color(0xFFFF5722), Color(0xFFD84315), muscle),
      plumageDeep = Color(0xFF7F1D1D),
      tailPrimary = Color(0xFFB71C1C),
      tailSecondary = Color(0xFF450A0A),
      combBright = Color(0xFFFFD54F),
      combDeep = Color(0xFFFF8F00),
      auraInner = Color(0xFFFF5252),
      auraOuter = Color(0xFFFF6D00),
      particleColor = Color(0xFFFFAB40),
    )
    "emerald_jade" -> Resolved2DPlumageTheme(
      plumageTop = Color(0xFFA7F3D0),
      plumageMid = lerp2DColor(Color(0xFF34D399), Color(0xFF059669), muscle),
      plumageDeep = Color(0xFF064E3B),
      tailPrimary = Color(0xFF0F766E),
      tailSecondary = Color(0xFF042F2E),
      combBright = Color(0xFFFF5252),
      combDeep = Color(0xFFC62828),
      auraInner = Color(0xFF34D399),
      auraOuter = Color(0xFF10B981),
      particleColor = Color(0xFF6EE7B7),
    )
    "obsidian_shadow" -> Resolved2DPlumageTheme(
      plumageTop = Color(0xFF94A3B8),
      plumageMid = lerp2DColor(Color(0xFF475569), Color(0xFF334155), muscle),
      plumageDeep = Color(0xFF0F172A),
      tailPrimary = Color(0xFF312E81),
      tailSecondary = Color(0xFF090D16),
      combBright = Color(0xFF38BDF8),
      combDeep = Color(0xFF0284C7),
      auraInner = Color(0xFF38BDF8),
      auraOuter = Color(0xFF6366F1),
      particleColor = Color(0xFF7DD3FC),
    )
    else -> Resolved2DPlumageTheme(
      plumageTop = Color(0xFFFFE082),
      plumageMid = lerp2DColor(Color(0xFFFFCA28), Color(0xFFFFB300), muscle),
      plumageDeep = lerp2DColor(Color(0xFFFF8F00), Color(0xFFE65100), muscle),
      tailPrimary = Color(0xFF0F524B),
      tailSecondary = Color(0xFF1E293B),
      combBright = Color(0xFFFF5252),
      combDeep = Color(0xFFD32F2F),
      auraInner = Color(0xFFFFD54F),
      auraOuter = Color(0xFFFF8F00),
      particleColor = Color(0xFFFFE082),
    )
  }
}

/**
 * Main Dashboard 2D Evolution Companion Card:
 * Integrates the 2D Illustrated Egg-to-Warrior-Rooster Evolution directly onto the Today Dashboard.
 * Features:
 * - 4 Evolving 2D Dojo Backgrounds across the 12 stages
 * - 4 Interactive 2D Rooster Actions (Idle, Power Flex, Lift Iron, Protein Feast)
 * - 4 Unlockable Plumage Palettes & Elemental Aura Effects
 * - Milestone Gift Accessories
 */
@Composable
fun DashboardChickenCompanionCard(
  activeProfile: AthleteProfile,
  onOpenBiometricsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val liveProgress = activeProfile.goalJourneyProgress.coerceIn(0f, 1f)
  val currentStage = activeProfile.evolutionStage
  val nextStage = activeProfile.nextEvolutionStage
  val stageFraction = activeProfile.currentStageProgressFraction
  val microLevel = activeProfile.growthMicroLevel
  val equippedIds = activeProfile.equippedAccessories
  val unlockedCount = allChickenAccessoryGifts.count { activeProfile.isAccessoryUnlocked(it) }
  val unlockedPalettesCount = allRoosterPlumagePalettes.count { activeProfile.isPaletteUnlocked(it) }

  var actionState by remember { mutableStateOf(RoosterActionState.IDLE) }
  var showAccessoriesPanel by remember { mutableStateOf(false) }
  var showPalettesPanel by remember { mutableStateOf(false) }

  val infiniteTransition = rememberInfiniteTransition(label = "dashboard_chicken_2d")
  val breathPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "breath_phase",
  )

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("dashboard_chicken_companion_card"),
    shape = RoundedCornerShape(28.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Top Header: Stage Badge + Stage Title + Dojo Tier Pill
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE65100),
          ) {
            Text(
              text = "STAGE ${currentStage.stageNumber}/12",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Black,
              color = Color.White,
              maxLines = 1,
              softWrap = false,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = currentStage.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = "Dojo Tier ${currentStage.dojoTierNumber}/4: ${currentStage.dojoTierName} • Lv.$microLevel/100",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Surface(
          shape = PillShape,
          color = MaterialTheme.colorScheme.secondaryContainer,
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(
              imageVector = Icons.Filled.Security,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(13.dp),
            )
            Text(
              text = "${activeProfile.streakFreezeShields} Shield${if (activeProfile.streakFreezeShields == 1) "" else "s"}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              softWrap = false,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
          }
        }
      }

      // Dual Gradual Progress Indicators
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (nextStage != null) {
                "Next: Stage ${nextStage.stageNumber} (${nextStage.shortLabel})"
              } else {
                "Maximum Stage Reached • Apex Warrior Rooster"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "${(stageFraction * 100).toInt()}% of stage",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFE65100),
            )
          }
          LinearProgressIndicator(
            progress = { stageFraction.coerceIn(0.03f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(7.dp)
              .clip(CircleShape),
            color = Color(0xFFFF8F00),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Timeline (${activeProfile.completedWorkouts}/${activeProfile.totalTimelineWorkoutsToGoal} sessions)",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
              text = "🔥 ${activeProfile.streakDays}d Streak • Lv.${activeProfile.avatarLevel} (${activeProfile.xp} XP)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Crisp 2D Illustrated Viewport with Evolving Dojo Background & Interactive Actions
      IllustratedChicken2DViewport(
        growthProgress = liveProgress,
        stage = currentStage,
        breathPhase = breathPhase,
        actionState = actionState,
        paletteId = activeProfile.selectedPlumagePalette,
        equippedAccessories = equippedIds,
        activeProfile = activeProfile,
        onTapViewport = {
          val nextIdx = (actionState.ordinal + 1) % RoosterActionState.entries.size
          actionState = RoosterActionState.entries[nextIdx]
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(315.dp)
          .testTag("chicken_2d_viewport"),
      )

      // Interactive 2D Rooster Action Bar (Idle | Power Flex | Lift Iron | Protein Feast)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        RoosterActionState.entries.forEach { state ->
          val selected = actionState == state
          FilterChip(
            selected = selected,
            onClick = { actionState = state },
            leadingIcon = {
              Icon(
                imageVector = state.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
            },
            label = {
              Text(
                text = state.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
              )
            },
            shape = PillShape,
          )
        }
      }

      Text(
        text = currentStage.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      // Bottom Control Row: Gifts | Plumage & Aura | Log Weight/Waist
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = {
            showAccessoriesPanel = !showAccessoriesPanel
            if (showAccessoriesPanel) showPalettesPanel = false
          },
          modifier = Modifier
            .weight(1f)
            .testTag("toggle_chicken_gifts_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.CardGiftcard,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Gifts ($unlockedCount/${allChickenAccessoryGifts.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }

        OutlinedButton(
          onClick = {
            showPalettesPanel = !showPalettesPanel
            if (showPalettesPanel) showAccessoriesPanel = false
          },
          modifier = Modifier
            .weight(1f)
            .testTag("toggle_rooster_palettes_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Palette,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Aura ($unlockedPalettesCount/${allRoosterPlumagePalettes.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }

        FilledTonalButton(
          onClick = onOpenBiometricsClick,
          modifier = Modifier
            .weight(1.05f)
            .testTag("companion_log_biometrics_button"),
          shape = RoundedCornerShape(14.dp),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Straighten,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Log Check-In",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      AnimatedVisibility(visible = showAccessoriesPanel) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Milestone Gift Accessories (Tap unlocked gift to equip/unequip on your 2D Rooster)",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          allChickenAccessoryGifts.forEach { gift ->
            val isUnlocked = activeProfile.isAccessoryUnlocked(gift)
            val isEquipped = equippedIds.contains(gift.id)
            AccessoryGiftCompactRow(
              gift = gift,
              isUnlocked = isUnlocked,
              isEquipped = isEquipped,
              onToggleEquip = {
                if (isUnlocked) {
                  AthleteProfileStore.toggleAccessory(gift.id, context)
                }
              },
            )
          }
        }
      }

      AnimatedVisibility(visible = showPalettesPanel) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Unlockable 2D Plumage Palettes & Elemental Auras",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          allRoosterPlumagePalettes.forEach { palette ->
            val isUnlocked = activeProfile.isPaletteUnlocked(palette)
            val isSelected = activeProfile.selectedPlumagePalette == palette.id
            PlumagePaletteCompactRow(
              palette = palette,
              isUnlocked = isUnlocked,
              isSelected = isSelected,
              onSelect = {
                if (isUnlocked) {
                  AthleteProfileStore.selectPlumagePalette(palette.id, context)
                }
              },
            )
          }
        }
      }
    }
  }
}

/**
 * v2 Weekly Biometric Trend Comparison & Quest Board Card for TodayScreen:
 * Combines:
 * 1. Weekly Waist & Weight Check-In Reminder with Trend Delta Comparison (Weight, Waist, RFM%)
 * 2. Weekly Quest Board & Streak Freeze Shield rewards
 */
@Composable
fun WeeklyQuestAndBiometricTrendCard(
  activeProfile: AthleteProfile,
  onOpenBiometricsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val weightDelta = activeProfile.weightDeltaFromPreviousKg
  val waistDelta = activeProfile.waistDeltaFromPreviousCm
  val rfmDelta = activeProfile.rfmDeltaFromPreviousPct

  Card(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("weekly_quest_and_trend_card"),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      // 1. Weekly Waist & Weight Check-In & Trend Delta Banner
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (activeProfile.isWeeklyCheckInDue) {
          MaterialTheme.colorScheme.tertiaryContainer
        } else {
          MaterialTheme.colorScheme.surfaceContainerHigh
        },
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f),
            ) {
              Icon(
                imageVector = Icons.Filled.TrendingDown,
                contentDescription = null,
                tint = GoogleGreen40,
                modifier = Modifier.size(20.dp),
              )
              Column {
                Text(
                  text = if (activeProfile.isWeeklyCheckInDue) {
                    "WEEKLY 3-BIOMETRIC CHECK-IN DUE"
                  } else {
                    "3-BIOMETRIC TREND COMPARISON"
                  },
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoogleGreen40,
                )
                Text(
                  text = "Weight, Waist & RFM% Delta vs. Previous Log",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            FilledTonalButton(
              onClick = onOpenBiometricsClick,
              shape = PillShape,
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Text(
                text = "Update 3-Metrics",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
              )
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            TrendDeltaPill(
              label = "WEIGHT",
              current = String.format(Locale.US, "%.1f kg", activeProfile.weightKg),
              delta = formatSignedDelta(weightDelta, "kg"),
              modifier = Modifier.weight(1f),
            )
            TrendDeltaPill(
              label = "WAIST",
              current = String.format(Locale.US, "%.1f cm", activeProfile.waistCm),
              delta = formatSignedDelta(waistDelta, "cm"),
              modifier = Modifier.weight(1f),
            )
            TrendDeltaPill(
              label = "RFM FAT",
              current = String.format(Locale.US, "%.1f%%", activeProfile.rfmBodyFatPct),
              delta = formatSignedDelta(rfmDelta, "%"),
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      // 2. Weekly Quest Board & Streak Freeze Shield Header
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
            imageVector = Icons.Filled.EmojiEvents,
            contentDescription = null,
            tint = Color(0xFFFF8F00),
            modifier = Modifier.size(20.dp),
          )
          Text(
            text = "Weekly Quest Board",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
          )
        }

        Surface(
          shape = PillShape,
          color = Color(0xFFE65100).copy(alpha = 0.14f),
        ) {
          Text(
            text = "🛡️ ${activeProfile.streakFreezeShields} Streak Shield${if (activeProfile.streakFreezeShields == 1) "" else "s"} Active",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE65100),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      activeProfile.weeklyQuests.forEach { quest ->
        WeeklyQuestRow(
          quest = quest,
          onClaim = {
            AthleteProfileStore.claimWeeklyQuest(quest, context)
          },
        )
      }
    }
  }
}

private fun formatSignedDelta(delta: Float, unit: String): String {
  return when {
    abs(delta) < 0.05f -> "0.0 $unit (Steady)"
    delta > 0f -> String.format(Locale.US, "+%.1f %s", delta, unit)
    else -> String.format(Locale.US, "%.1f %s", delta, unit)
  }
}

@Composable
private fun TrendDeltaPill(
  label: String,
  current: String,
  delta: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLowest,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
      )
      Text(
        text = current,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
      )
      Text(
        text = delta,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = GoogleGreen40,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun WeeklyQuestRow(
  quest: WeeklyQuest,
  onClaim: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = quest.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${quest.currentCount}/${quest.targetCount}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (quest.isCompleted) GoogleGreen40 else MaterialTheme.colorScheme.primary,
          )
        }
        Text(
          text = quest.subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        LinearProgressIndicator(
          progress = { quest.progressFraction.coerceIn(0.05f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(PillShape),
          color = if (quest.isCompleted) GoogleGreen40 else MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
      }

      if (quest.isClaimed) {
        Surface(
          shape = PillShape,
          color = GoogleGreen40.copy(alpha = 0.16f),
        ) {
          Text(
            text = "CLAIMED",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = GoogleGreen40,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          )
        }
      } else if (quest.isCompleted) {
        Button(
          onClick = onClaim,
          shape = PillShape,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = GoogleGreen40,
            contentColor = Color.White,
          ),
        ) {
          Text(
            text = "Claim +${quest.xpReward} XP",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}

/**
 * Settings-Only 12-Stage 2D Evolution Previewer Card:
 * Allows scrubbing 0% -> 100% across all 12 gradual stages, 4 evolving dojo backgrounds,
 * 4 interactive rooster actions, and 4 plumage palettes exclusively inside Settings.
 */
@Composable
fun SettingsChickenStagePreviewCard(
  activeProfile: AthleteProfile,
  modifier: Modifier = Modifier,
) {
  var previewProgress by remember { mutableFloatStateOf(activeProfile.goalJourneyProgress.coerceIn(0f, 1f)) }
  var previewActionState by remember { mutableStateOf(RoosterActionState.IDLE) }
  var previewPaletteId by remember { mutableStateOf(activeProfile.selectedPlumagePalette) }
  var showPreviewGifts by remember { mutableStateOf(false) }

  val previewStage = ChickenEvolutionStage.fromProgress(previewProgress)
  val previewMicroLevel = ((previewProgress * 99f).toInt() + 1).coerceIn(1, 100)

  val infiniteTransition = rememberInfiniteTransition(label = "settings_chicken_2d_preview")
  val breathPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart,
    ),
    label = "settings_breath",
  )

  val previewEquippedAccessories = remember(previewProgress, showPreviewGifts, activeProfile.equippedAccessories) {
    if (!showPreviewGifts) {
      emptySet()
    } else {
      val set = mutableSetOf<String>()
      set.addAll(activeProfile.equippedAccessories)
      if (previewProgress >= 0.15f) set.add("headband_crimson")
      if (previewProgress >= 0.30f) set.add("wrist_bracers")
      if (previewProgress >= 0.50f) set.add("golden_dumbbells")
      if (previewProgress >= 0.70f) set.add("spartan_pauldrons")
      if (previewProgress >= 0.85f) set.add("champion_belt")
      if (previewProgress >= 0.95f) set.add("warrior_cape_crown")
      set
    }
  }

  ElevatedCard(
    modifier = modifier
      .fillMaxWidth()
      .testTag("settings_chicken_preview_card"),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "2D Warrior Rooster & Dojo Stage Previewer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text = "Inspect all 12 stages, 4 evolving 2D dojo tiers, actions & plumage auras",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        TextButton(
          onClick = {
            previewProgress = activeProfile.goalJourneyProgress.coerceIn(0f, 1f)
            previewPaletteId = activeProfile.selectedPlumagePalette
            previewActionState = RoosterActionState.IDLE
          },
        ) {
          Text("Reset to Live", fontWeight = FontWeight.Bold)
        }
      }

      // Stage Slider (0% to 100%)
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "${previewStage.title} • ${previewStage.dojoTierName}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${(previewProgress * 100).toInt()}% (Lv.$previewMicroLevel/100)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }

        Slider(
          value = previewProgress,
          onValueChange = { previewProgress = it },
          valueRange = 0f..1f,
          modifier = Modifier.testTag("settings_growth_preview_slider"),
        )
      }

      // Horizontal Scrollable 12-Stage Selector Chips + Gifts Toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        FilterChip(
          selected = showPreviewGifts,
          onClick = { showPreviewGifts = !showPreviewGifts },
          label = {
            Text(
              text = if (showPreviewGifts) "Gifts: ON" else "Gifts: OFF",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
            )
          },
        )
        ChickenEvolutionStage.entries.forEach { stage ->
          val selected = previewStage == stage
          FilterChip(
            selected = selected,
            onClick = {
              previewProgress = ((stage.minProgress + stage.maxProgress) * 0.5f).coerceIn(0f, 1f)
            },
            label = {
              Text(
                text = "${stage.stageNumber}. ${stage.shortLabel}",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                softWrap = false,
              )
            },
          )
        }
      }

      // Preview Action & Palette Selector Strip
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        RoosterActionState.entries.forEach { action ->
          FilterChip(
            selected = previewActionState == action,
            onClick = { previewActionState = action },
            label = {
              Text(
                text = action.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                softWrap = false,
              )
            },
          )
        }
        allRoosterPlumagePalettes.forEach { palette ->
          FilterChip(
            selected = previewPaletteId == palette.id,
            onClick = { previewPaletteId = palette.id },
            label = {
              Text(
                text = palette.name,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                softWrap = false,
              )
            },
          )
        }
      }

      // Crisp 2D Illustrated Viewport
      IllustratedChicken2DViewport(
        growthProgress = previewProgress,
        stage = previewStage,
        breathPhase = breathPhase,
        actionState = previewActionState,
        paletteId = previewPaletteId,
        equippedAccessories = previewEquippedAccessories,
        activeProfile = activeProfile,
        onTapViewport = {
          val nextIdx = (previewActionState.ordinal + 1) % RoosterActionState.entries.size
          previewActionState = RoosterActionState.entries[nextIdx]
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(315.dp),
      )

      Text(
        text = previewStage.subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun AccessoryGiftCompactRow(
  gift: ChickenAccessoryGift,
  isUnlocked: Boolean,
  isEquipped: Boolean,
  onToggleEquip: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(enabled = isUnlocked) { onToggleEquip() },
    shape = RoundedCornerShape(14.dp),
    color = when {
      isEquipped -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
      isUnlocked -> MaterialTheme.colorScheme.surfaceContainer
      else -> MaterialTheme.colorScheme.surfaceContainerLowest
    },
    border = BorderStroke(
      width = 1.dp,
      color = if (isEquipped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = when {
          isEquipped -> Icons.Filled.CheckCircle
          isUnlocked -> Icons.Filled.WorkspacePremium
          else -> Icons.Filled.Lock
        },
        contentDescription = null,
        tint = when {
          isEquipped -> MaterialTheme.colorScheme.primary
          isUnlocked -> Color(0xFFFF8F00)
          else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier.size(20.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = gift.title,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = "${gift.milestoneLabel} • ${gift.description}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = when {
          isEquipped -> "EQUIPPED"
          isUnlocked -> "EQUIP"
          else -> "LOCKED"
        },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = when {
          isEquipped -> MaterialTheme.colorScheme.primary
          isUnlocked -> Color(0xFFE65100)
          else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
      )
    }
  }
}

@Composable
private fun PlumagePaletteCompactRow(
  palette: RoosterPlumagePalette,
  isUnlocked: Boolean,
  isSelected: Boolean,
  onSelect: () -> Unit,
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable(enabled = isUnlocked) { onSelect() },
    shape = RoundedCornerShape(14.dp),
    color = when {
      isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
      isUnlocked -> MaterialTheme.colorScheme.surfaceContainer
      else -> MaterialTheme.colorScheme.surfaceContainerLowest
    },
    border = BorderStroke(
      width = 1.dp,
      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Icon(
        imageVector = when {
          isSelected -> Icons.Filled.CheckCircle
          isUnlocked -> Icons.Filled.Palette
          else -> Icons.Filled.Lock
        },
        contentDescription = null,
        tint = when {
          isSelected -> MaterialTheme.colorScheme.primary
          isUnlocked -> Color(0xFFFF8F00)
          else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
        modifier = Modifier.size(20.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "${palette.name} • ${palette.auraName}",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = palette.unlockRequirement,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = when {
          isSelected -> "ACTIVE"
          isUnlocked -> "SELECT"
          else -> "LOCKED"
        },
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = when {
          isSelected -> MaterialTheme.colorScheme.primary
          isUnlocked -> Color(0xFFE65100)
          else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        },
      )
    }
  }
}

// ================================================================================================
// MASTER-CRAFTED 2D VECTOR CHARACTER & EVOLVING DOJO ENGINE
// ================================================================================================

@Composable
fun IllustratedChicken2DViewport(
  growthProgress: Float,
  stage: ChickenEvolutionStage,
  breathPhase: Float,
  actionState: RoosterActionState = RoosterActionState.IDLE,
  paletteId: String = "golden_amber",
  equippedAccessories: Set<String>,
  activeProfile: AthleteProfile,
  onTapViewport: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val animatedGrowth by animateFloatAsState(
    targetValue = growthProgress.coerceIn(0f, 1f),
    animationSpec = tween(400, easing = FastOutSlowInEasing),
    label = "smooth_2d_growth",
  )
  val isFlexing = actionState == RoosterActionState.POWER_FLEX || actionState == RoosterActionState.LIFT_IRON
  val flexFactor by animateFloatAsState(
    targetValue = when (actionState) {
      RoosterActionState.POWER_FLEX -> 1f
      RoosterActionState.LIFT_IRON -> 0.5f + 0.5f * ((sin(breathPhase * 2f) + 1f) * 0.5f)
      RoosterActionState.PROTEIN_FEAST -> 0.25f
      RoosterActionState.IDLE -> 0f
    },
    animationSpec = tween(220, easing = FastOutSlowInEasing),
    label = "flex_pose_factor",
  )

  val bgColors = when (stage.dojoTierNumber) {
    1 -> listOf(Color(0xFF1F1610), Color(0xFF2D2016), Color(0xFF3E2C1E)) // Warm Sanctuary Barn
    2 -> listOf(Color(0xFF122229), Color(0xFF1B333D), Color(0xFF24434F)) // Timber Training Courtyard
    3 -> listOf(Color(0xFF111827), Color(0xFF1E293B), Color(0xFF283548)) // Iron Foundry Gym
    else -> listOf(Color(0xFF1A0F1E), Color(0xFF2B162F), Color(0xFF3A1D3D)) // Grand Champion Colosseum
  }

  val themePalette = resolve2DPlumageTheme(
    paletteId = paletteId,
    muscle = ((animatedGrowth - 0.50f) / 0.50f).coerceIn(0f, 1f),
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(24.dp))
      .background(Brush.verticalGradient(colors = bgColors))
      .clickable { onTapViewport() },
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cx = size.width * 0.5f
      val cy = size.height * 0.53f
      val unit = min(size.width, size.height) * 0.36f

      // 1. Evolving 2D Vector Dojo Background Architecture (Tiers 1..4)
      draw2DEvolvingDojoBackdrop(
        dojoTier = stage.dojoTierNumber,
        cx = cx,
        cy = cy,
        u = unit,
        breathPhase = breathPhase,
      )

      // 2. Elemental Aura Glow & Rising Particles
      val auraRadius = unit * (1.15f + animatedGrowth * 0.30f + flexFactor * 0.14f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            themePalette.auraInner.copy(alpha = 0.28f + flexFactor * 0.12f),
            themePalette.auraOuter.copy(alpha = 0.10f),
            Color.Transparent,
          ),
          center = Offset(cx, cy - unit * 0.06f),
          radius = auraRadius,
        ),
        center = Offset(cx, cy - unit * 0.06f),
        radius = auraRadius,
      )

      // Rising Elemental Aura Sparks
      for (i in 0 until 6) {
        val phaseShift = (breathPhase + i * 1.05f) % (2f * PI.toFloat())
        val riseFrac = phaseShift / (2f * PI.toFloat())
        val px = cx + cos(i * 1.7f + riseFrac * 2f) * unit * (0.55f + (i % 2) * 0.22f)
        val py = (cy + unit * 0.55f) - riseFrac * unit * 1.35f
        val sparkR = unit * (0.018f + (1f - riseFrac) * 0.016f)
        drawCircle(
          color = themePalette.particleColor.copy(alpha = (1f - riseFrac) * 0.65f),
          radius = sparkR,
          center = Offset(px, py),
        )
      }

      // 3. 2D Sanctuary Dojo Pedestal Platform
      draw2DPedestalPlatform(
        cx = cx,
        cy = cy + unit * 0.76f,
        rx = unit * 0.86f,
        ry = unit * 0.18f,
        growthProgress = animatedGrowth,
        accentColor = themePalette.auraInner,
      )

      // 4. Character Renderer (Egg -> Chick/Cockerel -> Muscular Warrior Rooster)
      if (animatedGrowth < 0.28f) {
        draw2DEggAndHatchingStages(
          cx = cx,
          cy = cy + unit * 0.14f,
          u = unit,
          breathPhase = breathPhase,
          flexFactor = flexFactor,
          growthProgress = animatedGrowth,
          stage = stage,
        )
      } else if (animatedGrowth < 0.54f) {
        draw2DChickToCockerelStages(
          cx = cx,
          cy = cy + unit * 0.15f,
          u = unit,
          breathPhase = breathPhase,
          flexFactor = flexFactor,
          growthProgress = animatedGrowth,
          equipped = equippedAccessories,
          themePalette = themePalette,
        )
      } else {
        draw2DMuscularWarriorRooster(
          cx = cx,
          cy = cy + unit * 0.14f,
          u = unit,
          breathPhase = breathPhase,
          flexFactor = flexFactor,
          growthProgress = animatedGrowth,
          equipped = equippedAccessories,
          themePalette = themePalette,
          isLiftingIron = actionState == RoosterActionState.LIFT_IRON,
        )
      }

      // 5. Interactive Action Props (Lift Iron Motion Arcs OR Protein Feast Bowl)
      if (actionState == RoosterActionState.LIFT_IRON) {
        draw2DLiftIronEffects(
          cx = cx,
          cy = cy,
          u = unit,
          breathPhase = breathPhase,
          accentColor = themePalette.auraInner,
        )
      } else if (actionState == RoosterActionState.PROTEIN_FEAST) {
        draw2DProteinFeastBowl(
          cx = cx,
          cy = cy + unit * 0.62f,
          u = unit,
          breathPhase = breathPhase,
        )
      }
    }

    // Top HUD Bar: Action Mode + Dojo Tier & Stage Badge
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = RoundedCornerShape(50),
        color = Color.Black.copy(alpha = 0.48f),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.TouchApp,
            contentDescription = null,
            tint = Color(0xFFFFCA28),
            modifier = Modifier.size(13.dp),
          )
          Text(
            text = actionState.badgeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFE65100).copy(alpha = 0.92f),
      ) {
        Text(
          text = stage.badge,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          fontWeight = FontWeight.ExtraBold,
          maxLines = 1,
          softWrap = false,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
      }
    }

    // Bottom HUD: Weight -> Goal | Waist -> Goal | RFM%
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      ViewportMetricPill(
        label = "WEIGHT → GOAL",
        value = "${String.format(Locale.US, "%.1f", activeProfile.weightKg)} → ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWeightKg)} kg",
        modifier = Modifier.weight(1f),
      )
      ViewportMetricPill(
        label = "WAIST → GOAL",
        value = "${String.format(Locale.US, "%.1f", activeProfile.waistCm)} → ${String.format(Locale.US, "%.1f", activeProfile.autoGoalWaistCm)} cm",
        modifier = Modifier.weight(1f),
      )
      ViewportMetricPill(
        label = "EST. RFM",
        value = "${String.format(Locale.US, "%.1f", activeProfile.rfmBodyFatPct)}%",
        modifier = Modifier.weight(0.68f),
      )
    }
  }
}

/**
 * Draws the 4 Evolving 2D Vector Dojo Backgrounds behind the character:
 * - Tier 1 (Stages 1-4): Sunlit Sanctuary Barn (wooden rafters & warm golden sunbeams)
 * - Tier 2 (Stages 5-7): Timber Training Courtyard (dojo shoji screen panels & hanging lanterns)
 * - Tier 3 (Stages 8-10): Iron Foundry Gym (steel rafters, dumbbell rack silhouettes & iron plates)
 * - Tier 4 (Stages 11-12): Grand Champion Colosseum (Roman arches, championship banners & flaming braziers)
 */
private fun DrawScope.draw2DEvolvingDojoBackdrop(
  dojoTier: Int,
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
) {
  val w = size.width
  val h = size.height
  when (dojoTier) {
    1 -> {
      // Tier 1: Sunlit Sanctuary Barn — Angled warm sunbeams + barn roof rafters
      val rayPath = Path().apply {
        moveTo(cx - u * 0.45f, 0f)
        lineTo(cx + u * 0.45f, 0f)
        lineTo(cx + u * 1.15f, h)
        lineTo(cx - u * 1.15f, h)
        close()
      }
      drawPath(
        path = rayPath,
        brush = Brush.verticalGradient(
          colors = listOf(Color(0xFFFFD54F).copy(alpha = 0.14f), Color.Transparent),
        ),
      )
      // Barn timber beams
      drawLine(
        color = Color(0xFF5D4037).copy(alpha = 0.45f),
        start = Offset(0f, h * 0.24f),
        end = Offset(w * 0.5f, h * 0.06f),
        strokeWidth = 10f,
      )
      drawLine(
        color = Color(0xFF5D4037).copy(alpha = 0.45f),
        start = Offset(w, h * 0.24f),
        end = Offset(w * 0.5f, h * 0.06f),
        strokeWidth = 10f,
      )
    }
    2 -> {
      // Tier 2: Timber Training Courtyard — Shoji lattice panels & warm hanging dojo lanterns
      listOf(-1f, 1f).forEach { side ->
        val panelX = cx + side * u * 1.02f
        drawRoundRect(
          color = Color(0xFF264653).copy(alpha = 0.45f),
          topLeft = Offset(panelX - u * 0.22f, h * 0.16f),
          size = Size(u * 0.44f, h * 0.52f),
          cornerRadius = CornerRadius(8f, 8f),
        )
        drawRoundRect(
          color = Color(0xFFE9C46A).copy(alpha = 0.28f),
          topLeft = Offset(panelX - u * 0.22f, h * 0.16f),
          size = Size(u * 0.44f, h * 0.52f),
          cornerRadius = CornerRadius(8f, 8f),
          style = Stroke(width = 2.5f),
        )
        // Glowing dojo lantern
        val lanternY = h * 0.22f + sin(breathPhase + side) * 4f
        drawCircle(
          color = Color(0xFFFF9F1C).copy(alpha = 0.55f),
          radius = u * 0.09f,
          center = Offset(cx + side * u * 0.85f, lanternY),
        )
      }
    }
    3 -> {
      // Tier 3: Iron Foundry Gym — Industrial steel columns & side dumbbell rack silhouettes
      listOf(-1f, 1f).forEach { side ->
        val rackX = cx + side * u * 1.05f
        drawLine(
          color = Color(0xFF475569).copy(alpha = 0.45f),
          start = Offset(rackX, h * 0.18f),
          end = Offset(rackX, h * 0.75f),
          strokeWidth = 12f,
        )
        // Barbell plate silhouettes on side racks
        for (idx in 0..2) {
          val py = h * (0.30f + idx * 0.14f)
          drawRoundRect(
            color = Color(0xFF64748B).copy(alpha = 0.42f),
            topLeft = Offset(rackX - u * 0.16f, py),
            size = Size(u * 0.32f, u * 0.055f),
            cornerRadius = CornerRadius(6f, 6f),
          )
        }
      }
    }
    else -> {
      // Tier 4: Grand Champion Colosseum — Golden banners & twin flaming braziers
      listOf(-1f, 1f).forEach { side ->
        val bx = cx + side * u * 0.96f
        val by = cy + u * 0.36f
        // Brazier pillar
        drawRoundRect(
          color = Color(0xFF78350F).copy(alpha = 0.65f),
          topLeft = Offset(bx - u * 0.08f, by),
          size = Size(u * 0.16f, u * 0.36f),
          cornerRadius = CornerRadius(6f, 6f),
        )
        // Animated flame
        val flameH = u * (0.22f + 0.04f * sin(breathPhase * 2f + side))
        val flamePath = Path().apply {
          moveTo(bx - u * 0.10f, by)
          quadraticTo(bx, by - flameH * 1.3f, bx + u * 0.10f, by)
          close()
        }
        drawPath(
          path = flamePath,
          brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFEE58), Color(0xFFFF6D00)),
          ),
        )
      }
    }
  }
}

private fun DrawScope.draw2DLiftIronEffects(
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
  accentColor: Color,
) {
  val liftOffset = sin(breathPhase * 2f) * u * 0.12f
  listOf(-1f, 1f).forEach { side ->
    val arcX = cx + side * u * 0.66f
    val arcY = cy - u * 0.02f + liftOffset
    val dbHalf = u * 0.13f
    drawLine(
      color = InkOutline,
      start = Offset(arcX - dbHalf, arcY),
      end = Offset(arcX + dbHalf, arcY),
      strokeWidth = 6f,
      cap = StrokeCap.Round,
    )
    listOf(-1f, 1f).forEach { pSide ->
      val px = arcX + pSide * dbHalf
      drawRoundRect(
        color = accentColor,
        topLeft = Offset(px - u * 0.028f, arcY - u * 0.07f),
        size = Size(u * 0.056f, u * 0.14f),
        cornerRadius = CornerRadius(6f, 6f),
      )
      drawRoundRect(
        color = InkOutline,
        topLeft = Offset(px - u * 0.028f, arcY - u * 0.07f),
        size = Size(u * 0.056f, u * 0.14f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 3f),
      )
    }
  }
}

private fun DrawScope.draw2DProteinFeastBowl(
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
) {
  val bowlW = u * 0.42f
  val bowlH = u * 0.18f
  val bowlX = cx + u * 0.42f
  val bowlY = cy

  // Golden grain heap inside bowl
  drawOval(
    color = Color(0xFFFFD54F),
    topLeft = Offset(bowlX - bowlW * 0.46f, bowlY - bowlH * 0.65f),
    size = Size(bowlW * 0.92f, bowlH * 0.85f),
  )

  // Bowl body
  val bowlPath = Path().apply {
    moveTo(bowlX - bowlW * 0.5f, bowlY - bowlH * 0.25f)
    lineTo(bowlX + bowlW * 0.5f, bowlY - bowlH * 0.25f)
    quadraticTo(bowlX + bowlW * 0.42f, bowlY + bowlH * 0.65f, bowlX, bowlY + bowlH * 0.65f)
    quadraticTo(bowlX - bowlW * 0.42f, bowlY + bowlH * 0.65f, bowlX - bowlW * 0.5f, bowlY - bowlH * 0.25f)
    close()
  }
  drawPath(bowlPath, color = Color(0xFFD84315))
  drawPath(bowlPath, color = InkOutline, style = Stroke(width = 3.2f, join = StrokeJoin.Round))

  // Rising golden nourishment sparks
  for (i in 0..2) {
    val rise = ((breathPhase + i * 1.8f) % (2f * PI.toFloat())) / (2f * PI.toFloat())
    drawCircle(
      color = Color(0xFF4ADE80).copy(alpha = 1f - rise),
      radius = u * 0.024f,
      center = Offset(bowlX + (i - 1) * u * 0.08f, bowlY - bowlH * 0.5f - rise * u * 0.35f),
    )
  }
}

@Composable
private fun ViewportMetricPill(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    color = Color.Black.copy(alpha = 0.52f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 9.sp,
        color = Color.White.copy(alpha = 0.72f),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 11.sp,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

private val InkOutline = Color(0xFF2B170E)

private fun lerp2DColor(a: Color, b: Color, t: Float): Color {
  val f = t.coerceIn(0f, 1f)
  return Color(
    red = a.red + (b.red - a.red) * f,
    green = a.green + (b.green - a.green) * f,
    blue = a.blue + (b.blue - a.blue) * f,
    alpha = 1f,
  )
}

private fun DrawScope.draw2DPedestalPlatform(
  cx: Float,
  cy: Float,
  rx: Float,
  ry: Float,
  growthProgress: Float,
  accentColor: Color,
) {
  drawOval(
    color = Color.Black.copy(alpha = 0.42f),
    topLeft = Offset(cx - rx * 1.05f, cy - ry * 0.75f),
    size = Size(rx * 2.10f, ry * 1.95f),
  )
  drawOval(
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFF33475B), Color(0xFF1B2838)),
      startY = cy - ry,
      endY = cy + ry,
    ),
    topLeft = Offset(cx - rx, cy - ry),
    size = Size(rx * 2f, ry * 2f),
  )
  drawOval(
    color = accentColor.copy(alpha = 0.55f + growthProgress * 0.35f),
    topLeft = Offset(cx - rx * 0.90f, cy - ry * 0.82f),
    size = Size(rx * 1.80f, ry * 1.64f),
    style = Stroke(width = 2.5f),
  )
}

/**
 * PHASE A — STAGES 1 TO 4 (0%..28%):
 * Clean 2D Vector Sanctuary Egg & Hatching Chick in a Woven Golden Nest.
 */
private fun DrawScope.draw2DEggAndHatchingStages(
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
  flexFactor: Float,
  growthProgress: Float,
  stage: ChickenEvolutionStage,
) {
  val strokeW = (u * 0.032f).coerceAtLeast(3f)
  val bounce = sin(breathPhase) * u * 0.018f
  val eggW = u * 0.74f
  val eggH = u * 0.96f
  val eggCy = cy - u * 0.04f + bounce

  val nestCy = cy + u * 0.48f
  val nestRx = u * 0.62f
  val nestRy = u * 0.16f
  drawOval(
    color = Color(0xFF6D451F),
    topLeft = Offset(cx - nestRx, nestCy - nestRy),
    size = Size(nestRx * 2f, nestRy * 2f),
  )

  val isHatchingStage = stage == ChickenEvolutionStage.STAGE_04_HATCHING_CROWN || growthProgress >= 0.20f

  if (!isHatchingStage) {
    val eggPath = Path().apply {
      val topY = eggCy - eggH * 0.54f
      val botY = eggCy + eggH * 0.46f
      moveTo(cx, topY)
      cubicTo(cx + eggW * 0.38f, topY, cx + eggW * 0.54f, eggCy - eggH * 0.04f, cx + eggW * 0.48f, eggCy + eggH * 0.18f)
      cubicTo(cx + eggW * 0.42f, eggCy + eggH * 0.38f, cx + eggW * 0.22f, botY, cx, botY)
      cubicTo(cx - eggW * 0.22f, botY, cx - eggW * 0.42f, eggCy + eggH * 0.38f, cx - eggW * 0.48f, eggCy + eggH * 0.18f)
      cubicTo(cx - eggW * 0.54f, eggCy - eggH * 0.04f, cx - eggW * 0.38f, topY, cx, topY)
      close()
    }

    drawPath(
      path = eggPath,
      brush = Brush.radialGradient(
        colors = listOf(
          Color(0xFFFFFDF9),
          Color(0xFFF7E6C8),
          Color(0xFFE0C090),
          Color(0xFFC29560),
        ),
        center = Offset(cx - eggW * 0.16f, eggCy - eggH * 0.18f),
        radius = eggH * 0.78f,
      ),
    )

    drawOval(
      color = Color.White.copy(alpha = 0.65f),
      topLeft = Offset(cx - eggW * 0.26f, eggCy - eggH * 0.36f),
      size = Size(eggW * 0.16f, eggH * 0.24f),
    )

    if (growthProgress >= 0.06f) {
      val crackScale = ((growthProgress - 0.06f) / 0.14f).coerceIn(0.25f, 1f)
      val crackPath = Path().apply {
        val cyCrack = eggCy - eggH * 0.08f
        moveTo(cx - eggW * 0.28f * crackScale, cyCrack + u * 0.02f)
        lineTo(cx - eggW * 0.12f * crackScale, cyCrack - u * 0.06f)
        lineTo(cx + eggW * 0.02f, cyCrack + u * 0.03f)
        lineTo(cx + eggW * 0.15f * crackScale, cyCrack - u * 0.05f)
        lineTo(cx + eggW * 0.28f * crackScale, cyCrack + u * 0.02f)
      }
      drawPath(
        path = crackPath,
        color = InkOutline,
        style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round),
      )
    }

    drawPath(
      path = eggPath,
      color = InkOutline,
      style = Stroke(width = strokeW, join = StrokeJoin.Round),
    )
  } else {
    val chickY = eggCy - u * 0.08f - flexFactor * u * 0.04f
    val chickR = u * 0.29f

    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF59D), Color(0xFFFFCA28), Color(0xFFF57F17)),
        center = Offset(cx - chickR * 0.2f, chickY - chickR * 0.2f),
        radius = chickR * 1.3f,
      ),
      radius = chickR,
      center = Offset(cx, chickY),
    )
    drawCircle(
      color = InkOutline,
      radius = chickR,
      center = Offset(cx, chickY),
      style = Stroke(width = strokeW),
    )

    listOf(-1f, 1f).forEach { side ->
      val wingOval = Path().apply {
        moveTo(cx + side * chickR * 0.82f, chickY + u * 0.02f)
        quadraticTo(
          cx + side * (chickR * 1.32f),
          chickY - u * (0.06f + flexFactor * 0.08f),
          cx + side * (chickR * 1.05f),
          chickY + u * 0.16f,
        )
        close()
      }
      drawPath(wingOval, color = Color(0xFFFFB300))
      drawPath(wingOval, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
    }

    listOf(-1f, 1f).forEach { side ->
      val ex = cx + side * chickR * 0.38f
      val ey = chickY - chickR * 0.08f
      drawCircle(color = InkOutline, radius = u * 0.042f, center = Offset(ex, ey))
      drawCircle(color = Color.White, radius = u * 0.015f, center = Offset(ex - u * 0.012f, ey - u * 0.012f))
      drawOval(
        color = Color(0xFFFF7043).copy(alpha = 0.45f),
        topLeft = Offset(ex + side * u * 0.01f - u * 0.035f, ey + u * 0.035f),
        size = Size(u * 0.07f, u * 0.04f),
      )
    }

    val beakPath = Path().apply {
      moveTo(cx - u * 0.06f, chickY + u * 0.01f)
      lineTo(cx, chickY - u * 0.035f)
      lineTo(cx + u * 0.06f, chickY + u * 0.01f)
      lineTo(cx, chickY + u * 0.065f)
      close()
    }
    drawPath(beakPath, color = Color(0xFFFF8F00))
    drawPath(beakPath, color = InkOutline, style = Stroke(width = strokeW * 0.75f, join = StrokeJoin.Round))

    val capPath = Path().apply {
      val capY = chickY - chickR * 0.72f
      moveTo(cx - chickR * 0.85f, capY + u * 0.04f)
      cubicTo(
        cx - chickR * 0.55f, capY - u * 0.26f,
        cx + chickR * 0.55f, capY - u * 0.22f,
        cx + chickR * 0.82f, capY + u * 0.08f,
      )
      lineTo(cx + chickR * 0.42f, capY + u * 0.01f)
      lineTo(cx + chickR * 0.12f, capY + u * 0.08f)
      lineTo(cx - chickR * 0.22f, capY)
      lineTo(cx - chickR * 0.52f, capY + u * 0.07f)
      close()
    }
    drawPath(capPath, color = Color(0xFFF7E6C8))
    drawPath(capPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

    val bottomShell = Path().apply {
      val rimY = eggCy + u * 0.02f
      val botY = eggCy + eggH * 0.46f
      moveTo(cx - eggW * 0.48f, rimY)
      lineTo(cx - eggW * 0.28f, rimY + u * 0.07f)
      lineTo(cx - eggW * 0.08f, rimY - u * 0.02f)
      lineTo(cx + eggW * 0.12f, rimY + u * 0.07f)
      lineTo(cx + eggW * 0.30f, rimY - u * 0.01f)
      lineTo(cx + eggW * 0.48f, rimY + u * 0.04f)
      cubicTo(cx + eggW * 0.44f, eggCy + eggH * 0.36f, cx + eggW * 0.22f, botY, cx, botY)
      cubicTo(cx - eggW * 0.22f, botY, cx - eggW * 0.44f, eggCy + eggH * 0.36f, cx - eggW * 0.48f, rimY)
      close()
    }
    drawPath(
      path = bottomShell,
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF7E6C8), Color(0xFFD7B377)),
        startY = eggCy,
        endY = eggCy + eggH * 0.46f,
      ),
    )
    drawPath(bottomShell, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
  }

  val frontNestPath = Path().apply {
    moveTo(cx - nestRx * 1.02f, nestCy - nestRy * 0.2f)
    quadraticTo(cx, nestCy + nestRy * 0.95f, cx + nestRx * 1.02f, nestCy - nestRy * 0.2f)
    quadraticTo(cx, nestCy + nestRy * 1.55f, cx - nestRx * 1.02f, nestCy - nestRy * 0.2f)
    close()
  }
  drawPath(
    path = frontNestPath,
    brush = Brush.verticalGradient(
      colors = listOf(Color(0xFFD7A85B), Color(0xFF9C6B30)),
      startY = nestCy - nestRy,
      endY = nestCy + nestRy,
    ),
  )
  drawPath(frontNestPath, color = InkOutline, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))
}

/**
 * PHASE B — STAGES 5 TO 7 (28%..54%):
 * Clean, Expressive 2D Illustrated Chick -> Feathered Fledgling -> Young Cockerel.
 */
private fun DrawScope.draw2DChickToCockerelStages(
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
  flexFactor: Float,
  growthProgress: Float,
  equipped: Set<String>,
  themePalette: Resolved2DPlumageTheme,
) {
  val t = ((growthProgress - 0.28f) / 0.26f).coerceIn(0f, 1f)
  val s = u * (0.76f + t * 0.22f)
  val strokeW = (s * 0.038f).coerceAtLeast(3f)
  val bounce = sin(breathPhase) * s * 0.02f

  val plumageTop = themePalette.plumageTop
  val plumageMid = themePalette.plumageMid
  val plumageDeep = themePalette.plumageDeep
  val tailTeal = themePalette.tailPrimary

  val footY = cy + u * 0.56f
  val bodyCy = cy + s * 0.04f + bounce

  val tailCount = 2 + (t * 2f).toInt()
  for (i in 0 until tailCount) {
    val featherPath = Path().apply {
      val startX = cx - s * 0.30f
      val startY = bodyCy - s * 0.02f
      val tipX = cx - s * (0.55f + t * 0.28f + i * 0.06f)
      val tipY = bodyCy - s * (0.16f + t * 0.22f - i * 0.08f)
      moveTo(startX, startY)
      quadraticTo(
        startX - s * (0.20f + t * 0.16f),
        startY - s * (0.28f + t * 0.20f - i * 0.05f),
        tipX,
        tipY,
      )
      quadraticTo(
        startX - s * 0.14f,
        startY - s * 0.04f,
        startX,
        startY + s * 0.08f,
      )
      close()
    }
    drawPath(featherPath, color = if (i % 2 == 0) tailTeal else plumageDeep)
    drawPath(featherPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
  }

  listOf(-0.14f, 0.12f).forEach { xOff ->
    val hipX = cx + s * xOff
    val hipY = bodyCy + s * 0.26f
    val ankleX = hipX - s * 0.02f
    val ankleY = footY

    val thighPath = Path().apply {
      moveTo(hipX - s * 0.09f, hipY)
      quadraticTo(hipX - s * 0.08f, hipY + s * 0.16f, ankleX - s * 0.03f, hipY + s * 0.18f)
      lineTo(ankleX + s * 0.03f, hipY + s * 0.18f)
      quadraticTo(hipX + s * 0.09f, hipY + s * 0.16f, hipX + s * 0.09f, hipY)
      close()
    }
    drawPath(thighPath, color = plumageMid)
    drawPath(thighPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

    drawLine(
      color = InkOutline,
      start = Offset(ankleX, hipY + s * 0.17f),
      end = Offset(ankleX, ankleY),
      strokeWidth = strokeW * 1.8f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0xFFFF8F00),
      start = Offset(ankleX, hipY + s * 0.17f),
      end = Offset(ankleX, ankleY),
      strokeWidth = strokeW * 1.0f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0xFFFF8F00),
      start = Offset(ankleX - s * 0.07f, ankleY + s * 0.01f),
      end = Offset(ankleX + s * 0.12f, ankleY + s * 0.02f),
      strokeWidth = strokeW * 1.1f,
      cap = StrokeCap.Round,
    )
  }

  val headCx = cx + s * 0.18f
  val headCy = bodyCy - s * (0.38f + t * 0.10f)
  val headR = s * 0.23f

  val bodyPath = Path().apply {
    moveTo(headCx - headR * 0.72f, headCy + headR * 0.45f)
    quadraticTo(cx - s * 0.06f, bodyCy - s * 0.14f, cx - s * 0.38f, bodyCy - s * 0.04f)
    cubicTo(
      cx - s * 0.44f, bodyCy + s * 0.24f,
      cx - s * 0.18f, bodyCy + s * 0.38f,
      cx + s * 0.08f, bodyCy + s * 0.34f,
    )
    cubicTo(
      cx + s * (0.42f + flexFactor * 0.06f), bodyCy + s * 0.28f,
      cx + s * (0.48f + flexFactor * 0.08f), bodyCy - s * 0.10f,
      headCx + headR * 0.78f, headCy + headR * 0.55f,
    )
    close()
  }
  drawPath(
    path = bodyPath,
    brush = Brush.linearGradient(
      colors = listOf(plumageTop, plumageMid, plumageDeep),
      start = Offset(headCx, headCy),
      end = Offset(cx - s * 0.3f, bodyCy + s * 0.35f),
    ),
  )
  drawPath(bodyPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

  val combSize = 0.45f + t * 0.55f
  val combPath = Path().apply {
    val cBaseY = headCy - headR * 0.75f
    moveTo(headCx - headR * 0.45f, cBaseY + s * 0.04f)
    quadraticTo(headCx - headR * 0.50f, cBaseY - s * 0.12f * combSize, headCx - headR * 0.18f, cBaseY - s * 0.04f * combSize)
    quadraticTo(headCx, cBaseY - s * 0.18f * combSize, headCx + headR * 0.16f, cBaseY - s * 0.05f * combSize)
    quadraticTo(headCx + headR * 0.45f, cBaseY - s * 0.14f * combSize, headCx + headR * 0.40f, cBaseY + s * 0.06f)
    close()
  }
  drawPath(combPath, color = themePalette.combDeep)
  drawPath(combPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

  drawCircle(
    brush = Brush.radialGradient(
      colors = listOf(plumageTop, plumageMid),
      center = Offset(headCx - headR * 0.2f, headCy - headR * 0.2f),
      radius = headR * 1.3f,
    ),
    radius = headR,
    center = Offset(headCx, headCy),
  )
  drawCircle(
    color = InkOutline,
    radius = headR,
    center = Offset(headCx, headCy),
    style = Stroke(width = strokeW),
  )

  if (t > 0.2f) {
    val wattlePath = Path().apply {
      val wx = headCx + headR * 0.65f
      val wy = headCy + headR * 0.42f
      moveTo(wx, wy)
      cubicTo(
        wx + s * 0.06f, wy + s * 0.12f * t,
        wx - s * 0.05f, wy + s * 0.14f * t,
        wx - s * 0.02f, wy + s * 0.02f,
      )
      close()
    }
    drawPath(wattlePath, color = themePalette.combDeep)
    drawPath(wattlePath, color = InkOutline, style = Stroke(width = strokeW * 0.75f, join = StrokeJoin.Round))
  }

  val beakPath = Path().apply {
    val bx = headCx + headR * 0.82f
    val by = headCy - headR * 0.06f
    moveTo(bx, by - s * 0.055f)
    quadraticTo(bx + s * 0.16f, by - s * 0.04f, bx + s * 0.20f, by + s * 0.02f)
    lineTo(bx, by + s * 0.055f)
    close()
  }
  drawPath(beakPath, color = Color(0xFFFFB300))
  drawPath(beakPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

  val eyeX = headCx + headR * 0.26f
  val eyeY = headCy - headR * 0.12f
  drawCircle(color = InkOutline, radius = s * 0.042f, center = Offset(eyeX, eyeY))
  drawCircle(color = Color.White, radius = s * 0.015f, center = Offset(eyeX - s * 0.011f, eyeY - s * 0.012f))
  if (t > 0.35f) {
    drawLine(
      color = InkOutline,
      start = Offset(eyeX - s * 0.055f, eyeY - s * 0.052f),
      end = Offset(eyeX + s * 0.055f, eyeY - s * 0.032f),
      strokeWidth = strokeW * 0.85f,
      cap = StrokeCap.Round,
    )
  }

  val wingPath = Path().apply {
    val wx = cx + s * 0.06f
    val wy = bodyCy - s * 0.04f - flexFactor * s * 0.05f
    moveTo(wx, wy)
    cubicTo(
      wx - s * 0.34f, wy - s * 0.04f,
      wx - s * 0.38f, wy + s * 0.24f,
      wx - s * 0.14f, wy + s * 0.26f,
    )
    quadraticTo(wx + s * 0.10f, wy + s * 0.22f, wx, wy)
    close()
  }
  drawPath(wingPath, color = plumageMid)
  drawPath(wingPath, color = InkOutline, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))

  if (equipped.contains("headband_crimson")) {
    drawLine(
      color = Color(0xFFD32F2F),
      start = Offset(headCx - headR * 0.92f, headCy - headR * 0.42f),
      end = Offset(headCx + headR * 0.88f, headCy - headR * 0.34f),
      strokeWidth = s * 0.055f,
      cap = StrokeCap.Round,
    )
  }
}

/**
 * PHASE C — STAGES 8 TO 12 (54%..100%):
 * Master-Crafted 2D Illustrated Muscular Warrior Rooster!
 */
private fun DrawScope.draw2DMuscularWarriorRooster(
  cx: Float,
  cy: Float,
  u: Float,
  breathPhase: Float,
  flexFactor: Float,
  growthProgress: Float,
  equipped: Set<String>,
  themePalette: Resolved2DPlumageTheme,
  isLiftingIron: Boolean,
) {
  val muscle = ((growthProgress - 0.54f) / 0.46f).coerceIn(0f, 1f)
  val s = u * (0.86f + muscle * 0.18f)
  val strokeW = (s * 0.034f).coerceAtLeast(3.2f)
  val breath = sin(breathPhase) * s * 0.015f
  val flex = flexFactor * 0.18f

  val goldHighlight = themePalette.plumageTop
  val goldBase = themePalette.plumageMid
  val amberShade = themePalette.plumageDeep
  val bronzeDeep = Color(0xFF9A3412)
  val tailEmerald = themePalette.tailPrimary
  val tailObsidian = themePalette.tailSecondary
  val combRed = themePalette.combDeep
  val combBright = themePalette.combBright

  val footY = cy + u * 0.60f
  val waistY = cy + s * 0.14f
  val chestY = cy - s * 0.12f + breath
  val shoulderHalfW = s * (0.36f + muscle * 0.18f)
  val waistHalfW = s * (0.21f + muscle * 0.04f)

  if (equipped.contains("warrior_cape_crown")) {
    val capePath = Path().apply {
      moveTo(cx - shoulderHalfW * 0.85f, chestY - s * 0.10f)
      lineTo(cx + shoulderHalfW * 0.85f, chestY - s * 0.10f)
      cubicTo(
        cx + shoulderHalfW * 1.25f, waistY + s * 0.15f,
        cx + shoulderHalfW * 1.35f, footY - s * 0.05f,
        cx + shoulderHalfW * 1.05f, footY - s * 0.02f,
      )
      quadraticTo(cx, footY + s * 0.08f, cx - shoulderHalfW * 1.05f, footY - s * 0.02f)
      cubicTo(
        cx - shoulderHalfW * 1.35f, footY - s * 0.05f,
        cx - shoulderHalfW * 1.25f, waistY + s * 0.15f,
        cx - shoulderHalfW * 0.85f, chestY - s * 0.10f,
      )
      close()
    }
    drawPath(
      path = capePath,
      brush = Brush.verticalGradient(
        colors = listOf(Color(0xFFC62828), Color(0xFF7F0000)),
      ),
    )
    drawPath(capePath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
  }

  val plumeCount = 4 + (muscle * 3f).toInt()
  for (i in 0 until plumeCount) {
    val frac = i.toFloat() / (plumeCount - 1).coerceAtLeast(1)
    val plumePath = Path().apply {
      val baseX = cx + s * 0.12f
      val baseY = waistY - s * 0.04f
      val ctrlX = cx + shoulderHalfW * (0.95f + frac * 0.55f)
      val ctrlY = chestY - s * (0.45f + frac * 0.25f - i * 0.07f)
      val tipX = cx + shoulderHalfW * (1.28f + frac * 0.35f)
      val tipY = chestY + s * (0.02f + i * 0.09f)
      moveTo(baseX, baseY)
      quadraticTo(ctrlX, ctrlY, tipX, tipY)
      quadraticTo(ctrlX - s * 0.10f, ctrlY + s * 0.18f, baseX, baseY + s * 0.12f)
      close()
    }
    drawPath(
      path = plumePath,
      brush = Brush.linearGradient(
        colors = if (i % 2 == 0) listOf(tailEmerald, tailObsidian) else listOf(bronzeDeep, tailObsidian),
        start = Offset(cx, waistY),
        end = Offset(cx + shoulderHalfW * 1.4f, chestY),
      ),
    )
    drawPath(plumePath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
  }

  listOf(-1f, 1f).forEach { side ->
    val hipX = cx + side * (waistHalfW * 0.78f)
    val kneeX = cx + side * (waistHalfW * 0.92f)
    val kneeY = waistY + (footY - waistY) * 0.54f
    val ankleX = cx + side * (waistHalfW * 0.86f)
    val ankleY = footY - s * 0.04f
    val quadW = s * (0.14f + muscle * 0.05f)

    val shinPath = Path().apply {
      moveTo(kneeX - s * 0.038f, kneeY - s * 0.02f)
      lineTo(ankleX - s * 0.032f, ankleY)
      lineTo(ankleX + s * 0.032f, ankleY)
      lineTo(kneeX + s * 0.038f, kneeY - s * 0.02f)
      close()
    }
    drawPath(shinPath, color = Color(0xFFF57C00))
    drawPath(shinPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))

    listOf(-0.09f, 0f, 0.09f).forEach { toeOff ->
      val toeTipX = ankleX + side * s * 0.03f + s * toeOff
      val toeTipY = footY + s * 0.02f
      drawLine(
        color = InkOutline,
        start = Offset(ankleX, ankleY),
        end = Offset(toeTipX, toeTipY),
        strokeWidth = strokeW * 1.7f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = Color(0xFFFF9800),
        start = Offset(ankleX, ankleY),
        end = Offset(toeTipX, toeTipY),
        strokeWidth = strokeW * 0.95f,
        cap = StrokeCap.Round,
      )
    }

    val quadPath = Path().apply {
      moveTo(hipX - quadW, waistY - s * 0.04f)
      cubicTo(
        hipX - quadW * 1.28f, waistY + s * 0.14f,
        kneeX - quadW * 0.95f, kneeY - s * 0.04f,
        kneeX - s * 0.055f, kneeY + s * 0.02f,
      )
      lineTo(kneeX, kneeY - s * 0.01f)
      lineTo(kneeX + s * 0.055f, kneeY + s * 0.02f)
      cubicTo(
        kneeX + quadW * 0.95f, kneeY - s * 0.04f,
        hipX + quadW * 1.28f, waistY + s * 0.14f,
        hipX + quadW, waistY - s * 0.04f,
      )
      close()
    }
    drawPath(
      path = quadPath,
      brush = Brush.horizontalGradient(
        colors = listOf(goldBase, amberShade, bronzeDeep),
        startX = hipX - quadW,
        endX = hipX + quadW,
      ),
    )
    drawPath(quadPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))
  }

  listOf(-1f, 1f).forEach { side ->
    val shX = cx + side * shoulderHalfW
    val shY = chestY - s * 0.04f
    val elbowX = cx + side * (shoulderHalfW + s * (0.18f + muscle * 0.08f))
    val elbowY = chestY + s * (0.14f - flex * 0.35f)
    val fistX = cx + side * (shoulderHalfW + s * (0.10f + flex * 0.18f))
    val fistY = chestY + s * (0.28f - flex * 0.75f)

    val armW = s * (0.11f + muscle * 0.045f)

    val upperArmPath = Path().apply {
      moveTo(shX - side * armW * 0.5f, shY - armW * 0.7f)
      cubicTo(
        shX + side * armW * 1.2f, shY - armW * 0.9f,
        elbowX + side * armW * 0.9f, (shY + elbowY) * 0.5f,
        elbowX + side * armW * 0.4f, elbowY + armW * 0.3f,
      )
      lineTo(elbowX - side * armW * 0.5f, elbowY + armW * 0.2f)
      quadraticTo(
        (shX + elbowX) * 0.5f - side * armW * 0.3f,
        (shY + elbowY) * 0.5f + armW * 0.3f,
        shX - side * armW * 0.5f,
        shY + armW * 0.6f,
      )
      close()
    }
    drawPath(
      path = upperArmPath,
      brush = Brush.verticalGradient(
        colors = listOf(goldHighlight, goldBase, amberShade),
        startY = shY - armW,
        endY = elbowY + armW,
      ),
    )
    drawPath(upperArmPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    if (equipped.contains("spartan_pauldrons")) {
      val pauldronPath = Path().apply {
        moveTo(shX - side * armW * 0.6f, shY - armW * 0.75f)
        quadraticTo(shX + side * armW * 0.8f, shY - armW * 1.15f, shX + side * armW * 1.15f, shY + armW * 0.15f)
        lineTo(shX - side * armW * 0.4f, shY + armW * 0.05f)
        close()
      }
      drawPath(pauldronPath, color = Color(0xFFFFB300))
      drawPath(pauldronPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
    }

    val forearmPath = Path().apply {
      moveTo(elbowX - side * armW * 0.45f, elbowY - armW * 0.2f)
      lineTo(fistX - side * armW * 0.40f, fistY - armW * 0.35f)
      lineTo(fistX + side * armW * 0.45f, fistY + armW * 0.35f)
      lineTo(elbowX + side * armW * 1.05f, (elbowY + fistY) * 0.5f + s * 0.06f)
      lineTo(elbowX + side * armW * 0.75f, elbowY + s * 0.04f)
      lineTo(elbowX + side * armW * 1.15f, elbowY + s * 0.12f)
      close()
    }
    drawPath(
      path = forearmPath,
      brush = Brush.linearGradient(
        colors = listOf(goldBase, amberShade, bronzeDeep),
        start = Offset(fistX, fistY),
        end = Offset(elbowX, elbowY),
      ),
    )
    drawPath(forearmPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

    if (equipped.contains("wrist_bracers")) {
      drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(fistX - s * 0.055f, fistY - s * 0.045f),
        size = Size(s * 0.11f, s * 0.07f),
        cornerRadius = CornerRadius(6f, 6f),
      )
      drawRoundRect(
        color = Color(0xFFFFB300),
        topLeft = Offset(fistX - s * 0.055f, fistY - s * 0.045f),
        size = Size(s * 0.11f, s * 0.07f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = strokeW * 0.7f),
      )
    }

    if (equipped.contains("golden_dumbbells") && !isLiftingIron) {
      val dbHalf = s * 0.12f
      drawLine(
        color = InkOutline,
        start = Offset(fistX - dbHalf, fistY),
        end = Offset(fistX + dbHalf, fistY),
        strokeWidth = strokeW * 1.6f,
        cap = StrokeCap.Round,
      )
      listOf(-1f, 1f).forEach { pSide ->
        val px = fistX + pSide * dbHalf
        drawRoundRect(
          color = Color(0xFFFFB300),
          topLeft = Offset(px - s * 0.026f, fistY - s * 0.065f),
          size = Size(s * 0.052f, s * 0.13f),
          cornerRadius = CornerRadius(6f, 6f),
        )
        drawRoundRect(
          color = InkOutline,
          topLeft = Offset(px - s * 0.026f, fistY - s * 0.065f),
          size = Size(s * 0.052f, s * 0.13f),
          cornerRadius = CornerRadius(6f, 6f),
          style = Stroke(width = strokeW * 0.75f),
        )
      }
    }
  }

  val torsoPath = Path().apply {
    moveTo(cx - shoulderHalfW, chestY - s * 0.10f)
    lineTo(cx + shoulderHalfW, chestY - s * 0.10f)
    cubicTo(
      cx + shoulderHalfW * 1.04f, chestY + s * 0.08f,
      cx + waistHalfW * 1.15f, waistY - s * 0.08f,
      cx + waistHalfW, waistY + s * 0.04f,
    )
    quadraticTo(cx, waistY + s * 0.12f, cx - waistHalfW, waistY + s * 0.04f)
    cubicTo(
      cx - waistHalfW * 1.15f, waistY - s * 0.08f,
      cx - shoulderHalfW * 1.04f, chestY + s * 0.08f,
      cx - shoulderHalfW, chestY - s * 0.10f,
    )
    close()
  }
  drawPath(
    path = torsoPath,
    brush = Brush.verticalGradient(
      colors = listOf(goldHighlight, goldBase, amberShade),
      startY = chestY - s * 0.12f,
      endY = waistY + s * 0.10f,
    ),
  )
  drawPath(torsoPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

  val pecBottomY = chestY + s * (0.09f + muscle * 0.03f)
  listOf(-1f, 1f).forEach { side ->
    val pecPath = Path().apply {
      moveTo(cx + side * s * 0.025f, chestY - s * 0.04f)
      lineTo(cx + side * s * 0.025f, pecBottomY - s * 0.02f)
      quadraticTo(
        cx + side * shoulderHalfW * 0.48f,
        pecBottomY + s * 0.03f,
        cx + side * shoulderHalfW * 0.84f,
        chestY + s * 0.02f,
      )
    }
    drawPath(
      path = pecPath,
      color = InkOutline,
      style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
  }

  if (muscle > 0.2f) {
    val absTopY = pecBottomY + s * 0.02f
    val absBotY = waistY - s * 0.01f
    drawLine(
      color = InkOutline.copy(alpha = 0.75f),
      start = Offset(cx, absTopY),
      end = Offset(cx, absBotY),
      strokeWidth = strokeW * 0.7f,
      cap = StrokeCap.Round,
    )
    for (row in 1..2) {
      val ry = absTopY + (absBotY - absTopY) * (row / 3f)
      drawLine(
        color = InkOutline.copy(alpha = 0.70f),
        start = Offset(cx - waistHalfW * 0.52f, ry),
        end = Offset(cx + waistHalfW * 0.52f, ry),
        strokeWidth = strokeW * 0.65f,
        cap = StrokeCap.Round,
      )
    }
  }

  if (equipped.contains("champion_belt")) {
    val beltH = s * 0.09f
    drawRoundRect(
      color = Color(0xFF3E2723),
      topLeft = Offset(cx - waistHalfW * 1.04f, waistY - beltH * 0.5f),
      size = Size(waistHalfW * 2.08f, beltH),
      cornerRadius = CornerRadius(10f, 10f),
    )
    drawRoundRect(
      color = InkOutline,
      topLeft = Offset(cx - waistHalfW * 1.04f, waistY - beltH * 0.5f),
      size = Size(waistHalfW * 2.08f, beltH),
      cornerRadius = CornerRadius(10f, 10f),
      style = Stroke(width = strokeW * 0.8f),
    )
    drawOval(
      color = Color(0xFFFFB300),
      topLeft = Offset(cx - waistHalfW * 0.38f, waistY - beltH * 0.68f),
      size = Size(waistHalfW * 0.76f, beltH * 1.36f),
    )
    drawOval(
      color = InkOutline,
      topLeft = Offset(cx - waistHalfW * 0.38f, waistY - beltH * 0.68f),
      size = Size(waistHalfW * 0.76f, beltH * 1.36f),
      style = Stroke(width = strokeW * 0.8f),
    )
  }

  val headCy = chestY - s * 0.34f
  val headR = s * 0.21f
  val manePath = Path().apply {
    val topY = headCy + headR * 0.25f
    val botY = chestY - s * 0.01f
    val span = shoulderHalfW * 0.78f
    moveTo(cx - headR * 0.90f, topY)
    lineTo(cx - span, botY - s * 0.06f)
    lineTo(cx - span * 0.62f, botY + s * 0.03f)
    lineTo(cx - span * 0.42f, botY - s * 0.04f)
    lineTo(cx - span * 0.18f, botY + s * 0.06f)
    lineTo(cx, botY - s * 0.03f)
    lineTo(cx + span * 0.18f, botY + s * 0.06f)
    lineTo(cx + span * 0.42f, botY - s * 0.04f)
    lineTo(cx + span * 0.62f, botY + s * 0.03f)
    lineTo(cx + span, botY - s * 0.06f)
    lineTo(cx + headR * 0.90f, topY)
    close()
  }
  drawPath(
    path = manePath,
    brush = Brush.verticalGradient(
      colors = listOf(goldHighlight, goldBase, amberShade),
      startY = headCy,
      endY = chestY + s * 0.06f,
    ),
  )
  drawPath(manePath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

  val combPath = Path().apply {
    val cBaseY = headCy - headR * 0.72f
    val cSpan = headR * 0.85f
    moveTo(cx - cSpan * 0.65f, cBaseY + s * 0.04f)
    lineTo(cx - cSpan * 0.78f, cBaseY - s * 0.10f)
    lineTo(cx - cSpan * 0.38f, cBaseY - s * 0.03f)
    lineTo(cx - cSpan * 0.42f, cBaseY - s * 0.16f)
    lineTo(cx - cSpan * 0.08f, cBaseY - s * 0.05f)
    lineTo(cx + cSpan * 0.02f, cBaseY - s * 0.20f)
    lineTo(cx + cSpan * 0.25f, cBaseY - s * 0.05f)
    lineTo(cx + cSpan * 0.48f, cBaseY - s * 0.16f)
    lineTo(cx + cSpan * 0.55f, cBaseY - s * 0.03f)
    lineTo(cx + cSpan * 0.80f, cBaseY - s * 0.11f)
    lineTo(cx + cSpan * 0.62f, cBaseY + s * 0.05f)
    close()
  }
  drawPath(
    path = combPath,
    brush = Brush.verticalGradient(
      colors = listOf(combBright, combRed),
      startY = headCy - headR * 1.6f,
      endY = headCy - headR * 0.6f,
    ),
  )
  drawPath(combPath, color = InkOutline, style = Stroke(width = strokeW * 0.9f, join = StrokeJoin.Round))

  val headPath = Path().apply {
    moveTo(cx, headCy - headR * 0.92f)
    cubicTo(
      cx + headR * 0.88f, headCy - headR * 0.92f,
      cx + headR * 1.08f, headCy - headR * 0.10f,
      cx + headR * 0.92f, headCy + headR * 0.48f,
    )
    quadraticTo(cx, headCy + headR * 0.88f, cx - headR * 0.92f, headCy + headR * 0.48f)
    cubicTo(
      cx - headR * 1.08f, headCy - headR * 0.10f,
      cx - headR * 0.88f, headCy - headR * 0.92f,
      cx, headCy - headR * 0.92f,
    )
    close()
  }
  drawPath(
    path = headPath,
    brush = Brush.radialGradient(
      colors = listOf(goldHighlight, goldBase, amberShade),
      center = Offset(cx - headR * 0.15f, headCy - headR * 0.15f),
      radius = headR * 1.35f,
    ),
  )
  drawPath(headPath, color = InkOutline, style = Stroke(width = strokeW, join = StrokeJoin.Round))

  if (equipped.contains("headband_crimson")) {
    val bandY = headCy - headR * 0.56f
    drawLine(
      color = Color(0xFFD32F2F),
      start = Offset(cx - headR * 0.90f, bandY),
      end = Offset(cx + headR * 0.90f, bandY),
      strokeWidth = s * 0.055f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = InkOutline,
      start = Offset(cx - headR * 0.90f, bandY - s * 0.028f),
      end = Offset(cx + headR * 0.90f, bandY - s * 0.028f),
      strokeWidth = strokeW * 0.65f,
    )
    drawLine(
      color = InkOutline,
      start = Offset(cx - headR * 0.90f, bandY + s * 0.028f),
      end = Offset(cx + headR * 0.90f, bandY + s * 0.028f),
      strokeWidth = strokeW * 0.65f,
    )
  }

  listOf(-1f, 1f).forEach { wSide ->
    val wattlePath = Path().apply {
      val wx = cx + wSide * s * 0.038f
      val wy = headCy + headR * 0.42f
      moveTo(wx, wy)
      cubicTo(
        wx + wSide * s * 0.065f, wy + s * 0.11f,
        wx + wSide * s * 0.015f, wy + s * 0.14f,
        wx - wSide * s * 0.01f, wy + s * 0.04f,
      )
      close()
    }
    drawPath(wattlePath, color = combRed)
    drawPath(wattlePath, color = InkOutline, style = Stroke(width = strokeW * 0.75f, join = StrokeJoin.Round))
  }

  listOf(-1f, 1f).forEach { eSide ->
    val ex = cx + eSide * headR * 0.44f
    val ey = headCy - headR * 0.08f
    val ew = s * 0.046f
    val eh = s * 0.036f

    val eyeShape = Path().apply {
      moveTo(ex - ew, ey)
      lineTo(ex + eSide * ew * 0.2f, ey - eh * (1.0f - eSide * 0.25f))
      lineTo(ex + ew, ey)
      quadraticTo(ex, ey + eh * 1.1f, ex - ew, ey)
      close()
    }
    drawPath(eyeShape, color = Color.White)
    drawCircle(color = InkOutline, radius = s * 0.022f, center = Offset(ex - eSide * s * 0.005f, ey))
    drawCircle(color = Color.White, radius = s * 0.008f, center = Offset(ex - s * 0.010f, ey - s * 0.008f))
    drawPath(eyeShape, color = InkOutline, style = Stroke(width = strokeW * 0.75f, join = StrokeJoin.Round))

    drawLine(
      color = InkOutline,
      start = Offset(ex - eSide * ew * 1.15f, ey - eh * 0.55f),
      end = Offset(ex + eSide * ew * 1.15f, ey - eh * 1.35f),
      strokeWidth = strokeW * 1.05f,
      cap = StrokeCap.Round,
    )
  }

  val beakPath = Path().apply {
    val by = headCy + headR * 0.14f
    val bw = s * 0.078f
    moveTo(cx - bw, by)
    lineTo(cx, by - s * 0.045f)
    lineTo(cx + bw, by)
    lineTo(cx, by + s * 0.095f)
    close()
  }
  drawPath(
    path = beakPath,
    brush = Brush.horizontalGradient(
      colors = listOf(Color(0xFFFFE082), Color(0xFFFFB300), Color(0xFFE65100)),
      startX = cx - s * 0.08f,
      endX = cx + s * 0.08f,
    ),
  )
  drawLine(
    color = InkOutline.copy(alpha = 0.55f),
    start = Offset(cx, headCy + headR * 0.14f - s * 0.042f),
    end = Offset(cx, headCy + headR * 0.14f + s * 0.092f),
    strokeWidth = strokeW * 0.6f,
  )
  drawPath(beakPath, color = InkOutline, style = Stroke(width = strokeW * 0.85f, join = StrokeJoin.Round))
}
