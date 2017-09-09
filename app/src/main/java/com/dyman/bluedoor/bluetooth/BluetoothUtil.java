package com.dyman.bluedoor.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dyman.bluedoor.Global;
import com.dyman.bluedoor.util.TextUtil;
import com.dyman.bluedoor.util.countdowntimer.CountDownTimer;

import java.util.Arrays;
import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by hebin
 * on 2017/2/27 0027.
 */
@SuppressWarnings("ALL")
public class BluetoothUtil {

    private static final String TAG = "BluetoothUtil";

    private static BluetoothLeService mBluetoothLeService;

    private Context context;

    //TODO:2017-08-08
    private BluetoothAdapter mBluetoothAdapter;
    private BleScanCallback bleScanCallback = new BleScanCallback();
    private BluetoothManager bluetoothManager;
    private static boolean isScanning = false;


    private BluetoothUtil() {

    }

    private static BluetoothUtil bluetoothUtil = null;

    public static synchronized BluetoothUtil getInstance() {
        if (bluetoothUtil == null) {
            bluetoothUtil = new BluetoothUtil();
        }
        return bluetoothUtil;
    }

    // 服务绑定监听
    ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                return;
            }
            Log.i(TAG, "蓝牙设备链接开始");
//                Toast.makeText(context,"开始链接蓝牙设备",Toast.LENGTH_SHORT).show();
            mBluetoothLeService.connect(Global.Const.MAC);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    /**
     * 初始化蓝牙连接
     */
    public void initBluetooth2(final Context context) {
        this.context = context;
        //扫描蓝牙设备
        scanBLEDevice();
    }


    public void initBluetooth(final Context context) {
        this.context = context;
        // 蓝牙服务
        Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);

        Log.i(TAG, "蓝牙服务绑定");
        boolean bll = context.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        if (bll) {
//            Toast.makeText(context, "蓝牙服务绑定服务成功", Toast.LENGTH_LONG).show();
            Log.i(TAG, "蓝牙服务绑定服务成功");

        } else {
            Toast.makeText(context, "蓝牙服务绑定服务失败", Toast.LENGTH_LONG).show();
            Log.i(TAG, "蓝牙服务绑定服务失败");
        }
    }


    private void scanBLEDevice() {
        if (bluetoothManager == null){
            bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        if (mBluetoothAdapter != null && !isScanning){
            if (mBluetoothAdapter.startLeScan(bleScanCallback)) {
                isScanning = true;
                Toast.makeText(context,"正在扫描蓝牙设备", Toast.LENGTH_SHORT).show();
                countDownTimer.start();
            }else{
                Toast.makeText(context,"蓝牙搜索失败", Toast.LENGTH_SHORT).show();
                isScanning = false;
            }
        }

    }


    private CountDownTimer countDownTimer = new CountDownTimer(Global.Const.BLE_SCAN_TIMEOUT,1000) {
        @Override
        public void onTick(long millisUntilFinished) {}
        @Override
        public void onFinish() {
            mBluetoothAdapter.stopLeScan(bleScanCallback);
            isScanning = false;
            Log.d("TAG","蓝牙--------扫描结束，未发现设备");
        }
    };


    // 实现扫描回调接口
    private class BleScanCallback implements BluetoothAdapter.LeScanCallback {
        // 扫描到新设备时，会回调该接口。可以将新设备显示在ui中，看具体需求
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device.getAddress().equals(Global.Const.MAC)){
                mBluetoothAdapter.stopLeScan(bleScanCallback);
                countDownTimer.cancel();
                isScanning = false;
                //蓝牙服务
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.connect(Global.Const.MAC);
                    return;
                }
                Intent gattServiceIntent = new Intent(context, BluetoothLeService.class);
                Log.i(TAG, "蓝牙服务绑定");
                boolean bll = context.bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                if (bll) {
//                    Toast.makeText(context, "蓝牙服务绑定成功", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "蓝牙服务绑定服务成功");

                } else {
                    Toast.makeText(context, "蓝牙服务绑定服务失败", Toast.LENGTH_LONG).show();
                    Log.i(TAG, "蓝牙服务绑定服务失败");
                }
            }
        }
    }

    /**
     * 向设备输入指令
     */
    public void writeData(byte[] data) {

        if (mBluetoothLeService == null || !Global.Varible.isBluetoothConnected) {
            //TODO：2017-8-7
            Toast.makeText(context, "蓝牙设备未连接", Toast.LENGTH_LONG).show();
            Log.e(TAG, "蓝牙设备未连接");
            //重新连接
            BluetoothUtil.getInstance().initBluetooth2(context);
            return;
        }

        if (mBluetoothLeService.getSupportedGattServices() != null) {
            for (BluetoothGattService service : mBluetoothLeService.getSupportedGattServices()) {
                if (service.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {  //服务UUID
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")); //特征UUID
                    if (!TextUtil.isEmpty(Arrays.toString(data))) {
                        characteristic.setValue(data);
                        mBluetoothLeService.wirteCharacteristic(characteristic);

                        Log.i(TAG, "写入设备特征值: characteristic  =" + Arrays.toString(characteristic.getValue()));
                        Log.i(TAG, "写入服务地址: service  =" + characteristic.getService().getUuid());
                        Log.i(TAG, "写入特征地址: characteristic  = " + characteristic.getUuid());
                    }
                }
            }
        } else {
            Toast.makeText(context, "设备服务列表为空", Toast.LENGTH_LONG).show();
            Log.i(TAG, "设备服务列表为空");
        }
    }

    public void unbind(){
        if (this.context == null)
            return;
        context.unbindService(mServiceConnection);
    }


}
