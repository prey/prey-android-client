/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.prey.R
import com.prey.barcodereader.BarcodeActivity
import com.prey.PreyLogger
import com.prey.util.KeyboardStatusDetector

class SignInActivity : Activity() {
    private var error: String? = null
    private var noMoreDeviceError = false

    public override fun onResume() {
        PreyLogger.d("onResume of SignInActivity")
        super.onResume()
    }

    public override fun onPause() {
        PreyLogger.d("onPause of SignInActivity")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        this.setContentView(R.layout.signin)
        PreyLogger.d("onCreate of SignInActivity")
        val buttonSignin = findViewById<View>(R.id.buttonSignin) as Button
        val emailText = (findViewById<View>(R.id.editTextEmailAddress) as EditText)
        val passwordText = (findViewById<View>(R.id.editTextPassword) as EditText)
        val ctx: Context = this
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
        //TODO:cambiar
        /*
        keyboard.setVisibilityListener { keyboardVisible ->
            try {
                val params = linkSignin.layoutParams as RelativeLayout.LayoutParams
                if (keyboardVisible) {
                    PreyLogger.d("key on")
                    params.setMargins(20, 0, 20, halfHeight)
                } else {
                    PreyLogger.d("key off")
                    params.setMargins(20, 0, 20, 20)
                }
                linkSignin.layoutParams = params
            } catch (e: Exception) {
                PreyLogger.e("error:" + e.message, e)
            }
        }*/
        buttonSignin.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()
//TODO:cambiar
/*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) AddDeviceToAccount().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR,
            email,
            password,
            PreyUtils.getDeviceType(ctx)
        )
        else AddDeviceToAccount().execute(email, password, PreyUtils.getDeviceType(ctx))
        */
    }
    linkSignin.setOnClickListener {
        val intent = Intent(applicationContext, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }
    val imageViewQr = findViewById<View>(R.id.imageViewQR) as ImageView
    imageViewQr.setOnClickListener {
        val intent = Intent(applicationContext, BarcodeActivity::class.java)
        startActivity(intent)
        finish()
    }
}

//TODO:cambiar
/*
private inner class AddDeviceToAccount : AsyncTask<String?, Void?, Void?>() {
    var progressDialog: ProgressDialog? = null
    override fun onPreExecute() {
        try {
            progressDialog = ProgressDialog(this@SignInActivity)
            progressDialog!!.setMessage(getText(R.string.set_old_user_loading).toString())
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        } catch (e: Exception) {
            PreyLogger.e("error:" + e.message, e)
        }
    }

    protected override fun doInBackground(vararg data: String): Void? {
        try {
            noMoreDeviceError = false
            error = null
            val accountData: PreyAccountData =
                PreyWebServices.getInstance().registerNewDeviceToAccount(
                    this@SignInActivity,
                    data[0], data[1], data[2]
                )
            val ctx = applicationContext
            PreyConfig.getInstance(ctx).saveAccount(accountData)
            PreyConfig.getInstance(ctx).registerC2dm()
            val email = PreyWebServices.getInstance().getEmail(ctx)
            PreyConfig.getInstance(ctx).setEmail(email)
            PreyConfig.getInstance(ctx).setRunBackground(true)
            RunBackgroundCheckBoxPreference.notifyReady(ctx)
            PreyApp().run(ctx)
            Location()[ctx, null, null]
        } catch (e: Exception) {
            PreyLogger.e("error:" + e.message, e)
            error = e.message
        }
        return null
    }

    override fun onPostExecute(unused: Void?) {
        try {
            if (progressDialog != null) progressDialog!!.dismiss()
            if (error == null) {
                val message = getString(R.string.device_added_congratulations_text)
                val bundle = Bundle()
                bundle.putString("message", message)
                var intent: Intent? = null
                if (PreyConfig.getInstance(this@SignInActivity).isChromebook()) {
                    intent = Intent(this@SignInActivity, WelcomeActivity::class.java)
                    PreyConfig.getInstance(this@SignInActivity).setProtectReady(true)
                } else {
                    intent = Intent(
                        this@SignInActivity,
                        PermissionInformationActivity::class.java
                    )
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
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
            }
        } catch (e: Exception) {
            PreyLogger.e("error:" + e.message, e)
        }
    }
}*/

companion object {
    private const val NO_MORE_DEVICES_WARNING = 0
    private const val ERROR = 3
}
}