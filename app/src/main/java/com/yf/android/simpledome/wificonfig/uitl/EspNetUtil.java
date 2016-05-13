package com.yf.android.simpledome.wificonfig.uitl;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EspNetUtil {
    public EspNetUtil() {
    }

    public static InetAddress getLocalInetAddress(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wm.getConnectionInfo();
        int localAddrInt = wifiInfo.getIpAddress();
        String localAddrStr = __formatString(localAddrInt);
        InetAddress localInetAddr = null;

        try {
            localInetAddr = InetAddress.getByName(localAddrStr);
        } catch (UnknownHostException var7) {
            var7.printStackTrace();
        }

        return localInetAddr;
    }

    private static String __formatString(int value) {
        String strValue = "";
        byte[] ary = __intToByteArray(value);

        for ( int i = ary.length - 1; i >= 0; --i ) {
            strValue = strValue + (ary[i] & 255);
            if (i > 0) {
                strValue = strValue + ".";
            }
        }

        return strValue;
    }

    private static byte[] __intToByteArray(int value) {
        byte[] b = new byte[4];

        for ( int i = 0; i < 4; ++i ) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) (value >>> offset & 255);
        }

        return b;
    }

    public static InetAddress parseInetAddr(byte[] inetAddrBytes, int offset, int count) {
        InetAddress inetAddress = null;
        StringBuilder sb = new StringBuilder();

        for ( int e = 0; e < count; ++e ) {
            sb.append(Integer.toString(inetAddrBytes[offset + e] & 255));
            if (e != count - 1) {
                sb.append('.');
            }
        }

        try {
            inetAddress = InetAddress.getByName(sb.toString());
        } catch (UnknownHostException var6) {
            var6.printStackTrace();
        }

        return inetAddress;
    }

    public static byte[] parseBssid2bytes(String bssid) {
        String[] bssidSplits = bssid.split(":");
        byte[] result = new byte[bssidSplits.length];

        for ( int i = 0; i < bssidSplits.length; ++i ) {
            result[i] = (byte) Integer.parseInt(bssidSplits[i], 16);
        }

        return result;
    }
}
