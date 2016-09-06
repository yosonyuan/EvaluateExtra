package com.tencent.map.geolocation.iodetector.listener;

/**
 * 用来传递检测器内部数据的字符串描述<br/>
 * Created by toveyliu on 2016/8/29.
 */

public interface IODetectorDataListener {
    void onDetectorDataDescChange(int type, String dataStr);
}
