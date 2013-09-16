package com.prey.twilio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.prey.R;
 

public class TwilioPhoneActivity  extends Activity implements View.OnClickListener
{
   
   // private EditText numberField;

    @Override
    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.twilio_phone);

         

        ImageButton dialButton = (ImageButton)findViewById(R.id.dialButton);
        dialButton.setOnClickListener(this);

        ImageButton hangupButton = (ImageButton)findViewById(R.id.hangupButton);
        hangupButton.setOnClickListener(this);

        //  numberField = (EditText)findViewById(R.id.numberField);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }
 
    @Override
    public void onResume()
    {
        super.onResume();
 
        Intent intent = getIntent();
        TwilioPhoneManager.getInstance(getApplicationContext()).handleIncomingConnection(intent);
         
    }
    
 
    public void onClick(View view)
    {
    	TwilioPhoneManager manager=TwilioPhoneManager.getInstance(getApplicationContext());
    	 if (view.getId() == R.id.dialButton){
    		 manager.connect();
    	 	// phone.connect(numberField.getText().toString());
    	 }else{ 
    		 if (view.getId() == R.id.hangupButton){
    			 manager.disconnect();
    		 }
    	 }
    }
}

 
