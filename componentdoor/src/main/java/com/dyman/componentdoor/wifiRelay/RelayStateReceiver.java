package com.dyman.componentdoor.wifiRelay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dyman.componentdoor.util.ToastUtil;

/**
 * Created by thinkjoy on 2017/8/22.
 */

public class RelayStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (WIFIRelayUtil.ACTION_DEVICE_FINDED.equals(action)){ //发现设备
//            ToastUtil.showMessage(context,"发现设备");
        }else if(WIFIRelayUtil.ACTION_DEVICE_NOT_FIND.equals(action)){  //未发现设备
            ToastUtil.showMessage(context,"未发现设备");
        }else if(WIFIRelayUtil.ACTION_DEVICE_CONNECTED.equals(action)){ //连接成功
//            Logger.e("wifi继电器------------ACTION_DEVICE_CONNECTED");
            ToastUtil.showMessage(context,"设备连接成功");
        }else if(WIFIRelayUtil.ACTION_DEVICE_CONNECT_FAIL.equals(action)){ //连接失败
            ToastUtil.showMessage(context,"设备连接失败");
        }
    }
}
