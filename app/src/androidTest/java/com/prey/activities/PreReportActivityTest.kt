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
class PreReportActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<PreReportActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, PreReportActivity::class.java)
    }

    @Test
    fun test_pre_report_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, PreReportActivity.ACTIVITY_PRE_REPORT)
    }

}