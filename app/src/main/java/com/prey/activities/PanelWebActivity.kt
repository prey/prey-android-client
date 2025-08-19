/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * Activity responsible for displaying the Panel Web view.
 */
class PanelWebActivity : AppCompatActivity() {
    private val activity: Activity = this
    private lateinit var myWebView: WebView

    /**
     * Called when the activity is created.
     * Initializes the WebView component and sets its layout.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
    }

    /**
     * Called when the activity is resumed.
     * Configures the WebView component and loads the panel URL.
     */
    public override fun onResume() {
        super.onResume()
        myWebView = findViewById<View>(R.id.install_browser) as WebView
        myWebView.settings.useWideViewPort = true
        myWebView.settings.loadWithOverviewMode = true
        myWebView.settings.domStorageEnabled = true
        myWebView.settings.javaScriptEnabled = true
        myWebView.settings.javaScriptCanOpenWindowsAutomatically = true
        myWebView.isVerticalScrollBarEnabled = false
        myWebView.isHorizontalScrollBarEnabled = false
        myWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress);
                activity.setTitle(getText(R.string.loading).toString());
                activity.setProgress(newProgress * 100);
                if (newProgress == 100)
                    activity.setTitle(R.string.app_name);
            }
        }
        myWebView.webViewClient = object : WebViewClient() {
        }
        val apikey = PreyConfig.getInstance(this).getApiKey()
        val tokenJwt = PreyConfig.getInstance(this).getWebServices().getToken(this, apikey!!, "X")
        PreyLogger.d("token jwt:$tokenJwt")
        PreyConfig.getInstance(this).setTokenJwt(tokenJwt)
        val url: String = PreyConfig.getInstance(applicationContext).getPreyPanelJwt()
        PreyLogger.d("token url:$url")
        PreyLogger.d("token jwt:$tokenJwt")
        val postData = "token=$tokenJwt"
        myWebView.postUrl(url, postData.toByteArray())
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_PANEL_FORM)
    }

    /**
     * Handles the back button press event.
     * Redirects to the CheckPasswordHtmlActivity.
     */
    override fun onBackPressed() {
        PreyLogger.i("onBackPressed")
        val intent = Intent(application, CheckPasswordHtmlActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        const val ACTIVITY_PANEL_FORM: String = "ACTIVITY_PANEL_FORM"
    }

}