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

import java.util.List;

public class ControllerListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Controller> list;

    public ControllerListAdapter(Context c, List<Controller> l) {
        this.list = l;
        this.mContext = c;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.controler_list_item_view, null);
            new ViewHolder(convertView);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.magItemImageView.setImageResource(R.mipmap.ic_launcher);
        //Glide.with(mContext).load(R.mipmap.ic_launcher)
        //.fitCenter().into(viewHolder.magItemImageView);
        viewHolder.contName.setText(list.get(position).getContName());
        return convertView;
    }

    class ViewHolder {
        TextView contName;
        ImageView magItemImageView;

        public ViewHolder(View view) {
            magItemImageView = (ImageView) view.findViewById(R.id.magItemImageView);
            contName = (TextView) view.findViewById(R.id.contNameListText);
            view.setTag(this);
        }
    }
}