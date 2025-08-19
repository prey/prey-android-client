package com.prey.receivers

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider

import com.prey.PreyConfig
import com.prey.R

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PreyDeviceAdminReceiverTest {

    private lateinit var context: Context
    private val preyDeviceAdminReceiver = PreyDeviceAdminReceiver()

    @Before
    fun onBefore() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun test_onDisableRequested() {
        PreyConfig.getInstance(context).setDisablePowerOptions(true)
        val intent = Intent()
        val disableRequested = preyDeviceAdminReceiver.onDisableRequested(context, intent)
        assertEquals(
            disableRequested,
            context.getText(R.string.preferences_admin_enabled_dialog_message).toString()
        )
    }

}