package com.octgn.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Kelly on 9/24/2014.
 */
public class ClientSocket implements Runnable{
    private Socket mSocket;
    private final String mIp;
    private final int mPort;
    private Boolean mRunning;

    public ClientSocket(String ip, int port)
    {
        mIp = ip;
        mPort = port;
    }

    @Override
    public void run() {
        try {
            mRunning = true;
            InetAddress serverAddr = InetAddress.getByName(mIp);
            mSocket = new Socket(serverAddr,mPort);
            byte[] buffer = new byte[1024];
            while(mRunning) {
                int count = mSocket.getInputStream().read(buffer);
                if(count == -1)
                {

                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize(){
        mRunning = false;
        try{
            if(mSocket == null)
                return;
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
