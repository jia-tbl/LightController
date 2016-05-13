package com.yf.android.simpledome.datasource.localdata;

import android.support.annotation.NonNull;

import com.yf.android.simpledome.datasource.Scene;

import java.util.List;

public interface SceneDataSource {
    interface LoadScenesCallback {

        void onScenesLoaded(List<Scene> scenes);

        void onDataNotAvailable();
    }

    void getScenes(@NonNull LoadScenesCallback callback, int sceneCode);

    void insertScene(@NonNull Scene scene);

    void refreshScene(@NonNull Scene scene);

    void deleteScene(@NonNull int sceneId);

    void deleteScene(int sceneCode, String contCode);

    void close();
}
