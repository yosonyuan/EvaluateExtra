package com.soso.evaextra;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.ContentObserver;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import cn.edu.hust.cm.common.app.WidgetUtils;

import com.loc.ac;
import com.soso.evaextra.LocationService.LocationServiceProxy;
import com.soso.evaextra.config.Auto;
import com.soso.evaextra.config.UI;
import com.soso.evaextra.model.Result;
import com.soso.evaextra.update.AppUpdater;
import com.soso.evaextra.update.AppUpdater.AppUpdateInfo;
import com.soso.evaextra.util.CrashHandler;
import com.soso.evaextra.util.TimeUtil;
import com.soso.evaluateextra.R;

public class LocationTestActivity extends ActionBarActivity implements
		Observer, OnClickListener {
	private static final String TAG = "LocationTestActivity";
	private static final int MODE_LIST = 1;
	private static final int MODE_MAP = 2;

	private static Set<String> sCidSet;

	private AppStatus mAppStatus;
	private AppContext mAppContext;

	private boolean[] mChecked;

	private TextView mTvWifi;
	private TextView mTvGps;
	private TextView mTvCid;
	private TextView mTvSpeed;
	
	DecimalFormat df = new DecimalFormat("0.#");

	static {
		HashSet<String> set = new HashSet<String>();
		set.add("268435455");
		set.add("2147483647");
		set.add("50594049");
		set.add("65535");
		sCidSet = Collections.unmodifiableSet(set);
	}

	private final Handler mTimerHandler = new Handler() {
		public void handleMessage(final android.os.Message msg) {
			updateActionBarTitile();

			sendEmptyMessageDelayed(1, 1000);
		}
	};

	private boolean mBound = false;
	private LocationService mLocationService;
	private final ServiceConnection mServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocationServiceProxy proxy = (LocationServiceProxy) service;
			mLocationService = proxy.getService();
			mBound = true;

			postBound();
		}
	};
	private TelephonyManager mTelMgr;
	private PhoneStateListener mPhoneStateListener;
	private final BroadcastReceiver mWifiMonitor = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				updateWifiGpsCid();
			}
		}
	};

	private final ContentObserver mGpsMonitor = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			updateWifiGpsCid();
		}
	};
	/**
	 * 用于监听到boardcast时使用
	 */
	private MenuItem mRunorStopItem;
	public static final String RUN_STOP_ACTION = "com.soso.evaextra.RUN_STOP";
	private final BroadcastReceiver mRunOrStopToggleReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (RUN_STOP_ACTION.equals(action)) {
				runorStopSwitch(mRunorStopItem);
			}
		}
	};

	private final BroadcastReceiver mStopTestMonitor = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent != null
					&& StopTestMonitor.ACTION_STOP.equals(intent.getAction())) {
				new AlertDialog.Builder(LocationTestActivity.this)
						.setTitle("测试完成").setMessage("测试已完成，日志将自动上传")
						.setCancelable(false)
						.setPositiveButton("确定", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								LocationTestActivity.this.finish();
								NetMonitor
										.uploadIfNeed(LocationTestActivity.this);
							}
						}).show();
			}
		}
	};
	
	//时刻监听定位传送过来的广播，更改页面speed的数值
	private final BroadcastReceiver mgetTencentLocationReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (Proxy.SEND_TENCENT_LOCATION_SPEED.equals(action)) {
				Bundle bundle = intent.getBundleExtra("bundle");
				String result = bundle.getString("result");
				mTvSpeed.setText(result);
				if(result.equals("unknown")){
					mTvSpeed.setTextColor(Color.RED);
				}else{
					mTvSpeed.setTextColor(Color.GREEN);
				}
//				Result result = (Result) bundle.getSerializable("result");
//				//result.getProvider().equalsIgnoreCase("gps")?"G":"N"
//				if(result.getProvider().equalsIgnoreCase("gps")){
//					mTvSpeed.setText(df.format(result.getSpeed()));
//					mTvSpeed.setTextColor(Color.RED);
//				}else{
//					mTvSpeed.setText("-1");
//					mTvSpeed.setTextColor(Color.RED);
//				}
			}
			if(Proxy.SEND_TENCENT_LOCATION_GPS.equals(action)){
				Bundle bundle = intent.getBundleExtra("bundle");
				int status = bundle.getInt("status");
				switch (status) {
				case 0:
					mTvGps.setText("关闭");
					mTvGps.setTextColor(Color.RED);
					break;
				case 1:
					mTvGps.setText("打开");
					mTvGps.setTextColor(Color.GREEN);
					break;
				case 3:
					mTvGps.setText("可用");
					mTvGps.setTextColor(Color.GREEN);
					break;
				case 4:
					mTvGps.setText("不可用");
					mTvGps.setTextColor(Color.RED);
					break;
				default:
					break;
				}
			}
		}
	};
	
	

	private void postBound() {
		mLocationService.addObserver(this);
	}

	private void preUnbind() {
		mLocationService.deleteObserver(this);
	}

	@TargetApi(23)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=23){
			String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
			while(checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED){
				 requestPermissions(permissions, 0);
			}
		}
		
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		setContentView(R.layout.activity_location_test);

		mTelMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

		AppContext.shuffle();
		mAppContext = AppContext.getInstance(this);
		mAppStatus = mAppContext.getAppStatus();
		mChecked = AppConfig.getAppConfig(this).getCheckedSdk();

		mTvWifi = (TextView) findViewById(R.id.wifi_state);
		mTvGps = (TextView) findViewById(R.id.gps_state);
		mTvCid = (TextView) findViewById(R.id.cid);
		mTvSpeed = (TextView) findViewById(R.id.speed);

		updateActionBarTitile();
		getSupportActionBar().setDisplayShowHomeEnabled(false);

		if (null == savedInstanceState) {
			initFrags();
		}
		LocationService.start(this);
		new AppUpdaterTask().execute();

		PreferenceManager
				.setDefaultValues(this, R.xml.location_settigns, false);
		
	}

	private void initFrags() {
		boolean[] checked = AppConfig.getAppConfig(this).getCheckedSdk();

		int[] frags = AppContext.FRAG_IDS;
		String[] titles = AppContext.TITLES;
		String[] keys = AppContext.ALL_KEYS;

		FragmentManager fragmentManager = getSupportFragmentManager();
		// LocationStatFrag frag1 = (LocationStatFrag)
		// fragmentManager.findFragmentById(R.id.frag1);
		// LocationStatFrag frag2 = (LocationStatFrag)
		// fragmentManager.findFragmentById(R.id.frag2);
		// LocationStatFrag frag3 = (LocationStatFrag)
		// fragmentManager.findFragmentById(R.id.frag3);

		FragmentTransaction ft = fragmentManager.beginTransaction();

		for (int i = 0; i < checked.length; i++) {
			if (checked[i]) {
				ft.add(frags[i],
						LocationStatusFrag.newInstance(titles[i], keys[i]));
			}
		}
		ft.commit();
	}

	private void updateActionBarTitile() {
		final ActionBar actionBar = getSupportActionBar();
		
		AppStatus appStatus = mAppStatus;

		long totalDuration = appStatus.getTotalDuration();
		String surfix = "";
		if (totalDuration != 0) {
			surfix = "/" + TimeUtil.format(totalDuration);
		}

		if (appStatus.isLocationRunning()) {
			actionBar.setTitle(TimeUtil.format(appStatus.getLocationDuration())
					+ surfix);
		} else {
			actionBar.setTitle("00:00:00");
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!mBound) {
			LocationService.bind(this, mServiceConn);
		}
		updateTimerHandlerState(false);

		mTelMgr.listen(mPhoneStateListener = new PhoneStateListener() {
			@Override
			public void onCellLocationChanged(CellLocation location) {
				super.onCellLocationChanged(location);
				updateWifiGpsCid();
			}
		}, PhoneStateListener.LISTEN_CELL_LOCATION);

		IntentFilter intentFilter = new IntentFilter(
				WifiManager.WIFI_STATE_CHANGED_ACTION);
		registerReceiver(mWifiMonitor, intentFilter);

		IntentFilter intentFilter2 = new IntentFilter(
				StopTestMonitor.ACTION_STOP);
		registerReceiver(mStopTestMonitor, intentFilter2);

		IntentFilter intentFilter3 = new IntentFilter(RUN_STOP_ACTION);
		registerReceiver(mRunOrStopToggleReceiver, intentFilter3);
		
		//监听腾讯位置发送的广播
		IntentFilter filter = new IntentFilter(Proxy.SEND_TENCENT_LOCATION_SPEED);
		registerReceiver(mgetTencentLocationReceiver, filter);
		IntentFilter filter2 = new IntentFilter(Proxy.SEND_TENCENT_LOCATION_GPS);
		registerReceiver(mgetTencentLocationReceiver, filter2);

		getContentResolver()
				.registerContentObserver(
						Settings.Secure
								.getUriFor(Settings.System.LOCATION_PROVIDERS_ALLOWED),
						false, mGpsMonitor);

		updateWifiGpsCid();
	}

	private boolean checkNewCellProvider(){
		if(Build.VERSION.SDK_INT>=17){
			CellInfo cellInfo = getCellInfoQuietly(this);
			if(cellInfo != null)
				return true;
		}
		return false;
	}
	@SuppressLint("NewApi")
	private CellInfo getCellInfoQuietly(Context context) {
		TelephonyManager telManager = mTelMgr;
		if (telManager != null) {
			try {
				List<CellInfo> cList = telManager.getAllCellInfo();
				CellInfo cellInfo = null;
				if(cList != null){
					for(CellInfo c:cList){
						if(c.isRegistered()){
							cellInfo = c;
						}
					}
				}
				if(cellInfo == null & cList!=null &cList.size()>0)
					cellInfo = cList.get(0);
				return cellInfo;
			} catch (Exception e) {
				// print errors
			}
		}
		return null;
	}
	@SuppressLint("NewApi")
	private void updateWifiGpsCid() {
		TelephonyManager telMgr = mTelMgr;
		
		String cid = "--";
		if(checkNewCellProvider()){
			Log.e("Test", "newProvider");
			CellInfo info = getCellInfoQuietly(this);
			if(info !=null){
				if(info instanceof CellInfoGsm){
					CellIdentityGsm id = ((CellInfoGsm) info).getCellIdentity();
					cid = ""+id.getCid();
				}else if(info instanceof CellInfoCdma){
					CellIdentityCdma id = ((CellInfoCdma) info).getCellIdentity();
					cid = ""+id.getBasestationId();
				}else if(info instanceof CellInfoWcdma){
					CellIdentityWcdma id = ((CellInfoWcdma) info).getCellIdentity();
					cid = ""+id.getCid();
				}else if(info instanceof CellInfoLte){
					CellIdentityLte id = ((CellInfoLte) info).getCellIdentity();
					cid = ""+id.getCi();
				}
			}
		}else{
			CellLocation cellLocation = telMgr.getCellLocation();
			if (cellLocation != null
					&& telMgr.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
				cid = "" + ((GsmCellLocation) cellLocation).getCid();
			} else if (cellLocation != null
					&& telMgr.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
				cid = "" + ((CdmaCellLocation) cellLocation).getBaseStationId();
			}
		}
//		mTvCid.setText(String.format(getString(R.string.ph_cid), cid));
		mTvCid.setText(cid);

		if (sCidSet.contains(cid)) {
			mTvCid.setTextColor(Color.RED);
		} else {
			mTvCid.setTextColor(Color.BLACK);
		}

		WifiManager wifiMgr = (WifiManager) getSystemService(WIFI_SERVICE);
		String wifiState = wifiMgr.isWifiEnabled() ? "开启" : "关闭";
//		mTvWifi.setText(String.format(getString(R.string.ph_wifi_state),
//				wifiState));
		mTvWifi.setText(wifiState);
		//通过检测的wifi状态来显示不同的颜色
		if(wifiMgr.isWifiEnabled()){
			mTvWifi.setTextColor(Color.GREEN);
		}else{
			mTvWifi.setTextColor(Color.RED);
		}

		LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
		String gpsState = locMgr
				.isProviderEnabled(LocationManager.GPS_PROVIDER) ? "开启" : "关闭";
//		mTvGps.setText(String
//				.format(getString(R.string.ph_gps_state), gpsState));
		mTvGps.setText(gpsState);
		//通过检测的gps状态来显示不同的颜色
		if(locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			mTvGps.setTextColor(Color.GREEN);
		}else{
			mTvGps.setTextColor(Color.RED);
		}
		
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(mgetTencentLocationReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "this.unDestroy() ");
		if (mBound) {
			preUnbind();
			LocationService.unbind(this, mServiceConn);
			mBound = false;
		}
		updateTimerHandlerState(true);

		mTelMgr.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		unregisterReceiver(mWifiMonitor);
		unregisterReceiver(mStopTestMonitor);
		getContentResolver().unregisterContentObserver(mGpsMonitor);
		unregisterReceiver(mRunOrStopToggleReceiver);
		
		if (!mAppStatus.isLocationRunning()) {
			stopService(new Intent(this, LocationService.class));
		} else {
			WidgetUtils.toast(this, "应用已进入后台运行");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(UI.ACTION_MENU_RES_ID, menu);

		MenuItem item = menu.findItem(R.id.action_runorstop);
		// add by kejiwang 2014年11月14日10:04:27
		mRunorStopItem = item;
		// 2014年11月14日10:04:35
		updateRunMenu(item);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (DialogInterface.BUTTON_POSITIVE == which) {
			initFrags2();
		}
	}

	private void initFrags2() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();

		boolean[] checked = mChecked; // appConfig.getCheckedSdk();
		int[] frags = AppContext.FRAG_IDS;
		String[] titles = AppContext.TITLES;
		String[] keys = AppContext.ALL_KEYS;

		for (int i = 0; i < checked.length; i++) {
			Fragment f = fragmentManager.findFragmentById(frags[i]);
			if (checked[i] && f == null) {
				ft.replace(frags[i],
						LocationStatusFrag.newInstance(titles[i], keys[i]));
			} else if (!checked[i] && f != null) {
				ft.remove(f);
			}
		}
		ft.commit();

		AppConfig.getAppConfig(this).setCheckedSdk(mChecked);
	}

	private void updateRunMenu(MenuItem item) {
		if (mAppStatus.isLocationRunning()) {
			item.setIcon(R.drawable.ic_action_stop);
		} else {
			item.setIcon(R.drawable.ic_action_run);
		}
	}

	private void showConfigDialog(MenuItem menuItem) {
		LocationTestConfigFrag frag = LocationTestConfigFrag
				.newInstance(menuItem);
		frag.show(getSupportFragmentManager(), "dialog");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.action_settings:
			LocationSettingsActivity.start(this);
			return true;

		case R.id.action_tomapview:
			LocationMapActivity.startMe(this, AppContext.TENCENT);
			return true;
		case R.id.action_runorstop:
			runorStopSwitch(item);
			return true;
		case R.id.action_choosesdk:
			if (!mAppStatus.isLocationRunning()) {
				chooseSdk();
			} else {
				WidgetUtils.toast(this, "重新设置sdk前请停止测试");
			}
			return true;

			// case R.id.action_viewhistory:
			// viewHistory();
			// return true;
			//
			// case R.id.action_settings:
			// paramSettings();
			// return true;

			// case R.id.action_screenshot:
			// View toppest = ((ViewGroup)
			// getWindow().getDecorView().findViewById(android.R.id.content)).getChildAt(0);
			// toppest.setDrawingCacheEnabled(true);
			// Bitmap bmap = toppest.getDrawingCache();
			// //Utils.saveBitmapOnSdcard(bmap);
			//
			// ByteArrayOutputStream out = new ByteArrayOutputStream();
			// bmap.compress(CompressFormat.JPEG, 100, out);
			// Files.write(out, new
			// File(Environment.getExternalStorageDirectory(), name))
			// toppest.setDrawingCacheEnabled(false);
			// return true;

		case R.id.action_viewlog:
			if (!mAppStatus.isLocationRunning()) {
				LogListActivity.startMe(this);
			} else {
				WidgetUtils.toast(this, "查看log前请停止测试");
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void runorStopSwitch(final MenuItem item) {
		if (mAppStatus.isLocationRunning()) {
			runOrStop(item);
		} else {
			WifiManager wifiManager = (WifiManager) this
					.getSystemService(this.WIFI_SERVICE);
			//wifiManager.setWifiEnabled(true);
			LocationManager locMgr2 = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (Build.VERSION.SDK_INT>=23 && !locMgr2.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

				new AlertDialog.Builder(this).setTitle("提示")
						.setMessage("系统版本为6.0以上，请先打开位置服务开关并选择节电模式")
						.setPositiveButton("使用节电模式",  new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
						        startActivityForResult(intent, 0);
						        //onActivityResult(arg0, arg1, arg2);
								
						        if (Auto.AUTO_EXIT) {
									showConfigDialog(item);
								} else {
									runOrStop(item);
								}
							}
							
						})
						.setNegativeButton("就不打开", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (Auto.AUTO_EXIT) {
									showConfigDialog(item);
								} else {
									runOrStop(item);
								}
							}
						}).show();
				return;
			}
			
			if (Auto.AUTO_EXIT) {
				showConfigDialog(item);
			} else {
				runOrStop(item);
			}

		}
	}

	void runOrStop(MenuItem item) {
		if (mBound) {
			AppStatus appStatus = mAppStatus;
			boolean start = appStatus.isLocationRunning();

			if (start) {
				mLocationService.stopLocation();
				AppContext.clear(mAppContext);
				WidgetUtils.toast(this, "定位对比测试已停止");
				NetMonitor.uploadIfNeed(this);

				// 用户主动停止
				LocationTestConfigFrag.cancelScheduleStop(this);
			} else {
				if (!AppConfig.getAppConfig(this).anySdkChecked()) {
					WidgetUtils.toast(this, "请选择SDK");
					return;
				}

				final int uid = Process.myUid();
				mAppStatus.setTrafficBase(TrafficStats.getUidRxBytes(uid)
						+ TrafficStats.getUidTxBytes(uid));

				// mLocationService.startLocation(null);
				mLocationService.startLocation(AppConfig.getAppConfig(this)
						.getCheckedSdk());
				appStatus.setLocationRunning(true);
				appStatus.setLocationStart();
				WidgetUtils.toast(this, "定位对比测试开始");

				updateWifiGpsCid();
			}

			updateTimerHandlerState(false);
			updateActionBarTitile();
			updateFrags();
			updateRunMenu(item);
		}
	}

	/**
	 * 
	 * @param forceClear
	 *            true 强行停止 handler
	 */
	private void updateTimerHandlerState(boolean forceClear) {
		boolean stop = !mAppStatus.isLocationRunning();
		if (stop || forceClear) {
			mTimerHandler.removeMessages(1);
		} else {
			mTimerHandler.obtainMessage(1).sendToTarget();
		}
	}

	private void chooseSdk() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("选择sdk");

		final boolean[] checked = AppConfig.getAppConfig(this).getCheckedSdk();

		builder.setMultiChoiceItems(R.array.sdks, checked,
				new OnMultiChoiceClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						checked[which] = isChecked;
						mChecked = checked;
					}
				});
		builder.setNegativeButton("取消", this);
		builder.setPositiveButton("确定", this);
		builder.show();
	}

	private void viewHistory() {

	}

	private void paramSettings() {

	}

