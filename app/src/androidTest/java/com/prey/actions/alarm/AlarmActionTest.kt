package com.prey.actions.alarm

import android.content.Context
import android.media.AudioManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4

import com.prey.PreyConfig
import com.prey.PreyStatus
import com.prey.net.TestWebServices

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmActionTest {
    private lateinit var alarmAction: AlarmAction
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        PreyConfig.getInstance(context)
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        alarmAction = AlarmAction()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Test
    fun testAlarmThreadWithAlarmSound() {
        // Given
        val soundType = "alarm"
        val messageId = "123"
        val jobId = "456"
        PreyStatus.getInstance().setStateOfAlarm(PreyStatus.AlarmState.BEGIN)
        // When
        alarmAction.start(context, messageId, jobId, soundType)
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
            Assert.assertEquals(command, "stop")
            Assert.assertEquals(target, "alarm")
            Assert.assertEquals(status, "stopped")
        }
        // Then
        Assert.assertEquals(
            PreyStatus.getInstance().getStateOfAlarm(),
            PreyStatus.AlarmState.FINISH
        )
    }

}