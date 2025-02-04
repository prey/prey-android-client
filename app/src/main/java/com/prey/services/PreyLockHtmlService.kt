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
import android.os.Binder
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
import com.prey.PreyUtils

class PreyLockHtmlService : Service() {
    private var view: View? = null

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: PreyLockHtmlService
            get() = this@PreyLockHtmlService
    }

    fun stop() {
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("PreyLockHtmlService onCreate")
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val ctx: Context = this
        PreyLogger.d("PreyLockHtmlService onStart")
        val unlock = PreyConfig.getInstance(ctx).getUnlockPass()
        if (unlock != null && "" != unlock) {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.webview, null)
            PreyConfig.getInstance(ctx).viewLock = view!!
            val myWebView = view!!.findViewById<View>(R.id.install_browser) as WebView
            myWebView.setOnKeyListener { view, i, keyEvent ->
                CustomWebView.callDispatchKeyEvent(
                    applicationContext,
                    keyEvent
                )
                false
            }
            val settings = myWebView.settings
            myWebView.setBackgroundColor(0x00000000)
            settings.javaScriptEnabled = true
            settings.loadWithOverviewMode = true
            settings.loadsImagesAutomatically = true
            settings.useWideViewPort = true
            settings.setSupportZoom(false)
            settings.builtInZoomControls = false
            val lng = PreyUtils.getLanguage()
            var url = "%s#/%s/%s"
            val lockMessage = PreyConfig.getInstance(this).getLockMessage()
            url = if (lockMessage != null && "" != lockMessage) {
                String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lockmessage")
            } else {
                String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lock")
            }
            myWebView.addJavascriptInterface(
                WebAppInterface(applicationContext.applicationContext, this),
                CheckPasswordHtmlActivity.JS_ALIAS
            )
            myWebView.loadUrl(url)
            myWebView.loadUrl("javascript:window.location.reload(true)")
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
                            PreyConfig.getInstance(this).setOverLock(true)
                        } catch (e: Exception) {
                            PreyLogger.e(e.message, e)
                        }
                    }
                }
            }
        } else {
            stopSelf()
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        PreyLogger.d("PreyLockHtmlService onDestroy")
    }
}