package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.TestWebServices

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class PopUpAlertActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<PopUpAlertActivity>

    private fun withWebFormIntent(context: Context): Intent {
        val intent = Intent(context, PopUpAlertActivity::class.java)
        intent.putExtra("alert_message", "alert_message")
        intent.putExtra("notificationId", 1333)
        return intent
    }

    @Test
    fun test_popup_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setNoficationPopupId(10)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //   Thread.sleep(3_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("_________activityView:$activityView")
        assertNotNull(activityView)
        assertEquals(activityView, PopUpAlertActivity.POPUP_FORM)
    }

}