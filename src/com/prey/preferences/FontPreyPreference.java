package com.prey.preferences;

import com.prey.R;

import android.content.Context;
 

import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
 

public class FontPreyPreference extends Preference {

	 
	   
 

    public FontPreyPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FontPreyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutResource(R.layout.preference_title);
        
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
 
    }

 
}
