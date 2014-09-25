package com.octgn.library.Networking;

import android.os.AsyncTask;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Kelly on 9/24/2014.
 */
public abstract class ReconnectingSocketBase extends SocketBase{
    public int MaxRetryCount;
    public int RetryCount;
    public boolean Reconnecting;
    public int TimeoutTime;
    public boolean ForcedDisconnect = false;

    private boolean mReportedDisconnect = false;

    protected ReconnectingSocketBase(int maxRetryCount, int timeoutTime)
    {
        if (maxRetryCount < 0) maxRetryCount = 0;
        TimeoutTime = timeoutTime;
        if (timeoutTime == 0)
            TimeoutTime = Integer.MAX_VALUE;
        this.MaxRetryCount = maxRetryCount;
        this.RetryCount = 0;
        this.Reconnecting = false;
        this.mReportedDisconnect = false;
    }

    public void ForceDisconnect()
    {
        this.ForcedDisconnect = true;
        this.Reconnecting = false;
        this.Disconnect();
        this.ForcedDisconnect = false;
        this.mReportedDisconnect = false;
    }

    @Override
    public void OnConnectionEvent(Object sender, SocketConnectionEvent e)
    {
        //Log.DebugFormat("OnConnectionEvent {0}", e);
        if (e == SocketConnectionEvent.Disconnected && !this.ForcedDisconnect)
        {
            if (mReportedDisconnect == false)
            {
                mReportedDisconnect = true;
                //Log.ErrorFormat("Disconnect Event {0}",this.EndPoint);
            }
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    DoReconnect();
                    return null;
                }
            };
            task.execute();
            //task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            return;
        }
        if (e != SocketConnectionEvent.Disconnected)
        {
            this.RetryCount = 0;
            this.Reconnecting = false;
            this.ForcedDisconnect = false;
            this.mReportedDisconnect = false;
        }
    }

    protected void DoReconnect()
    {
        if (this.ForcedDisconnect) return;
        //Log.Debug("DoReconnect");
        this.Reconnecting = true;
        long endTime = new Date(0).getTime() + TimeoutTime;
        while (this.RetryCount < this.MaxRetryCount
                || this.MaxRetryCount == 0
                || new Date(0).getTime() < endTime)
                //|| new TimeSpan(DateTime.Now.Ticks - startTime.Ticks).TotalSeconds < TimeoutTime.TotalSeconds)
        {
            try
            {
                if (this.Reconnecting == false)
                {
                    //Log.Debug("DoReconnect Finished due to ForceDisconnect");
                    break;
                }
                //Log.Debug("DoReconnect Trying to reconnect");
                this.Connect();
                break;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                //Log.Warn("DoReconnect", e);
            }
            this.RetryCount++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.Reconnecting = false;
        this.RetryCount = 0;
    }
}
