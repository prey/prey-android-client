/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.prey.R
import com.prey.activities.js.WebAppInterface
import com.prey.PreyLogger
import java.util.Locale

class ReportActivity : FragmentActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {
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
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        setContentView(R.layout.report)
        PreyLogger.d("ReportActivity: onCreate")
        loadUrl()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val map = googleMap
        var lat = 0.0
        var lng = 0.0
        try {
            val extras = intent.extras
            lat = extras!!.getDouble("lat")
            lng = extras.getDouble("lng")
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        val UPV = LatLng(lat, lng)
        map.addMarker(MarkerOptions().position(UPV))
        map.moveCamera(CameraUpdateFactory.newLatLng(UPV))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(UPV, 17f))
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> PreyLogger.d("Maps The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> PreyLogger.d("Maps The legacy version of the renderer is used.")
        }
    }

    fun loadUrl() {
        settings()
        myWebView!!.addJavascriptInterface(
            WebAppInterface(applicationContext),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        myWebView!!.loadUrl(getUrl(this))
    }

    fun settings() {
        PreyLogger.d("ReportActivity: settings")
        myWebView = findViewById<View>(R.id.install_browserReport) as WebView
        val settings = myWebView!!.settings
        myWebView!!.setBackgroundColor(0x00000000)
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.allowFileAccess = true
    }

    fun getUrl(ctx: Context?): String {
        val lng = if ("es" == Locale.getDefault().language) "es" else "en"
        val url = CheckPasswordHtmlActivity.URL_ONB + "#/" + lng + "/report"
        PreyLogger.d("ReportActivity url: $url")
        return url
    }
}