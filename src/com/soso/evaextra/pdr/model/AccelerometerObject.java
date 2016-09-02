/*
 * Copyright (C) 2008-2012 Ritsumeikan University Nishio Laboratory All Rights Reserved.
 */
package com.soso.evaextra.pdr.model;

/**
 * This is an object class of the acceleration.
 * The direction of each axis are notated to the following URL.
 * http://developer.android.com/reference/android/hardware/SensorEvent.html
 *
 * @author sacchin
 */
public class AccelerometerObject {
    /**
     * this is an observed time
     */
    private final long time;

    /**
     * difference from observed time
     */
    private final long t;

    /**
     * Acceleration minus Gx on the x-axis(m/s^2)
     */
    private final float x;

    /**
     * Acceleration minus Gy on the y-axis(m/s^2)
     */
    private final float y;

    /**
     * Acceleration minus Gz on the z-axis(m/s^2)
     */
    private final float z;

    /**
     * @param x Acceleration minus Gx on the x-axis(m/s^2)
     * @param y Acceleration minus Gy on the y-axis(m/s^2)
     * @param z Acceleration minus Gz on the z-axis(m/s^2)
     */
    public AccelerometerObject(long time, long t, float x, float y, float z) {
        this.time = time;
        this.t = t;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * get a n observed time
     *
     * @return value of time(msec)
     */
    public long getTime() {
        return time;
    }

    /**
     * get a difference from observed time
     *
     * @return value of time(nsec)
     */
    public long getT() {
        return t;
    }

    /**
     * get a value of the x-axis
     *
     * @return value of the x-axis(m/s^2)
     */
    public float getX() {
        return this.x;
    }

    /**
     * get a value of the y-axis
     *
     * @return value of the y-axis(m/s^2)
     */
    public float getY() {
        return this.y;
    }

    /**
     * get a value of the z-axis
     *
     * @return value of the z-axis(m/s^2)
     */
    public float getZ() {
        return this.z;
    }

    public float getSyn() {
        double synthesis = Math.sqrt(Math.pow(this.x, 2) + Math.pow(this.y, 2) + Math.pow(this.z, 2));
        return (float) synthesis;
    }
}