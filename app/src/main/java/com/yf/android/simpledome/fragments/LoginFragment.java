package com.yf.android.simpledome.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.utils.FinalPargram;

public class LoginFragment extends Fragment {
    public final static String SHARE_NAME = "yfappinfo";
    public final static String USER_NAME = "username";
    public final static String USER_PWD = "userpwd";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private CustomDialog dialog;

    private EditText inputUesrName;
    private EditText inputPwd;
    private String userName;
    private String pwd;

    private CheckBox isRememberPwd;
    private TextView forgetPwdBtn;
    private Button loginBtn;
    private Button registerBtn;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 0) {// Error tip
                Toast.makeText(getActivity(), (String) msg.obj,
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == R.id.loginOk) {// login ok
                saveUser();
            } else if (msg.what == R.id.none_callback) {// time out
                Toast.makeText(getActivity(), getString(R.string.server_no_response), Toast.LENGTH_SHORT)
                        .show();
            } else if (msg.what == R.id.loginNc) {// login NC
                dialog.cancel();
                Toast.makeText(getActivity(), getString(R.string.wrong_username_or_pwd),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        preferences = getActivity().getSharedPreferences(SHARE_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        View rootView = inflater.inflate(R.layout.layout_login, container, false);
        inputUesrName = (EditText) rootView.findViewById(R.id.inputUesrName);
        inputUesrName.setText(preferences.getString(USER_NAME, ""));

        inputPwd = (EditText) rootView.findViewById(R.id.inputPwd);
        inputPwd.setText(preferences.getString(USER_PWD, ""));

        isRememberPwd = (CheckBox) rootView.findViewById(R.id.isRememberPwd);
        forgetPwdBtn = (TextView) rootView.findViewById(R.id.forgetPwdBtn);
        forgetPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // forgot Pwd view
            }
        });

        loginBtn = (Button) rootView.findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLoginInfo();
            }
        });

        registerBtn = (Button) rootView.findViewById(R.id.registerBtn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // register view
                getFragmentManager().beginTransaction().addToBackStack("login")
                        .replace(R.id.loginActivity, new RegisterFragment()).commit();
            }
        });
        return rootView;
    }

    protected void saveUser() {
        editor.putString(USER_NAME, userName);
        if (isRememberPwd.isChecked()) {
            editor.putString(USER_PWD, pwd);
        } else {
            editor.putString(USER_PWD, null);
        }
        editor.commit();

        //TODO: load server data
        if (SceneInfoFragment.handler != null) {
            SceneInfoFragment.handler.sendEmptyMessage(R.id.loadServerData);
        }
        if (ControlFragment.handler != null) {
            ControlFragment.handler.sendEmptyMessage(R.id.loadServerData);
        }

        dialog.cancel();
        FinalPargram.isLogin = true;
        getActivity().finish();
    }

    private void checkLoginInfo() {
        userName = inputUesrName.getText().toString().trim();
        Message msg = new Message();
        msg.what = 0;
        if (TextUtils.isEmpty(userName)) {
            inputUesrName.setError(getString(R.string.requir_username));
            msg.obj = getString(R.string.requir_username);
            handler.sendMessage(msg);
            return;
        }
        if (!userName.matches("\\w+")) {
            msg.obj = getString(R.string.wrong_format_username);
            handler.sendMessage(msg);
            return;
        }

        pwd = inputPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            msg.obj = getString(R.string.requir_pwd);
            handler.sendMessage(msg);
            return;
        }
        if (!pwd.matches("\\w+")) {
            msg.obj = getString(R.string.wrong_format_pwd);
            handler.sendMessage(msg);
            return;
        }
        dialog = new CustomDialog(getActivity(), getString(R.string.login_btn), handler, 1000 * 4);
        Connection.getInstance().loginCmd(userName, pwd);
    }
}
