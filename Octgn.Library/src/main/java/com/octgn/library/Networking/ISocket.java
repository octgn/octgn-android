package com.octgn.library.Networking;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Created by Kelly on 9/24/2014.
 */
public interface ISocket
{
    SocketStatus getStatus();
    String getIpAddress();
    int getPort();
    ISocketMessageProcessor getMessageProcessor();

    void Setup(SocketAddress addr, ISocketMessageProcessor processor);
    void Connect() throws IOException;
    void Disconnect();
    void Send(byte[] data);

    void OnConnectionEvent(Object sender, SocketConnectionEvent e);
    void OnDataReceived(Object sender, byte[] data);
}

