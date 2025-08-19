/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView

import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.activities.js.WebAppInterface
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.PreyUtils
import com.prey.receivers.PreyDisablePowerOptionsReceiver

import java.util.Date

class PreySecureService : Service() {
    private val windowManager: WindowManager? = null
    private var view: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("PreySecureService onCreate")
    }

    private var myWebView: WebView? = null

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val context: Context = this
        onStart(context)
    }

    fun onStart(context: Context) {
        PreyLogger.d("PreySecureService onStart")
        val canDrawOverlays = PreyPermission.canDrawOverlays(context)
        if (!canDrawOverlays) {
            stopSelf()
            return
        }
        val time = PreyConfig.getInstance(this).getTimeSecureLock()
        val now = Date().time
        PreyLogger.d(

            "PreyDisablePowerOptionsReceiver time:${time} now:${now} < ${(now < time)}"
        )
        if (now < time) {
            PreyLogger.d("PreySecureService close")
            stopSelf()
            return
        }
        val viewSecure = PreyConfig.getInstance(this).getViewSecure()
        if (!viewSecure) {
            PreyLogger.d("PreySecureService viewSecure stopSelf")
            stopSelf()
            return
        }
        PreyConfig.getInstance(this).setViewSecure(false)
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.webview, null)
        myWebView = view!!.findViewById<View>(R.id.install_browser) as WebView
        PreyConfig.getInstance(this).viewSecure = myWebView!!
        myWebView!!.setOnKeyListener { view, i, keyEvent ->
            CustomWebView.callDispatchKeyEvent(applicationContext, keyEvent)
            false
        }
        val settings = myWebView!!.settings
        myWebView!!.setBackgroundColor(0x00000000)
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        val lng = PreyUtils.getLanguage()
        val url = "${CheckPasswordHtmlActivity.URL_ONB}#/${lng}/pin"
        myWebView!!.addJavascriptInterface(
            WebAppInterface(this, this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        myWebView!!.loadUrl(url)
        myWebView!!.loadUrl("javascript:window.location.reload(true)")
        val pinNumber = PreyConfig.getInstance(context).getPinNumber()
        if (pinNumber != null && "" != pinNumber && pinNumber.length == 4) {
            try {
                val intentClose = Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS")
                intentClose.putExtra(
                    PreyDisablePowerOptionsReceiver.stringExtra,
                    PreyDisablePowerOptionsReceiver.stringExtra
                )
                this.sendBroadcast(intentClose)
            } catch (e: Exception) {
                PreyLogger.e("Error CLOSE_SYSTEM:${e.message}", e)
            }
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.format = PixelFormat.TRANSLUCENT
            layoutParams.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_FULLSCREEN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                if (Settings.canDrawOverlays(this)) {
                    if (wm != null) {
                        try {
                            wm.addView(view, layoutParams)
                            PreyConfig.getInstance(this).setOpenSecureService(true)
                        } catch (e: Exception) {
                            PreyLogger.e(e.message, e)
                        }
                    }
                }
            }
            try {
                val intentClose = Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS")
                intentClose.putExtra(
                    PreyDisablePowerOptionsReceiver.stringExtra,
                    PreyDisablePowerOptionsReceiver.stringExtra
                )
                this.sendBroadcast(intentClose)
            } catch (e: Exception) {
                PreyLogger.e("Error intentClose:%${e.message}", e)
            }
        } else {
            if (view != null) {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                wm?.removeView(view)
                view = null
            }
            PreyConfig.getInstance(this).setOpenSecureService(false)
            stopSelf()
        }
        PreyConfig.getInstance(context).setLastEvent("on_start_secure")
    }

    override fun onDestroy() {
        super.onDestroy()
        PreyLogger.d("PreySecureService onDestroy")
        PreyConfig.getInstance(this).setOpenSecureService(false)
        if (view != null) {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            if (wm != null) {
                try {
                    wm.removeView(view)
                } catch (e: Exception) {
                    PreyLogger.e("Error:${e.message}", e)
                }
            }
            view = null
        }
    }

    fun stop() {
        close()
    }

    fun close() {
        PreyLogger.d("PreySecureService close")
        stopSelf()
    }

}