package com.prey.actions.alert

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prey.PreyConfig
import com.prey.net.TestWebServices
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertActionTest {
    private lateinit var alertAction: AlertAction
    private lateinit var context: Context
    private lateinit var mockNotificationManager: NotificationManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        alertAction = AlertAction()
        mockNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    }

    @Test
    fun test_start_with_fullscreen_notification_on_newer_Android() {
        // Arrange
        val description = "Test Alert"
        val messageId = "123"
        val jobId = "456"
        PreyConfig.getInstance(context).setNextNotificationId(1000)
        // Act
        alertAction.start(context, messageId, jobId, description, true)
        val webServices = PreyConfig.getInstance(context).getWebServices() as TestWebServices
        val parameters = webServices.getNotificationParameters()
        // Verify
        Assert.assertNotNull(parameters)
        if (!parameters.isNullOrEmpty()) {
            val correlationId = parameters.get("correlationId")
            val command = parameters.get("command")
            val target = parameters.get("target")
            val status = parameters.get("status")
            Assert.assertEquals(messageId, correlationId)
            Assert.assertEquals(command, "start")
            Assert.assertEquals(target, "alert")
            Assert.assertEquals(status, "started")
        }
    }

    @Test
    fun test_start_with_notification_only_on_newer_Android() {
        // Arrange
        val description = "Test Alert"
        val messageId = "123"
        val jobId = "456"
        PreyConfig.getInstance(context).setNextNotificationId(1000)
        // Act
        alertAction.start(context, messageId, jobId, description, false)

        // Verify
        val nextNotificationId = PreyConfig.getInstance(context).getNextNotificationId();
        // Verify
        Assert.assertEquals(nextNotificationId, 1001)
    }

}