package com.yf.android.simpledome.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.connect.CustomDialog;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.datasource.Scene;
import com.yf.android.simpledome.datasource.SceneInfo;
import com.yf.android.simpledome.datasource.sqlite.DataBaseColumn;
import com.yf.android.simpledome.datasource.sqlite.LocalDataSqlHelper;
import com.yf.android.simpledome.fragments.dummy.ControllerContent;
import com.yf.android.simpledome.fragments.dummy.SceneContentContent;
import com.yf.android.simpledome.utils.DecodeByte;

import java.util.List;

public class AddSceneContentFragment extends Fragment {
    public static String ARG_INFO = "sceneInfo";
    public static String ARG_SCENE = "scene";

    public static String TYPE = "opt_type";
    public static final int TYPE_ADD = 1;
    public static final int TYPE_EDIT = 2;

    private int type;

    private Scene newScene;
    private SceneInfo curSceneInfo;
    private Controller curCont;

    private LinearLayout layout_cont_choose;
    private TextView controllerName;
    private LinearLayout layout_cont;

    private Spinner controllerSpinner;

    private LinearLayout layout_ck_2;
    private LinearLayout layout_ck_3;
    private LinearLayout layout_ck_4;

    private CheckBox id_ck_btn1;
    private CheckBox id_ck_btn2;
    private CheckBox id_ck_btn3;
    private CheckBox id_ck_btn4;

    private LinearLayout btn1_WC_view;
    private LinearLayout btn2_WC_view;
    private LinearLayout btn3_WC_view;
    private LinearLayout btn4_WC_view;

    private Spinner btn1_W_Spinner;
    private Spinner btn1_C_Spinner;
    private Spinner btn2_W_Spinner;
    private Spinner btn2_C_Spinner;
    private Spinner btn3_W_Spinner;
    private Spinner btn3_C_Spinner;
    private Spinner btn4_W_Spinner;
    private Spinner btn4_C_Spinner;

    private Button id_btn_add_scene;

    private ArrayAdapter<Integer> wcAdapter;
    private Integer[] integers = new Integer[100];

