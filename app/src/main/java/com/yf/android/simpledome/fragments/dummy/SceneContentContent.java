package com.yf.android.simpledome.fragments.dummy;

import android.content.Context;
import android.util.Log;

import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.localdata.LocalSceneDataSource;
import com.yf.android.simpledome.datasource.localdata.SceneDataSource;
import com.yf.android.simpledome.datasource.serverdata.ServerData;

import java.util.ArrayList;
import java.util.List;

public class SceneContentContent {
    public static List<Scene> ITEMS = new ArrayList<Scene>();

    public static void initServerData(final DataChangeListener listener, int sceneCode) {
        Connection.getInstance()
                .loadServerSceneData(new ServerData.LoadServerScenesCallBack() {
                    List<Scene> lists = new ArrayList<Scene>();

                    @Override
                    public void onLoadScene(Scene scene) {
                        LocalSceneDataSource.getInstance(null).deleteScene(scene.getCode(),
                                scene.getContCode());
                        LocalSceneDataSource.getInstance(null).insertScene(scene);
                        lists.add(scene);
                    }

                    @Override
                    public void onLoadDataComplete() {
                        ITEMS = lists;
                        if (listener != null) {
                            listener.onDataChanged();
                        }
                    }

                    @Override
                    public void onDataNotAvailable() {
                        ITEMS = lists;// 覆盖之前的数据
                        if (listener != null) {
                            listener.onDataNotAvailable();
                        }
                    }
                }, sceneCode);
    }


    public static void initLocalData(Context mContext, int sceneCode) {
        LocalSceneDataSource.getInstance(mContext)
                .getScenes(new SceneDataSource.LoadScenesCallback() {
                    @Override
                    public void onScenesLoaded(List<Scene> scenes) {
                        Log.e("LOAD", "----------------onScenesLoaded: " + scenes.size());
                        ITEMS = scenes;
                    }

                    @Override
                    public void onDataNotAvailable() {

                    }
                }, sceneCode);
    }
}
