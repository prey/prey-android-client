/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionsManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.OpenableColumns
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.view.KeyEvent
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.prey.R
import com.prey.activities.js.WebAppInterface
import com.prey.activities.js.CustomWebView
import com.prey.backwardcompatibility.FroyoSupport

import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyPermission
import com.prey.PreyUtils
import com.prey.services.PreyAccessibilityService
import com.prey.services.PreyOverlayService

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.ECGenParameterSpec
import java.util.UUID
import java.util.concurrent.Executor

/**
 * Activity responsible for checking the user's password.
 */
class CheckPasswordHtmlActivity() : AppCompatActivity() {

    /**
     * BroadcastReceiver to handle close Prey events.
     * Finishes the activity when the close Prey event is received.
     */
    private val close_prey_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            PreyLogger.d("CheckPasswordHtmlActivity BroadcastReceiver: finish")
            finish()
        }
    }

    /**
     * BroadcastReceiver to handle restriction events.
     * Checks if the device is already registered with Prey and if not, retrieves the setup key from application restrictions.
     */
    private val restriction_receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Resolve application restrictions by checking if the device is already registered with Prey
            resolveRestrictions(context)
        }
    }

    /**
     * Resolves application restrictions by checking if the device is already registered with Prey.
     * If not, retrieves the setup key from application restrictions and executes the AddDeviceWithRestriction task.
     *
     * @param context The Context in which the restrictions are being resolved.
     */
    private fun resolveRestrictions(context: Context) {
        // Check if the device is already registered with Prey
        if (!PreyConfig.getInstance(context).isThisDeviceAlreadyRegisteredWithPrey()) {
            // Get the RestrictionsManager instance
            val restrictionsManager = context.getSystemService(
                RESTRICTIONS_SERVICE
            ) as RestrictionsManager
            // Retrieve the application restrictions
            val restrictions = restrictionsManager.applicationRestrictions
            if (restrictions != null && restrictions.containsKey("enterprise_name")) {
                // Retrieve the enterprise name from the restrictions bundle
                val enterpriseName: String? = restrictions.getString("enterprise_name")
                // Check if the enterprise name is not null and not empty
                if (enterpriseName != null && !("" == enterpriseName)) {
                    // Set the organization ID in the Prey configuration
                    PreyConfig.getInstance(context).setOrganizationId(enterpriseName)
                }
            }
            // Check if the restrictions bundle is not null and contains the "setup_key"
            if (restrictions != null && restrictions.containsKey("setup_key")) {
                // Get the setup key from the restrictions bundle
                val setupKey: String? = restrictions.getString("setup_key")
                // Check if the setup key is not null and not empty
                if (setupKey != null && !("" == setupKey)) {
                    // Execute the AddDeviceWithRestriction task with the setup key
                    addDeviceWithRestriction(setupKey)
                }
            }
        }
    }

    private lateinit var myWebView: WebView

    /**
     * Called when the activity is created.
     *
     * This method is responsible for initializing the activity's UI and setting up the necessary
     * receivers and configurations.
     *
     * @param savedInstanceState The saved instance state of the activity, or null if not applicable.
     */
    @SuppressLint("SetJavaScriptEnabled", "UnspecifiedRegisterReceiverFlag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            supportActionBar!!.hide()
        } catch (e: Exception) {
            PreyLogger.e("Error ActionBar().hide:${e.message}", e)
        }
        setContentView(R.layout.webview)
        PreyLogger.d("CheckPasswordHtmlActivity: onCreate")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(close_prey_receiver, IntentFilter(CLOSE_PREY), RECEIVER_EXPORTED)
        } else {
            registerReceiver(close_prey_receiver, IntentFilter(CLOSE_PREY))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                restriction_receiver,
                IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                restriction_receiver,
                IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
            )
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val builder: VmPolicy.Builder = VmPolicy.Builder()
            StrictMode.setVmPolicy(builder.build())
        }
        val extras: Bundle? = intent.extras
        var nexturl: String? = ""
        try {
            nexturl = extras!!.getString("nexturl")
        } catch (e: Exception) {
            PreyLogger.d("not extra nexturl")
        }
        PreyLogger.d("CheckPasswordHtmlActivity nexturl: ${nexturl}")
        // Determine which action to take based on the next URL
        if (("tryReport" == nexturl)) {
            // Try to report if the next URL is "tryReport"
            tryReport()
        } else {
            // Load the URL otherwise
            loadUrl()
        }
    }

    /**
     * Called when the activity is resumed.
     *
     * This method is responsible for updating the activity's state and resolving any restrictions.
     */
    override fun onResume() {
        super.onResume()
        PreyConfig.getInstance(applicationContext).setCapsLockOn(false)
        PreyConfig.getInstance(applicationContext).setVerificateBiometric(false)
        PreyLogger.d("CheckPasswordHtmlActivity: onResume")
        resolveRestrictions(this)
    }

    /**
     * Called when the activity is destroyed.
     * This method is responsible for unregistering the receivers.
     */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(close_prey_receiver)
        unregisterReceiver(restriction_receiver)
    }

    /**
     * Configures the web view settings.
     * This method is responsible for setting up the web view's key listener, background color, and JavaScript settings.
     */
    fun settings() {
        PreyLogger.d("CheckPasswordHtmlActivity: settings")
        myWebView = findViewById(R.id.install_browser)
        myWebView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(view: View, i: Int, keyEvent: KeyEvent): Boolean {
                CustomWebView.callDispatchKeyEvent(applicationContext, keyEvent)
                return false
            }
        })
        val settings: WebSettings = myWebView!!.settings
        myWebView.setBackgroundColor(0x00000000)
        settings.javaScriptEnabled = true
        settings.loadWithOverviewMode = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                myWebView.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
                myWebView.settings.savePassword = false
                myWebView.clearFormData()
            } catch (e: Exception) {
                PreyLogger.e("Error autofill:${e.message}", e)
            }
        }
    }

    /**
     * Attempts to report the device's status.
     * This method is responsible for loading the activation URL and adding a JavaScript interface to the web view.
     */
    fun tryReport() {
        PreyLogger.d("CheckPasswordHtmlActivity: tryReport")
        val lng: String = PreyUtils.getLanguage()
        val url: StringBuffer = StringBuffer("")
        url.append(URL_ONB).append("#/").append(lng).append("/activation")
        settings()
        PreyLogger.d("_url:${url.toString()}")
        myWebView.addJavascriptInterface(
            WebAppInterface(applicationContext, this),
            JS_ALIAS
        )
        myWebView.loadUrl(url.toString())
        myWebView.loadUrl("javascript:window.location.reload(true)")
    }

    /**
     * Loads the security URL.
     * This method is responsible for loading the security URL and adding a JavaScript interface to the web view.
     */
    fun security() {
        PreyLogger.d("CheckPasswordHtmlActivity: security")
        val lng: String = PreyUtils.getLanguage()
        val url: StringBuffer = StringBuffer("")
        url.append(URL_ONB).append("#/").append(lng).append("/security")
        settings()
        PreyLogger.d("_url:${url.toString()}")
        myWebView.addJavascriptInterface(WebAppInterface(applicationContext, this), JS_ALIAS)
        myWebView.loadUrl(url.toString())
        myWebView.loadUrl("javascript:window.location.reload(true)")
    }

    /**
     * Loads the URL.
     * This method is responsible for loading the URL and adding a JavaScript interface to the web view.
     */
    fun loadUrl() {
        PreyLogger.d("CheckPasswordHtmlActivity: loadUrl")
        settings()
        myWebView.addJavascriptInterface(WebAppInterface(applicationContext, this), JS_ALIAS)
        myWebView.loadUrl(getUrl(this))
        myWebView.loadUrl("javascript:window.location.reload(true)")
    }

    /**
     * Reloads the web view with the current URL.
     * This method is responsible for reloading the web view and adding a JavaScript interface.
     */
    fun reload() {
        PreyLogger.d("CheckPasswordHtmlActivity: reload")
        settings()
        myWebView.addJavascriptInterface(WebAppInterface(applicationContext, this), JS_ALIAS)
        myWebView.loadUrl(getUrl(this))
        myWebView.reload()
    }

    /**
     * Returns the URL to be loaded into the web view.
     * This method takes into account the device's language and registration status.
     *
     * @param context The context in which the URL is being retrieved.
     * @return The URL to be loaded into the web view.
     */
    fun getUrl(context: Context?): String {
        val lng: String = PreyUtils.getLanguage()
        val url: StringBuffer = StringBuffer("")
        val deviceKey: String? = PreyConfig.getInstance(this).getDeviceId()
        val registered: Boolean =
            PreyConfig.getInstance(this).isThisDeviceAlreadyRegisteredWithPrey()
        PreyLogger.d("CheckPasswordHtmlActivity deviceKey:${deviceKey}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES >=M")
            val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(context)
            val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(this)
            val canAccessCamera: Boolean = PreyPermission.canAccessCamera(this)
            val canAccessStorage: Boolean = PreyPermission.canAccessStorage(this)
            val canAccessBackgroundLocation: Boolean =
                PreyPermission.canAccessBackgroundLocationView(this)
            val canScheduleExactAlarms: Boolean = PreyPermission.canScheduleExactAlarms(this)
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessFineLocation:${canAccessFineLocation}")
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCoarseLocation:${canAccessCoarseLocation}")
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessCamera:${canAccessCamera}")
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessStorage:${canAccessStorage}")
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessBackgroundLocation:${canAccessBackgroundLocation}")
            PreyLogger.d("CheckPasswordHtmlActivity: canScheduleExactAlarms:${canScheduleExactAlarms}")
            val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(this)
            PreyLogger.d("CheckPasswordHtmlActivity: canDrawOverlays:${canDrawOverlays}")
            val canAccessibility: Boolean = PreyPermission.isAccessibilityServiceView(this)
            PreyLogger.d("CheckPasswordHtmlActivity: canAccessibility:${canAccessibility}")
            val isAdminActive: Boolean = FroyoSupport.getInstance(this).isAdminActive()
            PreyLogger.d("CheckPasswordHtmlActivity: isAdminActive:${isAdminActive}")
            val isStorage: Boolean = PreyPermission.isExternalStorageManagerView(this)
            PreyLogger.d("CheckPasswordHtmlActivity: isStorage:${isStorage}")
            val configurated: Boolean =
                ((canAccessFineLocation || canAccessCoarseLocation) && canAccessBackgroundLocation && canAccessCamera
                        && canScheduleExactAlarms
                        && canAccessStorage && isAdminActive && canDrawOverlays && canAccessibility && isStorage)
            val installationStatus: String? = PreyConfig.getInstance(this).getInstallationStatus()
            PreyLogger.d("CheckPasswordHtmlActivity: configurated:${configurated} installationStatus:${installationStatus}")
            if (configurated) {
                if (registered) {
                    if (("" == installationStatus)) {
                        url.append(URL_ONB).append("#/").append(lng).append("/")
                    } else {
                        if (("OK" == installationStatus)) {
                            PreyConfig.getInstance(this).setInstallationStatus("")
                            url.append(URL_ONB).append("#/").append(lng).append("/emailok")
                        } else {
                            url.append(URL_ONB).append("#/").append(lng).append("/emailsent")
                        }
                    }
                } else {
                    if (("DEL" == installationStatus)) {
                        PreyConfig.getInstance(this).setInstallationStatus("")
                        url.append(URL_ONB).append("#/").append(lng).append("/emailretry")
                    } else {
                        url.append(URL_ONB).append("#/").append(lng).append("/signin")
                    }
                }
            } else {
                val permissionsAndBasic: Boolean =
                    ((canAccessFineLocation || canAccessCoarseLocation) && canAccessCamera
                            && canAccessStorage && isAdminActive && canDrawOverlays)
                val permissionsOrBasic: Boolean =
                    (canAccessFineLocation || canAccessCoarseLocation || canAccessCamera
                            || canAccessStorage || isAdminActive || canDrawOverlays)
                val start: Boolean = PreyConfig.getInstance(this).getStart()
                if (start || !permissionsOrBasic) {
                    url.append(URL_ONB).append("#/").append(lng).append("/start")
                } else {
                    if (permissionsAndBasic) {
                        if (!canAccessibility) {
                            PreyLogger.d("CheckPasswordHtmlActivity !canAccessibility")
                            url.append(URL_ONB).append("#/").append(lng).append("/accessibility")
                        } else {
                            if (!canAccessBackgroundLocation) {
                                PreyLogger.d("CheckPasswordHtmlActivity !canAccessBackgroundLocation")
                                url.append(URL_ONB).append("#/").append(lng).append("/bgloc")
                            } else {
                                url.append(URL_ONB).append("#/").append(lng).append("/permissions")
                            }
                        }
                    } else {
                        if (!canAccessibility) {
                            url.append(URL_ONB).append("#/").append(lng).append("/accessibility")
                        } else {
                            url.append(URL_ONB).append("#/").append(lng).append("/permissions")
                        }
                    }
                }
            }
        } else {
            PreyLogger.d("CheckPasswordHtmlActivity: Build.VERSION_CODES <M")
            if (registered) {
                url.append(URL_ONB).append("#/").append(lng).append("/")
            } else {
                url.append(URL_ONB).append("#/").append(lng).append("/signin")
            }
        }
        PreyLogger.d("_url:${url.toString()}")
        return url.toString()
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askForPermissionAndroid7() {
        PreyLogger.d("CheckPasswordHtmlActivity: askForPermissionAndroid7")
        val intent: Intent =
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName))
        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
        startOverlayService()
    }

    private fun startOverlayService() {
        PreyLogger.d("CheckPasswordHtmlActivity: startOverlayService")
        val intentOverlay: Intent = Intent(applicationContext, PreyOverlayService::class.java)
        startService(intentOverlay)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askForPermission() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermission")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this@CheckPasswordHtmlActivity,
                INITIAL_PERMS,
                REQUEST_PERMISSIONS
            )
        } else {
            ActivityCompat.requestPermissions(
                this@CheckPasswordHtmlActivity,
                INITIAL_PERMS_TIRAMISU,
                REQUEST_PERMISSIONS
            )
        }
    }

    /**
     * Method opens settings
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun openSettings() {
        PreyLogger.d("openSettings")
        val intentSetting: Intent = Intent()
        intentSetting.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intentSetting.setData(uri)
        intentSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intentSetting)
        finish()
    }

    /**
     * Displays an alert dialog when a permission is denied.
     * The dialog informs the user about the denied permission and provides an option to manually grant the permission.
     */
    fun deniedPermission() {
        PreyLogger.d("deniedPermission")
        val builder: AlertDialog.Builder = AlertDialog.Builder(
            this
        )
        var permission: String = ""
        val canAccessFineLocation = PreyPermission.canAccessFineLocation(this)
        val canAccessCoarseLocation = PreyPermission.canAccessCoarseLocation(this)
        if (!canAccessFineLocation || !canAccessCoarseLocation) {
            permission = this.getString(R.string.permission_location)
        } else {
            val canAccessBackgroundLocation = PreyPermission.canAccessBackgroundLocation(this)
            if (!canAccessBackgroundLocation) {
                permission = this.getString(R.string.permission_location)
            }
        }
        val canAccessCamera = PreyPermission.canAccessCamera(this)
        if (!canAccessCamera) {
            if (!("" == permission)) permission += ", "
            permission += this.getString(R.string.permission_camera)
        }
        val canAccessWriteExternalStorage = PreyPermission.canAccessStorage(this)
        if (!canAccessWriteExternalStorage) {
            if (!("" == permission)) permission += ", "
            permission += this.getString(R.string.permission_storage)
        }
        PreyLogger.d("permission:$permission")
        var message: String? = ""
        try {
            message =
                String.format(resources.getString(R.string.permission_message_popup), permission)
        } catch (e: Exception) {
            PreyLogger.e("Error format:" + e.message, e)
        }
        builder.setMessage(message)
        builder.setCancelable(false)
        builder.setPositiveButton(
            R.string.permission_manually,
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                    val intentSetting: Intent = Intent()
                    intentSetting.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intentSetting.setData(uri)
                    startActivity(intentSetting)
                }
            })
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PreyLogger.d("CheckPasswordHtmlActivity onRequestPermissionsResult:${requestCode}")
        if (requestCode == REQUEST_PERMISSIONS) {
            var i: Int = 0
            while (permissions != null && i < permissions.size) {
                PreyLogger.d(
                    "CheckPasswordHtmlActivity onRequestPermissionsResult:${
                        permissions.get(
                            i
                        )
                    } ${grantResults.get(i)}"
                )
                if ((permissions.get(i) == Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults.get(
                        i
                    ) == -1
                ) {
                    PreyConfig.getInstance(this).setPermissionLocation(false)
                }
                if ((permissions.get(i) == Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults.get(
                        i
                    ) == 0
                ) {
                    PreyConfig.getInstance(this).setPermissionLocation(true)
                }
                i++
            }
        }
        if (requestCode == REQUEST_PERMISSIONS_LOCATION) {
            var i: Int = 0
            while (permissions != null && i < permissions.size) {
                PreyLogger.d(
                    "CheckPasswordHtmlActivity onRequestPermissionsResult[${i}]: ${
                        grantResults.get(
                            i
                        )
                    }"
                )
                if ((permissions.get(i) == Manifest.permission.ACCESS_COARSE_LOCATION) && grantResults.get(
                        i
                    ) == -1
                ) {
                    PreyConfig.getInstance(this).setPermissionLocation(false)
                }
                if ((permissions.get(i) == Manifest.permission.CAMERA) && grantResults.get(i) == -1) {
                    PreyConfig.getInstance(this).setPermissionLocation(true)
                }
                if ((permissions.get(i) == Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults.get(
                        i
                    ) == -1
                ) {
                    PreyConfig.getInstance(this).setPermissionLocation(true)
                }
                i++
            }
        }
        val canAccessFineLocation: Boolean = PreyPermission.canAccessFineLocation(
            this
        )
        val canAccessCoarseLocation: Boolean = PreyPermission.canAccessCoarseLocation(
            this
        )
        val canAccessCamera: Boolean = PreyPermission.canAccessCamera(this)
        val canAccessStorage: Boolean = PreyPermission.canAccessStorage(this)
        if ((canAccessFineLocation && canAccessCoarseLocation && canAccessCamera
                    && canAccessStorage)
        ) {
            PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 1")
            val canDrawOverlays: Boolean = PreyPermission.canDrawOverlays(this)
            if (!canDrawOverlays) {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 2")
                askForPermissionAndroid7()
                startOverlayService()
            } else {
                PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 3")
                if (!canDrawOverlays) {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 4")
                    askForAdminActive()
                } else {
                    PreyLogger.d("CheckPasswordHtmlActivity: onRequestPermissionsResult 5")
                    val intentLogin: Intent = Intent(this, LoginActivity::class.java)
                    intentLogin.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intentLogin)
                    finish()
                }
            }
        }
        /*
        Set notification permission response
        */
        if (requestCode == REQUEST_PERMISSIONS_POST_NOTIFICATIONS) {
            PreyLogger.d("CheckPasswordHtmlActivity: setPostNotification")
            var i: Int = 0
            while (permissions != null && i < permissions.size) {
                PreyLogger.d(

                    "CheckPasswordHtmlActivity onRequestPermissionsResult:${permissions.get(i)} ${
                        grantResults.get(
                            i
                        )
                    }"
                )
                if ((permissions.get(i) == Manifest.permission.POST_NOTIFICATIONS) && grantResults.get(
                        i
                    ) == -1
                ) {
                    PreyConfig.getInstance(this).setDenyNotification(true)
                }
                i++
            }
        }
    }

    /**
     * Requests admin privileges from the user.
     *
     * This method creates an intent to request admin privileges and starts the activity for result.
     *
     * @see FroyoSupport.askForAdminPrivilegesIntent
     * @see startActivityForResult
     */
    fun askForAdminActive() {
        val intentAskForAdmin: Intent =
            FroyoSupport.getInstance(applicationContext).getAskForAdminPrivilegesIntent()
        // Start the activity for result with the admin privileges intent
        startActivityForResult(intentAskForAdmin, SECURITY_PRIVILEGES)
    }

    /**
     * Method that requests alarm permission from the user.
     */
    fun alarms() {
        PreyLogger.d("CheckPasswordHtmlActivity alarms")
        startActivity(
            Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:" + packageName)
            )
        )
    }

    /**
     * Method that requests accessibility permission from the user.
     */
    fun accessibility() {
        PreyLogger.d("CheckPasswordHtmlActivity accessibility")
        val intentService: Intent = Intent(applicationContext, PreyAccessibilityService::class.java)
        startService(intentService)
        val intentSetting: Intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intentSetting)
    }

    /**
     * Requests the ACCESS_BACKGROUND_LOCATION permission from the user.
     *
     * This method is used to request the permission to access the device's location in the background.
     *
     * @see ActivityCompat.requestPermissions
     * @see Manifest.permission.ACCESS_BACKGROUND_LOCATION
     */
    fun askForPermissionLocation() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermissionLocation")
        ActivityCompat.requestPermissions(
            this@CheckPasswordHtmlActivity,
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_PERMISSIONS_LOCATION
        )
    }

    /**
     * Method that open the image Chooser
     */
    fun openImageChooserActivity() {
        PreyLogger.d("CheckPasswordHtmlActivity openImageChooserActivity")
        val i: Intent = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.setType("image/*")
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    /**
     * Method activity result
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                val uri: Uri? = intent!!.data
                if (uri != null && uri.toString().startsWith("content:")) {
                    val fileName: String? = getFileNameHelp(applicationContext, uri)
                    PreyConfig.getInstance(this).setFileHelp(fileName!!)
                }
            }
        }
    }

    /**
     * Method get selected image
     *
     * @param context
     * @param uri
     * @return image
     */
    @SuppressLint("Range")
    fun getFileNameHelp(context: Context?, uri: Uri?): String? {
        var fileNameHelp: String? = null
        val cursor: Cursor? = contentResolver.query((uri)!!, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val displayName: String = cursor.getString(columnIndex)
                val dir: File = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    PreyConfig.HELP_DIRECTORY
                )
                if (!dir.exists()) {
                    dir.mkdir()
                }
                var newFile: File = File(dir, displayName)
                if (newFile.exists()) {
                    newFile.delete()
                    newFile = File(dir, displayName)
                }
                var out: FileOutputStream? = null
                var input: InputStream? = null
                try {
                    out = FileOutputStream(newFile)
                    input = applicationContext.contentResolver.openInputStream((uri))
                    PreyUtils.copyFile(input!!, out)
                    fileNameHelp = displayName
                } catch (e: Exception) {
                    PreyLogger.d("Error getFileNameHelp:${e.message}")
                } finally {
                    if (out != null) {
                        try {
                            out.close()
                        } catch (e: Exception) {
                            PreyLogger.d("Error getFileNameHelp:${e.message}")
                        }
                    }
                    if (input != null) {
                        try {
                            input.close()
                        } catch (e: Exception) {
                            PreyLogger.d("Error getFileNameHelp:${e.message}")
                        }
                    }
                }
            }
        } finally {
            cursor!!.close()
        }
        return fileNameHelp
    }

    /**
     * Method for requesting storage permission
     */
    fun allFiles() {
    }

    /**
     * Method for requesting notification permission
     */
    fun askForPermissionNotification() {
        PreyLogger.d("CheckPasswordHtmlActivity askForPermissionNotification")
        ActivityCompat.requestPermissions(
            this@CheckPasswordHtmlActivity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_PERMISSIONS_POST_NOTIFICATIONS
        )
    }

    private var mToBeSignedMessage: String? = null
    private var executor: Executor? = null

    /**
     * Shows a biometric prompt to the user for authentication.
     *
     * @param signature The signature to use for authentication.
     * @param attempts The number of attempts remaining.
     */
    fun showBiometricPrompt(signature: Signature?, attempts: Int) {
        executor = ContextCompat.getMainExecutor(this)
        // Create a new authentication callback.
        val authenticationCallback: BiometricPrompt.AuthenticationCallback =
            authenticationCallback
        // Create a new biometric prompt with the given executor and callback.
        val mBiometricPrompt: BiometricPrompt = BiometricPrompt(
            (this@CheckPasswordHtmlActivity as FragmentActivity),
            executor!!, authenticationCallback
        )
        // Create a new prompt info with the given description, title, and negative button text.
        val promptInfo: PromptInfo = PromptInfo.Builder()
            .setDescription(resources.getString(R.string.finger_description))
            .setTitle(resources.getString(R.string.finger_title))
            .setNegativeButtonText(resources.getString(R.string.cancel))
            .build()
        // If a signature is provided, attempt to authenticate with it.
        if (signature != null) {
            try {
                mBiometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
            } catch (e: Exception) {
                if (attempts > 0) {
                    try {
                        Thread.sleep(200)
                        showBiometricPrompt(signature, attempts - 1)
                    } catch (e1: Exception) {
                        PreyLogger.e("error Show biometric prompt:" + e1.message, e1)
                    }
                }
            }
        }
    }

    /**
     * Gets the authentication callback for biometric authentication.
     *
     * @return The authentication callback.
     */
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                if (result.cryptoObject != null &&
                    result.cryptoObject!!.signature != null
                ) {
                    try {
                        val signature: Signature? = result.cryptoObject!!.signature
                        signature!!.update(mToBeSignedMessage!!.toByteArray())
                        val signatureString: String = Base64.encodeToString(
                            signature.sign(), Base64.URL_SAFE
                        )
                        PreyLogger.d(signatureString)
                        PreyConfig.getInstance(applicationContext).setVerificateBiometric(true)

                    } catch (e: SignatureException) {
                        throw RuntimeException()
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

    /**
     * Gets the main thread executor.
     *
     * @return The main thread executor.
     */
    private val mainThreadExecutor: Executor
        get() {
            return MainThreadExecutor()
        }

    /**
     * A custom executor that runs tasks on the main thread.
     */
    private class MainThreadExecutor() : Executor {
        private val handler: Handler = Handler(Looper.getMainLooper())

        override fun execute(r: Runnable) {
            handler.post(r)
        }
    }

    @Throws(Exception::class)
    fun generateKeyPair(keyName: String?, invalidatedByBiometricEnrollment: Boolean): KeyPair {
        val keyPairGenerator: KeyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        var builder: KeyGenParameterSpec.Builder? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder = KeyGenParameterSpec.Builder(
                (keyName)!!,
                KeyProperties.PURPOSE_SIGN
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(
                    KeyProperties.DIGEST_SHA256,
                    KeyProperties.DIGEST_SHA384,
                    KeyProperties.DIGEST_SHA512
                )
                .setUserAuthenticationRequired(true)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder!!.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyPairGenerator.initialize(builder!!.build())
        }
        return keyPairGenerator.generateKeyPair()
    }

    @Throws(Exception::class)
    fun initSignature(keyName: String?): Signature? {
        val keyPair: KeyPair? = getKeyPair(keyName)
        if (keyPair != null) {
            val signature: Signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(keyPair.private)
            return signature
        }
        return null
    }

    /**
     * Generates a new key pair using the Android KeyStore.
     *
     * @param keyName the name of the key pair to generate
     * @param invalidatedByBiometricEnrollment whether the key pair should be invalidated by biometric enrollment
     * @return the generated key pair
     * @throws Exception if an error occurs during key pair generation
     */
    @Throws(Exception::class)
    fun getKeyPair(keyName: String?): KeyPair? {
        val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(keyName)) {
            val publicKey: PublicKey = keyStore.getCertificate(keyName).publicKey
            val privateKey: PrivateKey = keyStore.getKey(keyName, null) as PrivateKey
            return KeyPair(publicKey, privateKey)
        }
        return null
    }

    /**
     * Method opens biometric
     */
    fun openBiometric() {
        if (PreyPermission.checkBiometricSupport(this)) {  // Check whether this device can authenticate with biometrics
            val signature: Signature?
            try {
                val keyPair: KeyPair = generateKeyPair(KEY_NAME, true)
                mToBeSignedMessage =
                    (Base64.encodeToString(keyPair.public.encoded, Base64.URL_SAFE) +
                            ":" +
                            KEY_NAME)
                // ":" +
                // Generated by the server to protect against replay attack
                // "";
                signature = initSignature(KEY_NAME)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            showBiometricPrompt(signature, 2)
        }
    }

    var error: String? = null

    /**
     * AsyncTask to add a device with recovered the key from the restrictions.
     * This task registers a new device with the provided API key, device type, and device name.
     */
    fun addDeviceWithRestriction(apiKey: String) {
        /**
         * Performs the device registration in the background.
         *
         * @param data API key, device type, and device name.
         * @return null
         */

        // Reset error message
        error = null
        try {
            // Get application context
            val context: Context = applicationContext
            // Extract API key, device type, and device name from input data

            PreyConfig.getInstance(context).registerNewDeviceWithApiKey(apiKey)
        } catch (e: Exception) {
            PreyLogger.e("Error:${e.message}", e)
            error = e.message
        }

        /**
         * Called after device registration is complete.
         * Reloads the activity if no error occurred.
         *
         * @param unused unused
         */

        if (error == null) {
            // Reload activity
            reload()
        }

    }

    companion object {
        var JS_ALIAS: String = "Android"
        var URL_ONB: String = "file:///android_asset/html/index.html"

        val CLOSE_PREY: String = "close_prey"
        var OVERLAY_PERMISSION_REQ_CODE: Int = 5469
        var FILE_CHOOSER_RESULT_CODE: Int = 6969

        private val REQUEST_PERMISSIONS: Int = 5
        private val REQUEST_PERMISSIONS_LOCATION: Int = 6
        private val REQUEST_PERMISSIONS_POST_NOTIFICATIONS: Int = 12
        private val INITIAL_PERMS: Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )

        @TargetApi(Build.VERSION_CODES.TIRAMISU)
        private val INITIAL_PERMS_TIRAMISU: Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO
        )

        private val SECURITY_PRIVILEGES: Int = 10
        val KEY_NAME: String = UUID.randomUUID().toString()
    }
}