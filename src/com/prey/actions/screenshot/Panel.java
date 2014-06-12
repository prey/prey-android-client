package com.prey.actions.screenshot;

 
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {

    public static float mWidth;
    public static float mHeight;
    
	public Panel(Context context) {
	        super(context);
	}
	  
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	        mWidth = width;
	        mHeight = height;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
 
	
	 public void saveScreenshot() {
	        
		 int width= getWidth();
		 int height=getHeight();
		 if (width==0){
			 width=400;
		 }
		 if (height==0){
			 height=400;
		 }
		 
	            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	           // Canvas canvas = new Canvas(bitmap);
	          // doDraw(1, canvas);
	            
	          
	            FileOutputStream fos=null;;
	            try {
	             

	    			SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
	    			String time = sdf.format(new Date());
	    			String strFileName = "sdcard/osito" + time + ".png";
	    			fos = new FileOutputStream(strFileName);
	    			if (null != fos) {
	    				 
	    				 bitmap.compress(CompressFormat.JPEG, 100, fos);
	    				 
	    				fos.flush();
	    				fos.close();
	    			}
	                
	                
	                
	 
	                
	            } catch (FileNotFoundException e) {
	                Log.e("Panel", "FileNotFoundException", e);
	            } catch (IOException e) {
	                Log.e("Panel", "IOEception", e);
	            }
	        
	    }

}
