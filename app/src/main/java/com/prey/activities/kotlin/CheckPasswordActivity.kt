/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.prey.R
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class CheckPasswordActivity :  Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password2)
        //TODO:falta implementar
        //bindPasswordControls()
        val device_ready_h2_text = findViewById<View>(R.id.device_ready_h2_text) as TextView
        val textForgotPassword = findViewById<View>(R.id.link_forgot_password) as TextView
        val textLink_privacy = findViewById<View>(R.id.link_privacy) as TextView
        val textLink_uninstall_prey = findViewById<View>(R.id.link_uninstall_prey) as TextView
        val password_btn_login = findViewById<View>(R.id.password_btn_login) as Button
        val password_pass_txt = findViewById<View>(R.id.password_pass_txt) as EditText
        val titilliumWebRegular =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Regular.ttf")
        val titilliumWebBold =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val magdacleanmonoRegular =
            Typeface.createFromAsset(assets, "fonts/MagdaClean/magdacleanmono-regular.ttf")
        device_ready_h2_text.setTypeface(titilliumWebRegular)
        textForgotPassword.setTypeface(titilliumWebBold)
        textLink_privacy.setTypeface(titilliumWebBold)
        textLink_uninstall_prey.setTypeface(titilliumWebBold)
        password_btn_login.setTypeface(titilliumWebBold)
        password_pass_txt.setTypeface(magdacleanmonoRegular)
        try {
            textForgotPassword.setOnClickListener {
                try {
                    val url: String =
                        PreyConfig.getInstance(applicationContext).getPreyPanelUrl()
                    val browserIntent =
                        Intent("android.intent.action.VIEW", Uri.parse(url))
                    startActivity(browserIntent)
                } catch (e: Exception) {
                }
            }
            textLink_privacy.setOnClickListener {
                startActivity(Intent(applicationContext, PrivacyActivity::class.java))
                finish()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        textLink_uninstall_prey.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            val url = applicationContext.getString(R.string.uninstall_prey_link)
            i.setData(Uri.parse(url))
            startActivity(i)
        }
    }

    override fun onBackPressed() {
        var intent: Intent? = null
        intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}