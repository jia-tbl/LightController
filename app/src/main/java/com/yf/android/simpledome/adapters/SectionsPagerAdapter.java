package com.yf.android.simpledome.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.fragments.ControlFragment;
import com.yf.android.simpledome.fragments.SceneInfoFragment;
import com.yf.android.simpledome.fragments.SettingFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private int[] icons = {R.drawable.icon_setting_seletor, R.drawable.icon_main_seletor, R.drawable.icon_shopping_seletor};
    ControlFragment fragment = ControlFragment.newInstance();
    private Fragment[] fms = {SettingFragment.newInstance(1), fragment, SceneInfoFragment.newInstance(3)};

    public SectionsPagerAdapter(FragmentManager fm, Context c) {
        super(fm);
        this.mContext = c;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return fms[0];
        } else if (position == 1) {
            return fms[1];
        } else if (position == 2) {
            return fms[2];
        }
        return null;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        fragment.mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return 3;
    }

    public View getTabView(int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_img, null);
        ImageView tabViewMenuIcon = (ImageView) view.findViewById(R.id.tabViewMenuIcon);
        tabViewMenuIcon.setImageResource(icons[position]);
        return view;
    }
}
