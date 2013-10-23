package com.prey.actions.alarm;

import android.content.Context;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;

public class AlarmThread extends Thread {

        private Context ctx;

        public AlarmThread(Context ctx) {
                this.ctx = ctx;
        }
        
        public void run() {
                PreyLogger.d("Ejecuting Alarm Action");
                MediaPlayer mp =null;
                boolean start=false;
                try {
                       	PreyStatus.getInstance().setAlarmStart();
                        final AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
                        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        final int setVolFlags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_VIBRATE;
                        PreyLogger.i("volumenInicial:"+max);
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);
                        
                        mp = MediaPlayer.create(ctx, R.raw.siren);
                        
                        mp.start();
                        Mp3OnCompletionListener mp3Listener=new Mp3OnCompletionListener();
                        mp.setOnCompletionListener(mp3Listener);
                      //  PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","started"));
                        start=true;                                
                        int i=0;
                        while(PreyStatus.getInstance().isAlarmStart()&& i<40 ){
                                sleep(1000);
                                i++;
                        }
                        mp.stop();        
                        PreyStatus.getInstance().setAlarmStop();
                } catch (Exception e) {
                        PreyLogger.i("Error executing Mp3PlayerAction " + e.getMessage());
                       // PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","failed",e.getMessage()));
                }finally{
                        if(mp!=null)
                                        mp.release();
                }
               /* if (start){
                        PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,UtilJson.makeMapParam("start","alarm","stopped"));
                }*/
                PreyLogger.i("Ejecuting Mp3PlayerAction Action[Finish]");
        }
        
        class Mp3OnCompletionListener implements MediaPlayer.OnCompletionListener{
                
                
                public void onCompletion(MediaPlayer mp) {
                        PreyLogger.d("Stop Playing MP3. Mp3PlayerAction Action. DONE!");
                        mp.stop();
                        PreyStatus.getInstance().setAlarmStop();
                }
        }
}