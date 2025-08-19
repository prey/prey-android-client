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

/**
 * Activity responsible for displaying the password HTML page.
 */
class PasswordHtmlActivity : Activity() {
    private var myWebView: WebView? = null

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        PreyLogger.d("PasswordHtmlActivity: onCreate")
        val unlockPass: String? = PreyConfig.getInstance(applicationContext).getUnlockPass()
        val isLock = unlockPass != null && "" != unlockPass
        PreyLogger.d("PasswordHtmlActivity isLock:$isLock")
        if (!isLock) {
            PreyConfig.getInstance(this).setLoadUrl(URL_OUT)
            finishAffinity()
            return
        }
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
        var url = "${CheckPasswordHtmlActivity.URL_ONB}#/${lng}/"
        val lockMessage: String? = PreyConfig.getInstance(applicationContext).getLockMessage()
        if (lockMessage != null && "" != lockMessage) {
            url += URL_LOCK_WIH_MESSAGE
        } else {
            url += URL_LOCK
        }
        myWebView!!.addJavascriptInterface(
            WebAppInterface(this, this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        PreyConfig.getInstance(applicationContext).setLoadUrl(url)
        myWebView!!.loadUrl(url)
        PreyConfig.getInstance(this).setLoadUrl(url)
        myWebView!!.loadUrl("javascript:window.location.reload(true)")
        PreyConfig.getInstance(this).viewLock = myWebView
    }

    companion object {
        const val URL_LOCK: String = "lock"
        const val URL_LOCK_WIH_MESSAGE: String = "lockmessage"
        const val URL_OUT: String = "out"
    }

}