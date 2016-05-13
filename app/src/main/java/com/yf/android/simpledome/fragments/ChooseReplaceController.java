package com.yf.android.simpledome.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.ControllerListAdapter;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;

public class ChooseReplaceController extends Fragment {
    public static final String CONT_CODE = "controllerCode";

    private String newCode;
    private Controller mController;
    private ControllerListAdapter adapter;

    private CustomDialog mDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                mDialog.cancel();
                mController.setContCode(newCode);
                getActivity().finish();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                mDialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "操作超时,确保网络连接可用", Toast.LENGTH_SHORT).show();
                mDialog.cancel();
            }

        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        newCode = getArguments().getString(CONT_CODE);

        adapter = new ControllerListAdapter(getActivity(), ControllerContent.ITEMS);
        ListView rootView = new ListView(getActivity());
        rootView.setAdapter(adapter);
        rootView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mController = ControllerContent.ITEMS.get(position);
                showReplaceView();
            }
        });
        return rootView;
    }

    private Handler mRpHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                mDialog.cancel();
                commitChange();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                mDialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "操作超时,确保网络连接可用", Toast.LENGTH_SHORT).show();
                mDialog.cancel();
            }

        }
    };

    private void showReplaceView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setMessage("确定替换该控制器？\n替换后该控制器还可以被重新添加");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // NODES-RP-[NEW_CONT_COD]-[OLD_CONT_COD]-S
                //通知服务器替换控制器，等待是否接受替换
                mDialog = new CustomDialog(getActivity(), "替换...", mRpHandler, 6 * 1000);
                Connection.getInstance().writeCmd("NODES-RP-" + newCode
                        + "-" + mController.getContCode() + "-S");
            }
        });
        builder.setPositiveButton("取消", null);
        builder.show();
    }

    private void commitChange() {
        String infilter = DataBaseColumn.CONT_COD + "=" + mController.getContCode();
        mDialog = new CustomDialog(getActivity(), "替换...", mHandler, 3 * 1000);
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_CONT,
                DataBaseColumn.CONT_COD, newCode, infilter);
    }
}
