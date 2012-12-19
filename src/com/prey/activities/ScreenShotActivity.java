package com.prey.activities;

 
import java.io.FileOutputStream;
 
import java.text.SimpleDateFormat;
import java.util.Date;

 
import com.prey.PreyLogger;
import com.prey.R;
 

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
 
 
import android.view.Menu;
 
 

public class ScreenShotActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
		//setContentView(R.layout.activity_screen_shot); 
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
			String time = sdf.format(new Date());
			String strFileName = "sdcard/osito" + time + ".png";
			
			Bitmap bitmap = com.prey.actions.screenshot.ScreenShot.getScreenBitmap(this);
			/*
    		FileOutputStream out = new FileOutputStream(strFileName);
    		bt.compress(Bitmap.CompressFormat.PNG, 100, out);
    		out.flush();
    		out.close();
    		
			// setContentView(R.layout.activity_screen_shot);
			/*requestWindowFeature(Window.FEATURE_NO_TITLE);
			com.prey.actions.screenshot.Panel mPanel = new com.prey.actions.screenshot.Panel(this);
			 setContentView(mPanel);
			
			mPanel.saveScreenshot();
			
			 
			 View view = (View) this.findViewById(android.R.id.content);
			 
			 View v = view.getRootView();
			 int width=v.getWidth();
			 int height=v.getHeight();
			 if(width==0){
				 width=500;
			 }
			 if(height==0){
				 height=500;
			 }
			 //View view = (View)findViewById(R.id.screen_shot_layout);
			
			 
			   Bitmap bitmap2 = Bitmap.createBitmap(width,height, Config.ARGB_8888);
              Canvas canvas = new Canvas(bitmap2);
              v.draw(canvas);
              
			 /*
			 View v =   findViewById(R.id.screen_shot_layout);
			  
			 View view=v.getRootView(); 
			view.setDrawingCacheEnabled(true);
			//view.buildDrawingCache();
			 v.setDrawingCacheEnabled(true);
			 
			 
			 
			 int width = v.getWidth();
			 int height = v.getHeight();
			  
			   Bitmap screenshot = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
			  v.draw(new Canvas(screenshot));
			 
			 
			Bitmap bitmap2=  v.getDrawingCache() ;
			Bitmap bitmap=  view.getDrawingCache() ;
			v.setDrawingCacheEnabled(false);
			view.setDrawingCacheEnabled(false);
 */
			FileOutputStream fos = null;

	
			fos = new FileOutputStream(strFileName);
			if (null != fos) {

				bitmap.compress(CompressFormat.JPEG, 100, fos);

				fos.flush();
				fos.close();
			}
			 
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_screen_shot, menu);
		return true;
	}
}
