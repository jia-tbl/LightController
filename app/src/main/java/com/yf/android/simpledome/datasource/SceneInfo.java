package com.yf.android.simpledome.datasource;

import android.os.Build;

import java.io.Serializable;
import java.util.Objects;

public class SceneInfo implements Serializable {
    private int id;
    private int code;
    private String name;
    private String icon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SceneInfo s = (SceneInfo) o;
        if (Build.VERSION.SDK_INT >= 19) {
            return code == s.code && Objects.equals(name, s.name);
        } else {
            return code == s.code && name.equals(s.name);
        }
    }
}
