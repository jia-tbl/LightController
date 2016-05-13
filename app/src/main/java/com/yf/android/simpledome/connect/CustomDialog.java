package com.yf.android.simpledome.connect;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.yf.android.simpledome.R;

import java.util.Timer;
import java.util.TimerTask;

public class CustomDialog {
    private ProgressDialog dialog;

    private Context context;
    private String tips;
    private long timeOut;

    public static Timer timer;
    public static Handler handler;

    public CustomDialog(Handler handler) {
        CustomDialog.handler = handler;
    }

    public CustomDialog(Context context, String tips, Handler handler, long time) {
        this.context = context;
        this.tips = tips;
        this.timeOut = time;
        CustomDialog.handler = handler;
        showDialogView();
    }

    private void showDialogView() {
        dialog = new ProgressDialog(context);
        dialog.setMessage(tips);
        dialog.setCancelable(false);
        timer = new Timer();
        dialog.show();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                dialog.cancel();
                if (handler != null) {
                    handler.sendEmptyMessage(R.id.none_callback);
                    handler = null;
                }
            }
        }, timeOut);
    }

    public void cancel() {
        timer.cancel();
        dialog.cancel();
    }
}
