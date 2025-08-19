package com.prey.actions.wipe

import android.content.Context
import com.prey.PreyLogger

class TestWipe : WipeInterface {

    override fun deleteSD(context: Context) {
        PreyLogger.d("____________________deleteSD")
        PreyLogger.d("____________________deleteSD")
        PreyLogger.d("____________________deleteSD")
        PreyLogger.d("____________________deleteSD")
    }

    override fun wipeData(context: Context) {
        PreyLogger.d("____________________wipeData")
        PreyLogger.d("____________________wipeData")
        PreyLogger.d("____________________wipeData")
        PreyLogger.d("____________________wipeData")
    }

}