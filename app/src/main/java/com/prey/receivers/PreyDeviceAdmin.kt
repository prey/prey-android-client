/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

import com.prey.R

/**
 * This class represents a device administrator receiver.
 * It extends the DeviceAdminReceiver class and overrides the onDisableRequested method.
 */
class PreyDeviceAdmin : DeviceAdminReceiver() {

    /**
     * This method is called when the device administrator is disabled.
     * It returns a CharSequence that will be displayed to the user as a dialog message.
     *
     * @param context The context in which the receiver is running.
     * @param intent The intent that triggered the receiver.
     * @return The dialog message to be displayed.
     */
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence? {
        return context.getText(R.string.preferences_admin_enabled_dialog_message).toString()
    }
}