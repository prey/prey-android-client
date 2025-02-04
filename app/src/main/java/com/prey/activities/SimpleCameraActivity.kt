package com.prey.activities

import android.Manifest
import android.app.Activity
import android.content.Context
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
import androidx.core.app.ActivityCompat
import com.prey.R
import com.prey.actions.picture.PictureUtil
import com.prey.PreyLogger

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Arrays

class SimpleCameraActivity : Activity()  , OrientationManager.OrientationListener {

    private var textureView: TextureView? = null
    private var cameraId: String? = null
    protected var cameraDevice: CameraDevice? = null
    protected var cameraCaptureSessions: CameraCaptureSession? = null
    protected var captureRequestBuilder: CaptureRequest.Builder? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        val extras = intent.extras
        var kill = -1
        try {
            kill = extras!!.getInt("kill")
        } catch (e: Exception) {
            PreyLogger.e("report error:" + e.message, e)
        }
        PreyLogger.d("Kill:$kill")
        if (kill == 1) {
            //if finish the activity
            finish()
        } else {
            //gets the camera to use
            focus = if (extras != null) {
                extras.getString("focus")
            } else {
                PictureUtil.BACK
            }
            try {
                orientationManager = OrientationManager(
                    applicationContext, SensorManager.SENSOR_DELAY_NORMAL,
                    this
                )
                orientationManager!!.enable()
            } catch (e: Exception) {
                PreyLogger.e("report error:" + e.message, e)
            }
        }
        textureView = findViewById<View>(R.id.texture) as TextureView
        checkNotNull(textureView)
        textureView!!.surfaceTextureListener = textureListener
        PictureUtil.getInstance(this).activity = this
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
            PreyLogger.e("report error:" + e.message, e)
        }
    }

    /**
     * Method take picture
     */
    fun takePicture(ctx: Context) {
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
                        PreyLogger.e("report error:" + e.message, e)
                    } finally {
                        image?.close()
                    }
                }

                @Throws(IOException::class)
                fun save(bytes: ByteArray) {
                    PictureUtil.getInstance(ctx).dataImagen = getCompressedBitmap(bytes)
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
                            PreyLogger.e("report error:" + e.message, e)
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                    }
                },
                mBackgroundHandler
            )
        } catch (e: Exception) {
          //  PreyLogger.e("report error:" + e.message, e)
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
            PreyLogger.e("report error:" + e.message, e)
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
                    this@SimpleCameraActivity,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_CAMERA_PERMISSION
                )
                return
            }
            manager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: Exception) {
         //   PreyLogger.e("report error:" + e.message, e)
        }
    }

    protected fun updatePreview() {
        captureRequestBuilder!!.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        try {
            if (cameraCaptureSessions != null) cameraCaptureSessions!!.setRepeatingRequest(
                captureRequestBuilder!!.build(), null, mBackgroundHandler
            )
        } catch (e: Exception) {
         //   PreyLogger.e("report error:" + e.message, e)
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

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            textureView!!.surfaceTextureListener = textureListener
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
            PreyLogger.e("report error:" + e.message, e)
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (e: Exception) {
            PreyLogger.e("report error:" + e.message, e)
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
            PreyLogger.e("report error:" + e.message, e)
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


}