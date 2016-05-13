package com.yf.android.simpledome.wificonfig;

import java.util.List;

public interface IEsptouchTask {
    void interrupt();

    IEsptouchResult executeForResult() throws RuntimeException;

    List<IEsptouchResult> executeForResults(int var1) throws RuntimeException;

    boolean isCancelled();
}
