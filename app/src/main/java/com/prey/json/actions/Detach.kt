/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.content.Intent
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
object Detach : CommandTarget, BaseAction() {

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> start(context, options)
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
        val expired = options.optBoolean("expired", false)
        PreyLogger.d("Detach expired:${expired}")
        if (expired) {
            PreyConfig.getPreyConfig(context).installationStatus = "DEL"
            detachDevice(context, removePermissions = false, removeCache = false, expired = true)
        } else {
            detachDevice(context, removePermissions = true, removeCache = true, expired = false)
        }
    }

    /**
     * Executes the core logic to detach the device from the Prey service and perform local cleanup.
     *
     * This method orchestrates several cleanup tasks:
     * 1. Resets security and privacy configuration flags.
     * 2. Revokes device administrator privileges if requested.
     * 3. Stops background services, location tracking, and clears scheduled reports/retrieved files.
     */
    fun detachDevice(
        context: Context,
        removePermissions: Boolean,
        removeCache: Boolean,
        expired: Boolean
    ): String? {
        PreyLogger.d("Detach detachDevice initiated")
        val errors = mutableListOf<String>()
        val config = PreyConfig.getPreyConfig(context)
        //Reset Security and Privacy Flags
        with(config) {
            protectAccount = false
            protectPrivileges = false
            protectTour = false
            protectReady = false
            aware = false
            prefsBiometric = false
        }
        //Administrator Permissions Management
        if (removePermissions) {
            runCatching {
                val fSupport = FroyoSupport.getInstance(context)
                if (fSupport.isAdminActive) {
                    fSupport.removeAdminPrivileges()
                }
            }.onFailure { e ->
                PreyLogger.e("Error removing admin privileges: ${e.message}", e)
                errors.add("AdminPrivileges: ${e.message}")
            }
        }
        //Cleaning Services and Background
        runCatching {
            RunBackgroundCheckBoxPreference.notifyCancel(context)
            config.removeLocationAware()
            FileretrievalController.getInstance().deleteAll(context)
            ReportScheduled.getInstance(context).reset()
        }.onFailure { PreyLogger.d("Error cleaning services: ${it.message}") }
        //Notify the Server (Remote Deletion)
        runCatching {
            PreyWebServices.getInstance().deleteDevice(context)
        }.onFailure { PreyLogger.d("Error deleting device from web services: ${it.message}") }
        //Identity and Credential Cleansing
        with(config) {
            removeDeviceId()
            removeEmail()
            removeApiKey()
            pinNumber = ""
            email = ""
            deviceId = ""
            apiKey = ""
            if (!expired) {
                installationStatus = ""
            }
            PreyLogger.d("Verification -> Email: $email, DeviceId: $deviceId, ApiKey: $apiKey")
        }
        if (removeCache) {
            runCatching { config.wipeData() }
                .onFailure { errors.add("WipeData: ${it.message}") }
            runCatching { PreyConfig.deleteCacheInstance(context) }
                .onFailure { errors.add("DeleteCache: ${it.message}") }
        }
        //Final Navigation
        runCatching {
            val intent = Intent(context, CheckPasswordHtmlActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("nexturl", "loadUrl")
            }
            context.startActivity(intent)
        }.onFailure { e ->
            PreyLogger.e("Error starting activity: ${e.message}", e)
            errors.add("Navigation: ${e.message}")
        }
        return if (errors.isEmpty()) null else errors.joinToString("\n")
    }

}