package com.yf.android.simpledome.wificonfig;

import java.net.InetAddress;

public interface IEsptouchResult {
    boolean isSuc();

    String getBssid();

    boolean isCancelled();

    InetAddress getInetAddress();
}
