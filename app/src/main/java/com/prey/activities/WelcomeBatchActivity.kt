/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.R

/**
 * Activity for displaying a welcome screen and handling the batch installation process.
 */
class WelcomeBatchActivity : Activity(), View.OnClickListener {
    private var error: String? = null

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState The saved instance state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        //menu()
        if (PreyConfig.getInstance(this).isAskForNameBatch()) {
            setContentView(R.layout.welcomebatch2)
            try {
                val editTextBatch2 = findViewById<EditText>(R.id.editTextBatch2)
                editTextBatch2.setText(PreyUtils.getNameDevice(this))
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            menu()
            findViewById<View>(R.id.buttonBatch2).setOnClickListener(this)
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_ASK)
        } else {
            setContentView(R.layout.welcomebatch)
            menu()
            installBatch("")
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_NOT_ASK)
        }
    }

    @Override
    override fun onClick(view: View) {
        val editTextBatch2 =
            findViewById<View>(R.id.editTextBatch2) as EditText
        var name = ""
        try {
            name = editTextBatch2.text.toString()
        } catch (e: java.lang.Exception) {
        }
        if (name.isNotEmpty()) {
            installBatch(name)
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_ASK_NAME)
        } else {
            Toast.makeText(applicationContext, getText(R.string.error), Toast.LENGTH_LONG)
                .show()
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_ASK_EMPTY)
        }
    }

    /**
     * Updates the menu based on the protect ready status.
     */
    fun menu() {
        PreyLogger.d("menu ready:${PreyConfig.getInstance(this).getProtectReady()}")
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
            PreyConfig.getInstance(applicationContext).registerNewDeviceWithApiKey(apiKey!!)
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

    companion object {
        const val ACTIVITY_ASK: String = "WelcomeBatchActivity_ask"
        const val ACTIVITY_ASK_EMPTY: String = "WelcomeBatchActivity_ask_empty"
        const val ACTIVITY_NOT_ASK: String = "WelcomeBatchActivity_not_ask"
        const val ACTIVITY_ASK_NAME: String = "WelcomeBatchActivity_not_name"
    }
}