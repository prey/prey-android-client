package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R
import com.prey.net.TestWebServices

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class PinNativeActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<PinNativeActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, PinNativeActivity::class.java)
    }

    @Test
    fun test_pin_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setPinActivated("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("_________activityView:$activityView")
        assertNotNull(activityView)
        assertEquals(activityView, CloseActivity.ACTIVITY_CLOSE)
    }

    @Test
    fun test_pin_withPin() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setPinActivated("1234")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, PinNativeActivity.ACTIVITY_PIN_FORM)
    }

    @Test
    fun test_pin_withPinButton_ok() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setPinActivated("1234")
        PreyConfig.getInstance(context).setPinNumber("1234")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        onView(withId(R.id.editTextPin))
            .perform(clearText(), typeText("1234"), closeSoftKeyboard())
        onView(withId(R.id.button_Super_Lock_Unlock)).perform(ViewActions.click())
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, PinNativeActivity.ACTIVITY_PIN_BUTTON_OK)
    }

    @Test
    fun test_pin_withPinButton_error() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setPinActivated("1234")
        PreyConfig.getInstance(context).setPinNumber("1234")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        onView(withId(R.id.editTextPin))
            .perform(clearText(), typeText("1235"), closeSoftKeyboard())
        onView(withId(R.id.button_Super_Lock_Unlock)).perform(ViewActions.click())
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, PinNativeActivity.ACTIVITY_PIN_BUTTON_ERROR)
    }

}