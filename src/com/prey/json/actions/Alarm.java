package com.prey.json.actions;

 
import java.util.List;

 
import org.json.JSONObject;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;

 
import com.prey.actions.observer.ActionResult;
import com.prey.net.PreyWebServices;
 

public class Alarm {

 
	
	
	public void start(Context ctx,List<ActionResult> lista,JSONObject parameters){
		 
			/*
			String in=parameters.getString("in");
			String sound=parameters.getString("sound");
			String loops=parameters.getString("loops");
			PreyLogger.i("in:"+in+" sound:"+sound+" loops:"+loops);
			*/
			
			PreyLogger.d("Ejecuting Mp3PlayerAction Action");
			MediaPlayer mp =null;
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
				
				PreyWebServices.getInstance().sendEventsPreyHttpReport(ctx, "alarm_started", "true");
				int i=0; 
				while(PreyStatus.getInstance().isAlarmStart()&& i<40 ){
					Thread.sleep(1000);
					i++;
				}
				mp.stop();	
				PreyStatus.getInstance().setAlarmStop();
			} catch (Exception e) {
				PreyLogger.e("Error executing Mp3PlayerAction " + e.getMessage(),e);
			}finally{
				if(mp!=null)
						mp.release();
			}
			PreyWebServices.getInstance().sendEventsPreyHttpReport(ctx, "alarm_finished", "true");
			PreyLogger.d("Ejecuting Mp3PlayerAction Action[Finish]");
			
		 
	}
	

	public void stop(JSONObject parameters){
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
