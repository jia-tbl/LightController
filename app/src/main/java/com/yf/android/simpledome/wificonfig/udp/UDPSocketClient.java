package com.yf.android.simpledome.wificonfig.udp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPSocketClient {
    private static final String TAG = "UDPSocketClient";
    private DatagramSocket mSocket;
    private volatile boolean mIsStop;
    private volatile boolean mIsClosed;

    public UDPSocketClient() {
        try {
            this.mSocket = new DatagramSocket();
            this.mIsStop = false;
            this.mIsClosed = false;
        } catch (SocketException var2) {
            Log.e("UDPSocketClient", "SocketException");
            var2.printStackTrace();
        }

    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    public void interrupt() {
        Log.i("UDPSocketClient", "USPSocketClient is interrupt");
        this.mIsStop = true;
    }

    public synchronized void close() {
        if (!this.mIsClosed) {
            this.mSocket.close();
            this.mIsClosed = true;
        }

    }

    public void sendData(byte[][] data, String targetHostName, int targetPort, long interval) {
        this.sendData(data, 0, data.length, targetHostName, targetPort, interval);
    }

    public void sendData(byte[][] data, int offset, int count, String targetHostName, int targetPort, long interval) {
        if (data != null && data.length > 0) {
            for ( int i = offset; !this.mIsStop && i < offset + count; ++i ) {
                if (data[i].length != 0) {
                    try {

                        DatagramPacket e = new DatagramPacket(data[i], data[i].length, InetAddress.getByName(targetHostName), targetPort);

                        Log.e("TAG", "------------NUB:" + i + "--------LEN:" + data[i].length);
                        Log.e("TAG", InetAddress.getByName(targetHostName) + "-------P:" + targetPort);
                        // "\n----------------DATA:" + getDataLog(data[i]));

                        this.mSocket.send(e);

                    } catch (UnknownHostException var11) {
                        Log.e("UDPSocketClient", "sendData(): UnknownHostException");
                        var11.printStackTrace();
                        this.mIsStop = true;
                        break;
                    } catch (IOException var12) {
                        Log.e("UDPSocketClient", "sendData(): IOException, but just ignore it");
                    }

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException var10) {
                        var10.printStackTrace();
                        Log.e("UDPSocketClient", "sendData is Interrupted");
                        this.mIsStop = true;
                        break;
                    }
                }
            }

            if (this.mIsStop) {
                this.close();
            }

        } else {
            Log.e("UDPSocketClient", "sendData(): data == null or length <= 0");
        }
    }

    private String getDataLog(byte[] c) {
        String str = c[0] + "";
        for ( int i = 0; i < c.length; i++ ) {
            str += "," + c[i];
        }
        return str;
    }
}
