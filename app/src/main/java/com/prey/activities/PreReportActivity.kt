package com.prey.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.prey.PreyConfig
import com.prey.PreyConfig.Companion.getInstance
import com.prey.PreyLogger
import com.prey.PreyLogger.Companion.e
import com.prey.PreyPhone
import com.prey.R
import com.prey.actions.location.LastLocationService
import com.prey.actions.location.PreyLocation
import com.prey.actions.picture.PictureUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays

class PreReportActivity : ComponentActivity(), OrientationManager.OrientationListener {

    private var textureView: TextureView? = null
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSessions: CameraCaptureSession? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var imageDimension: Size? = null
    private var imageReader: ImageReader? = null
    private val REQUEST_CAMERA_PERMISSION: Int = 200
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null
    var focus: String? = null
    private var screenIntOrientation = -1
    private var orientationManager: OrientationManager? = null
    var camera: Camera? = null
    var mHolder: SurfaceHolder? = null
    var lastLocationServiceIntent: Intent? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_camera2)
        try {
            orientationManager = OrientationManager(
                applicationContext, SensorManager.SENSOR_DELAY_NORMAL,
                this
            )
            orientationManager!!.enable()
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        textureView = findViewById<View>(R.id.texture) as TextureView
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        // Start last location service
        lastLocationServiceIntent = Intent(applicationContext, LastLocationService::class.java)
        startService(lastLocationServiceIntent)
        if (textureView!!.isAvailable) {

        } else {
            textureView!!.surfaceTextureListener = textureListener
        }
        //openCamera()
        playWithCoroutines(applicationContext)
        PreyConfig.getInstance(applicationContext).setActivityView(ACTIVITY_PRE_REPORT)
    }

    private fun playWithCoroutines(context: Context) {
        PreyLogger.d("playWithCoroutines 1 ${Thread.currentThread()}")
        coroutineScope.launch {
            PreyLogger.d("playWithCoroutines 2 ${Thread.currentThread()}")
            execDelay(2, 1)

            PreyLogger.d("playWithCoroutines 3 ${this.coroutineContext}")
        }
        PreyLogger.d("Finished playWithCoroutines 4 ${Thread.currentThread()}")
    }

    private suspend fun execDelay(delay: Long, index: Int) {
        PreyLogger.d("execDelay $index ${Thread.currentThread()}")
        PreyLogger.d("start $delay")
        val progressDialog = ProgressDialog(this@PreReportActivity)
        try {
            progressDialog.setMessage(
                applicationContext.getText(R.string.pre_report_camera1).toString()
            )
            progressDialog.setIndeterminate(true)
            progressDialog.setCancelable(false)
            progressDialog.show()
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        focus =
            PictureUtil.BACK
        openCamera()
        delay(delay * 1000)
        takePicture(applicationContext)
        closeCamera()
        try {
            if (progressDialog != null) progressDialog.setMessage(
                applicationContext.getText(R.string.pre_report_camera2).toString()
            )
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        focus =
            PictureUtil.FRONT
        openCamera()
        delay(delay * 1000)
        takePicture(applicationContext)
        try {
            if (progressDialog != null) progressDialog.setMessage(
                applicationContext.getText(R.string.pre_report_location).toString()
            )
        } catch (e: java.lang.Exception) {
            e("PreReportActivity error:${e.message}", e)
        }
        var preyLocation: PreyLocation? = null
        try {
            preyLocation = getInstance(applicationContext).getLocation()
            if (preyLocation != null && preyLocation.getLocation() != null
            ) {
                getInstance(applicationContext).setLocation(preyLocation)
            } else {
                preyLocation = getInstance(applicationContext).getLocation()
            }
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            if (progressDialog != null) progressDialog.setMessage(
                applicationContext.getText(R.string.pre_report_public_ip).toString()
            )
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            val phone = PreyPhone.getInstance(applicationContext)
            val publicIp = phone.getIpAddress()
            getInstance(applicationContext).setPublicIp(publicIp)
            val wifiPhone = phone.getWifi()
            val ssid = if (wifiPhone!!.getSsid() == null) "" else wifiPhone!!.getSsid()!!
            val model = Build.MODEL
            var vendor = "Google"
            try {
                vendor = Build.MANUFACTURER
            } catch (e: java.lang.Exception) {
                e("PreReportActivity error:${e.message}", e)
            }
            val imei = phone.hardware!!.getSerialNumber()
            getInstance(applicationContext).setSsid(ssid)
            getInstance(applicationContext).setImei(imei)
            getInstance(applicationContext).setModel("$model $vendor")
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        val intent = Intent(applicationContext, ReportActivity::class.java)
        if (preyLocation != null) {
            intent.putExtra("lat", preyLocation.getLat())
            intent.putExtra("lng", preyLocation.getLng())
        }
        startActivity(intent)
        try {
            stopService(lastLocationServiceIntent)
        } catch (e: java.lang.Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        finish()
        PreyLogger.d("end $delay")
    }

    var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        }
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice!!.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            if (cameraDevice != null) {
                cameraDevice!!.close()
                cameraDevice = null
            }
        }
    }

    /**
     * Method start jobs on the background
     */
    protected fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    /**
     * Method stop jobs on the background
     */
    protected fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    /**
     * Method take picture
     */
    fun takePicture(context: Context) {
        if (null == cameraDevice) {
            openCamera()
        }
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            val characteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            val streamConfigurationMap =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val width = 240
            val height = 320
            val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurfaces: MutableList<Surface> = ArrayList(2)
            outputSurfaces.add(reader.surface)
            outputSurfaces.add(Surface(textureView!!.surfaceTexture))
            val captureBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
            if (PictureUtil.FRONT == focus) {
                val ORIENTATIONS = SparseIntArray()
                ORIENTATIONS.append(Surface.ROTATION_0, 270)
                ORIENTATIONS.append(Surface.ROTATION_90, 0)
                ORIENTATIONS.append(Surface.ROTATION_180, 90)
                ORIENTATIONS.append(Surface.ROTATION_270, 180)
                captureBuilder.set(
                    CaptureRequest.JPEG_ORIENTATION,
                    ORIENTATIONS[screenIntOrientation]
                )
            } else {
                val ORIENTATIONS = SparseIntArray()
                ORIENTATIONS.append(Surface.ROTATION_0, 90)
                ORIENTATIONS.append(Surface.ROTATION_90, 0)
                ORIENTATIONS.append(Surface.ROTATION_180, 270)
                ORIENTATIONS.append(Surface.ROTATION_270, 180)
                captureBuilder.set(
                    CaptureRequest.JPEG_ORIENTATION,
                    ORIENTATIONS[screenIntOrientation]
                )
            }
            val readerListener: OnImageAvailableListener = object : OnImageAvailableListener {
                override fun onImageAvailable(reader: ImageReader) {
                    var image: Image? = null
                    try {
                        image = reader.acquireLatestImage()
                        val buffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer[bytes]
                        save(bytes)
                    } catch (e: Exception) {
                        PreyLogger.e("Error: ${e.message}", e)
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                fun save(bytes: ByteArray) {
                    val dataImagen = getCompressedBitmap(bytes)
                    try {
                        //Get route with Android 12 support
                        val directoryPath = "${getExternalFilesDir(null).toString()}/Prey/"
                        PreyLogger.d("PreReportActivity directoryPath:${directoryPath}")
                        try {
                            File(directoryPath).mkdir()
                        } catch (e: java.lang.Exception) {
                            PreyLogger.e("PreReportActivity error:${e.message}", e)
                        }
                        val file = File("$directoryPath$focus.jpg")
                        PreyLogger.d("PreReportActivity file:$directoryPath$focus.jpg")
                        val bos = BufferedOutputStream(FileOutputStream(file))
                        bos.write(dataImagen)
                        bos.flush()
                        bos.close()
                    } catch (e: java.lang.Exception) {
                        PreyLogger.e("PreReportActivity error:${e.message}", e)
                    }
                }
            }
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
            val captureListener: CaptureCallback = object : CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    createCameraPreview()
                }
            }
            cameraDevice!!.createCaptureSession(
                outputSurfaces,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        try {
                            session.capture(
                                captureBuilder.build(),
                                captureListener,
                                mBackgroundHandler
                            )
                        } catch (e: Exception) {
                            PreyLogger.e("Error: ${e.message}", e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }
                },
                mBackgroundHandler
            )
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    protected fun createCameraPreview() {
        try {
            val texture = checkNotNull(textureView!!.surfaceTexture)
            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
            val surface = Surface(texture)
            captureRequestBuilder =
                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder!!.addTarget(surface)
            cameraDevice!!.createCaptureSession(
                Arrays.asList(surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (null == cameraDevice) {
                            return
                        }
                        cameraCaptureSessions = cameraCaptureSession
                        updatePreview()
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    }
                },
                null
            )
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    /**
     * Method open camera
     */
    private fun openCamera() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            cameraId = if (PictureUtil.FRONT == focus) {
                manager.cameraIdList[1]
            } else {
                manager.cameraIdList[0]
            }
            val characteristics = manager.getCameraCharacteristics(cameraId!!)
            val map =
                checkNotNull(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP))
            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@PreReportActivity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    protected fun updatePreview() {
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            if (cameraCaptureSessions != null) cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(), null, mBackgroundHandler
            )
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
    }

    /**
     * Method close camera
     */
    private fun closeCamera() {
        if (null != cameraDevice) {
            cameraDevice!!.close()
            cameraDevice = null
        }
        if (null != imageReader) {
            imageReader!!.close()
            imageReader = null
        }
    }


    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    /**
     * Method set orientation
     */
    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation?) {
        when (screenOrientation) {
            OrientationManager.ScreenOrientation.PORTRAIT -> screenIntOrientation = 0
            OrientationManager.ScreenOrientation.REVERSED_PORTRAIT -> screenIntOrientation = 2
            OrientationManager.ScreenOrientation.REVERSED_LANDSCAPE -> screenIntOrientation = 3
            OrientationManager.ScreenOrientation.LANDSCAPE -> screenIntOrientation = 1
            else -> screenIntOrientation = 0
        }
    }

    /**
     * Method to compress image
     */
    fun getCompressedBitmap(bytes: ByteArray): ByteArray {
        val maxHeight = 1920.0f
        val maxWidth = 1080.0f
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth
        var imgRatio = actualWidth.toFloat() / actualHeight.toFloat()
        val maxRatio = maxWidth / maxHeight
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)
        options.inJustDecodeBounds = false
        options.inDither = false
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
            bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )
        try {
            val matrix = Matrix()
            if (PictureUtil.FRONT == focus) {
                if (screenIntOrientation == 0) {
                    matrix.postRotate(270f)
                } else if (screenIntOrientation == 1) {
                    matrix.postRotate(0f)
                } else if (screenIntOrientation == 2) {
                    matrix.postRotate(90f)
                } else {
                    matrix.postRotate(180f)
                }
            } else {
                if (screenIntOrientation == 0) {
                    matrix.postRotate(90f)
                } else if (screenIntOrientation == 1) {
                    matrix.postRotate(0f)
                } else if (screenIntOrientation == 2) {
                    matrix.postRotate(270f)
                } else {
                    matrix.postRotate(180f)
                }
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )
        } catch (e: Exception) {
            PreyLogger.e("Error: ${e.message}", e)
        }
        val out = ByteArrayOutputStream()
        scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }

    /**
     * Method calculates the size of the image
     *
     * @return size
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    companion object {
        const val ACTIVITY_PRE_REPORT: String = "ACTIVITY_PRE_REPORT"
    }

}