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
class WelcomeBatchActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    lateinit var scenario: ActivityScenario<WelcomeBatchActivity>
    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, WelcomeBatchActivity::class.java)
    }

    @Test
    fun test_welcomeBatchActivity_ask() {
        PreyConfig.getInstance(context).setAskForNameBatch(true)
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("________________activityView:$activityView")
        assertNotNull(activityView)
        assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_ASK)
    }

    @Test
    fun test_welcomeBatchActivity_not_ask() {
        PreyConfig.getInstance(context).setAskForNameBatch(false)
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("________________activityView:$activityView")
        assertNotNull(activityView)
        // assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_NOT_ASK)
        assertEquals(activityView, PermissionInformationActivity.PERMISSIONS_ASK)
    }

    @Test
    fun test_welcomeBatchActivity_ask_name_empty() {
        PreyConfig.getInstance(context).setAskForNameBatch(true)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        onView(withId(R.id.editTextBatch2))
            .perform(clearText())
        onView(withId(R.id.buttonBatch2)).perform(ViewActions.click())
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("________________activityView:$activityView")
        assertNotNull(activityView)
        assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_ASK_EMPTY)
        // assertEquals(activityView,  PermissionInformationActivity.PERMISSIONS_ASK)
    }

    @Test
    fun test_welcomeBatchActivity_ask_name_ok() {
        PreyConfig.getInstance(context).setAskForNameBatch(true)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        onView(withId(R.id.editTextBatch2))
            .perform(clearText(), typeText("osito"), closeSoftKeyboard())
        onView(withId(R.id.buttonBatch2)).perform(ViewActions.click())
        val activityView = PreyConfig.getInstance(context).getActivityView()
        PreyLogger.i("________________activityView:$activityView")
        assertNotNull(activityView)
        //assertEquals(activityView, WelcomeBatchActivity.ACTIVITY_ASK_NAME)
        assertEquals(activityView, PermissionInformationActivity.PERMISSIONS_ASK)
    }

}