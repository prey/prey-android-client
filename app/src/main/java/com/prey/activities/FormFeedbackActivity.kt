/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText

import com.prey.R
import com.prey.FileConfigReader
import com.prey.PreyUtils

/**
 * An activity that displays a feedback form to the user.
 */
class FormFeedbackActivity : PreyActivity() {
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
        var popup: Dialog? = null
        when (id) {
            SHOW_POPUP -> {
                val factory = LayoutInflater.from(this)
                val textEntryView: View = factory.inflate(R.layout.dialog_signin, null)
                val alert: AlertDialog.Builder = AlertDialog.Builder(this)
                alert.setIcon(R.drawable.info)
                alert.setTitle(R.string.feedback_form_title)
                alert.setMessage(R.string.feedback_form_message)
                alert.setView(textEntryView)
                val input = textEntryView.findViewById(R.id.feedback_form_field_comment) as EditText
                alert.setPositiveButton(R.string.feedback_form_button2,
                    DialogInterface.OnClickListener { dialog, id -> finish() })
                alert.setNegativeButton(R.string.feedback_form_button1,
                    DialogInterface.OnClickListener { dialog, id ->
                        if (input != null) {
                            val context: Context = applicationContext
                            val emailFeedback: String = FileConfigReader.getInstance(
                                applicationContext
                            ).getEmailFeedback()
                            val subject = StringBuffer()
                            subject.append(FileConfigReader.getInstance(context).getSubjectFeedback())
                                .append(" ")
                            subject.append(PreyUtils.randomAlphaNumeric(7).toUpperCase())
                            // Create an intent to send the email
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.setType("text/plain")
                            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailFeedback))
                            intent.putExtra(Intent.EXTRA_SUBJECT, subject.toString())
                            intent.putExtra(Intent.EXTRA_TEXT, input.text.toString())
                            // Create a chooser for the email intent
                            val chooser = Intent.createChooser(
                                intent,
                                context.getText(R.string.feedback_form_send_email)
                            )
                            // Start the email chooser activit
                            startActivity(chooser)
                        }
                        finish()
                    })
                popup = alert.create()
            }
        }
        return popup!!
    }

    companion object {
        private const val SHOW_POPUP = 0
    }
}