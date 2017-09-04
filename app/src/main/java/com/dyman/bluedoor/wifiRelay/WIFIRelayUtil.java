package com.dyman.bluedoor.wifiRelay;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.dyman.bluedoor.Global;
import com.dyman.bluedoor.util.ToastUtil;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by thinkjoy on 2017/8/22.
 */
//wifi继电器辅助类
public class WIFIRelayUtil{

    private static WIFIRelayUtil relayUtil;
    private static Context mContext;
    private static String connectIP = null;    //找到设备的ip地址
    private static Socket socket;  //TCP socket
    private InetAddress UDPAddress; //广播地址
    public static boolean isConnected = false;
    private boolean isConnecting = false;
    public static byte[] ALL_OPEN_RELAY={(byte)0xaa,0x0a,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,0X01,(byte)0xbb};
    public static byte[] ALL_CLOSE_RELAY={(byte)0xaa,0x0b,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0x02,0X01,(byte)0xbb};

    public static final String ACTION_DEVICE_FINDED = "com.tjoybox.wifiRelay.ACTION_DEVICE_FINDED";
    public static final String ACTION_DEVICE_NOT_FIND = "com.tjoybox.wifiRelay.ACTION_DEVICE_NOT_FIND";
    public static final String ACTION_DEVICE_CONNECTED = "com.tjoybox.wifiRelay.ACTION_DEVICE_CONNECTED";
    public static final String ACTION_DEVICE_CONNECT_FAIL = "com.tjoybox.wifiRelay.ACTION_DEVICE_CONNECT_FAIL";

    private WIFIRelayUtil(){}

    public static synchronized WIFIRelayUtil getInstance() {
        if (relayUtil == null) {
            relayUtil = new WIFIRelayUtil();
        }
        return relayUtil;
    }

    public void init(Context context){
        mContext = context;
        try {
            UDPAddress = InetAddress.getByName(Global.Const.UDP_BROADCAST_ADDRESS);
            Log.e("TAG", "wifi继电器-------广播地址"+UDPAddress);
            new ScanThread().start();
//             new TCPConnectThread().start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Log.e("TAG", "wifi继电器-------广播地址获取失败");
        }
    }

    public void openDoor(boolean isOpen){
        if (socket == null || !socket.isConnected()){   //顺序不能变
            if (!isConnecting){
                ToastUtil.showMessage(mContext,"设备未连接，重新连接中...");
                reConnect();
            }
            return;
        }
        if (isOpen)
            new SendDataThread(socket,ALL_OPEN_RELAY).start();
        else
            new SendDataThread(socket,ALL_CLOSE_RELAY).start();
    }

    public void reConnect(){
        disConnect();
        if (connectIP == null){
            new ScanThread().start();
        }else{
            new TCPConnectThread().start();
        }
    }

    private class ScanThread extends Thread {
        MulticastSocket mMulSocket;
        @Override
        public void run() {
            isConnecting = true;
            byte[] send = new String("HLK").getBytes();
            byte[] recBuff =new byte[128];
            DatagramPacket dataPacket = new DatagramPacket(send,send.length, UDPAddress, Global.Const.UdpPort);
            DatagramPacket recPacket = new DatagramPacket(recBuff, recBuff.length);
            try {
                mMulSocket = new MulticastSocket();
                mMulSocket.send(dataPacket);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("TAG","wifi继电器-------ms初始化失败  "+e.toString());
                return;
            }
            int intWaitTime = 0;
            String udpResult;
            while(true){
                try {
                    mMulSocket.setSoTimeout(2000);
                    mMulSocket.receive(recPacket);
                    udpResult = new String(recPacket.getData()).trim();
                    if (udpResult.indexOf(Global.Const.relayUUID)!=-1){    //找到设备
                        connectIP = recPacket.getAddress().toString();
                        Log.e("TAG","wifi继电器-------设备的ip："+connectIP);
                        if(connectIP.indexOf("/")!=-1)
                        {
                            connectIP = connectIP.substring(connectIP.indexOf("/")+1);
                        }
                        mContext.sendBroadcast(new Intent(ACTION_DEVICE_FINDED));
                        new TCPConnectThread().start();
                        return;
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.e("TAG","wifi继电器-------ms出错：");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TAG","wifi继电器-------Timeout：");
                }
                if (++intWaitTime>3){
                    Log.e("TAG","wifi继电器-------未找到设备：");
                    isConnecting = false;
                    mContext.sendBroadcast(new Intent(ACTION_DEVICE_NOT_FIND));
                    return;
                }
            }
        }
    }

    private class TCPConnectThread extends Thread {

        @Override
        public void run() {
            isConnecting =true;
            InetAddress serverAddr = null;
            try {
                serverAddr = InetAddress.getByName(connectIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.e("TAG","wifi继电器-------serverAddr初始化出错：");
            }
            if(socket==null)
            {
                try {
                    socket = new Socket(serverAddr, Global.Const.TcpPort);
                    if (socket.isConnected()){
                        Log.e("TAG","wifi继电器-------tcp连接成功：");
                        isConnected = true;
                       mContext.sendBroadcast(new Intent(ACTION_DEVICE_CONNECTED));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("TAG","wifi继电器-------连接失败");
                    mContext.sendBroadcast(new Intent(ACTION_DEVICE_CONNECT_FAIL));
                }
            }
            isConnecting = false;
        }
    }

    public void disConnect(){
        if (socket!=null){
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
