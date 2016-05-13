package com.yf.android.simpledome.wificonfig;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.yf.android.simpledome.wificonfig.udp.UDPSocketClient;
import com.yf.android.simpledome.wificonfig.udp.UDPSocketServer;
import com.yf.android.simpledome.wificonfig.uitl.ByteUtil;
import com.yf.android.simpledome.wificonfig.uitl.EspNetUtil;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class __EsptouchTask implements __IEsptouchTask {
    private static final int ONE_DATA_LEN = 3;
    private static final String TAG = "EsptouchTask";
    private volatile List<IEsptouchResult> mEsptouchResultList;
    private volatile boolean mIsSuc = false;
    private volatile boolean mIsInterrupt = false;
    private volatile boolean mIsExecuted = false;
    private final UDPSocketClient mSocketClient;
    private final UDPSocketServer mSocketServer;
    private final String mApSsid;
    private final String mApBssid;
    private final boolean mIsSsidHidden;
    private final String mApPassword;
    private final Context mContext;
    private AtomicBoolean mIsCancelled;
    private IEsptouchTaskParameter mParameter;
    private volatile Map<String, Integer> mBssidTaskSucCountMap;

    public __EsptouchTask(String apSsid, String apBssid, String apPassword, Context context, IEsptouchTaskParameter parameter, boolean isSsidHidden) {
        if (TextUtils.isEmpty(apSsid)) {
            throw new IllegalArgumentException("the apSsid should be null or empty");
        } else {
            if (apPassword == null) {
                apPassword = "";
            }

            this.mContext = context;
            this.mApSsid = apSsid;
            this.mApBssid = apBssid;
            this.mApPassword = apPassword;
            this.mIsCancelled = new AtomicBoolean(false);
            this.mSocketClient = new UDPSocketClient();
            this.mParameter = parameter;
            this.mSocketServer = new UDPSocketServer(this.mParameter.getPortListening(), this.mParameter.getWaitUdpTotalMillisecond(), context);
            this.mIsSsidHidden = isSsidHidden;
            this.mEsptouchResultList = new ArrayList();
            this.mBssidTaskSucCountMap = new HashMap();
        }
    }

    private void __putEsptouchResult(boolean isSuc, String bssid, InetAddress inetAddress) {
        List var4 = this.mEsptouchResultList;
        synchronized (this.mEsptouchResultList) {
            boolean isTaskSucCountEnough = false;
            Integer count = (Integer) this.mBssidTaskSucCountMap.get(bssid);
            if (count == null) {
                count = Integer.valueOf(0);
            }

            count = Integer.valueOf(count.intValue() + 1);
            Log.d("EsptouchTask", "__putEsptouchResult(): count = " + count);
            this.mBssidTaskSucCountMap.put(bssid, count);
            isTaskSucCountEnough = count.intValue() >= this.mParameter.getThresholdSucBroadcastCount();
            if (!isTaskSucCountEnough) {
                Log.d("EsptouchTask", "__putEsptouchResult(): count = " + count + ", isn\'t enough");
            } else {
                boolean isExist = false;
                Iterator var9 = this.mEsptouchResultList.iterator();

                while (var9.hasNext()) {
                    IEsptouchResult esptouchResult = (IEsptouchResult) var9.next();
                    if (esptouchResult.getBssid().equals(bssid)) {
                        isExist = true;
                        break;
                    }
                }

                if (!isExist) {
                    Log.d("EsptouchTask", "__putEsptouchResult(): put one more result");
                    EsptouchResult esptouchResult1 = new EsptouchResult(isSuc, bssid, inetAddress);
                    this.mEsptouchResultList.add(esptouchResult1);
                }

            }
        }
    }

    private List<IEsptouchResult> __getEsptouchResultList() {
        List var1 = this.mEsptouchResultList;
        synchronized (this.mEsptouchResultList) {
            if (this.mEsptouchResultList.isEmpty()) {
                EsptouchResult esptouchResultFail = new EsptouchResult(false, (String) null, (InetAddress) null);
                esptouchResultFail.setIsCancelled(this.mIsCancelled.get());
                this.mEsptouchResultList.add(esptouchResultFail);
            }

            return this.mEsptouchResultList;
        }
    }

    private synchronized void __interrupt() {
        if (!this.mIsInterrupt) {
            this.mIsInterrupt = true;
            this.mSocketClient.interrupt();
            this.mSocketServer.interrupt();
            Thread.currentThread().interrupt();
        }

    }

    public void interrupt() {
        Log.d("EsptouchTask", "interrupt()");
        this.mIsCancelled.set(true);
        this.__interrupt();
    }

    private void __listenAsyn(final int expectDataLen) {
        (new Thread() {
            public void run() {
                Log.d("EsptouchTask", "__listenAsyn() start");
                long startTimestamp = System.currentTimeMillis();
                byte[] apSsidAndPassword = ByteUtil.getBytesByString(__EsptouchTask.this.mApSsid + __EsptouchTask.this.mApPassword);
                byte expectOneByte = (byte) (apSsidAndPassword.length + 9);
                Log.i("EsptouchTask", "expectOneByte: " + (0 + expectOneByte));
                boolean receiveOneByte = true;
                Object receiveBytes = null;

                while (__EsptouchTask.this.mEsptouchResultList.size() < __EsptouchTask.this.mParameter.getExpectTaskResultCount() && !__EsptouchTask.this.mIsInterrupt) {
                    byte[] receiveBytes1 = __EsptouchTask.this.mSocketServer.receiveSpecLenBytes(expectDataLen);
                    byte receiveOneByte1;
                    if (receiveBytes1 != null) {
                        receiveOneByte1 = receiveBytes1[0];
                    } else {
                        receiveOneByte1 = -1;
                    }

                    if (receiveOneByte1 == expectOneByte) {
                        Log.i("EsptouchTask", "receive correct broadcast");
                        long consume = System.currentTimeMillis() - startTimestamp;
                        int timeout = (int) ((long) __EsptouchTask.this.mParameter.getWaitUdpTotalMillisecond() - consume);
                        if (timeout < 0) {
                            Log.i("EsptouchTask", "esptouch timeout");
                            break;
                        }

                        Log.i("EsptouchTask", "mSocketServer\'s new timeout is " + timeout + " milliseconds");
                        __EsptouchTask.this.mSocketServer.setSoTimeout(timeout);
                        Log.i("EsptouchTask", "receive correct broadcast");
                        if (receiveBytes1 != null) {
                            String bssid = ByteUtil.parseBssid(receiveBytes1, __EsptouchTask.this.mParameter.getEsptouchResultOneLen(), __EsptouchTask.this.mParameter.getEsptouchResultMacLen());
                            InetAddress inetAddress = EspNetUtil.parseInetAddr(receiveBytes1, __EsptouchTask.this.mParameter.getEsptouchResultOneLen() + __EsptouchTask.this.mParameter.getEsptouchResultMacLen(), __EsptouchTask.this.mParameter.getEsptouchResultIpLen());
                            __EsptouchTask.this.__putEsptouchResult(true, bssid, inetAddress);
                        }
                    } else {
                        Log.i("EsptouchTask", "receive rubbish message, just ignore");
                    }
                }

                __EsptouchTask.this.mIsSuc = __EsptouchTask.this.mEsptouchResultList.size() >= __EsptouchTask.this.mParameter.getExpectTaskResultCount();
                __EsptouchTask.this.__interrupt();
                Log.d("EsptouchTask", "__listenAsyn() finish");
            }
        }).start();
    }

    private boolean __execute(IEsptouchGenerator generator) {
        long startTime = System.currentTimeMillis();
        long currentTime = startTime;
        long lastTime = startTime - this.mParameter.getTimeoutTotalCodeMillisecond();// 6000L
        byte[][] gcBytes2 = generator.getGCBytes2();
        byte[][] dcBytes2 = generator.getDCBytes2();
        int index = 0;

        while (!this.mIsInterrupt) {
            if (currentTime - lastTime >= this.mParameter.getTimeoutTotalCodeMillisecond()) {// 第一次必相等
                Log.e("EsptouchTask", "send gc code ");

                while (!this.mIsInterrupt &&
                        System.currentTimeMillis() - currentTime < this.mParameter.getTimeoutGuideCodeMillisecond()) {//2000L

                    this.mSocketClient.sendData(gcBytes2, this.mParameter.getTargetHostname(),
                            this.mParameter.getTargetPort(), this.mParameter.getIntervalGuideCodeMillisecond());

                    if (System.currentTimeMillis() - startTime > (long) this.mParameter.getWaitUdpSendingMillisecond()) {//45000L
                        break;
                    }

                }

                lastTime = currentTime;
            } else {
                this.mSocketClient.sendData(dcBytes2, index, 3, this.mParameter.getTargetHostname(),
                        this.mParameter.getTargetPort(), this.mParameter.getIntervalDataCodeMillisecond());
                index = (index + 3) % dcBytes2.length;
            }

            currentTime = System.currentTimeMillis();
            if (currentTime - startTime > (long) this.mParameter.getWaitUdpSendingMillisecond()) {
                break;
            }
        }

        return this.mIsSuc;
    }

    private void __checkTaskValid() {
        if (this.mIsExecuted) {
            throw new IllegalStateException("the Esptouch task could be executed only once");
        } else {
            this.mIsExecuted = true;
        }
    }

    public IEsptouchResult executeForResult() throws RuntimeException {
        return this.executeForResults(1).get(0);
    }

    public boolean isCancelled() {
        return this.mIsCancelled.get();
    }

    public List<IEsptouchResult> executeForResults(int expectTaskResultCount) throws RuntimeException {
        this.__checkTaskValid();
        this.mParameter.setExpectTaskResultCount(expectTaskResultCount);
        Log.d("EsptouchTask", "execute()");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Don\'t call the esptouch Task at Main(UI) thread directly.");
        } else {
            InetAddress localInetAddress = EspNetUtil.getLocalInetAddress(this.mContext);
            Log.e("EsptouchTask", "localInetAddress: " + localInetAddress);
            EsptouchGenerator generator = new EsptouchGenerator(this.mApSsid, this.mApBssid, this.mApPassword, localInetAddress, this.mIsSsidHidden);
            this.__listenAsyn(this.mParameter.getEsptouchResultTotalLen());
            boolean isSuc = false;

            for ( int e = 0; e < this.mParameter.getTotalRepeatTime(); ++e ) {
                isSuc = this.__execute(generator);
                if (isSuc) {
                    return this.__getEsptouchResultList();
                }
            }

            try {
                Thread.sleep((long) this.mParameter.getWaitUdpReceivingMillisecond());
            } catch (InterruptedException var6) {
                if (this.mIsSuc) {
                    return this.__getEsptouchResultList();
                }

                this.__interrupt();
                return this.__getEsptouchResultList();
            }

            this.__interrupt();
            return this.__getEsptouchResultList();
        }
    }
}
