package com.prey.services

import android.content.Context
import android.view.accessibility.AccessibilityEvent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse

class PreyAccessibilityServiceTest {

    private lateinit var context: Context
    private val preyAccessibilityService = PreyAccessibilityService()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_not_lock() {
        PreyConfig.getInstance(context).setUnlockPass("")
        val accessibilityEvent = AccessibilityEvent()
        val lock = preyAccessibilityService.chekLock(context, accessibilityEvent)
        assertFalse(lock)
    }

    @Test
    fun test_lock_false() {
        PreyConfig.getInstance(context).setUnlockPass("osito")
        val accessibilityEvent = AccessibilityEvent()
        accessibilityEvent.packageName = "com.prey"
        val lock = preyAccessibilityService.chekLock(context, accessibilityEvent)
        assertFalse(lock)
    }

    @Test
    fun test_lock_true() {
        PreyConfig.getInstance(context).setUnlockPass("osito")
        val accessibilityEvent = AccessibilityEvent()
        accessibilityEvent.packageName = "com.oso"
        val lock = preyAccessibilityService.chekLock(context, accessibilityEvent)
        assertFalse(lock)
    }

}