package com.prey.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


 
import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.screenshot.ColorPanelView;

 
 

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
 
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
 
import android.os.Bundle;
 
import android.view.View;
 
import android.view.View.MeasureSpec;
import android.widget.RelativeLayout;
 

public class ScreenshotsActivity extends PreyActivity {

 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		 /*
		try {
			
			// setContentView(R.layout.screenshot);
			 
			// View view = findViewById(android.R.id.background);
			 
			  
			  RelativeLayout  re=(RelativeLayout)this.findViewById(R.id.relEmpty);
			 
			 View view = this.
			//   View view = contentView.getRootView();
			    		
			//  Activity activity= ScreenshotsActivity.this;
			 
			 
			 //View view2  =this.getWindow().getDecorView();
			// View view =new ColorPanelView(getApplicationContext());
 
		        
		   /*     view.setDrawingCacheEnabled(true);
		        

		     // this is the important code :)  
		     // Without it the view will have a dimension of 0,0 and the bitmap will be null     
		       
		        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
		                 MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()); 
 
		        view.buildDrawingCache(true);
		      
		        
		        Bitmap b1 = view.getDrawingCache();
		       /* Rect frame = new Rect();
		        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		        int statusBarHeight = frame.top;
		        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
		        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
 
		        //Bitmap bitmap2 = Bitmap.createBitmap(b1);
		        
		        Bitmap bitmap2 = Bitmap.createBitmap(480, 800, Config.ARGB_8888);
		        
		        Canvas localCanvas = new Canvas(bitmap2);
		        Rect localRect = new Rect(0, 0, 480, 800);
		        Paint localPaint = new Paint();
		        localPaint.setFilterBitmap(true);
		        localCanvas.drawBitmap(b1, 0,480, localPaint);
		        
		        Canvas canvas = new Canvas(bitmap2);
		        view.draw(canvas);
		    
		        Bitmap.Config pf = Config.ARGB_8888;
		        Bitmap bmp = Bitmap.createBitmap(480, 800, pf);
		        Matrix matrix = new Matrix();
		        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

			FileOutputStream fos = null;

			SimpleDateFormat sdf = new SimpleDateFormat("hhmmss");
			String time = sdf.format(new Date());
			String strFileName = "sdcard/osito" + time + ".jpg";
			fos = new FileOutputStream(strFileName);
			if (null != fos) {
				 
				 bitmap2.compress(CompressFormat.JPEG, 100, fos);
				 
				fos.flush();
				fos.close();
			}
			
			
			 
			 
			String strFileName2 = "sdcard/osito_" + time + ".jpg";
			fos = new FileOutputStream(strFileName2);
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
		 */
	}
}
