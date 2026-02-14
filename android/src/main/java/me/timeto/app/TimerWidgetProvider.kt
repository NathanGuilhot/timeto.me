package me.timeto.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.timeto.shared.db.Goal2Db
import me.timeto.shared.db.IntervalDb
import me.timeto.shared.time
import me.timeto.shared.toHms

class TimerWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val views = RemoteViews(context.packageName, R.layout.widget_timer)

            try {
                val intervals = IntervalDb.selectDescSync(1)
                if (intervals.isNotEmpty()) {
                    val interval = intervals.first()
                    val now = time()
                    val secondsToEnd = interval.id + interval.timer - now
                    val elapsed = now - interval.id

                    val timerText = if (secondsToEnd < 0) {
                        "-${formatTimer(-secondsToEnd)}"
                    } else {
                        formatTimer(elapsed)
                    }

                    views.setTextViewText(R.id.widget_timer, timerText)

                    val goal = Goal2Db.selectAllSync().firstOrNull { it.id == interval.goal_id }
                    val taskName = goal?.name ?: interval.note ?: "No timer"
                    views.setTextViewText(R.id.widget_task, taskName)
                } else {
                    views.setTextViewText(R.id.widget_timer, "00:00")
                    views.setTextViewText(R.id.widget_task, "No timer")
                }
            } catch (e: Exception) {
                views.setTextViewText(R.id.widget_timer, "00:00")
                views.setTextViewText(R.id.widget_task, "Error")
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_timer, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_task, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun formatTimer(seconds: Int): String {
        val hms = seconds.toHms()
        return if (hms[0] > 0) {
            "${hms[0]}:${hms[1].toString().padStart(2, '0')}:${hms[2].toString().padStart(2, '0')}"
        } else {
            "${hms[1].toString().padStart(2, '0')}:${hms[2].toString().padStart(2, '0')}"
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
    }
}
