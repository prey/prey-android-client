/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.prey.R
import com.prey.activities.js.CustomWebView
import com.prey.PreyConfig
import com.prey.PreyLogger

class PanelWebActivity : Activity() {
    private val activity: Activity = this
    private var myWebView: WebView? = null

    override fun onBackPressed() {
        var intent: Intent? = null
        intent = Intent(application, CheckPasswordHtmlActivity::class.java)
        startActivity(intent)
        finish()
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.panelweb)
        this.setContentView(R.layout.activity_webview)
        myWebView = findViewById<View>(R.id.install_browser) as WebView
        myWebView!!.setOnKeyListener { view, i, keyEvent ->
            CustomWebView.callDispatchKeyEvent(applicationContext, keyEvent)
            false
        }
    }

    public override fun onResume() {
        super.onResume()
        val settings = myWebView!!.settings
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        myWebView!!.isVerticalScrollBarEnabled = false
        myWebView!!.isHorizontalScrollBarEnabled = false
        myWebView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                activity.title = getText(R.string.loading).toString()
                activity.setProgress(progress * 100)
                if (progress == 100) activity.setTitle(R.string.app_name)
            }
        }
        myWebView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                PreyLogger.d("Finished:$url")
                super.onPageFinished(view, url)
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                PreyLogger.d("Started:$url")
                super.onPageStarted(view, url, favicon)
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                PreyLogger.d("OverrideUrl:$url")
                return super.shouldOverrideUrlLoading(view, url)
            }
        }
        val url: String = PreyConfig.getInstance(applicationContext).getPreyPanelJwt()
        val tokenJwt: String? = PreyConfig.getInstance(applicationContext).getTokenJwt()
        PreyLogger.d("tokenJwt:$tokenJwt")
        val postData = "token=$tokenJwt"
        myWebView!!.postUrl(url, postData.toByteArray())
    }
}