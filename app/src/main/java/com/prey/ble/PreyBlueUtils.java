package com.prey.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PreyBlueUtils {
    public static final String IMMEDIATE_ALERT_SERVICE_UUID_STRING = "00001802-0000-1000-8000-00805f9b34fb";
    public static final java.util.UUID CUSTOM_SERVICE = java.util.UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");

    public static final java.util.UUID CHARACTERISTIC_CUSTOM_VERIFIED = java.util.UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");

    public static final java.util.UUID IMMEDIATE_ALERT_SERVICE_UUID = java.util.UUID.fromString(IMMEDIATE_ALERT_SERVICE_UUID_STRING);
    public static final java.util.UUID CHARACTERISTIC_KEY_PRESS_SRV_UUID = java.util.UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    final public static String UUID_KEY_DATA = "0000fff4-0000-1000-8000-00805f9b34fb";
    final public static String TI_KEYFOB_PROXIMITY_ALERT_PROPERTY_UUID = "00002a06-0000-1000-8000-00805f9b34fb";
    public static final java.util.UUID CHARACTERISTIC_KEY_PRESS_UUID = java.util.UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final java.util.UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    final public static String TI_KEYFOB_PROXIMITY_ALERT_UUID = "00001802-0000-1000-8000-00805f9b34fb";
    final public static String TI_KEYFOB_BATT_SERVICE_UUID = "0000180F-0000-1000-8000-00805f9b34fb";
    final public static String TI_KEYFOB_DISCONN_ALERT_SRV_UUID = "00001803-0000-1000-8000-00805f9b34fb";
    final public static String TI_KEYFOB_ITRACK_SRV_UUID = "0000FFF0-0000-1000-8000-00805f9b34fb";
    final public static String TI_KEYFOB_KEYS_SERVICE_UUID = "0000FFE0-0000-1000-8000-00805f9b34fb";


    private static HashMap<Integer, String> serviceTypes = new HashMap();
    static {
        // Sample Services.
        serviceTypes.put(BluetoothGattService.SERVICE_TYPE_PRIMARY, "PRIMARY");
        serviceTypes.put(BluetoothGattService.SERVICE_TYPE_SECONDARY, "SECONDARY");
    }

    public static String getServiceType(int type){
        return serviceTypes.get(type);
    }


    //-------------------------------------------
    private static HashMap<Integer, String> charPermissions = new HashMap();
    static {
        charPermissions.put(0, "UNKNOW");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ, "READ");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE, "WRITE");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
        charPermissions.put(BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
    }

    public static String getCharPermission(int permission){
        return getHashMapValue(charPermissions,permission);
    }
    //-------------------------------------------
    private static HashMap<Integer, String> charProperties = new HashMap();
    static {

        charProperties.put(BluetoothGattCharacteristic.PROPERTY_BROADCAST, "BROADCAST");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS, "EXTENDED_PROPS");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_INDICATE, "INDICATE");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_NOTIFY, "NOTIFY");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_READ, "READ");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, "SIGNED_WRITE");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_WRITE, "WRITE");
        charProperties.put(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, "WRITE_NO_RESPONSE");
    }

    public static String getCharPropertie(int property){
        return getHashMapValue(charProperties,property);
    }

    //--------------------------------------------------------------------------
    private static HashMap<Integer, String> descPermissions = new HashMap();
    static {
        descPermissions.put(0, "UNKNOW");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ, "READ");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED, "READ_ENCRYPTED");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM, "READ_ENCRYPTED_MITM");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE, "WRITE");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED, "WRITE_ENCRYPTED");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM, "WRITE_ENCRYPTED_MITM");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED, "WRITE_SIGNED");
        descPermissions.put(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM, "WRITE_SIGNED_MITM");
    }

    public static String getDescPermission(int property){
        return getHashMapValue(descPermissions,property);
    }


    private static String getHashMapValue(HashMap<Integer, String> hashMap,int number){
        String result =hashMap.get(number);
        if(TextUtils.isEmpty(result)){
            List<Integer> numbers = getElement(number);
            result="";
            for(int i=0;i<numbers.size();i++){
                result+=hashMap.get(numbers.get(i))+"|";
            }
        }
        return result;
    }

    /**
     * 位运算结果的反推函数10 -> 2 | 8;
     */
    static private List<Integer> getElement(int number){
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < 32; i++){
            int b = 1 << i;
            if ((number & b) > 0)
                result.add(b);
        }

        return result;
    }


    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}

