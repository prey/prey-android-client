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

/**
 * A utility class for handling picture-related operations.
 */
class PictureUtil private constructor(var context: Context) {

    var dataImagen: ByteArray? = null
    var activity: SimpleCameraActivity? = null

    /**
     * Obtains the images of the cameras with retry of 4 times per camera.
     *
     * @return an HttpDataService object containing the pictures.
     */
    fun getPicture(): HttpDataService? {
        var data: HttpDataService? = null
        data = HttpDataService(DATA_ID)
        data.setList(true)
        var currentVolume = 0
        var mgr: AudioManager? = null
        try {
            val sdf = SimpleDateFormat("yyyyMMddHHmmZ")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                var attempts = 0
                val maximum = 4
                mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                //get current volume
                currentVolume = mgr!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                PreyConfig.getInstance(context).setVolume(currentVolume)
                do {
                    try {
                        getInstance(context).dataImagen = null
                        PreyLogger.d("report front attempts FRONT:$attempts")
                        val frontPicture = getPicture(context, BACK)
                        if (frontPicture != null) {
                            PreyLogger.d("report data length front=${frontPicture.size}")
                            val file: InputStream = ByteArrayInputStream(frontPicture)
                            val entityFile = EntityFile()
                            entityFile.setFileInputStream(file)
                            entityFile.setFileMimeType("image/png")
                            entityFile.setFileName("picture.jpg")
                            entityFile.setName("picture")
                            entityFile.setFileType("image/png")
                            entityFile.setFileId("${sdf.format(Date())}_${entityFile.getFileType()}")
                            entityFile.setFileSize(frontPicture.size)
                            data.addEntityFile(entityFile)
                            attempts = maximum
                        }
                    } catch (e: Exception) {
                        PreyLogger.e("report error:${e.message}", e)
                        PreyFirebaseCrashlytics.getInstance(context).recordException(e)
                    }
                    attempts++
                } while (attempts < maximum)
                val numberOfCameras = getNumberOfCameras(context)
                if (numberOfCameras != null && numberOfCameras > 1) {
                    attempts = 0
                    do {
                        try {
                            getInstance(context).dataImagen = null
                            PreyLogger.d("report back attempts BACK:$attempts")
                            val backPicture = getPicture(context, FRONT)
                            if (backPicture != null) {
                                PreyLogger.d("report data length back=${backPicture.size}")
                                val file: InputStream = ByteArrayInputStream(backPicture)
                                val entityFile = EntityFile()
                                entityFile.setFileInputStream(file)
                                entityFile.setFileMimeType("image/png")
                                entityFile.setFileName("screenshot.jpg")
                                entityFile.setName("screenshot")
                                entityFile.setFileType("image/png")
                                entityFile.setFileId("${sdf.format(Date())}_${entityFile.getFileType()}")
                                entityFile.setFileSize(backPicture.size)
                                data.addEntityFile(entityFile)
                                attempts = maximum
                            }
                        } catch (e: Exception) {
                            PreyLogger.e("report error:$attempts", e)
                            PreyFirebaseCrashlytics.getInstance(context).recordException(e)
                        }
                        attempts++
                    } while (attempts < maximum)
                }
            }
            PreyLogger.d("report data files size:${data.getEntityFiles().size}")
            val intentCamera = Intent(context, SimpleCameraActivity::class.java)
            intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val myKillerBundle = Bundle()
            myKillerBundle.putInt("kill", 1)
            intentCamera.putExtras(myKillerBundle)
            context.startActivity(intentCamera)
            context.sendBroadcast(Intent(CheckPasswordHtmlActivity.CLOSE_PREY))
        } catch (e: Exception) {
            PreyLogger.e("report error: ${e.message}", e)
            PreyFirebaseCrashlytics.getInstance(context).recordException(e)
        } finally {
            try {
                currentVolume = PreyConfig.getInstance(context).getVolume()
                if (currentVolume > 0) {
                    //set old volume
                    mgr!!.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        currentVolume,
                        AudioManager.FLAG_PLAY_SOUND
                    )
                }
            } catch (e: Exception) {
                PreyLogger.e("report error: ${e.message}", e)
                PreyFirebaseCrashlytics.getInstance(context).recordException(e)
            }
        }
        return data
    }

    /**
     * Method gets the image from a camera, in the process it gets the initial volume, it is muted and at the end it returns to the initial volume
     *
     * @return byte array
     */
    public fun getPicture(context: Context, focus: String): ByteArray? {
        var mgr: AudioManager? = null
        getInstance(context).dataImagen = null
        val streamType = AudioManager.STREAM_SYSTEM
        getInstance(context).activity = null
        val intentCamera = Intent(context, SimpleCameraActivity::class.java)
        intentCamera.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intentCamera.putExtra("focus", focus)
        context.startActivity(intentCamera)
        var i = 0
        mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
            PreyLogger.e("report error:${e.message}", e)
        }
        while (getInstance(context).activity == null && i < 10) {
            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {
                PreyLogger.e("report error: ${e.message}", e)
            }
            i++
        }
        if (getInstance(context).activity != null) {
            getInstance(context).activity!!.takePicture(context)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mgr!!.ringerMode = AudioManager.RINGER_MODE_NORMAL
            mgr!!.setStreamMute(streamType, false)
        }
        try {
            i = 0
            while (getInstance(context).activity != null && getInstance(context).dataImagen == null && i < 9) {
                Thread.sleep(500)
                i++
            }
        } catch (e: InterruptedException) {
            PreyLogger.e("report error: ${e.message}", e)
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            PreyLogger.e("report error: ${e.message}", e)
        }
        var out: ByteArray? = null
        if (getInstance(context).activity != null) {
            out = getInstance(context).dataImagen
            getInstance(context).activity!!.finish()
            getInstance(context).activity = null
            getInstance(context).dataImagen = null
        }
        try {
            currentVolume = PreyConfig.getInstance(context).getVolume()
            if (currentVolume > 0) {
                //set old volume
                mgr!!.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    currentVolume,
                    AudioManager.FLAG_PLAY_SOUND
                )
            }
        } catch (e: Exception) {
            PreyLogger.e("report error: ${e.message}", e)
        }
        return out
    }

    /**
     * Method obtains camera numbers
     *
     * @return camera numbers
     */
    fun getNumberOfCameras(context: Context): Int {
        return Camera.getNumberOfCameras()
    }

    companion object {
        const val TAG: String = "memory"
        const val FRONT: String = "front"
        const val BACK: String = "back"
        const val DATA_ID: String = "webcam"
        private var instance: PictureUtil? = null
        fun getInstance(context: Context): PictureUtil {
            return instance ?: PictureUtil(context).also { instance = it }
        }
    }

}