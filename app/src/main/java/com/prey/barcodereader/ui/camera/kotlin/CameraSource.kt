/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader.ui.camera.kotlin

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.PreviewCallback
import android.os.Build
import android.os.SystemClock
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.annotation.RequiresPermission
import com.google.android.gms.common.images.Size
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.prey.kotlin.PreyLogger
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.ceil

@Suppress("deprecation")
class CameraSource private constructor() {

    //TODO:Cambiar
    @Retention(AnnotationRetention.SOURCE)
    private annotation class FocusMode


    @Retention(AnnotationRetention.SOURCE)
    private annotation class FlashMode

    private var mContext: Context? = null
    private val mCameraLock = Any()
    private var mCamera: Camera? = null
    var cameraFacing: Int = CAMERA_FACING_BACK
        private set
    private var mRotation = 0
    var previewSize: Size? = null
        private set
    private var mRequestedFps = 30.0f
    private var mRequestedPreviewWidth = 1024
    private var mRequestedPreviewHeight = 768

    @get:FocusMode
    var focusMode: String? = null
        private set

    @get:FlashMode
    var flashMode: String? = null
        private set
    private var mDummySurfaceView: SurfaceView? = null
    private var mDummySurfaceTexture: SurfaceTexture? = null
    private var mProcessingThread: Thread? = null
    private var mFrameProcessor: FrameProcessingRunnable? = null
    private val mBytesToByteBuffer: MutableMap<ByteArray, ByteBuffer> = HashMap()

    class Builder(context: Context?, detector: Detector<*>?) {
        private val mDetector: Detector<*>
        private val mCameraSource = CameraSource()

        init {
            requireNotNull(context) { "No context supplied." }
            requireNotNull(detector) { "No detector supplied." }

            mDetector = detector
            mCameraSource.mContext = context
        }

        fun setRequestedFps(fps: Float): Builder {
            require(!(fps <= 0)) { "Invalid fps: $fps" }
            mCameraSource.mRequestedFps = fps
            return this
        }

        fun setFocusMode(@FocusMode mode: String?): Builder {
            mCameraSource.focusMode = mode
            return this
        }

        fun setFlashMode(@FlashMode mode: String?): Builder {
            mCameraSource.flashMode = mode
            return this
        }

        fun setRequestedPreviewSize(width: Int, height: Int): Builder {
            val MAX = 1000000
            require(!((width <= 0) || (width > MAX) || (height <= 0) || (height > MAX))) { "Invalid preview size: " + width + "x" + height }
            mCameraSource.mRequestedPreviewWidth = width
            mCameraSource.mRequestedPreviewHeight = height
            return this
        }

        fun setFacing(facing: Int): Builder {
            require(!((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT))) { "Invalid camera: $facing" }
            mCameraSource.cameraFacing = facing
            return this
        }

        fun build(): CameraSource {
            mCameraSource.mFrameProcessor = mCameraSource.FrameProcessingRunnable(mDetector)
            return mCameraSource
        }
    }

    interface ShutterCallback {
        fun onShutter()
    }

    interface PictureCallback {
        fun onPictureTaken(data: ByteArray?)
    }

    interface AutoFocusCallback {
        fun onAutoFocus(success: Boolean)
    }

    interface AutoFocusMoveCallback {
        fun onAutoFocusMoving(start: Boolean)
    }

