/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js

import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.KeyEvent

import com.prey.activities.CloseActivity
import com.prey.activities.PanelWebActivity
import com.prey.activities.SecurityActivity
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices
import com.prey.services.PreyLockHtmlService

/**
 * CustomWebView object that handles dispatching key events.
 */
object CustomWebView {

    /**
     * Dispatches a key event to the CustomWebView.
     *
     * @param context The application context.
     * @param keyEvent The key event to dispatch.
     */
    fun callDispatchKeyEvent(context: Context, keyEvent: KeyEvent) {
        PreyLogger.d("callDispatchKeyEvent:" + keyEvent.keyCode)
        // Check if the key event is the Enter key.
        if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            val page: String? = PreyConfig.getInstance(context).getPage()
            val apikey: String? = PreyConfig.getInstance(context).getApiKey()
            val inputWebview: String? = PreyConfig.getInstance(context).getInputWebview()
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            PreyLogger.d("CustomWebView dispatchKeyEvent Enter page:$page inputWebview:$inputWebview")
            if ("setting" == page) {
                try {
                    val isPasswordOk =
                        PreyWebServices.getInstance().checkPassword(context, apikey!!, inputWebview!!)
                    if (isPasswordOk) {
                        PreyConfig.getInstance(context).setUnlockPass("")
                        val intentSecurity = Intent(context, SecurityActivity::class.java)
                        intentSecurity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intentSecurity)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            if ("login" == page) {
                try {
                    val isPasswordOk =
                        PreyWebServices.getInstance().checkPassword(context, apikey!!, inputWebview!!)
                    if (isPasswordOk) {
                        PreyConfig.getInstance(context).setUnlockPass("")
                        val intentPanelWeb = Intent(context, PanelWebActivity::class.java)
                        intentPanelWeb.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intentPanelWeb)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            if ("lock" == page) {
                val unlock: String = PreyConfig.getInstance(context).getUnlockPass().toString()
                PreyLogger.d("dispatchKeyEvent inputWebview:$inputWebview unlock:$unlock")
                PreyConfig.getInstance(context).setInputWebview("")
                if (unlock != null && "" != unlock && unlock == inputWebview) {
                    PreyConfig.getInstance(context).setUnlockPass("")
                    val intent = Intent(context, PreyLockHtmlService::class.java)
                    context.stopService(intent)
                    object : Thread() {
                        override fun run() {
                            val jobIdLock: String? = PreyConfig.getInstance(context).getJobIdLock()
                            var reason = "{\"origin\":\"user\"}"
                            if (jobIdLock != null && "" != jobIdLock) {
                                reason = "{\"origin\":\"user\",\"device_job_id\":\"$jobIdLock\"}"
                                PreyConfig.getInstance(context).setJobIdLock("")
                            }
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                                context,
                                UtilJson.makeMapParam("start", "lock", "stopped", reason)
                            )
                        }
                    }.start()
                    val intentClose = Intent(context, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intentClose)
                }
            }
        }
    }
}