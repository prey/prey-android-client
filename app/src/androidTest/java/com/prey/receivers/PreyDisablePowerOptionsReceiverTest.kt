package com.prey.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PreyDisablePowerOptionsReceiverTest {

    private lateinit var context: Context
    private val preyDisablePowerOptionsReceiver = PreyDisablePowerOptionsReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_not_disablePowerOptions() {
        val intent = Intent()
        preyDisablePowerOptionsReceiver.onReceive(context, intent)
        val lastEvent = PreyConfig.getInstance(context).getLastEvent()
        assertEquals(lastEvent, "not_disablePowerOptions")
    }

}