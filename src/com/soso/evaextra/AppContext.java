package com.soso.evaextra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.soso.evaextra.config.UI;
import com.soso.evaextra.model.Result;
import com.soso.evaluateextra.R;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.internal.TencentExtraKeys;
import com.tencent.map.geolocation.internal.TencentLog;

public final class AppContext extends Application {

	public static final String TENCENT = "T";
	public static final String BAIDU = "B";
	public static final String AMAP = "A";
	public static final String SOGOU = "s";
	/**
	 * 设置定位结果 暂时使用sougo的保存位置
	 */
	public static final String LOCATION_CORRECTION = "s";
	public static final String ALL_SDKS = "all";

	/**
	 * sdk的key, 本数组不可被修改
	 */
	public static final String[] ALL_KEYS = new String[] { TENCENT, BAIDU,
			AMAP, SOGOU };
	
	
	public TencentLocationManager tlm ;

	public static final String TITLE_TENCENT = "腾讯 V4.5.6";
	public static final String TITLE_BAIDU = "百度 V6.1.2";
	public static final String TITLE_AMAP = "高德 V2.0.0";
	public static final String TITLE_SOGOU = "搜狗 V1.0";
	public static final String[] TITLES = new String[] { TITLE_TENCENT,
			TITLE_BAIDU, TITLE_AMAP, TITLE_SOGOU };

	public static final int[] FRAG_IDS = new int[] { R.id.frag1, R.id.frag2,
			R.id.frag3, R.id.frag4 };

	public static final int[] POINT_COUNT = new int[] { 1, 5, 10, 20, 30,
			Integer.MAX_VALUE };
	public static final String[] POINT_COUNT_STR = new String[] { "1", "5",
			"10", "20", "30", "全部" };

	public static void shuffle() {
		Random random = new Random();
		if (UI.HIDE_SDK_NAME) {
			for (int i = 0; i < TITLES.length; i++) {
				TITLES[i] = "定位SDK-" + random.nextInt(100)
						+ random.nextInt(100);
			}
		}

		if (UI.SHUFFLE_SDK_SORT) {
			List<Integer> keys = new ArrayList<Integer>();
			keys.add(R.id.frag1);
			keys.add(R.id.frag2);
			keys.add(R.id.frag3);
			keys.add(R.id.frag4);
			Collections.shuffle(keys);
			for (int i = 0; i < ALL_KEYS.length; i++) {
				FRAG_IDS[i] = keys.get(i);
			}
		}
	}

	private final ArrayList<Result> mTencentLocations = new ArrayList<Result>();
	private final ArrayList<Result> mBdLocations = new ArrayList<Result>();
	private final ArrayList<Result> mAMapLocations = new ArrayList<Result>();
	private final ArrayList<Result> mSogouLocations = new ArrayList<Result>();
	private final HashMap<String, ArrayList<Result>> mLocationsMap = new HashMap<String, ArrayList<Result>>();

	private final LocationCouter mLocationCouter = new LocationCouter();
	private final LocationTimer mLocationTimer = new LocationTimer();

	private final AppStatus mAppStatus = new AppStatus();
	private final byte[] mLock = new byte[0];

	public static AppContext APP_CONTEXT;

	public static AppContext getInstance(Activity activity) {
		return (AppContext) activity.getApplication();
	}

	public static AppContext getInstance(Service service) {
		return (AppContext) service.getApplication();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mLocationsMap.put(TENCENT, mTencentLocations);
		mLocationsMap.put(BAIDU, mBdLocations);
		mLocationsMap.put(AMAP, mAMapLocations);
		mLocationsMap.put(SOGOU, mSogouLocations);
		TencentExtraKeys.setTencentLog(new TencentLog() {
			public void println(String t, int l, String m) {

			}
		});
		AppConfig appConfig = getAppConfig();
		appConfig.setCheckedSdk(appConfig.getCheckedSdk());

		APP_CONTEXT = this;
		
	}

	public AppStatus getAppStatus() {
		return mAppStatus;
	}

	public int getVersionCode() {
		int versionCode = 0;
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 向历史位置中添加一条数据
	 * 
	 * @param key
	 * @param point
	 */
	public void putLocation(String key, Result point) {
		ArrayList<Result> value = mLocationsMap.get(key);

		synchronized (mLock) {
			if (value != null) {
				value.add(point);
			}
		}
	}

	/**
	 * 获取所有的历史位置
	 * 
	 * @param key
	 * @return
	 */
	public List<Result> getAllLocations(String key) {
		ArrayList<Result> value = mLocationsMap.get(key);
		synchronized (mLock) {
			if (value == null || value.isEmpty()) {
				return Collections.<Result> emptyList();
			}

			return new ArrayList<Result>(value);
		}
	}

	/**
	 * 获取最近的若干条历史位置
	 * 
	 * @param key
	 * @param limit
	 *            条目数
	 * @return
	 */
	public List<Result> getLocations(String key, int limit) {
		ArrayList<Result> value = mLocationsMap.get(key);

		synchronized (mLock) {
			if (value == null || value.isEmpty()) {
				return Collections.<Result> emptyList();
			}
			int size = value.size();
			// XXX

			int from = size - limit;
			final int to = size;
			if (from < 0) {
				from = 0;
			}
			return new ArrayList<Result>(value.subList(from, to));
		}
	}

	public void removeLocations(String key) {
		if (key != null) {
			ArrayList<Result> value = mLocationsMap.get(key);

			synchronized (mLock) {
				if (value != null) {
					value.clear();
				}
			}
		} else {
			synchronized (mLock) {
				for (ArrayList<Result> v : mLocationsMap.values()) {
					v.clear();
				}
			}
		}
	}

	public LocationCouter getLocationCouter() {
		return mLocationCouter;
	}

	public LocationTimer getLocationTimer() {
		return mLocationTimer;
	}

	public AppConfig getAppConfig() {
		return AppConfig.getAppConfig(getApplicationContext());
	}

	/**
	 * 清空 app context 状态
	 * 
	 * @param appContext
	 */
	public static void clear(AppContext appContext) {
		AppStatus appStatus = appContext.getAppStatus();
		appStatus.setLocationRunning(false);
		appStatus.clearLocationStart();
		appStatus.clearLocationAddr();

		appStatus.amapFirst = -1;
		appStatus.amapStart = -1;
		appStatus.baiduFirst = -1;
		appStatus.baiduStart = -1;
		appStatus.tencentFirst = -1;
		appStatus.tencentStart = -1;
		appStatus.sogouFirst = -1;
		appStatus.sogouStart = -1;

		appStatus.setTraffic(-1);

		appStatus.setTotalDuration(0);

		LocationCouter couter = appContext.getLocationCouter();
		couter.resetLocationCount();

		appContext.removeLocations(null);
	}
}
