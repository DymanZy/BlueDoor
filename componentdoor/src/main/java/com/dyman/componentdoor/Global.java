package com.dyman.componentdoor;

/**
 * Created by dyman on 2017/9/11.
 */

public class Global {

    public static class Const {
        public final static boolean isDebug = false;

        //门禁控制
        public final static int openFlag = 1; //开门方式（0、ZMQ 1、蓝牙 2、WIFI）
        public final static int FLAG_ZQM = 0;
        public final static int FLAG_BLUETOOTH = 1;
        public final static int FLAG_WIFI = 2;

        public final static String MAC = "04:A3:16:A7:0D:93";

        public final static long BLE_SCAN_TIMEOUT = 10000;  //蓝牙扫描时间
        public final static long millisInFuture = 5000;     //关门时间
        public final static long countDownInterval = 1000;  //定时间隔

        public final static String UDP_BROADCAST_ADDRESS = "192.168.30.255";   //广播地址（thinkjoy路由）
        // public final static String UDP_BROADCAST_ADDRESS = "10.10.10.255";  //广播地址（广告机路由、未购买）
        public final static int UdpPort = 988;  //udp广播端口号
        public final static int TcpPort = 8080; //TCP连接端口号
        public final static String relayUUID = "HL00016261";    //WIFI继电器UUID


        /**
         * 蓝牙特征命令
         */
        public final static byte[] WRITE_OPEN_01 = new byte[8];
        public final static byte[] WRITE_CLOSE_01 = new byte[8];
        public final static byte[] WRITE_OPEN_02 = new byte[8];
        public final static byte[] WRITE_CLOSE_02 = new byte[8];
        public final static byte[] WRITE_ALLOPEN = new byte[8];
        public final static byte[] WRITE_ALLCLOSE = new byte[8];

        static {
            WRITE_OPEN_01[0] = 0x33;
            WRITE_OPEN_01[1] = 0x01;
            WRITE_OPEN_01[2] = 0x12;
            WRITE_OPEN_01[3] = 0x00;
            WRITE_OPEN_01[4] = 0x00;
            WRITE_OPEN_01[5] = 0x00;
            WRITE_OPEN_01[6] = 0x01;
            WRITE_OPEN_01[7] = 0x47;

            WRITE_CLOSE_01[0] = 0x33;
            WRITE_CLOSE_01[1] = 0x01;
            WRITE_CLOSE_01[2] = 0x11;
            WRITE_CLOSE_01[3] = 0x00;
            WRITE_CLOSE_01[4] = 0x00;
            WRITE_CLOSE_01[5] = 0x00;
            WRITE_CLOSE_01[6] = 0x01;
            WRITE_CLOSE_01[7] = 0x46;

            WRITE_OPEN_02[0] = 0x33;
            WRITE_OPEN_02[1] = 0x01;
            WRITE_OPEN_02[2] = 0x12;
            WRITE_OPEN_02[3] = 0x00;
            WRITE_OPEN_02[4] = 0x00;
            WRITE_OPEN_02[5] = 0x00;
            WRITE_OPEN_02[6] = 0x02;
            WRITE_OPEN_02[7] = 0x48;

            WRITE_CLOSE_02[0] = 0x33;
            WRITE_CLOSE_02[1] = 0x01;
            WRITE_CLOSE_02[2] = 0x11;
            WRITE_CLOSE_02[3] = 0x00;
            WRITE_CLOSE_02[4] = 0x00;
            WRITE_CLOSE_02[5] = 0x00;
            WRITE_CLOSE_02[6] = 0x02;
            WRITE_CLOSE_02[7] = 0x47;

            WRITE_ALLOPEN[0] = 0x33;
            WRITE_ALLOPEN[1] = 0x01;
            WRITE_ALLOPEN[2] = 0x14;
            WRITE_ALLOPEN[3] = 0x00;
            WRITE_ALLOPEN[4] = 0x00;
            WRITE_ALLOPEN[5] = 0x00;
            WRITE_ALLOPEN[6] = 0x00;
            WRITE_ALLOPEN[7] = 0x48;

            WRITE_ALLCLOSE[0] = 0x33;
            WRITE_ALLCLOSE[1] = 0x01;
            WRITE_ALLCLOSE[2] = 0x13;
            WRITE_ALLCLOSE[3] = 0x00;
            WRITE_ALLCLOSE[4] = 0x00;
            WRITE_ALLCLOSE[5] = 0x00;
            WRITE_ALLCLOSE[6] = 0x00;
            WRITE_ALLCLOSE[7] = 0x47;
        }
    }


    public static class Varible {
        public static boolean isBluetoothConnected = false; //判断蓝牙是否已连接
    }


    public static class UrlPath {
        //  ZMQ通讯蓝牙开门的地址
        public final static String ZMQ_BLUETOOTH_REQ_ADDRESS = "tcp://10.10.10.2:20000";
    }

}
