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

/**
 * This activity is responsible for displaying the configuration preferences for the Prey app.
 */
class PreyConfigurationActivity : PreferenceActivity() {

    /**
     * Called when the user presses the back button.
     * Redirects the user to the CheckPasswordActivity.
     */
    override fun onBackPressed() {
        val intent = Intent(application, CheckPasswordActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    /**
     * Called when the activity is created.
     * Initializes the preferences from the resources.
     *
     * @param savedInstanceState Saved instance state
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_5)
    }

    /**
     * Called when the activity is resumed.
     * Checks if the user is allowed to access the configuration activity.
     * If not, redirects the user to the LoginActivity.
     */
    override fun onResume() {
        super.onResume()
        if (!PreyStatus.getInstance().isPreyConfigurationActivityResume()) {
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("EXIT", true)
            startActivity(intent)
            finish()
        }
        val preyConfig: PreyConfig = PreyConfig.getInstance(applicationContext)
        var p = findPreference("PREFS_ADMIN_DEVICE")
        try {
            if (preyConfig.isFroyoOrAbove()) {
                if (FroyoSupport.getInstance(applicationContext).isAdminActive()) {
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

}