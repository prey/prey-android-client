/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader.ui.camera.kotlin

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.google.android.gms.vision.CameraSource

class GraphicOverlay<T : GraphicOverlay.Graphic?>(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private val mLock = Any()
    private var mPreviewWidth = 0
    private var mWidthScaleFactor = 1.0f
    private var mPreviewHeight = 0
    private var mHeightScaleFactor = 1.0f
    private var mFacing = CameraSource.CAMERA_FACING_BACK
    private val mGraphics: MutableSet<T> = HashSet()
    private var mFirstGraphic: T? = null

    abstract class Graphic(private val mOverlay: GraphicOverlay<*>) {
        abstract fun draw(canvas: Canvas?)
        fun scaleX(horizontal: Float): Float {
            return horizontal * mOverlay.mWidthScaleFactor
        }

        fun scaleY(vertical: Float): Float {
            return vertical * mOverlay.mHeightScaleFactor
        }

        fun translateX(x: Float): Float {
            return if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                mOverlay.width - scaleX(x)
            } else {
                scaleX(x)
            }
        }

        fun translateY(y: Float): Float {
            return scaleY(y)
        }

        fun postInvalidate() {
            mOverlay.postInvalidate()
        }
    }

    fun clear() {
        synchronized(mLock) {
            mGraphics.clear()
            mFirstGraphic = null
        }
        postInvalidate()
    }

    fun add(graphic: T) {
        synchronized(mLock) {
            mGraphics.add(graphic)
            if (mFirstGraphic == null) {
                mFirstGraphic = graphic
            }
        }
        postInvalidate()
    }

    fun remove(graphic: T) {
        synchronized(mLock) {
            mGraphics.remove(graphic)
            if (mFirstGraphic != null && mFirstGraphic == graphic) {
                mFirstGraphic = null
            }
        }
        postInvalidate()
    }

    val firstGraphic: T?
        get() {
            synchronized(mLock) {
                return mFirstGraphic
            }
        }

    fun setCameraInfo(previewWidth: Int, previewHeight: Int, facing: Int) {
        synchronized(mLock) {
            mPreviewWidth = previewWidth
            mPreviewHeight = previewHeight
            mFacing = facing
        }
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        synchronized(mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = canvas.width.toFloat() / mPreviewWidth.toFloat()
                mHeightScaleFactor = canvas.height.toFloat() / mPreviewHeight.toFloat()
            }
            //TODO:cambiar
            /*
            for (graphic in mGraphics) {
                graphic.draw(canvas)
            }*/
        }
    }
}