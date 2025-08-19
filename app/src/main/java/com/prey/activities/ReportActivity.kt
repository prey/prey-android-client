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
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.prey.PreyConfig
import com.prey.R
import com.prey.activities.js.WebAppInterface
import com.prey.PreyLogger
import com.prey.PreyUtils

/**
 * ReportActivity is a FragmentActivity that displays a map and a web view.
 * It handles map initialization, web view configuration, and map marker placement.
 */
class ReportActivity : FragmentActivity(), OnMapReadyCallback, OnMapsSdkInitializedCallback {
    private lateinit var webView: WebView

    /**
     * Called when the back button is pressed.
     * Starts the CheckPasswordHtmlActivity and finishes this activity.
     */
    override fun onBackPressed() {
        startActivity(Intent(application, CheckPasswordHtmlActivity::class.java))
        finish()
    }

    /**
     * Called when the activity is created.
     * Initializes the map, sets the content view, and configures the web view.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        setContentView(R.layout.report)
        PreyLogger.d("ReportActivity: onCreate")
        configureWebView()
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_REPORT)
    }

    /**
     * Called when the map is ready.
     * Places a marker on the map at the specified location and moves the camera to that location.
     *
     * @param googleMap The GoogleMap instance.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        val map = googleMap
        var lat = 0.0
        var lng = 0.0
        try {
            val extras = intent.extras
            lat = extras!!.getDouble("lat")
            lng = extras.getDouble("lng")
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        val UPV = LatLng(lat, lng)
        map.addMarker(MarkerOptions().position(UPV))
        map.moveCamera(CameraUpdateFactory.newLatLng(UPV))
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(UPV, 17f))
    }

    /**
     * Called when the maps SDK is initialized.
     * Logs a message indicating which renderer is being used.
     *
     * @param renderer The MapsInitializer.Renderer instance.
     */
    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> PreyLogger.d("Maps The latest version of the renderer is used.")
            MapsInitializer.Renderer.LEGACY -> PreyLogger.d("Maps The legacy version of the renderer is used.")
        }
    }

    /**
     * Configures the web view.
     * Sets up the web view's settings, adds a JavaScript interface, and loads a URL.
     */
    fun configureWebView() {
        PreyLogger.d("ReportActivity: settings")
        webView = findViewById<View>(R.id.install_browserReport) as WebView
        webView.setBackgroundColor(0x00000000)
        webView.addJavascriptInterface(
            WebAppInterface(applicationContext),
            CheckPasswordHtmlActivity.JS_ALIAS
        )
        val settings = webView.settings
        settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            allowFileAccess = true
        }
        val language: String = PreyUtils.getLanguage()
        val url = "${CheckPasswordHtmlActivity.URL_ONB}#/$language/report"
        PreyLogger.d("url:${url}")
        webView.loadUrl(url)
    }

    companion object {
        const val ACTIVITY_REPORT: String = "ACTIVITY_REPORT"
    }

}