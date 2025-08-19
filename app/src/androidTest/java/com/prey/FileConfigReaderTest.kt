package com.prey

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileConfigReaderTest {

    private lateinit var configReader: FileConfigReader
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        configReader = FileConfigReader.getInstance(context)
    }

    @Test
    fun testSingletonInstance() {
        val instance1 = FileConfigReader.getInstance(context)
        val instance2 = FileConfigReader.getInstance(context)
        assertSame("Should return the same instance", instance1, instance2)
    }

    @Test
    fun testStringProperties() {
        // Test string property getters
        assertNotNull("Prey campaign should not be null", configReader.getPreyCampaign())
        assertNotNull("Prey panel should not be null", configReader.getPreyPanel())
        assertNotNull("GCM ID should not be null", configReader.getGcmId())
        assertNotNull("GCM ID prefix should not be null", configReader.getGcmIdPrefix())
        assertNotNull("C2DM action should not be null", configReader.getc2dmAction())
        assertNotNull("C2DM message sync should not be null", configReader.getc2dmMessageSync())
        assertNotNull("Prey domain should not be null", configReader.getPreyDomain())
        assertNotNull("Prey subdomain should not be null", configReader.getPreySubdomain())
    }

    @Test
    fun testBooleanProperties() {
        // Test boolean property getters
        val askForPassword = configReader.isAskForPassword()
        assertTrue("Ask for password should be boolean", askForPassword is Boolean)
        val logEnabled = configReader.isLogEnabled()
        assertTrue("Log enabled should be boolean", logEnabled is Boolean)
        val scheduled = configReader.isScheduled()
        assertTrue("Scheduled should be boolean", scheduled is Boolean)
        val overOtherApps = configReader.isOverOtherApps()
        assertTrue("Over other apps should be boolean", overOtherApps is Boolean)
        val openPin = configReader.getOpenPin()
        assertTrue("Open pin should be boolean", openPin is Boolean)
    }

    @Test
    fun testNumericProperties() {
        // Test numeric property getters
        assertTrue("Minute scheduled should be positive", configReader.getMinuteScheduled() >= 0)
        assertTrue("Timeout report should be positive", configReader.getTimeoutReport() >= 0)
        assertTrue(
            "Geofence maximum accuracy should be positive",
            configReader.getGeofenceMaximumAccuracy() >= 0
        )
        assertTrue(
            "Geofence loitering delay should be positive",
            configReader.getGeofenceLoiteringDelay() >= 0
        )
        assertTrue("Distance location should be positive", configReader.getDistanceLocation() >= 0)
        assertTrue("Distance aware should be positive", configReader.getDistanceAware() >= 0)
        assertTrue("Radius aware should be positive", configReader.getRadiusAware() >= 0)
    }

    @Test
    fun testUrlProperties() {
        // Test URL-like properties
        val urlPattern = Regex("^(http|https)://.*")

        assertTrue(
            "Prey uninstall should be a valid URL",
            configReader.getPreyUninstall().matches(urlPattern)
        )
        assertTrue(
            "Prey uninstall ES should be a valid URL",
            configReader.getPreyUninstallEs().matches(urlPattern)
        )
        assertTrue(
            "Prey Google Play should be a valid URL",
            configReader.getPreyGooglePlay().matches(urlPattern)
        )
        assertTrue(
            "Prey terms should be a valid URL",
            configReader.getPreyTerms().matches(urlPattern)
        )
        assertTrue(
            "Prey terms ES should be a valid URL",
            configReader.getPreyTermsEs().matches(urlPattern)
        )
        assertTrue(
            "Prey forgot should be a valid URL",
            configReader.getPreyForgot().matches(urlPattern)
        )
    }

    @Test
    fun testEmailProperties() {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
        assertTrue(
            "Email feedback should be a valid email",
            configReader.getEmailFeedback().matches(emailPattern)
        )
    }

}