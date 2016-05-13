package com.yf.android.simpledome.datasource;

import android.text.TextUtils;

import com.yf.android.simpledome.utils.DecodeByte;

import java.io.Serializable;

public class Scene implements Serializable {
    private int id;
    private int code;
    private String contCode;
    private String data;

    private byte[] integers = new byte[8];

    public byte[] getIntegers() {
        return integers;
    }

    /**
     * 8 byte means WCWCWCWC
     *
     * @param i index
     * @return
     */
    public int getBtnWC(int i) {
        return integers[i];
    }

    // 每次修改后，保存修改后的值
    public void setBtnWC(int i, int value) {
        integers[i] = (byte) value;
        //data = DecodeByte.bytesToString(integers);
    }

    /**
     * i=0,1,2,3
     * 0123 as btn
     *
     * @param i
     * @return
     */
    public boolean getBtnChecked(int i) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        if (i < 4) {
            return (integers[i * 2] + integers[i * 2 + 1]) > 0;
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContCode() {
        return contCode;
    }

    public void setContCode(String contCode) {
        this.contCode = contCode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        if (!TextUtils.isEmpty(data) && data.length() == 16) {
            this.integers = DecodeByte.hexStringToByteArray(data, 8);
        }
        this.data = data;
    }
}
