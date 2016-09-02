/*
 * Copyright (C) 2013 Ritsumeikan University Nishio Laboratory All Rights Reserved.
 */
package com.soso.evaextra.pdr.model;

import java.util.LinkedList;
import java.util.List;


/**
 * Step detection algorithm
 */
public class PeakStepDetector extends StepDetector {

    /**
     * Threshold of change in acceleration
     */
    private final float DIFF_THRESH;

    /**
     * Interval of steps
     */
    private final float TIME_THRESH;

    /**
     * List of change in acceleration
     */
    private List<AccDeltaObject> deltaList = null;

    /**
     * Acceleration values after Low-pass-filter
     */
    private AccelerometerObject lowpassAcc = null;

    /**
     * Last acceleration value after Low-pass-filter
     */
    private AccelerometerObject lastAcc = null;

    /**
     * Difference of resultant acceleration after Low-pass-filter
     */
    private float delta = 0.0f;

    /**
     * Last difference of resultant acceleration.
     */
    private float lastDelta = 0.0f;

    /**
     * Starting time of a step that system detected.
     */
    private long startTime = 0;

    /**
     * True if acceleration wave shows a peak
     */
    private boolean isPeak = true;

    /**
     * Difference of resultant acceleration
     */
    private float diff = 0;

    /**
     * Constructor. Sets diffThresh and timeThresh．
     *
     * @param diffThresh Change in acceleration
     * @param timeThresh Interval of steps
     */
    public PeakStepDetector(float diffThresh, float timeThresh) {
        deltaList = new LinkedList<AccDeltaObject>();
        DIFF_THRESH = diffThresh;
        TIME_THRESH = timeThresh;
    }

    /**
     * 歩が踏まれたことを検知した時に，リスナーに登録されているものに通知する．
     *
     * @param acc  加速度センサの各軸の値
     * @param time センサの取得時刻(ナノ秒単位) SensorEvent.timestamp
     */
    public void detectStepAndNotify(float acc[], long time) {

        lowpassFiltering(time, acc[0], acc[1], acc[2]);

        if (delta != 0.0f && lastDelta != 0.0f) {
            AccDeltaObject deltaObject = new AccDeltaObject(delta, lastDelta, lowpassAcc.getSyn(), time);
            deltaList.add(deltaObject);
        }
        if (startTime == 0) {
            startTime = time;
        }

        if ((time - startTime) >= 1000000000 * TIME_THRESH) {
            if (isPeak) {
                int newHead = 0;
                for (int i = 0; i < deltaList.size(); i++) {
                    if (findMinPeak(deltaList.get(i))) {

                        if (diff < Math.abs(deltaList.get(i).getSynthesis() - deltaList.get(0).getSynthesis())) {
                            diff = Math.abs(deltaList.get(i).getSynthesis() - deltaList.get(0).getSynthesis());

                            newHead = i;
                        }
                    }
                }
                if (newHead < deltaList.size()) {

                    startTime = deltaList.get(newHead).getT();
                    for (int i = 0; i < newHead; i++) {
                        deltaList.remove(0);
                    }
                    if (diff > DIFF_THRESH) {
                        int SIZE = mStepListeners.size();
                        for (int i = 0; i < SIZE; i++) {
                            mStepListeners.get(i).onStep(time);
                        }
                    }
                    diff = 0;
                    isPeak = false;
                } else {
                    startTime = 0;
                }
            } else {
                int newHead = 0;
                for (int i = 0; i < deltaList.size(); i++) {
                    if (findMaxPeak(deltaList.get(i))) {
                        if (diff < Math.abs(deltaList.get(i).getSynthesis() - deltaList.get(0).getSynthesis())) {
                            diff = Math.abs(deltaList.get(i).getSynthesis() - deltaList.get(0).getSynthesis());
                            newHead = i;
                        }
                    }
                }
                if (newHead < deltaList.size()) {
                    startTime = deltaList.get(newHead).getT();
                    for (int i = 0; i < newHead; i++) {
                        deltaList.remove(0);
                    }
                    diff = 0;
                    isPeak = true;
                } else {
                    startTime = 0;
                }
            }
        }
        lastAcc = lowpassAcc;
        lastDelta = delta;
    }

    /**
     * Lop-pass-filter
     *
     * @param t SensorEvent.timestamp
     * @param x z axis of acceleration
     * @param y z axis of acceleration
     * @param z z axis of acceleration
     */
    private void lowpassFiltering(long t, float x, float y, float z) {
        long time = System.currentTimeMillis();
        if (lowpassAcc == null) {
            lowpassAcc = new AccelerometerObject(time, t, x, y, z);
            delta = lowpassAcc.getSyn() - 0;
        } else {
            lowpassAcc = new AccelerometerObject(time, t, x * 0.1f + lowpassAcc.getX() * 0.9f,
                    y * 0.1f + lowpassAcc.getY() * 0.9f, z * 0.1f + lowpassAcc.getZ() * 0.9f);
            if (lastAcc != null) {
                delta = lowpassAcc.getSyn() - lastAcc.getSyn();
            }
        }
    }

    /**
     * Detect local maximal
     *
     * @param object Change in acceleration object
     * @return true if local maximal, otherwise false.
     */
    private boolean findMinPeak(AccDeltaObject object) {
        return (object.getDelta() >= 0 && object.getLastDelta() <= 0);
    }

    /**
     * Detect local minimal
     *
     * @param object Change in acceleration object
     * @return true if local minimal, otherwise false.
     */
    private boolean findMaxPeak(AccDeltaObject object) {
        return (object.getDelta() <= 0 && object.getLastDelta() >= 0);
    }
}