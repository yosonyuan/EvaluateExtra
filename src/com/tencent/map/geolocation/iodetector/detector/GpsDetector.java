package com.tencent.map.geolocation.iodetector.detector;

import android.location.GpsStatus;
import android.util.Log;

import com.tencent.map.geolocation.iodetector.DetectionProfile;

/**
 * Created by toveyliu on 2016/8/26.
 */

public class GpsDetector extends AbstractDetector {
    private static final String TAG = "GpsDetector";

    private InOutRecognizer mIORecognizer;
    private GpsStatus mGpsStatus;

    /**
     * Singleton:static inner class
     */
    private static class SingletonHolder {
        public static final GpsDetector INSTANCE = new GpsDetector();
    }

    private GpsDetector() {
        super();
        mIORecognizer = InOutRecognizer.getInstance();
    }

    public static GpsDetector getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onGpsEvent(GpsStatus gpsStatus) {
        if (!isRunning()) {
            return;
        }
        mGpsStatus = gpsStatus;

        updateProfile();
        notifyDetecterListener(0);
        notifyDetecterDataDescListener(0);
    }

    @Override
    public int getDetectorType() {
        return DetectionProfile.TYPE_GPS;
    }

    @Override
    public void updateProfile() {
        if (mGpsStatus == null) {
            mProfile.setConfidence(0.0f, 0.0f, 0.0f);
            return;
        }
        boolean result = mIORecognizer.judgeInOut(mGpsStatus);
        if (result) {
            //室外
            mProfile.setConfidence(0.0f, 1.0f, 0.0f);
        } else {
            //室内
            mProfile.setConfidence(1.0f, 0.0f, 0.0f);
        }
    }

    @Override
    protected String getDetectorDataDesc() {
        StringBuilder sb = new StringBuilder();
        if (mGpsStatus == null) {
            sb.append("GpsStatus is null!");
        } else {
            sb.append(mIORecognizer.getJudgedetail());
        }
        return sb.toString();
    }

}
