package com.yf.android.simpledome.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yf.android.simpledome.R;

public class SceneIconAdapter extends BaseAdapter {
    private Context context;
    private int[] icons = {R.mipmap.scene_0, R.mipmap.scene_1, R.mipmap.scene_2};

    public SceneIconAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(icons[position]);
        return imageView;
    }

}
