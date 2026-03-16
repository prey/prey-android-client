/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlertTest {

    private lateinit var context: Context
    private lateinit var alert: Alert

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        alert = Alert()
    }

    @Test(expected = IllegalArgumentException::class)
    fun execute_invalidCommand_shouldThrowException() {
        val options = JSONObject()
        alert.execute(context, "invalid_command", options)
    }

    @Test
    fun notification_shouldCreateNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1234
        val message = "Test notification message"
        alert.createNotification(
            context,
            message,
            notificationId,
            "msg1",
            null
        )
        assertNotNull(notificationManager)
    }

    @Test
    fun notification_shouldBeVisible() {
        val notificationId = 777
        alert.createNotification(
            context,
            "Alert message test",
            notificationId,
            "messageId",
            null
        )
        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val active = manager.activeNotifications
        assertTrue(active.any { it.id == notificationId })
    }

    @Test
    fun notificationChannel_shouldExist() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        alert.createNotification(context, "msg", 1, "msg1", null)
        val channel = notificationManager.getNotificationChannel(Alert.CHANNEL_ALERT_ID)
        assertNotNull(channel)
    }

}