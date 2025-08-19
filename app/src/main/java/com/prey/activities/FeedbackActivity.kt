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

/**
 * Activity responsible for displaying a feedback dialog to the user.
 */
class FeedbackActivity : PreyActivity() {

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The saved instance state, or null if not saved.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showDialog(SHOW_POPUP)
    }

    /**
     * Creates a dialog for the given ID.
     *
     * @param id The ID of the dialog to create.
     * @return The created dialog.
     */
    override fun onCreateDialog(id: Int): Dialog {
        var dialog: Dialog? = null
        when (id) {
            SHOW_POPUP -> {
                val builder = AlertDialog.Builder(this)
                builder.setIcon(R.drawable.info)
                builder.setTitle(R.string.feedback_principal_title)
                builder.setMessage(R.string.feedback_principal_message)
                builder.setPositiveButton(
                    R.string.feedback_principal_button1
                ) { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(Uri.parse("market://details?id=com.prey"))
                    startActivity(intent)
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                builder.setNeutralButton(
                    R.string.feedback_principal_button2
                ) { _, _ ->
                    val popup = Intent(applicationContext, FormFeedbackActivity::class.java)
                    popup.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(popup)
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                builder.setNegativeButton(
                    R.string.feedback_principal_button3
                ) { _, _ ->
                    PreyConfig.getInstance(applicationContext)
                        .setFlagFeedback(FLAG_FEEDBACK_SEND)
                    finish()
                }
                dialog = builder.create()
                PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_FEEDBACK)
            }
        }
        return dialog!!
    }

    /**
     * Checks if the feedback should be shown based on the installation date and feedback flag.
     *
     * @param installationDate The date the app was installed.
     * @param feedbackFlag The feedback flag.
     * @return True if the feedback should be shown, false otherwise.
     */
    fun showFeedback(installationDate: Long, feedbackFlag: Int): Boolean {
        return when (feedbackFlag) {
            FLAG_FEEDBACK_C2DM -> true
            FLAG_FEEDBACK_INIT -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = installationDate
                calendar.add(Calendar.WEEK_OF_YEAR, 2)
                val twoWeeksLater = calendar.timeInMillis
                val now = Date().time
                now > twoWeeksLater
            }

            else -> false
        }
    }

    companion object {
        private const val SHOW_POPUP = 0
        private const val FLAG_FEEDBACK_INIT = 0
        private const val FLAG_FEEDBACK_C2DM = 1
        private const val FLAG_FEEDBACK_SEND = 2
        const val ACTIVITY_FEEDBACK: String = "ACTIVITY_FEEDBACK"
    }

}