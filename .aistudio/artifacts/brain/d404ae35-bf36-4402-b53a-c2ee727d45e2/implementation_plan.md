# Google Form v2 Release — Hands-Free Training, 2D Dojo Evolution & Smart Biometrics

The v2 release elevates **Google Form** into a complete, self-adjusting personal transformation companion by combining weekday-aware dumbbell programming, per-exercise load memory, hands-free voice and 3-2-1 audio cues, an interactive 2D Warrior Rooster Dojo with evolving backgrounds and quests, real CSV/JSON data export, and a native Android Home Screen Widget.

## User Review & Critical Decisions

> [!IMPORTANT]
> All feature priorities below incorporate your confirmed selections from our planning interview. Review the architectural and design details below and click **Proceed** to build the v2 release.

- **Confirmed Workout Engine Scope**: Auto-detected weekday routine on the dashboard, per-exercise dumbbell weight memory, synchronized voice coach narration with 3-2-1 audio countdown chimes, and a post-workout celebration receipt featuring a one-tap progressive overload prompt.
- **Confirmed 2D Warrior Rooster & Gamification Scope**: Evolving 2D dojo background tiers across the 12 stages, interactive 2D rooster actions (*Idle*, *Power Flex*, *Dumbbell Lift*, *Protein Feast*), unlockable rooster plumage palettes and aura effects, and a Weekly Quest Board with a Streak Freeze Shield.
- **Confirmed Biometrics & Utility Scope**: Weekly waist and weight check-in reminder with trend delta comparison, real CSV and JSON database export via Android Share Intent in Settings, and a native Android Home Screen Widget displaying live streak, rooster stage, and today's workout.

---

## 1. Overview & Core Concept

- **What It Does**: Automatically prescribes today's exact weekday training split scaled to the user's time constraint, coaches the athlete from a distance with spoken cues and countdown tones, rewards every session and biometric check-in with interactive 2D Warrior Rooster animations, quests, and evolving dojo arenas, and surfaces live progress on the Android home screen.
- **Target Audience / Persona**: Home and gym athletes training with dumbbells (and optional bench/treadmill) who want a zero-friction, hands-free workout timer paired with motivating 2D RPG progression and scientific 3-biometric tracking (Weight, Height, Waist).
- **Key Value**: Eliminates manual day selection and weight re-entry, prevents missed cues when training away from the phone, and turns long-term body recomposition into a rewarding daily habit.

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Weekday-Aware Dashboard & Per-Exercise Load Memory**: Opening the app automatically selects today's scheduled split (Monday = Day 1 Chest/Triceps/Abs through Saturday = Day 6 Biceps/Back/Abs, Sunday = Day 7 Cardio & Treadmill Shred, with a quick day-switcher pill). Adjusting the dumbbell load on any exercise saves that exact working weight for future sessions.
  2. **Hands-Free Voice & 3-2-1 Countdown Workout Flow**: During active workouts, the audio engine announces upcoming exercises during Prep/Rest, calls out the halfway mark during Active Work, and plays crisp 3-2-1 countdown chimes before phase transitions.
  3. **Post-Workout Celebration Receipt & Progressive Overload**: When the session timer reaches `00:00`, a celebratory receipt displays total volume lifted (kg), sets completed, XP earned, 2D Rooster micro-level gain, and a one-tap **Smart Overload Prompt** (*"+1.0 kg Dumbbell Load for Next Session"*).
  4. **Interactive 2D Warrior Rooster Dojo & Quests**: On the dashboard, users can trigger animated 2D actions (*Idle*, *Power Flex*, *Lift Iron*, *Protein Feast*), customize unlocked plumage color palettes and elemental auras, and track 3 weekly transformation quests to earn bonus XP and Streak Freeze Shields.
  5. **Weekly Biometric Check-In & Real Data Export**: A smart check-in card highlights 7-day Weight, Waist, and RFM% deltas, while Settings provides instant CSV and JSON export of all logged workouts and measurements via the system share sheet.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: High-craft 2D cel-shaded dojo illustration paired with high-contrast Material 3 athletic telemetry cards.
  - *Color Palette & Mood*:
    - `--background`: Deep Sanctuary Slate (`#0E1722` to `#1E3148`) inside the 2D Dojo viewport; adaptive Material 3 surface containers (`surfaceContainerLow` / `surfaceContainerHigh`) across dashboard cards.
    - `--primary`: Kinetic Workout Blue (`#1A73E8` / `#4285F4`) for active work states and primary actions.
    - `--secondary`: Recovery Emerald (`#2E7D32` / `#34A853`) for rest phases, streak shields, and goal improvements.
    - `--accent`: Warrior Amber & Crimson (`#FFB300` / `#E65100` / `#D32F2F`) for 2D rooster plumage, quest badges, and countdown alerts.
  - *Typography & Hierarchy*: Bold tabular numerals for distance-readable workout countdowns, clean single-line metric pills with `maxLines = 1` and `Ellipsis` overflow protection, and expressive title headers.
  - *Component Styling & Layout*: Generous `16.dp`–`20.dp` card padding, `28.dp` squircle cards, and full edge-to-edge safe-area insets.
