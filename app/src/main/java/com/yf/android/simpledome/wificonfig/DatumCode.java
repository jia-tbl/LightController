package com.yf.android.simpledome.wificonfig;

import com.yf.android.simpledome.wificonfig.uitl.ByteUtil;
import com.yf.android.simpledome.wificonfig.uitl.CRC8;
import com.yf.android.simpledome.wificonfig.uitl.DataCode;
import com.yf.android.simpledome.wificonfig.uitl.EspNetUtil;

import java.net.InetAddress;

public class DatumCode implements ICodeData {
    private static final int EXTRA_LEN = 40;
    private static final int EXTRA_HEAD_LEN = 5;
    private final DataCode[] mDataCodes;

    public DatumCode(String apSsid, String apBssid, String apPassword, InetAddress ipAddress, boolean isSsidHiden) {
        byte totalXor = 0;
        char apPwdLen = (char) ByteUtil.getBytesByString(apPassword).length;

        CRC8 crc = new CRC8();
        crc.update(ByteUtil.getBytesByString(apSsid));
        char apSsidCrc = (char) ((int) crc.getValue());

        crc.reset();

        crc.update(EspNetUtil.parseBssid2bytes(apBssid));

        char apBssidCrc = (char) ((int) crc.getValue());

        char apSsidLen = (char) ByteUtil.getBytesByString(apSsid).length;
        String[] ipAddrStrs = ipAddress.getHostAddress().split("\\.");
        int ipLen = ipAddrStrs.length;
        char[] ipAddrChars = new char[ipLen];

        for ( int _totalLen = 0; _totalLen < ipLen; ++_totalLen ) {
            ipAddrChars[_totalLen] = (char) Integer.parseInt(ipAddrStrs[_totalLen]);
        }

        char var23 = (char) (5 + ipLen + apPwdLen + apSsidLen);

        char totalLen = isSsidHiden ? (char) (5 + ipLen + apPwdLen + apSsidLen) : (char) (5 + ipLen + apPwdLen);

        this.mDataCodes = new DataCode[totalLen];
        this.mDataCodes[0] = new DataCode(var23, 0);
        char var22 = (char) (totalXor ^ var23);
        this.mDataCodes[1] = new DataCode(apPwdLen, 1);
        var22 ^= apPwdLen;
        this.mDataCodes[2] = new DataCode(apSsidCrc, 2);
        var22 ^= apSsidCrc;
        this.mDataCodes[3] = new DataCode(apBssidCrc, 3);
        var22 ^= apBssidCrc;
        this.mDataCodes[4] = null;

        for ( int apPwdBytes = 0; apPwdBytes < ipLen; ++apPwdBytes ) {
            this.mDataCodes[apPwdBytes + 5] = new DataCode(ipAddrChars[apPwdBytes], apPwdBytes + 5);
            var22 ^= ipAddrChars[apPwdBytes];
        }

        byte[] var24 = ByteUtil.getBytesByString(apPassword);
        char[] apPwdChars = new char[var24.length];

        int apSsidBytes;
        for ( apSsidBytes = 0; apSsidBytes < var24.length; ++apSsidBytes ) {
            apPwdChars[apSsidBytes] = ByteUtil.convertByte2Uint8(var24[apSsidBytes]);
        }

        for ( apSsidBytes = 0; apSsidBytes < apPwdChars.length; ++apSsidBytes ) {
            this.mDataCodes[apSsidBytes + 5 + ipLen] = new DataCode(apPwdChars[apSsidBytes], apSsidBytes + 5 + ipLen);
            var22 ^= apPwdChars[apSsidBytes];
        }

        byte[] var25 = ByteUtil.getBytesByString(apSsid);
        char[] apSsidChars = new char[var25.length];

        int i;
        for ( i = 0; i < var25.length; ++i ) {
            apSsidChars[i] = ByteUtil.convertByte2Uint8(var25[i]);
            var22 ^= apSsidChars[i];
        }

        if (isSsidHiden) {
            for ( i = 0; i < apSsidChars.length; ++i ) {
                this.mDataCodes[i + 5 + ipLen + apPwdLen] = new DataCode(apSsidChars[i], i + 5 + ipLen + apPwdLen);
            }
        }

        this.mDataCodes[4] = new DataCode(var22, 4);
    }

    public byte[] getBytes() {
        byte[] datumCode = new byte[this.mDataCodes.length * 6];

        for ( int i = 0; i < this.mDataCodes.length; ++i ) {
            System.arraycopy(this.mDataCodes[i].getBytes(), 0, datumCode, i * 6, 6);
        }

        return datumCode;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        byte[] dataBytes = this.getBytes();

        for ( int i = 0; i < dataBytes.length; ++i ) {
            String hexString = ByteUtil.convertByte2HexString(dataBytes[i]);
            sb.append("0x");
            if (hexString.length() == 1) {
                sb.append("0");
            }

            sb.append(hexString).append(" ");
        }

        return sb.toString();
    }

    public char[] getU8s() {
        byte[] dataBytes = this.getBytes();
        int len = dataBytes.length / 2;
        char[] dataU8s = new char[len];

        for ( int i = 0; i < len; ++i ) {
            byte high = dataBytes[i * 2];
            byte low = dataBytes[i * 2 + 1];
            dataU8s[i] = (char) (ByteUtil.combine2bytesToU16(high, low) + 40);
        }

        return dataU8s;
    }
}
