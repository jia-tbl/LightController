package com.yf.android.simpledome.utils;

public class DecodeByte {

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static byte[] hexStringToByteArray(String hexString,int len) {
        hexString.toUpperCase();
        byte[] bs = new byte[len];
        for ( int i = 0; i < bs.length; i++ ) {
            char[] hexChars = new char[2];
            hexChars[0] = hexString.charAt(2 * i);
            hexChars[1] = hexString.charAt(2 * i + 1);
            bs[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bs;
    }

    public static byte[] hexStringToByteArray(String hexString) {
        hexString.toUpperCase();
        byte[] bs = new byte[4];
        for ( int i = 0; i < bs.length; i++ ) {
            char[] hexChars = new char[2];
            hexChars[0] = hexString.charAt(2 * i);
            hexChars[1] = hexString.charAt(2 * i + 1);
            bs[i] = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
        }
        return bs;
    }

    public static String bytesToString(byte[] bytes) {
        StringBuffer sBuffer = new StringBuffer();
        for ( int i = 0; i < bytes.length; i++ ) {
            String s = Integer.toHexString(bytes[i] & 0xff);
            if (s.length() < 2) {
                sBuffer.append('0');
            }
            sBuffer.append(s);
        }
        return sBuffer.toString().toUpperCase();
    }

    public static String bytesToString(byte by) {
        return Integer.toHexString(by & 0xff);
    }
}
