package com.prey.events.retrieves;

/**
 * Created by oso on 24-08-15.
 */
import org.json.JSONException;
import org.json.JSONObject;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.prey.PreyLogger;
import com.prey.actions.battery.Battery;
import com.prey.actions.battery.BatteryInformation;
import com.prey.events.manager.EventManager;



public class EventRetrieveDataBattery {


    private EventManager manager;

    public  void execute(Context context,EventManager manager){
        this.manager=manager;
        context.getApplicationContext().registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            try{
                Battery battery=new BatteryInformation().makeBattery(intent);
                ctx.unregisterReceiver(mBatInfoReceiver);
                if (battery!=null){
                    String state=battery.isCharging()?"charging":"discharging";
                    String remaining=Double.toString(battery.getLevel());
                    JSONObject batteryJSon = new JSONObject();
                    try {
                        JSONObject batteryElementJSon = new JSONObject();
                        batteryElementJSon.put("state", state);
                        batteryElementJSon.put("percentage_remaining",remaining );
                        batteryJSon.put("battery_status", batteryElementJSon);
                    } catch (JSONException e) {
                    }
                    PreyLogger.d("battery: state["+state+"] remaining["+remaining+"]");
                    manager.receivesData(EventManager.BATTERY, batteryJSon);
                }
            }catch(Exception e){
                JSONObject batteryJSon = new JSONObject();
                manager.receivesData(EventManager.BATTERY, batteryJSon);
            }
        }
    };
}
