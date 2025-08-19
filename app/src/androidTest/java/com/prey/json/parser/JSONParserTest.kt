package com.prey.json.parser

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class JSONParserTest {

    private lateinit var jsonParser: JSONParser
    private lateinit var context: Context

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
        val apikey = "A123"
        val deviceId = "d456"
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        PreyConfig.getInstance(context).setApiKey(apikey)
        PreyConfig.getInstance(context).setDeviceId(deviceId)
        jsonParser = JSONParser()
    }

    @Test
    fun testGetJSONFromTxt_InvalidDataReceived_ReturnsNull() {
        val jsonString = "Invalid data received"
        val result = JSONParser().getJSONFromTxt(jsonString)
        Assert.assertNull(result)
    }

    @Test
    fun testGetJSONFromTxt_NullArray_ReturnsNull() {
        val jsonString = "[null]"
        val result = JSONParser().getJSONFromTxt(jsonString)
        Assert.assertNull(result)
    }

    @Test
    fun testGetJSONFromTxt_EmptyString_ReturnsNull() {
        val jsonString = ""
        val result = JSONParser().getJSONFromTxt(jsonString)
        Assert.assertNull(result)
    }

    @Test
    fun testGetJSONFromTxt_ValidJsonString_ReturnsListOfJsonObjects() {
        val jsonString = "[{\"key\":\"value\"},{\"key2\":\"value2\"}]"
        val result = JSONParser().getJSONFromTxt(jsonString)
        Assert.assertNotNull(result)
        Assert.assertEquals(2, result!!.size)
    }

    @Test
    fun testGetJSONFromTxt_InvalidJsonString_ReturnsNull() {
        val jsonString = "Invalid JSON"
        val result = JSONParser().getJSONFromTxt(jsonString)
        Assert.assertNull(result)
    }

}