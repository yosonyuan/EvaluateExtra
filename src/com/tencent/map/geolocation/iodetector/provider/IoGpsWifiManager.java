package com.tencent.map.geolocation.iodetector.provider;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tencent.map.geolocation.iodetector.IODetectorManager;

/**
 * Created by toveyliu on 2016/8/29.
 */

public class IoGpsWifiManager {
	private static final String TAG = "IOGpsWifiManager";

	private static IoGpsWifiManager mIOGWManager;

	private Context mContext;

	private boolean mProvidersNotFound;

	private IoGpsProvider mGpsProvider;
	private IoWifiProvider mWifiProvider;

	private MyHandler mHandler;

	private IoGpsWifiManager(Context context) {
		if (context != null) {
			mContext = context;
		}
		mGpsProvider = createGpsProvider();
		mWifiProvider = createWifiProvider();
		mProvidersNotFound = (mGpsProvider == null) && (mWifiProvider == null);
	}

	public static synchronized IoGpsWifiManager getInstance(Context context) {
		if (context == null) {
			throw new IllegalStateException("context cannot be null!");
		}
		if (mIOGWManager == null) {
			synchronized (IoGpsWifiManager.class) {
				mIOGWManager = new IoGpsWifiManager(context);
			}
		}
		return mIOGWManager;
	}

	@Nullable
	private IoWifiProvider createWifiProvider() {
		if (!hasWifiManager()) {
			Log.e(TAG, "createWifiProvider: failed");
			return null;
		}
		return IoWifiProvider.getInstance();
	}

	@Nullable
	private IoGpsProvider createGpsProvider() {
		if (!hasLocationManager()) {
			Log.e(TAG, "createGpsProvider: failed");
			return null;
		}
		return new IoGpsProvider(mContext);
	}

	private boolean hasLocationManager() {
		return mContext.getSystemService(Context.LOCATION_SERVICE) != null;
	}

	private boolean hasWifiManager() {
		return mContext.getSystemService(Context.WIFI_SERVICE) != null;
	}

	public void start(Looper looper) {
		prepareHandler(looper);
		shutdownProviders();
		startupProviders();
	}

	public void stop() {
		shutdownProviders();
		if (mHandler != null) {
			mHandler.shutdown();
		}
	}

	private void startupProviders() {
		boolean useNetwork = true;

		if (useNetwork && mWifiProvider != null) {
			mWifiProvider.startup(mHandler);
		}

		if (mGpsProvider != null) {
			// mGpsProvider.setNeedDeflect(false);
			mGpsProvider.startup(mHandler, 3000 - 2000, false);
		}
	}

	private void shutdownProviders() {
		if (mWifiProvider != null) {
			mWifiProvider.shutdown();
		}
		if (mGpsProvider != null) {
			mGpsProvider.shutdown();
		}
	}

	private void prepareHandler(Looper looper) {
		if (Looper.myLooper() == null)
			Looper.prepare();
		if (mHandler == null) {
			mHandler = new MyHandler(looper);
			return;
		}

		mHandler.removeCallbacksAndMessages(null);
		if (mHandler.getLooper() != looper) {
			mHandler = new MyHandler(looper);
		}
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		public void shutdown() {
			removeCallbacksAndMessages(null);
		}
	}
}
