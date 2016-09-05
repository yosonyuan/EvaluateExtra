package com.tencent.map.geolocation.iodetector.provider;

import com.tencent.map.geolocation.iodetector.detector.GpsDetector;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.location.GpsStatus;
import android.location.Location;

public class IoGpsProvider implements GpsStatus.Listener, LocationListener {
	/**
	 * System GPS Status
	 */
	public static final int GPS_STATE_UNKNOW = 1024;

	public static final int GPS_STATE_DISABLED = 0;

	public static final int GPS_STATE_ENABLED = 4;

	public static final int GPS_SUBSTATE_STOPED = 0;

	public static final int GPS_SUBSTATE_STARTED = 1;

	public static final int GPS_SUBSTATE_FIXED = 2;

	/**
	 * Check gps status
	 */
	private int mGpsStatus = GPS_STATE_UNKNOW;

	private Context mContext;
	private volatile boolean mStarted;

	// 定义LocationManager对象
	private LocationManager mLocationManager;
	private GpsDetector mGpsDetector;

	private GpsStatus mInOutStaus;

	public IoGpsProvider(Context context) {
		this.mContext = context.getApplicationContext();
		this.mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		this.mGpsDetector = GpsDetector.getInstance();
	}

	public void startup(Handler handler, long millis, boolean isDaemon) {
		if (mStarted) {
			return;
		}
		mStarted = true;

		LocationManager manager = mLocationManager;

		try {
			manager.addGpsStatusListener(this);

//			if (!isDaemon) {
//				manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, millis, 0.0f, this, handler.getLooper());
//			} else {
				manager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, millis, 0.0f, this,
						handler.getLooper());
//			}
		} catch (SecurityException e) {
		} catch (Exception e) {
		}
	}

	public final void shutdown() {
		if (!mStarted) {
			return;
		}
		mStarted = false;

		LocationManager manager = mLocationManager;
		try {
			manager.removeGpsStatusListener(this);
		} catch (Exception e) {
		}
		try {
			manager.removeUpdates(this);
		} catch (SecurityException e) {

		} catch (Exception e) {
		}
	}

	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {
		case GpsStatus.GPS_EVENT_STARTED:
			mGpsStatus |= GPS_SUBSTATE_STARTED;
			break;
		case GpsStatus.GPS_EVENT_STOPPED:
			mGpsStatus = GPS_SUBSTATE_STOPED;
			break;
		case GpsStatus.GPS_EVENT_FIRST_FIX:
			mGpsStatus |= GPS_SUBSTATE_FIXED;
			break;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
			LocationManager locationManager = mLocationManager;
			try {
				if (mInOutStaus == null) {
					mInOutStaus = locationManager.getGpsStatus(null);
				} else {
					locationManager.getGpsStatus(mInOutStaus);
				}
			} catch (SecurityException e) {
			}

			boolean outDoor = true;
			if (mInOutStaus != null) {
				notifyIOGpsEvent(mInOutStaus);
			}
			break;
		}
	}

	private void notifyIOGpsEvent(GpsStatus status) {
		// Log.d(TAG, "notifyIOGpsEvent: ");
		mGpsDetector.onGpsEvent(status);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}
}
