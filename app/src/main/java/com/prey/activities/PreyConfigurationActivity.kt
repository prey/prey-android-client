/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.preference.Preference.OnPreferenceClickListener
import android.preference.PreferenceActivity
import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyStatus
import com.prey.PreyLogger
import com.prey.backwardcompatibility.FroyoSupport

class PreyConfigurationActivity : PreferenceActivity() {
    override fun onBackPressed() {
        var intent: Intent? = null
        intent = Intent(application, CheckPasswordActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preferences_5)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onResume() {
        super.onResume()
        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("EXIT", true)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            finish()
        }
        val preyConfig: PreyConfig = PreyConfig.getInstance(applicationContext)
        var p = findPreference("PREFS_ADMIN_DEVICE")
        try {
            if (preyConfig.isFroyoOrAbove()) {
                if (FroyoSupport.getInstance(applicationContext)!!.isAdminActive) {
                    p.setTitle(R.string.preferences_admin_enabled_title)
                    p.setSummary(R.string.preferences_admin_enabled_summary)
                } else {
                    p.setTitle(R.string.preferences_admin_disabled_title)
                    p.setSummary(R.string.preferences_admin_disabled_summary)
                }
            } else p.isEnabled = false
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        p = findPreference("PREFS_ABOUT")
        p.summary = ("Version " + preyConfig.getPreyVersion()).toString() + " - Prey Inc."
        val pGo = findPreference("PREFS_GOTO_WEB_CONTROL_PANEL")
        pGo.onPreferenceClickListener = OnPreferenceClickListener {
            val url: String = PreyConfig.getInstance(applicationContext).getPreyPanelUrl()
            PreyLogger.d("url control:$url")
            val internetIntent = Intent(Intent.ACTION_VIEW)
            internetIntent.setData(Uri.parse(url))
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            false
        }
        PreyStatus.getInstance().setPreyConfigurationActivityResume(false)
    }

    override fun onPause() {
        super.onPause()
    }
}