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
import com.prey.mdm.MdmDebugReporter;
import com.prey.mdm.MdmKeyedAppStateReporter;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> info = new HashMap<>();
        info.put("action", intent != null ? intent.getAction() : "null");
        MdmDebugReporter.send(context, "restrictions_onreceive", info);
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
                // Persist managed config values AND auto-register with the
                // backend if a setup_key was delivered. This allows the AMAPI
                // FORCE_INSTALLED + no-setupActions flow: the app installs,
                // the wizard completes naturally, and registration happens
                // background-side as soon as managed config arrives — no UI
                // step required.
                handleApplicationRestrictions(context, applicationRestrictions);
            } else {
                // Log if no application restrictions are found
                PreyLogger.d("RestrictionsReceiver no restrictions found");
            }
        }
    }

    /**
     * Stores MDM restriction values into PreyConfig.
     * These values are read always, even if the device is already registered.
     *
     * @param context      The Context in which the restrictions are being handled.
     * @param restrictions The Bundle containing the application restrictions.
     */
    public static void saveRestrictionValues(Context context, Bundle restrictions) {
        if (restrictions == null) {
            return;
        }
        PreyConfig preyConfig = PreyConfig.getPreyConfig(context);

        PreyLogger.d(String.format("saveRestrictionValues restrictions: %s", restrictions.toString()));
        saveStringRestriction(restrictions, "enterprise_name", value -> preyConfig.setMdmOrganizationId(value));
        saveStringRestriction(restrictions, "serial_number", value -> preyConfig.setMdmSerialNumber(value));
        saveStringRestriction(restrictions, "device_name", value -> preyConfig.setMdmDeviceName(value));
        saveStringRestriction(restrictions, "imei", value -> preyConfig.setMdmImei(value));

        if (restrictions.containsKey("skip_manual_permissions")) {
            boolean skip = restrictions.getBoolean("skip_manual_permissions", false);
            PreyLogger.d(String.format("saveRestrictionValues skip_manual_permissions: %s", skip));
            preyConfig.setMdmSkipManualPermissions(skip);
        }
    }

    /**
     * Reads a string restriction from the bundle and applies it if non-empty.
     *
     * @param restrictions The Bundle containing the application restrictions.
     * @param key          The restriction key to read.
     * @param setter       The consumer to apply the value.
     */
    private static void saveStringRestriction(Bundle restrictions, String key, java.util.function.Consumer<String> setter) {
        if (restrictions.containsKey(key)) {
            String value = restrictions.getString(key);
    
            PreyLogger.d(String.format("saveStringRestriction %s: %s", key, value));
            if (value != null && !"".equals(value)) {
                setter.accept(value);
            }
        }
    }

    /**
     * Returns the setup_key from the restrictions bundle if available and the device is not registered.
     *
     * @param context      The Context.
     * @param restrictions The Bundle containing the application restrictions.
     * @return The setup key, or null if not available or device is already registered.
     */
    public static String getSetupKey(Context context, Bundle restrictions) {
        if (restrictions == null) {
            return null;
        }
        boolean registered = PreyConfig.getPreyConfig(context).isThisDeviceAlreadyRegisteredWithPrey();

        PreyLogger.d(String.format("getSetupKey registered: %s", registered));
        if (!registered && restrictions.containsKey("setup_key")) {
            String setupKey = restrictions.getString("setup_key");
            PreyLogger.d(String.format("getSetupKey setup_key: %s", setupKey));
            if (setupKey != null && !"".equals(setupKey)) {
                return setupKey;
            }
        }
        return null;
    }

    /**
     * Handles the application restrictions: saves values and registers device if needed.
     *
     * @param context      The Context in which the restrictions are being handled.
     * @param restrictions The Bundle containing the application restrictions.
     */
    public static void handleApplicationRestrictions(Context context, Bundle restrictions) {
        Map<String, Object> info = new HashMap<>();
        info.put("has_setup_key", restrictions != null && restrictions.containsKey("setup_key"));
        info.put("registered_before", PreyConfig.getPreyConfig(context).isThisDeviceAlreadyRegisteredWithPrey());
        MdmDebugReporter.send(context, "handle_restrictions_enter", info);
        saveRestrictionValues(context, restrictions);
        String setupKey = getSetupKey(context, restrictions);
        if (setupKey != null) {
            MdmDebugReporter.send(context, "calling_registerNewDeviceWithApiKey");
            try {
                PreyConfig.getPreyConfig(context).registerNewDeviceWithApiKey(setupKey);
                MdmDebugReporter.send(context, "register_returned");
            } catch (Exception e) {
                PreyLogger.e(String.format("Error:%s", e.getMessage()), e);
                Map<String, Object> err = new HashMap<>();
                err.put("error", e.getMessage());
                MdmDebugReporter.send(context, "register_exception", err);
            }
        }
        boolean afterRegistered = PreyConfig.getPreyConfig(context).isThisDeviceAlreadyRegisteredWithPrey();
        Map<String, Object> exitInfo = new HashMap<>();
        exitInfo.put("registered_after", afterRegistered);
        MdmDebugReporter.send(context, "handle_restrictions_exit", exitInfo);
        // Emit mdm_setup=linked when the device is confirmed registered.
        if (afterRegistered) {
            MdmKeyedAppStateReporter.reportSetupLinked(context);
        }
    }

}
