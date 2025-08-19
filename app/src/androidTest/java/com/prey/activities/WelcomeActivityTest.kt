package com.prey.activities

import android.content.Context
import android.content.Intent
import android.os.Build
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
class WelcomeActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<WelcomeActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, WelcomeActivity::class.java)
    }

    @Test
    fun test_welcomeActivity_login() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setActivityView("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        // assertEquals(activityView, WelcomeActivity.LOGIN_ACTIVITY)
        assertEquals(activityView, LoginActivity.ACTIVITY_LOGIN_CHECK_PASSWORD_HTML)
    }

    @Test
    fun test_welcomeActivity_password() {
        PreyConfig.getInstance(context).setApikey("apikey")
        PreyConfig.getInstance(context).setDeviceId("deviceId")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertEquals(activityView, WelcomeActivity.CHECK_PASSWORD_ACTIVITY)
        } else {
            assertEquals(activityView, WelcomeActivity.DEVICE_READY_ACTIVITY)
        }
    }

}