- **Interactive Feedback & Motion**:
  - Smooth cubic-Bezier 2D character animations for dumbbell curls, protein feeding particles, and flexing poses.
  - Haptic confirmation pulses paired with 3-2-1 audio tones and spring-animated quest progress bars.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Evolving 2D Vector Dojo & Unlockable Plumage Themes**
  - *Chosen Approach*: Extend the 2D Canvas character engine with 4 progressive dojo environment backdrops (Straw Sanctuary, Timber Courtyard, Iron Foundry Gym, Champion Colosseum), 4 interactive action states, and 4 unlockable plumage/aura palettes (*Golden Amber*, *Crimson Phoenix*, *Obsidian Shadow*, *Emerald Jade*).
  - *Why*: Delivers crisp, zero-artifact 2D artwork at any screen density while making every stage and milestone feel visually distinct.
  - *Alternatives Considered*: Static bitmap backgrounds were passed over because vector canvas rendering scales cleanly and animates smoothly at 60 FPS without bloating APK size.

- **Decision 2: Zero-Permission CSV & JSON Data Export**
  - *Chosen Approach*: Generate structured CSV and JSON payloads directly from Room DAO flows and `AthleteProfileStore`, allowing users to share or save files via Android's native `Intent.ACTION_SEND` share sheet and clipboard copy without requesting storage permissions.
  - *Why*: Fully complies with Google Play storage policies while supporting instant backup to Google Drive, email, or local file managers.
  - *Alternatives Considered*: Legacy external storage permissions (`WRITE_EXTERNAL_STORAGE`) were rejected due to Play policy restrictions.

- **Decision 3: Native AppWidgetProvider for Home Screen Streak & Workout Widget**
  - *Chosen Approach*: Implement a lightweight `AppWidgetProvider` using standard Android `RemoteViews` synchronized with `AthleteProfileStore` SharedPreferences.
  - *Why*: Guarantees fast, reliable rendering on all Android launchers without adding extra widget runtime dependencies.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        PERSISTENCE & STATE LAYER                             │
│  ┌───────────────────────────────┐      ┌─────────────────────────────────┐  │
│  │     AthleteProfileStore       │      │       GoogleFormDatabase        │  │
│  │ • Per-Exercise Load Memory    │◄────►│ • WorkoutSessionDao (Sessions)  │  │
│  │ • Plumage Palette & Aura ID   │      │ • BodyMeasurementDao (3-Bio)    │  │
│  │ • Streak Freeze Shields       │      │ • CSV / JSON Serializer Engine  │  │
│  │ • Weekly Quest Progress       │      └─────────────────────────────────┘  │
│  └──────────────┬────────────────┘                       │                   │
└─────────────────┼────────────────────────────────────────┼───────────────────┘
                  │                                        │
                  ▼                                        ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                     UI, AUDIO & WIDGET PRESENTATION LAYER                    │
