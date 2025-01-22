/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.prey.R
import com.prey.kotlin.PreyLogger
import com.prey.barcodereader.ui.camera.kotlin.CameraSource
import com.prey.barcodereader.ui.camera.kotlin.CameraSourcePreview
import java.io.IOException

class BarcodeCaptureActivity : AppCompatActivity() {
    private var mCameraSource: CameraSource? = null
    private var mPreview: CameraSourcePreview? = null
   // private var mGraphicOverlay: GraphicOverlay<BarcodeGraphic>? = null

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        supportActionBar!!.hide()
        setContentView(R.layout.barcode_capture)

        mPreview = findViewById<View>(R.id.preview) as CameraSourcePreview
       // mGraphicOverlay = findViewById<View>(R.id.graphicOverlay) as GraphicOverlay<BarcodeGraphic>

        val autoFocus = intent.getBooleanExtra(AutoFocus, false)
        val useFlash = intent.getBooleanExtra(UseFlash, false)

        val rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            requestCameraPermission()
        }

        gestureDetector = GestureDetector(this, CaptureGestureListener())
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener())
    }

    private fun requestCameraPermission() {
        PreyLogger.d("Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val thisActivity: Activity = this

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(
                thisActivity, permissions,
                RC_HANDLE_CAMERA_PERM
            )
        }
        //TODO:Cambiar
/*
        Snackbar.make(
            mGraphicOverlay, R.string.permission_camera_rationale,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.ok, listener)
            .show()*/
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val b = scaleGestureDetector!!.onTouchEvent(e)

        val c = gestureDetector!!.onTouchEvent(e)

        return b || c || super.onTouchEvent(e)
    }

    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {
        val context = applicationContext

        val barcodeDetector = BarcodeDetector.Builder(context)
            .setBarcodeFormats(Barcode.DRIVER_LICENSE or Barcode.DATA_MATRIX or Barcode.QR_CODE or Barcode.PDF417)
            .build()
        //TODO:cambiar
        /*
        val barcodeFactory = BarcodeTrackerFactory(mGraphicOverlay!!, this)
        barcodeDetector.setProcessor(
            MultiProcessor.Builder(barcodeFactory).build()
        )*/

        if (!barcodeDetector.isOperational) {
            PreyLogger.d("Detector dependencies are not yet available.")

            val lowstorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = registerReceiver(null, lowstorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show()
                PreyLogger.d(getString(R.string.low_storage_error))
            }
        }

        var builder = CameraSource.Builder(
            applicationContext, barcodeDetector
        )
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0f)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null
            )
        }

        mCameraSource = builder
            .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
            .build()
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        if (mPreview != null) {
            mPreview!!.stop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (mPreview != null) {
                mPreview!!.release()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            PreyLogger.d("Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults.size != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            PreyLogger.d("Camera permission granted - initialize the camera source")
            val autoFocus = intent.getBooleanExtra(AutoFocus, false)
            val useFlash = intent.getBooleanExtra(UseFlash, false)
            createCameraSource(autoFocus, useFlash)
            return
        }

        PreyLogger.d(
            "Permission not granted: results len = " + grantResults.size +
                    " Result code = " + (if (grantResults.size > 0) grantResults[0] else "(empty)")
        )

        val listener =
            DialogInterface.OnClickListener { dialog, id -> finish() }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Multitracker sample")
            .setMessage(R.string.no_camera_permission)
            .setPositiveButton(R.string.ok, listener)
            .show()
    }

    @Throws(SecurityException::class)
    private fun startCameraSource() {
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg =
                GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg!!.show()
        }

        if (mCameraSource != null) {
            try {
                //TODO:cambiar
              //  mPreview!!.start(mCameraSource, mGraphicOverlay)
            } catch (e: IOException) {
                PreyLogger.e("Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    private fun onTap(rawX: Float, rawY: Float): Boolean {
        //TODO:cambiar
        /*val graphic = mGraphicOverlay!!.firstGraphic
        var barcode: Barcode? = null
        if (graphic != null) {
            barcode = graphic.barcode

            if (barcode != null) {
                PreyLogger.d("displayValue:" + barcode.displayValue)
                val data = Intent()
                data.putExtra(BarcodeObject, barcode)
                setResult(CommonStatusCodes.SUCCESS, data)
                finish()
            } else {
                PreyLogger.d("barcode data is null")
            }
        } else {
            PreyLogger.d("no barcode detected")
        }
        return barcode != null
        */
        return false
    }

    fun updateBarcode(barcode: Barcode?) {
        if (barcode != null) {
            PreyLogger.d("displayValue:" + barcode.displayValue)
            val data = Intent()
            data.putExtra(BarcodeObject, barcode)
            setResult(CommonStatusCodes.SUCCESS, data)
            finish()
        }
    }

    private inner class CaptureGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

    private inner class ScaleListener : OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            mCameraSource!!.doZoom(detector.scaleFactor)
        }
    }

    companion object {
        private const val RC_HANDLE_GMS = 9001

        private const val RC_HANDLE_CAMERA_PERM = 2

        const val AutoFocus: String = "AutoFocus"
        const val UseFlash: String = "UseFlash"
        const val BarcodeObject: String = "Barcode"
    }
}
