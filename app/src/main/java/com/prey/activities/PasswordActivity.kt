/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyStatus
import com.prey.R
import com.prey.events.Event
import com.prey.events.manager.EventManagerRunner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity responsible for handling password entry and validation.
 */
class PasswordActivity : PreyActivity() {
    /**
     * Counter for the number of wrong password intents.
     */
    var wrongPasswordIntents: Int = 0

    /**
     * Called when the activity is created.
     * Initializes the activity and sets up the window feature.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.password2)
        bindPasswordControls()
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_PASSWORD)
    }

    /**
     * Binds the password controls to their respective listeners.
     */
    protected fun bindPasswordControls() {
        val checkPasswordOkButton = findViewById<View>(R.id.password_btn_login) as Button
        val pass1 = (findViewById<View>(R.id.password_pass_txt) as EditText)
        checkPasswordOkButton.setOnClickListener {
            val passwordtyped = pass1.text.toString()
            val context = applicationContext
            if (passwordtyped == "") Toast.makeText(
                context,
                R.string.preferences_password_length_error,
                Toast.LENGTH_LONG
            ).show()
            else {
                if (passwordtyped.length < 6 || passwordtyped.length > 32) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_password_out_of_range, "6", "32"),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    checkPassword(passwordtyped)
                }
            }
        }
        val password = findViewById<View>(R.id.password_pass_txt) as EditText
        password.setTypeface(Typeface.DEFAULT)
        password.transformationMethod = PasswordTransformationMethod()
    }

    /**
     * Checks the password against the server.
     *
     * @param password The password to check.
     */
    fun checkPassword(password: String) {
        var progressDialog: ProgressDialog? = null
        var isPasswordOk: Boolean = false
        var keepAsking: Boolean = true
        var error: String? = null
        val context = applicationContext
        try {
            progressDialog = ProgressDialog(this@PasswordActivity)
            progressDialog!!.setMessage(getText(R.string.password_checking_dialog).toString())
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        } catch (e: Exception) {
        }
        try {
            val apikey: String = PreyConfig.getInstance(context).getApiKey()!!
            PreyLogger.d("apikey:${apikey} password:${password}")
            isPasswordOk = PreyConfig.getInstance(context).getWebServices().checkPassword(
                this@PasswordActivity, apikey,
                password
            )
        } catch (e: Exception) {
            error = e.message
        }
        try {
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
        }
        if (error != null) Toast.makeText(this@PasswordActivity, error, Toast.LENGTH_LONG)
            .show()
        else if (!isPasswordOk) {
            wrongPasswordIntents++
            if (wrongPasswordIntents == 3) {
                Toast.makeText(
                    this@PasswordActivity,
                    R.string.password_intents_exceed,
                    Toast.LENGTH_LONG
                ).show()
                setResult(RESULT_CANCELED)
                finish()
            } else {
                Toast.makeText(
                    this@PasswordActivity,
                    R.string.password_wrong,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val intentConfiguration = Intent(
                this@PasswordActivity,
                PreyConfigurationActivity::class.java
            )
            PreyStatus.getInstance().setPreyConfigurationActivityResume(true)
            startActivity(intentConfiguration)
            CoroutineScope(Dispatchers.IO).launch {
                EventManagerRunner(
                    this@PasswordActivity,
                    Event(Event.APPLICATION_OPENED)
                )
            }
        }
    }

    companion object {
        const val ACTIVITY_PASSWORD: String = "ACTIVITY_PASSWORD"
    }

}