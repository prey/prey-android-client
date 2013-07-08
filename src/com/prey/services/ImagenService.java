package com.prey.services;



import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class ImagenService  extends Service{

	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	public class LocalBinder extends Binder {
		ImagenService getService() {
			return ImagenService.this;
		}
	}

	public void onCreate() {
 
	}
	
	public void onDestroy() {
		
	}
}
