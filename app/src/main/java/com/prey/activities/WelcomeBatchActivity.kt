/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.content.pm.ActivityInfo
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

class WelcomeBatchActivity : FragmentActivity() {
    private var error: String? = null

    public override fun onResume() {
        PreyLogger.d("onResume of WelcomeBatchActivity")
        super.onResume()
    }

    public override fun onPause() {
        PreyLogger.d("onPause of WelcomeBatchActivity")
        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        menu()
        if (PreyConfig.getInstance(this).isAskForNameBatch()) {
            setContentView(R.layout.welcomebatch2)
            try {
                val editTextBatch2 = findViewById<EditText>(R.id.editTextBatch2)
                editTextBatch2.setText(PreyUtils.getNameDevice(this))
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
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

    fun menu() {
        PreyLogger.d("menu ready:" + PreyConfig.getInstance(this).getProtectReady())
        val email: String? = PreyConfig.getInstance(this).getEmail()
        if (email == null || "" == email) {
            PreyConfig.getInstance(this).setProtectReady(false)
            PreyConfig.getInstance(this).setProtectAccount(false)
            PreyConfig.getInstance(this).setProtectTour(false)
        }
    }

    private fun installBatch(name: String) {
        error = null
        val config: PreyConfig = PreyConfig.getInstance(this)
        //TODO:cambiar
        /*
        AddDeviceToApiKeyBatch().execute(
            config.getApiKeyBatch(), config.getEmailBatch(), PreyUtils.getDeviceType(
                this
            ), name
        )*/
    }

    //TODO:cambiar
    /*
    private inner class AddDeviceToApiKeyBatch : AsyncTask<String?, Void?, Void?>() {
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            try {
                progressDialog = ProgressDialog(this@WelcomeBatchActivity)
                progressDialog!!.setMessage(getText(R.string.set_old_user_loading).toString())
                progressDialog!!.isIndeterminate = true
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }

        protected override fun doInBackground(vararg data: String): Void? {
            try {
                error = null
                val ctx = applicationContext
                val apiKey = data[0]
                PreyConfig.getInstance(ctx).registerNewDeviceWithApiKey(apiKey)
            } catch (e: Exception) {
                PreyLogger.e(String.format("Error:%s", e.message), e)
                error = e.message
            }
            return null
        }

        override fun onPostExecute(unused: Void?) {
            if (progressDialog != null) progressDialog!!.dismiss()
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
    }*/
}