    fun release() {
        synchronized(mCameraLock) {
            stop()
            mFrameProcessor!!.release()
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class)
    fun start(): CameraSource {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                return this
            }
            mCamera = createCamera()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                mDummySurfaceTexture = SurfaceTexture(DUMMY_TEXTURE_NAME)
                mCamera!!.setPreviewTexture(mDummySurfaceTexture)
            } else {
                mDummySurfaceView = SurfaceView(mContext)
                mCamera!!.setPreviewDisplay(mDummySurfaceView!!.holder)
            }
            mCamera!!.startPreview()
            mProcessingThread = Thread(mFrameProcessor)
            mFrameProcessor!!.setActive(true)
            mProcessingThread!!.start()
        }
        return this
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class)
    fun start(surfaceHolder: SurfaceHolder?): CameraSource {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                return this
            }
            mCamera = createCamera()
            mCamera!!.setPreviewDisplay(surfaceHolder)
            mCamera!!.startPreview()
            mProcessingThread = Thread(mFrameProcessor)
            mFrameProcessor!!.setActive(true)
            mProcessingThread!!.start()
        }
        return this
    }

    fun stop() {
        synchronized(mCameraLock) {
            mFrameProcessor!!.setActive(false)
            if (mProcessingThread != null) {
                try {
                    mProcessingThread!!.join()
                } catch (e: InterruptedException) {
                    PreyLogger.e("Frame processing thread interrupted on release.", e)
                }
                mProcessingThread = null
            }
            mBytesToByteBuffer.clear()
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.setPreviewCallbackWithBuffer(null)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        mCamera!!.setPreviewTexture(null)
                    } else {
                        mCamera!!.setPreviewDisplay(null)
                    }
                } catch (e: Exception) {
                    PreyLogger.e("Failed to clear camera preview: " + e.message, e)
                }
                mCamera!!.release()
                mCamera = null
            }
        }
    }

    fun doZoom(scale: Float): Int {
        synchronized(mCameraLock) {
            if (mCamera == null) {
                return 0
            }
            var currentZoom = 0
            val maxZoom: Int
            val parameters = mCamera!!.parameters
            if (!parameters.isZoomSupported) {
                PreyLogger.d("Zoom is not supported on this device")
                return currentZoom
            }
            maxZoom = parameters.maxZoom
            currentZoom = parameters.zoom + 1
            val newZoom = if (scale > 1) {
                currentZoom + scale * (maxZoom / 10)
            } else {
                currentZoom * scale
            }
            currentZoom = Math.round(newZoom) - 1
            if (currentZoom < 0) {
                currentZoom = 0
            } else if (currentZoom > maxZoom) {
                currentZoom = maxZoom
            }
            parameters.zoom = currentZoom
            mCamera!!.parameters = parameters
            return currentZoom
        }
    }

    fun takePicture(shutter: ShutterCallback?, jpeg: PictureCallback?) {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                val startCallback: PictureStartCallback = PictureStartCallback()
                startCallback.mDelegate = shutter
                val doneCallback: PictureDoneCallback = PictureDoneCallback()
                doneCallback.mDelegate = jpeg
                mCamera!!.takePicture(startCallback, null, null, doneCallback)
            }
        }
    }

    fun setFocusMode(@FocusMode mode: String?): Boolean {
        synchronized(mCameraLock) {
            if (mCamera != null && mode != null) {
                val parameters = mCamera!!.parameters
                if (parameters.supportedFocusModes.contains(mode)) {
                    parameters.focusMode = mode
                    mCamera!!.parameters = parameters
                    focusMode = mode
                    return true
                }
            }
            return false
        }
    }

    fun setFlashMode(@FlashMode mode: String?): Boolean {
        synchronized(mCameraLock) {
            if (mCamera != null && mode != null) {
                val parameters = mCamera!!.parameters
                if (parameters.supportedFlashModes.contains(mode)) {
                    parameters.flashMode = mode
                    mCamera!!.parameters = parameters
                    flashMode = mode
                    return true
                }
            }
            return false
        }
    }

    fun autoFocus(cb: AutoFocusCallback?) {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                var autoFocusCallback: CameraAutoFocusCallback? = null
                if (cb != null) {
                    autoFocusCallback = CameraAutoFocusCallback()
                    autoFocusCallback!!.mDelegate = cb
                }
                mCamera!!.autoFocus(autoFocusCallback)
            }
        }
    }

    fun cancelAutoFocus() {
        synchronized(mCameraLock) {
            if (mCamera != null) {
                mCamera!!.cancelAutoFocus()
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    fun setAutoFocusMoveCallback(cb: AutoFocusMoveCallback?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return false
        }
        synchronized(mCameraLock) {
            if (mCamera != null) {
                var autoFocusMoveCallback: CameraAutoFocusMoveCallback? = null
                if (cb != null) {
                    autoFocusMoveCallback = CameraAutoFocusMoveCallback()
                    autoFocusMoveCallback!!.mDelegate = cb
                }
                mCamera!!.setAutoFocusMoveCallback(autoFocusMoveCallback)
            }
        }
        return true
    }

    private inner class PictureStartCallback : Camera.ShutterCallback {
        var mDelegate: ShutterCallback? = null
        override fun onShutter() {
            mDelegate?.onShutter()
        }
    }

    private inner class PictureDoneCallback : Camera.PictureCallback {
        var mDelegate: PictureCallback? = null

        override fun onPictureTaken(data: ByteArray, camera: Camera) {
            mDelegate?.onPictureTaken(data)
            synchronized(mCameraLock) {
                if (mCamera != null) {
                    mCamera!!.startPreview()
                }
            }
        }
    }

    private inner class CameraAutoFocusCallback : Camera.AutoFocusCallback {
        var mDelegate: AutoFocusCallback? = null

        override fun onAutoFocus(success: Boolean, camera: Camera) {
            mDelegate?.onAutoFocus(success)
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private inner class CameraAutoFocusMoveCallback : Camera.AutoFocusMoveCallback {
        var mDelegate: AutoFocusMoveCallback? = null

        override fun onAutoFocusMoving(start: Boolean, camera: Camera) {
            mDelegate?.onAutoFocusMoving(start)
        }
    }

    @SuppressLint("InlinedApi")
    private fun createCamera(): Camera {
        val requestedCameraId = getIdForRequestedCamera(cameraFacing)
        if (requestedCameraId == -1) {
            throw RuntimeException("Could not find requested camera.")
        }
        val camera = Camera.open(requestedCameraId)
        val sizePair = selectSizePair(camera, mRequestedPreviewWidth, mRequestedPreviewHeight)
            ?: throw RuntimeException("Could not find suitable preview size.")
        val pictureSize = sizePair.pictureSize()
        previewSize = sizePair.previewSize()
        val previewFpsRange = selectPreviewFpsRange(camera, mRequestedFps)
            ?: throw RuntimeException("Could not find suitable preview frames per second range.")
        val parameters = camera.parameters
        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.width, pictureSize.height)
        }
        parameters.setPreviewSize(previewSize!!.width, previewSize!!.height)
        parameters.setPreviewFpsRange(
            previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
            previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
        )
        parameters.previewFormat = ImageFormat.NV21
        setRotation(camera, parameters, requestedCameraId)
        if (focusMode != null) {
            if (parameters.supportedFocusModes.contains(
                    focusMode
                )
            ) {
                parameters.focusMode = focusMode
            } else {
                PreyLogger.d("Camera focus mode: " + focusMode + " is not supported on this device.")
            }
        }
        focusMode = parameters.focusMode
        if (flashMode != null) {
            if (parameters.supportedFlashModes.contains(
                    flashMode
                )
            ) {
                parameters.flashMode = flashMode
            } else {
                PreyLogger.d("Camera flash mode: " + flashMode + " is not supported on this device.")
            }
        }
        flashMode = parameters.flashMode
        camera.parameters = parameters
        camera.setPreviewCallbackWithBuffer(CameraPreviewCallback())
        camera.addCallbackBuffer(createPreviewBuffer(previewSize))
        camera.addCallbackBuffer(createPreviewBuffer(previewSize))
        camera.addCallbackBuffer(createPreviewBuffer(previewSize))
        camera.addCallbackBuffer(createPreviewBuffer(previewSize))
        return camera
    }

    private class SizePair(
        previewSize: Camera.Size,
        pictureSize: Camera.Size?
    ) {
        private val mPreview =
            Size(previewSize.width, previewSize.height)
        private var mPicture: Size? = null

        init {
            if (pictureSize != null) {
                mPicture = Size(pictureSize.width, pictureSize.height)
            }
        }

        fun previewSize(): Size {
            return mPreview
        }

        @Suppress("unused")
        fun pictureSize(): Size? {
            return mPicture
        }
    }

    private fun selectPreviewFpsRange(camera: Camera, desiredPreviewFps: Float): IntArray? {
        val desiredPreviewFpsScaled = (desiredPreviewFps * 1000.0f).toInt()
        var selectedFpsRange: IntArray? = null
        var minDiff = Int.MAX_VALUE
        val previewFpsRangeList = camera.parameters.supportedPreviewFpsRange
        for (range in previewFpsRangeList) {
            val deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX]
            val deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]
            val diff = (abs(deltaMin.toDouble()) + abs(deltaMax.toDouble())).toInt()
            if (diff < minDiff) {
                selectedFpsRange = range
                minDiff = diff
            }
        }
        return selectedFpsRange
    }

    private fun setRotation(camera: Camera, parameters: Camera.Parameters, cameraId: Int) {
        val windowManager =
            mContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        var degrees = 0
        val rotation = windowManager.defaultDisplay.rotation
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
            else -> PreyLogger.d("Bad rotation value: $rotation")
        }
        val cameraInfo = CameraInfo()
        Camera.getCameraInfo(cameraId, cameraInfo)
        val angle: Int
        val displayAngle: Int
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360
            displayAngle = (360 - angle) // compensate for it being mirrored
        } else {  // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360
            displayAngle = angle
        }
        mRotation = angle / 90
        camera.setDisplayOrientation(displayAngle)
        parameters.setRotation(angle)
    }

    private fun createPreviewBuffer(previewSize: Size?): ByteArray {
        val bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21)
        val sizeInBits = (previewSize!!.height * previewSize.width * bitsPerPixel).toLong()
        val bufferSize = ceil(sizeInBits / 8.0) as Int + 1
        val byteArray = ByteArray(bufferSize)
        val buffer = ByteBuffer.wrap(byteArray)
        check(!(!buffer.hasArray() || (buffer.array() != byteArray))) { "Failed to create valid buffer for camera source." }
        mBytesToByteBuffer[byteArray] = buffer
        return byteArray
    }

    private inner class CameraPreviewCallback : PreviewCallback {
        override fun onPreviewFrame(data: ByteArray, camera: Camera) {
            mFrameProcessor!!.setNextFrame(data, camera)
        }
    }

    private inner class FrameProcessingRunnable(private var mDetector: Detector<*>?) : Runnable {
        private val mStartTimeMillis = SystemClock.elapsedRealtime()
        private val mLock = Any()
        private var mActive = true
        private var mPendingTimeMillis: Long = 0
        private var mPendingFrameId = 0
        private var mPendingFrameData: ByteBuffer? = null

        @SuppressLint("Assert")
        fun release() {
            assert(mProcessingThread!!.state == Thread.State.TERMINATED)
            mDetector!!.release()
            mDetector = null
        }

        fun setActive(active: Boolean) {
            synchronized(mLock) {
                mActive = active
                (mLock as Object).notifyAll()
            }
        }

        fun setNextFrame(data: ByteArray, camera: Camera) {
            synchronized(mLock) {
                if (mPendingFrameData != null) {
                    camera.addCallbackBuffer(mPendingFrameData!!.array())
                    mPendingFrameData = null
                }
                if (!mBytesToByteBuffer.containsKey(data)) {
                    PreyLogger.d(
                        "Skipping frame.  Could not find ByteBuffer associated with the image " +
                                "data from the camera."
                    )
                    return
                }
                mPendingTimeMillis = SystemClock.elapsedRealtime() - mStartTimeMillis
                mPendingFrameId++
                mPendingFrameData = mBytesToByteBuffer[data]
                (mLock as Object).notifyAll()
            }
        }

        override fun run() {
            var outputFrame: Frame?
            var data: ByteBuffer?
            while (true) {
                synchronized(mLock) {
                    while (mActive && (mPendingFrameData == null)) {
                        try {
                            (mLock as Object).wait()
                        } catch (e: InterruptedException) {
                            PreyLogger.e("Frame processing loop terminated.", e)
                            return
                        }
                    }
                    if (!mActive) {
                        return
                    }
                    outputFrame = Frame.Builder()
                        .setImageData(
                            mPendingFrameData, previewSize!!.width,
                            previewSize!!.height, ImageFormat.NV21
                        )
                        .setId(mPendingFrameId)
                        .setTimestampMillis(mPendingTimeMillis)
                        .setRotation(mRotation)
                        .build()
                    data = mPendingFrameData
                    mPendingFrameData = null
                }
                try {
                    mDetector!!.receiveFrame(outputFrame)
                } catch (t: Throwable) {
                    PreyLogger.e("Exception thrown from receiver.", t)
                } finally {
                    mCamera!!.addCallbackBuffer(data!!.array())
                }
            }
        }
    }

    companion object {
        @SuppressLint("InlinedApi")
        val CAMERA_FACING_BACK: Int = CameraInfo.CAMERA_FACING_BACK

        @SuppressLint("InlinedApi")
        val CAMERA_FACING_FRONT: Int = CameraInfo.CAMERA_FACING_FRONT
        private const val DUMMY_TEXTURE_NAME = 100
        private const val ASPECT_RATIO_TOLERANCE = 0.01f
        private fun getIdForRequestedCamera(facing: Int): Int {
            val cameraInfo = CameraInfo()
            for (i in 0 until Camera.getNumberOfCameras()) {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == facing) {
                    return i
                }
            }
            return -1
        }

        private fun selectSizePair(
            camera: Camera,
            desiredWidth: Int,
            desiredHeight: Int
        ): SizePair? {
            val validPreviewSizes = generateValidPreviewSizeList(camera)
            var selectedPair: SizePair? = null
            var minDiff = Int.MAX_VALUE
            for (sizePair in validPreviewSizes) {
                val size = sizePair.previewSize()
                val diff =
                    (abs((size.width - desiredWidth).toDouble()) + abs((size.height - desiredHeight).toDouble())).toInt()
                if (diff < minDiff) {
                    selectedPair = sizePair
                    minDiff = diff
                }
            }
            return selectedPair
        }

        private fun generateValidPreviewSizeList(camera: Camera): List<SizePair> {
            val parameters = camera.parameters
            val supportedPreviewSizes =
                parameters.supportedPreviewSizes
            val supportedPictureSizes =
                parameters.supportedPictureSizes
            val validPreviewSizes: MutableList<SizePair> = ArrayList()
            for (previewSize in supportedPreviewSizes) {
                val previewAspectRatio = previewSize.width.toFloat() / previewSize.height.toFloat()
                for (pictureSize in supportedPictureSizes) {
                    val pictureAspectRatio =
                        pictureSize.width.toFloat() / pictureSize.height.toFloat()
                    if (abs((previewAspectRatio - pictureAspectRatio).toDouble()) < ASPECT_RATIO_TOLERANCE) {
                        validPreviewSizes.add(SizePair(previewSize, pictureSize))
                        break
                    }
                }
            }
            if (validPreviewSizes.size == 0) {
                PreyLogger.d("No preview sizes have a corresponding same-aspect-ratio picture size")
                for (previewSize in supportedPreviewSizes) {
                    validPreviewSizes.add(SizePair(previewSize, null))
                }
            }
            return validPreviewSizes
        }
    }
}