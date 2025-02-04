/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.activities.js.WebAppInterface
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils

class PasswordHtmlActivity : Activity() {
    private var myWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        PreyLogger.d("PasswordHtmlActivity: onCreate")
        myWebView = findViewById<View>(R.id.install_browser) as WebView
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
        val lng: String = PreyUtils.getLanguage()
        var url = "%s#/%s/%s"
        val lockMessage: String? = PreyConfig.getInstance(this).getLockMessage()
        url = if (lockMessage != null && "" != lockMessage) {
            String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lockmessage")
        } else {
            String.format(url, CheckPasswordHtmlActivity.URL_ONB, lng, "lock")
        }
        myWebView!!.addJavascriptInterface(
            WebAppInterface(this, this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        myWebView!!.loadUrl(url)
        myWebView!!.loadUrl("javascript:window.location.reload(true)")
        //TODO:cambiar
       // PreyConfig.getInstance(this).viewLock = myWebView
    }

    override fun onResume() {
        super.onResume()
        val unlockPass: String? = PreyConfig.getInstance(applicationContext).getUnlockPass()
        val isLock = unlockPass != null && "" != unlockPass
        PreyLogger.d("PasswordHtmlActivity isLock:$isLock")
        if (!isLock) {
            finishAffinity()
        }
    }

    fun pfinish() {
        finishAffinity()
    }
}