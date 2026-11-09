package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AthleteProfileStore
import com.example.data.NutritionStore
import com.example.navigation.Screen
import com.example.ui.components.AccountSwitcherSheet
import com.example.ui.components.SquircleSpeedDialFab
import com.example.ui.components.WorkspaceBottomBar
import com.example.ui.components.WorkspaceDrawer
import com.example.ui.components.WorkspaceTopBar
import com.example.ui.dialogs.TapeMeasurementDialog
import com.example.ui.screens.ActiveWorkoutScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CardioTreadmillRecoveryScreen
import com.example.ui.screens.CoachScreen
import com.example.ui.screens.ExerciseLibraryScreen
import com.example.ui.screens.NutritionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProgrammeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.TransformationScreen
import com.example.ui.theme.GoogleFormTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    AthleteProfileStore.initialize(applicationContext)
    NutritionStore.initialize(applicationContext)
    enableEdgeToEdge()
    setContent {
      GoogleFormTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          GoogleFormApp()
        }
      }
    }
  }
}

@Composable
fun GoogleFormApp() {
  val context = LocalContext.current
  LaunchedEffect(context) {
    AthleteProfileStore.initialize(context)
    NutritionStore.initialize(context)
  }

  val activeProfile by AthleteProfileStore.activeProfile.collectAsState()

  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Today.route

  var activeWorkoutDay by remember { mutableStateOf<Int?>(null) }
  var showAccountSwitcher by remember { mutableStateOf(false) }
  var showTapeDialog by remember { mutableStateOf(false) }

  // 1. Mandatory First-Launch Onboarding Flow (Collects ONLY Name, Sex, Height, Weight, Waist Length)
  if (!activeProfile.hasCompletedOnboarding) {
    OnboardingScreen(
      onOnboardingCompleted = {
        scope.launch {
          snackbarHostState.showSnackbar("Welcome ${AthleteProfileStore.activeProfile.value.name}! Your Sacred Egg is ready to hatch!")
        }
      }
    )
    return
  }

  // Back navigation handling
  if (activeWorkoutDay != null) {
    BackHandler {
      activeWorkoutDay = null
    }
  } else if (showTapeDialog) {
    BackHandler {
      showTapeDialog = false
    }
  } else if (showAccountSwitcher) {
    BackHandler {
      showAccountSwitcher = false
    }
  } else if (currentRoute != Screen.Today.route) {
    BackHandler {
      if (drawerState.isOpen) {
        scope.launch { drawerState.close() }
      } else {
        navController.navigate(Screen.Today.route) {
          popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
          }
          launchSingleTop = true
          restoreState = true
        }
      }
    }
  }

  // Active Time-Scaled Workout Screen or Decoupled Sunday Cardio & Recovery Console Takeover
  if (activeWorkoutDay != null) {
    if (activeWorkoutDay == 7) {
      CardioTreadmillRecoveryScreen(
        onClosePlayer = { activeWorkoutDay = null },
      )
    } else {
      ActiveWorkoutScreen(
        dayNumber = activeWorkoutDay ?: 1,
        onClosePlayer = { activeWorkoutDay = null },
      )
    }
  } else {
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        WorkspaceDrawer(
          currentRoute = currentRoute,
          onNavigateToScreen = { screen ->
            navController.navigate(screen.route) {
              popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          },
          onCloseDrawer = {
            scope.launch { drawerState.close() }
          },
        )
      },
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = WindowInsets(0, 0, 0, 0),
          snackbarHost = { SnackbarHost(snackbarHostState) },
          topBar = {
            WorkspaceTopBar(
              onMenuClick = {
                scope.launch { drawerState.open() }
              },
              onProfileClick = {
                showAccountSwitcher = true
              },
            )
          },
          bottomBar = {
            WorkspaceBottomBar(
              currentRoute = currentRoute,
              onNavigateToScreen = { screen ->
                navController.navigate(screen.route) {
                  popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                  }
                  launchSingleTop = true
                  restoreState = true
                }
              },
            )
          },
        ) { innerPadding ->
          val screenPadding = PaddingValues(
            top = innerPadding.calculateTopPadding() + 8.dp,
            bottom = innerPadding.calculateBottomPadding(),
          )

          NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.fillMaxSize(),
          ) {
            composable(Screen.Today.route) {
              TodayScreen(
                onStartWorkout = {
                  activeWorkoutDay = AthleteProfileStore.getAutoDetectedWorkoutDay()
                },
                onStartSpecificWorkout = { day ->
                  activeWorkoutDay = day
                },
                onNavigateToTransformation = {
                  navController.navigate(Screen.Transformation.route)
                },
                onNavigateToNutrition = {
                  navController.navigate(Screen.Nutrition.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                      saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                  }
                },
                onOpenBiometricsClick = {
                  showTapeDialog = true
                },
                contentPadding = screenPadding,
              )
            }
            composable(Screen.Programme.route) {
              ProgrammeScreen(
                onStartSpecificWorkout = { day ->
                  activeWorkoutDay = day
                },
                contentPadding = screenPadding,
              )
            }
            composable(Screen.Transformation.route) {
              Box(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                TransformationScreen(
                  onOpenQuickLogDialog = {
                    showTapeDialog = true
                  }
                )
              }
            }
            composable(Screen.Analytics.route) {
              AnalyticsScreen(contentPadding = screenPadding)
            }
            composable(Screen.Nutrition.route) {
              NutritionScreen(contentPadding = screenPadding)
            }
            composable(Screen.ExerciseLibrary.route) {
              ExerciseLibraryScreen(contentPadding = screenPadding)
            }
            composable(Screen.Settings.route) {
              SettingsScreen(contentPadding = screenPadding)
            }
          }
        }

        // Squircle Speed Dial Floating Action Button Overlay
        SquircleSpeedDialFab(
          onStartWorkout = {
            activeWorkoutDay = AthleteProfileStore.getAutoDetectedWorkoutDay()
          },
          onLogWeightTape = {
            showTapeDialog = true
          },
          onPreWorkoutCheckIn = {
            showTapeDialog = true
          },
          modifier = Modifier.fillMaxSize(),
        )

        // Athlete 3-Biometric Profile Manager Sheet
        if (showAccountSwitcher) {
          AccountSwitcherSheet(
            onDismiss = { showAccountSwitcher = false },
            onNavigateToSettings = {
              showAccountSwitcher = false
              navController.navigate(Screen.Settings.route)
            },
          )
        }

        // 3-Biometric Check-In Dialog (Weight, Waist Length, Height)
        if (showTapeDialog) {
          TapeMeasurementDialog(
            onDismiss = { showTapeDialog = false },
            onSaveSuccess = {
              showTapeDialog = false
              scope.launch {
                val p = AthleteProfileStore.activeProfile.value
                snackbarHostState.showSnackbar(
                  "Saved ${p.weightKg} kg • Waist ${p.waistCm} cm • Height ${p.heightCm.toInt()} cm (+35 XP)"
                )
              }
            },
          )
        }
      }
    }
  }
}
