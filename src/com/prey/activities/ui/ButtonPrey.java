package com.prey.activities.ui;

import com.prey.PreyLogger;
import com.prey.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;


public class ButtonPrey extends Button {

	public ButtonPrey(Context context) {
		super(context);
	}

	public ButtonPrey(Context context, AttributeSet attrs) {
		super(context, attrs);
		setCustomFont(context, attrs);
	}

	public ButtonPrey(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setCustomFont(context, attrs);
	}
	
	private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.ButtonPrey);
        String customFont = a.getString(R.styleable.ButtonPrey_customFont);
        if (customFont != null){
        	setCustomFont(ctx, customFont);
        }
        a.recycle();
        
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface tf = null;
        try {
        tf = Typeface.createFromAsset(ctx.getAssets(), asset);  
        } catch (Exception e) {
            PreyLogger.e("Could not get typeface: "+e.getMessage(), e);
            return false;
        }

        setTypeface(tf);  
        return true;
    }


}
