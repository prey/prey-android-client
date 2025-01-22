/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities.kotlin

import android.app.AlertDialog
import android.app.Dialog
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.prey.R
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.activities.LoginActivity
import com.prey.activities.PreyActivity
import com.prey.kotlin.PreyConfig
import com.prey.kotlin.PreyLogger

class PopUpAlertActivity : PreyActivity() {
    private var message: String? = null
    private var notificationId = 0
    private val close_prey_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            PreyLogger.d("PopUpAlertActivity close_prey_receiver finish")
            finish()
        }
    }

    private val popup_prey_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            PreyLogger.d("PopUpAlertActivity popup_prey_receiver finish")
            PreyConfig.getInstance(context).setNoficationPopupId(0)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.intent.extras
        if (bundle != null) {
            this.message = bundle.getString("alert_message")
            this.notificationId = bundle.getInt("notificationId")
        }
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
        val popup: Dialog = alertBuilder.create()
        popup.setOnDismissListener { finish() }
        popup.show()
        try {
            registerReceiver(
                close_prey_receiver,
                IntentFilter(CheckPasswordHtmlActivity.CLOSE_PREY)
            )
            registerReceiver(popup_prey_receiver, IntentFilter(POPUP_PREY + "_" + notificationId))
        } catch (e: Exception) {
            PreyLogger.d(String.format("Error receiver:%s", e.message))
        }
    }

    override fun onResume() {
        super.onResume()
        val noficationPopupId: Int = PreyConfig.getInstance(this).getNoficationPopupId()
        PreyLogger.d("PopUpAlertActivity onResume noficationPopupId:$noficationPopupId")
        if (noficationPopupId == 0) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NO_HISTORY)
            startActivity(intent)
        }
    }

    companion object {
        private const val SHOW_POPUP = 0
        const val POPUP_PREY: String = "popup_prey"
    }
}