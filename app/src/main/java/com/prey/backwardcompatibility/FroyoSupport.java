/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backwardcompatibility;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyUtils;
import com.prey.R;
import com.prey.exceptions.PreyException;
import com.prey.exceptions.PreyFirebaseCrashlytics;
import com.prey.json.actions.Lock;
import com.prey.receivers.PreyDeviceAdmin;

@TargetApi(Build.VERSION_CODES.FROYO)
public class FroyoSupport {

    private static FroyoSupport _instance;
    private Context ctx;
    private DevicePolicyManager policyManager;
    ComponentName deviceAdmin;

    public static FroyoSupport getInstance(Context context) {
        if (_instance == null) {
            _instance = new FroyoSupport(context);
        }
        return _instance;
    }

    private FroyoSupport(Context context) {
        policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        deviceAdmin = new ComponentName(context, PreyDeviceAdmin.class);
        ctx = context;
    }

    public void changePasswordAndLock(String newPass, boolean lock) throws PreyException{
        try {
            PreyLogger.d("change0");
            if (isAdminActive()) {
                PreyLogger.d("change1");
                boolean isPatternSet=Lock.isPatternSet(ctx);
                boolean isPassOrPinSet= Lock.isPassOrPinSet(ctx);
                if( !isPatternSet&&!isPassOrPinSet) {
                    try {
                        int length=0;
                        if(newPass!=null){
                            length=newPass.length();
                        }
                        if ("".equals(newPass))
                            android.provider.Settings.System.putInt(ctx.getContentResolver(), android.provider.Settings.System.LOCK_PATTERN_ENABLED, 0);
                        if(length>=4){
                            //This method was deprecated in API level 31.
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                                try {
                                    policyManager.setPasswordMinimumLength(deviceAdmin, 0);
                                    policyManager.setPasswordQuality(deviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                                }catch (SecurityException se){
                                    PreyLogger.e(String.format("SecurityException:%s", se.getMessage()), se);
                                }
                            }
                            //This method was deprecated in API level 30.
                            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                                try {
                                    policyManager.resetPassword(newPass, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                                }catch (SecurityException se){
                                    PreyLogger.e(String.format("SecurityException:%s", se.getMessage()), se);
                                }
                            }
                        }else {
                            if (lock) {
                                lockNow();
                            }
                        }
                    } catch (Exception e1) {
                        if (lock) {
                            lockNow();
                        }
                        PreyLogger.e("locked:" + e1.getMessage(), e1);
                        throw new PreyException("This device couldn't be locked");
                    }
                }
                if (lock){
                    lockNow();
                }
            }
        } catch (Exception e) {
            throw new PreyException("This device couldn't be locked");
        }
    }

    public void lockNow() {
        if (isAdminActive())
            policyManager.lockNow();
    }

    public boolean isAdminActive() {
        if(!PreyUtils.isChromebook(ctx)){
            return policyManager.isAdminActive(deviceAdmin);
        } else{
            return true;
        }
    }

    public void removeAdminPrivileges() {
        policyManager.removeActiveAdmin(deviceAdmin);
    }

    public Intent getAskForAdminPrivilegesIntent() {
        PreyConfig.getPreyConfig(ctx).setSecurityPrivilegesAlreadyPrompted(true);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        return intent;
    }

    /**
     * Performs a factory reset/wipe of the device data.
     * <p>
     * This method attempts to wipe all data from the device using the DevicePolicyManager.
     * It first tries to use the standard {@code wipeData(0)} method. If that fails and the device
     * is running Android 14 (API 34, Upside Down Cake) or later, it will attempt to use
     * the newer {@code wipeDevice(WIPE_RESET_PROTECTION_DATA)} method as a fallback.
     *
     * @throws Exception if the device administrator is not active or if the wipe operation fails.
     *                  The exception message will contain a localized error string explaining
     *                  the reason for the failure.
     * @throws SecurityException if the caller does not have the required permissions to perform
     *                          a device wipe operation.
     *
     * @see DevicePolicyManager#wipeData(int)
     * @see DevicePolicyManager#wipeDevice(int)
     * @see #isAdminActive()
     */
    public void wipe() throws Exception {
        if (isAdminActive())
            try {
                policyManager.wipeData(0);
            } catch (Exception e) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    policyManager.wipeDevice(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
                } else {
                    PreyFirebaseCrashlytics.getInstance(ctx).recordException(e);
                    throw e;
                }
            }
        else
            throw new Exception(ctx.getString(R.string.administrator_disabled));
    }

    public static boolean supportSMS(Context ctx) {
        TelephonyManager telephonyManager1 = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isPhone = !(telephonyManager1.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE);
        return isPhone;
    }

    /**
     * Retrieves the enrollment-specific ID for the device, if available.
     *
     * This method checks if the device is running Android S (API level 31) or later,
     * and attempts to retrieve the enrollment-specific ID using the DevicePolicyManager.
     *
     * @return The enrollment-specific ID, or an empty string if not available.
     */
    public String getEnrollmentSpecificId() {
        // Initialize the ID as an empty string
        String id = "";
        // Check if the device is running Android S (API level 31) or later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // Retrieve the organization ID from the PreyConfig
                String organizationId = PreyConfig.getPreyConfig(ctx).getOrganizationId();
                // Check if the organization ID is not null and not empty
                if (organizationId != null && !"".equals(organizationId)) {
                    // Set the organization ID before attempting to retrieve the enrollment-specific ID
                    policyManager.setOrganizationId(PreyConfig.getPreyConfig(ctx).getOrganizationId());
                    // Attempt to retrieve the enrollment-specific ID using the DevicePolicyManager
                    id = policyManager.getEnrollmentSpecificId();
                }
            } catch (Exception e) {
                // Log any exceptions that occur during the retrieval process
                PreyLogger.e("Failed to get enrollment specific ID", e);
            }
        }
        // Return the retrieved ID, or an empty string if not available
        return id;
    }

}