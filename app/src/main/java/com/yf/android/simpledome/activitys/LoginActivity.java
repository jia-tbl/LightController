package com.yf.android.simpledome.activitys;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.fragments.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initActionBar();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.loginActivity, new LoginFragment()).commit();
    }

    private void initActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.loginToolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.title_activity_login));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().findFragmentById(R.id.loginActivity) instanceof LoginFragment) {
                LoginActivity.this.finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
