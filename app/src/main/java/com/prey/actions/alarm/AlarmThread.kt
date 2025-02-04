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
import android.media.MediaPlayer.OnCompletionListener
import com.prey.R
import com.prey.json.UtilJson
import com.prey.PreyLogger
import com.prey.PreyStatus
import com.prey.net.PreyWebServices


class AlarmThread(
    private val ctx: Context,
    private val sound: String,
    private val messageId: String?,
    private val jobId: String?
) :
    Thread() {
    override fun run() {
        PreyLogger.d("started alarm")
        var mp: MediaPlayer? = null
        var start = false
        var reason: String? = null
        if (jobId != null && "" != jobId) {
            reason = "{\"device_job_id\":\"$jobId\"}"
        }
        try {
            PreyStatus.getInstance().setAlarmStart()
            val audio = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val setVolFlags =
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE or AudioManager.FLAG_VIBRATE
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags)
            if ("alarm" == sound) mp = MediaPlayer.create(ctx, R.raw.alarm)
            else if ("ring" == sound) mp = MediaPlayer.create(ctx, R.raw.ring)
            else if ("modem" == sound) mp = MediaPlayer.create(ctx, R.raw.modem)
            else mp = MediaPlayer.create(ctx, R.raw.siren)
            mp!!.start()
            val mp3Listener: Mp3OnCompletionListener = Mp3OnCompletionListener()
            mp.setOnCompletionListener(mp3Listener)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx, "processed",
                messageId, UtilJson.makeMapParam("start", "alarm", "started", reason)
            )
            start = true
            var i = 0
            while (PreyStatus.getInstance().isAlarmStart && i < 80) {
                sleep(500)
                val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVolume != max) {
                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, max, setVolFlags)
                }
                i++
            }
            mp.stop()
            PreyStatus.getInstance().setAlarmStop()
        } catch (e: Exception) {
            PreyLogger.e("failed alarm: " + e.message, e)
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx, "failed",
                messageId, UtilJson.makeMapParam("start", "alarm", "failed", e.message)
            )
        } finally {
            mp?.release()
        }
        if (start) {
            PreyWebServices.getInstance().sendNotifyActionResultPreyHttp(
                ctx, "processed",
                messageId, UtilJson.makeMapParam("stop", "alarm", "stopped", reason)
            )
        }
        PreyLogger.d("stopped alarm")
    }

    internal inner class Mp3OnCompletionListener : OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer) {
            mp.stop()
            PreyLogger.d("stop alarm")
            PreyStatus.getInstance().setAlarmStop()
        }
    }
}