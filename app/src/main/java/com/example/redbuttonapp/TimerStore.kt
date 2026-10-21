package com.example.redbuttonapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.Settings

data class TimerState(val minutes: Int, val remaining: Long) {
    val active: Boolean get() = remaining > 0L
}

/** One persisted timer shared by the activity and every widget instance. */
class TimerStore(private val context: Context) {
    val preferences = context.getSharedPreferences("timer", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(AlarmManager::class.java)
    private fun bootCount() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)

    fun read(): TimerState {
        val minutes = preferences.getInt("minutes", 30)
        val remaining = if (preferences.getInt("boot", -2) == bootCount()) {
            (preferences.getLong("deadline", 0L) - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
        } else 0L
        return TimerState(minutes, remaining)
    }

    fun selectMinutes(minutes: Int) {
        if (minutes !in listOf(15, 30, 60) || read().active) return
        preferences.edit().putInt("minutes", minutes).apply()
        RedButtonWidget.updateAll(context)
    }

    fun activate() {
        val state = read()
        if (state.active) return
        val deadline = SystemClock.elapsedRealtime() + state.minutes * 60_000L
        preferences.edit().putLong("deadline", deadline).putInt("boot", bootCount()).commit()
        scheduleExpiry(deadline)
        RedButtonWidget.updateAll(context)
    }

    fun reset() {
        preferences.edit().remove("deadline").commit()
        alarms.cancel(expiryIntent())
        RedButtonWidget.updateAll(context)
    }

    fun reschedule() {
        val state = read()
        if (state.active) scheduleExpiry(SystemClock.elapsedRealtime() + state.remaining)
    }

    private fun scheduleExpiry(deadline: Long) {
        // No special alarm permission required. Android may defer this refresh in Doze.
        alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, deadline, expiryIntent())
    }

    private fun expiryIntent(): PendingIntent = PendingIntent.getBroadcast(
        context, 0, Intent(context, RedButtonWidget::class.java).setAction(RedButtonWidget.EXPIRE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
