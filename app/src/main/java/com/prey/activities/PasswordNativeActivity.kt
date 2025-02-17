/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import com.prey.R
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.PreyWebServices

/**
 * Activity responsible for handling password entry and validation.
 */
class PasswordNativeActivity : Activity() {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Saved instance state, if any.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.prey.R.layout.lock_android7)
        PreyLogger.d("PasswordActivity2: onCreate")
        val editText = findViewById<View>(R.id.EditText_Lock_Password) as EditText
        val unlockButton = findViewById<Button>(R.id.Button_Lock_Unlock)
        val imageLock = findViewById<ImageView>(R.id.ImageView_Lock_AccessDenied)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (imm.isAcceptingText) {
            PreyLogger.d("Software Keyboard was shown")
        } else {
            PreyLogger.d("Software Keyboard was not shown")
        }
        val text = findViewById<TextView>(R.id.TextView_Lock_AccessDenied)
        val contentView = findViewById<View>(android.R.id.content)
        contentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            private var mPreviousHeight = 0
            override fun onGlobalLayout() {
                val newHeight = contentView.height
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight > newHeight) {
                        PreyLogger.d("Software Keyboard was shown")
                        imageLock.visibility = View.GONE
                    } else if (mPreviousHeight < newHeight) {
                        PreyLogger.d("Software Keyboard was not shown")
                        imageLock.visibility = View.VISIBLE
                    }
                }
                mPreviousHeight = newHeight
            }
        })
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                text.setText(R.string.lock_access_denied)
            }
            override fun afterTextChanged(editable: Editable) {
            }
        })
        unlockButton.setOnClickListener {
            try {
                val unlock = PreyConfig.getInstance(
                    applicationContext
                ).getUnlockPass()
                val key = editText.text.toString().trim { it <= ' ' }
                PreyLogger.d("PasswordActivity2 unlock key:$key unlock:$unlock")
                if (unlock != null && unlock == key) {
                    PreyConfig.getInstance(applicationContext).setUnlockPass ("")
                    object : Thread() {
                        override fun run() {
                            val reason = "{\"origin\":\"user\"}"
                            PreyWebServices.getInstance()
                                .sendNotifyActionResultPreyHttp(
                                    applicationContext,
                                    UtilJson.makeMapParam(
                                        "start",
                                        "lock",
                                        "stopped",
                                        reason
                                    )
                                )
                        }
                    }.start()
                    onResume()
                } else {
                    if (unlock == null) {
                        onResume()
                    } else {
                        text.setText(R.string.password_wrong)
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
    }

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        val unlock = PreyConfig.getInstance(applicationContext).getUnlockPass()
        PreyLogger.d("PasswordActivity2 unlock:$unlock")
        if (unlock == null || "" == unlock) {
            val intent = Intent(applicationContext, CloseActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Called when the configuration changes.
     *
     * @param newConfig New configuration.
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
}