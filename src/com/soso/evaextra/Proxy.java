package com.soso.evaextra;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.edu.hust.cm.common.util.Conditions;
import cn.edu.hust.cm.common.util.Files;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.sogou.map.loc.SGErrorListener;
import com.sogou.map.loc.SGLocClient;
import com.sogou.map.loc.SGLocListener;
import com.sogou.map.loc.SGLocation;
import com.soso.evaextra.LocationService.ObservableExt;
import com.soso.evaextra.SimpleDb.LogEntry;
import com.soso.evaextra.SimpleDb.SimpleDbUtil;
import com.soso.evaextra.model.Result;
import com.soso.evaextra.util.FileLogger;
import com.soso.evaextra.util.SosoLocUtils;
import com.tencent.map.geolocation.TencentDistanceAnalysis;
import com.tencent.map.geolocation.TencentDistanceListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.map.geolocation.TencentLocationUtils;
import com.tencent.map.geolocation.internal.TencentExtraKeys;
import com.tencent.map.geolocation.internal.TencentLog;
import com.tencent.map.geolocation.internal.TencentLogImpl;

public class Proxy implements TencentDistanceListener,TencentLocationListener, BDLocationListener,
		AMapLocationListener, SGLocListener, SGErrorListener {
	private static final boolean SOGOU_GCJ02 = true;
	private static final double[] SOGOU_LAT_LNG = new double[2];

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyyMMdd_kkmmss", Locale.ENGLISH);
	private static final SimpleDateFormat TIME2 = new SimpleDateFormat(
			"MM-dd kk:mm:ss", Locale.ENGLISH);
	private static final File SD = Environment.getExternalStorageDirectory();
	private static final String TAG = "Proxy";

	private final LocationService mContext;
	private final SimpleDb mSimpleDb;
	
	/**
	 * 用于单点测试的日志
	 */
	private FileLogger mSinglePointLog;
	
	/**
	 * 用于 PointShow 分析的log
	 */
	private FileLogger mPointShowLog;

	/**
	 * 详细 log
	 */
	private FileLogger mDetailLog;
	/**
	 * 用户log
	 */
	private FileLogger mUserLog;
	private double bdistance = 0;
	private AMapLocation aLocation = null;
	private BDLocation bLocation = null;
	private double adistance = 0;
	private double sdistance = 0;
	private SGLocation sLocation = null;

	private final AppStatus mAppStatus;
	private final LocationCouter mLocationCouter;

	private LocationClient mBaiduLocationClient;

	private SGLocClient mSgLocClient;
	
	private AMapLocationClient mAmapLocationClient;
	
	private boolean mIsRunning;
	
	public static final String SET_LOCATION_ACTION =
	        "com.soso.evaextra.ACTION_SET_LOCATION";
	
	public static final String SEND_TENCENT_LOCATION_DATA = 
			"com.soso.evaextra.ACTION_SEND_TENCENT_LOCATION";
	
	public static final String SEND_TENCENT_LOCATION_SPEED = 
			"com.soso.evaextra.ACTION_SEND_TENCENT_LOCATION_SPEED";
	
	public static final String STOP_LOCATION = 
			"com.soso.evaextra.STOP_LOCATION";
	
	private boolean isStop = false;//默认是没有关闭的，只有点击stop按钮时变为true
	
	private boolean onlyChangeStart = false;//只有在点击start按钮的时候触发一次
	
	private TelephonyManager telephonyManager;
	private String imei;
	
	private final BroadcastReceiver mSetLocationReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (SET_LOCATION_ACTION.equals(action)) {
				double[] location=new double[2];
				location[0]=intent.getDoubleExtra("latitude", -1);
				location[1]=intent.getDoubleExtra("longtitude", -1);
				setLocation(location);
				if(isStop){
					mContext.unregisterReceiver(mSetLocationReceiver);
					isStop = false;
				}
			}
		}
	};

	public Proxy(LocationService context) {
		super();
		this.mContext = context;
		this.mSimpleDb = new SimpleDb(mContext);
		this.mAppStatus = AppContext.getInstance(context).getAppStatus();
		this.mLocationCouter = AppContext.getInstance(context)
				.getLocationCouter();	
		telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
	}
																				
	public void startLocation(boolean[] checked) {
		if (mIsRunning) {
			return;
		}
		
		onlyChangeStart = true;
		
		final Date now = new Date();
		final String filename = SDF.format(now);
		
		//将文件名称记录下来，以便点击退出时可以删除文件
		SharedPreferences sp = mContext.getSharedPreferences("filename", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putString("name", filename);
		editor.commit();
		
		this.mSinglePointLog = new FileLogger(new File(SD,"SingleTest"),filename +  "_singletestshow_");
		
		this.mPointShowLog = new FileLogger(new File(SD, "MapEva"), filename
				+ "_pointshow_");
		this.mDetailLog = new FileLogger(new File(SD, "MapEva"), filename
				+ "_details_");
		this.mUserLog = new FileLogger(new File(SD, "MapEva"), filename
				+ "_UserLogdetails_");
		
		LogEntry entry = new LogEntry();
		mAppStatus.logId = SimpleDbUtil.insert(mSimpleDb,
				entry.setLogName(filename).setLogStart(now.getTime()));
		File tmp = mContext.getFileStreamPath("tmp");
		if (tmp.exists()) {
			tmp.delete();
		}

		if (checked[0]) {
			startTencentLocation();
			Log.i(TAG, "tencent started ");
		}
		if (checked[1]) {
			startBaiduLocation();
			Log.i(TAG, "baidu started ");
		}
		if (checked[2]) {
			startAmapLocation();
			Log.i(TAG, "amap started ");
		}
		if (checked[3]) {
			startSogouLocation();
			Log.i(TAG, "sogou started ");
		}
		mIsRunning = true;
		IntentFilter filter = new IntentFilter(SET_LOCATION_ACTION);
		mContext.registerReceiver(mSetLocationReceiver, filter);
		
//		mTimerHandler.sendEmptyMessageDelayed(1, 60*1000);
	}

    /**
     * 更新定位结果log
     * @param result 定位结果
     * @param SDKname SDK的名称   如AppContext.TENCENT
     * @param notifyChange 是否通知改变（错误/无结果/手动设置不需要通知）
     */
	private void updateLog(final Result result,final String SDKname,final boolean notifyChange) {
		mDetailLog.log(SDKname, result.toString());
		mPointShowLog.log(SDKname, result.toPointShowString());
		mUserLog.logWithoutEncrypt(AppContext.ALL_SDKS, result.toString());
		mSinglePointLog.logWithoutEncrypt(AppContext.ALL_SDKS, result.toSingleString());
		if (notifyChange) {
			notifyChange(SDKname, result);
		}	
	}	
	
	/**
	 * 手动设置定位结果点
	 */
	public void setLocation(double[] location) {
		
		if (location.length!=2) {
			return;
		}
		trafficStat();
	
		if (mAppStatus.sogouFirst == -1) {
			mAppStatus.sogouFirst = SystemClock.elapsedRealtime();
		}

		Result result = new Result(AppContext.LOCATION_CORRECTION);
		result.setTime(TIME2.format(new Date()));

		result.setReason("M");
		result.setError(0 + "");

		double latitude = location[0];
		double longitude = location[1];
		float accurancy = (float) 0.5;
		int offset = 5;
		result.setData(0 + "");
		result.setLat(latitude);
		result.setLng(longitude);
		result.setRadius(accurancy);
		result.setOffSet(offset + "");
		result.setImei(imei);
		
		updateLog(result,AppContext.LOCATION_CORRECTION,false);
		
	}	

	public void startLocation(List<String> exclude) {
		
		startTencentLocation();
		Log.i(TAG, "tencent started ");

		startBaiduLocation();
		Log.i(TAG, "baidu started ");

		startAmapLocation();
		Log.i(TAG, "amap started ");
		
		startSogouLocation();
		Log.i(TAG, "sogou started ");
		
		//mTimerHandler.sendEmptyMessageDelayed(1, 60*1000);
	}

	public void stopLocation() {
		
		if (!mIsRunning) {
			return;
		}
		
		//点击停止时，获取十字中心点的位置,并记录到日志文件里面
		//当点击停止按钮时，向activity发送广播，告知要结束，对方接收到广播后，将十字中心点的位置发送过来
		Intent it = new Intent();
		it.setAction(STOP_LOCATION);
		it.putExtra("stop", true);
		mContext.sendBroadcast(it);
		
		stopTencentLocation();
		Log.i(TAG, "tencent stopped ");

		stopBaiduLocation();
		Log.i(TAG, "baidu stopped ");

		stopAmapLocation();
		Log.i(TAG, "amap stopped ");

		stopSogouLocation();
		Log.i(TAG, "sogou stopped ");
		//停止定位时更新产生的数据到数据库中
		File tmp = updateDb();

		if (tmp.exists()) {
			tmp.delete();
		}
		mSimpleDb.close();
		mIsRunning = false;
		//因为不能确定广播何时才能够完成，所以在这里不移除监听，改为在二次监听结束后移除
//		mContext.unregisterReceiver(mSetLocationReceiver);
		isStop = true;
		
		mTimerHandler.removeMessages(1);
		
	}
	
	
	/**
	* @Title: updateDb 
	* @Description: 更新定位文件的信息到数据库中  
	* @param @return  
	* @return File
	* @throws
	 */
	private File updateDb() {
		long id = mAppStatus.logId;
		LogEntry entry = SimpleDbUtil.findById(mSimpleDb, id);
		entry.log_duration = mAppStatus.getLocationDuration();
		File tmp = mContext.getFileStreamPath("tmp");
		entry.log_path = mPointShowLog.mergePointShowLog(
				entry.log_duration / 1000 / 60, getDeviceId(), tmp);
		SimpleDbUtil.update(mSimpleDb, entry);
		return tmp;
	}
	/**
	 * 定时更新数据库计时器,防止因程序crash丢失文件
	 */
	private final Handler mTimerHandler = new Handler() {
		public void handleMessage(final android.os.Message msg) {
			
			File tmp = updateDb();
			if (tmp.exists()) {
				tmp.delete();
			}
			sendEmptyMessageDelayed(1, 60*1000);
		}
	};
	
	private String getDeviceId() {
		TelephonyManager telManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telManager != null) {
			return telManager.getDeviceId();
		}
		return "_no_imei_";
	}

	private void startTencentLocation() {
		mAppStatus.tencentStart = SystemClock.elapsedRealtime();

		TencentLocationRequest request = TencentLocationRequest.create()
				.setInterval(5000)
				.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME)
				.setAllowCache(true);
