/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import com.prey.R
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class DeviceReadyActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        var intent: Intent? = null
        intent = Intent(application, CheckPasswordHtmlActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val nManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nManager.cancel(PreyConfig.TAG, PreyConfig.NOTIFY_ANDROID_6)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.device_ready)
        PreyLogger.d("onCreate of DeviceReadyActivity")
        val titilliumWebBold =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val magdacleanmonoRegular =
            Typeface.createFromAsset(assets, "fonts/MagdaClean/magdacleanmono-regular.ttf")
        val textView3_1 = findViewById<View>(R.id.textView3_1) as TextView
        val textView3_2 = findViewById<View>(R.id.textView3_2) as TextView
        val textView4_1 = findViewById<View>(R.id.textView4_1) as TextView
        val textView4_2 = findViewById<View>(R.id.textView4_2) as TextView
        textView3_1.setTypeface(magdacleanmonoRegular)
        textView3_2.setTypeface(titilliumWebBold)
        textView4_1.setTypeface(magdacleanmonoRegular)
        textView4_2.setTypeface(titilliumWebBold)
        val linearLayout1 = findViewById<View>(R.id.linearLayout1) as LinearLayout
        linearLayout1.setOnClickListener {
            var intent: Intent? = null
            intent = Intent(application, PanelWebActivity::class.java)
            startActivity(intent)
            finish()
        }
        val linearLayout2 = findViewById<View>(R.id.linearLayout2) as LinearLayout
        linearLayout2.setOnClickListener {
            var intent: Intent? = null
            intent = Intent(application, PreyConfigurationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}