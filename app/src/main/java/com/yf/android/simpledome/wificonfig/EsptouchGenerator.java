package com.yf.android.simpledome.wificonfig;

import com.yf.android.simpledome.wificonfig.uitl.ByteUtil;

import java.net.InetAddress;

public class EsptouchGenerator implements IEsptouchGenerator {
    private final byte[][] mGcBytes2;
    private final byte[][] mDcBytes2;

    public EsptouchGenerator(String apSsid, String apBssid, String apPassword, InetAddress inetAddress, boolean isSsidHiden) {
        GuideCode gc = new GuideCode();
        char[] gcU81 = gc.getU8s();
        this.mGcBytes2 = new byte[gcU81.length][];

        for ( int i = 0; i < this.mGcBytes2.length; ++i ) {
            this.mGcBytes2[i] = ByteUtil.genSpecBytes(gcU81[i]);
        }

        DatumCode var11 = new DatumCode(apSsid, apBssid, apPassword, inetAddress, isSsidHiden);
        char[] dcU81 = var11.getU8s();
        this.mDcBytes2 = new byte[dcU81.length][];

        for ( int i = 0; i < this.mDcBytes2.length; ++i ) {
            this.mDcBytes2[i] = ByteUtil.genSpecBytes(dcU81[i]);
        }
    }

    private String getDCbyte1(char[] c) {
        String str = c[0] + "";
        for ( int i = 0; i < c.length; i++ ) {
            str += "," + c[i];
        }
        return str;
    }

    public byte[][] getGCBytes2() {
        return this.mGcBytes2;
    }

    public byte[][] getDCBytes2() {
        return this.mDcBytes2;
    }
}
