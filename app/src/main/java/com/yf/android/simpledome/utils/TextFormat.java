package com.yf.android.simpledome.utils;

import android.util.Log;

import java.util.Arrays;

public class TextFormat {
    /**
     * 检查 text 是否 符合服务器数据格式
     *
     * @param text
     * @return
     */
    public static boolean isTextAvailable(String text) {
        if (null == text) return false;
        if (text.contains("-")) return false;
        if (text.contains("\n")) return false;
        if (text.contains("\r")) return false;
        return true;
    }

    /**
     * 特殊字符集
     */
    private static Character[] regEx = {'·', '`', '~', '!', '@', '#', '$', '%',
            '^', '&', '*', '(', ')', '+', '=', '|', '\\', '{', '}', '【',
            '】', '\'', '"', '“', '”', ':', ';', ',', '<', '‘', '’', '[',
            ']', '.', '>', '/', '?', '（', '）', '；', '。', '、', '？',
            '—', ':', '￥', '！', '，', '…'};

    /**
     * 转义 特殊字符
     *
     * @param str
     * @return
     */
    public static String translateString(String str) {
        String temp = str;
        int count = 0;
        for ( int i = 0; i < str.length(); i++ ) {
            if (Arrays.asList(regEx).contains(str.charAt(i))) {
                temp = temp.substring(0, i + count) + "\\" + temp.substring(i + count);
                count++;
            }
        }
        Log.e("TRA", "--------translateString: " + temp);
        return temp;
    }
}