//		request = TencentExtraKeys.setAllowGps(request, false);
//		request = TencentLocationManager.setAllowGps(request, false);
//		if(!TencentLocationManager.isAllowGps(request)){
//			Log.e("Proxy", "gps is closed");
//		}
		File file = mContext.getExternalCacheDir();
		TencentLog log = new TencentLogImpl(mContext, file);
		TencentExtraKeys.addTencentLog(log);
		TencentLocationManager locationManager = TencentLocationManager
				.getInstance(mContext);

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		String value = sharedPreferences.getString(
				"key_location_settings_url_index", "0");
		//locationManager.setServerIndex(Integer.valueOf(value));

		HandlerThread ht = new HandlerThread("Proxy");
		ht.start();
		locationManager.requestLocationUpdates(request, this, ht.getLooper());
		//locationManager.requestLocationUpdates(request, this);
		locationManager.startDistanceCalculate(this);
//		double[] a = {40.074403333333333,116.35256};
//		double[] b = {1,2};
//		if(TencentLocationUtils.wgs84ToGcj02(a, b))
//			Log.e("a", b[0]+","+b[1]);
	}

	private void startBaiduLocation() {
		if (mBaiduLocationClient == null) {
			mBaiduLocationClient = new LocationClient(mContext);
		}

		mAppStatus.baiduStart = SystemClock.elapsedRealtime();

		// 注册监听函数
		// set baidu listener option
		LocationClientOption option = new LocationClientOption(); 
//		option.setOpenGps(true);
		option.setIsNeedAddress(true);
		option.setCoorType("gcj02");
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置高精度定位模式
		option.setScanSpan(5000);
		mBaiduLocationClient.setLocOption(option);
		mBaiduLocationClient.registerLocationListener(this);
		// open baudu listener
		mBaiduLocationClient.start();
	}

	private void startAmapLocation() {
		mAppStatus.amapStart = SystemClock.elapsedRealtime();
		if(mAmapLocationClient == null){
			mAmapLocationClient = new AMapLocationClient(mContext);
		}
		AMapLocationClientOption amapOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		amapOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		//设置是否返回地址信息（默认返回地址信息）
		amapOption.setNeedAddress(true);
		//设置是否只定位一次,默认为false
		amapOption.setOnceLocation(false);
		//设置是否强制刷新WIFI，默认为强制刷新
		amapOption.setWifiActiveScan(true);
		//设置是否允许模拟位置,默认为false，不允许模拟位置
		amapOption.setMockEnable(false);
		//设置定位间隔,单位毫秒,默认为2000ms
		amapOption.setInterval(5000);
		//给定位客户端对象设置定位参数
		mAmapLocationClient.setLocationOption(amapOption);
		//监听回调
		mAmapLocationClient.setLocationListener(this);
		//启动定位
		mAmapLocationClient.startLocation();
	}

	private void startSogouLocation() {
		if (mSgLocClient == null) {
			mSgLocClient = new SGLocClient(mContext);
			mSgLocClient.setKey("b9bde990238e77b6a80297ff29ccfa00a244a598");
			mSgLocClient.setStrategy(SGLocClient.NETWORK_FIRST);
			// clientInst.addErrorListener(errListener);
			if (SOGOU_GCJ02) {
				mSgLocClient.setProp("go2map-coordinate", "latlon");
			}
		}
		mAppStatus.sogouStart = SystemClock.elapsedRealtime();

		mSgLocClient.addLocListener(this);
		mSgLocClient.watchLocation(5000);
	}

	

	private void stopTencentLocation() {
		TencentDistanceAnalysis tda = TencentLocationManager.getInstance(mContext).stopDistanceCalculate(this);
		TencentLocationManager.getInstance(mContext).removeUpdates(this);
		String line = "D|confidence=" + tda.getConfidence() + ",gpscount=" + tda.getGpsCount() + ",networkcount=" + tda.getNetworkCount()+ "\n";
		try {
			Files.append(SosoLocUtils.encryptBytes(line.getBytes()),
					mContext.getFileStreamPath("tmp"));
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	private void stopBaiduLocation() {
		if (mBaiduLocationClient != null) {
			mBaiduLocationClient.stop();
			mBaiduLocationClient.unRegisterLocationListener(this);
			mBaiduLocationClient = null;
			bdistance=0;
			bLocation = null;
		}
	}

	private void stopAmapLocation() {
		if(mAmapLocationClient != null){
			mAmapLocationClient.stopLocation();//停止定位
			mAmapLocationClient.onDestroy();//销毁定位客户端。
			mAmapLocationClient = null;
			adistance=0;
			aLocation = null;
		}
		
	}
	private void stopSogouLocation() {
		if (mSgLocClient != null) {
			mSgLocClient.clearWatch();
			mSgLocClient.removeErrorListener(this);
			mSgLocClient.removeLocListener(this);
			mSgLocClient.destroy();
			mSgLocClient = null;
		}
	}
	/**
	 * 将 result 加入全局记录中并通知观察者
	 * 
	 * @param key
	 * @param result
	 */
	private void notifyChange(String key, Result result) {
		AppContext appContext = AppContext.getInstance(mContext);
		appContext.putLocation(key, result);

		ObservableExt o = mContext.mObservable;
		o.setChanged();
		o.notifyObservers();
	}


	/*********************************amap***************************************/
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		
		trafficStat();
		
	    if (Conditions.isNull(amapLocation)) {
	    	return;
	    }
    	if (mAppStatus.amapFirst == -1) {
			mAppStatus.amapFirst = SystemClock.elapsedRealtime();
		}
    	Result result = new Result(AppContext.AMAP);
		result.setTime(TIME2.format(new Date()));
	    	
        if (amapLocation.getErrorCode() == 0) {
        	result.setReason("A");
			result.setError("none");
			double latitude = amapLocation.getLatitude();
			double longitude = amapLocation.getLongitude();
			if(aLocation != null){
				adistance+=TencentLocationUtils.distanceBetween(aLocation.getLatitude(), aLocation.getLongitude(), latitude, longitude);
			}
			aLocation = amapLocation;
			float accurancy = amapLocation.getAccuracy();
			int offset = 5;
			result.setData(0 + "");
			result.setLat(latitude);
			result.setLng(longitude);
			result.setRadius(accurancy);
			result.setOffSet(offset + "");	
			result.setDistance(adistance/1000);
			result.setImei(imei);
	    } else {
	    	String errorMsg = amapLocation.getErrorInfo();
			result.setReason("高德"+errorMsg);
			mLocationCouter.increaseLocationCount(AppContext.AMAP);
	        
	    }
	    mAppStatus.setAddr(AppContext.AMAP,amapLocation.getAddress());
		updateLog(result, AppContext.AMAP, true);
	}

	/*********************************baidu***************************************/
	@Override
	public void onReceiveLocation(BDLocation location) {
		trafficStat();

		if (Conditions.isNull(location)) {
			return;
		}
		if (mAppStatus.baiduFirst == -1) {
			mAppStatus.baiduFirst = SystemClock.elapsedRealtime();
		}

		Result result = new Result(AppContext.BAIDU);
		result.setTime(TIME2.format(new Date()));

		int error = location.getLocType();
		if (error == 161 || error == 61) {
			result.setReason("B");
			result.setError(error + "");

			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			float accurancy = location.getRadius();
			int offset = 5;
			if(bLocation != null)
				bdistance+=TencentLocationUtils.distanceBetween(bLocation.getLatitude(), bLocation.getLongitude(), latitude, longitude);
			bLocation= location;
			result.setData(0 + "");

			result.setLat(latitude);
			result.setLng(longitude);
			result.setRadius(accurancy);
			result.setOffSet(offset + "");
			result.setDistance(bdistance/1000);
			result.setImei(imei);
		} else {
			result.setReason("百度定位失败");
			result.setError(error + "");
			mLocationCouter.increaseLocationCount(AppContext.BAIDU);
		}
		mAppStatus.setAddr(AppContext.BAIDU,location.getAddrStr());
		updateLog(result, AppContext.BAIDU, true);

	}

	@Override
	public void onLocationChanged(TencentLocation arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		Log.e("a", arg0.getAccuracy()+"");
	}

	@Override
	public void onStatusUpdate(String arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
		
	}
	
	/*********************************tencent***************************************/
	@Override
	public void onDistanceChanged(TencentLocation location, double distance, int error,
			String reason) {
		// TODO Auto-generated method stub
		trafficStat();

		if (Conditions.isNull(location)) {
			return;
		}
		if (mAppStatus.tencentFirst == -1) {
			mAppStatus.tencentFirst = SystemClock.elapsedRealtime();
		}

		Result result = new Result(AppContext.TENCENT);
		result.setTime(TIME2.format(new Date()));

		if (error == TencentLocation.ERROR_OK) {
			result.setReason("T");
			result.setError(error + "");

			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			float accurancy = location.getAccuracy();
			int offset = 5;

			result.setData(0 + "");
			result.setLat(latitude);
			result.setLng(longitude);
			result.setRadius(accurancy);
			result.setOffSet(offset + "");
			result.setDistance(distance);
			result.setSpeed(location.getSpeed());
			result.setProvider(location.getProvider());
			result.setImei(imei);
			
			//腾讯定位成功后，将点的数据传送到LocationMapActivity类中，然后在LocationMapActivity类中将十字中心的点的坐标设置为腾讯的坐标
			if(onlyChangeStart){
				Intent it = new Intent();
				it.setAction(SEND_TENCENT_LOCATION_DATA);
				Bundle bundle = new Bundle();
				bundle.putSerializable("result", result);
				it.putExtra("bundleData", bundle);
				mContext.sendBroadcast(it);
				onlyChangeStart = false;
			}
			
			//将定位的结果通过广播发送到LocationTestActivity中去，实时的更新speed元素
			Intent speed = new Intent();
			speed.setAction(SEND_TENCENT_LOCATION_SPEED);
			Bundle bundle = new Bundle();
			bundle.putSerializable("result", result);
			speed.putExtra("bundle", bundle);
			mContext.sendBroadcast(speed);
			
			//请求的query，需要存储在日志文件中
			String req = TencentExtraKeys.getRawQuery(location);
			mUserLog.logWithoutEncrypt(AppContext.ALL_SDKS, req);
			mSinglePointLog.logWithoutEncrypt(AppContext.ALL_SDKS, req);
			String line = "T|" + latitude + "," + longitude + "," + accurancy
					+ "|" + req + "|" + System.currentTimeMillis() +","+distance+","+location.getSpeed()+","+location.getProvider()+"\n";
			try {
				Files.append(SosoLocUtils.encryptBytes(line.getBytes()),
						mContext.getFileStreamPath("tmp"));
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		} else {
			result.setReason("腾讯定位失败");
			result.setError(error + "");
			mLocationCouter.increaseLocationCount(AppContext.TENCENT);
		}

		mAppStatus.setAddr(AppContext.TENCENT,location.getAddress());
		updateLog(result, AppContext.TENCENT, true);
	}
	/*********************************sogou***************************************/
	@Override
	public void onError(int error, String arg1) {
		Result result = new Result(AppContext.SOGOU);
		System.out.println("test " + error);
		result.setReason("sogou定位失败");
		result.setError(error + "");
		mLocationCouter.increaseLocationCount(AppContext.SOGOU);
		updateLog(result, AppContext.SOGOU, false);
	}
    //sogou
	@Override
	public void onLocationUpdate(SGLocation location) {
		System.out.println("test " + location);
		trafficStat();
		if (Conditions.isNull(location)) {
			return;
		}
		if (mAppStatus.sogouFirst == -1) {
			mAppStatus.sogouFirst = SystemClock.elapsedRealtime();
		}

		Result result = new Result(AppContext.SOGOU);
		result.setTime(TIME2.format(new Date()));

		result.setReason("S");
		result.setError(0 + "");
		

		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		float accurancy = location.getAccuracy();
		int offset = 5;

		if(sLocation != null){
			sdistance+=TencentLocationUtils.distanceBetween(bLocation.getLatitude(), bLocation.getLongitude(), latitude, longitude);
			sLocation= location;
		}
		if (SOGOU_GCJ02) {
			EvilTransform.transform(latitude, longitude, SOGOU_LAT_LNG);
			latitude = SOGOU_LAT_LNG[0];
			longitude = SOGOU_LAT_LNG[1];
		}
		result.setData(0 + "");

		result.setLat(latitude);
		result.setLng(longitude);
		result.setRadius(accurancy);
		result.setOffSet(offset + "");
		result.setDistance(sdistance/10000);
		result.setImei(imei);
		
		updateLog(result,AppContext.SOGOU,true);
	}
	private void trafficStat() {
		final int uid = Process.myUid();
		mAppStatus.setTraffic(TrafficStats.getUidRxBytes(uid)
				+ TrafficStats.getUidTxBytes(uid));
	}
	
}
