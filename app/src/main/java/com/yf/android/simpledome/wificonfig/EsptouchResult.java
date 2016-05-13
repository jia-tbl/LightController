package com.yf.android.simpledome.wificonfig;


import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class EsptouchResult implements IEsptouchResult {
    private final boolean mIsSuc;
    private final String mBssid;
    private final InetAddress mInetAddress;
    private AtomicBoolean mIsCancelled;

    public EsptouchResult(boolean isSuc, String bssid, InetAddress inetAddress) {
        this.mIsSuc = isSuc;
        this.mBssid = bssid;
        this.mInetAddress = inetAddress;
        this.mIsCancelled = new AtomicBoolean(false);
    }

    public boolean isSuc() {
        return this.mIsSuc;
    }

    public String getBssid() {
        return this.mBssid;
    }

    public boolean isCancelled() {
        return this.mIsCancelled.get();
    }

    public void setIsCancelled(boolean isCancelled) {
        this.mIsCancelled.set(isCancelled);
    }

    public InetAddress getInetAddress() {
        return this.mInetAddress;
    }
}
