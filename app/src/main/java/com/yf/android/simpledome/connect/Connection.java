package com.yf.android.simpledome.connect;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.datasource.serverdata.ServerData;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.SettingFragment;
import com.yf.android.simpledome.utils.FinalPargram;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection {
    private WeakReference<Socket> mSocket;

    private ReconnectThread reconnectThread;
    // 接收线程
    private ReceiveThread receiveThread;

    public boolean isConnected = false;

    private final static Long HEART_BEAT_RATE = 30 * 1000L;
    private long sendTime = 0L;
    // For heart Beat,同时处理断线重连登陆反馈
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.loginNc) {
                FinalPargram.isLogin = false;
                if (SettingFragment.handler != null) {
                    SettingFragment.handler.sendEmptyMessage(R.id.loginNc);
                }
            }
        }
    };
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                //发送心跳包过去 如果发送失败，就重新初始化一个socket
                boolean isSuccess = writeCmd("HEART-BELL-S");
                if (!isSuccess) {
                    mHandler.removeCallbacks(heartBeatRunnable);
                    receiveThread.release();
                    releaseLastSocket();
                    new Thread(new ConnectThread()).start();
                }
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private static Connection instance;

    private Connection() {// 初始化
        this.reconnectThread = new ReconnectThread();
    }

    public static Connection getInstance() {
        if (instance == null) {
            synchronized (Connection.class) {
                if (instance == null) {
                    instance = new Connection();
                }
            }
        }
        return instance;
    }

    public void connect() {
        new Thread(new ConnectThread()).start();
    }

    class ConnectThread implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = new Socket();
                SocketAddress address = new InetSocketAddress(FinalPargram.SERVER_HOST,
                        FinalPargram.SERVER_PORT);
                socket.connect(address, 1000);

                mSocket = new WeakReference<Socket>(socket);

                isConnected = true;

                if (FinalPargram.isLogin) {
                    new CustomDialog(mHandler);
                    writeCmd(FinalPargram.loginCmd);
                }

                //初始化成功后，就准备发送心跳包
                mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);

                receiveThread = new ReceiveThread(socket);
                receiveThread.start();
            } catch (IOException e) {
                if (reconnectThread == null || !reconnectThread.isAlive()) {
                    reconnectThread = new ReconnectThread();
                    reconnectThread.start();
                }
            }
        }
    }

    public void loginCmd(String userName, String pwd) {
        //LOGIN-[userName]-[pwd]-S
        String cmd = "LOGIN-" + userName + "-" + pwd + "-S";
        FinalPargram.loginCmd = cmd;
        writeCmd(cmd);
    }

    public void cmdRegister(String head, String... boddy) {
        String cmd = "REGIS-" + head;
        for ( int i = 0; i < boddy.length; i++ ) {
            cmd += "-" + boddy[i];
        }
        cmd += "-S";
        writeCmd(cmd);
    }

    public void loadServerControllerData(ServerData.LoadServerControllersCallBack callBack) {
        if (receiveThread != null) {
            receiveThread.loadServerControllerData(callBack);
        } else {
            callBack.onDataNotAvailable();
            connect();
        }
    }

    public void loadServerSceneInfoData(ServerData.LoadServerSceneInfoCallBack callBack) {
        if (receiveThread != null) {
            receiveThread.loadServerSceneInfoData(callBack);
        } else {
            callBack.onDataNotAvailable();
        }
    }

    public void loadServerSceneData(ServerData.LoadServerScenesCallBack callBack, int sceneCode) {
        if (receiveThread != null) {
            receiveThread.loadServerSceneData(callBack, sceneCode);
        } else {
            callBack.onDataNotAvailable();
        }
    }

    public void cmdSceneOpt(int sceneCode) {
        // LIGHT-SC-[场景号]-S
        writeCmd("LIGHT-SC-" + sceneCode + "-S");
    }

    public void cmdContOpt(String contCode, String data) {
        // LIGHT-LC-[设备COD]-[DATA]-S
        writeCmd("LIGHT-LC-" + contCode + "-" + data + "-S");
    }

    public void cmdDeleteSceneInfo(int sceneCode) {
        // DATAO-DEL-[表名]-NC-NC-[条件]-S
        writeCmd("DATAO-DEL-" + LocalDataSqlHelper.TABLE_SCIN + "-NC-NC-" +
                DataBaseColumn.SCIN_COD + "=" + sceneCode + "-S");
        // NODES-DE-[SCIN_COD]-S
        writeCmd("NODES-DE-" + sceneCode + "-S");
    }

    public void cmdBindCont(String contCode) {
        // BINDS-[CONT_COD]-S
        writeCmd("BINDS-" + contCode + "-S");
    }

    public void cmdDBUpdate(String table, String column, String value, String infliter) {
        // DATAO-UPD-[表名]-[字段]-[字段值]-[条件]-S
        // 支持单项修改
        writeCmd("DATAO-UPD-" + table + "-" + column + "-" + value + "-" + infliter + "-S");
    }

    public void cmdDBDelete(String table, String inflter) {
        // DATAO-DEL-[表名]-NC-NC-[条件]-S
        writeCmd("DATAO-DEL-" + table + "-NC-NC-" + inflter + "-S");
    }

    public void cmdDBAdd(String table, String[] columns, String[] values) {
        // DATAO-ADD-[表名]-[字段]-[字段值]-NC-S
        if (columns.length == 0 || values.length == 0) {
            return;
        }
        String c = columns[0];
        String v = values[0];
        for ( int i = 1; i < columns.length; i++ ) {
            c += "," + columns[i];
        }
        for ( int i = 1; i < values.length; i++ ) {
            v += "," + values[i];
        }
        writeCmd("DATAO-ADD-" + table + "-" + c + "-" + v + "-NC-S");
    }

    public void cmdSetControllerScene(String contCode, int key, int sceneCode) {
        // DATAO-UPD-[表名]-[字段]-[字段值]-[条件]-S
        String column = DataBaseColumn.CONT_BAS;
        switch (key) {
            case 1:
                column = DataBaseColumn.CONT_BAS;
                break;
            case 2:
                column = DataBaseColumn.CONT_BBS;
                break;
            case 3:
                column = DataBaseColumn.CONT_BCS;
                break;
            case 4:
                column = DataBaseColumn.CONT_BDS;
        }
        writeCmd("DATAO-UPD-" + LocalDataSqlHelper.TABLE_CONT + "-" + column + "-"
                + sceneCode + "-" + DataBaseColumn.CONT_COD + "=" + contCode + "-S");
        // NODES-CU-[CONT_COD]-[CONT_KEY]-[SCIN_COD]-S
        writeCmd("NODES-CU-" + contCode + "-" + key + "-" + sceneCode + "-S");
    }


    public synchronized boolean writeCmd(String cmd) {
        Log.e("CMD", "--------------writeCmd: " + cmd);
        if (null == mSocket || null == mSocket.get()) {
            return false;
        }
        Socket soc = mSocket.get();
        try {
            if (!soc.isClosed() && !soc.isOutputShutdown()) {
                OutputStream os = soc.getOutputStream();
                String message = cmd + "\r\n";
                os.write(message.getBytes());
                os.flush();
                //每次发送成数据，就改一下最后成功发送的时间，节省心跳间隔时间
                sendTime = System.currentTimeMillis();
            } else {
                return false;
            }
        } catch (IOException e) {
            if (reconnectThread == null || !reconnectThread.isAlive()) {
                reconnectThread = new ReconnectThread();
                reconnectThread.start();
            } else {
                connect();
            }
            return false;
        }
        return true;
    }

    public void releaseLastSocket() {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if (sk != null && !sk.isClosed()) {
                    sk.close();
                }
                isConnected = false;
                sk = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}