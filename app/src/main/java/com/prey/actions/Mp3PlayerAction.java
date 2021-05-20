/*******************************************************************************
 * Created by Carlos Yaconi
 * Copyright 2015 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions;

import android.content.Context;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.prey.PreyLogger;
import com.prey.R;
import com.prey.actions.observer.ActionJob;

public class Mp3PlayerAction extends PreyAction {

    public static final String DATA_ID = "alarm";
    public final String ID = "alarm";

    @Override
    public String textToNotifyUserOnEachReport(Context ctx) {
        return "";
    }

    @Override
    public void execute(ActionJob actionJob, Context ctx) {
        MediaPlayer mp =null;
        try {
            final AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
            int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            final int setVolFlags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_VIBRATE;
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);
            mp = MediaPlayer.create(ctx, R.raw.siren);
            mp.start();
            Mp3OnCompletionListener mp3Listener=new Mp3OnCompletionListener();
            // i.e. react on the end of the music-file:
            mp.setOnCompletionListener(mp3Listener);
            Thread.sleep(30000);
        } catch (Exception e) {
            PreyLogger.e("Error" + e.getMessage(),e);
        }finally{
            if(mp!=null)
                mp.stop();
        }
    }

    @Override
    public boolean isSyncAction() {
        return false;
    }

    @Override
    public boolean shouldNotify() {
        return false;
    }

    public int getPriority(){
        return ALARM_PRIORITY;
    }

    class Mp3OnCompletionListener implements MediaPlayer.OnCompletionListener{
        public void onCompletion(MediaPlayer mp) {
            mp.release();
        }
    }

}