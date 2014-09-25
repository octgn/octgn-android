package com.octgn.library.Networking;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Kelly on 9/24/2014.
 */
public abstract class SocketMessageProcessorBase implements ISocketMessageProcessor {
    public Queue<byte[]> Messages = new LinkedList<byte[]>();
    public List<Byte> Buffer = new ArrayList<Byte>();

    public void AddData(byte[] data)
    {
        synchronized (this.Buffer)
        {
            for(int i = 0;i<data.length;i++)
            {
                this.Buffer.add((Byte) data[i]);
            }
            byte[] newBuff = new byte[this.Buffer.size()];
            for(int i = 0;i<this.Buffer.size();i++)
            {
                newBuff[i] = (byte)this.Buffer.get(i);
            }
            int messageCount = this.ProcessBuffer(newBuff);
            while (messageCount > 0)
            {
                if (messageCount > this.Buffer.size()) throw new IllegalStateException("Message count is greater than the available buffer size");
                //var nb = this.Buffer.GetRange(0, messageCount);
                byte[] nb = new byte[messageCount];
                for(int i = 0; i<messageCount;i++)
                {
                    nb[i] = this.Buffer.get(i);
                }
                this.Messages.add(nb);
                for(int i = 0;i<messageCount;i++)
                    this.Buffer.remove(0);

                newBuff = new byte[this.Buffer.size()];
                for(int i = 0;i<this.Buffer.size();i++)
                {
                    newBuff[i] = (byte)this.Buffer.get(i);
                }
                messageCount = this.ProcessBuffer(newBuff);
            }
        }
    }

    public byte[] PopMessage()
    {
        synchronized (this.Buffer)
        {
            return this.Messages.size() == 0 ? null : this.Messages.remove();
        }
    }

    public void Clear()
    {
        synchronized (this.Buffer)
        {
            this.Buffer.clear();
            this.Messages.clear();
        }
    }

    public abstract int ProcessBuffer(byte[] data);
}
