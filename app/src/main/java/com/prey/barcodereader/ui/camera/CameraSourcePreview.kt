/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader.ui.camera

import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import com.prey.PreyLogger
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

class CameraSourcePreview(private val mContext: Context, attrs: AttributeSet?) :
    ViewGroup(mContext, attrs) {
    private val mSurfaceView = SurfaceView(mContext)
    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mCameraSource: CameraSource? = null

    private var mOverlay: GraphicOverlay<*>? = null

    init {
        mSurfaceView.holder.addCallback(SurfaceCallback())
        addView(mSurfaceView)
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class, SecurityException::class)
    fun start(cameraSource: CameraSource?) {
        if (cameraSource == null) {
            stop()
        }
        mCameraSource = cameraSource
        if (mCameraSource != null) {
            mStartRequested = true
            startIfReady()
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class, SecurityException::class)
    fun start(cameraSource: CameraSource?, overlay: GraphicOverlay<*>?) {
        mOverlay = overlay
        start(cameraSource)
    }

    fun stop() {
        if (mCameraSource != null) {
            mCameraSource!!.stop()
        }
    }

    fun release() {
        if (mCameraSource != null) {
            mCameraSource!!.release()
            mCameraSource = null
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    @Throws(IOException::class, SecurityException::class)
    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource!!.start(mSurfaceView.holder)
            if (mOverlay != null) {
                val size = mCameraSource!!.previewSize
                val min =
                    min(size!!.width.toDouble(), size!!.height.toDouble()).toInt()
                val max =
                    max(size!!.width.toDouble(), size!!.height.toDouble()).toInt()
                if (isPortraitMode) {
                    mOverlay!!.setCameraInfo(min, max, mCameraSource!!.cameraFacing)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mCameraSource!!.cameraFacing)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (se: SecurityException) {
                PreyLogger.e("Do not have permission to start the camera", se)
            } catch (e: IOException) {
                PreyLogger.e("Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var width = 320
        var height = 240
        if (mCameraSource != null) {
            val size = mCameraSource!!.previewSize
            if (size != null) {
                width = size.width
                height = size.height
            }
        }
        if (isPortraitMode) {
            val tmp = width
            width = height
            height = tmp
        }
        val layoutWidth = right - left
        val layoutHeight = bottom - top
        var childWidth = layoutWidth
        var childHeight = ((layoutWidth.toFloat() / width.toFloat()) * height).toInt()
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight
            childWidth = ((layoutHeight.toFloat() / height.toFloat()) * width).toInt()
        }
        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, childWidth, childHeight)
        }
        try {
            startIfReady()
        } catch (se: SecurityException) {
            PreyLogger.e("Do not have permission to start the camera", se)
        } catch (e: IOException) {
            PreyLogger.e("Could not start camera source.", e)
        }
    }

    private val isPortraitMode: Boolean
        get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            PreyLogger.d("isPortraitMode returning false by default")
            return false
        }
}