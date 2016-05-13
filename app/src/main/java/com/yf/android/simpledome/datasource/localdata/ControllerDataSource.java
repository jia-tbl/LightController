package com.yf.android.simpledome.datasource.localdata;

import android.support.annotation.NonNull;

import com.yf.android.simpledome.datasource.Controller;

import java.util.ArrayList;

public interface ControllerDataSource {

    interface LoadControllersCallback {

        void onControllersLoaded(ArrayList<Controller> controllers);

        void onDataNotAvailable();
    }

    void getControllers(@NonNull LoadControllersCallback callback);

    void insertController(@NonNull Controller controller);

    void operateController(@NonNull Controller controller);

    void deleteController(@NonNull String contCode);

    void close();
}
