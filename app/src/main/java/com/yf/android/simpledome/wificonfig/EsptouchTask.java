package com.yf.android.simpledome.wificonfig;

import android.content.Context;

import java.util.List;

public class EsptouchTask implements IEsptouchTask {
    public __EsptouchTask _mEsptouchTask;
    private IEsptouchTaskParameter _mParameter = new EsptouchTaskParameter();

    public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, Context context) {
        this._mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword, context, this._mParameter, isSsidHidden);
    }

    public EsptouchTask(String apSsid, String apBssid, String apPassword, boolean isSsidHidden, int timeoutMillisecond, Context context) {
        this._mParameter.setWaitUdpTotalMillisecond(timeoutMillisecond);
        this._mEsptouchTask = new __EsptouchTask(apSsid, apBssid, apPassword, context, this._mParameter, isSsidHidden);
    }

    public void interrupt() {
        this._mEsptouchTask.interrupt();
    }

    public IEsptouchResult executeForResult() throws RuntimeException {
        return this._mEsptouchTask.executeForResult();
    }

    public boolean isCancelled() {
        return this._mEsptouchTask.isCancelled();
    }

    public List<IEsptouchResult> executeForResults(int expectTaskResultCount) throws RuntimeException {
        if (expectTaskResultCount <= 0) {
            expectTaskResultCount = 2147483647;
        }

        return this._mEsptouchTask.executeForResults(expectTaskResultCount);
    }
}
