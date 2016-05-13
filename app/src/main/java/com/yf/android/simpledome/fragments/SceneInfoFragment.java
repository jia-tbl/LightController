package com.yf.android.simpledome.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.SceneInfoAdapter;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.fragments.dummy.CustomLinearLayoutManager;
import com.yf.android.simpledome.fragments.dummy.DataChangeListener;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;

public class SceneInfoFragment extends Fragment {
    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";

    // TODO: Customize parameters
    private int mColumnCount = 3;

    private OnListFragmentInteractionListener mListener;


    private RecyclerView recyclerView;
    private SceneInfoAdapter adapter;
    private SwipeRefreshLayout layout;

    private final static long REFRESH_DUR = 5000;
    private static long LAST_REFRESH = System.currentTimeMillis() - REFRESH_DUR;

    public static Handler handler;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.loadServerData) {
                Log.e("START", "-----------------handleMessage:");
                SceneInfoContent.initServerData(new DataChangeListener() {
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
                adapter = new SceneInfoAdapter(SceneInfoContent.ITEMS, mListener);
                recyclerView.setAdapter(adapter);
                layout.setRefreshing(false);
            } else if (msg.what == R.id.noDataLoaded) {
                Toast.makeText(getActivity(), getString(R.string.serverNoDataTip), Toast.LENGTH_LONG).show();
                layout.setRefreshing(false);
            }
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SceneInfoFragment() {
    }

    // TODO: Customize parameter initialization
    public static SceneInfoFragment newInstance(int columnCount) {
        SceneInfoFragment fragment = new SceneInfoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler = mHandler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_sceneinfo_list, container, false);

        handler = mHandler;

        layout = (SwipeRefreshLayout) view.findViewById(R.id.sceneInfoSwipeRefreshLayout);
        Context context = layout.getContext();
        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        adapter = new SceneInfoAdapter(SceneInfoContent.ITEMS, mListener);
        recyclerView.setAdapter(adapter);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new CustomGridLayoutManager(context, mColumnCount));
        }

        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (System.currentTimeMillis() - LAST_REFRESH > REFRESH_DUR) {
                    mHandler.sendEmptyMessage(R.id.loadServerData);
                    LAST_REFRESH = System.currentTimeMillis();
                } else {
                    layout.setRefreshing(false);
                }
            }
        });

        return view;
    }

    class CustomGridLayoutManager extends GridLayoutManager {
        public CustomGridLayoutManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, final int widthSpec, final int heightSpec) {
            try {
                //不能使用   View view = recycler.getViewForPosition(0);
                //measureChild(view, widthSpec, heightSpec);
                // int measuredHeight  view.getMeasuredHeight();  这个高度不准确

                if (adapter != null && adapter.getItemHeight() > 0) {
                    int measuredWidth = View.MeasureSpec.getSize(widthSpec);
                    int measuredHeight = adapter.getItemHeight() + recyclerView.getPaddingBottom()
                            + recyclerView.getPaddingTop();
                    int line = adapter.getItemCount() / getSpanCount();
                    if (adapter.getItemCount() % getSpanCount() > 0) line++;
                    setMeasuredDimension(measuredWidth, measuredHeight * line);
                } else {
                    super.onMeasure(recycler, state, widthSpec, heightSpec);
                }

            } catch (Exception e) {
                super.onMeasure(recycler, state, widthSpec, heightSpec);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(SceneInfo item);
    }
}
