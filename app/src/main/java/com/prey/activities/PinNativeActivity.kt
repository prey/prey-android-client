/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger
import java.util.Calendar
import java.util.Date

class PinNativeActivity : Activity() {
    var button_Super_Lock_Unlock: Button? = null
    var button_close: Button? = null
    var textViewPin: TextView? = null
    var editTextPin: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.super_lock)
        PreyLogger.d("PinNativeActivity: onCreate")
        editTextPin = findViewById<View>(R.id.editTextPin) as EditText
        textViewPin = findViewById<View>(R.id.textViewPin) as TextView
        button_Super_Lock_Unlock = findViewById<View>(R.id.button_Super_Lock_Unlock) as Button
        button_close = findViewById<View>(R.id.button_close) as Button
        val regularBold = Typeface.createFromAsset(assets, "fonts/Regular/regular-bold.otf")
        val regularBook = Typeface.createFromAsset(assets, "fonts/Regular/regular-book.otf")
        editTextPin!!.setTypeface(regularBold)
        textViewPin!!.setTypeface(regularBook)
        button_Super_Lock_Unlock!!.setTypeface(regularBook)
        editTextPin!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editTextPin!!.setBackgroundColor(Color.WHITE)
            }

            override fun afterTextChanged(s: Editable) {
                editTextPin!!.setBackgroundColor(Color.WHITE)
            }
        })
        button_close!!.setOnClickListener { finish() }
        button_Super_Lock_Unlock!!.setOnClickListener {
            val pin = editTextPin!!.text.toString()
            if (pin != null) {
                val pinNumber: String? =
                    PreyConfig.getInstance(applicationContext).getPinNumber()
                PreyLogger.d("pinNumber:$pinNumber pin:$pin")
                if (pinNumber == pin) {
                    PreyConfig.getInstance(applicationContext).setPinActivated("")
                    PreyConfig.getInstance(applicationContext).setCounterOff(0)
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = Date().time
                    cal.add(Calendar.MINUTE, 1)
                    PreyConfig.getInstance(applicationContext).setTimeSecureLock(cal.timeInMillis)
                    finish()
                } else {
                    PreyLogger.d("error")
                    editTextPin!!.setBackgroundColor(Color.RED)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val pinActivated: String? = PreyConfig.getInstance(applicationContext).getPinActivated()
        PreyLogger.d("PinNativeActivity unlock:$pinActivated")
        if (pinActivated == null || "" == pinActivated) {
            val intent = Intent(applicationContext, CloseActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show()
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show()
        }
    }
}