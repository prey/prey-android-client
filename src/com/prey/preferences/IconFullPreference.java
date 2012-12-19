package com.prey.preferences;

import com.prey.R;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class IconFullPreference extends Preference {

    public IconFullPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconFullPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_icon_full);
    }
  
}
