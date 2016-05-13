package com.yf.android.simpledome.wificonfig;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import java.util.Iterator;
import java.util.List;

public class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {
    private ProgressDialog mProgressDialog;
    private IEsptouchTask mEsptouchTask;
    private final Object mLock = new Object();
    private Context mContext;

    public EsptouchAsyncTask3(Context c) {
        this.mContext = c;
    }

    protected void onPreExecute() {
        this.mProgressDialog = new ProgressDialog(mContext);
        this.mProgressDialog.setMessage("正在配置WIFI帐号和密码...");
        this.mProgressDialog.setCanceledOnTouchOutside(false);
        this.mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                synchronized (EsptouchAsyncTask3.this.mLock) {
                    if (EsptouchAsyncTask3.this.mEsptouchTask != null) {
                        EsptouchAsyncTask3.this.mEsptouchTask.interrupt();
                    }

                }
            }
        });
        this.mProgressDialog.setButton(-1, "请稍等...", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mProgressDialog.show();
        this.mProgressDialog.getButton(-1).setEnabled(false);
    }

    protected List<IEsptouchResult> doInBackground(String... params) {
        boolean taskResultCount = true;
        Object resultList = this.mLock;
        int taskResultCount1;
        synchronized (this.mLock) {
            String apSsid = params[0];
            String apBssid = params[1];
            String apPassword = params[2];
            String isSsidHiddenStr = params[3];
            String taskResultCountStr = params[4];
            boolean isSsidHidden = false;
            if (isSsidHiddenStr.equals("YES")) {
                isSsidHidden = true;
            }

            taskResultCount1 = Integer.parseInt(taskResultCountStr);
            this.mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, isSsidHidden, mContext);
        }

        List resultList1 = this.mEsptouchTask.executeForResults(taskResultCount1);
        return resultList1;
    }

    protected void onPostExecute(List<IEsptouchResult> result) {
        this.mProgressDialog.getButton(-1).setEnabled(true);
        this.mProgressDialog.getButton(-1).setText("确定");
        IEsptouchResult firstResult = (IEsptouchResult) result.get(0);
        if (!firstResult.isCancelled()) {
            int count = 0;
            boolean maxDisplayCount = true;
            if (firstResult.isSuc()) {
                StringBuilder sb = new StringBuilder();
                Iterator var7 = result.iterator();

                while (var7.hasNext()) {
                    IEsptouchResult resultInList = (IEsptouchResult) var7.next();
                    sb.append("  配置成功！\n");
                    ++count;
                    if (count >= 5) {
                        break;
                    }
                }

                if (count < result.size()) {
                    sb.append("\nthere\'s " + (result.size() - count) + " more result(s) without showing\n");
                }

                this.mProgressDialog.setMessage(sb.toString());
            } else {
                this.mProgressDialog.setMessage("  配置失败！");
            }
        }

    }
}
