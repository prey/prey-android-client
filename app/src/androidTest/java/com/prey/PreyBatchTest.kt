package com.prey

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreyBatchTest {

    private lateinit var context: Context
    private lateinit var preyBatch: PreyBatch

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        preyBatch = PreyBatch.getInstance(context)
    }

    @Test
    fun testSingletonInstance() {
        val instance1 = PreyBatch.getInstance(context)
        val instance2 = PreyBatch.getInstance(context)
        assertSame("Singleton instances should be the same", instance1, instance2)
    }

    @Test
    fun testGetApiKeyBatch() {
        val apiKey = preyBatch.getApiKeyBatch()
        assertNotNull("API key should not be null", apiKey)
    }

    @Test
    fun testGetEmailBatch() {
        val email = preyBatch.getEmailBatch()
        assertNotNull("Email should not be null", email)
    }

    @Test
    fun testIsAskForNameBatch() {
        val askForName = preyBatch.isAskForNameBatch()
        // This is a boolean value, so we just verify it can be retrieved without exception
        // The actual value depends on your batch configuration
        assertNotNull("Ask for name flag should not be null", askForName)
    }

    @Test
    fun testGetToken() {
        val token = preyBatch.getToken()
        assertNotNull("Token should not be null", token)
    }

    @Test
    fun testIsThereBatchInstallationKey() {
        val hasInstallationKey = preyBatch.isThereBatchInstallationKey()
        // Test both the positive and negative cases
        if (preyBatch.getApiKeyBatch().isNotEmpty()) {
            assertTrue("Should return true when API key is present", hasInstallationKey)
        } else {
            assertFalse("Should return false when API key is empty", hasInstallationKey)
        }
    }

    @Test
    fun testPropertiesLoading() {
        // Verify that all essential properties are loaded
        assertNotEquals("", preyBatch.getApiKeyBatch(), "API key should not be empty")
        assertNotEquals("", preyBatch.getEmailBatch(), "Email should not be empty")
        assertNotEquals("", preyBatch.getToken(), "Token should not be empty")
    }

}