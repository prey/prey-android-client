package com.prey.json.actions

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.actions.observer.ActionResult
import com.prey.net.PreyWebServices
import com.prey.net.TestWebServices

import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class AlarmTest {

    private lateinit var context: Context
    private lateinit var preyWebServices: PreyWebServices
    private lateinit var alarm: Alarm
    private lateinit var parameters: JSONObject
    private lateinit var actionResults: List<ActionResult?>

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        val apikey = "A123"
        val deviceId = "d456"
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setApiKey(apikey)
        PreyConfig.getInstance(context).setDeviceId(deviceId)
        alarm = Alarm()
        actionResults = emptyList()
        parameters = JSONObject()
    }

    @Test
    fun testStartWithValidParameters() {
        // Arrange
        parameters.put("sound", "validSound")
        parameters.put(PreyConfig.MESSAGE_ID, "validMessageId")
        parameters.put(PreyConfig.JOB_ID, "validJobId")
        // Act
        alarm.start(context, actionResults, parameters)
        // Assert
        // Verify AlarmThread is started with correct parameters
        // You can use Mockito.verify here to check if AlarmThread.start() is called with correct parameters
    }

    @Test
    fun testStartWithInvalidSoundParameter() {
        // Arrange
        // parameters.put("sound")}throws(Exception())
        // Act and Assert
        alarm.start(context, actionResults, parameters)
        // Verify PreyLogger.e is called with correct error message
        // You can use Mockito.verify here to check if PreyLogger.e is called with correct error message
    }

    @Test
    fun testStartWithInvalidMessageIDParameter() {
        // Arrange
        //every{parameters.getString(PreyConfig.MESSAGE_ID)}throws(Exception())
        // Act and Assert
        alarm.start(context, actionResults, parameters)
        // Verify PreyLogger.e is called with correct error message
        // You can use Mockito.verify here to check if PreyLogger.e is called with correct error message
    }

}