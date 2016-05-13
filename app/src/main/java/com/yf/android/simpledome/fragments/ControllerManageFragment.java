package com.yf.android.simpledome.fragments;

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
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.ControllerListAdapter;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.localdata.LocalControllerDataSource;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.swipemenulistview.SwipeMenu;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuCreator;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuItem;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuListView;

public class ControllerManageFragment extends Fragment {
    private SwipeMenuListView mListView;
    private ControllerListAdapter mAdapter;

    private Controller deleteCont;

    private CustomDialog dialog;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == R.id.addOK) {
                deleteSuccess();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "删除失败",
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
        ControllerContent.ITEMS.remove(deleteCont);
        LocalControllerDataSource.getInstance(null)
                .deleteController(deleteCont.getContCode());
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
        mAdapter = new ControllerListAdapter(getActivity(), ControllerContent.ITEMS);
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
                deleteCont = ControllerContent.ITEMS.get(position);
                dialog = new CustomDialog(getActivity(), "删除控制器", handler,
                        3 * 1000);
                Connection.getInstance().cmdDBDelete(LocalDataSqlHelper.TABLE_CONT,
                        DataBaseColumn.CONT_COD + "=" + deleteCont.getContCode());
                return true;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ScanResultFragment fragment = new ScanResultFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(ScanResultFragment.TYPE, ScanResultFragment.TYPE_EDIT);
                bundle.putSerializable(ScanResultFragment.CONTROLLER_KEY, ControllerContent.ITEMS.get(position));
                fragment.setArguments(bundle);
                getFragmentManager().beginTransaction().addToBackStack("")
                        .replace(R.id.contMagActFrag, fragment).commit();
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
