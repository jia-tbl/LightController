package com.yf.android.simpledome.wificonfig;

public interface IEsptouchTaskParameter {
    long getIntervalGuideCodeMillisecond();

    long getIntervalDataCodeMillisecond();

    long getTimeoutGuideCodeMillisecond();

    long getTimeoutDataCodeMillisecond();

    long getTimeoutTotalCodeMillisecond();

    int getTotalRepeatTime();

    int getEsptouchResultOneLen();

    int getEsptouchResultMacLen();

    int getEsptouchResultIpLen();

    int getEsptouchResultTotalLen();

    int getPortListening();

    String getTargetHostname();

    int getTargetPort();

    int getWaitUdpReceivingMillisecond();

    int getWaitUdpSendingMillisecond();

    int getWaitUdpTotalMillisecond();

    int getThresholdSucBroadcastCount();

    void setWaitUdpTotalMillisecond(int var1);

    int getExpectTaskResultCount();

    void setExpectTaskResultCount(int var1);
}
