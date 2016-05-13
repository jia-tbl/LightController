package com.yf.android.simpledome.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.activitys.ConfigActivity;
import com.yf.android.simpledome.activitys.ContMagAct;
import com.yf.android.simpledome.activitys.LoginActivity;
import com.yf.android.simpledome.activitys.SceneActivity;
import com.yf.android.simpledome.datasource.localdata.LocalControllerDataSource;
import com.yf.android.simpledome.utils.FinalPargram;

public class SettingFragment extends Fragment {
    private ImageView userIconView;
    private TextView userNameTextVIew;

    public static SettingFragment newInstance(int sectionNumber) {
        return new SettingFragment();
    }

    public static Handler handler;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == R.id.loginOk) {// login ok
                userNameTextVIew.setText(getActivity().getSharedPreferences(LoginFragment.SHARE_NAME,
                        Context.MODE_PRIVATE).getString(LoginFragment.USER_NAME, ""));
            } else if (msg.what == R.id.loginNc) {
                userNameTextVIew.setText(getString(R.string.uesr_icon_text));
            }
        }
    };

    public SettingFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        handler = mHandler;
        if (FinalPargram.isLogin) {
            userNameTextVIew.setText(getActivity().getSharedPreferences(LoginFragment.SHARE_NAME,
                    Context.MODE_PRIVATE).getString(LoginFragment.USER_NAME, "USER"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tabed, container, false);

        handler = mHandler;

        rootView.findViewById(R.id.contMagMenuItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContMagAct.class);
                // Intent intent = new Intent(getActivity(), AllPicActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        rootView.findViewById(R.id.sceneMagMenuItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SceneActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        rootView.findViewById(R.id.configLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), ConfigActivity.class), 1);
            }
        });

        rootView.findViewById(R.id.logoutLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FinalPargram.isLogin) showLogoutTips();
            }
        });

        rootView.findViewById(R.id.setLoginLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!FinalPargram.isLogin) {
                    startActivityForResult(new Intent(getActivity(), LoginActivity.class), 0);
                }
            }
        });

        rootView.findViewById(R.id.setLoginLayout).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showIpView();
                return true;
            }
        });

        userIconView = (ImageView) rootView.findViewById(R.id.userIconView);
        userNameTextVIew = (TextView) rootView.findViewById(R.id.userNameTextVIew);
        if (FinalPargram.isLogin) {
            userNameTextVIew.setText(getActivity().getSharedPreferences(LoginFragment.SHARE_NAME,
                    Context.MODE_PRIVATE).getString(LoginFragment.USER_NAME, ""));
        }

        return rootView;
    }

    private void showLogoutTips() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setMessage("退出后当前用户信息将被清除\n确认退出？");
        builder.setNegativeButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocalControllerDataSource.getInstance(getActivity()).deleteTable();
                userNameTextVIew.setText(getString(R.string.uesr_icon_text));
                FinalPargram.isLogin = false;
            }
        });
        builder.setPositiveButton("取消", null);
        builder.show();


    }

    private void showIpView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final View v = LayoutInflater.from(getActivity()).inflate(R.layout.layout_input_port, null);
        builder.setView(v);

        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String port = ((EditText) v.findViewById(R.id.inputPort))
                        .getText().toString().trim();
                String ip = ((EditText) v.findViewById(R.id.inputIP))
                        .getText().toString().trim();
                if (!TextUtils.isEmpty(port) && !TextUtils.isEmpty(ip)) {
                    FinalPargram.SERVER_HOST = ip;
                    FinalPargram.SERVER_PORT = Integer.parseInt(port);
                }
            }
        });
        builder.show();
    }

}
