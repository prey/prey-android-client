package com.prey.actions;

/**
 * Created by oso on 24-08-15.
 */

import com.prey.PreyLogger;

class MonitorObject{
}
public class PreyExecutionWaitNotify{

    MonitorObject myMonitorObject = new MonitorObject();
    boolean wasSignalled = false;

    public void doWait(){
        synchronized(myMonitorObject){
            while(!wasSignalled){
                try{
                    myMonitorObject.wait();
                } catch(InterruptedException e){
                    PreyLogger.e("doWait interrupted!", e);
                }
            }
            //clear signal and continue running.
            wasSignalled = false;
        }
    }

    public void doNotify(){
        synchronized(myMonitorObject){
            wasSignalled = true;
            myMonitorObject.notify();
        }
    }
}

