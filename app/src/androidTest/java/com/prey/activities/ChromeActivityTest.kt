package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class ChromeActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<ChromeActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, ChromeActivity::class.java)
    }

    @Test
    fun test_device_ready_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")

        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //Thread.sleep(3_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, ChromeActivity.ACTIVITY_CHROME)
    }

}