package com.prey.services

import android.content.Context
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.net.TestWebServices

import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class MyFirebaseMessagingServiceTest {

    private lateinit var context: Context
    private val myFirebaseMessagingService = MyFirebaseMessagingService()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_onNewToken() {
        PreyConfig.getInstance(context).setApikey("A111")
        PreyConfig.getInstance(context).setDeviceId("D111")
        PreyConfig.getInstance(context).setNotificationId("")
        PreyConfig.getInstance(context).setWebServices(TestWebServices())
        myFirebaseMessagingService.sendToken(context, "token")
        val notificationId = PreyConfig.getInstance(context).getNotificationId()
        assertNotNull(notificationId)
    }

}