package com.yf.android.simpledome.datasource.localdata;

import android.support.annotation.NonNull;

import com.yf.android.simpledome.datasource.SceneInfo;

import java.util.List;

public interface SceneInfoDataSource {

    interface LoadSceneInfosCallback {

        void onSceneInfosLoaded(List<SceneInfo> infos);

        void onDataNotAvailable();
    }

    void getSceneInfos(@NonNull LoadSceneInfosCallback callback);

    void insertSceneInfo(@NonNull SceneInfo sceneInfo);

    void refreshSceneInfoName(@NonNull SceneInfo sceneInfo);

    void refreshSceneInfoIcon(@NonNull SceneInfo sceneInfo);

    void deleteSceneInfo(@NonNull int sceneCod);

    void close();
}
