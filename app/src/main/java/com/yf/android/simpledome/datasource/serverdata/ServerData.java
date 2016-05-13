package com.yf.android.simpledome.datasource.serverdata;

import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.SceneInfo;

public interface ServerData {
    interface LoadServerControllersCallBack {

        void onLoadController(Controller controller);

        void onLoadDataComplete();

        void onDataNotAvailable();
    }

    interface LoadServerSceneInfoCallBack {

        void onLoadSceneInfo(SceneInfo sceneInfo);

        void onLoadDataComplete();

        void onDataNotAvailable();
    }

    interface LoadServerScenesCallBack {

        void onLoadScene(Scene scene);

        void onLoadDataComplete();

        void onDataNotAvailable();
    }
}
