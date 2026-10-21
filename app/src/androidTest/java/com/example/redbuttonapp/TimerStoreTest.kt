package com.example.redbuttonapp

import android.os.SystemClock
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimerStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val appStore = TimerStore(context)
    private val widgetStore = TimerStore(context)

    @Before fun prepare() {
        appStore.reset()
        appStore.preferences.edit().clear().commit()
    }

    @After fun cleanup() {
        appStore.reset()
        appStore.preferences.edit().clear().commit()
    }

    @Test fun appAndWidgetShareDurationActivationAndReset() {
        appStore.selectMinutes(15)
        assertEquals(15, widgetStore.read().minutes)
        widgetStore.activate()
        assertTrue(appStore.read().active)
        assertTrue(appStore.read().remaining in 1..900_000L)
        val deadline = appStore.preferences.getLong("deadline", 0)
        appStore.activate()
        appStore.selectMinutes(60)
        assertEquals(deadline, widgetStore.preferences.getLong("deadline", 0))
        assertEquals(15, widgetStore.read().minutes)
        appStore.reset()
        assertFalse(widgetStore.read().active)
    }

    @Test fun expiredOrPreviousBootIntervalsAreInactive() {
        val boot = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)
        appStore.preferences.edit().putInt("boot", boot)
            .putLong("deadline", SystemClock.elapsedRealtime() - 1).commit()
        assertFalse(widgetStore.read().active)
        appStore.preferences.edit().putInt("boot", boot - 1)
            .putLong("deadline", SystemClock.elapsedRealtime() + 900_000).commit()
        assertFalse(widgetStore.read().active)
        assertEquals(0L, widgetStore.read().remaining)
    }
}
