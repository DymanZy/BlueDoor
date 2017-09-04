package com.dyman.bluedoor.util;

import android.content.Context;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;

import com.dyman.bluedoor.Global;
import com.dyman.bluedoor.bluetooth.BluetoothLeService;
import com.dyman.bluedoor.bluetooth.BluetoothUtil;
import com.dyman.bluedoor.receiver.BluetoothReceiver;
import com.dyman.bluedoor.wifiRelay.RelayStateReceiver;
import com.dyman.bluedoor.wifiRelay.WIFIRelayUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by thinkjoy on 2017/8/25.
 */
//门禁控制辅助类
public class DoorUtil {

    private static final String TAG = "DoorUtil";

    private static CountDownTimer countDownTimer;   //关门倒计时
    private static Timer openDoorTimer = new Timer();   //ZMQ开门定时器
    private static RelayStateReceiver mRelayStateReceiver;  //wifi继电器广播接收
    private static BluetoothReceiver mGattUpdateReceiver;   //蓝牙广播接收
    public static final int ZMQ_REQ_BLUETOOTH_TIMEOUT = 13;  //蓝牙ZMQ连接超时
    private static boolean doorIsOpened = false;    //开门标志
    private static Context mContext;
    private static Handler mHandler;

    {
        countDownTimer = new CountDownTimer(Global.Const.millisInFuture, Global.Const.countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {}
            @Override
            public void onFinish() {
                //关门
                openDoor(false);
            }
        };
    }

    public DoorUtil(Context context){
        mContext = context;
        openDoorTimer = new Timer();
        register();
    }

    public DoorUtil(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        openDoorTimer = new Timer();
        register();
    }


    public  void openDoor(boolean isOpen){
        switch (Global.Const.openFlag){
            case Global.Const.FLAG_ZQM :
                openDoorZmq(isOpen);
                break;
            case Global.Const.FLAG_BLUETOOTH:
                openDoorBlue(isOpen);
                break;
            case Global.Const.FLAG_WIFI :
                openDoorWIFI(isOpen);
                break;
        }
    }


    public void openDoorZmq(final boolean open) {

        openDoorTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                openDoorByZMQ(open);
            }
        }, 1);
    }


    public void openDoorByZMQ(boolean isOpen) {

        if (doorIsOpened && isOpen) {    // 门已开，不重复开
            countDownTimer.cancel();    //关门倒计时重新计时
            countDownTimer.start();
            return;
        }

        if (!doorIsOpened && !isOpen) {  //  门已关，不重复关
            return;
        }

        String open;
        if (isOpen) {
            open = "{\"status\" : \"1\"}";
        } else {
            open = "{\"status\" : \"0\"}";
        }

        mHandler.sendEmptyMessageDelayed(ZMQ_REQ_BLUETOOTH_TIMEOUT, 5 * 1000);
        ZMQ.Context zCtx = ZMQ.context(1);
        ZMQ.Socket req = zCtx.socket(ZMQ.REQ);
        req.connect(Global.UrlPath.ZMQ_BLUETOOTH_REQ_ADDRESS);
        req.send(open.getBytes(), 0);
        String state = new String(req.recv(0));
        mHandler.removeMessages(ZMQ_REQ_BLUETOOTH_TIMEOUT);

        try {
            JSONObject resultJson = new JSONObject(state);
            if (resultJson.getString("flag").equals("ok")) {
                doorIsOpened = isOpen;
                if (doorIsOpened) {
                    countDownTimer.start();
                }
                Log.i("TAG","dzy   " + (isOpen ? "开门成功" : "关门成功"));
            } else if (resultJson.getString("flag").equals("timeout")) {
                Log.i("TAG","dzy   服务器连接连接超时，重新连接中");
            } else {
                Log.i("TAG","dzy   服务器不能响应该未知指令");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        req.close();
        zCtx.term();
    }


    public void openDoorBlue(boolean isOpen) {

        if (doorIsOpened && isOpen) {    // 门已开，不重复开
            countDownTimer.cancel();    //关门倒计时重新计时
            countDownTimer.start();
            return;
        }
        if (!doorIsOpened && !isOpen) {  //  门已关，不重复关
            return;
        }

        if (isOpen) {
            BluetoothUtil.getInstance().writeData(Global.Const.WRITE_ALLOPEN);
            countDownTimer.start();
        } else {
            BluetoothUtil.getInstance().writeData(Global.Const.WRITE_ALLCLOSE);
        }

        if (Global.Varible.isBluetoothConnected) {  //确保蓝牙连接
            doorIsOpened = isOpen;
        }
    }

    public void openDoorWIFI(boolean isOpen){
        if (doorIsOpened && isOpen) {    // 门已开，不重复开
            countDownTimer.cancel();    //关门倒计时重新计时
            countDownTimer.start();
            return;
        }
        if (!doorIsOpened && !isOpen) {  //  门已关，不重复关
            return;
        }
        if (isOpen) {
            WIFIRelayUtil.getInstance().openDoor(true);
            countDownTimer.start();
        } else {
            WIFIRelayUtil.getInstance().openDoor(false);
        }

        if (WIFIRelayUtil.isConnected) {  //确保蓝牙连接
            doorIsOpened = isOpen;
        }
    }

    public void register(){
        if (Global.Const.openFlag == Global.Const.FLAG_BLUETOOTH)
            registerBluetooth();
        else if(Global.Const.openFlag == Global.Const.FLAG_WIFI)
            registerWIFIRelay();
    }
    public void registerWIFIRelay() {
        mRelayStateReceiver = new RelayStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFIRelayUtil.ACTION_DEVICE_FINDED);
        intentFilter.addAction(WIFIRelayUtil.ACTION_DEVICE_NOT_FIND);
        intentFilter.addAction(WIFIRelayUtil.ACTION_DEVICE_CONNECTED);
        intentFilter.addAction(WIFIRelayUtil.ACTION_DEVICE_CONNECT_FAIL);
        mContext.registerReceiver(mRelayStateReceiver, intentFilter);
        WIFIRelayUtil.getInstance().init(mContext);
    }

    public void registerBluetooth(){
        //注册蓝牙监听器
        mGattUpdateReceiver = new BluetoothReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        mContext.registerReceiver(mGattUpdateReceiver, intentFilter);
        BluetoothUtil.getInstance().initBluetooth2(mContext);
    }

    public void close(){
        if (mGattUpdateReceiver!=null){
            mContext.unregisterReceiver(mGattUpdateReceiver);
        }
        if (mRelayStateReceiver!=null){
            mContext.unregisterReceiver(mRelayStateReceiver);
        }
        countDownTimer.cancel();
        openDoorTimer.cancel();
        mContext = null;
    }

}
