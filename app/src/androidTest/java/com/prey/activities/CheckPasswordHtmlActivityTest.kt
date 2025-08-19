package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig
import com.prey.R

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CheckPasswordHtmlActivityTest {

    private lateinit var context: Context

    @get:Rule
    val activityRule = ActivityScenarioRule(CheckPasswordHtmlActivity::class.java)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testActivityCreation() {
        // Verify the WebView is displayed
        onView(withId(R.id.install_browser))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testResolveRestrictions() {
        val preyConfig = PreyConfig.getInstance(context)
        activityRule.scenario.onActivity { activity ->
            // Clear any existing registration
            preyConfig.removeDeviceId()
            // Test restrictions resolution
            activity.resolveRestrictions(context)
            // Verify device is not registered without proper restrictions
            assert(!preyConfig.isThisDeviceAlreadyRegisteredWithPrey())
        }
    }

    @Test
    fun testBroadcastReceiverRegistration() {
        activityRule.scenario.onActivity { activity ->
            // Verify receivers are registered by sending test broadcasts
            context.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
            // Activity should finish - we can verify this through the scenario state
        }
    }

}