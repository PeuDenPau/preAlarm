package com.peudenhau.prealarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.peudenhau.prealarm.R
import com.peudenhau.prealarm.schedule.SessionPlan

class AlarmScheduler(private val context: Context) {

    companion object {
        const val ACTION_START = "com.peudenhau.prealarm.action.START"
        const val ACTION_TASK_END = "com.peudenhau.prealarm.action.TASK_END"
        const val ACTION_DEPARTURE = "com.peudenhau.prealarm.action.DEPARTURE"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_TEXT = "extra_text"

        private const val RC_START = 1
        private const val RC_DEPARTURE = 2
        private const val RC_TASK_BASE = 10
        private const val MAX_TASKS = 32
    }

    fun canScheduleExact(): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
    }

    fun scheduleSession(
        plan: SessionPlan,
        startText: String,
        taskEndText: (Int) -> String,
        departureText: String,
    ) {
        setExact(
            RC_START,
            plan.startEpochMs,
            baseIntent(ACTION_START)
                .putExtra(EXTRA_TITLE, context.getString(R.string.notify_start_title))
                .putExtra(EXTRA_TEXT, startText),
        )
        plan.tasks.forEachIndexed { index, taskPlan ->
            setExact(
                RC_TASK_BASE + index,
                taskPlan.plannedEndEpochMs,
                baseIntent(ACTION_TASK_END)
                    .putExtra(EXTRA_TITLE, context.getString(R.string.notify_task_end_title))
                    .putExtra(EXTRA_TEXT, taskEndText(index)),
            )
        }
        setExact(
            RC_DEPARTURE,
            plan.departureEpochMs,
            baseIntent(ACTION_DEPARTURE)
                .putExtra(EXTRA_TITLE, context.getString(R.string.notify_departure_title))
                .putExtra(EXTRA_TEXT, departureText),
        )
    }

    fun cancelTaskEnd(taskIndex: Int) {
        cancel(RC_TASK_BASE + taskIndex, ACTION_TASK_END)
    }

    fun cancelSession() {
        cancel(RC_START, ACTION_START)
        cancel(RC_DEPARTURE, ACTION_DEPARTURE)
        repeat(MAX_TASKS) { cancel(RC_TASK_BASE + it, ACTION_TASK_END) }
    }

    private fun baseIntent(action: String): Intent =
        Intent(context, AlarmReceiver::class.java).setAction(action)

    private fun pendingIntent(requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun setExact(requestCode: Int, triggerAtEpochMs: Long, intent: Intent) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(requestCode, intent)
        if (!canScheduleExact()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtEpochMs, pi)
        }
    }

    private fun cancel(requestCode: Int, action: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(requestCode, baseIntent(action)))
    }
}
