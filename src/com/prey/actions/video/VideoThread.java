package com.prey.actions.video;

import android.content.Context;

import com.prey.PreyEmail;
import com.prey.actions.HttpDataService;


public class VideoThread extends Thread {
    private Context ctx;

    public VideoThread(Context ctx) {
            this.ctx = ctx;
    }

    public void run() {
            HttpDataService data= VideoUtil.getVideo(ctx);
            PreyEmail.sendDataMail(ctx, data);
            VideoUtil.deleteFile();
    }

}