│                                                                              │
│  ┌───────────────────────────────┐      ┌─────────────────────────────────┐  │
│  │     TodayScreen (v2 Hub)      │      │   ActiveWorkoutScreen (v2)      │  │
│  │ • Auto Weekday Split Selector │      │ • TextToSpeech + 3-2-1 Chimes   │  │
│  │ • 2D Dojo & Action Controls   │      │ • Per-Exercise Load Adjuster    │  │
│  │ • Plumage & Aura Customizer   │      │ • Post-Workout Receipt Modal    │  │
│  │ • Weekly Quest & Check-In Bar │      │ • +1kg Smart Overload Prompt    │  │
│  └───────────────────────────────┘      └─────────────────────────────────┘  │
│                                                                              │
│  ┌───────────────────────────────┐      ┌─────────────────────────────────┐  │
│  │     Settings & Export Hub     │      │  GoogleFormStreakWidgetProvider │  │
│  │ • 12-Stage 2D Dojo Previewer  │      │ • Live Streak & Shield Badge    │  │
│  │ • One-Tap CSV & JSON Share    │      │ • Today's Weekday Split & Tap   │  │
│  └───────────────────────────────┘      └─────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State

- **`AthleteProfile` Extensions**:
  - `exerciseWeightOverrides: Map<String, Float>`: Stores custom dumbbell weights (kg) per exercise name so adjustments persist across workouts.
  - `selectedPlumagePalette: String`: Active 2D rooster color theme (`"golden_amber"`, `"crimson_phoenix"`, `"obsidian_shadow"`, `"emerald_jade"`).
  - `streakFreezeShields: Int`: Count of earned streak freeze shields (awarded upon completing weekly quests).
  - `lastBiometricCheckInEpochMs: Long`: Tracks exact timestamp of the latest Weight/Waist/Height log to compute 7-day reminders and trend deltas.
- **`WeeklyQuest` Model**:
  - Evaluates live completion percentage and XP/Shield rewards from `completedWorkouts`, weekly volume lifted, and recent biometric check-in timestamps.

### Interactive Component & State Mapping

- **Auto Weekday Routine & Override**: `TodayScreen` computes `Calendar.DAY_OF_WEEK` to default to the matching Day 1–7 protocol, while exposing compact day chips (`Day 1`–`Day 7`) so the user can preview or launch any day on demand.
- **Per-Exercise Load Memory**: Tapping "Adjust Working Dumbbell Load" in the exercise sheet or during a workout opens a `+0.5 kg / -0.5 kg` stepper that updates `AthleteProfileStore.setExerciseWeightOverride(exerciseName, weightKg)` and immediately recalculates `WorkoutProtocolRepository.getScaledWorkoutPlan(...)`.
- **2D Rooster Action & Palette Controls**: Action pills (*Idle*, *Power Flex*, *Lift Iron*, *Protein Feast*) animate the 2D character canvas (curling dumbbells, animated protein bowl with rising `+XP` sparks, or flexing), while unlocked palette chips switch the 2D cel-shaded gradient tokens in real time.
- **Post-Workout Celebration Receipt**: Reaching `00:00` transitions `ActiveWorkoutScreen` to a comprehensive summary card with a one-tap button to apply `+1.0 kg` progressive overload to the profile's base dumbbell weight.
- **CSV / JSON Export & Home Screen Widget**: Tapping Export in `SettingsScreen` formats all Room database rows and profile metrics into shareable CSV/JSON text via `Intent.ACTION_SEND`, and any profile/workout update triggers an `AppWidgetManager` broadcast to refresh `GoogleFormStreakWidgetProvider`.
