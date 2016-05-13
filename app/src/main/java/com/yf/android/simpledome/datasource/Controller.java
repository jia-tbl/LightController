package com.yf.android.simpledome.datasource;

import android.text.TextUtils;

import com.yf.android.simpledome.utils.DecodeByte;

import java.io.Serializable;

public class Controller implements Serializable {
    private int id;

    private String contName;
    private String contData;
    private String contCode;

    private String btnName1;
    private String btnName2;
    private String btnName3;
    private String btnName4;

    private int btnScene1;
    private int btnScene2;
    private int btnScene3;
    private int btnScene4;

    private int contType;
    private int contOnline;
    private String optData;

    private byte[] integers = new byte[8];

    private byte[] dataArray = new byte[8];

    public byte getDataBtnWC(int i) {
        return dataArray[i];
    }

    public byte[] getDataArray() {
        return dataArray;
    }

    public void setDataBtnWC(int i, int value) {
        dataArray[i] = (byte) value;
    }

    public byte[] getIntegers() {
        return integers;
    }

    /**
     * 8 byte means WCWCWCWC
     *
     * @param i index
     * @return
     */
    public int getContBtnWC(int i) {
        return integers[i];
    }

    public void setContBtnWC(int i, int value) {
        integers[i] = (byte) value;
    }

    /**
     * i=0,1,2,3,4
     * 0123 as btn ,4 as all
     *
     * @param i
     * @return
     */
    public boolean getBtnLighted(int i) {
        if (TextUtils.isEmpty(contData)) {
            return false;
        }
        if (i < 4 && i >= 0) {
            return integers[i * 2] + integers[i * 2 + 1] > 1;
        } else if (i == 4) {
            return getBtnLighted(0) && getBtnLighted(1) &&
                    getBtnLighted(2) && getBtnLighted(3);
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getContOnline() {
        return contOnline;
    }

    public void setContOnline(int contOnline) {
        this.contOnline = contOnline;
    }

    public int getContType() {
        return contType;
    }

    public void setContType(int contType) {
        this.contType = contType;
    }

    public int getBtnScene4() {
        return btnScene4;
    }

    public void setBtnScene4(int btnScene4) {
        this.btnScene4 = btnScene4;
    }

    public int getBtnScene3() {
        return btnScene3;
    }

    public void setBtnScene3(int btnScene3) {
        this.btnScene3 = btnScene3;
    }

    public int getBtnScene2() {
        return btnScene2;
    }

    public void setBtnScene2(int btnScene2) {
        this.btnScene2 = btnScene2;
    }

    public String getBtnName4() {
        return btnName4;
    }

    public void setBtnName4(String btnName4) {
        this.btnName4 = btnName4;
    }

    public int getBtnScene1() {
        return btnScene1;
    }

    public void setBtnScene1(int btnScene1) {
        this.btnScene1 = btnScene1;
    }

    public String getBtnName3() {
        return btnName3;
    }

    public void setBtnName3(String btnName3) {
        this.btnName3 = btnName3;
    }

    public String getBtnName2() {
        return btnName2;
    }

    public void setBtnName2(String btnName2) {
        this.btnName2 = btnName2;
    }

    public String getBtnName1() {
        return btnName1;
    }

    public void setBtnName1(String btnName1) {
        this.btnName1 = btnName1;
    }

    public String getContCode() {
        return contCode;
    }

    public void setContCode(String contCode) {
        this.contCode = contCode;
    }

    public String getContData() {
        return contData;
    }

    public void setContData(String contData) {
        if (!TextUtils.isEmpty(contData) && contData.length() == 16) {
            this.dataArray = DecodeByte.hexStringToByteArray(contData, 8);
        }
        this.contData = contData;
    }

    public String getContName() {
        return contName;
    }

    public void setContName(String contName) {
        this.contName = contName;
    }

    public String getOptData() {
        return optData;
    }

    public void setOptData(String optData) {
        if (!TextUtils.isEmpty(optData) && optData.length() == 16) {
            this.integers = DecodeByte.hexStringToByteArray(optData, 8);
        }
        this.optData = optData;
    }
}
