package com.soso.evaextra.pdr;

import java.util.Arrays;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;

import com.soso.evaextra.LocationTestActivity;
import com.soso.evaextra.Proxy;
import com.tencent.map.geolocation.internal.TencentExtraKeys;
//import com.tencent.map.geolocation.util.SosoLocUtils;

/* 1. 创建实例前已确保 LocationManager 不为 null
 * 2. 公开的方法非线程安全的, 调用者应处理同步问题
 * 3. 负责完成坐标偏转, 保证给上层的提供满足需坐标
 * */
final class TxGpsProvider implements LocationListener,
		GpsStatus.Listener,GpsStatus.NmeaListener{

	private static final String TAG = "TxGpsProvider";
	private static final boolean DEBUG = false;

	/** System GPS Status */
	public static final int GPS_STATE_UNKNOW = 1024;

	public static final int GPS_STATE_DISABLED = 0;

	public static final int GPS_STATE_ENABLED = 4;

	public static final int GPS_SUBSTATE_STOPED = 0;

	public static final int GPS_SUBSTATE_STARTED = 1;

	public static final int GPS_SUBSTATE_FIXED = 2;

	private long mLastGpsTime = 0L;
	private final Context mAppContext;
	private final LocationManager mLocationManager;

	/** Check gps status */
	private int mGpsStatus = GPS_STATE_UNKNOW;

	private boolean mIsGPSUsedStarAvailable = false;
	private boolean mIsGPSViewStarAvailable = false;

	private int mVisibleSatellite = 0;
	private int mUsedSatellite = 0;

	private volatile boolean mStarted;


	private GpsStatus mInOutStaus;

	public TxGpsProvider(Context appContext) {
		mAppContext = appContext;
		mLocationManager = (LocationManager)mAppContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public void startup(Handler handler, long millis) {
		if (mStarted) {
			return;
		}
		mStarted = true;

		millis = 1000L;

		try {
			mLocationManager.addGpsStatusListener(this);
			mLocationManager.addNmeaListener(this);
			mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, millis, 0.0f, this, handler.getLooper());
		} catch (Exception e) {
		}
	}

	public final void shutdown() {
		if (!mStarted) {
			return;
		}
		mStarted = false;

		mLastGpsTime = 0;
		mGpsStatus = GPS_STATE_UNKNOW;
		mIsGPSUsedStarAvailable = false;
		mIsGPSViewStarAvailable = false;
		mVisibleSatellite = 0;
		mUsedSatellite = 0;
		Arrays.fill(mDeflected, 0);

		try {
			mLocationManager.removeGpsStatusListener(this);
		} catch (Exception e) {
		}
		try {
			mLocationManager.removeUpdates(this);
		} catch (Exception e) {
		}
	}


	
	private boolean isInteger(double a){
		Double D = Double.valueOf(a);
		if(Math.abs(D.longValue()-a)<Double.MIN_VALUE){
			return true;
		}
		return false;
	}

	private boolean isLaLoRegular(double lat, double lng, float accurency) {
//		if(accurency > 1000)
//			return false;
		if(isInteger(lat) && isInteger(lng))
			return false;
		if (Math.abs(lat) < 1E-8 || Math.abs(lng) < 1E-8) {
			if(TencentExtraKeys.DIDI_INTERNAL) {
				return false;
			}
		} else if (Math.abs(lat - 1.0) < 1E-8 || Math.abs(lng - 1.0) < 1E-8) {
			return false;
		} else if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
			return false;
		}
		return true;
	}

	/** 用于存放本地偏转后的坐标, 0 - lat, 1 - lng */
	private final double[] mDeflected = new double[2];
	//private Location[] mLocationList = new Location[4];

	private void notifyListeners(Location location) {
		
		//SosoLocUtils.LocalGPSAid(location, mDeflected);
		location.setLatitude(mDeflected[0]);
		location.setLongitude(mDeflected[1]);
		Intent it = new Intent();
		it.setAction(Proxy.SET_LOCATION_ACTION);
		Bundle bundle = new Bundle();
		//bundle.putSerializable("location", location);
		bundle.putParcelable("location", location);
		bundle.putInt("mVisibleSatellite", mVisibleSatellite);
		bundle.putInt("mUsedSatellite", mUsedSatellite);
		it.putExtra("bundle", bundle);
        mAppContext.sendBroadcast(it);     
//		TxGpsInfo gpsInfo = new TxGpsInfo(location, mLastGpsTime,
//				mVisibleSatellite, mUsedSatellite, mGpsStatus);
	}



	

	@Override
	public void onLocationChanged(Location location) {
		try{
			if (location == null
					|| !LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
				return;
			}
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			float accurency = location.getAccuracy();
			if (!isLaLoRegular(lat, lng, accurency)) {
				return;
			}
	
			updateSatelliteStat();
			mGpsStatus |= GPS_SUBSTATE_FIXED;
	
			mLastGpsTime = System.currentTimeMillis();
			// LogUtil.i(TAG, "onLocationChanged: " + DateFormat.format("yyyy-MM-dd kk:mm:ss", mLastGpsTime));
			notifyListeners(location);
		}catch(Exception e){   //在某些机型上，会出现调用系统api getLatitude出现NPE的情况，这里加入保护，防止crash
		}catch (Error e) {}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// ignore
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			Log.i(TAG, "onProviderEnabled: gps is enabled");
			mGpsStatus = GPS_STATE_ENABLED;
		}

	}

	@Override
	public void onProviderDisabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			Log.i(TAG, "onProviderDisabled: gps is disabled");

			mVisibleSatellite = mUsedSatellite = 0;
			mGpsStatus = GPS_STATE_DISABLED;
			mIsGPSUsedStarAvailable = false;
			mLastGpsTime = 0L;
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
		}
		updateSatelliteStat();

		// 强行更新 mIsGPSUsedStarAvailable
		// 强行更新 mIsGPSViewStarAvailable
		isStarNumRegular();
	}

	private void updateSatelliteStat() {
		mVisibleSatellite = mUsedSatellite = 0;

		LocationManager manager = mLocationManager;
		GpsStatus gpsStatus = manager.getGpsStatus(null);
		if (gpsStatus == null) {
			return;
		}

		int maxSatelliteNum = gpsStatus.getMaxSatellites();
		Iterator<GpsSatellite> satelliteItr = gpsStatus.getSatellites()
				.iterator();
		if (satelliteItr == null) {
			return;
		}
		while (satelliteItr.hasNext() && mVisibleSatellite <= maxSatelliteNum) {
			++mVisibleSatellite;
			if (satelliteItr.next().usedInFix()) {
				++mUsedSatellite;
			}
		}
	}


	/**
	 * 判断卫星个数是否合法
	 *
	 * @return 是否合法
	 */
	private boolean isStarNumRegular() {
		int viewGPSNum = getGPSViewSatellitesNum();
		int usedGPSNum = getGPSUsedSatellitesNum();
		if (viewGPSNum > 0) {
			mIsGPSViewStarAvailable = true;
		}
		if (usedGPSNum > 0) {
			mIsGPSUsedStarAvailable = true;
		}

		// if (false) {
		//	LogUtil.i(TAG, "vNum=" + viewGPSNum + ",uNum=" + usedGPSNum
		//			+ ",vAvail=" + mIsGPSViewStarAvailable + ",uAvail="
		//			+ mIsGPSUsedStarAvailable);
		//}

		if (mIsGPSViewStarAvailable) {
			if (viewGPSNum <= 2) {
				return false;
			}
		}
		if (mIsGPSUsedStarAvailable) {
			if (usedGPSNum >= 3 || usedGPSNum == 0) { // 有些机器上这里读出来的是0
				return true;
			}
		} else {
			if (usedGPSNum == 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取当前GPS能够看到的卫星数
	 *
	 * @return 当前GPS的卫星数
	 */
	private int getGPSViewSatellitesNum() {
		// 构造方法已保证 mAppContext.getLocationManager() 不会返回 null
		GpsStatus status = mLocationManager.getGpsStatus(null);
		if (status == null) {
			return -1;
		}

		int maxSatellites = status.getMaxSatellites();
		Iterator<GpsSatellite> it = status.getSatellites().iterator();
		if (it == null) {
			return -1;
		}

		int count = 0;
		while (it.hasNext() && count <= maxSatellites) {
			it.next();
			++count;
		}
		return count;
	}

	/**
	 * 获取当前GPS能够使用的卫星数
	 *
	 * @return 当前GPS的卫星数
	 */
	private int getGPSUsedSatellitesNum() {
		// 构造方法已保证 mAppContext.getLocationManager() 不会返回 null
		GpsStatus status = mLocationManager.getGpsStatus(null);
		if (status == null) {
			return -1;
		}

		int maxSatellites = status.getMaxSatellites();
		Iterator<GpsSatellite> it = status.getSatellites().iterator();
		if (it == null) {
			return -1;
		}

		int count = 0;
		while (it.hasNext() && count <= maxSatellites) {
			if (it.next().usedInFix()) {
				++count;
			}
		}
		return count;
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {
		// TODO Auto-generated method stub
		
	}
}
