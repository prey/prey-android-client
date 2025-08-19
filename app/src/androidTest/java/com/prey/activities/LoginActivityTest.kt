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
class LoginActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<LoginActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, LoginActivity::class.java)
    }

    @Test
    fun test_check_password_html() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setUnlockPass("")
        PreyConfig.getInstance(context).setProtectReady(false)
        PreyConfig.getInstance(context).setApiKeyBatch("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //Thread.sleep(4_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, LoginActivity.ACTIVITY_LOGIN_CHECK_PASSWORD_HTML)
    }

    @Test
    fun test_check_password_splash() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setUnlockPass("")
        PreyConfig.getInstance(context).setProtectReady(false)
        PreyConfig.getInstance(context).setApiKeyBatch("AP123")
        PreyConfig.getInstance(context).setTokenBatch("token")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_ASK)
    }

}