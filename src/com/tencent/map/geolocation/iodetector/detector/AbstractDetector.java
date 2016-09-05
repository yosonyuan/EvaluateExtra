package com.tencent.map.geolocation.iodetector.detector;

import com.tencent.map.geolocation.iodetector.listener.IODetectorDataListener;
import com.tencent.map.geolocation.iodetector.listener.IODetectorListener;

import android.util.Log;

import com.tencent.map.geolocation.iodetector.DetectionProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toveyliu on 2016/8/26.
 */

public abstract class AbstractDetector {
    /**
     * 延迟回调时间:ms
     */
    protected final int CALLBACK_DELAY_TIME = 1000;

    /**
     * 上一次检测结果回调时间
     */
    private long lastCallbackTime = 0L;
    /**
     * 上一次内部数据描述回调时间
     */
    private long lastDescCallbackTime = 0L;

    private boolean isRunning = false;
    private List<IODetectorListener> ioListeners;
    private List<IODetectorDataListener> dataListeners;

    protected DetectionProfile mProfile;

    public AbstractDetector() {
        this.mProfile = new DetectionProfile(getDetectorType());
        this.ioListeners = new ArrayList<IODetectorListener>();
        this.dataListeners = new ArrayList<IODetectorDataListener>();
    }

    /**
     * 获取检测器的类型
     *
     * @return
     */
    public abstract int getDetectorType();

    /**
     * 更新室内外检测结果
     */
    protected abstract void updateProfile();

    /**
     * 获取Detector的内部检测数据
     *
     * @return
     */
    protected abstract String getDetectorDataDesc();

    /**
     * 开启检测器
     */
    public void start() {
        isRunning = true;
    }

    /**
     * 关闭检测器
     */
    public void stop() {
        isRunning = false;
    }

    /**
     * 检测器是否正在运行
     *
     * @return
     */
    public boolean isRunning() {
        return this.isRunning;
    }

    // ------------------室内外检测结果相关---------------------------------------

    /**
     * 添加判断结果回调监听器
     *
     * @param listener 添加的监听器
     */
    public void addDetectorListener(IODetectorListener listener) {
        this.ioListeners.add(listener);
    }

    /**
     * 删除判断结果回调监听器
     *
     * @param listener 需要删除的监听器
     */
    public void removeDetectorListener(IODetectorListener listener) {
        this.ioListeners.remove(listener);
    }

    /**
     * 删除所有判断结果回调监听器
     */
    public void removeAllDetectorListener() {
        this.ioListeners.clear();
    }

    /**
     * 监听器回调监听结果
     *
     * @param delayTime 延迟时间(ms)
     */
    protected void notifyDetecterListener(int delayTime) {
        if (ioListeners != null) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastCallbackTime > delayTime) {
                for (int i = 0; i < ioListeners.size(); i++) {
                    ioListeners.get(i).onIODetecterUpdate(mProfile);
                }
                lastCallbackTime = curTime;
            }
        }
    }

    // -----------------------返回Detector内部数据相关---------------------------------------
    /**
     * 添加检测器内部数据描述监听器
     *
     * @param listener
     */
    public void addDetectorDataDescListener(IODetectorDataListener listener) {
        this.dataListeners.add(listener);
    }

    /**
     * 删除检测器内部数据描述监听器
     *
     * @param listener
     */
    public void removeDetectorDataDescListener(IODetectorDataListener listener) {
        this.dataListeners.remove(listener);
    }

    /**
     * 删除检测器内部所有数据描述监听器
     */
    public void removeAllDetectorDataDescListener(IODetectorDataListener listener) {
        this.dataListeners.clear();
    }

    /**
     * 回调检测器内部所有数据描述监听器
     *
     * @param delayTime 延迟时间（ms）
     */
    protected void notifyDetecterDataDescListener(int delayTime) {
        if (dataListeners != null) {
            long curTime = System.currentTimeMillis();
            if (curTime - lastDescCallbackTime > delayTime) {
                for (int i = 0; i < dataListeners.size(); i++) {
                    dataListeners.get(i).onDetectorDataDescChange(getDetectorType(), getDetectorDataDesc());
                }
                lastDescCallbackTime = curTime;
            }
        }
    }

}
