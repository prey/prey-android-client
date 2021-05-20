/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2019 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.activities.js.WebAppInterface;

import java.util.Locale;

public class ReportActivity extends FragmentActivity implements OnMapReadyCallback {
    private WebView myWebView = null;

    public void onBackPressed() {
        Intent intent = null;
        intent = new Intent(getApplication(), CheckPasswordHtmlActivity.class);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report);
        PreyLogger.d("ReportActivity: onCreate");
        loadUrl();
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap map = googleMap;
        double lat = 0;
        double lng = 0;
        try{
            Bundle extras = getIntent().getExtras();
            lat=extras.getDouble("lat");
            lng=extras.getDouble("lng");
        }catch (Exception e){
            PreyLogger.e("Error:"+e.getMessage(),e);
        }
        LatLng UPV = new LatLng(lat,lng);
        map.addMarker(new MarkerOptions().position(UPV));
        map.moveCamera(CameraUpdateFactory.newLatLng(UPV));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(UPV, 17f));
    }

    public void loadUrl(){
        settings();
        myWebView.addJavascriptInterface(new WebAppInterface(getApplicationContext()), CheckPasswordHtmlActivity.JS_ALIAS);
        myWebView.loadUrl(getUrl(this));
    }

    public void settings(){
        PreyLogger.d("ReportActivity: settings");
        myWebView = (WebView) findViewById(R.id.install_browserReport);
        WebSettings settings = myWebView.getSettings();
        myWebView.setBackgroundColor(0x00000000);
        settings.setJavaScriptEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setAllowFileAccess(true);
    }

    public String getUrl(Context ctx){
        String lng="es".equals(Locale.getDefault().getLanguage())?"es":"en";
        String url = CheckPasswordHtmlActivity.URL_ONB+"#/"+lng+"/report";
        PreyLogger.d("ReportActivity url: "+url);
        return url;
    }

}