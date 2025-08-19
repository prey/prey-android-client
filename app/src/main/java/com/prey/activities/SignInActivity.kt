/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

import com.prey.PreyAccountData
import com.prey.PreyApp
import com.prey.PreyConfig
import com.prey.R
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.util.KeyboardStatusDetector

/**
 * Activity for signing in to the app.
 */
class SignInActivity : Activity() {
    private var error: String? = null
    private var noMoreDeviceError = false

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.signin)
        PreyLogger.d("onCreate of SignInActivity")
        val buttonSignin = findViewById<View>(R.id.buttonSignin) as Button
        val emailText = (findViewById<View>(R.id.editTextEmailAddress) as EditText)
        val passwordText = (findViewById<View>(R.id.editTextPassword) as EditText)
        val context: Context = this
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val halfHeight = metrics.heightPixels / 3
        val linkSignin = findViewById<View>(R.id.linkSignin) as TextView
        val magdacleanmonoRegular =
            Typeface.createFromAsset(assets, "fonts/MagdaClean/magdacleanmono-regular.ttf")
        val titilliumWebBold =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val textViewInit1 = findViewById<View>(R.id.textViewInit1) as TextView
        val textViewInit2 = findViewById<View>(R.id.textViewInit2) as TextView
        val editTextEmailAddress = findViewById<View>(R.id.editTextEmailAddress) as EditText
        val editTextPassword = findViewById<View>(R.id.editTextPassword) as EditText
        textViewInit1.setTypeface(magdacleanmonoRegular)
        textViewInit2.setTypeface(titilliumWebBold)
        buttonSignin.setTypeface(titilliumWebBold)
        linkSignin.setTypeface(titilliumWebBold)
        editTextEmailAddress.setTypeface(magdacleanmonoRegular)
        editTextPassword.setTypeface(magdacleanmonoRegular)
        val keyboard = KeyboardStatusDetector()
        keyboard.registerActivity(this)
        PreyConfig.getInstance(applicationContext).setActivityView(SIGN_IN_ACTIVITY_FORM)
        // Set click listener for sign in button
        buttonSignin.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            addDeviceToAccount(email, password)
        }
        // Set click listener for sign up link
        linkSignin.setOnClickListener {
            val intent = Intent(applicationContext, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Set click listener for QR code image
        val imageViewQr = findViewById<View>(R.id.imageViewQR) as ImageView
        imageViewQr.setOnClickListener {
            val intent = Intent(applicationContext, BarcodeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Adds a device to an account.
     *
     * @param mail The email address of the account.
     * @param password The password for the account.
     */
    fun addDeviceToAccount(
        mail: String,
        password: String
    ) {
        var progressDialog: ProgressDialog? = null
        val context = applicationContext
        try {
            progressDialog = ProgressDialog(this@SignInActivity)
            progressDialog.setMessage(getText(R.string.set_old_user_loading).toString())
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            noMoreDeviceError = false
            error = null
            val accountData: PreyAccountData? =
                PreyConfig.getInstance(context).getWebServices().registerNewDeviceToAccount(
                    context,
                    mail, password, PreyUtils.getDeviceType(context)
                )
            PreyConfig.getInstance(context).saveAccount(accountData!!)
            PreyConfig.getInstance(context).registerC2dm()
            val email = PreyConfig.getInstance(context).getWebServices().getEmail(context)
            PreyConfig.getInstance(context).setEmail(email!!)
            PreyConfig.getInstance(context).setRunBackground(true)
            PreyApp().initialize(context)
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
            error = e.message
        }
        try {
            if (progressDialog != null) progressDialog!!.dismiss()
            if (error == null) {
                val message = getString(R.string.device_added_congratulations_text)
                val bundle = Bundle()
                bundle.putString("message", message)
                var intent: Intent? = null
                if (PreyConfig.getInstance(context).isChromebook()) {
                    intent = Intent(context, WelcomeActivity::class.java)
                    PreyConfig.getInstance(context).setProtectReady(true)
                } else {
                    intent = Intent(
                        this@SignInActivity,
                        PermissionInformationActivity::class.java
                    )
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                PreyConfig.getInstance(applicationContext).setActivityView(SIGN_IN_ACTIVITY_WELCOME)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            } else {
                val alertDialog = AlertDialog.Builder(this@SignInActivity)
                alertDialog.setIcon(R.drawable.error).setTitle(R.string.error_title)
                    .setMessage(error)
                    .setPositiveButton(
                        R.string.ok
                    ) { dialog, which -> }.setCancelable(false)
                alertDialog.show()
                PreyConfig.getInstance(applicationContext).setActivityView(SIGN_IN_ACTIVITY_ERROR)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    companion object {
        private const val NO_MORE_DEVICES_WARNING = 0
        private const val ERROR = 3
        const val SIGN_IN_ACTIVITY_FORM: String = "SIGN_IN_ACTIVITY_FORM"
        const val SIGN_IN_ACTIVITY_ERROR: String = "SIGN_IN_ACTIVITY_ERROR"
        const val SIGN_IN_ACTIVITY_WELCOME: String = "SIGN_IN_ACTIVITY_WELCOME"
    }

}