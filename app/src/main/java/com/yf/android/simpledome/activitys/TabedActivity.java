package com.yf.android.simpledome.activitys;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.SectionsPagerAdapter;
import com.yf.android.simpledome.connect.ConnectService;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.localdata.LocalControllerDataSource;
import com.yf.android.simpledome.datasource.localdata.LocalSceneDataSource;
import com.yf.android.simpledome.datasource.localdata.LocalSceneInfoDataSource;
import com.yf.android.simpledome.fragments.ControlFragment;
import com.yf.android.simpledome.fragments.LoginFragment;
import com.yf.android.simpledome.fragments.SceneInfoFragment;
import com.yf.android.simpledome.fragments.SettingFragment;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;
import com.yf.android.simpledome.utils.FinalPargram;
import com.yf.android.simpledome.zxing.activity.CaptureActivity;

public class TabedActivity extends AppCompatActivity implements
        ControlFragment.OnFragmentInteractionListener,
        SceneInfoFragment.OnListFragmentInteractionListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout tabLayout;

    // 用于登陆
    private CustomDialog dialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.loginOk) {// login ok
                //TODO: load server data
                if (SettingFragment.handler != null) {
                    SettingFragment.handler.sendEmptyMessage(R.id.loginOk);
                }
                if (SceneInfoFragment.handler != null) {
                    SceneInfoFragment.handler.sendEmptyMessage(R.id.loadServerData);
                }
                if (ControlFragment.handler != null) {
                    ControlFragment.handler.sendEmptyMessage(R.id.loadServerData);
                }
                dialog.cancel();
                FinalPargram.isLogin = true;
            } else if (msg.what == R.id.none_callback) {// time out
                Toast.makeText(TabedActivity.this,
                        getString(R.string.server_no_response), Toast.LENGTH_SHORT)
                        .show();
            }
        }
    };

    private static long exitTime = System.currentTimeMillis() - 2000;

    private ConnectService.CustomBinder mBinder;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectService.CustomBinder) service;
            mBinder.customFunction();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabed);
        bindService();
        initToolBar();
        initTabView();
        initData();
    }

    private void initData() {
        SceneInfoContent.initSceneInfo(this);
        ControllerContent.initController(this);

        SharedPreferences preferences = getSharedPreferences(LoginFragment.SHARE_NAME,
                Context.MODE_PRIVATE);
        final String name = preferences.getString(LoginFragment.USER_NAME, "");
        final String pwd = preferences.getString(LoginFragment.USER_PWD, "");
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd)) {
            if (name.length() > 2 && pwd.length() > 2) {
                dialog = new CustomDialog(TabedActivity.this,
                        getString(R.string.login_btn), mHandler, 1000 * 4);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Connection.getInstance().loginCmd(name, pwd);
                    }
                }, 1000);
            }
        }
    }

    private void bindService() {
        Intent mIntent = new Intent(this, ConnectService.class);
        bindService(mIntent, connection, BIND_AUTO_CREATE);
    }

    private void initTabView() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        for ( int i = 0; i < tabLayout.getTabCount(); i++ ) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
            }
        }
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mViewPager.setCurrentItem(1);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
        }
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tabed, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        if (LocalSceneDataSource.getInstance(null) != null) {
            LocalSceneDataSource.getInstance(null).close();
        }
        if (LocalSceneInfoDataSource.getInstance(null) != null) {
            LocalSceneInfoDataSource.getInstance(null).close();
        }
        if (LocalControllerDataSource.getInstance(null) != null) {
            LocalControllerDataSource.getInstance(null).close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            if (FinalPargram.isLogin) {
                startActivityForResult(new Intent(TabedActivity.this, CaptureActivity.class), 1);
            } else {
                Toast.makeText(TabedActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(TabedActivity.this, LoginActivity.class), 1);
            }
            return true;
        } else if (id == R.id.action_info) {
            return true;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean flag = false;
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime >= 2000) {
                Toast.makeText(this, getString(R.string.again_exit), Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                System.exit(0);
            }
            flag = true;
        }
        return flag;
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public void onListFragmentInteraction(SceneInfo item) {

    }
}
