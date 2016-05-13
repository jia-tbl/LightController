package com.yf.android.simpledome.choosepic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yf.android.simpledome.R;

import java.util.List;


public class ImageAdapter extends BaseAdapter {
    private List<String> mImages;
    private Context mContext;
    private String path;

    public ImageAdapter(Context c, List<String> data, String path) {
        mContext = c;
        mImages = data;
        this.path = path;
    }

    @Override
    public int getCount() {
        return mImages.size();
    }

    @Override
    public Object getItem(int position) {
        return mImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.all_pic_item, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.picItemIamge);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.imageView.setImageResource(R.drawable.not);
        ImageLoader.getInstance(3, ImageLoader.Type.FILO).loadeImage(path + "/" + mImages.get(position), holder.imageView);
        return convertView;
    }

    class ViewHolder {
        private ImageView imageView;
    }
}
