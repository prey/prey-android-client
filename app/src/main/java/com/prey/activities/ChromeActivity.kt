/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.prey.R
import com.prey.PreyLogger
import com.prey.PreyUtils

/****
 * This activity shows the html view for Chromebook
 */
class ChromeActivity : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreyLogger.d("ChromeActivity: onCreate")
        try {
            supportActionBar!!.hide()
        } catch (e: Exception) {
            PreyLogger.e("Error ActionBar().hide", e)
        }
        setContentView(R.layout.webview)
    }

    override fun onResume() {
        super.onResume()
        PreyLogger.d("ChromeActivity: onResume")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
        val lng: String = PreyUtils.getLanguage()
        val url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/chrome"
        val myWebView = findViewById<View>(R.id.install_browser) as WebView
        val settings = myWebView.settings
        myWebView.setBackgroundColor(0x00000000)
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        myWebView.loadUrl(url)
        myWebView.loadUrl("javascript:window.location.reload(true)")
    }

    override fun onDestroy() {
        super.onDestroy()
        PreyLogger.d("ChromeActivity: onDestroy")
    }
}