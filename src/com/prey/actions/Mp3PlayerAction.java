package com.prey.actions;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.prey.PreyConfig;
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
		PreyLogger.d("Ejecuting Mp3PlayerAction Action");

		try {
			// AudioManager.setVolume(AudioManager.STREAM_MUSIC,
			// AudioManager.MAX_VOLUME - step);
			final AudioManager audio = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
			int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			final int setVolFlags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_VIBRATE;

			audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags);

			MediaPlayer mp = MediaPlayer.create(ctx, R.raw.siren);
			// mp.prepare();

			mp.start();
			// i.e. react on the end of the music-file:
			mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

				public void onCompletion(MediaPlayer mp) {
					PreyLogger.d("Stop Playing MP3. Mp3PlayerAction Action. DONE!");
					mp.release();
				}
			});
		} catch (IllegalStateException e) {
			PreyLogger.e("Error executing Mp3PlayerAction " + e.getMessage(),e);

			// } catch (IOException e) {
			// Log.e("Error executing Mp3PlayerAction" +
			// e.getLocalizedMessage());
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

}
