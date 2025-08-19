package com.prey.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AlarmScheduledReceiverTest {

    private lateinit var context: Context
    private val alarmScheduledReceiver = AlarmScheduledReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_start_report() {
        val intent = Intent()
        alarmScheduledReceiver.onReceive(context, intent)
        Thread.sleep(3_000)
        val lastEvent = PreyConfig.getInstance(context).getLastEvent()
        assertEquals(lastEvent, "start_prey")
    }

}