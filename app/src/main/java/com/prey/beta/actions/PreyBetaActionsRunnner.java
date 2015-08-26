package com.prey.beta.actions;

/**
 * Created by oso on 24-08-15.
 */



import android.content.Context;

public class PreyBetaActionsRunnner {

    protected boolean running = false;

    Thread myActionsRunnerThread = null;
    private String cmd;

    public PreyBetaActionsRunnner(String cmd) {
        this.cmd=cmd;
    }

    public void run(Context ctx) {
        this.myActionsRunnerThread = new Thread(new PreyBetaActionsRunner(ctx,cmd));
        this.myActionsRunnerThread.start();

    }

}

