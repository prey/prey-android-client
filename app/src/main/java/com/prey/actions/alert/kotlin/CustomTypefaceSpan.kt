/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert.kotlin

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.util.LruCache

class CustomTypefaceSpan(private val context: Context, private val typefaceName: String) :
    MetricAffectingSpan() {

    private lateinit var typeface: Typeface

    init {
        typeface = TypefaceCache[typefaceName] ?: run {
            Typeface.createFromAsset(context.assets, typefaceName).also {
                TypefaceCache.put(typefaceName, it)
            }
        }
    }

    override fun updateMeasureState(textPaint: TextPaint) {
        textPaint.typeface = typeface
        textPaint.flags = textPaint.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    override fun updateDrawState(textPaint: TextPaint) {
        textPaint.typeface = typeface
        textPaint.flags = textPaint.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    companion object {
        private val TypefaceCache = LruCache<String, Typeface>(12)
    }
}
