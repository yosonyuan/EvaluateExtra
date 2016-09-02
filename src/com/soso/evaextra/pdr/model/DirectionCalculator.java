/*
 * Copyright (C) 2013 Ritsumeikan University Nishio Laboratory All Rights Reserved.
 */
package com.soso.evaextra.pdr.model;

/**
 * This class handles heading direction.
 */
public class DirectionCalculator {

    /**
     * Inclination of device(smartphone).
     */
    private double pitch;
    private double roll;

    private long lastTime = 0;
    private boolean isFirst = true;
    private double simpson[] = {0.0, 0.0, 0.0};
    private boolean isEven = false;

    /**
     * Heading direction(radian)
     * East is 0π(0°), North is π/2(90°), West is ±π(180°), South is -π/2(270°).
     */
    private double direction = 0;

    /**
     * Offset of gyro.
     */
    private double offset[] = {0.0, 0.0, 0.0};

    /**
     * @param offset Offset for each axis of gyro.
     */
    public DirectionCalculator(double offset[]) {
        setOffset(offset);
    }

    /**
     * Method to obtain device inclination.
     *
     * @param acc Acceleration value.
     */
    public void calculateLean(float acc[]) {
        final double invA = 1.0f / Math.sqrt(acc[0] * acc[0] + acc[1] * acc[1] + acc[2] * acc[2]);
        double x = invA * acc[0];
        double y = invA * acc[1];
        double z = invA * acc[2];
        pitch = -Math.asin(-y);
        roll = Math.atan2(-x, z);
    }

    /**
     * Method to obtain heading direction.
     *
     * @param gyro Gyro value
     * @param time Timestamp of gyro value.
     */
    public void calculateDirection(float gyro[], long time) {
        if (isFirst) {
            lastTime = time;
            isFirst = false;
        } else {
            /**
             *Calibration with offset
             */
            double[] calibratedGyro = {gyro[0] - offset[0], gyro[1] - offset[1], gyro[2] - offset[2]};

            double[] rotateGyro = rotateAxis(calibratedGyro);

            /**
             * Simpson's rule for numerical integration
             */
            simpson[2] = simpson[1];
            simpson[1] = simpson[0];
            simpson[0] = rotateGyro[2];

            if (isEven) {
                direction += (simpson[0] + 4 * simpson[1] + simpson[2]) * (time - lastTime) / 1000000000 / 3;
                isEven = false;
            } else {
                isEven = true;
            }

            lastTime = time;
        }
    }

    public double[] rotateAxis(double axis[]) {
        double[] rAxis = new double[3];

        double sinA = Math.sin(pitch);
        double cosA = Math.cos(pitch);
        double sinB = Math.sin(roll);
        double cosB = Math.cos(roll);

        rAxis[0] = axis[0] * cosB + axis[1] * sinA * sinB + axis[2] * cosA * sinB;
        rAxis[1] = axis[1] * cosA - axis[2] * sinA;
        rAxis[2] = -axis[0] * sinB + axis[1] * sinA * cosB + axis[2] * cosA * cosB;
        return rAxis;
    }

    public double loopAngle(double degrees) {
        degrees %= 360;
        if (degrees > 180.0) {
            degrees -= 360.0;
        }
        return degrees;
    }

    /**
     * Method to get heading directin in [rad].
     */
    public double getRadiansDirection() {
        return direction;
    }

    /**
     * Method to set heading direction in [rad].
     */
    public void setRadiansDirection(double direction) {
        this.direction = direction;
    }

    /**
     * Method to get heading direction in [deg].
     */
    public double getDegreesDirection() {
        return loopAngle(Math.toDegrees(direction));
    }

    /**
     * Method to set heading direction in [deg].
     */
    public void setDegreesDirection(double direction) {
        this.direction = Math.toRadians(direction);
    }

    public void setOffset(double offset[]) {
        this.offset = offset;
    }
}