package com.yf.android.simpledome.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;

import java.util.List;

public class SceneContentAdapter extends BaseAdapter {
    private Context mContext;
    private List<Scene> sceneList;

    public SceneContentAdapter(Context context, List<Scene> list) {
        mContext = context;
        sceneList = list;
    }

    @Override
    public int getCount() {
        return sceneList.size();
    }

    @Override
    public Object getItem(int position) {
        return sceneList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_scenecontent_item, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        Scene scene = sceneList.get(position);

        Controller cont = ControllerContent.getContByCode(scene.getContCode());

        if (cont != null) {
            holder.contentContName.setText(cont.getContName());
        }

        if (scene.getBtnChecked(0)) {
            holder.contentBtnA.setImageResource(R.drawable.btn_choosed);
        } else {
            holder.contentBtnA.setImageResource(R.drawable.btn_unchoosed);
        }
        if (scene.getBtnChecked(1)) {
            holder.contentBtnB.setImageResource(R.drawable.btn_choosed);
        } else {
            holder.contentBtnB.setImageResource(R.drawable.btn_unchoosed);
        }
        if (scene.getBtnChecked(2)) {
            holder.contentBtnC.setImageResource(R.drawable.btn_choosed);
        } else {
            holder.contentBtnC.setImageResource(R.drawable.btn_unchoosed);
        }
        if (scene.getBtnChecked(3)) {
            holder.contentBtnD.setImageResource(R.drawable.btn_choosed);
        } else {
            holder.contentBtnD.setImageResource(R.drawable.btn_unchoosed);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView contentContIcon;
        TextView contentContName;
        ImageView contentBtnA;
        ImageView contentBtnB;
        ImageView contentBtnC;
        ImageView contentBtnD;

        public ViewHolder(View view) {
            contentContIcon = (ImageView) view.findViewById(R.id.contentContIcon);
            contentContName = (TextView) view.findViewById(R.id.contentContName);
            contentBtnA = (ImageView) view.findViewById(R.id.contentBtnA);
            contentBtnB = (ImageView) view.findViewById(R.id.contentBtnB);
            contentBtnC = (ImageView) view.findViewById(R.id.contentBtnC);
            contentBtnD = (ImageView) view.findViewById(R.id.contentBtnD);
            view.setTag(this);
        }
    }
}
