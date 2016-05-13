package com.yf.android.simpledome.activitys;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.utils.TextFormat;
import com.yf.android.simpledome.utils.WifiAdmin;
import com.yf.android.simpledome.wificonfig.EsptouchAsyncTask3;

public class ConfigActivity extends AppCompatActivity {
    private TextView wifiSSIDTextView;
    private EditText inputWIFIPwd;
    private Button id_btn_config;

    private WifiAdmin admin;
    private String name;
    private String pwd;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.errorTip) {
                Toast.makeText(ConfigActivity.this, "当前无可用WIFI\n请先链接WIFI后再试",
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        initActionBar();

        findById();

        initWifiState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        findById();
        initWifiState();
    }

    private void findById() {
        wifiSSIDTextView = (TextView) findViewById(R.id.wifiSSID);
        inputWIFIPwd = (EditText) findViewById(R.id.inputWIFIPwd);
        id_btn_config = (Button) findViewById(R.id.id_btn_config);
    }

    private void initWifiState() {
        admin = new WifiAdmin(this);
        admin.OpenWifi(this);

        name = admin.GetSSID();

        if (name != null && name.length() > 0) {// 当前连接
            if (wifiSSIDTextView != null) {
                wifiSSIDTextView.setText(name);
            }
            id_btn_config.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendUDPInfos();
                }
            });
        } else {
            mHandler.sendEmptyMessage(R.id.errorTip);
            id_btn_config.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            });
        }
    }

    private void sendUDPInfos() {
        pwd = inputWIFIPwd.getText().toString().trim();
        if (TextUtils.isEmpty(pwd)) {
            inputWIFIPwd.setError("Requrie");
            return;
        }
        String mac = admin.GetBSSID();

        if (isConSpeCharacters(pwd))
            pwd = TextFormat.translateString(pwd);
        if (isConSpeCharacters(name))
            name = TextFormat.translateString(name);

        String[] values = {name, mac, pwd, "NO", "1"};
        // (new EsptouchAsyncTask3(this)).execute(values);
    }


    private void initActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.configToolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.title_activity_config));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //string 为要判断的字符串
    public boolean isConSpeCharacters(String string) {
        if (string.replaceAll("[\u4e00-\u9fa5]*[a-z]*[A-Z]*\\d*-*_*\\s*", "").length() == 0) {
            //不包含特殊字符
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ConfigActivity.this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
