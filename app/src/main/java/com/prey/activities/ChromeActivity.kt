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
import com.prey.PreyConfig
import com.prey.R
import com.prey.PreyLogger
import com.prey.PreyUtils

/****
 * This activity shows the html view for Chromebook
 */
class ChromeActivity : AppCompatActivity() {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
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

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        PreyLogger.d("ChromeActivity: onResume")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
        val webView = findViewById<View>(R.id.install_browser) as WebView
        webView.apply {
            setBackgroundColor(0x00000000)
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                loadsImagesAutomatically = true
                useWideViewPort = true
                setSupportZoom(false)
                builtInZoomControls = false
            }
        }
        val language: String = PreyUtils.getLanguage()
        webView.loadUrl("${CheckPasswordHtmlActivity.URL_ONB}#/$language/chrome")
        webView.loadUrl("javascript:window.location.reload(true)")
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_CHROME)
    }

    companion object {
        const val ACTIVITY_CHROME: String = "ACTIVITY_CHROME"
    }

}