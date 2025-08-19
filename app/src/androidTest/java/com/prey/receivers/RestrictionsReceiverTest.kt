package com.prey.receivers

import android.content.Context
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.TestWebServices

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RestrictionsReceiverTest {

    private lateinit var context: Context
    private val restrictionsReceiver = RestrictionsReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testHandleApplicationRestrictions_withValidEnterpriseName() {
        val enterpriseName = "TestEnterprise"
        val restrictions = Bundle().apply {
            putString("enterprise_name", enterpriseName)
        }
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setOrganizationId("")
        restrictionsReceiver.handleApplicationRestrictions(context, restrictions)
        assertEquals(enterpriseName, PreyConfig.getInstance(context).getOrganizationId())
    }

    @Test
    fun testHandleApplicationRestrictions_withValidSetupKey() {
        val setupKey = "A1234"
        val enterpriseName = "Test Enterprise"
        val restrictions = Bundle().apply {
            putString("setup_key", setupKey)
            putString("enterprise_name", enterpriseName)
        }
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setOrganizationId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        restrictionsReceiver.handleApplicationRestrictions(context, restrictions)
        assertEquals(enterpriseName, PreyConfig.getInstance(context).getOrganizationId())
        assertEquals(setupKey, PreyConfig.getInstance(context).getApiKey())
    }

    @Test
    fun testHandleApplicationRestrictions_withNoRestrictions() {
        val restrictions = Bundle() // empty
        PreyConfig.getInstance(context).setDeviceId("")
        PreyConfig.getInstance(context).setApikey("")
        PreyConfig.getInstance(context).setOrganizationId("")
        restrictionsReceiver.handleApplicationRestrictions(context, restrictions)
        PreyLogger.i("organizationId:${PreyConfig.getInstance(context).getOrganizationId()}")
        assertEquals("", PreyConfig.getInstance(context).getOrganizationId())
        assertEquals("", PreyConfig.getInstance(context).getApiKey())
    }

}