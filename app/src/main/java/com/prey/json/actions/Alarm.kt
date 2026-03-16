/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2026 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.json.actions

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.PreyStatus
import com.prey.R
import com.prey.json.CommandTarget
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * `CommandTarget` implementation for handling the "alarm" action.
 * This class is responsible for playing a loud sound on the device when triggered by a remote command.
 * It manages the entire lifecycle of the alarm, from starting the sound to ensuring the volume is at maximum,
 * and finally stopping it after a certain duration or when the sound playback completes.
 * It also notifies the backend service about the start and stop events of the alarm.
 */
class Alarm : CommandTarget, BaseAction() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    companion object {
        private const val TARGET = "alarm"
    }

    override fun execute(context: Context, command: String, options: JSONObject) {
        when (command) {
            CMD_START -> scope.launch { start(context, options) }
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    private suspend fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Alarm start options: $options")
        val messageId = options.optString(PreyConfig.MESSAGE_ID, null)
        val jobId = options.optString(PreyConfig.JOB_ID, null)
        val reason = jobId?.let { "{\"device_job_id\":\"$it\"}" }
        val soundName = options.optString("sound", "siren")
        var mediaPlayer: MediaPlayer? = null
        try {
            // Notify start asynchronously
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STARTED, reason, messageId)
            PreyStatus.getInstance().setAlarmStart()
            // Configure Audio
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            forceMaxVolume(audioManager, maxVolume)
            //Prepare MediaPlayer
            val resId = getSoundResource(soundName)
            mediaPlayer = MediaPlayer.create(context, resId).apply {
                isLooping = false // Lo controlamos con el loop de tiempo
                setOnCompletionListener {
                    PreyLogger.d("Sound playback completed naturally")
                }
                start()
            }
            //Control loop (Maximum 30 seconds)
            //We use delay(500) instead of Thread.sleep to avoid blocking the thread
            repeat(60) {
                if (!PreyStatus.getInstance().isAlarmStart) return@repeat
                forceMaxVolume(audioManager, maxVolume)
                delay(500)
            }
        } catch (e: Exception) {
            PreyLogger.e("Error during alarm execution: ${e.message}", e)
        } finally {
            //Cleanup and End Notification
            stopAndRelease(mediaPlayer)
            PreyStatus.getInstance().setAlarmStop()
            PreyConfig.getPreyConfig(context).lastEvent = "alarm_finished"
            PreyWebServicesKt.notify(context, CMD_START, TARGET, STATUS_STOPPED)
        }
    }

    /**
     * Maps a sound name string to its corresponding Android raw resource ID.
     *
     * @param soundName The name of the sound requested (e.g., "alarm", "ring", "modem").
     * @return The resource ID of the audio file. Defaults to [R.raw.siren] if the name is unrecognized.
     */
    private fun getSoundResource(soundName: String): Int = when (soundName) {
        "alarm" -> R.raw.alarm
        "ring" -> R.raw.ring
        "modem" -> R.raw.modem
        else -> R.raw.siren
    }

    /**
     * Ensures the device's music stream volume is set to the specified maximum level.
     *
     * This method checks the current volume of [AudioManager.STREAM_MUSIC] and, if it
     * differs from the provided [max] value, updates it using flags to manage
     * sound and vibration feedback.
     *
     * @param audio The [AudioManager] instance used to interact with system audio settings.
     * @param max The target volume level to be enforced.
     */
    private fun forceMaxVolume(audio: AudioManager, max: Int) {
        val flags = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_VIBRATE
        if (audio.getStreamVolume(AudioManager.STREAM_MUSIC) != max) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, flags)
        }
    }

    /**
     * Stops and releases the [MediaPlayer] resources.
     *
     * This method ensures that if the media player is currently playing, it is stopped
     * before being released. It handles null values safely and catches any exceptions
     * that might occur during the release process to avoid application crashes.
     *
     * @param mp The [MediaPlayer] instance to be stopped and released, or null.
     */
    private fun stopAndRelease(mp: MediaPlayer?) {
        try {
            mp?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            PreyLogger.e("Error releasing MediaPlayer: ${e.message}", e)
        }
    }

    /**
     * Implementation of [OnCompletionListener] that handles the end of the alarm sound
     */
    class Mp3OnCompletionListener : OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer) {
            mp.stop()
            PreyLogger.d("stop alarm")
            PreyStatus.getInstance().setAlarmStop()
        }
    }

}