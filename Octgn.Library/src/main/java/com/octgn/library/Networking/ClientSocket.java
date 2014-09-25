package com.octgn.library.Networking;

import android.renderscript.RSInvalidStateException;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by Kelly on 9/24/2014.
 */
public class ClientSocket extends ReconnectingSocketBase {
    //internal IServerCalls Rpc { get; set; }
    //internal Handler Handler { get; set; }

    public int Muted;
    private SocketAddress mAddress;

    public ClientSocket(SocketAddress address)
    {
        super(0,0);
        mAddress = address;
        this.Setup(address, new ClientMessageProcessor());
        try {
            this.getClient().setSoTimeout(4000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        //Handler = new Handler();
        //Rpc = new BinarySenderStub(this);
    }

    @Override
    public String getIpAddress() {
        return ((InetSocketAddress)mAddress).getAddress().toString();
    }

    @Override
    public int getPort() {
        return ((InetSocketAddress)mAddress).getPort();
    }

    @Override
    public void OnConnectionEvent(Object sender, SocketConnectionEvent e)
    {
        super.OnConnectionEvent(sender, e);
        switch (e)
        {
            case Disconnected:
                //if (Program.GameEngine != null)
                //    Program.GameEngine.IsConnected = false;
                //Program.GameMess.Warning("You have been disconnected from server.");
                break;
            case Connected:
                //if (Program.GameEngine != null)
                //    Program.GameEngine.IsConnected = true;
                break;
            case Reconnected:
                //if (Program.GameEngine != null)
                //{
                //    Program.GameEngine.IsConnected = true;
                //    Program.GameEngine.Resume();
                //}
                //Program.GameMess.System("You have reconnected");

                break;
            default:
                throw new IllegalStateException("e");
        }
    }

    @Override
    public void OnDataReceived(Object sender, byte[] data)
    {
        //Program.Dispatcher.BeginInvoke(new Action(() =>
        //        {
        //                Handler.ReceiveMessage(data.Skip(4).ToArray());
        //}));
    }

    public void StartPings() throws NoSuchMethodException {
        //Log.Debug("StartPings");
        //Task.Factory.StartNew(
        //        () =>
        //        {
        //while (Status == SocketStatus.Connected)
        //{
        //    Rpc.Ping();
        //    Thread.Sleep(2000);
        //}
        //Log.Debug("StartPings Done Pinging");
        //});
    }
    public class ClientMessageProcessor extends SocketMessageProcessorBase
    {
        @Override
        public int ProcessBuffer(byte[] data)
        {
            if (data.length < 4) return 0;
            int length = data[0] | data[1] << 8 | data[2] << 16 | data[3] << 24;
            if (data.length < length) return 0;
            return length;
        }
    }
}
