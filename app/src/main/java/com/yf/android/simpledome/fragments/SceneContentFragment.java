package com.yf.android.simpledome.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.adapters.SceneContentAdapter;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.localdata.LocalSceneDataSource;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.fragments.dummy.DataChangeListener;
import com.yf.android.simpledome.fragments.dummy.SceneContentContent;
import com.yf.android.simpledome.swipemenulistview.SwipeMenu;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuCreator;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuItem;
import com.yf.android.simpledome.swipemenulistview.SwipeMenuListView;
import com.yf.android.simpledome.utils.TextFormat;

import java.util.List;

public class SceneContentFragment extends Fragment {
    public static final String SCENE_KEY = "sceneitem";
    private SceneInfo sceneInfo;

    private TextView editSceneName;
    private ImageView editSceneIcon;

    private SwipeRefreshLayout refreshSceneContent;

    private SwipeMenuListView sceneContentList;
    private SceneContentAdapter mAdapter;

    // for delete scene content
    private Scene deleteScene;


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.loadServerData) {
                SceneContentContent.initServerData(new DataChangeListener() {
                    @Override
                    public void onDataChanged() {
                        mHandler.sendEmptyMessage(R.id.loadComplete);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        mHandler.sendEmptyMessage(R.id.noDataLoaded);
                    }
                }, msg.arg1);
            } else if (msg.what == R.id.loadComplete) {
                refresh();
                refreshSceneContent.setRefreshing(false);
            } else if (msg.what == R.id.noDataLoaded) {
                if (SceneContentFragment.this.isAdded()) {
                    Toast.makeText(getContext(), getString(R.string.serverNoDataTip), Toast.LENGTH_LONG).show();
                    refresh();
                }
                refreshSceneContent.setRefreshing(false);
            } else if (msg.what == R.id.addOK) {
                deleteSuccess();
            } else if (msg.what == R.id.errorTip) {
                Toast.makeText(getActivity(), (String) msg.obj,
                        Toast.LENGTH_SHORT).show();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "删除失败",
                        Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        }
    };

    private void refresh() {
        mAdapter = new SceneContentAdapter(getActivity(), SceneContentContent.ITEMS);
        sceneContentList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void deleteSuccess() {
        SceneContentContent.ITEMS.remove(deleteScene);
        dialog.cancel();
        LocalSceneDataSource.getInstance(null)
                .deleteScene(deleteScene.getCode(), deleteScene.getContCode());
        mAdapter.notifyDataSetChanged();
    }

    //TODO: 添加 场景内控制器
    public void showAddSceneContentView() {
        List<Controller> lists = ControllerContent
                .getUnAddedControllers(sceneInfo.getCode());
        if (lists.size() > 0) {// 存在未被场景添加的控制器
            AddSceneContentFragment fragment = new AddSceneContentFragment();

            Bundle bundle = new Bundle();
            bundle.putInt(AddSceneContentFragment.TYPE, AddSceneContentFragment.TYPE_ADD);
            bundle.putSerializable(AddSceneContentFragment.ARG_INFO, sceneInfo);
            fragment.setArguments(bundle);

            getFragmentManager()
                    .beginTransaction()
                    .addToBackStack("abc")
                    .replace(R.id.sceneMagActFrag, fragment)
                    .commit();
        } else {
            Message msg = new Message();
            msg.what = R.id.errorTip;
            msg.obj = "无新控制器可添加";
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_scene_content, container, false);

        sceneInfo = (SceneInfo) getArguments().getSerializable(SCENE_KEY);

        editSceneName = (TextView) rootView.findViewById(R.id.editSceneName);
        editSceneName.setText(sceneInfo.getName());
        editSceneName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change scene name
                TextEditDialog dialog = new TextEditDialog();
                dialog.show(getFragmentManager(), sceneInfo.getName());
            }
        });

        editSceneIcon = (ImageView) rootView.findViewById(R.id.editSceneIcon);
        if (sceneInfo.getIcon().matches("\\d+")) {
            int key = Integer.parseInt(sceneInfo.getIcon());
            switch (key) {
                case 0:
                    editSceneIcon.setImageResource(R.mipmap.scene_0);
                    break;
                case 1:
                    editSceneIcon.setImageResource(R.mipmap.scene_1);
                    break;
                case 2:
                    editSceneIcon.setImageResource(R.mipmap.scene_2);
            }
        }

        editSceneIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: change scene icon
            }
        });
        sceneContentList = (SwipeMenuListView) rootView.findViewById(R.id.sceneContentList);

        initSwipeMenuList();

        refreshSceneContent = (SwipeRefreshLayout) rootView.findViewById(R.id.refreshSceneContent);

        refreshSceneContent.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadServerData();
            }
        });

        refreshSceneContent.setRefreshing(true);

        loadServerData();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = new SceneContentAdapter(getActivity(), SceneContentContent.ITEMS);
        sceneContentList.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    protected void loadServerData() {
        Message msg = new Message();
        msg.what = R.id.loadServerData;
        msg.arg1 = sceneInfo.getCode();
        mHandler.sendMessage(msg);
    }

    private void initSwipeMenuList() {
        //TODO: adapter
        mAdapter = new SceneContentAdapter(getActivity(), SceneContentContent.ITEMS);
        sceneContentList.setAdapter(mAdapter);

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
        sceneContentList.setMenuCreator(creator);

        // step 2. listener item click event
        sceneContentList.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                //TODO: 删除数据集中记录
                deleteScene = SceneContentContent.ITEMS.get(position);
                dialog = new CustomDialog(getActivity(), "删除场景", mHandler,
                        3 * 1000);
                Connection.getInstance().cmdDBDelete(LocalDataSqlHelper.TABLE_SCEN,
                        DataBaseColumn.SCIN_COD + "=" + deleteScene.getCode() + "," +
                                DataBaseColumn.CONT_COD + "=" + deleteScene.getContCode());
                return true;
            }
        });

        sceneContentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO：编辑 场景中 控制器信息，W、C
                if (ControllerContent.ITEMS == null || ControllerContent.ITEMS.size() == 0) {
                    Toast.makeText(getActivity(), "没有查询到该控制器信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                Controller cont = ControllerContent
                        .getContByCode(SceneContentContent.ITEMS.get(position).getContCode());
                if (cont == null) {
                    Toast.makeText(getActivity(), "没有查询到该控制器信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddSceneContentFragment fragment = new AddSceneContentFragment();

                Bundle bundle = new Bundle();
                bundle.putInt(AddSceneContentFragment.TYPE, AddSceneContentFragment.TYPE_EDIT);
                bundle.putSerializable(AddSceneContentFragment.ARG_SCENE,
                        SceneContentContent.ITEMS.get(position));
                fragment.setArguments(bundle);

                getFragmentManager()
                        .beginTransaction()
                        .addToBackStack("abc")
                        .replace(R.id.sceneMagActFrag, fragment)
                        .commit();
            }
        });
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    // for edit sceneInfo name
    private String newName;
    private CustomDialog dialog;

    private Handler mEditNameHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                editNameSuccess();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无响应", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };

    private void editNameSuccess() {
        dialog.cancel();
        sceneInfo.setName(newName);
        editSceneName.setText(newName);
    }

    class TextEditDialog extends DialogFragment {
        private EditText editTextEditText;
        private Button editCommitBtn;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            View rootView = inflater.inflate(R.layout.dialog_edit_text, container, false);

            editTextEditText = (EditText) rootView.findViewById(R.id.editTextEditText);
            editTextEditText.setText(sceneInfo.getName());
            editCommitBtn = (Button) rootView.findViewById(R.id.editCommitBtn);
            editCommitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newName = editTextEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(newName)) {
                        editTextEditText.setError("不能为空");
                        return;
                    }

                    if (!TextFormat.isTextAvailable(newName)) {
                        editTextEditText.setError("不能包含特殊字符");
                        return;
                    }

                    TextEditDialog.this.dismiss();
                    if (!newName.equals(sceneInfo.getName())) {
                        commitNameChange();
                    }
                }
            });
            return rootView;
        }
    }

    private void commitNameChange() {
        dialog = new CustomDialog(getActivity(), "修改...", mEditNameHandler, 3 * 1000);
        String infilter = DataBaseColumn.SCIN_COD + "=" + sceneInfo.getCode();
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_SCIN, DataBaseColumn.SCIN_NAME, newName, infilter);
    }
}
