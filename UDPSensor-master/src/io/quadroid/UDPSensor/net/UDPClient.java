package io.quadroid.UDPSensor.net;

import android.os.AsyncTask;
import android.os.Build;

import java.io.IOException;
import java.net.*;

/**
 * UDPSensor
 * User: Matthias Nagel
 * Date: 26.11.13
 * http://www.quadroid.io
 */
/*public class UDPClient {

    private static UDPClient mInstance;
    private InetAddress mInetAddress;
    private int mPort;
    private DatagramSocket mDatagramSocket;
    private DatagramPacket mDatagramPacket;
    private String bufferString;

    public static UDPClient getInstance() throws SocketException {

        if (mInstance == null) {
            mInstance = new UDPClient();
        }

        return mInstance;
    }

    private UDPClient() throws SocketException {
        this.mDatagramSocket = new DatagramSocket();
        this.bufferString = "";
    }

    public void sendPacket( float a0, float a1, float a2, float g0, float g1, float g2, float m0, float m1, float m2, float b) {


        bufferString += String.format("%.4f", a0);
        bufferString += "; ";
        bufferString += String.format("%.4f", a1);
        bufferString += "; ";
        bufferString += String.format("%.4f", a2);
        bufferString += "; ";
        bufferString += String.format("%.4f", g0);
        bufferString += "; ";
        bufferString += String.format("%.4f", g1);
        bufferString += "; ";
        bufferString += String.format("%.4f", g2);
        bufferString += "; ";
        bufferString += String.format("%.2f", m0);
        bufferString += "; ";
        bufferString += String.format("%.2f", m1);
        bufferString += "; ";
        bufferString += String.format("%.2f", m2);
        bufferString += "; ";
        bufferString += String.format("%.4f", b);

        bufferString = bufferString.replace(",", ".");

        this.mDatagramPacket = new DatagramPacket(this.bufferString.getBytes(), this.bufferString.getBytes().length, this.mInetAddress, this.mPort);
        try {
            this.mDatagramSocket.send(this.mDatagramPacket);
            this.mDatagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void setInetAddress(String inetAddress) throws UnknownHostException {
        this.mInetAddress = InetAddress.getByName(inetAddress);
    }

    public void setPort(int port) {
        this.mPort = port;
    }
}*/

import java.net.DatagramPacket;
        import java.net.DatagramSocket;
        import android.annotation.SuppressLint;
        import android.os.AsyncTask;
        import android.os.Build;
        import java.net.InetAddress;

public class UDPClient {
    private AsyncTask<Void, Void, Void> task;
    String m_message;
    InetAddress m_address;
    int m_port;

    public void Send(InetAddress address, int port, String msg) {
        // store the details so we can get to them later via async task
        m_message = msg;
        m_address = address;
        m_port = port;

        task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                DatagramSocket ds = null;
                try {
                    ds = new DatagramSocket();
                    DatagramPacket dp;
                    dp = new DatagramPacket(m_message.getBytes(),
                            m_message.length(),
                            m_address, m_port);
                    ds.setBroadcast(true);
                    ds.send(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (ds != null) {
                        ds.close();
                    }
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        };

        if (Build.VERSION.SDK_INT >= 11) task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else task.execute();
    }

}