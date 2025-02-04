package com.prey.actions.picture


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.prey.actions.camera.CameraAction
import com.prey.actions.HttpDataService
import com.prey.activities.CheckPasswordHtmlActivity
import com.prey.activities.SimpleCameraActivity
import com.prey.exceptions.PreyFirebaseCrashlytics
import com.prey.PreyConfig
import com.prey.PreyLogger
import com.prey.net.http.EntityFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date


class PictureUtil private constructor(context: Context) {

    var dataImagen: ByteArray? = null

    var activity: SimpleCameraActivity? = null

    /**
     * Method obtains the images of the cameras with retry of 4 times per camera
     *
     * @return pictures
     */
    fun getPicture(ctx: Context): HttpDataService? {
        var data: HttpDataService?  = null
        data = HttpDataService(CameraAction.DATA_ID)
        data.setList(true)
        var currentVolume = 0
        var mgr: AudioManager? = null
        try {
            val sdf = SimpleDateFormat("yyyyMMddHHmmZ")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                var attempts = 0
                val maximum = 4
                mgr = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                //get current volume
                currentVolume = mgr!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                PreyConfig.getInstance(ctx).setVolume (currentVolume)


                do {
                    try {
                        getInstance(ctx).dataImagen = null
                        PreyLogger.d("report front attempts FRONT:$attempts")
                        val frontPicture = getPicture(ctx, BACK)
                        if (frontPicture != null) {
                            PreyLogger.d("report data length front=" + frontPicture.size)
                            val file: InputStream = ByteArrayInputStream(frontPicture)
                            val entityFile = EntityFile()
                            entityFile.setFile(file)
                            entityFile.setMimeType("image/png")
                            entityFile.setFilename("picture.jpg")
                            entityFile.setName("picture")
                            entityFile.setType("image/png")
                            entityFile.setIdFile(sdf.format(Date()) + "_" + entityFile.getType())
                            entityFile.setLength(frontPicture.size)
                            data.addEntityFile(entityFile)
                            attempts = maximum
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("report error:" + e.message, e)
                        PreyFirebaseCrashlytics.getInstance(ctx).recordException(e)
                    }
                    attempts++
                } while (attempts < maximum)
                val numberOfCameras = getNumberOfCameras(ctx)
                if (numberOfCameras != null && numberOfCameras > 1) {
                    attempts = 0
                    do {
                        try {
                            getInstance(ctx).dataImagen = null
                            PreyLogger.d("report back attempts BACK:$attempts")
                            val backPicture = getPicture(ctx, FRONT)
                            if (backPicture != null) {
                                PreyLogger.d("report data length back=" + backPicture.size)
                                val file: InputStream = ByteArrayInputStream(backPicture)
                                val entityFile = EntityFile()
                                entityFile.setFile(file)
                                entityFile.setMimeType("image/png")
                                entityFile.setFilename("screenshot.jpg")
                                entityFile.setName("screenshot")
                                entityFile.setType("image/png")
                                entityFile.setIdFile(sdf.format(Date()) + "_" + entityFile.getType())
                                entityFile.setLength(backPicture.size)
                                data.addEntityFile(entityFile)
                                attempts = maximum
                            }
                        } catch (e: Exception) {
                            PreyLogger.e("report error:$attempts", e)
                            PreyFirebaseCrashlytics.getInstance(ctx).recordException(e)
                        }
                        attempts++
                    } while (attempts < maximum)
                }
            }
            PreyLogger.d("report data files size:${data.getEntityFiles().size}")
            val intentCamera = Intent(ctx, SimpleCameraActivity::class.java)
            intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val myKillerBundle = Bundle()
            myKillerBundle.putInt("kill", 1)
            intentCamera.putExtras(myKillerBundle)
            ctx.startActivity(intentCamera)
            ctx.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
        } catch (e: Exception) {
            PreyLogger.e("report error:" + e.message, e)
            PreyFirebaseCrashlytics.getInstance(ctx).recordException(e)
        } finally {
            try {
                currentVolume = PreyConfig.getInstance(ctx).getVolume()
                if (currentVolume > 0) {
                    //set old volume
                    mgr!!.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        currentVolume,
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            } catch (e: Exception) {
                PreyLogger.e("report error:" + e.message, e)
                PreyFirebaseCrashlytics.getInstance(ctx).recordException(e)
            }
        }
        return data
    }

    /**
     * Method gets the image from a camera, in the process it gets the initial volume, it is muted and at the end it returns to the initial volume
     *
     * @return byte array
     */
    private fun getPicture(ctx: Context, focus: String): ByteArray? {
        var mgr: AudioManager? = null
        getInstance(ctx).dataImagen = null
        val streamType = AudioManager.STREAM_SYSTEM
        getInstance(ctx).activity = null
        val intentCamera = Intent(ctx, SimpleCameraActivity::class.java)
        intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intentCamera.putExtra("focus", focus)
        ctx.startActivity(intentCamera)
        var i = 0
        mgr = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        //get current volume
        var currentVolume = mgr!!.getStreamVolume(AudioManager.STREAM_MUSIC)
        try {
            if (currentVolume > 0) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    mgr!!.ringerMode = AudioManager.RINGER_MODE_SILENT
                    mgr!!.setStreamMute(streamType, true)
                } else {
                    val setVolFlags = AudioManager.FLAG_PLAY_SOUND
                    mgr!!.setStreamVolume(AudioManager.STREAM_MUSIC, 0, setVolFlags)
                }
            }
        } catch (e: Exception) {
            PreyLogger.e("report error:" + e.message, e)
        }
        while ( getInstance(ctx).activity == null && i < 10) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                PreyLogger.e("report error:" + e.message, e)
            }
            i++
        }
        if ( getInstance(ctx).activity != null) {
            getInstance(ctx).activity!!.takePicture(ctx)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mgr!!.ringerMode = AudioManager.RINGER_MODE_NORMAL
            mgr!!.setStreamMute(streamType, false)
        }
        try {
            i = 0
            while ( getInstance(ctx).activity != null &&  getInstance(ctx).dataImagen == null && i < 9) {
                Thread.sleep(500)
                i++
            }
        } catch (e: InterruptedException) {
            PreyLogger.e("report error:" + e.message, e)
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            PreyLogger.e("report error:" + e.message, e)
        }
        var out: ByteArray? = null
        if ( getInstance(ctx).activity != null) {
            out =  getInstance(ctx).dataImagen
            getInstance(ctx).activity!!.finish()
            getInstance(ctx).activity = null
            getInstance(ctx).dataImagen = null
        }
        try {
            currentVolume = PreyConfig.getInstance(ctx).getVolume()
            if (currentVolume > 0) {
                //set old volume
                mgr!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
        } catch (e: Exception) {
            PreyLogger.e("report error:" + e.message, e)
        }
        return out
    }

    companion object {
        var TAG: String = "memory"

        private var instance: PictureUtil? = null

        @Synchronized
        fun getInstance(context: Context): PictureUtil {
            if (instance == null) {
                instance = PictureUtil(context)
            }
            return instance!!
        }

        var FRONT: String = "front"

        var BACK: String = "back"
    }

    /**
     * Method obtains camera numbers
     *
     * @return camera numbers
     */
    fun getNumberOfCameras(ctx: Context): Int {
        return Camera.getNumberOfCameras()
    }
}