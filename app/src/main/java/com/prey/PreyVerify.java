package com.prey;

/**
 * Created by oso on 24-08-15.
 */
import com.prey.net.PreyWebServices;

import android.content.Context;

public class PreyVerify {


    private static PreyVerify instance=null;

    private PreyVerify(Context ctx){
        init(ctx);
    }

    public static PreyVerify getInstance(Context ctx){
        if (instance == null){
            instance=new PreyVerify(ctx);
        }
        return instance;
    }


    private void init(Context ctx){
        final Context myContext=ctx;
        new Thread(){
            public void run() {
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                }
                try{
                    PreyWebServices.getInstance().verify(myContext);
                }catch(Exception e){
                }
            }
        }.start();
    }

}

