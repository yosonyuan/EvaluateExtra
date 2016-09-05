package com.tencent.map.geolocation.iodetector.provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.tencent.map.geolocation.iodetector.detector.WifiDetector;

import android.net.wifi.ScanResult;
import android.os.Handler;
import android.util.Log;

public class IoWifiProvider {
	private static final String TAG = "IoWifiProvider";

	private volatile boolean mStarted;
	private WifiDetector mWifiDetector;

	/**
	 * Singleton:static inner class
	 */
	private static class SingletonHolder {
		public static final IoWifiProvider INSTANCE = new IoWifiProvider();
	}

	private IoWifiProvider() {
		mWifiDetector = WifiDetector.getInstance();
	}

	public static IoWifiProvider getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public void startup(Handler handler) {
		if (mStarted) {
			return;
		}
		mStarted = true;
	}

	public void shutdown() {
		if (!mStarted) {
			return;
		}
		mStarted = false;
	}

	public void onLocationRawData(String rawData) {
		if (!mStarted) {
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject(rawData);
			JSONArray wifiArr = jsonObject.getJSONArray("wifis");

			Class cls = Class.forName("android.net.wifi.ScanResult");
			Constructor<?> cons[] = cls.getConstructors();

			List<ScanResult> results = new ArrayList<ScanResult>();
			for (int i = 0; i < wifiArr.length(); i++) {
				JSONObject tmpObj = wifiArr.getJSONObject(i);

				ScanResult tmpScanResult = (ScanResult) cons[0].newInstance();
				tmpScanResult.BSSID = tmpObj.getString("mac");
				tmpScanResult.level = tmpObj.getInt("rssi");
				results.add(tmpScanResult);
			}

			notifyIOWifiEvent(results);
		} catch (Exception e) {
			e.printStackTrace();
			
			notifyIOWifiEvent(Collections.<ScanResult> emptyList());
		}
	}

	private void notifyIOWifiEvent(List<ScanResult> list) {
		mWifiDetector.onWifiEvent(list);
		Log.d(TAG, "size = " + list.size());
	}
}
