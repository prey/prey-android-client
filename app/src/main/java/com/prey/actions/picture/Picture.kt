package com.prey.actions.picture

import android.content.Context

import com.prey.PreyEmail

/**
 * Represents a thread for taking and sending pictures.
 */
class Picture(var context: Context) {

    /**
     * Runs the picture thread, taking a picture and sending it via email.
     */
    fun run() {
        val data = PictureUtil.getInstance(context).getPicture()
        PreyEmail.sendDataMail(context, data)
    }

}