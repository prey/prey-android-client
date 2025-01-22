/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions.kotlin

import android.content.Context
import android.content.Intent
import com.prey.actions.fileretrieval.kotlin.FileretrievalController
import com.prey.actions.observer.kotlin.ActionResult
import com.prey.actions.report.kotlin.ReportScheduled
import com.prey.activities.kotlin.LoginActivity
import com.prey.backwardcompatibility.kotlin.FroyoSupport
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import com.prey.preferences.kotlin.RunBackgroundCheckBoxPreference
import org.json.JSONObject

class Detach {
    fun start(ctx: Context, list: List<ActionResult?>?, parameters: JSONObject?) {
        PreyLogger.i("Detach")
        var expired = false
        try {
            expired = UtilJson.getBoolean(parameters, "expired")
        } catch (e: Exception) {
            PreyLogger.e(String.format("Error:%s", e.message), e)
        }
        if (expired) {
            PreyConfig.getInstance(ctx).setInstallationStatus( "DEL")
            PreyLogger.d(String.format("Detach expired:%b", expired))
            detachDevice(ctx, true, false, false, expired)
        } else {
            detachDevice(ctx)
        }
    }

    companion object {
        @JvmOverloads
        fun detachDevice(
            ctx: Context,
            openApplication: Boolean = true,
            removePermissions: Boolean = true,
            removeCache: Boolean = true,
            expired: Boolean = false
        ): String? {
            PreyLogger.d("detachDevice")
            var error: String? = null
            try {
                PreyConfig.getInstance(ctx).unregisterC2dm(false)
            } catch (e: Exception) {
                error = e.message
            }
            try {
                PreyConfig.getInstance(ctx).setSecurityPrivilegesAlreadyPrompted(false)
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            PreyLogger.d(String.format("1:%s", error))
            try {
                PreyConfig.getInstance(ctx).setProtectAccount (false)
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyConfig.getInstance(ctx).setProtectPrivileges (false)
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyConfig.getInstance(ctx).setProtectTour  (false)
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyConfig.getInstance(ctx).setProtectReady  (false)
            } catch (e: Exception) {
                error += e.message
            }
            PreyLogger.d(String.format("2:%s", error))
            try {
                if (removePermissions) {
                    val fSupport = FroyoSupport.getInstance(ctx)
                    if (fSupport!!.isAdminActive) {
                        fSupport!!.removeAdminPrivileges()
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            try {
                RunBackgroundCheckBoxPreference.notifyCancel(ctx)
                PreyConfig.getInstance(ctx).removeLocationAware()
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            PreyLogger.d(String.format("3:%s", error))
            try {
                PreyConfig.getInstance(ctx).setAware  (false)
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s" + e.message), e)
            }
            try {
                FileretrievalController.getInstance().deleteAll(ctx)
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            PreyConfig.getInstance(ctx).setPrefsBiometric  (false)
            PreyLogger.d(String.format("5:%s", error))
            try {
                ReportScheduled.getInstance(ctx)!!.reset()
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyWebServices.getInstance().deleteDevice(ctx)
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            PreyLogger.d(String.format("6:%s", error))
            if (removeCache) {
                try {
                    PreyConfig.getInstance(ctx).wipeData()
                } catch (e: Exception) {
                    error += e.message
                }
            }
            try {
                PreyConfig.getInstance(ctx).removeDeviceId()
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyConfig.getInstance(ctx).removeEmail()
            } catch (e: Exception) {
                error += e.message
            }
            try {
                PreyConfig.getInstance(ctx).removeApiKey()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                PreyConfig.getInstance(ctx).setPinNumber ("")
            } catch (e: Exception) {
                error = e.message
            }
            try {
                PreyConfig.getInstance(ctx).setEmail  ("")
            } catch (e: Exception) {
                error = e.message
            }
            PreyLogger.d(String.format("7:%s", error))
            try {
                PreyConfig.getInstance(ctx).setDeviceId  ("")
            } catch (e: Exception) {
                error = e.message
            }
            try {
                PreyConfig.getInstance(ctx).setApiKey ("")
            } catch (e: Exception) {
                error = e.message
            }
            PreyLogger.d(String.format("8:%s", error))
            if (!expired) {
                try {
                    PreyConfig.getInstance(ctx).setInstallationStatus ("")
                } catch (e: Exception) {
                    error = e.message
                }
            }
            PreyLogger.d( "Email:%s"+PreyConfig.getInstance(ctx).getEmail()!!)
            PreyLogger.d( "DeviceId:%s"+ PreyConfig.getInstance(ctx).getDeviceId()!!)
            PreyLogger.d( "ApiKey:%s"+PreyConfig.getInstance(ctx).getApiKey()!!)
            if (removeCache) {
                try {
                    PreyConfig.getInstance(ctx).deleteCacheInstance(ctx)
                } catch (e: Exception) {
                    PreyLogger.e(String.format("Error:%s", e.message), e)
                }
            }
            try {
                if (openApplication) {
                    val intent = Intent(ctx, LoginActivity::class.java)
                    intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                    ctx.startActivity(intent)
                }
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
            }
            return error
        }
    }
}
