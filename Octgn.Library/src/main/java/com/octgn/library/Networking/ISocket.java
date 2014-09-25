package com.octgn.library.Networking;

import java.io.IOException;

/**
 * Created by Kelly on 9/24/2014.
 */
public interface ISocket
{
    SocketStatus getStatus();
    String getIpAddress();
    int getPort();
    ISocketMessageProcessor getMessageProcessor();

    void Setup(String ipAddress, int port, ISocketMessageProcessor processor);
    void Connect() throws IOException;
    void Disconnect();
    void Send(byte[] data);

    void OnConnectionEvent(Object sender, SocketConnectionEvent e);
    void OnDataReceived(Object sender, byte[] data);
}

