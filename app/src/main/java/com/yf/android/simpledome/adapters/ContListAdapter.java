package com.yf.android.simpledome.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yf.android.simpledome.R;
import com.yf.android.simpledome.connect.Connection;
import com.yf.android.simpledome.datasource.Controller;
import com.yf.android.simpledome.fragments.ControllerBtnSetAsScene;
import com.yf.android.simpledome.fragments.ControllerItemOptFragment;
import com.yf.android.simpledome.fragments.dummy.SceneInfoContent;
import com.yf.android.simpledome.utils.DecodeByte;

import java.util.List;

public class ContListAdapter extends BaseAdapter {
    private Context mContext;
    private List<Controller> controllers;
    private FragmentManager fManager;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == R.id.errorTip) {
                Toast.makeText(mContext, "该设备不在线", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public ContListAdapter(Context c, List<Controller> list, FragmentManager fm) {
        this.mContext = c;
        this.fManager = fm;
        controllers = list;
    }

    @Override
    public int getCount() {
        return controllers.size();
    }

    @Override
    public Object getItem(int position) {
        return controllers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Controller con = controllers.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.control_holder, null);
            new ViewHolder(convertView);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();

        holder.contName.setText(con.getContName());

        // same layout ,load different screen
        switch (con.getContType()) {
            case 1:
                loadScreen1(holder, con);
                break;
            case 2:
                loadScreen2(holder, con);
                break;
            case 3:
                loadScreen3(holder, con);
                break;
            case 4:
                loadScreen4(holder, con);
                break;
        }

        return convertView;
    }

    private void loadScreen1(ViewHolder holder, final Controller con) {
        if (con.getBtnScene1() == 0) {// set btn name by btn type( scene or default)
            holder.groupNameText1.setText(con.getBtnName1());
        } else {
            setTextAsSceneName(holder.groupNameText1, con, 1);
        }
        if (con.getBtnScene2() == 0) {
            holder.groupNameText2.setText(con.getBtnName1());
        } else {
            setTextAsSceneName(holder.groupNameText2, con, 2);
        }
        if (con.getBtnScene3() == 0) {
            holder.groupNameText3.setText(mContext.getString(R.string.btn_text_wc));
        } else {
            setTextAsSceneName(holder.groupNameText3, con, 3);
        }
        if (con.getBtnScene4() == 0) {
            holder.groupNameText4.setText(mContext.getString(R.string.btn_text_wc));
        } else {
            setTextAsSceneName(holder.groupNameText4, con, 4);
        }
        // 1 位屏，无全开全关
        holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch_off);

        if (con.getBtnLighted(0)) {
            holder.imageView1.setSelected(true);
        } else {
            holder.imageView1.setSelected(false);
        }
        if (con.getBtnLighted(1)) {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21);
        } else {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21_off);
        }
        // w/c 形式存储 ，8位最后两位表征当前冷暖状态
        if (con.getContBtnWC(4) != 0) {
            holder.imageView3.setSelected(true);
        } else {
            holder.imageView3.setSelected(false);
        }
        // w/c 形式存储 ，8位最后两位表征当前冷暖状态
        if (con.getContBtnWC(6) != 0) {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22);
        } else {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22_off);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                byte[] tempData = new byte[8];
                System.arraycopy(con.getIntegers(), 0, tempData, 0, 8);

                int curBtnKey = 0;
                if (id == R.id.imageView1) curBtnKey = 0;
                if (id == R.id.imageView2) curBtnKey = 1;
                if (id == R.id.imageView3) curBtnKey = 2;
                if (id == R.id.imageView4) curBtnKey = 3;

                if (isSceneBtn(con, curBtnKey)) {
                    return;
                }

                if (id == R.id.imageView4) {
                    if (con.getContBtnWC(6) == 100) {
                        tempData = switchWC(tempData, 1, false);
                    } else {
                        tempData = switchWC(tempData, 1, true);
                    }
                } else if (id == R.id.imageView3) {
                    if (con.getContBtnWC(4) == 100) {
                        tempData = switchWC(tempData, 1, false);
                    }
                    {
                        tempData = switchWC(tempData, 1, true);
                    }
                } else {
                    if (con.getBtnLighted(0)) {
                        tempData[0] = 0;
                        tempData[1] = 0;
                        tempData[2] = 0;
                        tempData[3] = 0;
                    } else {
                        tempData[0] = con.getDataBtnWC(0);
                        tempData[1] = con.getDataBtnWC(1);
                        tempData[2] = con.getDataBtnWC(0);
                        tempData[3] = con.getDataBtnWC(1);
                    }
                }
                if (con.getContOnline() == 1) {
                    Connection.getInstance().cmdContOpt(con.getContCode(),
                            DecodeByte.bytesToString(tempData));
                } else {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            }
        };
        View.OnLongClickListener longClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (con.getContOnline() == 0) {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                    return true;
                }
                int id = v.getId();
                if (id == R.id.imageView1) {
                    if (con.getBtnScene1() > 0) {
                        showBtnSceneDialog(con, 1);
                        return true;
                    }
                    showItemOptDialog(con, 1);
                } else if (id == R.id.imageView2) {
                    if (con.getBtnScene2() > 0) {
                        showBtnSceneDialog(con, 2);
                        return true;
                    }
                    showItemOptDialog(con, 2);
                } else if (id == R.id.imageView3) {
                    showBtnSceneDialog(con, 3);
                } else if (id == R.id.imageView4) {
                    showBtnSceneDialog(con, 4);
                }
                return true;
            }
        };
        holder.imageView1.setOnLongClickListener(longClick);
        holder.imageView2.setOnLongClickListener(longClick);
        holder.imageView3.setOnLongClickListener(longClick);
        holder.imageView4.setOnLongClickListener(longClick);

        holder.imageView1.setOnClickListener(listener);
        holder.imageView2.setOnClickListener(listener);
        holder.imageView3.setOnClickListener(listener);
        holder.imageView4.setOnClickListener(listener);
    }

    private boolean isSceneBtn(Controller con, int curBtnKey) {
        if (curBtnKey == 3 && con.getBtnScene4() != 0) {
            Connection.getInstance().cmdSceneOpt(con.getBtnScene4());
            return true;
        } else if (curBtnKey == 2 && con.getBtnScene3() != 0) {
            Connection.getInstance().cmdSceneOpt(con.getBtnScene3());
            return true;
        } else if (curBtnKey == 1 && con.getBtnScene2() != 0) {
            Connection.getInstance().cmdSceneOpt(con.getBtnScene2());
            return true;
        } else if (curBtnKey == 0 && con.getBtnScene1() != 0) {
            Connection.getInstance().cmdSceneOpt(con.getBtnScene1());
            return true;
        }
        return false;
    }

    private void setTextAsSceneName(TextView textView, Controller cont, int key) {
        String name = null;
        if (key == 1) {
            name = SceneInfoContent.getSceneNameByCode(cont.getBtnScene1());
        } else if (key == 2) {
            name = SceneInfoContent.getSceneNameByCode(cont.getBtnScene2());
        } else if (key == 3) {
            name = SceneInfoContent.getSceneNameByCode(cont.getBtnScene3());
        } else if (key == 4) {
            name = SceneInfoContent.getSceneNameByCode(cont.getBtnScene4());
        }
        if (name == null) {
            textView.setText("该场景已删除");
            textView.setTextColor(Color.RED);
        } else {
            textView.setText(name);
        }
    }

    private void loadScreen2(ViewHolder holder, final Controller con) {
        if (con.getBtnScene1() == 0) {// set btn tip by btn type( scene or default)
            holder.groupNameText1.setText(con.getBtnName1());
        } else {
            setTextAsSceneName(holder.groupNameText1, con, 1);
        }
        if (con.getBtnScene2() == 0) {
            holder.groupNameText2.setText(con.getBtnName2());
        } else {
            setTextAsSceneName(holder.groupNameText2, con, 2);
        }
        if (con.getBtnScene3() == 0) {
            holder.groupNameText3.setText(mContext.getString(R.string.btn_text_wc));
        } else {
            setTextAsSceneName(holder.groupNameText3, con, 3);
        }
        if (con.getBtnScene4() == 0) {
            holder.groupNameText4.setText(mContext.getString(R.string.btn_text_wc));
        } else {
            setTextAsSceneName(holder.groupNameText4, con, 4);
        }

        if (con.getBtnLighted(4)) {// 8byte全不为0
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch);
        } else {
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch_off);
        }
        if (con.getBtnLighted(0)) {
            holder.imageView1.setSelected(true);
        } else {
            holder.imageView1.setSelected(false);
        }
        if (con.getBtnLighted(1)) {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21);
        } else {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21_off);
        }
        // w/c 形式存储 ，8位最后两位表征当前冷暖状态
        if (con.getContBtnWC(4) != 0) {
            holder.imageView3.setSelected(true);
        } else {
            holder.imageView3.setSelected(false);
        }
        // w/c 形式存储 ，8位最后两位表征当前冷暖状态
        if (con.getContBtnWC(6) != 0) {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22);
        } else {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22_off);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                byte[] tempData = new byte[8];
                System.arraycopy(con.getIntegers(), 0, tempData, 0, 8);

                int curBtnKey = 0;
                if (id == R.id.imageView1) curBtnKey = 0;
                if (id == R.id.imageView2) curBtnKey = 1;
                if (id == R.id.imageView3) curBtnKey = 2;
                if (id == R.id.imageView4) curBtnKey = 3;

                if (isSceneBtn(con, curBtnKey)) {
                    return;
                }

                if (id == R.id.imageButtonSwitch) {
                    if (con.getBtnLighted(4)) {
                        for ( int i = 0; i < 6; i++ ) {
                            tempData[i] = 0;
                        }
                    } else {
                        System.arraycopy(con.getDataArray(), 0, tempData, 0, 8);
                    }
                } else if (id == R.id.imageView4) {
                    if (con.getContBtnWC(6) != 0) {
                        tempData = switchWC(tempData, 2, false);
                    } else {
                        tempData = switchWC(tempData, 2, true);
                    }
                } else if (id == R.id.imageView3) {
                    if (con.getContBtnWC(4) != 0) {
                        tempData = switchWC(tempData, 2, false);
                    } else {
                        tempData = switchWC(tempData, 2, true);
                    }
                } else {
                    if (con.getBtnLighted(curBtnKey)) {
                        tempData[curBtnKey * 2] = 0;
                        tempData[curBtnKey * 2 + 1] = 0;
                    } else {
                        tempData[curBtnKey * 2] = con.getDataBtnWC(curBtnKey * 2);
                        tempData[curBtnKey * 2 + 1] = con.getDataBtnWC(curBtnKey * 2 + 1);
                    }
                }
                if (con.getContOnline() == 1) {
                    Connection.getInstance().cmdContOpt(con.getContCode(),
                            DecodeByte.bytesToString(tempData));
                } else {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            }
        };
        View.OnLongClickListener longClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (con.getContOnline() == 0) {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                    return true;
                }
                int id = v.getId();
                if (id == R.id.imageView1) {
                    if (con.getBtnScene1() > 0) {
                        showBtnSceneDialog(con, 1);
                        return true;
                    }
                    showItemOptDialog(con, 1);
                } else if (id == R.id.imageView2) {
                    if (con.getBtnScene2() > 0) {
                        showBtnSceneDialog(con, 2);
                        return true;
                    }
                    showItemOptDialog(con, 2);
                } else if (id == R.id.imageView3) {
                    showBtnSceneDialog(con, 3);
                } else if (id == R.id.imageView4) {
                    showBtnSceneDialog(con, 4);
                }
                return true;
            }
        };
        holder.imageButtonSwitch.setOnClickListener(listener);

        holder.imageView1.setOnLongClickListener(longClick);
        holder.imageView2.setOnLongClickListener(longClick);
        holder.imageView3.setOnLongClickListener(longClick);
        holder.imageView4.setOnLongClickListener(longClick);

        holder.imageView1.setOnClickListener(listener);
        holder.imageView2.setOnClickListener(listener);
        holder.imageView3.setOnClickListener(listener);
        holder.imageView4.setOnClickListener(listener);
    }

    private void loadScreen3(ViewHolder holder, final Controller con) {
        if (con.getBtnScene1() == 0) {// set btn tip by btn type( scene or default)
            holder.groupNameText1.setText(con.getBtnName1());
        } else {
            setTextAsSceneName(holder.groupNameText1, con, 1);
        }
        if (con.getBtnScene2() == 0) {
            holder.groupNameText2.setText(con.getBtnName2());
        } else {
            setTextAsSceneName(holder.groupNameText2, con, 2);
        }
        if (con.getBtnScene3() == 0) {
            holder.groupNameText3.setText(con.getBtnName3());
        } else {
            setTextAsSceneName(holder.groupNameText3, con, 3);
        }
        if (con.getBtnScene4() == 0) {
            holder.groupNameText4.setText(mContext.getString(R.string.btn_text_wc));
        } else {
            setTextAsSceneName(holder.groupNameText4, con, 4);
        }

        if (con.getBtnLighted(4)) {// 8byte全不为0
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch);
        } else {
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch_off);
        }
        if (con.getBtnLighted(0)) {
            holder.imageView1.setSelected(true);
        } else {
            holder.imageView1.setSelected(false);
        }
        if (con.getBtnLighted(1)) {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21);
        } else {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21_off);
        }
        if (con.getBtnLighted(2)) {
            holder.imageView3.setSelected(true);
        } else {
            holder.imageView3.setSelected(false);
        }
        // w/c 形式存储 ，8位最后两位表征当前冷暖状态
        if (con.getContBtnWC(6) != 0) {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22);
        } else {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22_off);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                byte[] tempData = new byte[8];
                System.arraycopy(con.getIntegers(), 0, tempData, 0, 8);

                int curBtnKey = 0;
                if (id == R.id.imageView1) curBtnKey = 0;
                if (id == R.id.imageView2) curBtnKey = 1;
                if (id == R.id.imageView3) curBtnKey = 2;
                if (id == R.id.imageView4) curBtnKey = 3;

                if (isSceneBtn(con, curBtnKey)) {
                    return;
                }
                if (id == R.id.imageButtonSwitch) {
                    if (con.getBtnLighted(4)) {
                        for ( int i = 0; i < 6; i++ ) {
                            tempData[i] = 0;
                        }
                    } else {
                        System.arraycopy(con.getDataArray(), 0, tempData, 0, 8);
                    }
                } else if (id == R.id.imageView4) {
                    if (con.getContBtnWC(6) != 0) {
                        tempData = switchWC(tempData, 3, false);
                    } else {
                        tempData = switchWC(tempData, 3, true);
                    }
                } else {
                    if (con.getBtnLighted(curBtnKey)) {
                        tempData[curBtnKey * 2] = 0;
                        tempData[curBtnKey * 2 + 1] = 0;
                    } else {
                        tempData[curBtnKey * 2] = con.getDataBtnWC(curBtnKey * 2);
                        tempData[curBtnKey * 2 + 1] = con.getDataBtnWC(curBtnKey * 2 + 1);
                    }
                }
                if (con.getContOnline() == 1) {
                    Connection.getInstance().cmdContOpt(con.getContCode(),
                            DecodeByte.bytesToString(tempData));
                } else {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            }
        };
        View.OnLongClickListener longClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (con.getContOnline() == 0) {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                    return true;
                }
                int id = v.getId();
                if (id == R.id.imageView1) {
                    if (con.getBtnScene1() > 0) {
                        showBtnSceneDialog(con, 1);
                        return true;
                    }
                    showItemOptDialog(con, 1);
                } else if (id == R.id.imageView2) {
                    if (con.getBtnScene2() > 0) {
                        showBtnSceneDialog(con, 2);
                        return true;
                    }
                    showItemOptDialog(con, 2);
                } else if (id == R.id.imageView3) {
                    if (con.getBtnScene3() > 0) {
                        showBtnSceneDialog(con, 3);
                        return true;
                    }
                    showItemOptDialog(con, 3);
                } else if (id == R.id.imageView4) {
                    showBtnSceneDialog(con, 4);
                }
                return true;
            }
        };
        holder.imageButtonSwitch.setOnClickListener(listener);

        holder.imageView1.setOnLongClickListener(longClick);
        holder.imageView2.setOnLongClickListener(longClick);
        holder.imageView3.setOnLongClickListener(longClick);
        holder.imageView4.setOnLongClickListener(longClick);

        holder.imageView1.setOnClickListener(listener);
        holder.imageView2.setOnClickListener(listener);
        holder.imageView3.setOnClickListener(listener);
        holder.imageView4.setOnClickListener(listener);
    }

    /**
     * @param temp
     * @param type  1,2,3
     * @param isToW
     * @return
     */
    private byte[] switchWC(byte[] temp, int type, boolean isToW) {
        if (temp.length != 8) {
            return null;
        }
        if (isToW) {
            temp[0] = (byte) (temp[0] > 0 || temp[1] > 0 ? 100 : 0);
            temp[1] = 0;
            temp[2] = (byte) (temp[2] > 0 || temp[3] > 0 ? 100 : 0);
            temp[3] = 0;
            if (type == 1 || type == 2) {
                temp[4] = 100;
                temp[5] = 0;
            } else if (type == 3) {
                temp[4] = (byte) (temp[4] > 0 || temp[5] > 0 ? 100 : 0);
                temp[5] = 0;
            }
            temp[6] = 100;
            temp[7] = 0;
        } else {
            temp[1] = (byte) (temp[0] > 0 || temp[1] > 0 ? 100 : 0);
            temp[0] = 0;
            temp[3] = (byte) (temp[2] > 0 || temp[3] > 0 ? 100 : 0);
            temp[2] = 0;
            if (type == 1 || type == 2) {
                temp[4] = 0;
                temp[5] = 100;
            } else if (type == 3) {
                temp[5] = (byte) (temp[4] > 0 || temp[5] > 0 ? 100 : 0);
                temp[4] = 0;
            }
            temp[7] = 100;
            temp[6] = 0;
        }
        return temp;
    }

    private void loadScreen4(ViewHolder holder, final Controller con) {
        if (con.getBtnScene1() == 0) {// set btn tip by btn type( scene or default)
            holder.groupNameText1.setText(con.getBtnName1());
        } else {
            setTextAsSceneName(holder.groupNameText1, con, 1);
        }
        if (con.getBtnScene2() == 0) {
            holder.groupNameText2.setText(con.getBtnName2());
        } else {
            setTextAsSceneName(holder.groupNameText2, con, 2);
        }
        if (con.getBtnScene3() == 0) {
            holder.groupNameText3.setText(con.getBtnName3());
        } else {
            setTextAsSceneName(holder.groupNameText3, con, 3);
        }
        if (con.getBtnScene4() == 0) {
            holder.groupNameText4.setText(con.getBtnName4());
        } else {
            setTextAsSceneName(holder.groupNameText4, con, 4);
        }

        if (con.getBtnLighted(4)) {// 8byte全不为0
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch);
        } else {
            holder.imageButtonSwitch.setImageResource(R.mipmap.img_btn_switch_off);
        }
        if (con.getBtnLighted(0)) {
            holder.imageView1.setSelected(true);
        } else {
            holder.imageView1.setSelected(false);
        }
        if (con.getBtnLighted(1)) {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21);
        } else {
            holder.imageView2.setImageResource(R.mipmap.img_btn_21_off);
        }
        if (con.getBtnLighted(2)) {
            holder.imageView3.setSelected(true);
        } else {
            holder.imageView3.setSelected(false);
        }
        if (con.getBtnLighted(3)) {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22);
        } else {
            holder.imageView4.setImageResource(R.mipmap.img_btn_22_off);
        }

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                byte[] tempData = new byte[8];
                System.arraycopy(con.getIntegers(), 0, tempData, 0, 8);
                int curBtnKey = 0;
                if (id == R.id.imageView1) curBtnKey = 0;
                if (id == R.id.imageView2) curBtnKey = 1;
                if (id == R.id.imageView3) curBtnKey = 2;
                if (id == R.id.imageView4) curBtnKey = 3;

                if (isSceneBtn(con, curBtnKey)) {
                    return;
                }

                if (id == R.id.imageButtonSwitch) {
                    if (con.getBtnLighted(4)) {
                        tempData = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
                    } else {
                        System.arraycopy(con.getDataArray(), 0, tempData, 0, 8);
                    }
                } else {
                    if (con.getBtnLighted(curBtnKey)) {
                        tempData[curBtnKey * 2] = 0;
                        tempData[curBtnKey * 2 + 1] = 0;
                    } else {
                        tempData[curBtnKey * 2] = con.getDataBtnWC(curBtnKey * 2);
                        tempData[curBtnKey * 2 + 1] = con.getDataBtnWC(curBtnKey * 2 + 1);
                    }
                }
                if (con.getContOnline() == 1) {
                    Connection.getInstance().cmdContOpt(con.getContCode(),
                            DecodeByte.bytesToString(tempData));
                } else {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                }
            }
        };
        View.OnLongClickListener longClick = new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                if (con.getContOnline() == 0) {
                    mHandler.sendEmptyMessage(R.id.errorTip);
                    return true;
                }
                int id = v.getId();
                if (id == R.id.imageView1) {
                    if (con.getBtnScene1() > 0) {
                        showBtnSceneDialog(con, 1);
                        return true;
                    }
                    showItemOptDialog(con, 1);
                } else if (id == R.id.imageView2) {
                    if (con.getBtnScene2() > 0) {
                        showBtnSceneDialog(con, 2);
                        return true;
                    }
                    showItemOptDialog(con, 2);
                } else if (id == R.id.imageView3) {
                    if (con.getBtnScene3() > 0) {
                        showBtnSceneDialog(con, 3);
                        return true;
                    }
                    showItemOptDialog(con, 3);
                } else if (id == R.id.imageView4) {
                    if (con.getBtnScene4() > 0) {
                        showBtnSceneDialog(con, 4);
                        return true;
                    }
                    showItemOptDialog(con, 4);
                }
                return true;
            }
        };
        holder.imageButtonSwitch.setOnClickListener(listener);

        holder.imageView1.setOnLongClickListener(longClick);
        holder.imageView2.setOnLongClickListener(longClick);
        holder.imageView3.setOnLongClickListener(longClick);
        holder.imageView4.setOnLongClickListener(longClick);

        holder.imageView1.setOnClickListener(listener);
        holder.imageView2.setOnClickListener(listener);
        holder.imageView3.setOnClickListener(listener);
        holder.imageView4.setOnClickListener(listener);
    }

    private void showBtnSceneDialog(Controller cont, int btnCode) {
        if (SceneInfoContent.ITEMS == null || SceneInfoContent.ITEMS.size() == 0) {
            Toast.makeText(mContext, "未找到场景信息", Toast.LENGTH_LONG).show();
            return;
        }
        ControllerBtnSetAsScene dialog = new ControllerBtnSetAsScene();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ControllerBtnSetAsScene.ARG_CONT, cont);
        bundle.putInt(ControllerBtnSetAsScene.HEAD_CODE, btnCode);
        dialog.setArguments(bundle);

        dialog.show(fManager, cont.getContCode());
    }

    private void showItemOptDialog(Controller cont, int btnCode) {
        ControllerItemOptFragment dialog = new ControllerItemOptFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(ControllerItemOptFragment.ARG_CONT, cont);
        bundle.putInt(ControllerItemOptFragment.HEAD_CODE, btnCode);
        dialog.setArguments(bundle);

        dialog.show(fManager, cont.getContName());
    }

    class ViewHolder {
        private TextView contName;

        private ImageView imageButtonSwitch;

        private ImageView imageView1;
        private ImageView imageView2;
        private ImageView imageView3;
        private ImageView imageView4;

        private TextView groupNameText1;
        private TextView groupNameText2;
        private TextView groupNameText3;
        private TextView groupNameText4;

        public ViewHolder(View convertView) {
            contName = (TextView) convertView.findViewById(R.id.headLineTitle);

            groupNameText1 = (TextView) convertView.findViewById(R.id.groupNameText1);
            groupNameText2 = (TextView) convertView.findViewById(R.id.groupNameText2);
            groupNameText3 = (TextView) convertView.findViewById(R.id.groupNameText3);
            groupNameText4 = (TextView) convertView.findViewById(R.id.groupNameText4);

            imageButtonSwitch = (ImageView) convertView.findViewById(R.id.imageButtonSwitch);

            imageView1 = (ImageView) convertView.findViewById(R.id.imageView1);
            imageView2 = (ImageView) convertView.findViewById(R.id.imageView2);
            imageView3 = (ImageView) convertView.findViewById(R.id.imageView3);
            imageView4 = (ImageView) convertView.findViewById(R.id.imageView4);

            convertView.setTag(this);
        }
    }
}
