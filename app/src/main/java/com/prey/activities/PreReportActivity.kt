/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.activities

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.hardware.Camera
import android.hardware.SensorManager
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.TextView
import com.prey.R
import com.prey.PreyConfig
import com.prey.PreyLogger
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class PreReportActivity : Activity(), SurfaceHolder.Callback,
    OrientationManager.OrientationListener {
    var focus: String = ""
    var firstPicture: Boolean = false
    var secondPicture: Boolean = false
    var orientationManager: OrientationManager? = null

    var mHolder: SurfaceHolder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_camera2)
        orientationManager = OrientationManager(
            applicationContext, SensorManager.SENSOR_DELAY_NORMAL,
            this
        )
        orientationManager!!.enable()
        val surfaceView = findViewById<View>(R.id.surfaceView1) as SurfaceView
        mHolder = surfaceView.holder
        //TODO:cambiar
        //mHolder.addCallback(this)
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        val titilliumWebBold =
            Typeface.createFromAsset(assets, "fonts/Titillium_Web/TitilliumWeb-Bold.ttf")
        val pre_report_title = findViewById<View>(R.id.pre_report_title) as TextView
        pre_report_title.typeface = titilliumWebBold
        //TODO:cambiar
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) CameraTask().executeOnExecutor(
            AsyncTask.THREAD_POOL_EXECUTOR
        )
        else CameraTask().execute()

         */
    }

    fun takePicture(ctx: Context, focus: String) {
        try {
            if (camera != null) {
                var parameters = camera!!.parameters
                if (PreyConfig.getInstance(ctx).isEclairOrAbove()) {
                    parameters = setParameters1(parameters)
                }
                parameters["iso"] = 400
                if (PreyConfig.getInstance(ctx).isFroyoOrAbove()) {
                    parameters = setParameters2(parameters)
                }
                camera!!.parameters = parameters
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
        try {
            if (camera != null) {
                camera!!.takePicture(shutterCallback, rawCallback, jpegCallback)
                PreyLogger.d("PreReportActivity open takePicture()")
            }
        } catch (e: Exception) {
            PreyLogger.e("Error:" + e.message, e)
        }
    }

    @TargetApi(5)
    private fun setParameters1(parameters: Camera.Parameters): Camera.Parameters {
        parameters.flashMode = Camera.Parameters.FLASH_MODE_OFF
        parameters.whiteBalance = Camera.Parameters.WHITE_BALANCE_AUTO
        parameters.sceneMode = Camera.Parameters.SCENE_MODE_AUTO
        return parameters
    }

    @TargetApi(8)
    private fun setParameters2(parameters: Camera.Parameters): Camera.Parameters {
        val progress = 0.5f
        val min = parameters.minExposureCompensation // -3 on my phone
        val max = parameters.maxExposureCompensation // 3 on my phone
        val realProgress = progress - 0.5f
        val value = if (realProgress < 0) {
            -(realProgress * 2 * min).toInt()
        } else {
            (realProgress * 2 * max).toInt()
        }
        PreyLogger.d("setExposureCompensation value:$value")
        PreyLogger.d("setExposureCompensation   max:" + parameters.maxExposureCompensation)
        parameters.exposureCompensation = value //parameters.getMaxExposureCompensation());
        return parameters
    }

    override fun onDestroy() {
        super.onDestroy()
        if (camera != null) {
            camera!!.stopPreview()
            camera!!.release()
            camera = null
        }
    }

    var shutterCallback: Camera.ShutterCallback = Camera.ShutterCallback { }

    var rawCallback: Camera.PictureCallback =
        Camera.PictureCallback { data, camera -> }

    var jpegCallback: Camera.PictureCallback =
        Camera.PictureCallback { data, camera ->
            PreyLogger.d("PreReportActivity camera jpegCallback")
            dataImagen = resizeImage(data)
            try {
                //Get route with Android 12 support
                val path = getExternalFilesDir(null).toString() + "/Prey/"
                PreyLogger.d("PreReportActivity path:$path")
                try {
                    File(path).mkdir()
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                val file = File("$path$focus.jpg")
                val bos = BufferedOutputStream(FileOutputStream(file))
                bos.write(dataImagen)
                bos.flush()
                bos.close()
                if (dataImagen != null) {
                    if (FRONT == focus) {
                        firstPicture = true
                    } else {
                        secondPicture = true
                    }
                }
            } catch (e: Exception) {
                PreyLogger.e("PreReportActivity camera jpegCallback err" + e.message, e)
            }
        }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (camera != null) {
            try {
                val parameters = camera!!.parameters
                camera!!.parameters = parameters
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                camera!!.startPreview()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        PreyLogger.d("PreReportActivity camera setPreviewDisplay()")
        mHolder = holder
        try {
            if (camera != null) camera!!.setPreviewDisplay(mHolder)
        } catch (e: Exception) {
            PreyLogger.e("Error PreviewDisplay:" + e.message, e)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        PreyLogger.d("PreReportActivity camera surfaceDestroyed()")
        if (camera != null) {
            try {
                camera!!.stopPreview()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                camera!!.release()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            camera = null
        }
    }

    private var screenIntOrientation = -1

    fun resizeImage(input: ByteArray): ByteArray {
        try {
            val original = BitmapFactory.decodeByteArray(input, 0, input.size)
            val resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, true)
            var resized2: Bitmap? = null
            val matrix = Matrix()
            PreyLogger.d("SimpleCameraActivity focus :$focus screenIntOrientation:$screenIntOrientation")
            if (screenIntOrientation == CODE_PORTRAIT) {
                if ("front" == focus) {
                    matrix.postRotate(90f)
                } else {
                    matrix.postRotate(270f)
                }
            }
            if (screenIntOrientation == CODE_REVERSED_PORTRAIT) {
                if ("front" == focus) {
                    matrix.postRotate(270f)
                } else {
                    matrix.postRotate(90f)
                }
            }
            if (screenIntOrientation == CODE_LANDSCAPE) {
                if ("front" == focus) {
                    matrix.postRotate(0f)
                } else {
                    matrix.postRotate(0f)
                }
            }
            if (screenIntOrientation == CODE_REVERSED_LANDSCAPE) {
                if ("front" == focus) {
                    matrix.postRotate(180f)
                } else {
                    matrix.postRotate(180f)
                }
            }
            resized2 =
                Bitmap.createBitmap(resized, 0, 0, resized.width, resized.height, matrix, true)
            val blob = ByteArrayOutputStream()
            resized2.compress(Bitmap.CompressFormat.JPEG, 100, blob)
            return blob.toByteArray()
        } catch (e: Exception) {
            return input
        }
    }

    //TODO:cambiar
    /*
    private inner class CameraTask : AsyncTask<String?, Void?, Void?>() {
        var progressDialog: ProgressDialog? = null
        var preyLocation: PreyLocation? = null
        override fun onPreExecute() {
            PreyLogger.d("PreReportActivity antes camera")
            try {
                progressDialog = ProgressDialog(this@PreReportActivity)
                progressDialog!!.setMessage(
                    applicationContext.getText(R.string.pre_report_camera1).toString()
                )
                progressDialog!!.isIndeterminate = true
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
            } catch (e: Exception) {
            }
        }

        /**
         * Method that generates a pre-report and when obtaining the images from the cameras it is retried 5 times
         *
         */
        override fun doInBackground(vararg data: String): Void? {
            try {
                firstPicture = false
                secondPicture = false
                //Get route with Android 12 support
                val path = getExternalFilesDir(null).toString() + "/Prey/"
                val file1 = File(path + "" + FRONT + ".jpg")
                file1.delete()
                val file2 = File(path + "" + BACK + ".jpg")
                file2.delete()
                val file3 = File(path + "map.jpg")
                file3.delete()
                focus = FRONT
                var f = 0
                while (f < MAX_RETRIES) {
                    val cameraInfo = CameraInfo()
                    Camera.getCameraInfo(0, cameraInfo)
                    camera = Camera.open(0)
                    if (camera != null) {
                        try {
                            camera!!.setPreviewDisplay(mHolder)
                            camera!!.startPreview()
                        } catch (e: Exception) {
                            PreyLogger.e("Error:" + e.message, e)
                        }
                    }
                    takePicture(applicationContext, focus)
                    var i = 0
                    while (i < 20 && !firstPicture) {
                        Thread.sleep(500)
                        i++
                    }
                    camera.stopPreview()
                    camera.release()
                    if (firstPicture) {
                        f = MAX_RETRIES
                    }
                    f++
                }
                PreyLogger.d("PreReportActivity Camera 1 size:" + (if (dataImagen == null) -1 else dataImagen!!.size))
            } catch (e: Exception) {
                PreyLogger.e("Camera 1 error:" + e.message, e)
            }
            try {
                Thread.sleep(2000)
                try {
                    if (progressDialog != null) progressDialog!!.setMessage(
                        applicationContext.getText(
                            R.string.pre_report_camera2
                        ).toString()
                    )
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                focus = BACK
                var b = 0
                while (b < MAX_RETRIES) {
                    val cameraInfo = CameraInfo()
                    Camera.getCameraInfo(1, cameraInfo)
                    camera = Camera.open(1)
                    if (camera != null) {
                        try {
                            camera!!.setPreviewDisplay(mHolder)
                            camera!!.startPreview()
                        } catch (e: Exception) {
                            PreyLogger.e("Error:" + e.message, e)
                        }
                    }
                    takePicture(applicationContext, focus)
                    var i = 0
                    while (i < 20 && !secondPicture) {
                        Thread.sleep(500)
                        i++
                    }
                    if (secondPicture) {
                        b = MAX_RETRIES
                    }
                    b++
                }
                PreyLogger.d("PreReportActivity Camera 2 size:" + (if (dataImagen == null) -1 else dataImagen!!.size))
            } catch (e: Exception) {
                PreyLogger.e("Camera 2 error:" + e.message, e)
            }
            try {
                if (progressDialog != null) progressDialog!!.setMessage(
                    applicationContext.getText(R.string.pre_report_location).toString()
                )
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                preyLocation = LocationUtil.getLocation(applicationContext, null, true)
                if (preyLocation != null && preyLocation!!.location != null && preyLocation!!.location.latitude != 0.0 && preyLocation!!.location.longitude != 0.0) {
                    PreyConfig.getInstance(applicationContext).setLocation(preyLocation)
                } else {
                    preyLocation = PreyConfig.getInstance(applicationContext).getLocation()
                }
            } catch (e: Exception) {
                PreyLogger.e("error location:" + e.message, e)
            }
            try {
                if (progressDialog != null) progressDialog!!.setMessage(
                    applicationContext.getText(R.string.pre_report_public_ip).toString()
                )
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            try {
                val phone: PreyPhone = PreyPhone(applicationContext)
                val publicIp: String = phone.iPAddress
                PreyConfig.getInstance(applicationContext).setPublicIp(publicIp)
                val wifiPhone:  PreyPhone.Wifi = phone.wifi!!
                val ssid = if (wifiPhone.ssid == null) "" else wifiPhone.ssid
                val model = Build.MODEL
                var vendor = "Google"
                try {
                    vendor = AboveCupcakeSupport.getDeviceVendor()
                } catch (e: Exception) {
                    PreyLogger.e("Error:" + e.message, e)
                }
                val imei: String = phone.hardware.serialNumber!!
                PreyConfig.getInstance(applicationContext).setSsid(ssid)
                PreyConfig.getInstance(applicationContext).setImei(imei)
                PreyConfig.getInstance(applicationContext).setModel("$model $vendor")
            } catch (e: Exception) {
                PreyLogger.e("error public_ip:" + e.message, e)
            }
            return null
        }

        override fun onPostExecute(unused: Void?) {
            PreyLogger.d("PreReportActivity post camera")
            try {
                if (progressDialog != null) progressDialog!!.dismiss()
            } catch (e: Exception) {
                PreyLogger.e("Error:" + e.message, e)
            }
            val intent = Intent(applicationContext, ReportActivity::class.java)
            if (preyLocation != null) {
                intent.putExtra("lat", preyLocation!!.lat)
                intent.putExtra("lng", preyLocation!!.lng)
            }
            startActivity(intent)
            finish()
        }
    }
*/
    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation?) {
        //TODO: cambiar
        /* when (attr.screenOrientation) {
             com.prey.activities.PreReportActivity.ScreenOrientation.PORTRAIT -> screenIntOrientation = 1
             REVERSED_PORTRAIT -> screenIntOrientation = 2
             REVERSED_LANDSCAPE -> screenIntOrientation = 3
             LANDSCAPE -> screenIntOrientation = 4
        }*/
    }


    companion object {
        var activity: PreReportActivity? = null
        var camera: Camera? = null
        var mHolder: SurfaceHolder? = null
        var dataImagen: ByteArray? = null
        var BACK: String = "back"
        var FRONT: String = "front"
        const val CODE_PORTRAIT: Int = 1
        const val CODE_REVERSED_PORTRAIT: Int = 2
        const val CODE_REVERSED_LANDSCAPE: Int = 3
        const val CODE_LANDSCAPE: Int = 4
        const val MAX_RETRIES: Int = 5

        private const val PHOTO_HEIGHT = 1024
        private const val PHOTO_WIDTH = 768

        enum class ScreenOrientation {
            REVERSED_LANDSCAPE, LANDSCAPE, PORTRAIT, REVERSED_PORTRAIT
        }

    }
}