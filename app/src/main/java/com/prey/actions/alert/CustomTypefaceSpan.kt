/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alert

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.util.LruCache

/**
 * A custom typeface span that allows you to apply a custom font to a text.
 *
 * @param context The context to use for loading the font.
 * @param fontName The name of the font to use.
 */
class CustomTypefaceSpan(private val context: Context, private val fontName: String) :
    MetricAffectingSpan() {

    // The font to use for this span.
    private lateinit var font: Typeface

    /**
     * Initializes the font for this span.
     */
    init {
        font = FontCache[fontName] ?: createFontFromAsset(context.assets, fontName)
    }

    /**
     * Updates the measure state of the text paint with the custom font.
     *
     * @param textPaint The text paint to update.
     */
    override fun updateMeasureState(textPaint: TextPaint) {
        applyFont(textPaint)
    }

    /**
     * Updates the draw state of the text paint with the custom font.
     *
     * @param textPaint The text paint to update.
     */
    override fun updateDrawState(textPaint: TextPaint) {
        applyFont(textPaint)
    }

    /**
     * Applies the custom font to the text paint.
     *
     * @param textPaint The text paint to update.
     */
    private fun applyFont(textPaint: TextPaint) {
        textPaint.typeface = font
        textPaint.flags = textPaint.flags or Paint.SUBPIXEL_TEXT_FLAG
    }

    /**
     * Creates a font from an asset.
     *
     * @param assetManager The asset manager to use for loading the font.
     * @param fontName The name of the font to load.
     * @return The loaded font.
     */
    private fun createFontFromAsset(assetManager: AssetManager, fontName: String): Typeface {
        val font = Typeface.createFromAsset(assetManager, fontName)
        FontCache.put(fontName, font)
        return font
    }

    companion object {
        private val FontCache = LruCache<String, Typeface>(12)
    }

}
