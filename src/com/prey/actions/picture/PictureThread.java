package com.prey.actions.picture;

 

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
