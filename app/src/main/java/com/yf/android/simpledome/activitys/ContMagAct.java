package com.yf.android.simpledome.activitys;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.fragments.ControllerManageFragment;

public class ContMagAct extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controler_manage);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.contMagActFrag, new ControllerManageFragment(), "contlist").commit();

        initActionBar();
    }

    private void initActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.contMagToolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.title_activity_cont));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().findFragmentById(R.id.contMagActFrag) instanceof ControllerManageFragment) {
                ContMagAct.this.finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
