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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;

public class RegisterFragment extends Fragment {
    private ImageView registerNameState;

    private EditText registerUesrName;
    private EditText registerPwd;
    private EditText registerPwdConfrim;
    private EditText registerPhone;
    private EditText registerEmail;

    private CheckBox checkboxAgree;
    private TextView user_protocol;

    private Button submitRegister;

    private String userName;
    private String pwd;

    private CustomDialog dialog;
    private SharedPreferences.Editor editor;

    private boolean isNameVal = false;
    private final String HEAD_SUBMIT = "RE";
    private final String HEAD_COMFRIM = "QU";

    private Handler comfrimHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.registerOk) {
                registerNameState.setImageResource(R.drawable.ok);
                isNameVal = true;
            } else if (msg.what == R.id.registerNc) {
                if (RegisterFragment.this.isAdded()) {
                    Toast.makeText(getActivity(), "用户名不可用",
                            Toast.LENGTH_SHORT).show();
                }
                registerNameState.setImageResource(R.drawable.not);
                isNameVal = false;
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == R.id.registerOk) {// register OK
                dialog.cancel();
                editor.putString(LoginFragment.USER_NAME, userName);
                editor.putString(LoginFragment.USER_PWD, pwd);
                editor.commit();
                getFragmentManager().popBackStack();
            } else if (msg.what == R.id.registerNc) {// register NC
                dialog.cancel();
                Toast.makeText(getActivity(), getString(R.string.register_failed),
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == R.id.none_callback) {// time out
                Toast.makeText(getActivity(), getString(R.string.server_no_response),
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == 0) {
                Toast.makeText(getActivity(), (String) msg.obj,
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        editor = getActivity().getSharedPreferences(LoginFragment.SHARE_NAME,
                Context.MODE_PRIVATE).edit();
        View rootView = inflater.inflate(R.layout.layout_register, container, false);

        registerUesrName = (EditText) rootView.findViewById(R.id.registerUesrName);
        registerUesrName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    userName = registerUesrName.getText().toString()
                            .trim();
                    if (!TextUtils.isEmpty(userName)) {
                        if (userName.matches("\\w+")) {
                            dialog = new CustomDialog(comfrimHandler);
                            Connection.getInstance().cmdRegister(HEAD_COMFRIM,
                                    userName);
                        } else {
                            comfrimHandler
                                    .sendEmptyMessage(R.id.registerOk);
                        }
                    }
                }
            }
        });
        registerNameState = (ImageView) rootView.findViewById(R.id.registerNameState);

        registerPwd = (EditText) rootView.findViewById(R.id.registerPwd);
        registerPwdConfrim = (EditText) rootView.findViewById(R.id.registerPwdConfrim);
        registerPhone = (EditText) rootView.findViewById(R.id.registerPhone);
        registerEmail = (EditText) rootView.findViewById(R.id.registerEmail);
        checkboxAgree = (CheckBox) rootView.findViewById(R.id.checkboxAgree);
        user_protocol = (TextView) rootView.findViewById(R.id.user_protocol);
        user_protocol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: show agree view
            }
        });
        submitRegister = (Button) rootView.findViewById(R.id.submitRegister);
        submitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRegister();
            }
        });
        return rootView;
    }

    protected void submitRegister() {
        Message message = new Message();
        message.what = 0;

        userName = registerUesrName.getText().toString().trim();
        if (TextUtils.isEmpty(userName)) {
            registerUesrName.requestFocusFromTouch();
            return;
        }
        if (!isNameVal) {
            message.obj = getString(R.string.unavailable_username);
            handler.sendMessage(message);
            return;
        }
        if (!userName.matches("\\w+")) {
            message.obj = getString(R.string.wrong_format_username);
            handler.sendMessage(message);
            return;
        }

        pwd = registerPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            registerPwd.requestFocusFromTouch();
            return;
        }
        if (!pwd.matches("\\w+")) {
            message.obj = getString(R.string.wrong_format_pwd);
            handler.sendMessage(message);
            return;
        }

        String pwd2 = registerPwdConfrim.getText().toString().trim();
        if (!pwd.equals(pwd2)) {
            registerPwdConfrim.requestFocusFromTouch();
            message.obj = getString(R.string.confirm_failed);
            handler.sendMessage(message);
            return;
        }

        String phone = registerPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            registerPhone.requestFocusFromTouch();
            return;
        }
        if (!phone.matches("\\d+")) {
            message.obj = getString(R.string.wrong_phone);
            handler.sendMessage(message);
            return;
        }

        String email = registerEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            registerEmail.requestFocusFromTouch();
            registerEmail.setError("Requite");
            return;
        }
        if (!checkboxAgree.isChecked()) {
            checkboxAgree.requestFocusFromTouch();
            return;
        }
        dialog = new CustomDialog(getActivity(), getString(R.string.register_btn), handler, 3000);
        Connection.getInstance().cmdRegister(HEAD_SUBMIT, userName, phone, email, pwd);
    }
}
