/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.R
import com.prey.barcodereader.QrCodeViewModel

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode)
        if (!PreyConfig.getInstance(applicationContext).isTest()) {
            // Request camera permissions
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_BARCODE)
    }

    private fun startCamera() {
        val cameraController = LifecycleCameraController(baseContext)
        val previewView: PreviewView = findViewById(R.id.viewFinder)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                if ((barcodeResults == null) ||
                    (barcodeResults.size == 0) ||
                    (barcodeResults.first() == null)
                ) {
                    previewView.overlay.clear()
                    previewView.setOnTouchListener { _, _ -> false }
                    return@MlKitAnalyzer
                }
                val qrCodeViewModel = QrCodeViewModel(barcodeResults[0])
                PreyLogger.d("qrCodeViewModel qrContent: ${qrCodeViewModel.qrContent}")
                registerNewDevice(qrCodeViewModel.qrContent)
                previewView.overlay.clear()
            }
        )
        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    private fun registerNewDevice(qrContent: String) {
        if (qrContent.contains("prey?api_key=")) {
            val apikey = qrContent.substring(13)
            PreyLogger.i("___apikey:$apikey")
            val progressDialog = ProgressDialog(this@BarcodeActivity)
            progressDialog.setMessage(getText(R.string.set_old_user_loading).toString())
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.show()
            var error: String? = null
            try {
                PreyConfig.getInstance(applicationContext).registerNewDeviceWithApiKey(apikey)
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
                error = e.message
            }
            try {
                progressDialog.dismiss()
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            if (error == null) {
                val message = getString(R.string.device_added_congratulations_text)
                val bundle = Bundle()
                bundle.putString("message", message)
                bundle.putString(CheckPasswordHtmlActivity.NEXT_URL, "/activation")
                PreyConfig.getInstance(applicationContext).setCamouflageSet(true)
                var intent: Intent? = null
                intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Intent(applicationContext, JumpActivity::class.java)
                } else {
                    Intent(applicationContext, PermissionInformationActivity::class.java)
                }
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            } else {
                showDialog(ERROR)
            }
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PreyConfig.getInstance(applicationContext).isTest()) {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    companion object {
        const val ACTIVITY_BARCODE: String = "ACTIVITY_BARCODE"
        private const val ERROR = 3
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    R.string.permissions_not_granted_by_user,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

}