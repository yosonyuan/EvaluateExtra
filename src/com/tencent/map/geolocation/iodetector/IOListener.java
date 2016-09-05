package com.tencent.map.geolocation.iodetector;

/**
 * Created by toveyliu on 2016/9/2.
 */

public interface IOListener {
    /**
     * 返回最终的室内外判定结果
     * @param env <br/>IODetectorManager.INDOOR,<br/>IODetectorManager.OUTDOOR,<br/>IODetectorManager.UNKNOW
     */
    void onIOEnvListener(int env);
}
