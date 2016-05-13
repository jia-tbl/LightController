package com.yf.android.simpledome.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiAdmin {
    //定义WifiManager对象
    private WifiManager mWifiManager;

    //定义WifiInfo对象
    private WifiInfo mWifiInfo;

    //扫描出的网络连接列表
    private List<ScanResult> mWifiList;

    //网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;

    //定义一个WifiLock
    WifiManager.WifiLock mWifiLock;

    // 广播
    WifiManager.MulticastLock mMulticastLock;

    //构造器
    public WifiAdmin(Context context) {
        //取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        //取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    //打开WIFI
    public void OpenWifi(Context mContext) {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
            final ProgressDialog dialog = new ProgressDialog(mContext);
            dialog.setMessage("开启WIFI...");
            dialog.setCancelable(false);
            Timer timer = new Timer();
            dialog.show();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    dialog.cancel();
                }
            }, 3 * 1000);
        }
    }

    //关闭WIFI
    public void CloseWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    //锁定WifiLock
    public void AcquireWifiLock() {
        mWifiLock.acquire();
        mMulticastLock.acquire();
    }

    //解锁WifiLock
    public void ReleaseWifiLock() {
        //判断时候锁定
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        if (mMulticastLock.isHeld()) {
            mMulticastLock.release();
        }
    }

    //创建一个WifiLock
    public void CreatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
        // 取得MulticastLock对象
        mMulticastLock = mWifiManager.createMulticastLock("mMulticastLock");
    }

    //得到配置好的网络
    public List<WifiConfiguration> GetConfiguration() {
        return mWifiConfiguration;
    }

    //指定配置好的网络进行连接
    public void ConnectConfiguration(int index) {
        //索引大于配置好的网络索引返回
        if (index > mWifiConfiguration.size()) {
            return;
        }
        //连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }

    public void StartScan() {
        mWifiManager.startScan();
        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        //得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }

    //得到网络列表
    public List<ScanResult> GetWifiList() {
        return mWifiList;
    }

    //查看扫描结果
    public StringBuilder LookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();
        for ( int i = 0; i < mWifiList.size(); i++ ) {
            stringBuilder.append("Index_" + new Integer(i + 1).toString() + ":");
            //将ScanResult信息转换成一个字符串包
            //其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }

    //得到MAC地址
    public String GetMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    //得到接入点的SSID
    public String GetSSID() {
        if (mWifiInfo != null) {
            String ssid = mWifiInfo.getSSID();
            if (ssid != null && ssid.length() > 1) {
                return ssid.substring(1, ssid.length() - 1);
            }
        }
        return "";
    }

    //得到接入点的BSSID
    public String GetBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    //得到IP地址
    public int GetIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    //得到连接的ID
    public int GetNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    //得到WifiInfo的所有信息包
    public String GetWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }

    //添加一个网络并连接
    public void AddNetwork(WifiConfiguration wcg) {
        int wcgID = mWifiManager.addNetwork(wcg);
        mWifiManager.enableNetwork(wcgID, true);
    }

    //断开指定ID的网络
    public void DisconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }
}
