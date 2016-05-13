package com.yf.android.simpledome.zxing.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.fragments.ChooseReplaceController;

public final class CaptureActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_capture);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.scanActToolBar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.title_activity_scan));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        InterFragment fragment = new InterFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.scanActFrag, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().findFragmentById(R.id.scanActFrag) instanceof ChooseReplaceController) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}