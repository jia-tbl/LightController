package com.yf.android.simpledome.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.localdata.LocalControllerDataSource;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.utils.TextFormat;

public class ScanResultFragment extends Fragment {
    // edit controller logic
    public static final int TYPE_EDIT = 1;
    // scan result logic
    public static final int TYPE_BIND = 2;

    public static final String TYPE = "contType";
    public static final String CONTROLLER_KEY = "controllerArg";

    private Controller mController;
    private int type;

    // for edit
    private String curText;
    private String curColumn;

    private TextView contNameView;
    private TextView editBtnName1;
    private TextView editBtnName2;
    private TextView editBtnName3;
    private TextView editBtnName4;

    private TableRow tableRow1;
    private TableRow tableRow2;
    private TableRow tableRow3;
    private TableRow tableRow4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        type = getArguments().getInt(TYPE);
        mController = (Controller) getArguments().getSerializable(CONTROLLER_KEY);

        View rootView = inflater.inflate(R.layout.controler_edit_view, container, false);

        contNameView = ((TextView) rootView.findViewById(R.id.contNameView));
        editBtnName1 = ((TextView) rootView.findViewById(R.id.editBtnName1));
        editBtnName2 = ((TextView) rootView.findViewById(R.id.editBtnName2));
        editBtnName3 = ((TextView) rootView.findViewById(R.id.editBtnName3));
        editBtnName4 = ((TextView) rootView.findViewById(R.id.editBtnName4));

        tableRow1 = (TableRow) rootView.findViewById(R.id.tableRow1);
        tableRow2 = (TableRow) rootView.findViewById(R.id.tableRow2);
        tableRow3 = (TableRow) rootView.findViewById(R.id.tableRow3);
        tableRow4 = (TableRow) rootView.findViewById(R.id.tableRow4);

        if (mController.getContType() == 1) {
            tableRow4.setVisibility(View.GONE);
            tableRow3.setVisibility(View.GONE);
            tableRow2.setVisibility(View.GONE);
        } else if (mController.getContType() == 2) {
            tableRow4.setVisibility(View.GONE);
            tableRow3.setVisibility(View.GONE);
        } else if (mController.getContType() == 3) {
            tableRow4.setVisibility(View.GONE);
        }

        ((TextView) rootView.findViewById(R.id.contCodeView))
                .setText(mController.getContCode());
        ((TextView) rootView.findViewById(R.id.contTypeView))
                .setText(mController.getContType() + "");

        contNameView.setText(mController.getContName());
        editBtnName1.setText(mController.getBtnName1());
        editBtnName2.setText(mController.getBtnName2());
        editBtnName3.setText(mController.getBtnName3());
        editBtnName4.setText(mController.getBtnName4());


        contNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEditDialog dialog = new TextEditDialog();
                curText = mController.getContName();
                curColumn = DataBaseColumn.CONT_NAME;
                dialog.show(getFragmentManager(), curText);
            }
        });
        editBtnName1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEditDialog dialog = new TextEditDialog();
                curText = mController.getBtnName1();
                curColumn = DataBaseColumn.CONT_BA;
                dialog.show(getFragmentManager(), curText);
            }
        });
        editBtnName2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEditDialog dialog = new TextEditDialog();
                curText = mController.getBtnName2();
                curColumn = DataBaseColumn.CONT_BB;
                dialog.show(getFragmentManager(), curText);
            }
        });
        editBtnName3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEditDialog dialog = new TextEditDialog();
                curText = mController.getBtnName3();
                curColumn = DataBaseColumn.CONT_BC;
                dialog.show(getFragmentManager(), curText);
            }
        });
        editBtnName4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextEditDialog dialog = new TextEditDialog();
                curText = mController.getBtnName4();
                curColumn = DataBaseColumn.CONT_BD;
                dialog.show(getFragmentManager(), curText);
            }
        });

        rootView.findViewById(R.id.commitBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == TYPE_BIND) {
                    // add controller to server sql
                    addControllerToServer();
                } else if (type == TYPE_EDIT) {
                    getFragmentManager().popBackStack();
                }
            }
        });

        if (type == TYPE_BIND) {
            // 询问用户是否替换已有控制器
            showTipReplaceView();
        }
        return rootView;
    }

    /**
     * 询问用户是否替换已有控制器
     */
    private void showTipReplaceView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("提示");
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setMessage("是否替换现有控制器？");
        builder.setNegativeButton("是，选择替换", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showChooseReplaceControllerView();
            }
        });
        builder.setPositiveButton("否，添加新控制器", null);
        builder.show();
    }

    private void showChooseReplaceControllerView() {
        ChooseReplaceController fragment = new ChooseReplaceController();
        Bundle bundle = new Bundle();
        bundle.putString(ChooseReplaceController.CONT_CODE, mController.getContCode());
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.scanActFrag, fragment)
                .addToBackStack("twoTwo").commit();
    }

    private Handler mAddHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                addSuccess();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "添加失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无响应", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };

    private void addSuccess() {
        dialog.cancel();
        LocalControllerDataSource.getInstance(getActivity()).insertController(mController);
        ControllerContent.ITEMS.add(mController);
        getActivity().finish();
    }

    private void addControllerToServer() {
        dialog = new CustomDialog(getActivity(), "提交...", mAddHandler, 3 * 1000);
        String[] columns = {DataBaseColumn.CONT_COD, DataBaseColumn.CONT_NAME, DataBaseColumn.CONT_DATA, DataBaseColumn.CONT_BA,
                DataBaseColumn.CONT_BAS, DataBaseColumn.CONT_BB, DataBaseColumn.CONT_BBS, DataBaseColumn.CONT_BC, DataBaseColumn.CONT_BCS,
                DataBaseColumn.CONT_BD, DataBaseColumn.CONT_BDS, DataBaseColumn.CONT_ON, DataBaseColumn.CONT_TYPE, DataBaseColumn.CONT_OPT};
        String[] vslues = {mController.getContCode(), mController.getContName(), mController.getContData(), mController.getBtnName1(), mController.getBtnScene1() + "",
                mController.getBtnName2(), mController.getBtnScene2() + "", mController.getBtnName3(), mController.getBtnScene3() + "",
                mController.getBtnName4(), mController.getBtnScene4() + "", mController.getContOnline() + "", mController.getContType() + "", mController.getOptData()};
        Connection.getInstance().cmdDBAdd(LocalDataSqlHelper.TABLE_CONT, columns, vslues);
    }

    class TextEditDialog extends DialogFragment {
        private EditText editTextEditText;
        private Button editCommitBtn;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            View rootView = inflater.inflate(R.layout.dialog_edit_text, container, false);

            editTextEditText = (EditText) rootView.findViewById(R.id.editTextEditText);
            editTextEditText.setText(curText);
            editCommitBtn = (Button) rootView.findViewById(R.id.editCommitBtn);
            editCommitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = editTextEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(text)) {
                        editTextEditText.setError("不能为空");
                        return;
                    }

                    if (!TextFormat.isTextAvailable(text)) {
                        editTextEditText.setError("不能包含特殊字符");
                        return;
                    }

                    TextEditDialog.this.dismiss();

                    if (!curText.equals(text) && type == TYPE_EDIT) {
                        curText = text;
                        commitChange();
                    }
                    if (!curText.equals(text) && type == TYPE_BIND) {
                        curText = text;
                        mHandler.sendEmptyMessage(R.id.errorTip);
                    }
                }
            });
            return rootView;
        }
    }

    private void setTextChange() {
        if (curColumn.equals(DataBaseColumn.CONT_NAME)) {
            mController.setContName(curText);
            contNameView.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BA)) {
            mController.setBtnName1(curText);
            editBtnName1.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BB)) {
            mController.setBtnName2(curText);
            editBtnName2.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BC)) {
            mController.setBtnName3(curText);
            editBtnName3.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BD)) {
            mController.setBtnName4(curText);
            editBtnName4.setText(curText);
        }
    }

    private CustomDialog dialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                saveChange();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "修改失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.errorTip) {
                setTextChange();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无响应", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };

    private void saveChange() {
        if (curColumn.equals(DataBaseColumn.CONT_NAME)) {
            mController.setContName(curText);
            contNameView.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BA)) {
            mController.setBtnName1(curText);
            editBtnName1.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BB)) {
            mController.setBtnName2(curText);
            editBtnName2.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BC)) {
            mController.setBtnName3(curText);
            editBtnName3.setText(curText);
        } else if (curColumn.equals(DataBaseColumn.CONT_BD)) {
            mController.setBtnName4(curText);
            editBtnName4.setText(curText);
        }
        LocalControllerDataSource.getInstance(null)
                .updateController(mController.getContCode(), curColumn, curText);
        dialog.cancel();
    }

    private void commitChange() {
        dialog = new CustomDialog(getActivity(), "提交...", mHandler, 3 * 1000);
        String infilter = DataBaseColumn.CONT_COD + "=" + mController.getContCode();
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_CONT, curColumn, curText, infilter);
    }
}
