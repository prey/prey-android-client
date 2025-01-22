/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers.kotlin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.RestrictionsManager
import android.os.Bundle
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

/**
 * BroadcastReceiver that listens for changes in application restrictions.
 */
class RestrictionsReceiver : BroadcastReceiver() {
    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the Intent is being received.
     * @param intent  The Intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the Intent action is for application restrictions changed
        if (Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED == intent.action) {
            // Get the RestrictionsManager instance
            val manager =
                context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
            // Get the application restrictions
            val applicationRestrictions = manager.applicationRestrictions
            // Check if application restrictions are not null
            if (applicationRestrictions != null) {
                // Log the application restrictions
                PreyLogger.d(
                    String.format(
                        "RestrictionsReceiver restrictions applied: %s",
                        applicationRestrictions.toString()
                    )
                )
                // Handle the application restrictions
                handleApplicationRestrictions(context, applicationRestrictions)
            } else {
                // Log if no application restrictions are found
                PreyLogger.d("RestrictionsReceiver no restrictions found")
            }
        }
    }

    companion object {
        /**
         * Handles the application restrictions.
         *
         * @param context      The Context in which the restrictions are being handled.
         * @param restrictions The Bundle containing the application restrictions.
         */
        fun handleApplicationRestrictions(context: Context, restrictions: Bundle?) {
            // Check if the device is already registered with Prey
            if (!PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()) {
                if (restrictions != null && restrictions.containsKey("enterprise_name")) {
                    // Retrieve the enterprise name from the restrictions bundle
                    val enterpriseName = restrictions.getString("enterprise_name")
                    // Check if the enterprise name is not null and not empty
                    if (enterpriseName != null && "" != enterpriseName) {
                        // Set the organization ID in the Prey configuration
                        PreyConfig.getInstance(context).setOrganizationId (enterpriseName)
                    }
                }
                // Check if the restrictions bundle is not null and contains the "setup_key"
                if (restrictions != null && restrictions.containsKey("setup_key")) {
                    // Get the setup key from the restrictions bundle
                    val setupKey = restrictions.getString("setup_key")
                    // Check if the setup key is not null and not empty
                    if (setupKey != null && "" != setupKey) {
                        try {
                            // Attempt to register a new device with the setup key
                            PreyConfig.getInstance(context).registerNewDeviceWithApiKey(setupKey)
                        } catch (e: Exception) {
                            // Log any errors that occur during registration
                            PreyLogger.e(String.format("Error:%s", e.message), e)
                        }
                    }
                }
            }
        }
    }
}