package com.prey.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AlarmReportReceiverTest {

    private lateinit var context: Context
    private val alarmReportReceiver = AlarmReportReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_start_report() {
        val intent = Intent()
        PreyConfig.getInstance(context).setLastEvent("")
        alarmReportReceiver.onReceive(context, intent)
        Thread.sleep(2_000)
        val lastEvent = PreyConfig.getInstance(context).getLastEvent()
        assertEquals(lastEvent, "report_start")
    }

}