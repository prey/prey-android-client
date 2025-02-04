/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.prey.R
import com.prey.json.UtilJson
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.net.PreyWebServices

class PreyLockService : Service() {
    private val windowManager: WindowManager? = null
    private var view: View? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        PreyLogger.d("PreyLockService onCreate")
    }

    override fun onStart(intent: Intent, startId: Int) {
        super.onStart(intent, startId)
        val ctx: Context = this
        PreyLogger.d("PreyLockService onStart")
        val unlock = PreyConfig.getInstance(ctx).getUnlockPass()
        if (unlock != null && "" != unlock) {
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.lock_android7, null)
            val regularMedium = Typeface.createFromAsset(assets, "fonts/Regular/regular-medium.ttf")
            val textViewLockAccessDenied =
                view!!.findViewById<View>(R.id.TextView_Lock_AccessDenied) as TextView
            textViewLockAccessDenied.setTypeface(regularMedium)
            val regularBold = Typeface.createFromAsset(assets, "fonts/Regular/regular-bold.ttf")
            val editTextLockPassword =
                view!!.findViewById<View>(R.id.EditText_Lock_Password) as EditText
            editTextLockPassword.setTypeface(regularMedium)
            val textViewWarning = view!!.findViewById<View>(R.id.textView8) as TextView
            textViewWarning.setTypeface(regularBold)
            val editText = view!!.findViewById<View>(R.id.EditText_Lock_Password) as EditText
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    textViewWarning.text = ""
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable) {
                }
            })
            val btn_unlock = view!!.findViewById<View>(R.id.Button_Lock_Unlock) as Button
            btn_unlock.setOnClickListener {
                try {
                    val key = editText.text.toString().trim { it <= ' ' }
                    val unlock = PreyConfig.getInstance(ctx).getUnlockPass()
                    val canDrawOverlays = PreyPermission.canDrawOverlays(ctx)
                    PreyLogger.d("unlock key:$key unlock:$unlock canDrawOverlays:$canDrawOverlays")
                    if (unlock == null || "" == unlock || unlock == key) {
                        val jobIdLock = PreyConfig.getInstance(ctx).getJobIdLock()
                        var reason = "{\"origin\":\"user\"}"
                        if (jobIdLock != null && "" != jobIdLock) {
                            reason = "{\"device_job_id\":\"$jobIdLock\",\"origin\":\"user\"}"
                            PreyConfig.getInstance(ctx).setJobIdLock("")
                        }
                        val reasonFinal = reason
                        PreyConfig.getInstance(ctx).setLock(false)
                        PreyConfig.getInstance(ctx).deleteUnlockPass()
                        object : Thread() {
                            override fun run() {
                                PreyWebServices.getInstance()
                                    .sendNotifyActionResultPreyHttp(
                                        ctx,
                                        UtilJson.makeMapParam(
                                            "start",
                                            "lock",
                                            "stopped",
                                            reasonFinal
                                        )
                                    )
                                if (canDrawOverlays) {
                                    try {
                                        val wm =
                                            getSystemService(WINDOW_SERVICE) as WindowManager
                                        if (wm != null && view != null) {
                                            wm.removeView(view)
                                            view = null
                                        } else {
                                            view = null
                                            Process.killProcess(Process.myPid())
                                        }
                                    } catch (e: Exception) {
                                        view = null
                                        Process.killProcess(Process.myPid())
                                    }
                                }
                            }
                        }.start()
                    } else {
                        editText.setText("")
                        textViewWarning.text = ctx.getString(R.string.password_wrong)
                    }
                } catch (e: Exception) {
                }
            }
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.format = PixelFormat.TRANSLUCENT
            layoutParams.flags =
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_FULLSCREEN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                if (Settings.canDrawOverlays(this)) {
                    if (wm != null) {
                        try {
                            wm.addView(view, layoutParams)
                            PreyConfig.getInstance(this).viewLock = view
                        } catch (e: Exception) {
                            PreyLogger.e(e.message, e)
                        }
                    }
                }
            }
        } else {
            if (view != null) {
                val wm = getSystemService(WINDOW_SERVICE) as WindowManager
                wm?.removeView(view)
                view = null
            }
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreyLogger.d("PreyLockService onDestroy")
        if (view != null) {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.removeView(view)
            view = null
        }
    }
}