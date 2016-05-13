package com.yf.android.simpledome.connect;

import android.util.Log;

public class ReconnectThread extends Thread {
    private int waiting;

    public ReconnectThread() {
        this.waiting = 0;
    }

    @Override
    public void run() {
        while (!isInterrupted() && !Connection.getInstance().isConnected) {
            try {
                Thread.sleep(waiting() * 1000L);
                if (NetWorkState.isNetworkAvailable)
                    Connection.getInstance().connect();
                Log.e("REC", "-------------REC");
                waiting++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private int waiting() {
        if (waiting > 300) {
            return 120;
        }
        if (waiting > 200) {
            return 60;
        }
        return waiting <= 100 ? 5 : 30;
    }
}
