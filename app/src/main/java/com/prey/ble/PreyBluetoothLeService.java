package com.prey.ble;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;

import android.util.Log;
import android.widget.Toast;

import com.prey.PreyLogger;
import com.prey.actions.alarm.AlarmThread;
import com.prey.ble.db.KeyDatasource;
import com.prey.ble.db.KeyDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PreyBluetoothLeService  extends Service {



    public PreyBluetoothLeService() {
        PreyLogger.i("PreyBluetoothLeService  create ________");


    }
    @Override
    public void onStart(Intent intent, int startId) {
        PreyLogger.i("PreyBluetoothLeService  start ________");
        initialize(this);


    }


    private BluetoothAdapter mBluetoothAdapter=null;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private ArrayList<BluetoothDevice> devices;
    private Map<String,BluetoothDevice> map;
    private PreyBluetoothLeService mBluetoothLeService;

    private final IBinder mBinder = new LocalBinder();


    public class LocalBinder extends Binder {
        PreyBluetoothLeService getService() {
            return PreyBluetoothLeService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }



    BluetoothManager mBluetoothManager=null;

    public boolean initialize(Context ctx) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager)  this.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                PreyLogger.d(  "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            PreyLogger.d( "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        mBluetoothLeScanner =mBluetoothAdapter.getBluetoothLeScanner();
        mHandler = new Handler();

        starScan();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);


        KeyDatasource data=new KeyDatasource(getApplicationContext());
        //  data.deleteAllKeys();

        List<KeyDto> list= data.getAllKeys();
        for (int i=0;list!=null&&i<list.size();i++){
            KeyDto key=list.get(i);

            remote (key.getAddress());
        }


        return true;
    }

    public void starScan()
    {

        PreyLogger.d("starScan");
        mScanning = true;
        mBluetoothLeScanner.startScan(mScanCallback);
    }

    public void stopScan(){
        PreyLogger.d("stopScan");
        try {
            if (mScanning) {
                mBluetoothLeScanner.stopScan(mScanCallback);
                mScanning = false;

                PreyLogger.d("now stop scan success fully");
            }
        } catch (RuntimeException excp) {
            PreyLogger.d("start scan error" + excp.getCause());
        }


    }


    private BluetoothLeScanner mBluetoothLeScanner =null;
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice device=result.getDevice();
            String name=device.getName();
            String address=device.getAddress();

            if (name!=null&&("iTrack".equals(name) || "TrackR".equals(name))) {
                PreyLogger.i("device  name:" +name+" address:"+address);

                BluetoothGatt gatt=KeyInstance.getKeyInstance(getApplicationContext()).getBluetoothGatt(address);
                if(gatt==null){
                    remote(address );
                }
            }



        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void remote(String strMacAddr){
        PreyLogger.d( "remote on " + strMacAddr);
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(strMacAddr);
        if (device == null) {
            PreyLogger.d( "Start connect failed " + strMacAddr);
        }else{
            PreyLogger.d( "Start connect start " + strMacAddr);
            BluetoothGatt bluegatt=device.connectGatt(this,true,new  PreyBluetoothGattCallback(this));
            if(bluegatt==null){
                PreyLogger.d( "bluegatt null" + strMacAddr);
            }else{
                PreyLogger.d( "bluegatt connectGatt " + strMacAddr);
                KeyInstance.getKeyInstance(this).putBluetoothGatt(strMacAddr,bluegatt);
            }
        }

    }



    private String newState( int newState){
        String state="";
        switch (  newState){
            case 2: state="STATE_CONNECTED";break;
            case 1: state="STATE_CONNECTING";break;
            case 0: state="STATE_DISCONNECTED";break;
            default: state="STATE_DISCONNECTING";break;
        }
        return state;
    }

    public class PreyBluetoothGattCallback extends BluetoothGattCallback {
        PreyBluetoothLeService service=null;
        PreyBluetoothGattCallback(PreyBluetoothLeService service){
            this.service=service;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {


            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onConnectionStateChange:"+status+" "+newState(newState));
            BluetoothDevice tmpBlePerp = gatt.getDevice();

            //check if result is success
            if (status != BluetoothGatt.GATT_SUCCESS)
            {

                PreyLogger.d( "["+gatt.getDevice().getAddress()+"]onConnectionStateChange failed:" );
                service.remote(gatt.getDevice().getAddress());
            }else {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Attempts to discover services after successful connection.
                    PreyLogger.d( "["+gatt.getDevice().getAddress()+"]Attempting to start service discovery:");

                    //发现服务
                    if (!gatt.discoverServices()) {
                        PreyLogger.d( "["+gatt.getDevice().getAddress()+"]start service discovery failed");
                    }


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    PreyLogger.d( "["+gatt.getDevice().getAddress()+"]Disconnected from GATT server." );



                } else if (newState == BluetoothProfile.STATE_CONNECTING) {

                    PreyLogger.d( "["+gatt.getDevice().getAddress()+"]Connecting to GATT server.");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    service.remote(gatt.getDevice().getAddress());
                    PreyLogger.d( "["+gatt.getDevice().getAddress()+"]DisConnecting to GATT server.");
                } else {
                    PreyLogger.d( "["+gatt.getDevice().getAddress()+"]Unknown state to GATT server.");
                }
            }

        }


        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onServicesDiscovered:"+status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                //wirte password to iTrack
                writePassword2Itrack(gatt);

                List<BluetoothGattService> gattSrvs = gatt.getServices();
                if (gattSrvs == null)
                {
                    return;
                }

                //print services and services uuid
                for (BluetoothGattService gattService : gattSrvs) {
                    //-----Service的字段信息-----//
                    int type = gattService.getType();
                    //PreyLogger.d( "-->service type:" + Utils.getServiceType(type));
                    //PreyLogger.d( "-->includedServices size:" + gattService.getIncludedServices().size());
                    //PreyLogger.d( "-->service uuid:" + gattService.getUuid());

                    //-----Characteristics的字段信息-----//
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                        //PreyLogger.d( "---->char uuid:" + gattCharacteristic.getUuid());

                        int permission = gattCharacteristic.getPermissions();
                        //PreyLogger.d( "---->char permission:" + Utils.getCharPermission(permission));

                        int property = gattCharacteristic.getProperties();
                        // PreyLogger.d( "---->char property:" + Utils.getCharPropertie(property));

                        byte[] data = gattCharacteristic.getValue();
                        if (data != null && data.length > 0) {
                            //PreyLogger.d( "---->char value:" + new String(data));
                        }

                        //-----Descriptors的字段信息-----//
                        List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                        for (BluetoothGattDescriptor gattDescriptor : gattDescriptors) {
                            //PreyLogger.d( "-------->desc uuid:" + gattDescriptor.getUuid());
                            int descPermission = gattDescriptor.getPermissions();
                            //PreyLogger.d( "-------->desc permission:" + Utils.getDescPermission(descPermission));

                            byte[] desData = gattDescriptor.getValue();
                            if (desData != null && desData.length > 0) {
                                //PreyLogger.d( "-------->desc value:" + new String(desData));
                            }
                        }
                    }
                }
            }
            else{
                PreyLogger.d( "onServicesDiscovered received: " + status);
            }

        }

        public  boolean IsAllConnect()
        {

            PreyLogger.d("IsAllConnect");
            return  true;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onCharacteristicRead:"+status);

            if (status == BluetoothGatt.GATT_SUCCESS)
                PreyLogger.d("onCharRead "+gatt.getDevice().getName()
                        +" read "
                        +characteristic.getUuid().toString()
                        +" -> "
                        +PreyBlueUtils.bytesToHexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onCharacteristicWrite:"+status);
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onCharWrite "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

            //enable key notifycation
            UUID serviceUuid = characteristic.getService().getUuid();
            UUID charUuid = characteristic.getUuid();
            if(serviceUuid.equals(PreyBlueUtils.CUSTOM_SERVICE)
                    && charUuid.equals(PreyBlueUtils.CHARACTERISTIC_CUSTOM_VERIFIED)) {
                enableKeyPressNotification(gatt);
            }
        }

        //write password
        private void writePassword2Itrack(BluetoothGatt gatt)
        {
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]writePassword2Itrack:");
            BluetoothGattService service = gatt.getService(PreyBlueUtils.CUSTOM_SERVICE);
            if (service == null) {
                PreyLogger.d( "["+gatt.getDevice().getAddress()+"] writePassword2Itrack failed.");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(PreyBlueUtils.CHARACTERISTIC_CUSTOM_VERIFIED);
            if (characteristic == null) {
                PreyLogger.d( "["+gatt.getDevice().getAddress()+"]: writePassword2Itrack failed.");
                return;
            }

            gatt.setCharacteristicNotification(characteristic, true);
            //set password
            byte pwd[] = {(byte) 0xA1, (byte) 0xA4, (byte) 0x24, (byte) 0xA4};
            characteristic.setValue(pwd);

            //write password
            if (gatt.writeCharacteristic(characteristic))
            {
                PreyLogger.d( "["+gatt.getDevice().getAddress()+"]start write password success");
            }
        }

        //enable key notification
        private void enableKeyPressNotification(BluetoothGatt gatt)
        {
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]enableKeyPressNotification:");

            BluetoothGattService service = gatt.getService(PreyBlueUtils.CHARACTERISTIC_KEY_PRESS_SRV_UUID);
            if (service == null) {
                PreyLogger.d( ":setCharacteristicNotification set enable failed.");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(PreyBlueUtils.CHARACTERISTIC_KEY_PRESS_UUID);
            if (characteristic == null) {
                PreyLogger.d( ":setCharacteristicNotification set enable failed.");
                return;
            }

            //set enable
            if (!gatt.setCharacteristicNotification(characteristic, true)) {
                PreyLogger.d( ":setCharacteristicNotification set enable failed.");
                return;
            }

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(PreyBlueUtils.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

            if (!gatt.writeDescriptor(descriptor))
            {
                //descriptor write operation successfully started?
                PreyLogger.d( ":writeDescriptor set enable failed.");
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            PreyLogger.d("["+gatt.getDevice().getAddress()+"]onCharacteristicChanged:");
            String strMac = gatt.getDevice().getAddress();


            UUID charUuid = characteristic.getUuid();
            if (charUuid.equals(PreyBlueUtils.CHARACTERISTIC_KEY_PRESS_UUID)) {
                Integer keyPressValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

                if (keyPressValue == 2){
                    PreyLogger.d("["+gatt.getDevice().getAddress()+"]KEY_HOLD_NOTIFY");
                    alert("KEY_HOLD:"+ color(gatt.getDevice().getAddress()));
                    new AlarmThread(getApplicationContext(), "modem",null,null).start();
                }else{
                    PreyLogger.d("["+gatt.getDevice().getAddress()+"]KEY_PRESS_NOTIFY");
                    alert("KEY_PRESS:"+ color(gatt.getDevice().getAddress()));
                    new AlarmThread(getApplicationContext(), "ring",null,null).start();
                }



                PreyLogger.d( String.format("["+gatt.getDevice().getAddress()+"]Receive an key press ntf, value:%d", keyPressValue));
            }
        }

    };

    public String color(String address){
        if("00:81:F9:53:77:7C".equals(address))
            return "rojo";
        if("E8:CE:6A:1D:7F:13".equals(address))
            return "n2";
        if("00:81:F9:4C:85:03".equals(address))
            return "verde";

        return "naranjo";
    }
    public void alert(final String texto){


        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(PreyBluetoothLeService.this.getApplicationContext(),texto,Toast.LENGTH_SHORT).show();
            }
        });
        try {
            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void addDevice(BluetoothDevice device){
        if(!contiene(device)){
            devices.add(device);
            map.put(device.getAddress(),device);
            PreyLogger.d("se agrego"+device.getAddress());
        }else {
            //  PreyLogger.d("ya esta");
        }
    }

    private boolean contiene(BluetoothDevice device){
        for (int i=0;devices!=null&&i<devices.size();i++){
            BluetoothDevice dev=devices.get(i);
            if(dev.getAddress().equals(device.getAddress())){
                return true;
            }
        }
        return false;
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String name = null;
            String address = null;
            /*
            try {
                name = result.getDevice().getName();
                PreyLogger.d( name);
            }catch (Exception e){}
            try {
                address=result.getDevice().getAddress();
                PreyLogger.d(address);
            }catch (Exception e){}
            if(name!=null&&"iTrack".equals(name)){


                addDevice(result.getDevice());
            }
            if("AC:23:3F:25:24:8F".equals(address)){
                addDevice(result.getDevice());
            }*/
            addDevice(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };






}
