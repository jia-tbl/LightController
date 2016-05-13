package com.yf.android.simpledome.wificonfig;

import com.yf.android.simpledome.wificonfig.uitl.ByteUtil;

public class GuideCode implements ICodeData {
    public static final int GUIDE_CODE_LEN = 4;

    public byte[] getBytes() {
        throw new RuntimeException("DataCode don\'t support getBytes()");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        char[] dataU8s = this.getU8s();

        for ( int i = 0; i < 4; ++i ) {
            String hexString = ByteUtil.convertU8ToHexString(dataU8s[i]);
            sb.append("0x");
            if (hexString.length() == 1) {
                sb.append("0");
            }

            sb.append(hexString).append(" ");
        }

        return sb.toString();
    }

    public char[] getU8s() {
        char[] guidesU8s = new char[]{'ȃ', 'Ȃ', 'ȁ', 'Ȁ'};
        return guidesU8s;
    }
}
