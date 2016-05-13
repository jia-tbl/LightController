package com.yf.android.simpledome.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.SceneIconAdapter;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;
import com.yf.android.simpledome.utils.TextFormat;

public class AddSceneFragment extends Fragment {
    private View rootView;
    private CustomDialog customDialog;

    private SceneInfo sceneInfo;
    private String sceneName;
    private int iconId;
    private int sceneCod;

    private EditText sceneNameEditText;
    private Spinner sceneIconSpinner;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == R.id.addOK) {
                addSuccess();
            } else if (msg.what == R.id.errorTip) {
                Toast.makeText(getActivity(), (String) msg.obj,
                        Toast.LENGTH_SHORT).show();
                if (customDialog != null) customDialog.cancel();
            }
        }
    };

    private void addSuccess() {
        SceneInfoContent.ITEMS.add(sceneInfo);
        customDialog.cancel();
        getFragmentManager().popBackStack();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_add_scene, container, false);

        sceneNameEditText = (EditText) rootView.findViewById(R.id.sceneNameEditText);

        sceneIconSpinner = (Spinner) rootView.findViewById(R.id.sceneIconSpinner);
        sceneIconSpinner.setAdapter(new SceneIconAdapter(getContext()));
        sceneIconSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View v,
                                               int position, long id) {
                        iconId = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });

        rootView.findViewById(R.id.id_btn_add_scene).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addScene();
            }
        });
        return rootView;
    }

    private void addScene() {
        Message message = new Message();
        message.what = R.id.errorTip;

        sceneCod = SceneInfoContent.getAvailableSceneCode();
        if (sceneCod == -1) {
            message.obj = "不能再继续添加场景，允许最多场景数量为" + SceneInfoContent.COUNT;
            handler.sendMessage(message);
            return;
        }

        sceneName = sceneNameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(sceneName)) {
            message.obj = "请输入场景名";
            sceneNameEditText.setError("Not Empty");
            handler.sendMessage(message);
            return;
        }

        if (!TextFormat.isTextAvailable(sceneName)) {
            sceneNameEditText.setError("不能包含特殊字符");
            return;
        }

        sceneInfo = new SceneInfo();
        sceneInfo.setName(sceneName);
        sceneInfo.setCode(sceneCod);
        sceneInfo.setIcon(iconId + "");

        String[] columns = {DataBaseColumn.SCIN_NAME, DataBaseColumn.SCIN_COD, DataBaseColumn.SCIN_ICON};
        String[] values = {sceneName, sceneCod + "", iconId + ""};

        customDialog = new CustomDialog(getActivity(), "添加场景", handler,
                3 * 1000);
        Connection.getInstance().cmdDBAdd(LocalDataSqlHelper.TABLE_SCIN, columns, values);
    }
}
