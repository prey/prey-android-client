package com.prey.preferences;

import com.prey.R;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TextPreference extends Preference {

	
	public TextPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public TextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		 setLayoutResource(R.layout.preference_text);
	}

 


    @Override
    public void onBindView(View view) {
        super.onBindView(view);
 
        TextView text=(TextView)view.findViewById(android.R.id.title);         
        Typeface large9 =	Typeface.createFromAsset(view.getContext().getAssets(), "fonts/large9.ttf");
        text.setTypeface(large9);
    }


	 

}
