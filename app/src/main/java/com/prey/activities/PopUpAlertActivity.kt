/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger

/**
 * Activity responsible for displaying a popup alert to the user.
 */
class PopUpAlertActivity : PreyActivity() {
    private var message: String? = null
    private var notificationId = 0

    /**
     * BroadcastReceiver to handle the close prey event.
     */
    private val close_prey_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            PreyLogger.d("PopUpAlertActivity close_prey_receiver finish")
            finish()
        }
    }

    /**
     * BroadcastReceiver to handle the popup prey event.
     */
    private val popup_prey_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            PreyLogger.d("PopUpAlertActivity popup_prey_receiver finish")
            PreyConfig.getInstance(context).setNoficationPopupId(0)
            finish()
        }
    }

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState The saved instance state, or null if not saved.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.intent.extras
        if (bundle != null) {
            this.message = bundle.getString("alert_message")
            this.notificationId = bundle.getInt("notificationId")
        }
        // Create the popup alert dialog
        val alertBuilder = AlertDialog.Builder(this@PopUpAlertActivity)
        alertBuilder.setTitle(R.string.popup_alert_title)
        alertBuilder.setMessage(this.message)
        alertBuilder.setCancelable(true)
        alertBuilder.setNeutralButton(
            R.string.close_alert
        ) { dialog, which ->
            val nMgr =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nMgr.cancel(notificationId)
            finish()
        }
        // Create and show the popup dialog
        val popup: Dialog = alertBuilder.create()
        popup.setOnDismissListener { finish() }
        popup.show()
        try {
            registerReceiver(
                close_prey_receiver,
                IntentFilter(CheckPasswordHtmlActivity.CLOSE_PREY)
            )
            registerReceiver(popup_prey_receiver, IntentFilter("${POPUP_PREY}_${notificationId}"))
        } catch (e: Exception) {
            PreyLogger.d("Error receiver:${e.message}")
        }
    }

    /**
     * Called when the activity is resumed.
     */
    override fun onResume() {
        super.onResume()
        val noficationPopupId: Int = PreyConfig.getInstance(this).getNoficationPopupId()
        PreyLogger.d("PopUpAlertActivity onResume noficationPopupId:$noficationPopupId")
        if (noficationPopupId == 0) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
        PreyConfig.getInstance(this).setActivityView(POPUP_FORM)
    }

    companion object {
        private const val SHOW_POPUP = 0
        const val POPUP_PREY: String = "popup_prey"
        const val POPUP_FORM: String = "POPUP_FORM"
    }
}