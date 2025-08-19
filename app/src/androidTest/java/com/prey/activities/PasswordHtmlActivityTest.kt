package com.prey.activities

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest

import com.prey.PreyConfig

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertContains
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@LargeTest
class PasswordHtmlActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val activityScenarioRule = activityScenarioRule<PasswordHtmlActivity>()

    private fun withWebFormIntent(context: Context): Intent {
        return Intent(context, PasswordHtmlActivity::class.java)
    }

    lateinit var scenario: ActivityScenario<PasswordHtmlActivity>

    @Test
    fun test_lock() {
        PreyConfig.getInstance(context).setUnlockPass("osito")
        PreyConfig.getInstance(context).setLockMessage("")
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        //Thread.sleep(1_000)
        val url = PreyConfig.getInstance(context).getLoadUrl()
        assertNotNull(url)
        assertContains(url, PasswordHtmlActivity.URL_LOCK)
    }

    @Test
    fun test_lock_with_message() {
        PreyConfig.getInstance(context).setUnlockPass("osito")
        PreyConfig.getInstance(context).setLockMessage("Hola")
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        // Thread.sleep(1_000)
        val url = PreyConfig.getInstance(context).getLoadUrl()
        assertNotNull(url)
        assertContains(url, PasswordHtmlActivity.URL_LOCK_WIH_MESSAGE)
    }

    @Test
    fun test_not_lock() {
        PreyConfig.getInstance(context).setUnlockPass("")
        PreyConfig.getInstance(context).setLockMessage("")
        scenario = ActivityScenario.launch(withWebFormIntent(context))
        val url = PreyConfig.getInstance(context).getLoadUrl()
        assertNotNull(url)
        assertContains(url, PasswordHtmlActivity.URL_OUT)
    }

}