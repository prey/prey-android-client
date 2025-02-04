/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.prey.R
import com.prey.PreyLogger

/****
 * This activity verify that the installer has a valid token
 */
class SplashBatchActivity : FragmentActivity() {
    private var error: String? = null
    private var textSplash: TextView? = null

    public override fun onResume() {
        PreyLogger.d("onResume of SplashBatchActivity")
        super.onResume()
        //TODO:cambiar
        //TokenBatchTask().execute()
    }

    public override fun onPause() {
        PreyLogger.d("onPause of SplashBatchActivity")
        super.onPause()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        PreyLogger.d("onCreate of SplashBatchActivity")
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.splash_batch)
        textSplash = findViewById<View>(R.id.text_splash) as TextView
    }

    /****
     * This asyncTask verify that the installer has a valid token
     */
    //TODO:cambiar
    /*
    private inner class TokenBatchTask : AsyncTask<String?, Void?, Void?>() {
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            try {
                textSplash!!.text = ""
                progressDialog = ProgressDialog(this@SplashBatchActivity)
                progressDialog!!.setMessage(getText(R.string.loading).toString())
                progressDialog!!.isIndeterminate = true
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }

        protected override fun doInBackground(vararg data: String): Void? {
            val ctx = applicationContext
            try {
                error = null
                val token: String = PreyBatch.getInstance(ctx)!!.token
                if (token == null || "" == token) {
                    error = ctx.getString(R.string.error_token)
                } else {
                    val validToken = PreyWebServices.getInstance()
                        .validToken(ctx, PreyBatch.getInstance(ctx)!!.token)
                    if (!validToken) {
                        error = ctx.getString(R.string.error_token)
                    }
                }
            } catch (e: Exception) {
                error = e.message
            }
            return null
        }

        override fun onPostExecute(unused: Void?) {
            if (progressDialog != null) progressDialog!!.dismiss()
            if (error == null) {
                val intentPermission = Intent(
                    this@SplashBatchActivity,
                    WelcomeBatchActivity::class.java
                )
                startActivity(intentPermission)
                finish()
            } else {
                textSplash!!.text = error
            }
        }
    }*/
}