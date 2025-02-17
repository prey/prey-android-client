/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity

import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils

/**
 * Activity for displaying a welcome screen and handling the batch installation process.
 */
class WelcomeBatchActivity : FragmentActivity() {
    private var error: String? = null

    /**
     * Called when the activity is resumed.
     */
    public override fun onResume() {
        PreyLogger.d("onResume of WelcomeBatchActivity")
        super.onResume()
    }

    /**
     * Called when the activity is paused.
     */
    public override fun onPause() {
        PreyLogger.d("onPause of WelcomeBatchActivity")
        super.onPause()
    }

    /**
     * Called when the configuration of the activity changes.
     *
     * @param newConfig The new device configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        menu()
        if (PreyConfig.getInstance(this).isAskForNameBatch()) {
            setContentView(R.layout.welcomebatch2)
            try {
                val editTextBatch2 = findViewById<EditText>(R.id.editTextBatch2)
                editTextBatch2.setText(PreyUtils.getNameDevice(this))
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            menu()
            val buttonBatch2 = findViewById<View>(R.id.buttonBatch2) as Button
            buttonBatch2.setOnClickListener {
                val editTextBatch2 =
                    findViewById<View>(R.id.editTextBatch2) as EditText
                val name = editTextBatch2.text.toString()
                if (name != null && "" != name) {
                    installBatch(name)
                } else {
                    Toast.makeText(applicationContext, getText(R.string.error), Toast.LENGTH_LONG)
                        .show()
                }
            }
        } else {
            setContentView(R.layout.welcomebatch)
            menu()
            installBatch("")
        }
    }

    /**
     * Updates the menu based on the protect ready status.
     */
    fun menu() {
        PreyLogger.d("menu ready:${PreyConfig.getInstance(this).getProtectReady()}" )
        val email: String? = PreyConfig.getInstance(this).getEmail()
        if (email == null || "" == email) {
            PreyConfig.getInstance(this).setProtectReady(false)
            PreyConfig.getInstance(this).setProtectAccount(false)
            PreyConfig.getInstance(this).setProtectTour(false)
        }
    }

    /**
     * Installs the batch with the given name.
     *
     * @param name The name of the batch.
     */
    private fun installBatch(name: String) {
        error = null
        val config: PreyConfig = PreyConfig.getInstance(this)
        addDeviceToApiKeyBatch()
    }

    /**
     * Adds the device to the API key batch.
     */
    fun addDeviceToApiKeyBatch() {
        val progressDialog = ProgressDialog(this).apply {
            setMessage(getText(R.string.set_old_user_loading).toString())
            setIndeterminate(true)
            setCancelable(false)
            show()
        }
        try {
            error = null
            val apiKey = PreyConfig.getInstance(this).getApiKeyBatch()
            PreyConfig.getInstance(applicationContext).registerNewDeviceWithApiKey(apiKey)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            error = e.message
        }
        if (progressDialog != null) progressDialog.dismiss()
        if (error == null) {
            val message = getString(R.string.device_added_congratulations_text)
            val bundle = Bundle()
            bundle.putString("message", message)
            PreyConfig.getInstance(this@WelcomeBatchActivity).setCamouflageSet(true)
            val intentPermission =
                Intent(this@WelcomeBatchActivity, PermissionInformationActivity::class.java)
            intentPermission.putExtras(bundle)
            startActivity(intentPermission)
            finish()
        }
    }
}