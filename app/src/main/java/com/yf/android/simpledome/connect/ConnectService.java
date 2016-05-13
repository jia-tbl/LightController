package com.yf.android.simpledome.connect;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;

public class ConnectService extends Service {
    private NetWorkState netWorkState;
    private CustomBinder mBinder;


    public ConnectService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        Connection.getInstance().connect();


        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netWorkState = new NetWorkState(this);
        registerReceiver(netWorkState, filter);

        mBinder = new CustomBinder();
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Connection.getInstance().releaseLastSocket();

        unregisterReceiver(netWorkState);
    }

    public class CustomBinder extends Binder {
        public void customFunction() {
            //TODO: custom logic
        }
    }
}
