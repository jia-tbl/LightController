package com.yf.android.simpledome.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.localdata.LocalSceneDataSource;
import com.yf.android.simpledome.datasource.localdata.LocalSceneInfoDataSource;
import com.yf.android.simpledome.fragments.dummy.SceneContentContent;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;
import com.yf.android.simpledome.swipemenulistview.SwipeMenu;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuCreator;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuItem;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuListView;

import java.util.List;

public class SceneManageFragment extends Fragment {
    private SwipeMenuListView mListView;
    private SceneListAdapter mAdapter;
    private SceneInfo deleteInfo;

    private CustomDialog dialog;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == R.id.addOK) {
                deleteSuccess();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "删除失败\n确保关联该场景的控制器全部在线",
                        Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.errorTip) {
                Toast.makeText(getActivity(), (String) msg.obj,
                        Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        }
    };

    private void deleteSuccess() {
        LocalSceneInfoDataSource.getInstance(null).deleteSceneInfo(deleteInfo.getCode());
        if (SceneManageFragment.this.isAdded()) {
            LocalSceneDataSource.getInstance(getActivity()).deleteScene(deleteInfo.getCode(), null);
        }
        SceneInfoContent.ITEMS.remove(deleteInfo);
        mAdapter.notifyDataSetChanged();
        dialog.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initView();
        return mListView;
    }

    private void initView() {
        mListView = new SwipeMenuListView(getActivity());
        mAdapter = new SceneListAdapter(getActivity(), SceneInfoContent.ITEMS);
        mListView.setAdapter(mAdapter);

        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getActivity());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setWidth(dp2px(90));
                deleteItem.setIcon(R.mipmap.menu_cancel);
                menu.addMenuItem(deleteItem);
            }
        };
        // set creator
        mListView.setMenuCreator(creator);

        // step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                deleteInfo = SceneInfoContent.ITEMS.get(position);
                dialog = new CustomDialog(getActivity(), "删除场景", handler,
                        6 * 1000);
                Connection.getInstance()
                        .writeCmd("NODES-DE-" + deleteInfo.getCode() + "-S");
                return true;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SceneInfo info = SceneInfoContent.ITEMS.get(position);
                // 初始化当前场景下 详情表
                SceneContentContent.initLocalData(getActivity(), info.getCode());

                SceneContentFragment fragment = new SceneContentFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable(SceneContentFragment.SCENE_KEY, info);
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().addToBackStack("")
                        .replace(R.id.sceneMagActFrag, fragment).commit();
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }


    class SceneListAdapter extends BaseAdapter {
        private Context mContext;
        private List<SceneInfo> list;

        public SceneListAdapter(Context c, List<SceneInfo> l) {
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
            SceneInfo info = list.get(position);

            if (info.getIcon().matches("\\d+")) {
                int key = Integer.parseInt(info.getIcon());
                switch (key) {
                    case 0:
                        viewHolder.magItemImageView.setImageResource(R.mipmap.scene_0);
                        break;
                    case 1:
                        viewHolder.magItemImageView.setImageResource(R.mipmap.scene_1);
                        break;
                    case 2:
                        viewHolder.magItemImageView.setImageResource(R.mipmap.scene_2);
                }
            }

            viewHolder.contName.setText(info.getName());
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
}
