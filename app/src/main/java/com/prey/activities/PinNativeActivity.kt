/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.Activity
import android.content.Intent
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

/**
 * PinNativeActivity is responsible for handling the PIN entry and validation.
 * It sets up the UI, handles text changes, and validates the PIN against the stored PIN number.
 */
class PinNativeActivity : Activity() {
    // UI components
    var button_Super_Lock_Unlock: Button? = null
    var button_close: Button? = null
    var textViewPin: TextView? = null
    var editTextPin: EditText? = null

    /**
     * Called when the activity is created.
     * Sets up the UI, loads the fonts, and sets up the text watchers and click listeners.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                    PreyConfig.getInstance(applicationContext)
                        .setActivityView(ACTIVITY_PIN_BUTTON_OK)
                    finish()
                } else {
                    PreyLogger.d("error")
                    editTextPin!!.setBackgroundColor(Color.RED)
                    PreyConfig.getInstance(applicationContext)
                        .setActivityView(ACTIVITY_PIN_BUTTON_ERROR)
                }
            }
        }
    }

    /**
     * Called when the activity is resumed.
     * Checks if the PIN is activated and if not, starts the CloseActivity.
     */
    override fun onResume() {
        super.onResume()
        val pinActivated: String? = PreyConfig.getInstance(applicationContext).getPinActivated()
        PreyLogger.d("PinNativeActivity unlock:$pinActivated")
        if (pinActivated == null || "" == pinActivated) {
            val intent = Intent(applicationContext, CloseActivity::class.java)
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_PIN_CLOSE)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        } else {
            PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_PIN_FORM)
        }
    }

    /**
     * Called when the configuration changes.
     * Checks if a hardware keyboard is available and shows a toast message accordingly.
     *
     * @param newConfig The new configuration.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks whether a hardware keyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show()
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val ACTIVITY_PIN_FORM: String = "ACTIVITY_PIN_FORM"
        const val ACTIVITY_PIN_CLOSE: String = "ACTIVITY_PIN_CLOSE"
        const val ACTIVITY_PIN_BUTTON_OK: String = "ACTIVITY_PIN_BUTTON_OK"
        const val ACTIVITY_PIN_BUTTON_ERROR: String = "ACTIVITY_PIN_BUTTON_ERROR"
    }

}