/*
 * Copyright (C) 2008-2012 Ritsumeikan University Nishio Laboratory All Rights Reserved.
 */
package com.soso.evaextra.pdr.model;

import java.util.ArrayList;

public class StepDetector {
    /**
     * These listeners are called when this class detect a step.
     */
    protected ArrayList<StepListener> mStepListeners = new ArrayList<StepListener>();

    /**
     * Registers a StepListener for the Leave Event.
     *
     * @param stepListener A StepListener object.
     */
    public void addListener(StepListener stepListener) {
        mStepListeners.add(stepListener);
    }


    /**
     * Returns if this Collection contains no elements. This implementation tests, whether size returns 0.
     *
     * @return if there is no listeners, return true.
     */
    public boolean isEmpty() {
        return mStepListeners.isEmpty();
    }
}