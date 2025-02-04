/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.barcodereader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.android.gms.vision.barcode.Barcode
import com.prey.barcodereader.ui.camera.GraphicOverlay


class BarcodeGraphic : GraphicOverlay.Graphic {


    val COLOR_CHOICES: IntArray = intArrayOf(
        Color.BLUE,
        Color.CYAN,
        Color.GREEN
    )
    var mCurrentColorIndex: Int = 0

    constructor (overlay: GraphicOverlay<*>) : super(overlay) {


        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.size
        val selectedColor = COLOR_CHOICES[mCurrentColorIndex]

        mRectPaint = Paint()
        mRectPaint!!.color = selectedColor
        mRectPaint!!.style = Paint.Style.STROKE
        mRectPaint!!.strokeWidth = 4.0f

        mTextPaint = Paint()
        mTextPaint!!.color = selectedColor
        mTextPaint!!.textSize = 36.0f
    }

    private var mId = 0
    private var mRectPaint: Paint? = null
    private var mTextPaint: Paint? = null


    private val mBarcode: Barcode? = null


    override fun draw(canvas: Canvas?) {
        val barcode: Barcode = mBarcode ?: return

        val rect = RectF(barcode.boundingBox)
        rect.left = translateX(rect.left)
        rect.top = translateY(rect.top)
        rect.right = translateX(rect.right)
        rect.bottom = translateY(rect.bottom)
        canvas!!.drawRect(rect, mRectPaint!!)
        var barcodeValue = barcode.rawValue
        if (barcodeValue.contains("prey")) barcodeValue = ""
        canvas!!.drawText(barcodeValue, rect.left, rect.bottom, mTextPaint!!)
    }

    fun getId(): Int {
        return mId
    }

    fun setId(id: Int) {
        this.mId = id
    }

    fun getBarcode(): Barcode {
        return mBarcode!!
    }

}