/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alarm;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.prey.PreyConfig;
import com.prey.PreyLogger;
import com.prey.PreyStatus;
import com.prey.R;
import com.prey.json.UtilJson;
import com.prey.net.PreyWebServices;

public class AlarmThread extends Thread {

    private Context ctx;
    private String sound;
    private String messageId;
    private String jobId;

    public AlarmThread(Context ctx, String sound,String messageId,String jobId) {
        this.ctx = ctx;
        this.sound = sound;
        this.messageId=messageId;
        this.jobId=jobId;
    }

    public void run() {
        PreyLogger.d("started alarm");
        MediaPlayer mp = null;
        boolean start = false;
        String reason=null;
        if(jobId!=null&&!"".equals(jobId)){
            reason="{\"device_job_id\":\""+jobId+"\"}";
        }
        try {
            PreyStatus.getInstance().setAlarmStart();
            final AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            final int setVolFlags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_VIBRATE;
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);

            if ("alarm".equals(sound))
                mp = MediaPlayer.create(ctx, R.raw.alarm);
            else if ("ring".equals(sound))
                mp = MediaPlayer.create(ctx, R.raw.ring);
            else if ("modem".equals(sound))
                mp = MediaPlayer.create(ctx, R.raw.modem);
            else
                mp = MediaPlayer.create(ctx, R.raw.siren);

            mp.start();
            Mp3OnCompletionListener mp3Listener = new Mp3OnCompletionListener();
            mp.setOnCompletionListener(mp3Listener);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("start", "alarm", "started",reason));
            start = true;
            int i = 0;
            while (PreyStatus.getInstance().isAlarmStart() && i < 80) {
                sleep(500);
                int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                if(currentVolume!=max){
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);
                }
                i++;
            }
            mp.stop();
            PreyStatus.getInstance().setAlarmStop();
            PreyConfig.getPreyConfig(ctx).setLastEvent("alarm_finished");
        } catch (Exception e) {
            PreyLogger.e("failed alarm: " + e.getMessage(),e);
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"failed", messageId, UtilJson.makeMapParam("start", "alarm", "failed", e.getMessage()));
        } finally {
            if (mp != null)
                mp.release();
        }
        if (start) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(ctx,"processed", messageId, UtilJson.makeMapParam("stop", "alarm", "stopped",reason));
        }
        PreyLogger.d("stopped alarm");
    }

    class Mp3OnCompletionListener implements MediaPlayer.OnCompletionListener {

        public void onCompletion(MediaPlayer mp) {
            mp.stop();
            PreyLogger.d("stop alarm");
            PreyStatus.getInstance().setAlarmStop();
        }
    }
}
