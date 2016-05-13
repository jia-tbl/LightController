package com.yf.android.simpledome.choosepic;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

public class DirListPopupWindow extends PopupWindow {
    private List<FolderBean> folderBeenList;
    private ListView folderList;
    private OnFolderListItemSelectedListener mListener;

    public interface OnFolderListItemSelectedListener {
        void onSelected(FolderBean folder);
    }

    public void setFolderSelectedListener(OnFolderListItemSelectedListener listener) {
        this.mListener = listener;
    }

    public DirListPopupWindow(List<FolderBean> list) {
        folderBeenList = list;
    }

    private void initView() {
        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onSelected(folderBeenList.get(position));
                }
            }
        });
    }

}
