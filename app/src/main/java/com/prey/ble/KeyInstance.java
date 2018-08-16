package com.prey.ble;

import android.bluetooth.BluetoothGatt;
import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class KeyInstance {

    private static KeyInstance cachedInstance = null;
    private Context ctx;

    public Map<String,BluetoothGatt> map=new HashMap<>();

    public static synchronized KeyInstance getKeyInstance(Context ctx) {
        if (cachedInstance==null){
            synchronized(KeyInstance.class) {
                if (cachedInstance == null)
                    cachedInstance = new KeyInstance(ctx);
            }
        }
        return cachedInstance;
    }
    private KeyInstance(Context ctx) {
        this.ctx = ctx;
    }

    public BluetoothGatt getBluetoothGatt(String macAddress){
        return map.get(macAddress);
    }

    public void putBluetoothGatt(String macAddress,BluetoothGatt bluetoothGatt){
        map.put(macAddress,bluetoothGatt);

    }
}
