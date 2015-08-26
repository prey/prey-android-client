package com.prey.actions.picture;

/**
 * Created by oso on 24-08-15.
 */
import com.prey.PreyEmail;
import com.prey.actions.HttpDataService;


import android.content.Context;

public class PictureThread extends Thread {
    private Context ctx;

    public PictureThread(Context ctx) {
        this.ctx = ctx;
    }

    public void run() {
        HttpDataService data= PictureUtil.getPicture(ctx);
        PreyEmail.sendDataMail(ctx, data);
    }

}
