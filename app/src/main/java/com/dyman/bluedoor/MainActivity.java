package com.dyman.bluedoor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.dyman.componentdoor.util.DoorUtil;
import com.orhanobut.logger.Logger;

public class MainActivity extends Activity {

    private final static int REQUEST_PERMISSION_CALLBACK = 101;
    private final static int REQUEST_PERMISSION_SETTINGS = 102;

    private Button openDoorBtn;
    private Button closeDoorBtn;
    private DoorUtil mDoorUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        mDoorUtil = new DoorUtil(this);
        openDoorBtn = findViewById(R.id.openDoor_btn);
        closeDoorBtn = findViewById(R.id.closeDoor_btn);

        openDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDoorUtil = new DoorUtil(MainActivity.this);
                mDoorUtil.openDoor(true);
            }
        });

        closeDoorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDoorUtil = new DoorUtil(MainActivity.this);
                mDoorUtil.openDoor(false);
            }
        });
    }


    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_WIFI_STATE) ||
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CHANGE_NETWORK_STATE)) {
                //  该权限已被禁止
                ToastUtil.showMessage(MainActivity.this, "您已拒绝权限，请手动打开设置");
                Intent it = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                it.setData(uri);
                startActivityForResult(it, REQUEST_PERMISSION_SETTINGS);
                return;
            }
            Logger.i("请求权限");
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_NETWORK_STATE,
            }, REQUEST_PERMISSION_CALLBACK);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CALLBACK:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Logger.i("请求权限成功");
                } else {
                    Logger.i("权限已被禁用");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_SETTINGS) {
            Logger.i("收到手动设置权限的回调");
            checkPermission();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDoorUtil.close();
    }
}
