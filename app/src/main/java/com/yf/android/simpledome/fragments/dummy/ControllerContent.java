package com.yf.android.simpledome.fragments.dummy;

import android.content.Context;
import android.util.Log;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.localdata.ControllerDataSource;
import com.yf.android.simpledome.datasource.localdata.LocalControllerDataSource;
import com.yf.android.simpledome.datasource.serverdata.ServerData;
import com.yf.android.simpledome.fragments.ControlFragment;

import java.util.ArrayList;
import java.util.List;

public class ControllerContent {

    public static List<Controller> ITEMS = new ArrayList<Controller>();


    public static void initController(Context mContext) {
        LocalControllerDataSource.getInstance(mContext)
                .getControllers(new ControllerDataSource.LoadControllersCallback() {
                    @Override
                    public void onControllersLoaded(ArrayList<Controller> controllers) {
                        ITEMS = controllers;
                    }

                    @Override
                    public void onDataNotAvailable() {

                    }
                });
    }

    public static boolean updateAsSceneCallBack(String contCode, String data) {
        Controller controller = getContByCode(contCode);
        if (controller != null) {
            Log.e("TAG2", "--------------dateOpt: " + controller.getContName());
            controller.setOptData(data);
            if (ControlFragment.handler != null) {
                ControlFragment.handler.sendEmptyMessage(R.id.refresh);
            }
            return true;
        }
        return false;
    }

    /**
     * setContOnlineState
     *
     * @param contCode
     * @param online
     * @return
     */
    public static boolean setContOnlineState(String contCode, int online) {
        for ( Controller cont : ITEMS ) {
            if (cont.getContCode().equals(contCode)) {
                cont.setContOnline(online);
                return true;
            }
        }
        return false;
    }

    /**
     * getContByCode
     *
     * @param contCode
     * @return
     */
    public static Controller getContByCode(String contCode) {
        for ( Controller cont : ITEMS ) {
            if (cont.getContCode().equals(contCode)) {
                return cont;
            }
        }
        return null;
    }

    /**
     * get UnAdded to scene Controllers
     *
     * @param sceneCode
     * @return
     */
    public static List<Controller> getUnAddedControllers(int sceneCode) {
        List<Controller> lists = new ArrayList<>();
        lists.addAll(ITEMS);
        for ( Controller cont : ITEMS ) {
            for ( Scene s : SceneContentContent.ITEMS ) {
                if (cont.getContCode().equals(s.getContCode())) {
                    lists.remove(cont);
                }
            }
        }
        return lists;
    }

    public static void initServerData(final DataChangeListener listener) {
        Connection.getInstance()
                .loadServerControllerData(new ServerData.LoadServerControllersCallBack() {
                    List<Controller> lists = new ArrayList<Controller>();

                    @Override
                    public void onLoadController(Controller controller) {
                        LocalControllerDataSource.getInstance(null).deleteController(controller.getContCode());
                        LocalControllerDataSource.getInstance(null).insertController(controller);
                        lists.add(controller);
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
}
