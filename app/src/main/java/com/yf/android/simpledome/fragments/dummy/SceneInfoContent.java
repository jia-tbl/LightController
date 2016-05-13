package com.yf.android.simpledome.fragments.dummy;

import android.content.Context;

import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.localdata.LocalSceneInfoDataSource;
import com.yf.android.simpledome.datasource.localdata.SceneInfoDataSource;
import com.yf.android.simpledome.datasource.serverdata.ServerData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class SceneInfoContent {

    public static List<SceneInfo> ITEMS = new ArrayList<SceneInfo>();

    public static final int COUNT = 16;

    public static void initServerData(final DataChangeListener listener) {
        Connection.getInstance()
                .loadServerSceneInfoData(new ServerData.LoadServerSceneInfoCallBack() {
                    List<SceneInfo> lists = new ArrayList<SceneInfo>();

                    @Override
                    public void onLoadSceneInfo(SceneInfo sceneInfo) {
                        LocalSceneInfoDataSource.getInstance(null).deleteSceneInfo(sceneInfo.getCode());
                        LocalSceneInfoDataSource.getInstance(null).insertSceneInfo(sceneInfo);
                        lists.add(sceneInfo);
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
                        if (listener != null) {
                            listener.onDataNotAvailable();
                        }
                    }
                });
    }

    /**
     * 处理 场景 指令 反馈
     *
     * @param sceneCode
     * @return
     */
    public static boolean decodeSceneCallBack(int sceneCode) {
        if (sceneCode > 0 && sceneCode <= COUNT) {
            SceneInfo sceneInfo = getSceneInfoByCode(sceneCode);
            final List<Scene> sceneContents = new ArrayList<>();
            if (sceneInfo != null) {
                SceneContentContent.initServerData(new DataChangeListener() {
                    @Override
                    public void onDataChanged() {
                        sceneContents.addAll(SceneContentContent.ITEMS);
                        for ( Scene scene : sceneContents ) {
                            ControllerContent.updateAsSceneCallBack(scene.getContCode(), scene.getData());
                        }
                    }

                    @Override
                    public void onDataNotAvailable() {

                    }
                }, sceneCode);
            }
        }
        return false;
    }

    /**
     * getIndexByCode
     *
     * @param sceneCode
     * @return
     */
    public static int getIndexByCode(int sceneCode) {
        for ( int i = 0; i < ITEMS.size(); i++ ) {
            if (ITEMS.get(i).getCode() == sceneCode) {
                return i;
            }
        }
        return -1;
    }

    /**
     * getSceneInfoByCode
     *
     * @param code
     * @return
     */
    public static SceneInfo getSceneInfoByCode(int code) {
        for ( SceneInfo info : ITEMS ) {
            if (info.getCode() == code) {
                return info;
            }
        }
        return null;
    }

    /**
     * getSceneNameByCode
     *
     * @param code
     * @return
     */
    public static String getSceneNameByCode(int code) {
        SceneInfo info = getSceneInfoByCode(code);
        if (info == null) {
            return null;
        } else {
            return info.getName();
        }
    }

    public static int getAvailableSceneCode() {
        int[] existIds = new int[ITEMS.size()];
        for ( int i = 0; i < ITEMS.size(); i++ ) {
            existIds[i] = ITEMS.get(i).getCode();
        }
        Arrays.sort(existIds);
        for ( int i = 1; i <= COUNT; i++ ) {
            if (Arrays.binarySearch(existIds, i) < 0) {
                return i;
            }
        }
        return -1;
    }

    public static void initSceneInfo(Context mContext) {
        LocalSceneInfoDataSource.getInstance(mContext).getSceneInfos(new SceneInfoDataSource.LoadSceneInfosCallback() {
            @Override
            public void onSceneInfosLoaded(List<SceneInfo> infos) {
                ITEMS = infos;
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }
}
