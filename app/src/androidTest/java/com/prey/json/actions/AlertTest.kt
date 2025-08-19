package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.actions.observer.ActionResult
import com.prey.net.TestWebServices

import io.mockk.every
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class AlertTest {

    private lateinit var context: Context
    private lateinit var alert: Alert
    private lateinit var parameters: JSONObject
    private lateinit var actionResults: List<ActionResult?>

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        context = ApplicationProvider.getApplicationContext()
        val apikey = "A123"
        val deviceId = "d456"
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setApiKey(apikey)
        PreyConfig.getInstance(context).setDeviceId(deviceId)
        alert = Alert()
        parameters = JSONObject()
    }

    @Test
    fun testStartWithValidParameters() {
        // Arrange
        every { parameters.getString("alert_message") } returns ("Test Alert Message")
        every { parameters.getString(PreyConfig.MESSAGE_ID) } returns ("Test Message ID")
        every { parameters.getString(PreyConfig.JOB_ID) } returns ("Test Job ID")
        every { parameters.getBoolean("fullscreen_notification") } returns (true)
        // Act
        alert.start(context, actionResults, parameters)
        // Assert
        // Verify that startAlert is called with the correct parameters
        // You can use Mockito.verify for this
    }

    @Test
    fun testStartWithInvalidAlertMessageParameter() {
        // Arrange
        every { parameters.getString("alert_message") }.throws(Exception("Test Exception"))
        // Act and Assert
        // Verify that the error is logged
        // You can use Mockito.verify for this
    }

    @Test
    fun testStartWithInvalidMessageIDParameter() {
        // Arrange
        every { parameters.getString(PreyConfig.MESSAGE_ID) }.throws(Exception("Test Exception"))
        // Act and Assert
        // Verify that the error is logged
        // You can use Mockito.verify for this
    }

}