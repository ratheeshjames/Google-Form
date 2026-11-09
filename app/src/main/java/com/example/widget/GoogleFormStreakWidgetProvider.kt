package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AthleteProfileStore
import com.example.model.WorkoutProtocolRepository
import java.util.Locale

class GoogleFormStreakWidgetProvider : AppWidgetProvider() {

  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    AthleteProfileStore.initialize(context)
    for (appWidgetId in appWidgetIds) {
      updateSingleWidget(context, appWidgetManager, appWidgetId)
    }
  }

  companion object {
    fun updateAllWidgets(context: Context) {
      try {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val componentName = ComponentName(appContext, GoogleFormStreakWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(componentName)
        if (ids != null && ids.isNotEmpty()) {
          for (id in ids) {
            updateSingleWidget(appContext, manager, id)
          }
        }
      } catch (_: Throwable) {
        // Ignore in non-widget test environments
      }
    }

    private fun updateSingleWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int,
    ) {
      val profile = AthleteProfileStore.activeProfile.value
      val todayDay = AthleteProfileStore.getAutoDetectedWorkoutDay()
      val todayWorkout = WorkoutProtocolRepository.getWorkout(todayDay)
      val stage = profile.evolutionStage

      val views = RemoteViews(context.packageName, R.layout.widget_streak_workout)
      views.setTextViewText(
        R.id.widget_streak_badge,
        "🔥 ${profile.streakDays}-Day Streak • 🛡️ ${profile.streakFreezeShields} Shield${if (profile.streakFreezeShields == 1) "" else "s"}",
      )
      views.setTextViewText(
        R.id.widget_stage_badge,
        "Stage ${stage.stageNumber}/12 • ${stage.shortLabel}",
      )
      views.setTextViewText(
        R.id.widget_workout_title,
        "Day $todayDay • ${todayWorkout.name}",
      )
      views.setTextViewText(
        R.id.widget_biometric_subtitle,
        String.format(
          Locale.US,
          "%dm Scaled Fit • %.1f→%.1f kg • Tap to Train",
          profile.sessionDurationMinutes,
          profile.weightKg,
          profile.autoGoalWeightKg,
        ),
      )

      val launchIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      }
      val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
      views.setOnClickPendingIntent(R.id.widget_root_container, pendingIntent)

      appWidgetManager.updateAppWidget(appWidgetId, views)
    }
  }
}