    private CustomDialog dialog;
    private Handler mEditHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {

                newScene.setData(DecodeByte.bytesToString(newScene.getIntegers()));
                dialog.cancel();
                getFragmentManager().popBackStack();

            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "保存失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无响应", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }

        }
    };
    private Handler mADDHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.addOK) {
                SceneContentContent.ITEMS.add(newScene);
                dialog.cancel();
                getFragmentManager().popBackStack();
            } else if (msg.what == R.id.addNC) {
                Toast.makeText(getActivity(), "添加失败", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            } else if (msg.what == R.id.none_callback) {
                Toast.makeText(getActivity(), "服务器无响应", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_scene_content, container, false);

        findById(rootView);

        for ( int i = 0; i < 100; i++ ) {
            integers[i] = i + 1;
        }
        wcAdapter = new ArrayAdapter<Integer>(getActivity(), android.R.layout.simple_spinner_item, integers);

        type = getArguments().getInt(TYPE);

        if (type == TYPE_ADD) {
            curSceneInfo = (SceneInfo) getArguments().getSerializable(ARG_INFO);
            initAddView();
        } else if (type == TYPE_EDIT) {
            newScene = (Scene) getArguments().getSerializable(ARG_SCENE);
            layout_cont_choose.setVisibility(View.GONE);
            curCont = ControllerContent.getContByCode(newScene.getContCode());
            controllerName.setText(curCont.getContName());
            initEditView(curCont);
        }
        id_btn_add_scene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == TYPE_ADD) {
                    // add newScene
                    newScene.setData(DecodeByte.bytesToString(newScene.getIntegers()));
                    commitAdd();
                } else if (type == TYPE_EDIT) {
                    // update scene content data
                    commitEdit();
                }
            }
        });
        return rootView;
    }

    private void commitAdd() {
        dialog = new CustomDialog(getActivity(), "添加...", mADDHandler, 3 * 1000);
        String[] columns = {DataBaseColumn.SCIN_COD, DataBaseColumn.CONT_COD, DataBaseColumn.CONT_DATA};
        String[] values = {newScene.getCode() + "", newScene.getContCode(), newScene.getData()};
        Connection.getInstance().cmdDBAdd(LocalDataSqlHelper.TABLE_SCEN, columns, values);
    }

    private void commitEdit() {
        dialog = new CustomDialog(getActivity(), "保存...", mEditHandler, 3 * 1000);
        String infilter = DataBaseColumn.CONT_COD + "=" + newScene.getContCode() + "," +
                DataBaseColumn.SCIN_COD + "=" + newScene.getCode();
        String value = DecodeByte.bytesToString(newScene.getIntegers());
        Connection.getInstance().cmdDBUpdate(LocalDataSqlHelper.TABLE_SCEN,
                DataBaseColumn.CONT_DATA, value, infilter);
    }

    private void initAddView() {
        layout_cont.setVisibility(View.GONE);
        final List<Controller> lists = ControllerContent
                .getUnAddedControllers(curSceneInfo.getCode());

        newScene = new Scene();
        newScene.setCode(curSceneInfo.getCode());
        newScene.setContCode(lists.get(0).getContCode());
        newScene.setData("0000000000000000");

        initEditView(lists.get(0));

        String[] names = new String[lists.size()];
        for ( int i = 0; i < names.length; i++ ) {
            names[i] = lists.get(i).getContName();
        }
        controllerSpinner.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, names));
        controllerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initEditView(lists.get(position));
                newScene.setContCode(lists.get(position).getContCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initEditView(Controller curCont) {
        if (curCont.getContType() == 1) {
            layout_ck_4.setVisibility(View.GONE);
            layout_ck_3.setVisibility(View.GONE);
            layout_ck_2.setVisibility(View.GONE);
        } else if (curCont.getContType() == 2) {
            layout_ck_4.setVisibility(View.GONE);
            layout_ck_3.setVisibility(View.GONE);
            layout_ck_2.setVisibility(View.VISIBLE);
        } else if (curCont.getContType() == 3) {
            layout_ck_4.setVisibility(View.GONE);
            layout_ck_3.setVisibility(View.VISIBLE);
            layout_ck_2.setVisibility(View.VISIBLE);
        } else if (curCont.getContType() == 4) {
            layout_ck_4.setVisibility(View.VISIBLE);
            layout_ck_3.setVisibility(View.VISIBLE);
            layout_ck_2.setVisibility(View.VISIBLE);
        }

        if (newScene.getBtnChecked(0)) {
            id_ck_btn1.setChecked(true);
            btn1_WC_view.setVisibility(View.VISIBLE);
        }
        if (newScene.getBtnChecked(1)) {
            id_ck_btn2.setChecked(true);
            btn2_WC_view.setVisibility(View.VISIBLE);
        }
        if (newScene.getBtnChecked(2)) {
            id_ck_btn3.setChecked(true);
            btn3_WC_view.setVisibility(View.VISIBLE);
        }
        if (newScene.getBtnChecked(3)) {
            id_ck_btn4.setChecked(true);
            btn4_WC_view.setVisibility(View.VISIBLE);
        }

        id_ck_btn1.setText(curCont.getBtnName1());
        id_ck_btn2.setText(curCont.getBtnName2());
        id_ck_btn3.setText(curCont.getBtnName3());
        id_ck_btn4.setText(curCont.getBtnName4());

        id_ck_btn1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn1_WC_view.setVisibility(View.VISIBLE);
                    newScene.setBtnWC(0, 100);
                    newScene.setBtnWC(1, 100);
                    btn1_W_Spinner.setSelection(99);
                    btn1_C_Spinner.setSelection(99);
                } else {
                    btn1_WC_view.setVisibility(View.GONE);
                    newScene.setBtnWC(0, 0);
                    newScene.setBtnWC(1, 0);
                }
            }
        });

        id_ck_btn2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn2_WC_view.setVisibility(View.VISIBLE);
                    newScene.setBtnWC(2, 100);
                    newScene.setBtnWC(3, 100);
                    btn2_W_Spinner.setSelection(99);
                    btn2_C_Spinner.setSelection(99);
                } else {
                    btn2_WC_view.setVisibility(View.GONE);
                    newScene.setBtnWC(2, 0);
                    newScene.setBtnWC(3, 0);
                }
            }
        });

        id_ck_btn3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn3_WC_view.setVisibility(View.VISIBLE);
                    newScene.setBtnWC(4, 100);
                    newScene.setBtnWC(5, 100);
                    btn3_W_Spinner.setSelection(99);
                    btn3_C_Spinner.setSelection(99);
                } else {
                    btn3_WC_view.setVisibility(View.GONE);
                    newScene.setBtnWC(4, 0);
                    newScene.setBtnWC(5, 0);
                }
            }
        });

        id_ck_btn4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btn4_WC_view.setVisibility(View.VISIBLE);
                    newScene.setBtnWC(6, 100);
                    newScene.setBtnWC(7, 100);
                    btn4_W_Spinner.setSelection(99);
                    btn4_C_Spinner.setSelection(99);
                } else {
                    btn4_WC_view.setVisibility(View.GONE);
                    newScene.setBtnWC(6, 0);
                    newScene.setBtnWC(7, 0);
                }
            }
        });

        btn1_W_Spinner.setAdapter(wcAdapter);
        btn1_W_Spinner.setSelection(newScene.getBtnWC(0) - 1);
        btn1_W_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(0, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(0, 0);
            }
        });
        btn2_W_Spinner.setAdapter(wcAdapter);
        btn2_W_Spinner.setSelection(newScene.getBtnWC(2) - 1);
        btn2_W_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(2, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(2, 0);
            }
        });
        btn3_W_Spinner.setAdapter(wcAdapter);
        btn3_W_Spinner.setSelection(newScene.getBtnWC(4) - 1);
        btn3_W_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(4, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(4, 0);
            }
        });
        btn4_W_Spinner.setAdapter(wcAdapter);
        btn4_W_Spinner.setSelection(newScene.getBtnWC(6) - 1);
        btn4_W_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(6, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(6, 0);
            }
        });
        btn4_C_Spinner.setAdapter(wcAdapter);
        btn4_C_Spinner.setSelection(newScene.getBtnWC(7) - 1);
        btn4_C_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(7, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(7, 0);
            }
        });
        btn3_C_Spinner.setAdapter(wcAdapter);
        btn3_C_Spinner.setSelection(newScene.getBtnWC(5) - 1);
        btn3_C_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(5, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(5, 0);
            }
        });
        btn2_C_Spinner.setAdapter(wcAdapter);
        btn2_C_Spinner.setSelection(newScene.getBtnWC(3) - 1);
        btn2_C_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(3, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(3, 0);
            }
        });
        btn1_C_Spinner.setAdapter(wcAdapter);
        btn1_C_Spinner.setSelection(newScene.getBtnWC(1) - 1);
        btn1_C_Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newScene.setBtnWC(1, position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                newScene.setBtnWC(1, 0);
            }
        });
    }

    private void findById(View rootView) {
        layout_cont_choose = (LinearLayout) rootView.findViewById(R.id.layout_cont_choose);
        layout_cont = (LinearLayout) rootView.findViewById(R.id.layout_cont);

        controllerName = (TextView) rootView.findViewById(R.id.controllerName);
        controllerSpinner = (Spinner) rootView.findViewById(R.id.controllerSpinner);

        layout_ck_2 = (LinearLayout) rootView.findViewById(R.id.layout_ck_2);
        layout_ck_3 = (LinearLayout) rootView.findViewById(R.id.layout_ck_3);
        layout_ck_4 = (LinearLayout) rootView.findViewById(R.id.layout_ck_4);

        id_ck_btn1 = (CheckBox) rootView.findViewById(R.id.id_ck_btn1);
        id_ck_btn2 = (CheckBox) rootView.findViewById(R.id.id_ck_btn2);
        id_ck_btn3 = (CheckBox) rootView.findViewById(R.id.id_ck_btn3);
        id_ck_btn4 = (CheckBox) rootView.findViewById(R.id.id_ck_btn4);

        btn1_WC_view = (LinearLayout) rootView.findViewById(R.id.btn1_WC_view);
        btn2_WC_view = (LinearLayout) rootView.findViewById(R.id.btn2_WC_view);
        btn3_WC_view = (LinearLayout) rootView.findViewById(R.id.btn3_WC_view);
        btn4_WC_view = (LinearLayout) rootView.findViewById(R.id.btn4_WC_view);

        btn1_W_Spinner = (Spinner) rootView.findViewById(R.id.btn1_W_Spinner);
        btn1_C_Spinner = (Spinner) rootView.findViewById(R.id.btn1_C_Spinner);

        btn2_W_Spinner = (Spinner) rootView.findViewById(R.id.btn2_W_Spinner);
        btn2_C_Spinner = (Spinner) rootView.findViewById(R.id.btn2_C_Spinner);

        btn3_W_Spinner = (Spinner) rootView.findViewById(R.id.btn3_W_Spinner);
        btn3_C_Spinner = (Spinner) rootView.findViewById(R.id.btn3_C_Spinner);

        btn4_W_Spinner = (Spinner) rootView.findViewById(R.id.btn4_W_Spinner);
        btn4_C_Spinner = (Spinner) rootView.findViewById(R.id.btn4_C_Spinner);

        id_btn_add_scene = (Button) rootView.findViewById(R.id.id_btn_add_scene);
    }
}
