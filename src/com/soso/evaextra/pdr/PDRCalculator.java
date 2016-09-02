package com.soso.evaextra.pdr;

import java.util.ArrayList;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.soso.evaextra.pdr.model.DirectionCalculator;
import com.soso.evaextra.pdr.model.PeakStepDetector;
import com.soso.evaextra.pdr.model.StepListener;
import com.tencent.tencentmap.mapsdk.maps.a.ev;


/**
 * Created by mot on 7/5/15.
 */
public class PDRCalculator implements StepListener,SensorEventListener{

    // Conversion of coordinate(circumference of the Earth)
    private final double RX = 40076500;
    private final double RY = 40008600;
    private float directionAdjust = 90.0f;
    private float defaultStepDiffThresh = 1.0f;
    private float defaultPeakStepDetectorTimeThresh = 0.4f;

    private PeakStepDetector peakStepDetector;
    private DirectionCalculator directionCalculator;
    private Context mContext;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mRotation;
    private boolean mPdrStartFlag = false;
    private ArrayList<PDRListener> pdrListeners = new ArrayList<PDRListener>();

    private double latitude;
    private double longitude;
    private double stepLength;
    private double altitude;
    private double direction;

    public PDRCalculator(Context context) {
    	mContext = context;
    }

    public void addListener(PDRListener listener) {
        this.pdrListeners.add(listener);
    }

    /**
     * Start PDR
     * Setting initial point and the next point for indicating direction, this method starts PDR from the point and to the direction.
     *
     * @param startLatitude      Initial latitude
     * @param startLongitude     Initial longitude
     * @param directionLatitude  Second initial latitude
     * @param directionLongitude Second initial longitude
     * @param stepLength         Step length
     */
    public void startPDR(double startLatitude, double startLongitude, double stepLength) {

    	
        this.latitude = startLatitude;
        this.longitude = startLongitude;
        this.stepLength = stepLength;
        this.peakStepDetector = new PeakStepDetector(
                defaultStepDiffThresh,
                defaultPeakStepDetectorTimeThresh
        );
        this.peakStepDetector.addListener(this);

        double[] gyroOffsets = {0.0, 0.0, 0.0};
        this.directionCalculator = new DirectionCalculator(gyroOffsets);
        this.mPdrStartFlag = true;
        this.directionCalculator.setRadiansDirection(direction);
    	//mSensorManager.unregisterListener(this, mRotation);
//        this.directionCalculator.setDegreesDirection(
//                calculateStartDirectionDegree(
//                        startLatitude,
//                        startLongitude,
//                        directionLatitude,
//                        directionLongitude
//                )
//        );
    }

//    /**
//     * Start PDR
//     *
//     * @param initialPositionX          Initial Relative X
//     * @param initialPositionY          Initial Relative Y
//     * @param initialPositionZ          Initial Relative Z
//     * @param initialDirectionPositionX Initial Relative Direction X
//     * @param initialDirectionPositionY Initial Relative Direction Y
//     * @param initialDirectionPositionZ Initial Relative Direction Z
//     * @param stepLength                Step length
//     */
//    public void startPDR(double initialPositionX, double initialPositionY, double initialPositionZ,
//                         double initialDirectionPositionX, double initialDirectionPositionY, double initialDirectionPositionZ,
//                         double stepLength) {
//        double startLatitude = PositionConverter.relativeYtoLatitude(initialPositionY);
//        double startLongitude = PositionConverter.relativeXtoLongitude(initialPositionX);
//        double directionLatitude = PositionConverter.relativeYtoLatitude(initialDirectionPositionY);
//        double directionLongitude = PositionConverter.relativeXtoLongitude(initialDirectionPositionX);
//
//        startPDR(startLatitude, startLongitude, directionLatitude, directionLongitude, stepLength);
//    }
    
