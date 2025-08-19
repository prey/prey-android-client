package com.prey.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AwareJobServiceTest {

    private lateinit var context: Context

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_awareJobService_success() {
        PreyConfig.getInstance(context).setLastEvent("")
        AwareJobService.scheduleJob(context)
        Thread.sleep(1_000)
        val lastEvent = PreyConfig.getInstance(context).getLastEvent()
        assertEquals(lastEvent, "awareJobService_success")
    }

}