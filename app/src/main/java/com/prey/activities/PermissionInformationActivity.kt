/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import androidx.core.app.ActivityCompat
import com.prey.actions.aware.AwareController
import com.prey.backwardcompatibility.FroyoSupport
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.services.PreyOverlayService

class PermissionInformationActivity : PreyActivity() {
    private val congratsMessage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun onBackPressed() {
    }

    override fun onResume() {
        super.onResume()
        showScreen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        PreyLogger.d("requestCode:$requestCode resultCode:$resultCode")
        if (requestCode == SECURITY_PRIVILEGES) showScreen()
    }

    private fun showScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("PermissionInformationActivity: Build.VERSION_CODES >=M")
            val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(this)
            val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(
                this
            )
            val canAccessCamera: Boolean = PreyPermission.canAccessCamera(this)
            val canAccessStorage: Boolean = PreyPermission.canAccessStorage(this)
            val configurated = (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                    && canAccessStorage)
            if (!configurated) {
                askForPermission()
            }
        }
        if (FroyoSupport.getInstance(this)!!.isAdminActive) {
            var intentPermission: Intent? = null
            val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(this)
            if (!canDrawOverlays) {
                askForPermissionAndroid7()
                startOverlayService()
            } else {
                intentPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Intent(
                        this@PermissionInformationActivity,
                        CheckPasswordHtmlActivity::class.java
                    )
                } else {
                    Intent(this@PermissionInformationActivity, LoginActivity::class.java)
                }
            }
            PreyConfig.getInstance(this@PermissionInformationActivity).setProtectReady(true)
            object : Thread() {
                override fun run() {
                    try {
                        AwareController.getInstance().init(applicationContext)
                    } catch (e: Exception) {
                        PreyLogger.e("Error:" + e.message, e)
                    }
                }
            }.start()
            try {
                if (intentPermission != null) {
                    startActivity(intentPermission)
                }
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            finish()
        } else {
            val intentPrivileges =
                FroyoSupport.getInstance(applicationContext)!!.askForAdminPrivilegesIntent
            startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askForPermission() {
        ActivityCompat.requestPermissions(
            this@PermissionInformationActivity,
            INITIAL_PERMS,
            REQUEST_PERMISSIONS
        )
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askForPermissionAndroid7() {
        PreyLogger.d("PermissionInformationActivity: askForPermissionAndroid7")
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse(
                "package:$packageName"
            )
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        startOverlayService()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        PreyLogger.d("PermissionInformationActivity: onRequestPermissionsResult")
        val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(this)
        val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(this)
        val canAccessCamera: Boolean = PreyPermission.canAccessCamera(this)
        val canAccessStorage: Boolean = PreyPermission.canAccessStorage(this)
        if (canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
            && canAccessStorage
        ) {
            val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(this)
            if (!canDrawOverlays) {
                askForPermissionAndroid7()
                startOverlayService()
            } else {
                if (!canDrawOverlays) {
                    askForAdminActive()
                } else {
                    finish()
                    var intent: Intent? = null
                    intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Intent(this, CheckPasswordHtmlActivity::class.java)
                    } else {
                        Intent(this, CheckPasswordActivity::class.java)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun startOverlayService() {
        PreyLogger.d("PermissionInformationActivity: startOverlayService")
        val intentOverlay = Intent(applicationContext, PreyOverlayService::class.java)
        startService(intentOverlay)
    }

    fun askForAdminActive() {
        val intentPrivileges =
            FroyoSupport.getInstance(applicationContext)!!.askForAdminPrivilegesIntent
        startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES)
    }

    companion object {
        private const val SECURITY_PRIVILEGES = 10
        private const val REQUEST_PERMISSIONS = 5

        private val INITIAL_PERMS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        var OVERLAY_PERMISSION_REQ_CODE: Int = 5473
    }
}