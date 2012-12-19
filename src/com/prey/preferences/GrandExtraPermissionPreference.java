package com.prey.preferences;

 
import com.prey.PreyLogger;
 
 

 
 
 
import android.content.Context;
 
 
import android.preference.CheckBoxPreference;

import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
 
 
 

public class GrandExtraPermissionPreference extends CheckBoxPreference{

	public GrandExtraPermissionPreference(Context context) {
        super(context);
    }

    public GrandExtraPermissionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public GrandExtraPermissionPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        
       
        
        
        	
     
        	 
        	 
        	 
        	
        
        
     
     this.setOnPreferenceClickListener(new OnPreferenceClickListener() {

    	    public boolean onPreferenceClick(Preference preference) {
    	    	PreyLogger.d("isSelectable:"+preference.isSelectable());
    	    	
    	    	/*  AlertDialog alertDialog =new AlertDialog.Builder().create();
                  alertDialog.setTitle("Titel");
                  	alertDialog.setMessage("Do you really want to whatever?");
                  	alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                  	alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                  		   public void onClick(DialogInterface dialog, int which) {
                  		      // do something when the user presses OK (place focus on weight input?)
                  		   }
                  		});
                  	alertDialog.show();*/
    	    	return true;
    	    }
     });
    }
    
    

    
    
 
        
    
 
 
    
 
    
}
