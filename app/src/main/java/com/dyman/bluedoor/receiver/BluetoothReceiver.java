package com.dyman.bluedoor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dyman.bluedoor.bluetooth.BluetoothLeService;
import com.dyman.bluedoor.bluetooth.BluetoothUtil;
import com.dyman.bluedoor.util.ToastUtil;


/**
 * Created by hebin
 * on 2017/3/31 0031.
 */

public class BluetoothReceiver extends BroadcastReceiver {
    
    private static final String TAG ="TAG";

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        Log.i(TAG,"BluetoothReceiver -->" + "action = " + action);


        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {                              //设备链接成功
            Log.i(TAG,"BluetoothReceiver -->BluetoothLeService.ACTION_GATT_CONNECTED");
            ToastUtil.showMessage(context,"设备连接成功");

        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {                    //设备断开连接
            Log.i(TAG,"BluetoothReceiver -->BluetoothLeService.ACTION_GATT_DISCONNECTED");
            BluetoothUtil.getInstance().initBluetooth2(context);

        } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {             //发现服务  展示服务列表
            Log.i(TAG,"BluetoothReceiver -->BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED");

        } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {                       //服务可用 展示设备返回数据
            Log.i(TAG,"BluetoothReceiver -->BluetoothLeService.ACTION_DATA_AVAILABLE");


        }
    }
}
