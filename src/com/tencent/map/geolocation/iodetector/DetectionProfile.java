package com.tencent.map.geolocation.iodetector;

import android.util.SparseArray;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tencent.map.geolocation.iodetector.IODetectorManager.INDOOR;
import static com.tencent.map.geolocation.iodetector.IODetectorManager.OUTDOOR;
import static com.tencent.map.geolocation.iodetector.IODetectorManager.UNKNOW;

/**
 * Created by toveyliu on 2015/9/19.
 */
public class DetectionProfile implements Cloneable {
    // 描述信息 ------------------------------------start
    public static final int FUSION_TYPE_SIZE = 3;
    public static final int TYPE_FUSION = 0;
    public static final int TYPE_GPS = 1;
    public static final int TYPE_WIFI = 2;
    public static final int TYPE_LIGHT = 3;
    public static final int TYPE_MAG = 4;

    private static SimpleDateFormat mSdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS");
    private static SparseArray<String> typeMap;

    static {
        typeMap = new SparseArray<String>(FUSION_TYPE_SIZE + 1);
        typeMap.put(TYPE_FUSION, "fusion");
        typeMap.put(TYPE_GPS, "gps");
        typeMap.put(TYPE_WIFI, "wifi");
        typeMap.put(TYPE_LIGHT, "light");
    }

    public static String getDetectorTypeDesc(int type) {
        return typeMap.get(type);
    }
    // 描述信息 ------------------------------------end

    /**
     * [indoorConf,outdoorConf,unknow]
     */
    private float[] confidence;
    /**
     * 判断出当前所处环境
     */
    private int environment;
    /**
     * 判断时间
     */
    private long time;
    /**
     * 判断器类型
     */
    private int detectType;

    /**
     * 缩放因子，用来放大该检测器的作用，默认不放大
     */
    private float fcator = 1.0f;

    public DetectionProfile(DetectionProfile profile) {
        this.confidence = profile.confidence.clone();
        this.environment = profile.environment;
        this.time = profile.time;
        this.detectType = profile.detectType;
    }

    public DetectionProfile(int detectType) {
        this.detectType = detectType;

        this.environment = UNKNOW;
        this.confidence = new float[]{0.0f, 0.0f, 1.0f};
        this.time = System.currentTimeMillis();
    }


    private void normized() {
        float sum = confidence[INDOOR] + confidence[OUTDOOR];// + confidence[UNKNOW];
        // 归一化
        if (sum != 0.0f && sum != 1.0f) {
            confidence[INDOOR] /= sum;
            confidence[OUTDOOR] /= sum;
        }
    }

    public float[] getConfidence() {
        boolean flag = false;
        if (this.confidence != null) {
            for (int i = 0; i < confidence.length; i++) {
                if (Float.isNaN(confidence[i])) {
                    flag = true;
                    break;
                }
            }
        } else {
            flag = true;
        }

        if (flag) {
            return new float[]{0.0f, 0.0f, 1.0f};
        } else {
            normized();
            return this.confidence;
        }
    }

    public void setConfidence(float[] conf) {
        if (conf == null || conf.length != 3) {
            return;
        }
        setConfidence(conf[0], conf[1], conf[2]);
    }

    public void setConfidence(float indoorConf, float outdoorConf, float unknowConf) {
        this.confidence[INDOOR] = indoorConf;
        this.confidence[OUTDOOR] = outdoorConf;
        this.confidence[UNKNOW] = unknowConf;
        this.time = System.currentTimeMillis();
    }

    public void addConfidence(float indoorConf, float outdoorConf, float unknowConf) {
        this.confidence[INDOOR] += indoorConf;
        this.confidence[OUTDOOR] += outdoorConf;
        this.confidence[UNKNOW] += unknowConf;
        this.time = System.currentTimeMillis();
    }

    /**
     * 获取判断结果
     *
     * @return
     */
    public int getEnvironment() {
        normized();
        if (confidence[INDOOR] == confidence[OUTDOOR]) {
            environment = UNKNOW;
        } else {
            environment = confidence[INDOOR] > confidence[OUTDOOR] ? INDOOR : OUTDOOR;
        }

        return environment;
    }

    public String getEnvironmentDescrible() {
        return IODetectorManager.getEnvironmentDesc(this.getEnvironment());
    }

    public long getTime() {
        return this.time;
    }

    public int getDetectType() {
        return this.detectType;
    }

    public String getDetectTypeDescrible() {
        return getDetectorTypeDesc(this.detectType);
    }

    /**
     * 获取缩放因子
     */
    public float getFcator() {
        return fcator;
    }

    public void setFcator(float fcator) {
        this.fcator = fcator;
    }

    @Override
    public DetectionProfile clone() {
        return new DetectionProfile(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDetectTypeDescrible()).append(",");
        sb.append(getEnvironmentDescrible()).append("\n");
        sb.append('[').append(String.format("%.2f", confidence[INDOOR])).append(',');
        sb.append(String.format("%.2f", confidence[OUTDOOR])).append(',');
        sb.append(String.format("%.2f", confidence[UNKNOW])).append("]\n");
        sb.append(mSdf.format(new Date(this.time)));
        return sb.toString();
    }
}
