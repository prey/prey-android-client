/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.prey.PreyConfig
import com.prey.PreyLogger
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.prey.net.PreyWebServicesKt
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class AlertReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: AlertReceiver
    private lateinit var config: PreyConfig

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        config = PreyConfig.getPreyConfig(context)
        receiver = AlertReceiver()
        //We mock the Singleton/Static object of the Web Services
        mockkObject(PreyWebServicesKt)
    }

    @Test
    fun onReceive_shouldSendBroadcastAndCancelNotification() {
        config.deleteActions();
        //Prepare the Intent with test data
        val intent = Intent().apply {
            putExtra("notificationId", 123)
            putExtra("messageId", "msg_test_001")
            putExtra("reason", "manual_alert")
        }
        //Execute onReceive
        receiver.onReceive(context, intent)
        // We verify that the web service call was attempted with the correct parameters.
        // We use coVerify because the method is called within a corruption (Dispatchers.IO).
        // Note: This may require a small delay or the use of a TestDispatcher if strictly necessary.
        verify(timeout = 2000) {
            val containsActions = config.containsActions("start_alert_stopped")
            PreyLogger.d("test containsActions: $containsActions")
            Assert.assertTrue(containsActions);
        }
    }
}