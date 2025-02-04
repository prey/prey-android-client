/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.graphics.Typeface
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.prey.R

class PasswordActivity : PreyActivity() {
    var wrongPasswordIntents: Int = 0

    protected fun bindPasswordControls() {
        val checkPasswordOkButton = findViewById<View>(R.id.password_btn_login) as Button
        val pass1 = (findViewById<View>(R.id.password_pass_txt) as EditText)
        checkPasswordOkButton.setOnClickListener {
            val passwordtyped = pass1.text.toString()
            val ctx = applicationContext
            if (passwordtyped == "") Toast.makeText(
                ctx,
                R.string.preferences_password_length_error,
                Toast.LENGTH_LONG
            ).show()
            else {
                if (passwordtyped.length < 6 || passwordtyped.length > 32) {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.error_password_out_of_range, "6", "32"),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    //TODO:cambiar
                    /*
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) CheckPassword()
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, passwordtyped)
                    else CheckPassword().execute(passwordtyped)

                     */
                }
            }
        }
        val password = findViewById<View>(R.id.password_pass_txt) as EditText
        password.setTypeface(Typeface.DEFAULT)
        password.transformationMethod = PasswordTransformationMethod()
    }
//TODO:cambiar
/*
protected inner class CheckPassword : AsyncTask<String?, Void?, Void?>() {
var progressDialog: ProgressDialog? = null
var isPasswordOk: Boolean = false
var keepAsking: Boolean = true
var error: String? = null
override fun onPreExecute() {
    try {
        progressDialog = ProgressDialog(this@PasswordActivity)
        progressDialog!!.setMessage(getText(R.string.password_checking_dialog).toString())
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
    } catch (e: Exception) {
    }
}
//TODO:cambiar
/*
protected override fun doInBackground(vararg password: String): Void? {
    try {
        val apikey: String = preyConfig.getApiKey()
        PreyLogger.d(String.format("apikey:%s password:%s", apikey, password[0]))
        isPasswordOk = PreyWebServices.getInstance().checkPassword(
            this@PasswordActivity, apikey,
            password[0]
        )
    } catch (e: Exception) {
        error = e.message
    }
    return null
}*/

override fun onPostExecute(unused: Void?) {
    try {
        if (progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }
    } catch (e: Exception) {
        PreyLogger.e("Error:" + e.message, e)
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
        Thread(
            EventManagerRunner(
                this@PasswordActivity,
                Event(Event.APPLICATION_OPENED)
            )
        ).start()
    }
}
}*/
}