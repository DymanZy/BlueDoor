package com.dyman.bluedoor;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dyman.bluedoor.bluetooth.BluetoothUtil;
import com.dyman.bluedoor.util.DoorUtil;
import com.dyman.bluedoor.util.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    private Button openDoorBtn;
    private Button closeDoorBtn;
    private DoorUtil mDoorUtil;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DoorUtil.ZMQ_REQ_BLUETOOTH_TIMEOUT:
                    ToastUtil.showMessage(MainActivity.this, "门禁控制异常");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDoorUtil = new DoorUtil(this, mHandler);
        openDoorBtn = findViewById(R.id.openDoor_btn);
        closeDoorBtn = findViewById(R.id.closeDoor_btn);

        openDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDoorUtil = new DoorUtil(MainActivity.this, mHandler);
                mDoorUtil.openDoor(true);
            }
        });

        closeDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDoorUtil = new DoorUtil(MainActivity.this, mHandler);
                mDoorUtil.openDoor(false);
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDoorUtil.close();
    }
}
