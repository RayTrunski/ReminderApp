package com.example.redbuttonapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews

class RedButtonWidget : AppWidgetProvider() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val store = TimerStore(context)
        when (intent.action) {
            ACTIVATE -> store.activate()
            RESET -> store.reset()
            EXPIRE -> { store.reschedule(); updateAll(context) }
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                store.reschedule()
                updateAll(context)
            }
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        TimerStore(context).reschedule()
        updateAll(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        updateAll(context)
    }

    companion object {
        const val ACTIVATE = "com.example.redbuttonapp.ACTIVATE"
        const val RESET = "com.example.redbuttonapp.RESET"
        const val EXPIRE = "com.example.redbuttonapp.EXPIRE"

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, RedButtonWidget::class.java))
            val state = TimerStore(context).read()
            ids.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.red_button_widget)
                views.setImageViewResource(R.id.widget_button_image,
                    if (state.active) R.drawable.widget_button_green else R.drawable.widget_button_red)
                views.setTextViewText(R.id.widget_button_label, context.getString(
                    if (state.active) R.string.widget_active else R.string.widget_activate))
                views.setContentDescription(R.id.widget_button, context.getString(
                    if (state.active) R.string.widget_active else R.string.widget_activate))
                val openApp = PendingIntent.getActivity(
                    context, 0, Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                views.setOnClickPendingIntent(R.id.widget_button,
                    if (state.active) openApp else broadcast(context, ACTIVATE))
                manager.updateAppWidget(id, views)
            }
        }

        private fun broadcast(context: Context, action: String) = PendingIntent.getBroadcast(
            context, 0, Intent(context, RedButtonWidget::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
}
