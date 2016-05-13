package com.yf.android.simpledome.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.utils.DecodeByte;

public class ControllerItemOptFragment extends DialogFragment {
    public static final String HEAD_CODE = "currentbtncode";
    public static final String ARG_CONT = "controlerasargu";

    private Controller con;
    private String newData;
    private int currentBtn;

    private View rootView;

    private SeekBar seekBarWarmCool;
    private SeekBar seekBarBright;

    private int pWC;

    private int warm;
    private int cool;
    private double rec = 1;


    private CustomDialog dialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                saveChange();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无应答", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };

    private void saveChange() {
        con.setContData(newData);
        seekBarWarmCool.setProgress(pWC);
        seekBarBright.setProgress(100);
        rec = 1.0;
        dialog.cancel();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        con = (Controller) getArguments().getSerializable(ARG_CONT);
        currentBtn = getArguments().getInt(HEAD_CODE);
        warm = con.getContBtnWC(currentBtn * 2 - 2);
        cool = con.getContBtnWC(currentBtn * 2 - 1);

        rootView = inflater.inflate(R.layout.con_opt_item, container, false);

        setTitle();
        initWCBarView();

        rootView.findViewById(R.id.btn_set_as_scene).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ControlFragment.handler != null) {
                    Message msg = new Message();
                    msg.what = R.id.errorTip;
                    msg.obj = con;
                    msg.arg1 = currentBtn;
                    ControlFragment.handler.sendMessage(msg);
                }
                ControllerItemOptFragment.this.dismiss();
            }
        });
        return rootView;
    }


    private void initWCBarView() {

        rootView.findViewById(R.id.brightLevelBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warm = 100;
                cool = 0;
                pWC = 0;
                updateWCB(con, currentBtn, (byte) warm, (byte) cool);
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

        rootView.findViewById(R.id.brightLevelBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warm = 100;
                cool = 66;
                pWC = 33;
                updateWCB(con, currentBtn, (byte) warm, (byte) cool);
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

        rootView.findViewById(R.id.brightLevelBtn3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warm = 68;
                cool = 100;
                pWC = 66;
                updateWCB(con, currentBtn, (byte) warm, (byte) cool);
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

        rootView.findViewById(R.id.brightLevelBtn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                warm = 0;
                cool = 100;
                pWC = 100;
                updateWCB(con, currentBtn, (byte) warm, (byte) cool);
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

        seekBarWarmCool = (SeekBar) rootView.findViewById(R.id.seekBarWarmCool);
        seekBarWarmCool.setProgress(getStartProgress());
        seekBarWarmCool.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // p<50 : w=100 , c=p*2
                // p>50 : w=200-p*2 , c=100
                if (progress < 50) {
                    warm = 100;
                    cool = progress * 2;
                } else if (progress == 50) {
                    warm = 100;
                    cool = 100;
                } else {
                    warm = 200 - progress * 2;
                    cool = 100;
                }
                pWC = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // change data array w/c   --update sql CONT_DATA
                // write cmd * rec
                updateWCB(con, currentBtn, (byte) warm, (byte) cool);
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

        seekBarBright = (SeekBar) rootView.findViewById(R.id.seekBarBright);
        seekBarBright.setProgress(getStartRec());
        seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rec = progress / 100.0;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // write cmd * rec
                writeCmd(con, currentBtn, (byte) warm, (byte) cool, rec);
            }
        });

    }

    private int getStartRec() {
        int p = 0;
        byte w = con.getDataBtnWC(currentBtn * 2 - 2);
        byte c = con.getDataBtnWC(currentBtn * 2 - 1);
        if (w == 100) {
            p = con.getContBtnWC(currentBtn * 2 - 2);
        } else {
            p = con.getContBtnWC(currentBtn * 2 - 1);
        }
        return p;
    }

    private int getStartProgress() {
        int p = 0;
        byte w = con.getDataBtnWC(currentBtn * 2 - 2);
        byte c = con.getDataBtnWC(currentBtn * 2 - 1);
        if (w == 100) {
            p = c / 2;
        } else {
            p = 100 - w / 2;
        }
        return p;
    }

    private void setTitle() {
        String title = null;
        if (currentBtn == 1) {
            title = con.getContName() + "  - -  " + con.getBtnName1();
        } else if (currentBtn == 2) {
            title = con.getContName() + "  - -  " + con.getBtnName2();
        } else if (currentBtn == 3) {
            title = con.getContName() + "  - -  " + con.getBtnName3();
        } else if (currentBtn == 4) {
            title = con.getContName() + "  - -  " + con.getBtnName4();
        }
        ((TextView) rootView.findViewById(R.id.headLineTitle)).setText(title);
    }

    /**
     * @param cont
     * @param key  1,2,3,4 means curBtn
     * @param w
     * @param c
     */
    private void updateWCB(Controller cont, int key, byte w, byte c) {
        String infilter = DataBaseColumn.CONT_COD + "=" + cont.getContCode();

        byte[] temp = new byte[8];
        System.arraycopy(cont.getDataArray(), 0, temp, 0, 8);

        temp[key * 2 - 2] = w;
        temp[key * 2 - 1] = c;

        newData = DecodeByte.bytesToString(temp);

        dialog = new CustomDialog(getActivity(), "提交...", mHandler, 3 * 1000);
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_CONT,
                DataBaseColumn.CONT_DATA, newData, infilter);
    }

    private void writeCmd(Controller cont, int key, byte w, byte c, double rec) {
        byte[] temp = new byte[8];
        System.arraycopy(cont.getIntegers(), 0, temp, 0, 8);

        temp[key * 2 - 2] = (byte) (w * rec);
        temp[key * 2 - 1] = (byte) (c * rec);

        Connection.getInstance().cmdContOpt(cont.getContCode(), DecodeByte.bytesToString(temp));
    }
}
