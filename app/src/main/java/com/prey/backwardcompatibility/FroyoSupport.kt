/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.backwardcompatibility

import android.annotation.TargetApi
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.prey.exceptions.PreyException
import com.prey.json.actions.Lock
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.beta.actions.PreyBetaActionsRunner
import com.prey.receivers.PreyDeviceAdmin

@TargetApi(Build.VERSION_CODES.FROYO)
class FroyoSupport private constructor(val context: Context) {

    private val policyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    var deviceAdmin: ComponentName = ComponentName(context, PreyDeviceAdmin::class.java)

    @Throws(PreyException::class)
    fun changePasswordAndLock(newPass: String?, lock: Boolean) {
        try {
            PreyLogger.d("change0")
            if (isAdminActive()) {
                PreyLogger.d("change1")
                val isPatternSet = Lock().isPatternSet(context)
                val isPassOrPinSet = Lock().isPassOrPinSet(context)
                if (!isPatternSet && !isPassOrPinSet) {
                    try {
                        var length = 0
                        if (newPass != null) {
                            length = newPass.length
                        }
                        if ("" == newPass) Settings.System.putInt(
                            context.contentResolver,
                            Settings.System.LOCK_PATTERN_ENABLED,
                            0
                        )
                        if (length >= 4) {
                            policyManager.setPasswordMinimumLength(deviceAdmin, 0)
                            policyManager.setPasswordQuality(
                                deviceAdmin,
                                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED
                            )
                            policyManager.resetPassword(
                                newPass,
                                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY
                            )
                        } else {
                            if (lock) {
                                lockNow()
                            }
                        }
                    } catch (e1: Exception) {
                        if (lock) {
                            lockNow()
                        }
                        PreyLogger.e("locked:" + e1.message, e1)
                        throw PreyException("This device couldn't be locked")
                    }
                }
                if (lock) {
                    lockNow()
                }
            }
        } catch (e: Exception) {
            throw PreyException("This device couldn't be locked")
        }
    }

    fun lockNow() {
        if (isAdminActive()) policyManager.lockNow()
    }

    fun isAdminActive(): Boolean  = if (!PreyUtils.isChromebook(context)) {
            policyManager.isAdminActive(deviceAdmin)
        } else {
            true
        }

    fun removeAdminPrivileges() {
        policyManager.removeActiveAdmin(deviceAdmin)
    }

    fun getAskForAdminPrivilegesIntent(): Intent {
        PreyConfig.getInstance(context).setSecurityPrivilegesAlreadyPrompted(true)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        return intent
    }

    fun wipe() {
        if (isAdminActive()) policyManager.wipeData(0)
    }

    /**
     * Retrieves the enrollment-specific ID for the device, if available.
     *
     * This method checks if the device is running Android S (API level 31) or later,
     * and attempts to retrieve the enrollment-specific ID using the DevicePolicyManager.
     *
     * @return The enrollment-specific ID, or an empty string if not available.
     */
    fun getEnrollmentSpecificId(): String {
        // Initialize the ID as an empty string
        var id = ""
        // Check if the device is running Android S (API level 31) or later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                // Retrieve the organization ID from the PreyConfig
                val organizationId = PreyConfig.getInstance(context).getOrganizationId()
                // Check if the organization ID is not null and not empty
                if (organizationId != null && "" != organizationId) {
                    // Set the organization ID before attempting to retrieve the enrollment-specific ID
                    policyManager.setOrganizationId(
                        PreyConfig.getInstance(context).getOrganizationId()!!
                    )
                    // Attempt to retrieve the enrollment-specific ID using the DevicePolicyManager
                    id = policyManager.enrollmentSpecificId
                }
            } catch (e: Exception) {
                // Log any exceptions that occur during the retrieval process
                PreyLogger.e("Failed to get enrollment specific ID", e)
            }
        }
        // Return the retrieved ID, or an empty string if not available
        return id
    }

    companion object {
        private var instance: FroyoSupport? = null
        fun getInstance(context: Context): FroyoSupport =
            instance ?: FroyoSupport(context).also { instance = it }
    }
}