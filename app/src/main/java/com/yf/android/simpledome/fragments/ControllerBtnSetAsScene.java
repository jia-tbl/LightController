package com.yf.android.simpledome.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;

public class ControllerBtnSetAsScene extends DialogFragment {
    public static final String HEAD_CODE = "btnCode";
    public static final String ARG_CONT = "controllerArg";

    private Controller con;
    private int currentBtn;

    private int oldSceneCode = 0;
    private SceneInfo curSceneInfo;

    private LinearLayout alreadyCommitView;
    private TextView curSceneName;

    private Spinner sceneChooseSpinner;


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
        if (currentBtn == 1) {
            con.setBtnScene1(curSceneInfo.getCode());
        } else if (currentBtn == 2) {
            con.setBtnScene2(curSceneInfo.getCode());
        } else if (currentBtn == 3) {
            con.setBtnScene3(curSceneInfo.getCode());
        } else if (currentBtn == 4) {
            con.setBtnScene4(curSceneInfo.getCode());
        }

        if (curSceneInfo.getCode() != 0) {
            alreadyCommitView.setVisibility(View.VISIBLE);
            curSceneName.setText(curSceneInfo.getName());
            Toast.makeText(getActivity(), "关联成功", Toast.LENGTH_SHORT).show();
        } else {
            curSceneName.setText("");
            Toast.makeText(getActivity(), "关联已取消", Toast.LENGTH_SHORT).show();
            alreadyCommitView.setVisibility(View.GONE);
        }

        if (ControlFragment.handler != null) {
            ControlFragment.handler.sendEmptyMessage(R.id.refresh);
        }

        dialog.cancel();
        ControllerBtnSetAsScene.this.dismiss();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        con = (Controller) getArguments().getSerializable(ARG_CONT);
        currentBtn = getArguments().getInt(HEAD_CODE);
        View rootView = inflater.inflate(R.layout.con_opt_item_set_as_scene, container, false);

        sceneChooseSpinner = (Spinner) rootView.findViewById(R.id.sceneChooseSpinner);
        alreadyCommitView = (LinearLayout) rootView.findViewById(R.id.alreadyCommitView);
        curSceneName = ((TextView) rootView.findViewById(R.id.curSceneName));

        // init Spinner
        String[] sceneNames = new String[SceneInfoContent.ITEMS.size() + 1];
        sceneNames[0] = "无";
        for ( int i = 1; i < sceneNames.length; i++ ) {
            sceneNames[i] = SceneInfoContent.ITEMS.get(i - 1).getName();
        }
        curSceneInfo = new SceneInfo();
        curSceneInfo.setCode(0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, sceneNames);
        sceneChooseSpinner.setAdapter(adapter);

        // 初始化 当前 btn 状态
        initAlreadySetLayout();

        sceneChooseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    curSceneInfo = new SceneInfo();
                    curSceneInfo.setCode(0);
                    return;
                }
                curSceneInfo = SceneInfoContent.ITEMS.get(position - 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // init title
        String title = getTitleByType();
        ((TextView) rootView.findViewById(R.id.headLineTitle)).setText(title);

        // btn commit
        rootView.findViewById(R.id.id_btn_set_as).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (oldSceneCode != curSceneInfo.getCode()) {// 场景被修改
                    // 通知服务器修改  btn  为 场景按键
                    nodeServerUpdate(con.getContCode(), currentBtn, curSceneInfo.getCode());
                } else {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            }
        });

        return rootView;
    }

    private void initAlreadySetLayout() {
        if (currentBtn == 1) {
            if (con.getBtnScene1() == 0) {
                alreadyCommitView.setVisibility(View.GONE);
                sceneChooseSpinner.setSelection(0);
            } else {
                oldSceneCode = con.getBtnScene1();
                curSceneInfo = SceneInfoContent.ITEMS.get(SceneInfoContent.getIndexByCode(oldSceneCode));
                sceneChooseSpinner.setSelection(SceneInfoContent.getIndexByCode(oldSceneCode));
                curSceneName.setText(SceneInfoContent.getSceneNameByCode(oldSceneCode));
            }
        } else if (currentBtn == 2) {
            if (con.getBtnScene2() == 0) {
                alreadyCommitView.setVisibility(View.GONE);
                sceneChooseSpinner.setSelection(0);
            } else {
                oldSceneCode = con.getBtnScene2();
                curSceneInfo = SceneInfoContent.ITEMS.get(SceneInfoContent.getIndexByCode(oldSceneCode));
                sceneChooseSpinner.setSelection(SceneInfoContent.getIndexByCode(oldSceneCode));
                curSceneName.setText(SceneInfoContent.getSceneNameByCode(oldSceneCode));
            }
        } else if (currentBtn == 3) {
            if (con.getBtnScene3() == 0) {
                alreadyCommitView.setVisibility(View.GONE);
                sceneChooseSpinner.setSelection(0);
            } else {
                oldSceneCode = con.getBtnScene3();
                curSceneInfo = SceneInfoContent.ITEMS.get(SceneInfoContent.getIndexByCode(oldSceneCode));
                sceneChooseSpinner.setSelection(SceneInfoContent.getIndexByCode(oldSceneCode));
                curSceneName.setText(SceneInfoContent.getSceneNameByCode(oldSceneCode));
            }
        } else if (currentBtn == 4) {
            if (con.getBtnScene4() == 0) {
                alreadyCommitView.setVisibility(View.GONE);
                sceneChooseSpinner.setSelection(0);
            } else {
                oldSceneCode = con.getBtnScene4();
                curSceneInfo = SceneInfoContent.ITEMS.get(SceneInfoContent.getIndexByCode(oldSceneCode));
                sceneChooseSpinner.setSelection(SceneInfoContent.getIndexByCode(oldSceneCode));
                curSceneName.setText(SceneInfoContent.getSceneNameByCode(oldSceneCode));
            }
        }
    }

    private Handler mNodeHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                dialog.cancel();
                updateBtnScene(con, currentBtn, curSceneInfo.getCode());
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "提交失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无应答", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };

    /**
     * 通知服务器修改  btn  为 场景按键
     *
     * @param contCode
     * @param currentBtn
     * @param code
     */
    private void nodeServerUpdate(String contCode, int currentBtn, int code) {
        // NODES-CU-[CONT_COD]-[CONT_KEY]-[SCIN_COD]-S
        dialog = new CustomDialog(getActivity(), "提交...", mNodeHandler, 5 * 1000);
        Connection.getInstance().writeCmd("NODES-CU-" + contCode + "-" + currentBtn +
                "-" + code + "-S");
    }

    private String getTitleByType() {
        String title = "";
        if (con.getContType() == 4) {
            if (currentBtn == 1) {
                title = con.getContName() + "  - -  " + con.getBtnName1();
            } else if (currentBtn == 2) {
                title = con.getContName() + "  - -  " + con.getBtnName2();
            } else if (currentBtn == 3) {
                title = con.getContName() + "  - -  " + con.getBtnName3();
            } else if (currentBtn == 4) {
                title = con.getContName() + "  - -  " + con.getBtnName4();
            }
        } else if (con.getContType() == 3) {
            if (currentBtn == 1) {
                title = con.getContName() + "  - -  " + con.getBtnName1();
            } else if (currentBtn == 2) {
                title = con.getContName() + "  - -  " + con.getBtnName2();
            } else if (currentBtn == 3) {
                title = con.getContName() + "  - -  " + con.getBtnName3();
            } else if (currentBtn == 4) {
                title = con.getContName() + "  - -  " + getString(R.string.btn_text_wc);
            }
        } else {
            if (currentBtn == 1) {
                title = con.getContName() + "  - -  " + con.getBtnName1();
            } else if (currentBtn == 2) {
                title = con.getContName() + "  - -  " + con.getBtnName2();
            } else if (currentBtn == 3) {
                title = con.getContName() + "  - -  " + getString(R.string.btn_text_wc);
            } else if (currentBtn == 4) {
                title = con.getContName() + "  - -  " + getString(R.string.btn_text_wc);
            }
        }
        return title;
    }

    /**
     * @param cont
     * @param key       1,2,3,4
     * @param sceneCode
     */
    private void updateBtnScene(Controller cont, int key, int sceneCode) {
        String column = DataBaseColumn.CONT_BAS;
        if (key == 1) {
            column = DataBaseColumn.CONT_BAS;
        } else if (key == 2) {
            column = DataBaseColumn.CONT_BBS;
        } else if (key == 3) {
            column = DataBaseColumn.CONT_BCS;
        } else if (key == 4) {
            column = DataBaseColumn.CONT_BDS;
        }
        String infilter = DataBaseColumn.CONT_COD + "=" + cont.getContCode();
        dialog = new CustomDialog(getActivity(), "关联...", mHandler, 3 * 1000);
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_CONT,
                column, sceneCode + "", infilter);
    }
}
