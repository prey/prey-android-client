/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import com.prey.kotlin.FileConfigReader
import com.prey.kotlin.PreyLogger
import com.prey.util.HttpUtil
import com.prey.util.KeyboardStatusDetector
import java.util.Locale

class SignUpActivity : Activity() {
    private var error: String? = null
    private var email: String? = null
    private var htmTerms = ""

    public override fun onResume() {
        PreyLogger.d("onResume of SignUpActivity")
        super.onResume()
    }

    public override fun onPause() {
        PreyLogger.d("onPause of SignUpActivity")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
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
        val ctx: Context = this
        var urlTerms = ""
        urlTerms = if ("es" == Locale.getDefault().language) {
            FileConfigReader.getInstance(applicationContext)!!.preyTermsEs
        } else {
            FileConfigReader.getInstance(applicationContext)!!.preyTerms
        }
        PreyLogger.d("urlTerms:$urlTerms")
        htmTerms = HttpUtil.getContents(urlTerms)
        val regex = "<a href.*?>"
        htmTerms = htmTerms.replace(regex.toRegex(), "<a>")
        val builder = AlertDialog.Builder(ctx)
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
                PreyLogger.e("e.getMessage():" + e.message, e)
            }
        }
        text_linear_agree_terms_condition.setOnClickListener {
            try {
                alert.show()
            } catch (e: Exception) {
                PreyLogger.e("e.getMessage():" + e.message, e)
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
        buttonSignup.setOnClickListener {
            val name = nameText.text.toString()
            email = emailText.text.toString()
            val password = passwordText.text.toString()
            val confirm_over = "" + checkBox_linear_confirm_over.isChecked
            val agree_terms_condition = "" + checkBox_linear_agree_terms_condition.isChecked
            val offer = "" + checkBox_linear_offer.isChecked
            PreyLogger.d("email:$email")
            PreyLogger.d("password:$password")
            PreyLogger.d("confirm_over:$confirm_over")
            PreyLogger.d("agree_terms_condition:$agree_terms_condition")
            PreyLogger.d("offer:$offer")
            val ctx = applicationContext
            //TODO:cambiar
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) CreateAccount(ctx).executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                name,
                email,
                password,
                confirm_over,
                agree_terms_condition,
                offer
            )
            else CreateAccount(ctx).execute(
                name,
                email,
                password,
                confirm_over,
                agree_terms_condition,
                offer
            )*/
        }
        linkSignup.setOnClickListener {
            val intent = Intent(applicationContext, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //TODO:cambiar
    /*
    private inner class CreateAccount(var context: Context) :
        AsyncTask<String?, Void?, Void?>() {
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            try {
                progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog!!.setMessage(getText(R.string.creating_account_please_wait).toString())
                progressDialog!!.isIndeterminate = true
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
            } catch (e: Exception) {
                PreyLogger.e("e.getMessage():" + e.message, e)
            }
            error = null
        }

        protected override fun doInBackground(vararg data: String): Void? {
            try {
                error = null
                val ctx = applicationContext
                val accountData: PreyAccountData = PreyWebServices.getInstance().registerNewAccount(
                    ctx,
                    data[0], data[1], data[2], data[3], data[4], data[5], PreyUtils.getDeviceType(
                        application
                    )
                )
                PreyLogger.d("Response creating account: " + accountData.toString())
                PreyConfig.getInstance(ctx).saveAccount(accountData)
                PreyConfig.getInstance(ctx).registerC2dm()
                PreyConfig.getInstance(ctx).setEmail(email)
                PreyConfig.getInstance(ctx).setRunBackground(true)
                RunBackgroundCheckBoxPreference.notifyReady(ctx)
                PreyApp().run(ctx)
            } catch (e: Exception) {
                error = e.message
                PreyLogger.e("e.getMessage():" + e.message, e)
            }
            return null
        }

        override fun onPostExecute(unused: Void?) {
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
                }
            } catch (e: Exception) {
                PreyLogger.e("e.getMessage():" + e.message, e)
            }
        }
    }
*/
    companion object {
        private const val ERROR = 1
    }
}