package com.soso.evaextra.pdr.model;

/**
 * Used for receiving notifications from the LeaveDetector when step is detected.
 *
 * @author yosonyuan
 */
public interface StepListener {

    /**
     * Called when the step is detected.
     */
    public void onStep(long time);
}