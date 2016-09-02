/*
 * Copyright (C) 2008-2012 Ritsumeikan University Nishio Laboratory All Rights Reserved.
 */
package com.soso.evaextra.pdr.model;

/**
 * This class handles change in resultant acceleration value.
 *
 * @author sacchin
 */
public class AccDeltaObject {
    /**
     * Change in resultant acceleration value.
     */
    private final float delta;

    /**
     * Last change in resultant acceleration value.
     */
    private final float lastDelta;

    /**
     * Resultant acceleration value.
     */
    private final float synthesis;

    /**
     * SensorEvent.t（nsec）
     */
    private final long t;

    /**
     * Constructor
     *
     * @param delta     Change in resultant acceleration value.
     * @param lastDelta Last change in resultant acceleration value.
     * @param synthesis Resultant acceleration value.
     * @param t         SensorEvent.t
     */
    public AccDeltaObject(float delta, float lastDelta, float synthesis, long t) {
        this.delta = delta;
        this.lastDelta = lastDelta;
        this.synthesis = synthesis;
        this.t = t;
    }

    /**
     * Returns change in resultant acceleration value.
     *
     * @return change in resultant acceleration value.
     */
    public float getDelta() {
        return this.delta;
    }

    /**
     * Returns last change in resultant acceleration value.
     *
     * @return Last change in resultant acceleration value.
     */
    public float getLastDelta() {
        return this.lastDelta;
    }

    /**
     * Returns resultant acceleration value.
     *
     * @return Resultant acceleration value.
     */
    public float getSynthesis() {
        return this.synthesis;
    }

    /**
     * Returns relative time.
     *
     * @return Time(nsec) obtaiend with SensorEvent.t.
     */
    public long getT() {
        return this.t;
    }
}