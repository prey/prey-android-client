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
import com.prey.json.UtilJson
import com.prey.net.PreyWebServicesKt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * `CommandTarget` implementation for handling the "alarm" action.
 * This class is responsible for playing a loud sound on the device when triggered by a remote command.
 * It manages the entire lifecycle of the alarm, from starting the sound to ensuring the volume is at maximum,
 * and finally stopping it after a certain duration or when the sound playback completes.
 * It also notifies the backend service about the start and stop events of the alarm.
 */
class Alarm : CommandTarget {

    override fun execute(context: Context, command: String, options: JSONObject): Any? {
        return when (command) {
            "start" -> start(context, options)
            else -> throw IllegalArgumentException("Unknown command: $command")
        }
    }

    /**
     * Starts the alarm sound on the device.
     *
     * This function handles the entire lifecycle of the alarm action. It performs the following steps:
     * 1.  Parses `messageId` and `jobId` from the incoming `options` to notify the backend that the alarm has started.
     * 2.  Sets the device's media volume to its maximum level.
     * 3.  Determines which sound to play based on the "sound" key in `options`. It defaults to "siren" if not specified.
     * 4.  Plays the selected sound (`siren`, `alarm`, `ring`, or `modem`).
     * 5.  The sound plays for approximately 40 seconds (80 loops of 500ms sleep) or until the alarm status is manually stopped.
     * 6.  During this time, it periodically checks and resets the volume to maximum if it has been changed.
     * 7.  After the alarm stops (either by timeout or completion), it notifies the backend that the alarm has stopped.
     * 8.  Releases the `MediaPlayer` resources to prevent memory leaks.
     *
     * @param context The application context, used for accessing system services like `AudioManager` and resources.
     * @param options A `JSONObject` containing command parameters. Expected keys include `message_id`, `job_id`, and optionally `sound`.
     */
    fun start(context: Context, options: JSONObject) {
        PreyLogger.d("Alarm start options:${options}")
        try {
            var messageId: String? = null
            try {
                messageId = options.getString(PreyConfig.MESSAGE_ID)
                PreyLogger.d("messageId:${messageId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var reason: String? = null
            try {
                val jobId = options.getString(PreyConfig.JOB_ID)
                reason = "{\"device_job_id\":\"${jobId}\"}"
                PreyLogger.d("jobId:${jobId}")
            } catch (e: java.lang.Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            var mp: MediaPlayer? = null
            var sound = "siren"
            try {
                sound = options.getString("sound")
            } catch (e: Exception) {
                PreyLogger.e("Error:${e.message}", e)
            }
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    PreyWebServicesKt.sendNotifyActions(
                        context,
                        UtilJson.makeJsonResponse("start", "alarm", "started", reason),
                        messageId
                    )
                }
                PreyStatus.getInstance().setAlarmStart()
                val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val setVolFlags =
                    AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_VIBRATE
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags)
                if ("alarm" == sound) mp = MediaPlayer.create(context, R.raw.alarm)
                else if ("ring" == sound) mp = MediaPlayer.create(context, R.raw.ring)
                else if ("modem" == sound) mp = MediaPlayer.create(context, R.raw.modem)
                else mp = MediaPlayer.create(context, R.raw.siren)
                mp!!.start()
                val mp3Listener = Mp3OnCompletionListener()
                mp.setOnCompletionListener(mp3Listener)
                var i = 0
                while (PreyStatus.getInstance().isAlarmStart() && i < 80) {
                    Thread.sleep(500)
                    val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (currentVolume != max) {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags)
                    }
                    i++
                }
                mp.stop()
                PreyStatus.getInstance().setAlarmStop()
                PreyConfig.getPreyConfig(context).setLastEvent("alarm_finished")
                CoroutineScope(Dispatchers.IO).launch {
                    PreyWebServicesKt.sendNotifyActions(
                        context,
                        UtilJson.makeJsonResponse("start", "alarm", "stopped")
                    )
                }
            } catch (e: java.lang.Exception) {
                PreyLogger.e("failed alarm:${e.message}", e)
            } finally {
                mp?.release()
            }
        } catch (e: Exception) {
            PreyLogger.e("failed alarm:${e.message}", e)
        }
    }

    class Mp3OnCompletionListener : OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer) {
            mp.stop()
            PreyLogger.d("stop alarm")
            PreyStatus.getInstance().setAlarmStop()
        }
    }
}