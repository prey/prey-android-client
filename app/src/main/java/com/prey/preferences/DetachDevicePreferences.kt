package com.prey.preferences

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.DialogPreference
import android.util.AttributeSet
import android.widget.Toast
import com.prey.PreyLogger
import com.prey.R
import com.prey.activities.LoginActivity
import com.prey.json.actions.Detach

class DetachDevicePreferences : DialogPreference {
    var ctx: Context? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        this.ctx = context
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        this.ctx = context
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        super.onClick(dialog, which)
        if (which == DialogInterface.BUTTON_POSITIVE) {
            progressDialog = ProgressDialog(context)
            progressDialog!!.setMessage(
                context.getText(R.string.preferences_detach_dettaching_message).toString()
            )
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.show()
            error = Detach().detachDevice(context)
            PreyLogger.d("Error:$error")
            try {
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
            } catch (e: Exception) {
                PreyLogger.e("Error: ${e.message}", e)
            }
            try {
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                    showDialog(Bundle())
                } else {
                    val welcome = Intent(
                        context,
                        LoginActivity::class.java
                    )
                    context.startActivity(welcome)
                }
            } catch (e: Exception) {
                PreyLogger.e("Error: ${e.message}", e)
            }
        }
    }

    private var error: String? = null
    var progressDialog: ProgressDialog? = null

}