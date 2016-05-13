package com.yf.android.simpledome.activitys;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.fragments.AddSceneFragment;
import com.yf.android.simpledome.fragments.SceneContentFragment;
import com.yf.android.simpledome.fragments.SceneManageFragment;

public class SceneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.sceneMagActFrag, new SceneManageFragment(), "sceneLists")
                .commit();

        initActionBar();
    }

    private void initActionBar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.sceneMagToolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(getString(R.string.set_item_mag_s));
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scene, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            if (getSupportFragmentManager().findFragmentById(R.id.sceneMagActFrag)
                    instanceof SceneManageFragment) {// 只在场景列表界面添加
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack("aaa" + Math.random() * 10)
                        .replace(R.id.sceneMagActFrag, new AddSceneFragment())
                        .commit();
            }
            if (getSupportFragmentManager().findFragmentById(R.id.sceneMagActFrag)
                    instanceof SceneContentFragment) {
                SceneContentFragment fragment = (SceneContentFragment) getSupportFragmentManager().
                        findFragmentById(R.id.sceneMagActFrag);
                fragment.showAddSceneContentView();
            }
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().findFragmentById(R.id.sceneMagActFrag)
                    instanceof SceneManageFragment) {
                SceneActivity.this.finish();
            } else {
                getSupportFragmentManager().popBackStack();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
