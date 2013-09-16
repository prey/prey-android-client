/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2013 Fork Ltd. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions;

 
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.actions.HttpDataService;
import com.prey.actions.observer.ActionResult;
import com.prey.json.JsonAction;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;
 

public class Alarm extends JsonAction{


	public HttpDataService run(Context ctx, List<ActionResult> lista, JSONObject parameters){
		return null;
	}

	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		 
			PreyLogger.d("Ejecuting Alarm Action");
			MediaPlayer mp =null;
			boolean start=false;
			try {
				PreyStatus.getInstance().setAlarmStart();
				final AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
				int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				final int setVolFlags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_VIBRATE;
				PreyLogger.d("volumenInicial:"+max);
				audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);
				
				mp = MediaPlayer.create(ctx, R.raw.siren);
				
    			mp.start();
				Mp3OnCompletionListener mp3Listener=new Mp3OnCompletionListener();
				mp.setOnCompletionListener(mp3Listener);
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","started"));
				start=true;				
				int i=0; 
				while(PreyStatus.getInstance().isAlarmStart()&& i<40 ){
					Thread.sleep(1000);
					i++;
				}
				mp.stop();	
				PreyStatus.getInstance().setAlarmStop();
			} catch (Exception e) {
				PreyLogger.e("Error executing Mp3PlayerAction " + e.getMessage(),e);
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","failed",e.getMessage()));
			}finally{
				if(mp!=null)
						mp.release();
			}
			if (start){
				PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","stopped"));
			}
			PreyLogger.d("Ejecuting Mp3PlayerAction Action[Finish]");
	}
	

	public void stop(Context ctx, List<ActionResult> lista, JSONObject options) {
		PreyStatus.getInstance().setAlarmStop();
	}
	
	class Mp3OnCompletionListener implements MediaPlayer.OnCompletionListener{
		 
 
		public void onCompletion(MediaPlayer mp) {
			PreyLogger.d("Stop Playing MP3. Mp3PlayerAction Action. DONE!");
			mp.stop();
			PreyStatus.getInstance().setAlarmStop();
		}
	}
}
