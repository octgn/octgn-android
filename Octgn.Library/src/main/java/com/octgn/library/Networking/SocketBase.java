package com.octgn.library.Networking;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

/**
 * Created by Kelly on 9/24/2014.
 */
public abstract class SocketBase implements ISocket {
    //internal ILog Log;
    private SocketStatus mStatus;
    private SocketAddress mEndPoint;
    private ISocketMessageProcessor mMessageProcessor;
    private Socket mClient;
    protected Boolean mFirstConnection = true;

    public SocketStatus getStatus(){return mStatus;}
    public SocketAddress getEndPoint(){return mEndPoint;}
    public ISocketMessageProcessor getMessageProcessor(){return mMessageProcessor;}
    public Socket getClient(){return mClient;}

    //protected SocketBase(ILog log)
    //{
    //    this.Log = log;
    //}

    @Override
    public void Setup(SocketAddress ep,ISocketMessageProcessor processor)
    {
        synchronized (this)
        {
            if (ep == null) throw new IllegalArgumentException("ep");
            if (processor == null) throw new IllegalArgumentException("processor");
            if (this.mStatus != SocketStatus.Disconnected) throw new IllegalStateException("You can't setup a socket if it isn't disconnected.");
            //Log.DebugFormat("Setup {0}", ep);
            this.mEndPoint = ep;
            if (this.mClient != null)
            {
                try { this.mClient.close(); }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            this.mClient = new Socket();
            this.mMessageProcessor = processor;
        }
    }

    public void Setup(Socket client, ISocketMessageProcessor processor)
    {
        synchronized (this)
        {
            if(client == null)throw new IllegalArgumentException("client");
            if (processor == null) throw new IllegalArgumentException("processor");
            if (this.mStatus != SocketStatus.Disconnected) throw new IllegalStateException("You can't setup a socket if it isn't disconnected.");
            //Log.DebugFormat("Setup {0}",client.Client.RemoteEndPoint);
            this.mEndPoint = client.getRemoteSocketAddress();//Client.RemoteEndPoint as IPEndPoint;
            this.mMessageProcessor = processor;
            this.mClient = client;
            this.mStatus = SocketStatus.Connected;
        }
        this.CallOnConnectionEvent(this.mFirstConnection ? SocketConnectionEvent.Connected : SocketConnectionEvent.Reconnected);
        this.mFirstConnection = false;
        startReceive();
    }

    public void Connect() throws IOException {
        synchronized (this)
        {
            if (this.mEndPoint == null) throw new IllegalStateException("EndPoint must be set.");
            if (this.mStatus != SocketStatus.Disconnected) throw new IllegalStateException("You can't connect if the socket isn't disconnected");
            //Log.Debug("Connect");
            if (this.mClient.isClosed())
            {
                this.mClient = new Socket();
            }
            this.mClient.connect(this.mEndPoint);
            this.mStatus = SocketStatus.Connected;
        }
        this.CallOnConnectionEvent(this.mFirstConnection ? SocketConnectionEvent.Connected : SocketConnectionEvent.Reconnected);
        this.mFirstConnection = false;
        startReceive();
    }

    public void Disconnect()
    {
        synchronized (this)
        {
            if (this.mStatus == SocketStatus.Disconnected) return;
            this.mStatus = SocketStatus.Disconnected;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Log.Debug("OnDisconnect");
        try { this.mClient.close(); }
        catch (IOException e) {
            e.printStackTrace();
        }
        //this.Client = null;
        this.mMessageProcessor.Clear();
        this.CallOnConnectionEvent(SocketConnectionEvent.Disconnected);
    }

    public void Send(byte[] data)
    {
        try
        {
            this.mClient.getOutputStream().write(data);
        }
        catch (Exception e)
        {
            //Log.Warn("Send", e);
            Disconnect();
        }
    }

    public void CallOnConnectionEvent(SocketConnectionEvent args)
    {
        try
        {
            //Log.DebugFormat("CallOnConnectionEvent {0}", args);
            this.OnConnectionEvent(this, args);
        }
        catch (Exception e)
        {
            //Log.Error("CallOnConnectionEvent Error", e);
        }
    }

    public void CallOnDataReceived(byte[] data)
    {
        if (data == null) throw new IllegalArgumentException("data");
        try
        {
            //Log.DebugFormat("CallOnDataReceived {0} bytes", data.Length);
            this.OnDataReceived(this, data);
        }
        catch (Exception e)
        {
            //Log.Error("CallOnDataReceived Error", e);
        }
    }

    public void EndReceive(SocketReceiveBundle state)
    {
        int count = state.Count;
        if (count <= 0)
        {
            this.Disconnect();
            return;
        }
        byte[] newArray = new byte[count];
        for(int i = 0;i<count;i++)
        {
            newArray[i] = state.Buffer[i];
        }
        this.mMessageProcessor.AddData(newArray);

        while (true)
        {
            byte[] buff = this.mMessageProcessor.PopMessage();
            if (buff == null) break;
            this.CallOnDataReceived(buff);
        }
        startReceive();
    }

    private void startReceive()
    {
        SocketReaderTask task = new SocketReaderTask();
        task.execute(this.mClient);
    }

    public abstract void OnConnectionEvent(Object sender, SocketConnectionEvent e);

    public abstract void OnDataReceived(Object sender, byte[] data);

    @Override
    public void finalize()
    {
        try
        {
            this.mClient.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.mClient = null;
        this.mEndPoint = null;
    }
    class SocketReaderTask extends AsyncTask<Socket,Void,SocketReceiveBundle> {
        boolean mCancel;
        @Override
        protected SocketReceiveBundle doInBackground(Socket... socks) {
            Socket sock = socks[0];
            SocketReceiveBundle ret = new SocketReceiveBundle(socks[0]);
            while(sock.isConnected() && mCancel == false){
                try {
                    ret.Count = sock.getInputStream().read(ret.Buffer);
                    if (ret.Count == 0)
                        Thread.sleep(100);
                    else{
                        return ret;
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(SocketReceiveBundle data)
        {
            EndReceive(data);
        }

        @Override
        protected void onCancelled(){
            mCancel = true;
        }
    }
}

class SocketReceiveBundle
{
    public final int BufferSize = 1024;
    public byte[] Buffer = new byte[BufferSize];
    public Socket Client;
    public int Count;

    public SocketReceiveBundle(Socket client)
    {
        this.Client = client;
        this.Count = 0;
    }

    @Override
    public void finalize()
    {
        this.Client = null;
        this.Buffer = null;
    }
}
