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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity that displays information about the required permissions.
 *
 * This activity is responsible for explaining the necessary permissions to the user
 * and providing a way to grant them.
 */
class PermissionInformationActivity : PreyActivity() {
    private val congratsMessage: String? = null

    /**
     * Called when the activity is created.
     * Initializes the activity and sets up the window feature.
     *
     * @param savedInstanceState The saved instance state, or null if not saved
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onResume() {
        super.onResume()
        showScreen()
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode The request code of the activity that was launched
     * @param resultCode The result code of the activity that was launched
     * @param data The data returned by the activity that was launched
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        PreyLogger.d("requestCode:$requestCode resultCode:$resultCode")
        if (requestCode == SECURITY_PRIVILEGES) showScreen()
    }

    /**
     * Shows the screen and performs necessary actions based on the device's configuration.
     */
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
        if (FroyoSupport.getInstance(this).isAdminActive()) {
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
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    AwareController.getInstance().initLastLocation(applicationContext)
                } catch (e: Exception) {
                    PreyLogger.e("Error: ${e.message}", e)
                }
            }
            try {
                if (intentPermission != null) {
                    startActivity(intentPermission)
                }
            } catch (e: Exception) {
                PreyLogger.e("Error: ${e.message}", e)
            }
            finish()
        } else {
            val intentPrivileges =
                FroyoSupport.getInstance(applicationContext).getAskForAdminPrivilegesIntent()
            startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES)
        }
    }

    /**
     * Asks for the necessary permissions.
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun askForPermission() {
        PreyConfig.getInstance(applicationContext).setActivityView(PERMISSIONS_ASK)
        ActivityCompat.requestPermissions(
            this@PermissionInformationActivity,
            INITIAL_PERMS,
            REQUEST_PERMISSIONS
        )
    }

    /**
     * Asks for the Android 7 overlay permission.
     * This permission is required for the app to draw over other apps.
     */
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

    /**
     * Handles the result of the permission request.
     * This method is called after the user has granted or denied the requested permissions.
     *
     * @param requestCode The request code of the permission request
     * @param permissions The array of requested permissions
     * @param grantResults The array of grant results for the requested permissions
     */
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

    /**
     * Starts the overlay service.
     * This service is responsible for drawing over other apps.
     */
    private fun startOverlayService() {
        PreyLogger.d("PermissionInformationActivity: startOverlayService")
        val intentOverlay = Intent(applicationContext, PreyOverlayService::class.java)
        startService(intentOverlay)
    }

    /**
     * Asks for admin privileges.
     * This method is called when the app needs to perform administrative tasks.
     */
    fun askForAdminActive() {
        val intentPrivileges =
            FroyoSupport.getInstance(applicationContext).getAskForAdminPrivilegesIntent()
        startActivityForResult(intentPrivileges, SECURITY_PRIVILEGES)
    }

    companion object {
        const val SECURITY_PRIVILEGES = 10
        const val REQUEST_PERMISSIONS = 5
        const val OVERLAY_PERMISSION_REQ_CODE: Int = 5473
        const val PERMISSIONS_ASK: String = "PERMISSIONS_ASK"
        private val INITIAL_PERMS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

}