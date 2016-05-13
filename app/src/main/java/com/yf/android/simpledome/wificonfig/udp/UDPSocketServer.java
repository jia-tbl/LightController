package com.yf.android.simpledome.wificonfig.udp;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPSocketServer {
    private static final String TAG = "UDPSocketServer";
    private DatagramPacket mReceivePacket;
    private DatagramSocket mServerSocket;
    private Context mContext;
    private WifiManager.MulticastLock mLock;
    private final byte[] buffer;
    private volatile boolean mIsClosed;

    private synchronized void acquireLock() {
        if (this.mLock != null && !this.mLock.isHeld()) {
            this.mLock.acquire();
        }

    }

    private synchronized void releaseLock() {
        if (this.mLock != null && this.mLock.isHeld()) {
            try {
                this.mLock.release();
            } catch (Throwable var2) {
                ;
            }
        }

    }

    public UDPSocketServer(int port, int socketTimeout, Context context) {
        this.mContext = context;
        this.buffer = new byte[64];
        this.mReceivePacket = new DatagramPacket(this.buffer, 64);

        try {
            this.mServerSocket = new DatagramSocket(port);
            this.mServerSocket.setSoTimeout(socketTimeout);
            this.mIsClosed = false;
            WifiManager e = (WifiManager) this.mContext.getSystemService("wifi");
            this.mLock = e.createMulticastLock("test wifi");
            Log.d("UDPSocketServer", "mServerSocket is created, socket read timeout: " + socketTimeout + ", port: " + port);
        } catch (IOException var5) {
            Log.e("UDPSocketServer", "IOException");
            var5.printStackTrace();
        }

    }

    public boolean setSoTimeout(int timeout) {
        try {
            this.mServerSocket.setSoTimeout(timeout);
            return true;
        } catch (SocketException var3) {
            var3.printStackTrace();
            return false;
        }
    }

    public byte receiveOneByte() {
        Log.d("UDPSocketServer", "receiveOneByte() entrance");

        try {
            this.acquireLock();
            this.mServerSocket.receive(this.mReceivePacket);
            Log.d("UDPSocketServer", "receive: " + (0 + this.mReceivePacket.getData()[0]));
            return this.mReceivePacket.getData()[0];
        } catch (IOException var2) {
            var2.printStackTrace();
            return (byte) -128;
        }
    }

    public byte[] receiveSpecLenBytes(int len) {
        Log.d("UDPSocketServer", "receiveSpecLenBytes() entrance: len = " + len);

        try {
            this.acquireLock();
            this.mServerSocket.receive(this.mReceivePacket);
            byte[] e = Arrays.copyOf(this.mReceivePacket.getData(), this.mReceivePacket.getLength());
            Log.d("UDPSocketServer", "received len : " + e.length);

            for ( int i = 0; i < e.length; ++i ) {
                Log.e("UDPSocketServer", "recDatas[" + i + "]:" + e[i]);
            }

            Log.e("UDPSocketServer", "receiveSpecLenBytes: " + new String(e));
            if (e.length != len) {
                Log.w("UDPSocketServer", "received len is different from specific len, return null");
                return null;
            } else {
                return e;
            }
        } catch (IOException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public void interrupt() {
        Log.i("UDPSocketServer", "USPSocketServer is interrupt");
        this.close();
    }

    public synchronized void close() {
        if (!this.mIsClosed) {
            Log.e("UDPSocketServer", "mServerSocket is closed");
            this.mServerSocket.close();
            this.releaseLock();
            this.mIsClosed = true;
        }

    }

    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
