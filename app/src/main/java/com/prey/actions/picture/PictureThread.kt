package com.prey.actions.picture

import android.content.Context
import com.prey.PreyEmail

class PictureThread  {
    private var ctx: Context? = null

    constructor(ctx: Context) {
        this.ctx = ctx
    }

    fun run() {
        val data = PictureUtil.getInstance(ctx!!).getPicture(ctx!!)
        PreyEmail.sendDataMail(ctx!!, data)
    }
}