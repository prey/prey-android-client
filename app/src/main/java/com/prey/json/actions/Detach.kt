/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.fileretrieval.FileretrievalController
import com.prey.actions.report.ReportScheduled
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServices
import com.prey.preferences.RunBackgroundCheckBoxPreference
import org.json.JSONObject

/**
 * Command target for handling the detachment of a device from the Prey service.
 *
 * This class processes the "detach" command received from the backend. Detaching a device involves
 * a comprehensive cleanup process, which includes:
 * - Resetting various configuration flags (e.g., protection settings, prompted statuses).
 * - Optionally removing administrative privileges and clearing cached data.
 * - Deleting scheduled reports and retrieved files.
 * - Clearing sensitive user and device identifiers (email, device ID, API key) from local storage.
 * - Notifying the Prey web services that the device is being deleted.
 * - Finally, redirecting the user to the login/setup screen.
 *
 * The detachment can be triggered in two main scenarios:
 * 1. A standard detachment initiated by the user, which performs a full cleanup.
 * 2. An "expired" detachment, typically when a subscription ends, which performs a partial cleanup
 *    but marks the installation as deleted.
 */
object Detach : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "start" -> start(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Initiates the device detachment process based on the provided options.
     *
     * This function serves as the entry point for the "detach" command. It inspects the `options`
     * to determine the type of detachment.
     *
     * - If the `expired` flag is `true` in the `options`, it signifies a detachment due to an expired
     *   subscription. In this case, the installation status is marked as deleted ("DEL"), and a
     *   partial cleanup is performed (keeping user data but disabling most features).
     * - Otherwise, it triggers a standard, full detachment, which includes removing permissions and
     *   clearing all cached data.
     *
     * @param context The application context.
     * @param options A [JSONObject] containing command options. It may contain a boolean "expired" key.
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Detach start options:${options}")
        var expired = false
        try {
            expired = options.getBoolean("expired")
        } catch (e: Exception) {

        }
        if (expired) {
            PreyConfig.getPreyConfig(context).setInstallationStatus("DEL")
            PreyLogger.d("Detach expired:${expired}")
            detachDevice(context, false, false, expired)
        } else {
            detachDevice(context, true, true, false)
        }
    }

    fun detachDevice(
        context: Context,
        removePermissions: Boolean,
        removeCache: Boolean,
        expired: Boolean
    ): String? {
        PreyLogger.d("Detach detachDevice")
        var error: String? = null
        try {
            PreyConfig.getPreyConfig(context).setSecurityPrivilegesAlreadyPrompted(false)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("1:${error}")
        try {
            PreyConfig.getPreyConfig(context).setProtectAccount(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getPreyConfig(context).setProtectPrivileges(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getPreyConfig(context).setProtectTour(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getPreyConfig(context).setProtectReady(false)
        } catch (e: Exception) {
            error += e.message
        }
        PreyLogger.d("2:${error}")
        try {
            if (removePermissions) {
                val fSupport = FroyoSupport.getInstance(context)
                if (fSupport.isAdminActive()) {
                    fSupport.removeAdminPrivileges()
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        try {
            RunBackgroundCheckBoxPreference.notifyCancel(context)
            PreyConfig.getPreyConfig(context).removeLocationAware()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("3:${error}")
        try {
            PreyConfig.getPreyConfig(context).setAware(false)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("4:${error}")
        try {
            FileretrievalController.getInstance().deleteAll(context)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyConfig.getPreyConfig(context).setPrefsBiometric(false)
        PreyLogger.d("5:${error}")
        try {
            ReportScheduled.getInstance(context).reset()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyWebServices.getInstance().deleteDevice(context)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("6:${error}")
        if (removeCache) {
            try {
                PreyConfig.getPreyConfig(context).wipeData()
            } catch (e: Exception) {
                error += e.message
            }
        }
        try {
            PreyConfig.getPreyConfig(context).removeDeviceId()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getPreyConfig(context).removeEmail()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getPreyConfig(context).removeApiKey()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        try {
            PreyConfig.getPreyConfig(context).setPinNumber("")
        } catch (e: Exception) {
            error = e.message
        }
        try {
            PreyConfig.getPreyConfig(context).setEmail("")
        } catch (e: Exception) {
            error = e.message
        }
        PreyLogger.d("7:${error}")
        try {
            PreyConfig.getPreyConfig(context).setDeviceId("")
        } catch (e: Exception) {
            error = e.message
        }
        try {
            PreyConfig.getPreyConfig(context).setApiKey("")
        } catch (e: Exception) {
            error = e.message
        }
        PreyLogger.d("8:${error}")
        if (!expired) {
            try {
                PreyConfig.getPreyConfig(context).setInstallationStatus("")
            } catch (e: Exception) {
                error = e.message
            }
        }
        val email = PreyConfig.getPreyConfig(context).email
        val deviceId = PreyConfig.getPreyConfig(context).deviceId
        val apiKey = PreyConfig.getPreyConfig(context).apiKey
        PreyLogger.d("Email:${email}")
        PreyLogger.d("DeviceId:${deviceId}")
        PreyLogger.d("ApiKey:${apiKey}")
        if (removeCache) {
            try {
                PreyConfig.deleteCacheInstance(context)
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
        try {
            val bundle = Bundle()
            bundle.putString("nexturl", "loadUrl")
            val intent: Intent = Intent(context, CheckPasswordHtmlActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtras(bundle)
            context.startActivity(intent)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return error
    }
}