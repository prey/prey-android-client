/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.content.Intent

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.actions.fileretrieval.FileretrievalController
import com.prey.actions.observer.ActionResult
import com.prey.actions.report.ReportScheduled
import com.prey.activities.LoginActivity
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.json.UtilJson

import org.json.JSONObject

/**
 * Class responsible for handling device detachment.
 */
class Detach {

    /**
     * Starts the detachment process.
     *
     * @param context The application context.
     * @param actionResults A list of action results.
     * @param parameters A JSON object containing detachment parameters.
     */
    fun start(context: Context, actionResults: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.i("Detach")
        var isExpired = false
        try {
            isExpired = UtilJson.getBooleanValue(parameters, "expired")
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        if (isExpired) {
            PreyConfig.getInstance(context).setInstallationStatus("DEL")
            PreyLogger.d("Detach expired:%${isExpired}")
            // Call detachDevice with expiration flag
            detachDevice(context, true, false, false, isExpired)
        } else {
            // Call detachDevice without expiration flag
            detachDevice(context)
        }
    }

    /**
     * Detaches the device with default settings.
     *
     * @param context The application context.
     * @return An error message if any.
     */
    fun detachDevice(context: Context): String? {
        return detachDevice(context, true, true, true, false)
    }

    /**
     * Detaches the device with customizable settings.
     *
     * @param context The application context.
     * @param openApp Whether to open the app after detachment.
     * @param removePermissions Whether to remove permissions.
     * @param removeCache Whether to remove cache.
     * @param isExpired Whether the detachment is due to expiration.
     * @return An error message if any.
     */
    fun detachDevice(
        context: Context,
        openApp: Boolean = true,
        removePermissions: Boolean = true,
        removeCache: Boolean = true,
        isExpired: Boolean = false
    ): String? {
        PreyLogger.d("detachDevice")
        var error: String? = null
        try {
            PreyConfig.getInstance(context).unregisterC2dm(false)
        } catch (e: Exception) {
            error = e.message
        }
        try {
            PreyConfig.getInstance(context).setSecurityPrivilegesAlreadyPrompted(false)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("1:${error}")
        try {
            PreyConfig.getInstance(context).setProtectAccount(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).setProtectPrivileges(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).setProtectTour(false)
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).setProtectReady(false)
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
            PreyConfig.getInstance(context).removeLocationAware()
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("3:${error}")
        try {
            PreyConfig.getInstance(context).setAware(false)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        try {
            FileretrievalController.getInstance().deleteAll(context)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyConfig.getInstance(context).setPrefsBiometric(false)
        PreyLogger.d("5:${error}")
        try {
            ReportScheduled.getInstance(context)!!.reset()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).getWebServices().deleteDevice(context)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        PreyLogger.d("6:${error}")
        if (removeCache) {
            try {
                PreyConfig.getInstance(context).wipeData()
            } catch (e: Exception) {
                error += e.message
            }
        }
        try {
            PreyConfig.getInstance(context).removeDeviceId()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).removeEmail()
        } catch (e: Exception) {
            error += e.message
        }
        try {
            PreyConfig.getInstance(context).removeApiKey()
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            PreyConfig.getInstance(context).setPinNumber("")
        } catch (e: Exception) {
            error = e.message
        }
        try {
            PreyConfig.getInstance(context).setEmail("")
        } catch (e: Exception) {
            error = e.message
        }
        PreyLogger.d("7:${error}")
        try {
            PreyConfig.getInstance(context).setDeviceId("")
        } catch (e: Exception) {
            error = e.message
        }
        try {
            PreyConfig.getInstance(context).setApiKey("")
        } catch (e: Exception) {
            error = e.message
        }
        PreyLogger.d("8:${error}")
        if (!isExpired) {
            try {
                PreyConfig.getInstance(context).setInstallationStatus("")
            } catch (e: Exception) {
                error = e.message
            }
        }
        PreyLogger.d("Email:${PreyConfig.getInstance(context).getEmail()}")
        PreyLogger.d("DeviceId:${PreyConfig.getInstance(context).getDeviceId()}")
        PreyLogger.d("ApiKey:${PreyConfig.getInstance(context).getApiKey()}")
        if (removeCache) {
            try {
                PreyConfig.getInstance(context).deleteCacheInstance(context)
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
        try {
            if (openApp) {
                val intent = Intent(context, LoginActivity::class.java)
                intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        return error
    }

}