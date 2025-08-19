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
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView

import com.prey.R
import com.prey.FileConfigReader
import com.prey.PreyAccountData
import com.prey.PreyApp
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyUtils
import com.prey.util.HttpUtil
import com.prey.util.KeyboardStatusDetector

import java.util.Locale

/**
 * SignUpActivity is the activity responsible for handling the sign-up process.
 */
class SignUpActivity : Activity() {
    private var error: String? = null
    private var email: String? = null
    private var htmTerms = ""

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.signup)
        PreyLogger.d("onCreate of SignUpActivity")
        val nameText = (findViewById<View>(R.id.editTextName) as EditText)
        val emailText = (findViewById<View>(R.id.editTextEmailAddress) as EditText)
        val passwordText = (findViewById<View>(R.id.editTextPassword) as EditText)
        val buttonSignup = findViewById<View>(R.id.buttonSignup) as Button
        val linkSignup = findViewById<View>(R.id.linkSignup) as TextView
        val checkBox_linear_agree_terms_condition =
            findViewById<View>(R.id.checkBox_linear_agree_terms_condition) as CheckBox
        val checkBox_linear_confirm_over =
            findViewById<View>(R.id.checkBox_linear_confirm_over) as CheckBox
        val checkBox_linear_offer = findViewById<View>(R.id.checkBox_linear_offer) as CheckBox
        val magdacleanmonoRegular =
            Typeface.createFromAsset(assets, "fonts/MagdaClean/magdacleanmono-regular.ttf")
        val titilliumWebBold =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val textViewInit1 = findViewById<View>(R.id.textViewInit1) as TextView
        val textViewInit2 = findViewById<View>(R.id.textViewInit2) as TextView
        val text_linear_agree_terms_condition =
            findViewById<View>(R.id.text_linear_agree_terms_condition) as TextView
        val text_linear_confirm_over = findViewById<View>(R.id.text_linear_confirm_over) as TextView
        textViewInit1.setTypeface(magdacleanmonoRegular)
        textViewInit2.setTypeface(titilliumWebBold)
        text_linear_agree_terms_condition.setTypeface(titilliumWebBold)
        text_linear_confirm_over.setTypeface(titilliumWebBold)
        val context: Context = this
        var urlTerms = ""
        urlTerms = if ("es" == Locale.getDefault().language) {
            FileConfigReader.getInstance(applicationContext)!!.getPreyTermsEs()
        } else {
            FileConfigReader.getInstance(applicationContext)!!.getPreyTerms()
        }
        PreyLogger.d("urlTerms:$urlTerms")
        htmTerms = HttpUtil().getContents(urlTerms)
        val regex = "<a href.*?>"
        htmTerms = htmTerms.replace(regex.toRegex(), "<a>")
        val builder = AlertDialog.Builder(context)
        val alert = builder.create()
        val wv = WebView(applicationContext)
        wv.webChromeClient = WebChromeClient()
        wv.settings.javaScriptEnabled = true
        wv.settings.loadWithOverviewMode = true
        wv.settings.useWideViewPort = false
        wv.settings.setSupportZoom(false)
        wv.loadData(htmTerms, "text/html", "UTF-8")
        alert.setView(wv)
        alert.setButton(
            getString(R.string.warning_close)
        ) { dialog, id ->
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
        text_linear_agree_terms_condition.setOnClickListener {
            try {
                alert.show()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
        }
        linkSignup.setTypeface(titilliumWebBold)
        buttonSignup.setTypeface(titilliumWebBold)
        nameText.setTypeface(magdacleanmonoRegular)
        emailText.setTypeface(magdacleanmonoRegular)
        passwordText.setTypeface(magdacleanmonoRegular)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val halfHeight = metrics.heightPixels / 3
        val keyboard = KeyboardStatusDetector()
        keyboard.registerActivity(this)
        PreyConfig.getInstance(applicationContext).setActivityView(SIGN_UP_ACTIVITY_FORM)
        buttonSignup.setOnClickListener {
            val name = nameText.text.toString()
            email = emailText.text.toString()
            val password = passwordText.text.toString()
            val confirmOver = "${checkBox_linear_confirm_over.isChecked}"
            val agreeTermsCondition = "${checkBox_linear_agree_terms_condition.isChecked}"
            val offer = "${checkBox_linear_offer.isChecked}"
            PreyLogger.d("email:$email")
            PreyLogger.d("password:$password")
            PreyLogger.d("confirm_over:$confirmOver")
            PreyLogger.d("agree_terms_condition:$agreeTermsCondition")
            PreyLogger.d("offer:$offer")
            createAccount(
                applicationContext,
                name,
                email,
                password,
                confirmOver,
                agreeTermsCondition,
                offer
            )
        }
        linkSignup.setOnClickListener {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Creates a new account on the server.
     *
     * @param context The application context.
     * @param name The user's name.
     * @param email The user's email address.
     * @param password The user's password.
     * @param confirmOver The confirmation of the user's age.
     * @param agreeTermsCondition Whether the user agrees to the terms and conditions.
     * @param offer The offer code (if any).
     */
    fun createAccount(
        context: Context,
        name: String?,
        email: String?,
        password: String?,
        confirmOver: String?,
        agreeTermsCondition: String?,
        offer: String?
    ) {
        var progressDialog: ProgressDialog? = null
        try {
            progressDialog = ProgressDialog(this@SignUpActivity)
            progressDialog.setMessage(getText(R.string.creating_account_please_wait).toString())
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            error = null
            val context = applicationContext
            val accountData: PreyAccountData =
                PreyConfig.getInstance(context).getWebServices().registerNewAccount(
                    context,
                    name,
                    email,
                    password,
                    confirmOver,
                    agreeTermsCondition,
                    offer, PreyUtils.getDeviceType(
                        application
                    )
                )
            PreyLogger.d("Response creating account: ${accountData.toString()}")
            PreyConfig.getInstance(context).saveAccount(accountData)
            PreyConfig.getInstance(context).registerC2dm()
            PreyConfig.getInstance(context).setEmail(email!!)
            PreyConfig.getInstance(context).setRunBackground(true)
            PreyApp().initialize(context)
        } catch (e: Exception) {
            error = e.message
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            if (progressDialog != null) progressDialog!!.dismiss()
            if (error == null) {
                val message = getString(R.string.new_account_congratulations_text, email)
                val bundle = Bundle()
                bundle.putString("message", message)
                var intent: Intent? = null
                if (PreyConfig.getInstance(this@SignUpActivity).isChromebook()) {
                    intent = Intent(this@SignUpActivity, WelcomeActivity::class.java)
                    PreyConfig.getInstance(this@SignUpActivity).setProtectReady(true)
                } else {
                    intent = Intent(
                        this@SignUpActivity,
                        PermissionInformationActivity::class.java
                    )
                }
                PreyConfig.getInstance(applicationContext).setActivityView(SIGN_UP_ACTIVITY_WELCOME)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            } else {
                val alertDialog = AlertDialog.Builder(this@SignUpActivity)
                alertDialog.setIcon(R.drawable.error).setTitle(R.string.error_title)
                    .setMessage(error)
                    .setPositiveButton(
                        R.string.ok
                    ) { dialog, which -> }.setCancelable(false)
                alertDialog.show()
                PreyConfig.getInstance(applicationContext).setActivityView(SIGN_UP_ACTIVITY_ERROR)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    companion object {
        private const val ERROR = 1
        const val SIGN_UP_ACTIVITY_FORM: String = "SIGN_UP_ACTIVITY_FORM"
        const val SIGN_UP_ACTIVITY_ERROR: String = "SIGN_UP_ACTIVITY_ERROR"
        const val SIGN_UP_ACTIVITY_WELCOME: String = "SIGN_UP_ACTIVITY_WELCOME"
    }

}