    public void startSensor(){
    	  mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

          mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
          mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
          mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
          mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
          mSensorManager.registerListener(this, mGyroscope,SensorManager.SENSOR_DELAY_FASTEST);
          mSensorManager.registerListener(this, mRotation,SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * Stop PDR
     */
    public void stopPDR() {
    	mSensorManager.unregisterListener(this, mAccelerometer);
    	mSensorManager.unregisterListener(this, mGyroscope);
    	mSensorManager.unregisterListener(this,mRotation);
    }

    /**
     * This is Callback called when PeakStepDtector, library we made, detects a step.
     *
     * @param time Time (nanosecond)
     */
    @Override
    public void onStep(long time) {
        double directionRadian = this.directionCalculator.getRadiansDirection();
    	//double directionDegress = this.directionCalculator.getDegreesDirection();
        calculatePosition(directionRadian, this.stepLength);

        for (PDRListener pdrListener : this.pdrListeners) {
//            pdrListener.onPDRChanged(this.latitude, this.longitude, pdrCalibrator.getDirectionAdjust() - this.directionCalculator.getDegreesDirection(), this.altitude);
            pdrListener.onPDRChanged(
//                    PositionConverter.longitudeToRelativeX(this.longitude),
//                    PositionConverter.latitudeToRelativeY(this.latitude),
            		this.latitude,
            		this.longitude,
                    this.altitude);
        }
    }

    /**
     * This method calculates next point from the given direction and step length.
     *
     * @param direction  Direction[rad]
     * @param stepLength Step length
     */
    private void calculatePosition(double direction, double stepLength) {
        this.latitude += (stepLength * Math.sin(direction) / 100 / (RX / 360));
        this.longitude += (stepLength * Math.cos(direction) / 100 / (RY * Math.cos(Math.toRadians(latitude)) / 360));
    }

    /**
     * Method that calculates direction from current point and the next point.
     *
     * @param startLatitude      Current latitude
     * @param startLongitude     Current longitude
     * @param directionLatitude  Latitude at next point
     * @param directionLongitude Longitude at next point
     * @return 方向/Direction
     */
    private float calculateStartDirectionDegree(double startLatitude, double startLongitude, double directionLatitude, double directionLongitude) {
        double dX = Math.toRadians(directionLongitude - startLongitude);
        double y1 = Math.toRadians(startLatitude);
        double y2 = Math.toRadians(directionLatitude);
        double direction = 90 - Math.toDegrees(Math.atan2(Math.sin(dX), Math.cos(y1) * Math.tan(y2) - Math.sin(y1) * Math.cos(dX)));

        if (direction < -270) {
            direction += 360;
        }
        return (float) direction;

//        double y = Math.cos(directionLongitude * Math.PI / 180.0) * Math.sin(directionLatitude * Math.PI / 180.0 - startLatitude * Math.PI / 180.0);
//        double x = Math.cos(startLongitude * Math.PI / 180.0) * Math.sin(directionLongitude * Math.PI / 180.0) - Math.sin(directionLongitude * Math.PI / 180.0) * Math.cos(directionLongitude * Math.PI / 180.0) * Math.cos(directionLatitude * Math.PI / 180.0 - startLatitude * Math.PI / 180.0);
//
//        double direction = 180.0 * Math.atan2(y, x);
//        if (direction < 0.0) {
//            direction += 360.0;
//        }
//        return pdrCalibrator.getDirectionAdjust() - (float) direction;
    }

    private float[] mRotationMatrix = new float[16];
    private float[] mOrientationValues = new float[3];
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		 switch (event.sensor.getType()) {
         case Sensor.TYPE_ACCELEROMETER:
        	 if(mPdrStartFlag){
        		 this.peakStepDetector.detectStepAndNotify(event.values,event.timestamp);
        		 //this.directionCalculator.calculateLean(event.values);
        	 }
             break;
         case Sensor.TYPE_GYROSCOPE:
        	 if(mPdrStartFlag){
        		 this.directionCalculator.calculateDirection(event.values,event.timestamp);
        		 //Log.e("PDR", "direction:"+this.directionCalculator.getRadiansDirection());
        		 //Log.e("PDR", "direction:"+this.directionCalculator.getDegreesDirection());
        	 }
             break;
         case Sensor.TYPE_ROTATION_VECTOR:
				SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
				SensorManager.getOrientation(mRotationMatrix, mOrientationValues);
				double a = mOrientationValues[0];
				if(a>=-(Math.PI/2) && a<=Math.PI){
					direction = Math.PI/2-a;
				}else{
					direction = -(Math.PI*3/2)-a; 
				}
				if(mPdrStartFlag)
					this.directionCalculator.setRadiansDirection(direction);
				
				Log.e("PDR", "rotation:"+direction);
		 }
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
}

