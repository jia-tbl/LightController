package com.yf.android.simpledome.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.ContListAdapter;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.fragments.dummy.DataChangeListener;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;

import java.lang.reflect.Field;

public class ControlFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AbsListView mListView;
    public ContListAdapter mAdapter;

    private final static long REFRESH_DUR = 5000;
    private static long LAST_REFRESH = System.currentTimeMillis() - REFRESH_DUR;

    public static Handler handler;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.refresh) {
                mAdapter.notifyDataSetChanged();
            } else if (msg.what == R.id.errorTip) {
                if (SceneInfoContent.ITEMS == null || SceneInfoContent.ITEMS.size() == 0) {
                    Toast.makeText(getActivity(), "未找到场景信息", Toast.LENGTH_LONG).show();
                    return;
                }
                showBtnSceneTip(msg);
            }
            if (msg.what == R.id.loadServerData) {
                ControllerContent.initServerData(new DataChangeListener() {
                    @Override
                    public void onDataChanged() {
                        mHandler.sendEmptyMessage(R.id.loadComplete);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        mHandler.sendEmptyMessage(R.id.noDataLoaded);
                    }
                });
            } else if (msg.what == R.id.loadComplete) {
                mAdapter = new ContListAdapter(getActivity(), ControllerContent.ITEMS, getFragmentManager());
                mListView.setAdapter(mAdapter);
                swipeRefreshLayout.setRefreshing(false);
            } else if (msg.what == R.id.noDataLoaded) {
                Toast.makeText(getActivity(), getString(R.string.serverNoDataTip), Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };

    private void showBtnSceneTip(Message msg) {
        final Controller cont = (Controller) msg.obj;
        final int btnCode = msg.arg1;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setMessage("此操作将会设置当前按键\n" + getCurBtnName(cont, btnCode) +
                " 为场景按键\n当前按键下灯具将不再受控制\n确定继续？");
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showBtnSceneDialog(cont, btnCode);
            }
        });
        builder.setPositiveButton("取消", null);
        builder.show();
    }

    private String getCurBtnName(Controller cont, int btnCode) {
        String title = null;
        if (btnCode == 1) {
            title = cont.getContName() + " - " + cont.getBtnName1();
        } else if (btnCode == 2) {
            title = cont.getContName() + " - " + cont.getBtnName2();
        } else if (btnCode == 3) {
            title = cont.getContName() + " - " + cont.getBtnName3();
        } else if (btnCode == 4) {
            title = cont.getContName() + " - " + cont.getBtnName4();
        }
        return title;
    }

    private void showBtnSceneDialog(Controller cont, int btnCode) {
        ControllerBtnSetAsScene dialog = new ControllerBtnSetAsScene();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ControllerBtnSetAsScene.ARG_CONT, cont);
        bundle.putInt(ControllerBtnSetAsScene.HEAD_CODE, btnCode);
        dialog.setArguments(bundle);

        dialog.show(getFragmentManager(), cont.getContCode());
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        handler = mHandler;
    }

    public static ControlFragment newInstance() {
        return new ControlFragment();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ContListAdapter(getActivity(), ControllerContent.ITEMS, getFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);
        //init handler
        handler = mHandler;
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (System.currentTimeMillis() - LAST_REFRESH > REFRESH_DUR) {
                    mHandler.sendEmptyMessage(R.id.loadServerData);
                    LAST_REFRESH = System.currentTimeMillis();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);
        mListView.setVerticalScrollBarEnabled(false);

        setEmptyText(getString(R.string.empty_text));

        return view;
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
            ((TextView) emptyView).setTextColor(Color.WHITE);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
