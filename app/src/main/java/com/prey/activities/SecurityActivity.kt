/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.activities.js.WebAppInterface
import com.prey.PreyLogger
import com.prey.PreyUtils

class SecurityActivity : AppCompatActivity() {
    private var myWebView: WebView? = null

    override fun onBackPressed() {
        var intent: Intent? = null
        intent = Intent(application, CheckPasswordHtmlActivity::class.java)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            supportActionBar!!.hide()
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        setContentView(R.layout.webview)
        PreyLogger.d("SecurityActivity: onCreate")
        security()
    }

    fun settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings")
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
    }

    fun security() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport")
        val lng: String = PreyUtils.getLanguage()
        val url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/security"
        settings()
        PreyLogger.d("_url:$url")
        myWebView!!.addJavascriptInterface(
            WebAppInterface(this),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        myWebView!!.loadUrl(url)
    }
}