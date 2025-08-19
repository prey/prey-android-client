package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.net.TestWebServices
import com.prey.PreyLogger

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class SecurityActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<SecurityActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, SecurityActivity::class.java)
    }

    @Test
    fun test_secutity_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setActivityView("")
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("_________activityView:$activityView")
        assertNotNull(activityView)
        assertEquals(activityView, ReportActivity.ACTIVITY_REPORT)
    }

}