//	private static Fragment create(int mode) {
//		if (mode == MODE_LIST) {
//			return new LocationListFrag();
//		} else if (mode == MODE_MAP) {
//			return new LocationMapFrag();
//		}
//		return null;
//	}

	@Override
	public void update(Observable observable, Object data) {
		Log.i("tag", "LocationTestActivity.update(): ");
		// Toast.makeText(this, "c", Toast.LENGTH_SHORT).show();
		updateFrags();
	}

	private void updateFrags() {
		FragmentManager fragmentManager = getSupportFragmentManager();

		final LocationStatusFrag mFrag1 = (LocationStatusFrag) fragmentManager
				.findFragmentById(R.id.frag1);
		final LocationStatusFrag mFrag2 = (LocationStatusFrag) fragmentManager
				.findFragmentById(R.id.frag2);
		final LocationStatusFrag mFrag3 = (LocationStatusFrag) fragmentManager
				.findFragmentById(R.id.frag3);
		final LocationStatusFrag mFrag4 = (LocationStatusFrag) fragmentManager
				.findFragmentById(R.id.frag4);

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mFrag1 != null) {
					mFrag1.update();
				}
				if (mFrag2 != null) {
					mFrag2.update();
				}
				if (mFrag3 != null) {
					mFrag3.update();
				}
				if (mFrag4 != null) {
					mFrag4.update();
				}
			}
		});
	}

	class AppUpdaterTask extends AsyncTask<Void, Void, AppUpdateInfo> {

		private final AppUpdater mAppUpdater;
		private AppUpdateInfo mUpdateInfo;
		private final OnClickListener mListener = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (which == DialogInterface.BUTTON_POSITIVE) {
					mAppUpdater
							.download(LocationTestActivity.this, mUpdateInfo);
				}
			}
		};

		AppUpdaterTask() {
			super();
			mAppUpdater = new AppUpdater();
		}

		@Override
		protected AppUpdateInfo doInBackground(Void... params) {
			return mAppUpdater.check();
		}

		@Override
		protected void onPostExecute(AppUpdateInfo result) {
			super.onPostExecute(result);

			if (result != AppUpdateInfo.NULL && !isCancelled()) {
				mUpdateInfo = result;
				if (mUpdateInfo.getCode() <= mAppContext.getVersionCode()) {
					return;
				}

				// 弹出 alert
				new AlertDialog.Builder(LocationTestActivity.this)
						.setTitle("更新").setMessage(result.toString())
						.setPositiveButton("下载", mListener)
						.setNegativeButton("取消", mListener).show();
			}
		}
	}
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         