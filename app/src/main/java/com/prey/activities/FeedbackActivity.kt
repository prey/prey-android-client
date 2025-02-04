/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.prey.R
import com.prey.PreyConfig
import java.util.Calendar
import java.util.Date

class FeedbackActivity : PreyActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDialog(SHOW_POPUP)
    }

    override fun onCreateDialog(id: Int): Dialog {
        var popup: Dialog? = null
        when (id) {
            SHOW_POPUP -> {
                val alert = AlertDialog.Builder(this)
                alert.setIcon(R.drawable.info)
                alert.setTitle(R.string.feedback_principal_title)
                alert.setMessage(R.string.feedback_principal_message)
                alert.setPositiveButton(
                    R.string.feedback_principal_button1
                ) { dialog, id ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("market://details?id=com.prey"))
                    startActivity(intent)
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                alert.setNeutralButton(
                    R.string.feedback_principal_button2
                ) { dialog, id ->
                    val popup = Intent(applicationContext, FormFeedbackActivity::class.java)
                    popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(popup)
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                alert.setNegativeButton(
                    R.string.feedback_principal_button3
                ) { dialog, id ->
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                popup = alert.create()
            }
        }
        return popup!!
    }

    companion object {
        private const val SHOW_POPUP = 0
        var FLAG_FEEDBACK_INIT: Int = 0
        var FLAG_FEEDBACK_C2DM: Int = 1
        var FLAG_FEEDBACK_SEND: Int = 2

        fun showFeedback(installationDate: Long, flagFeedback: Int): Boolean {
            if (flagFeedback == FLAG_FEEDBACK_C2DM) {
                return true
            } else {
                if (flagFeedback == FLAG_FEEDBACK_INIT) {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = installationDate
                    cal.add(Calendar.WEEK_OF_YEAR, 2)
                    val twoWeekOfYear = cal.timeInMillis
                    val now = Date().time
                    if (now > twoWeekOfYear) {
                        return true
                    }
                }
            }
            return false
        }
    }
}