package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.activities.SplashBatchActivity.Companion.SPLASH_BATCH_ACTIVITY_ERROR
import com.prey.net.TestWebServices

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class SplashBatchActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<SplashBatchActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, SplashBatchActivity::class.java)
    }

    @Test
    fun test_splash_error() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        //assertEquals(activityView, SPLASH_BATCH_ACTIVITY_ERROR)
        assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_ASK)
    }

    @Test
    fun test_splash_token() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setTokenBatch("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, SPLASH_BATCH_ACTIVITY_ERROR)
    }

}