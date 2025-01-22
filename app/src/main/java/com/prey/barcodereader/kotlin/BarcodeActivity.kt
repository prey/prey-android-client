/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader.kotlin

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.prey.R
import com.prey.activities.kotlin.CheckPasswordHtmlActivity
import com.prey.activities.kotlin.LoginActivity
import com.prey.kotlin.PreyLogger

class BarcodeActivity : Activity() {
    private var autoFocus: CompoundButton? = null
    private var useFlash: CompoundButton? = null
    private var statusMessage: TextView? = null
    private var barcodeValue: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_barcode)
        statusMessage = findViewById<View>(R.id.status_message) as TextView
        barcodeValue = findViewById<View>(R.id.barcode_value) as TextView
        autoFocus = findViewById<View>(R.id.auto_focus) as CompoundButton
        useFlash = findViewById<View>(R.id.use_flash) as CompoundButton
        autoFocus!!.isChecked = true
        val intent = Intent(this, BarcodeCaptureActivity::class.java)
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus!!.isChecked)
        intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash!!.isChecked)
        startActivityForResult(intent, RC_BARCODE_CAPTURE)
    }

    override fun onResume() {
        super.onResume()
        val readBarcodeButton = findViewById<View>(R.id.read_barcode) as Button
        readBarcodeButton.setOnClickListener {
            val intent = Intent(applicationContext, BarcodeCaptureActivity::class.java)
            intent.putExtra(BarcodeCaptureActivity.AutoFocus, autoFocus!!.isChecked)
            intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash!!.isChecked)
            startActivityForResult(intent, RC_BARCODE_CAPTURE)
        }
    }

    override fun onBackPressed() {
        var intent: Intent? = null
        intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent(applicationContext, CheckPasswordHtmlActivity::class.java)
        } else {
            Intent(applicationContext, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    val barcode =
                        data.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
                    statusMessage!!.setText(R.string.barcode_success)
                    PreyLogger.d("Barcode read: " + barcode!!.displayValue)
                    var barcodeValue = barcode.displayValue
                    var apikey = ""
                    val mail = "batch@preyproject.com"
                    if (barcodeValue.indexOf("prey") >= 0) {
                        barcodeValue = barcodeValue.substring(5)
                        if (barcodeValue.indexOf("&") >= 0) {
                            val pairs =
                                barcodeValue.split("&".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            for (pair in pairs) {
                                val llave = pair.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                                PreyLogger.d("key[" + llave[0] + "]" + llave[1])
                                if (llave[0] == "api_key") {
                                    apikey = llave[1]
                                }
                            }
                        } else {
                            val llave =
                                barcodeValue.split("=".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            PreyLogger.d("key[" + llave[0] + "]" + llave[1])
                            if (llave[0] == "api_key") {
                                apikey = llave[1]
                            }
                        }
                        if ("" != apikey) {
                            //TODO:cambiar
                           // AddDeviceToApiKeyBatch().execute(   apikey, mail, PreyUtils.getDeviceType(   this     ) )
                        }
                    }
                } else {
                    statusMessage!!.setText(R.string.barcode_failure)
                    PreyLogger.d("No barcode captured, intent data is null")
                }
            } else {
                statusMessage!!.text = String.format(
                    getString(R.string.barcode_error),
                    CommonStatusCodes.getStatusCodeString(resultCode)
                )
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    var error: String? = null
    private val noMoreDeviceError = false
    //TODO:cambiar
    /*
    private inner class AddDeviceToApiKeyBatch : AsyncTask<String?, Void?, Void?>() {
        var progressDialog: ProgressDialog? = null

        override fun onPreExecute() {
            progressDialog = ProgressDialog(this@BarcodeActivity)
            progressDialog!!.setMessage(getText(R.string.set_old_user_loading).toString())
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
        }

         fun doInBackground(vararg data: String): Void? {
            error = null
            try {
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
            try {
                progressDialog!!.dismiss()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            PreyLogger.d("error[$error]")
            if (error == null) {
                val message = getString(R.string.device_added_congratulations_text)
                val bundle = Bundle()
                bundle.putString("message", message)
                bundle.putString("nexturl", "tryReport")
                PreyConfig.getInstance(applicationContext).setCamouflageSet(true)
                var intent: Intent? = null
                intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Intent(applicationContext, CheckPasswordHtmlActivity::class.java)
                } else {
                    Intent(applicationContext, PermissionInformationActivity::class.java)
                }
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            } else {
                showDialog(ERROR)
            }
        }
    }*/

    override fun onCreateDialog(id: Int): Dialog {
        val pass: Dialog? = null
        when (id) {
            ERROR -> return AlertDialog.Builder(this@BarcodeActivity).setIcon(R.drawable.error)
                .setTitle(R.string.error_title).setMessage(error)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, which -> }).setCancelable(false)
                .create()

            NO_MORE_DEVICES_WARNING -> return AlertDialog.Builder(
                this@BarcodeActivity
            ).setIcon(R.drawable.info).setTitle(R.string.set_old_user_no_more_devices_title)
                .setMessage(error)
                .setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { dialog, which -> }).setCancelable(false)
                .create()
        }
        return pass!!
    }

    override fun onPrepareDialog(id: Int, dialog: Dialog) {
        var ad: AlertDialog? = null
        when (id) {
            ERROR -> {
                ad = dialog as AlertDialog
                ad.setIcon(R.drawable.error)
                ad.setTitle(R.string.error_title)
                ad.setMessage(error)
                ad.setButton(
                    DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok)
                ) { dialog, id -> }
                ad.setCancelable(false)
            }

            NO_MORE_DEVICES_WARNING -> {
                ad = dialog as AlertDialog
                ad.setIcon(R.drawable.info)
                ad.setTitle(R.string.set_old_user_no_more_devices_title)
                ad.setMessage(error)
                ad.setButton(
                    DialogInterface.BUTTON_POSITIVE, this.getString(R.string.ok)
                ) { dialog, id -> }
                ad.setCancelable(false)
            }

            else -> super.onPrepareDialog(id, dialog)
        }
    }

    companion object {
        private const val RC_BARCODE_CAPTURE = 9001

        private const val NO_MORE_DEVICES_WARNING = 0
        private const val ERROR = 3
    }
}