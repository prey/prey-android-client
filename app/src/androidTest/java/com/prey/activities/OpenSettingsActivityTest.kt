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
class OpenSettingsActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<OpenSettingsActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, OpenSettingsActivity::class.java)
    }

    @Test
    fun test_privacy_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(2_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, OpenSettingsActivity.DETAILS_SETTINGS)
    }

}