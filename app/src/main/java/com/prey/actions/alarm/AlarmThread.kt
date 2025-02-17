/*******************************************************************************
 * Created by Orlando Aliaga
 * Copyright 2025 Prey Inc. All rights reserved.
 * License: GPLv3
 * Full license at "/LICENSE"
 ******************************************************************************/
package com.prey.actions.alarm

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer

import com.prey.R
import com.prey.json.UtilJson
import com.prey.PreyLogger
import com.prey.PreyStatus
import com.prey.net.PreyWebServices

/**
 * A thread that plays an alarm sound and handles its lifecycle.
 *
 * @param context The application context.
 * @param soundType The type of sound to play (e.g. "alarm", "ring", "modem", etc.).
 * @param messageId The message ID associated with the alarm.
 * @param jobId The job ID associated with the alarm.
 */
class AlarmThread(
    private val context: Context,
    private val soundType: String,
    private val messageId: String?,
    private val jobId: String?
) :
    Thread() {

    /**
     * The main entry point of the thread.
     */
    override fun run() {
        // Log the start of the alarm
        PreyLogger.d("started alarm")
        // Initialize variables to track the alarm state
        var mediaPlayer: MediaPlayer? = null
        var alarmStarted = false
        var reason: String? = null
        // If a job ID is provided, create a reason string for logging
        if (jobId != null && jobId.isNotEmpty()) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        try {
            // Set the alarm start status
            PreyStatus.getInstance().setAlarmStart()
            // Get the audio manager and set the maximum volume
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumeFlags =
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_VIBRATE
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, volumeFlags)
            // Create a media player based on the sound type
            mediaPlayer = when (soundType) {
                "alarm" -> MediaPlayer.create(context, R.raw.alarm)
                "ring" -> MediaPlayer.create(context, R.raw.ring)
                "modem" -> MediaPlayer.create(context, R.raw.modem)
                else -> MediaPlayer.create(context, R.raw.siren)
            }
            // Start the media player
            mediaPlayer.start()
            // Set a completion listener to stop the alarm when the sound finishes
            mediaPlayer.setOnCompletionListener(Mp3OnCompletionListener())
            // Send a notification to the server that the alarm has started
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context, "processed",
                messageId, UtilJson.makeMapParam("start", "alarm", "started", reason)
            )
            // Mark the alarm as started
            alarmStarted = true
            // Loop for 80 iterations, checking the volume and stopping the alarm if necessary
            var i = 0
            while (PreyStatus.getInstance().isAlarmStart && i < 80) {
                sleep(500)
                val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVolume != maxVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, volumeFlags)
                }
                i++
            }
            // Stop the media player
            mediaPlayer.stop()
            // Set the alarm stop status
            PreyStatus.getInstance().setAlarmStop()
        } catch (e: Exception) {
            // Log any exceptions that occur during alarm playback
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context, "failed",
                messageId, UtilJson.makeMapParam("start", "alarm", "failed", e.message)
            )
        } finally {
            // Release the media player resources
            mediaPlayer?.release()
        }
        // If the alarm was started, send a notification to the server that it has stopped
        if (alarmStarted) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                context, "processed",
                messageId, UtilJson.makeMapParam("stop", "alarm", "stopped", reason)
            )
        }
        // Log the end of the alarm
        PreyLogger.d("stopped alarm")
    }

    /**
     * A completion listener that stops the alarm when the sound finishes.
     */
    internal inner class Mp3OnCompletionListener : MediaPlayer.OnCompletionListener {
        override fun onCompletion(mediaPlayer: MediaPlayer) {
            // Stop the media player
            mediaPlayer.stop()
            // Set the alarm stop status
            PreyStatus.getInstance().setAlarmStop()
        }
    }
}