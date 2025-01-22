/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.js.kotlin

import android.content.Context
import android.content.Intent
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.KeyEvent
import com.prey.activities.kotlin.CloseActivity
import com.prey.activities.kotlin.PanelWebActivity
import com.prey.activities.kotlin.SecurityActivity
import com.prey.json.kotlin.UtilJson
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger
import com.prey.net.kotlin.PreyWebServices
import com.prey.services.kotlin.PreyLockHtmlService

object CustomWebView {
    fun callDispatchKeyEvent(ctx: Context, event: KeyEvent) {
        PreyLogger.d("callDispatchKeyEvent:" + event.keyCode)
        if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
            val page: String? = PreyConfig.getInstance(ctx).getPage()
            val apikey: String? = PreyConfig.getInstance(ctx).getApiKey()
            val inputWebview: String? = PreyConfig.getInstance(ctx).getInputWebview()
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            PreyLogger.d("CustomWebView dispatchKeyEvent Enter page:$page inputWebview:$inputWebview")
            if ("setting" == page) {
                try {
                    val isPasswordOk =
                        PreyWebServices.getInstance().checkPassword(ctx, apikey!!, inputWebview!!)
                    if (isPasswordOk) {
                        PreyConfig.getInstance(ctx).setUnlockPass("")
                        val intentSecurity = Intent(ctx, SecurityActivity::class.java)
                        intentSecurity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(intentSecurity)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            if ("login" == page) {
                try {
                    val isPasswordOk =
                        PreyWebServices.getInstance().checkPassword(ctx, apikey!!, inputWebview!!)
                    if (isPasswordOk) {
                        PreyConfig.getInstance(ctx).setUnlockPass("")
                        val intentPanelWeb = Intent(ctx, PanelWebActivity::class.java)
                        intentPanelWeb.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(intentPanelWeb)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
            }
            if ("lock" == page) {
                val unlock: String = PreyConfig.getInstance(ctx).getUnlockPass().toString()
                PreyLogger.d("dispatchKeyEvent inputWebview:$inputWebview unlock:$unlock")
                PreyConfig.getInstance(ctx).setInputWebview("")
                if (unlock != null && "" != unlock && unlock == inputWebview) {
                    PreyConfig.getInstance(ctx).setUnlockPass("")
                    val intent = Intent(ctx, PreyLockHtmlService::class.java)
                    ctx.stopService(intent)
                    object : Thread() {
                        override fun run() {
                            val jobIdLock: String? = PreyConfig.getInstance(ctx).getJobIdLock()
                            var reason = "{\"origin\":\"user\"}"
                            if (jobIdLock != null && "" != jobIdLock) {
                                reason = "{\"origin\":\"user\",\"device_job_id\":\"$jobIdLock\"}"
                                PreyConfig.getInstance(ctx).setJobIdLock("")
                            }
                            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                                ctx,
                                UtilJson.makeMapParam("start", "lock", "stopped", reason)
                            )
                        }
                    }.start()
                    val intentClose = Intent(ctx, CloseActivity::class.java)
                    intentClose.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(intentClose)
                    //TODO: falta implemnetar
                    /*
                    try {
                        val viewLock: View = PreyConfig.getInstance(ctx).viewLock
                        if (viewLock != null) {
                            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                            wm.removeView(viewLock)
                        } else {
                            Process.killProcess(Process.myPid())
                        }
                    } catch (e: Exception) {
                        Process.killProcess(Process.myPid())
                    }*/
                }
            }
        }
    }
}