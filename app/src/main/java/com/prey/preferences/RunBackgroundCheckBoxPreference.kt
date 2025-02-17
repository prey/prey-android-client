/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.preferences

import android.content.Context
import android.preference.CheckBoxPreference
import android.util.AttributeSet
import com.prey.PreyLogger

/**
 * A custom CheckBoxPreference that allows the user to enable or disable running in the background.
 */
class RunBackgroundCheckBoxPreference : CheckBoxPreference {

    /**
     * Constructor for creating a new instance of RunBackgroundCheckBoxPreference.
     *
     * @param context The Context the preference is running in.
     */
    constructor(context: Context?) : super(context)

    /**
     * Constructor for creating a new instance of RunBackgroundCheckBoxPreference with attributes.
     *
     * @param context The Context the preference is running in.
     * @param attrs The attributes of the XML tag that is inflating the preference.
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    /**
     * Constructor for creating a new instance of RunBackgroundCheckBoxPreference with attributes and default style.
     *
     * @param context The Context the preference is running in.
     * @param attrs The attributes of the XML tag that is inflating the preference.
     * @param defStyle The default style to apply to this preference.
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    /**
     * Sets the checked state of this preference.
     *
     * @param checked True to set the preference to checked, false to set it to unchecked.
     */
    override fun setChecked(checked: Boolean) {
        super.setChecked(checked)
        PreyLogger.d("RunBackgroundCheckBoxPreference:$checked")
    }
}