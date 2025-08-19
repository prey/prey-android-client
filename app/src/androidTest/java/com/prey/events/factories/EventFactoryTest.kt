package com.prey.events.factories

import android.content.Context
import android.content.Intent
import android.content.ContentResolver
import android.net.wifi.WifiManager
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.events.Event
import com.prey.net.TestWebServices

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class EventFactoryTest {
    private lateinit var context: Context

    private lateinit var mockContentResolver: ContentResolver

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
    }

    @Test
    fun test_boot_completed_event() {
        val intent = Intent(EventFactory.BOOT_COMPLETED)
        val event = EventFactory.getEvent(context, intent)
        assertNotNull(event)
        assertEquals(Event.TURNED_ON, event?.name)
    }

    @Test
    fun test_power_connected_event() {
        val intent = Intent(EventFactory.ACTION_POWER_CONNECTED)
        val event = EventFactory.getEvent(context, intent)
        assertNotNull(event)
        assertEquals(Event.POWER_CONNECTED, event?.name)
    }

    @Test
    fun test_power_disconnected_event() {
        val intent = Intent(EventFactory.ACTION_POWER_DISCONNECTED)
        val event = EventFactory.getEvent(context, intent)
        assertNotNull(event)
        assertEquals(Event.POWER_DISCONNECTED, event?.name)
    }

    @Test
    fun test_wifi_state_changed_event() {
        val intent = Intent(EventFactory.WIFI_STATE_CHANGED).apply {
            putExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_ENABLED)
        }
        val event = EventFactory.getEvent(context, intent)
        assertNotNull(event)
        assertEquals(Event.WIFI_CHANGED, event?.name)
        assertTrue(event?.info?.contains("wifi") == true)
    }

}