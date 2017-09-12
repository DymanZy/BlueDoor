package com.dyman.componentdoor.wifiRelay;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by thinkjoy on 2017/8/21.
 */

public class SendDataThread extends Thread {
    private Socket socket=null;
    private byte[] data =new byte[20];

    public SendDataThread(byte[] data)
    {
        this.data =data;
    }
    public SendDataThread(Socket socket)
    {
        this.socket=socket;
    }
    public SendDataThread(Socket socket, byte[] data)
    {
        this.data =data;
        this.socket=socket;
    }
    //	@Override
    public void run() {
        OutputStream output;
        try {
            output = socket.getOutputStream();
            output.write(data);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
