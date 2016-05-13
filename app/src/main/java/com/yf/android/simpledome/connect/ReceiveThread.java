package com.yf.android.simpledome.connect;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.serverdata.ServerData;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.ControlFragment;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;
import com.yf.android.simpledome.utils.FinalPargram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ReceiveThread extends Thread {
    // 请求数据等待时间
    private final static int NO_DATA_WAIT_TIME = 7000;
    // 信息不完整等待时间
    private final static int WAIT_TIME = 4000;


    private Timer mTimerCont;
    private Timer mNoDataTimerCont;
    private int curContCount = -1;
    private int loadedContCount = 0;
    private ServerData.LoadServerControllersCallBack controllersCallBack;

    private Timer mTimerSI;
    private Timer mNoDataTimerSI;
    private int curSceneInfoCount = -1;
    private int loadedSceneInfoCount = 0;
    private ServerData.LoadServerSceneInfoCallBack sceneInfoCallBack;

    private Timer mTimerSE;
    private Timer mNoDataTimerSE;
    private int curSceneCount = -1;
    private int loadedSceneCount = 0;
    private ServerData.LoadServerScenesCallBack scenesCallBack;

    private WeakReference<Socket> mWeakSocket;
    private boolean isStart = true;

    public ReceiveThread(Socket socket) {
        mWeakSocket = new WeakReference<Socket>(socket);
    }

    public void release() {
        isStart = false;
        try {
            if (null != mWeakSocket) {
                Socket sk = mWeakSocket.get();
                if (sk != null && !sk.isClosed()) {
                    sk.close();
                }
                sk = null;
                mWeakSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        Socket socket = mWeakSocket.get();
        if (null != socket) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "UTF-8"));
                String callBack;
                while (!socket.isClosed() && !socket.isInputShutdown()
                        && isStart && ((callBack = reader.readLine()) != null)) {
                    decodeCmd(callBack);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadServerControllerData(ServerData.LoadServerControllersCallBack callBack) {
        // DATAO-SEL-[表名]-NC-NC-[条件]-S
        Connection.getInstance().writeCmd("DATAO-SEL-" + LocalDataSqlHelper.TABLE_CONT + "-NC-NC-NC-S");
        this.controllersCallBack = callBack;
        mNoDataTimerCont = new Timer();
        mNoDataTimerCont.schedule(new TimerTask() {
            @Override
            public void run() {
                if (controllersCallBack != null) {
                    controllersCallBack.onDataNotAvailable();
                }
            }
        }, NO_DATA_WAIT_TIME);
    }

    public void loadServerSceneInfoData(ServerData.LoadServerSceneInfoCallBack callBack) {
        Connection.getInstance().writeCmd("DATAO-SEL-" + LocalDataSqlHelper.TABLE_SCIN + "-NC-NC-NC-S");
        this.sceneInfoCallBack = callBack;
        mNoDataTimerSI = new Timer();
        mNoDataTimerSI.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sceneInfoCallBack != null) {
                    sceneInfoCallBack.onDataNotAvailable();
                }
            }
        }, NO_DATA_WAIT_TIME);
    }

    public void loadServerSceneData(ServerData.LoadServerScenesCallBack callBack, int sceneCode) {
        Connection.getInstance().writeCmd("DATAO-SEL-" + LocalDataSqlHelper.TABLE_SCEN + "-NC-NC-"
                + DataBaseColumn.SCIN_COD + "=" + sceneCode + "-S");
        this.scenesCallBack = callBack;
        mNoDataTimerSE = new Timer();
        mNoDataTimerSE.schedule(new TimerTask() {
            @Override
            public void run() {
                if (scenesCallBack != null) {
                    scenesCallBack.onDataNotAvailable();
                }
            }
        }, NO_DATA_WAIT_TIME);
    }

    private void decodeCmd(String callBack) {
        Log.e("CALLBACK", "--------------:" + callBack);
        if (TextUtils.isEmpty(callBack)) {
            return;
        }
        if (callBack.length() < 5) {
            return;
        }
        if (callBack.equals("LOGIN-OK-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.loginOk);
            CustomDialog.handler = null;
        } else if (callBack.equals("LOGIN-NC-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.loginNc);
            CustomDialog.handler = null;
        } else if (callBack.equals("REGIS-OK-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.registerOk);
            CustomDialog.handler = null;
        } else if (callBack.equals("REGIS-NC-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.registerNc);
            CustomDialog.handler = null;
        } else if (FinalPargram.isLogin) {
            decodeCommunicate(callBack);
        }
    }

    private void decodeCommunicate(String callBack) {
        // CustomDialog.handler 接收过信息后置空 handler
        if (callBack.startsWith("BINDS-OK") && CustomDialog.handler != null) {
            Message msg = new Message();
            if (callBack.split("-")[2].matches("[1-4]")) {
                msg.what = R.id.addOK;
                msg.arg1 = Integer.parseInt(callBack.split("-")[2]);
                CustomDialog.handler.sendMessage(msg);
            } else {
                msg.what = R.id.errorTip;
                CustomDialog.handler.sendMessage(msg);
            }
            CustomDialog.handler = null;
        } else if (callBack.equals("BINDS-NC-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.addNC);
            CustomDialog.handler = null;
        } else if (callBack.equals("NODES-OK-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.addOK);
            CustomDialog.handler = null;
        } else if (callBack.equals("NODES-NC-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.addNC);
            CustomDialog.handler = null;
        } else if (callBack.startsWith("LIGHT-LC-")) {
            // LIGHT-LC-[设备COD]-[DATA]-S
            String[] strs = callBack.split("-");
            ControllerContent.updateAsSceneCallBack(strs[2], strs[3]);
        } else if (callBack.startsWith("LIGHT-SC-")) {
            // LIGHT-SC-[场景号]-S
            String[] strs = callBack.split("-");
            if (strs[2].matches("\\d+")) {
                SceneInfoContent.decodeSceneCallBack(Integer.parseInt(strs[2]));
            }
        } else if (callBack.startsWith("CONTO-OF")) {
            //CONTO-OF-[CONT_COD]-[0/1]-M
            String[] strs = callBack.split("-");
            if (strs[3].equals("1") || strs[3].equals("0")) {
                ControllerContent.setContOnlineState(strs[2], Integer.parseInt(strs[3]));
            }
        } else if (callBack.equals("DATAO-NC-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.addNC);
            CustomDialog.handler = null;
        } else if (callBack.equals("DATAO-OK-M") && CustomDialog.handler != null) {
            CustomDialog.handler.sendEmptyMessage(R.id.addOK);
            CustomDialog.handler = null;
        } else if (callBack.startsWith("NODES-CW-")) {// 控制器类型变更
            //NODES-CW-[CONT_COD]-[CONT_TYPE]-M
            // NODES-CW-003000A7-2-M
            String[] strs = callBack.split("-");
            if (strs[2].length() == 8 && strs[3].matches("\\d+")) {
                Controller cont = ControllerContent.getContByCode(strs[2]);
                int type = Integer.parseInt(strs[3]);
                if (cont != null && type > 0 && type <= 4) cont.setContType(type);
                if (ControlFragment.handler != null) {
                    ControlFragment.handler.sendEmptyMessage(R.id.refresh);
                }
            }
        } else if (callBack.equals("HEART-HOK-M")) {
            // heart thread
        } else if (callBack.startsWith(LocalDataSqlHelper.TABLE_SCEN)) {
            if (callBack.equals(LocalDataSqlHelper.TABLE_SCEN + "-NC-M")
                    && scenesCallBack != null) {
                scenesCallBack.onDataNotAvailable();
                scenesCallBack = null;
            } else if (scenesCallBack != null) {
                String[] strs = callBack.split("-");
                if (strs.length != 6 || !strs[1].matches("\\d+")) {
                    return;
                }
                scenesCallBack.onLoadScene(decodeScene(strs));
                markSceneLoad(Integer.parseInt(callBack.split("-")[1]));
            }
        } else if (callBack.startsWith(LocalDataSqlHelper.TABLE_SCIN)) {
            if (callBack.equals(LocalDataSqlHelper.TABLE_SCIN + "-NC-M")
                    && sceneInfoCallBack != null) {
                sceneInfoCallBack.onDataNotAvailable();
                sceneInfoCallBack = null;
            } else if (sceneInfoCallBack != null) {
                String[] strs = callBack.split("-");
                if (strs.length != 6 || !strs[1].matches("\\d+")) {
                    return;
                }
                sceneInfoCallBack.onLoadSceneInfo(decodeSceneInfo(strs));
                markSceneInfoLoad(Integer.parseInt(strs[1]));
            }
        } else if (callBack.startsWith(LocalDataSqlHelper.TABLE_CONT)) {
            if (callBack.equals(LocalDataSqlHelper.TABLE_CONT + "-NC-M")
                    && controllersCallBack != null) {
                controllersCallBack.onDataNotAvailable();
                controllersCallBack = null;
            } else if (controllersCallBack != null) {
                String[] strs = callBack.split("-");
                if (strs.length != 17 || !strs[1].matches("\\d+")) {
                    return;
                }
                controllersCallBack.onLoadController(decodeController(strs));
                markControllerLoad(Integer.parseInt(strs[1]));
            }
        }
    }

    private Scene decodeScene(String[] strs) {
        Scene scene = new Scene();
        scene.setCode(Integer.parseInt(strs[2]));
        scene.setContCode(strs[3]);
        scene.setData(strs[4]);
        return scene;
    }


    private SceneInfo decodeSceneInfo(String[] strs) {
        SceneInfo info = new SceneInfo();

        info.setCode(Integer.parseInt(strs[2]));
        info.setName(strs[3]);
        //info.setIcon(strs[4]);
        info.setIcon(R.mipmap.scene_1 + "");

        return info;
    }


    private Controller decodeController(String[] strs) {
        Controller cont = new Controller();

        cont.setContName(strs[2]);
        cont.setContData(strs[3]);
        cont.setContCode(strs[4]);

        cont.setBtnName1(strs[5]);
        cont.setBtnName2(strs[6]);
        cont.setBtnName3(strs[7]);
        cont.setBtnName4(strs[8]);

        cont.setBtnScene1(Integer.parseInt(strs[9]));
        cont.setBtnScene2(Integer.parseInt(strs[10]));
        cont.setBtnScene3(Integer.parseInt(strs[11]));
        cont.setBtnScene4(Integer.parseInt(strs[12]));

        cont.setContType(Integer.parseInt(strs[13]));
        cont.setContOnline(Integer.parseInt(strs[14]));
        cont.setOptData(strs[15]);
        return cont;
    }

    private void markSceneLoad(int len) {
        if (curSceneCount == -1) {
            curSceneCount = len;
            mNoDataTimerSE.cancel();
            mTimerSE = new Timer();
            mTimerSE.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (scenesCallBack != null) {
                        scenesCallBack.onLoadDataComplete();
                        scenesCallBack = null;
                    }
                    loadedSceneCount = 0;
                    curSceneCount = -1;
                }
            }, WAIT_TIME);
        }
        loadedSceneCount++;
        if (loadedSceneCount == curSceneCount) {
            loadedSceneCount = 0;
            curSceneCount = -1;
            mTimerSE.cancel();
            if (scenesCallBack != null) {
                scenesCallBack.onLoadDataComplete();
                scenesCallBack = null;
            }
        }
    }

    private void markSceneInfoLoad(int len) {
        if (curSceneInfoCount == -1) {
            curSceneInfoCount = len;
            mNoDataTimerSI.cancel();
            mTimerSI = new Timer();
            mTimerSI.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (sceneInfoCallBack != null) {
                        sceneInfoCallBack.onLoadDataComplete();
                        sceneInfoCallBack = null;
                    }
                    loadedSceneInfoCount = 0;
                    curSceneInfoCount = -1;
                }
            }, WAIT_TIME);
        }
        loadedSceneInfoCount++;
        if (loadedSceneInfoCount == curSceneInfoCount) {
            loadedSceneInfoCount = 0;
            curSceneInfoCount = -1;
            mTimerSI.cancel();
            if (sceneInfoCallBack != null) {
                sceneInfoCallBack.onLoadDataComplete();
                sceneInfoCallBack = null;
            }
        }
    }

    private void markControllerLoad(int len) {
        if (curContCount == -1) {
            curContCount = len;
            mNoDataTimerCont.cancel();
            mTimerCont = new Timer();
            mTimerCont.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (controllersCallBack != null) {
                        controllersCallBack.onLoadDataComplete();
                        controllersCallBack = null;
                    }
                    loadedContCount = 0;
                    curContCount = -1;
                }
            }, WAIT_TIME);
        }
        loadedContCount++;
        if (loadedContCount == curContCount) {
            loadedContCount = 0;
            curContCount = -1;
            mTimerCont.cancel();
            if (controllersCallBack != null) {
                controllersCallBack.onLoadDataComplete();
                controllersCallBack = null;
            }
        }
    }
}