/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2024 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.os.Bundle;

import com.prey.PreyConfig;
import com.prey.PreyLogger;

/**
 * BroadcastReceiver that listens for changes in application restrictions.
 */
public class RestrictionsReceiver extends BroadcastReceiver {

    /**
     * Called when the BroadcastReceiver receives an Intent.
     *
     * @param context The Context in which the Intent is being received.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the Intent action is for application restrictions changed
        if (Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED.equals(intent.getAction())) {
            // Get the RestrictionsManager instance
            RestrictionsManager manager = (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
            // Get the application restrictions
            Bundle applicationRestrictions = manager.getApplicationRestrictions();
            // Check if application restrictions are not null
            if (applicationRestrictions != null) {
                // Log the application restrictions
                PreyLogger.d(String.format("RestrictionsReceiver restrictions applied: %s", applicationRestrictions.toString()));
                // Handle the application restrictions
                handleApplicationRestrictions(context, applicationRestrictions);
            } else {
                // Log if no application restrictions are found
                PreyLogger.d("RestrictionsReceiver no restrictions found");
            }
        }
    }

    /**
     * Handles the application restrictions.
     *
     * @param context      The Context in which the restrictions are being handled.
     * @param restrictions The Bundle containing the application restrictions.
     */
    public static void handleApplicationRestrictions(Context context, Bundle restrictions) {
        // Check if the device is already registered with Prey
        if (!PreyConfig.getPreyConfig(context).isThisDeviceAlreadyRegisteredWithPrey()) {
            if (restrictions != null && restrictions.containsKey("enterprise_name")) {
                // Retrieve the enterprise name from the restrictions bundle
                String enterpriseName = restrictions.getString("enterprise_name");
                // Check if the enterprise name is not null and not empty
                if (enterpriseName != null && !"".equals(enterpriseName)) {
                    // Set the organization ID in the Prey configuration
                    PreyConfig.getPreyConfig(context).setOrganizationId(enterpriseName);
                }
            }
            // Check if the restrictions bundle is not null and contains the "setup_key"
            if (restrictions != null && restrictions.containsKey("setup_key")) {
                // Get the setup key from the restrictions bundle
                String setupKey = restrictions.getString("setup_key");
                // Check if the setup key is not null and not empty
                if (setupKey != null && !"".equals(setupKey)) {
                    try {
                        // Attempt to register a new device with the setup key
                        PreyConfig.getPreyConfig(context).registerNewDeviceWithApiKey(setupKey);
                    } catch (Exception e) {
                        // Log any errors that occur during registration
                        PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                    }
                }
            }
        }
    }

}