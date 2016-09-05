package com.tencent.map.geolocation.iodetector.listener;

import com.tencent.map.geolocation.iodetector.DetectionProfile;

/**
 * 用来传递所有的室内外检测结果<br/>
 * 包括每种中间结果的置信度<br/>
 * Created by toveyliu on 2016/8/26.
 */

public interface IODetectorListener {
    void onIODetecterUpdate(DetectionProfile profile);
}
