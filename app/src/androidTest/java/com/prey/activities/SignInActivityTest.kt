package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.R
import com.prey.activities.SignInActivity.Companion.SIGN_IN_ACTIVITY_ERROR
import com.prey.activities.SignInActivity.Companion.SIGN_IN_ACTIVITY_FORM
import com.prey.PreyConfig
import com.prey.net.TestWebServices
import com.prey.PreyLogger

import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId

@RunWith(AndroidJUnit4::class)
@LargeTest
class SignInActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<SignInActivity>

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, SignInActivity::class.java)
    }

    @Test
    fun test_signInActivity_withWebForm() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //  Thread.sleep(1_000)
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, SIGN_IN_ACTIVITY_FORM)
    }

    @Test
    fun test_signInActivity_button_empty() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        val testWebServices = TestWebServices()
        val errorText = "Test exception"
        val exception = Exception(errorText)
        testWebServices.setErrorException(exception)
        PreyConfig.getInstance(context).setWebServices(testWebServices)
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //Thread.sleep(2_000)
        onView(withId(R.id.buttonSignin)).perform(ViewActions.click())
        val activityView = PreyConfig.getInstance(context).getActivityView()
        assertNotNull(activityView)
        assertEquals(activityView, SIGN_IN_ACTIVITY_ERROR)
    }

    @Test
    fun test_signInActivity_button_ok() {
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        PreyConfig.getInstance(context).setActivityView("")
        onView(withId(R.id.buttonSignin)).perform(ViewActions.click())
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("_________activityView:$activityView")
        assertNotNull(activityView)
        //assertEquals(activityView, SIGN_IN_ACTIVITY_WELCOME)
        assertEquals(activityView, PermissionInformationActivity.PERMISSIONS_ASK)
    }

}