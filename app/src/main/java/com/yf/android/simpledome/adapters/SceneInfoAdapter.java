package com.yf.android.simpledome.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.fragments.SceneInfoFragment.OnListFragmentInteractionListener;

import java.util.List;

public class SceneInfoAdapter extends RecyclerView.Adapter<SceneInfoAdapter.ViewHolder> {

    private final List<SceneInfo> mValues;
    private final OnListFragmentInteractionListener mListener;

    private int itemHeight;

    public SceneInfoAdapter(List<SceneInfo> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_sceneinfo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        if (holder.mItem.getIcon().matches("\\d+")) {
            int key = Integer.parseInt(holder.mItem.getIcon());
            switch (key) {
                case 0:
                    holder.sceneItemImage.setImageResource(R.mipmap.scene_0);
                    break;
                case 1:
                    holder.sceneItemImage.setImageResource(R.mipmap.scene_1);
                    break;
                case 2:
                    holder.sceneItemImage.setImageResource(R.mipmap.scene_2);
            }
        }

        //Glide.with(holder.mView.getContext()).load(Integer.parseInt(holder.mItem.getIcon()))
        //.into(holder.sceneItemImage);

        holder.sceneItemName.setText(mValues.get(position).getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: send scene cmd LIGHT-SC-[场景号]-S
                Connection.getInstance().writeCmd("LIGHT-SC-"
                        + holder.mItem.getCode() + "-S");
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    public int getItemHeight() {
        return itemHeight;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        public ImageView sceneItemImage;
        public TextView sceneItemName;
        public SceneInfo mItem;

        public ViewHolder(final View view) {
            super(view);
            mView = view;
            itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    itemHeight = view.getMeasuredHeight();
                    return true;
                }
            });
            sceneItemImage = (ImageView) view.findViewById(R.id.sceneItemImage);
            sceneItemName = (TextView) view.findViewById(R.id.sceneItemName);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + sceneItemName.getText() + "'";
        }
    }
}
