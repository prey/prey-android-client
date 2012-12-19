package com.prey.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.prey.PreyLogger;
import com.prey.R;

 
 
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
 
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

public class ScreenshotsActivity extends PreyActivity {

 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 
		try {
			
			 setContentView(R.layout.screenshot);
			 RelativeLayout view = (RelativeLayout)findViewById(R.id.screenRoot);
			    
			//LayoutInflater li = LayoutInflater.from(this);
			//View view = li.inflate(R.layout.screenshot, null);
		 
			  View v = view.getRootView();
			   Bitmap bitmap2 = Bitmap.createBitmap(400,400, Config.ARGB_8888);
               Canvas canvas = new Canvas(bitmap2);
               v.draw(canvas);

               
			 /*
			  
			    v.layout(0, 0, 200, 200); 
			    
			    v.setDrawingCacheEnabled(true);
			    //Bitmap bitmap = v.getDrawingCache(); 
			    Bitmap bitmap = Bitmap.createBitmap( 200, 200, Bitmap.Config.ARGB_8888);                
			     Canvas c = new Canvas(bitmap);
			     v.layout(0, 0, 200, 200);
			     v.draw(c);
			 */
			 
		    
		  //  view.setDrawingCacheEnabled(true); 
		 //   view.buildDrawingCache();
		  //  bitmap = view.getDrawingCache();
		    
		   // bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		   // Canvas canvas = new Canvas(bitmap);
		    
		//    view.setDrawingCacheEnabled(false); 
		    
		     
		   
		    

			FileOutputStream fos = null;

			SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
			String time = sdf.format(new Date());
			String strFileName = "sdcard/osito" + time + ".png";
			fos = new FileOutputStream(strFileName);
			if (null != fos) {
				 
				 bitmap2.compress(CompressFormat.JPEG, 100, fos);
				 
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		} catch (IOException e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		} catch (Exception e) {
			PreyLogger.e("Error, causa:" + e.getMessage(), e);
		}
		 
	}
}
