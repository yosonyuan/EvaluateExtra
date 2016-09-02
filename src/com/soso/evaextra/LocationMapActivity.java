package com.soso.evaextra;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import cn.edu.hust.cm.common.app.WidgetUtils;

import com.soso.evaextra.model.Result;
import com.soso.evaextra.util.TimeUtil;
import com.soso.evaluateextra.R;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.TencentMap.CancelableCallback;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

public class LocationMapActivity extends ActionBarActivity implements android.view.View.OnClickListener, TencentMap.OnMapLoadedCallback{
	public static final String RECREATE_FROM_LOG = "com.soso.evaextra.RECREATE_FROM_LOG";
	private static HashMap<String, String> KEY_TITLE = new HashMap<String, String>();
	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd",
			Locale.ENGLISH);

	static {
		for (int i = 0; i < AppContext.ALL_KEYS.length; i++) {
			KEY_TITLE.put(AppContext.ALL_KEYS[i], AppContext.TITLES[i]);
		}
	}
	
	
	private boolean flag;//用来监听停止时的广播，监听到后即变为false,就不再去监听

	private AppStatus mAppStatus;
	private AppContext mAppContext;
	private boolean mCreatedFromLog;
	
	private TencentMap tencentMap;
	private String mKey;
	private LocationOverlay mLocationOverlay;
	private Canvas canvas;

	public static void startMe(Context context, String key) {
		Intent intent = new Intent(context, LocationMapActivity.class);
		intent.putExtra("com.soso.evaextra.KEY", key);
		context.startActivity(intent);
	}
	public static void startMe(Context context, String key, Bundle data) {
		Intent intent = new Intent(context, LocationMapActivity.class);
		intent.putExtra("com.soso.evaextra.KEY", key);
		intent.putExtras(data);
		context.startActivity(intent);
	}
	
	private final Handler mUploadHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			int code = msg.what;
			switch (code) {
			case 0:
				Toast.makeText(LocationMapActivity.this, "上传文件成功", 1).show();
				break;
			case 1:
				Toast.makeText(LocationMapActivity.this, "上传文件", 1).show();
				break;
			default:
				break;
			}
		}
	};

	private final Handler mTimerHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			updateActionBarTitile();
			sendEmptyMessageDelayed(1, 1000);
		}
	};

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

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
//		setContentView(R.layout.activity_location_map);
		setContentView(R.layout.showmap);

		mAppContext = AppContext.getInstance(this);
		mAppStatus = mAppContext.getAppStatus();

		FragmentManager fragmentManager = getSupportFragmentManager();

		Intent intent = getIntent();
		mKey = intent.getStringExtra("com.soso.evaextra.KEY");
		mCreatedFromLog = intent.getBooleanExtra(RECREATE_FROM_LOG, false);
		
		SupportMapFragment fragment = (SupportMapFragment) fragmentManager.findFragmentById(R.id.mapview_fg);
		tencentMap = fragment.getMap();
		tencentMap.setOnMapLoadedCallback(this);

		mAppConfig = AppConfig.getAppConfig(this);
		mShowLine = mAppConfig.isShowLine();
		mShowCountIndex = mAppConfig.getShowCountIndex();

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setTitle(KEY_TITLE.get(mKey) + "定位轨迹");
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		updateActionBarTitile();
		
		//监听腾讯位置发送的广播
		IntentFilter filter = new IntentFilter(Proxy.SEND_TENCENT_LOCATION_DATA);
		mAppContext.registerReceiver(mgetTencentLocationReceiver, filter);
//		//监听结束时的广播
//		if(flag){
//			IntentFilter stopfilter = new IntentFilter(Proxy.STOP_LOCATION);
//			mAppContext.registerReceiver(mCallbackLocation, stopfilter);
//		}
		
		View v = findViewById(R.id.location);
		View up = findViewById(R.id.move_up);
		View right = findViewById(R.id.move_right);
		View down = findViewById(R.id.move_down);
		View left = findViewById(R.id.move_left);
		
		v.setOnClickListener(this);
		up.setOnClickListener(this);
		right.setOnClickListener(this);
		down.setOnClickListener(this);
		left.setOnClickListener(this);
		
		
		Bitmap bmpMarker = BitmapFactory.decodeResource(getResources(),
				android.R.drawable.presence_away);
		Bitmap amap = BitmapFactory.decodeResource(getResources(),
				android.R.drawable.presence_busy);
		Bitmap bd = BitmapFactory.decodeResource(getResources(),
				android.R.drawable.presence_online);
		Bitmap sg = BitmapFactory.decodeResource(getResources(),
				android.R.drawable.presence_invisible);

		mLocationOverlay = new LocationOverlay(
				AppContext.getInstance(LocationMapActivity.this), bmpMarker, amap, bd, sg,
				mKey);
		
		tencentMap.getCameraPosition().builder().zoom(mAppStatus.getMapLevel());
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		updateTimerHandlerState(false);
		IntentFilter stopfilter = new IntentFilter(Proxy.STOP_LOCATION);
		mAppContext.registerReceiver(mCallbackLocation, stopfilter);

		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		//在停止的时候就移除监听
		try {
			mAppContext.unregisterReceiver(mCallbackLocation);
		} catch (Exception e) {
			// TODO: 不做任何处理
		}
		updateTimerHandlerState(true);
		super.onStop();
		
		mAppStatus.setMapLevel((int)(tencentMap.getCameraPosition().zoom));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCreatedFromLog) {
			AppContext.getInstance(this).removeLocations(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.location_map_activity_actions, menu);
		updateRunMenu(menu.getItem(0));
		return super.onCreateOptionsMenu(menu);
	}
	
	private void updateRunMenu(MenuItem item) {
		if (mAppStatus.isLocationRunning()) {
			item.setIcon(R.drawable.ic_action_stop);
		} else {
			item.setIcon(R.drawable.ic_action_run);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_runorstopatmap://定位开始\停止 
			
			LocationManager locMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
//			if (!locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//				WidgetUtils.toast(this, "测试前清先打开GPS");
//			} else {
//				if (!mAppStatus.isLocationRunning()) {
//					updateTimerHandlerState(false);
//					updateActionBarTitile();
//					mTimerHandler.sendEmptyMessageDelayed(1, 1000);
//					item.setIcon(R.drawable.ic_action_stop);
//				} else {
//					item.setIcon(R.drawable.ic_action_run);
//				}
//				Intent it = new Intent();
//				it.setAction(LocationTestActivity.RUN_STOP_ACTION);
//				sendBroadcast(it);
//			}
			
			if (!mAppStatus.isLocationRunning()) {
				updateTimerHandlerState(false);
				updateActionBarTitile();
				mTimerHandler.sendEmptyMessageDelayed(1, 1000);
				item.setIcon(R.drawable.ic_action_stop);
				//android中的dialog为异步操作，因此广播放在这里不能保证执行的先后顺序，所以需要放入到点击事件里面
				Intent it = new Intent();
				it.setAction(LocationTestActivity.RUN_STOP_ACTION);
				sendBroadcast(it);
			} else {
				setLocation(1);
//				SharedPreferences sp = getSharedPreferences("filename", Context.MODE_PRIVATE);
//				final String filename = sp.getString("name", "文件不存在");
//				
//				File appPath = new File(Environment.getExternalStorageDirectory(),"SingleTest");
//				File subdir = new File(appPath, SDF.format(new Date()));
//				final File childFile = new File(subdir.getAbsolutePath(), filename +  "_singletestshow_all");
//				
//				//增加一个提示框，确认即认为当前的数据是有效的，否则认为无效，并删除当前的日志文件
//				AlertDialog alertDialog = new AlertDialog.Builder(this)
//				.setMessage("是否确认这次记录的位置是有效的？")
//				.setPositiveButton("确定", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						//点击确定的时候即认为当前文件有效，会记录下文件，并上传文件内容到服务器
//						if(childFile.exists() && childFile.length() > 0){
//							//将产生singletest文件上传到服务器端
//							new Thread(){
//								@Override
//								public void run() {
//									try {
//										String data = ReadSingleTestFile.parseSingleTestFile(childFile);
//										String result = ConnectToServlet.postToServlet(data);
////										mUploadHandler.obtainMessage(0).sendToTarget();
//									} catch (Exception e) {
//										mUploadHandler.obtainMessage(1).sendToTarget();
//									}
//								}
//							}.start();
//						}
//						}
//					}).setNegativeButton("取消", new OnClickListener() {
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						//点击取消的时候就认为是无效的，并要删除当前记录的文件
//						if(childFile.exists()){
//							boolean isdelete = childFile.delete();
//						}
//					}
//				}).create();
//				Window window = alertDialog.getWindow();
//				WindowManager.LayoutParams lp = window.getAttributes();
//				lp.alpha = 0.9f;
//				window.setGravity(Gravity.TOP);
//				window.setAttributes(lp);
//				alertDialog.show();
//				//将SharedPreferences中的数据删除掉
//				SharedPreferences.Editor editor =  sp.edit();
//				editor.remove("name");
//				editor.commit();
				
				item.setIcon(R.drawable.ic_action_run);
			}
			
			return true;
		case R.id.action_showline:
			setShowLine();
			return true;
		case R.id.action_count:
			setCount();
			return true;
		case R.id.action_showall:
			if (!isShowAll()) {
				WidgetUtils.toast(this, "显示所有SDK的位置信息");
			}

			setShowAll(!isShowAll());
			return true;
		case R.id.action_setLocation://设置当前坐标
			setLocation(2);
			return true;
		case R.id.action_revert://返回
			this.finish();
			return true;
		case android.R.id.home:
			this.finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//当点击开始时，腾讯定位会定位出一个位置，然后将这个位置赋值给十字中心图标的位置
	private final BroadcastReceiver mgetTencentLocationReceiver = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (Proxy.SEND_TENCENT_LOCATION_DATA.equals(action)) {
				Bundle bundle = intent.getBundleExtra("bundleData");
				Result result = (Result) bundle.getSerializable("result");
				tencentMap.animateCamera(CameraUpdateFactory.newLatLng(result.toGeoPoint()));
				
				//设置完数据之后直接取消监听，只监听一次即可
//				mAppContext.unregisterReceiver(mgetTencentLocationReceiver);
			}
		}
	};
	
	//监听停止定位按钮的广播，如果接收到，则回传广播到PROXY类中，并记录最后位置的点
	private final BroadcastReceiver mCallbackLocation = new BroadcastReceiver() {
		public void onReceive(android.content.Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			String action = intent.getAction();
			if (Proxy.STOP_LOCATION.equals(action)) {
				boolean isStop = intent.getBooleanExtra("stop", false);
				if(isStop){
					LatLng gp = tencentMap.getCameraPosition().target;	
					Intent it = new Intent();
					it.setAction(Proxy.SET_LOCATION_ACTION);
					it.putExtra("latitude", gp.latitude);
					it.putExtra("longtitude", gp.longitude);
		            sendBroadcast(it);
		            flag = false;//将监听状态变为false,不再去监听
				}
			}
		}
	};
	
	private void setLocation(final int i) {
		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setMessage("设置十字中心为当前准确位置？")
				.setPositiveButton("确定", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LatLng gp = tencentMap.getCameraPosition().target;								
						Intent it = new Intent();
						it.setAction(Proxy.SET_LOCATION_ACTION);
						it.putExtra("latitude", gp.latitude);
						it.putExtra("longtitude", gp.longitude);
			            sendBroadcast(it);     
			            if(i== 1){
							Intent it2 = new Intent();
							it2.setAction(LocationTestActivity.RUN_STOP_ACTION);
							sendBroadcast(it2);
			            }
						}
				}).setNegativeButton("取消", null).create();
		Window window = alertDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.9f;
		window.setGravity(Gravity.TOP);
		window.setAttributes(lp);
		alertDialog.show();
	}
	
	
	private AppConfig mAppConfig;
	private boolean mShowLine;
	private int mShowCountIndex;

	private void setShowLine() {
		if (mAppConfig.isShowLine()) {//toast形式
			setShowLine(false);
			mAppConfig.setShowLine(false);
			WidgetUtils.toast(this, "不显示定位点连线");
		}
		else {
			setShowLine(true);
			mAppConfig.setShowLine(true);
			WidgetUtils.toast(this, "显示定位点连线");
		}
		
	}

	private void setCount() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置点数");

		builder.setSingleChoiceItems(AppContext.POINT_COUNT_STR,
				mShowCountIndex, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mShowCountIndex = which;
						// 更新 ui
						int[] c = AppContext.POINT_COUNT; // { 1, 5, 10, 20, 30,
															// Integer.MAX_VALUE
															// };
						setCount(c[which]);
						// 保存
						mAppConfig.setShowCountIndex(which);

						dialog.dismiss();
					}
				});
		builder.show();
	}
	
	
	
	
	//**************************************移植LocationMapfrag类的代码到这里************************************************
	
		@Override
		protected void onResume() {
			super.onResume();
			mOverlayHandler.sendEmptyMessage(0);
		}
		
		@Override
		protected void onPause() {
			super.onPause();
			mOverlayHandler.removeMessages(0);
		}
		
		
		public void setShowAll(boolean show) {
			if (mLocationOverlay != null) {
				mLocationOverlay.setShowAll(show);
//				mapview_edit.invalidate();
			}
		}

		public boolean isShowAll() {
			if (mLocationOverlay != null) {
				return mLocationOverlay.isShowAll();
			}
			return false;
		}

		public void setShowLine(boolean show) {
			if (mLocationOverlay != null) {
				// mLocationOverlay.setShowLine(show);
//				mapview_edit.invalidate();
			}
			
		}

		public void setCount(int count) {
			if (mLocationOverlay != null) {
//				 mLocationOverlay.setCount(count);
//				mapview_edit.invalidate();
			}
		}
		
		
		
		private final Handler mOverlayHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
					tencentMap.clear();
					updataMapView(canvas,tencentMap);
					sendEmptyMessageDelayed(0, 5000);
			}
		};
		
		private void updataMapView(Canvas canvas , TencentMap tencentMap) {
			mLocationOverlay.draw(canvas, tencentMap);
		}
		
		@Override
		public void onClick(View v) {
			int value = v.getId();
			switch (value) {
			case R.id.location:
				moveToCenter(true);
				break;

			case R.id.move_up:
				moveToUp();
				break;
			
			case R.id.move_right:
				moveToRight();
				break;
		
			case R.id.move_down:
				moveToDown();
				break;

			case R.id.move_left:
				moveToLeft();
				break;
			}
		}
		
		private void moveToCenter(boolean animated) {
			List<Result> results = mAppContext.getLocations(mKey, 1);
			if (results.size() > 0) {
				Result r = results.get(0);
				if (animated) {
					tencentMap.animateCamera(CameraUpdateFactory.newLatLng(r.toGeoPoint()));
				} else {
//					mapview_edit.getController().setCenter(r.toGeoPoint());
					tencentMap.animateCamera(CameraUpdateFactory.newLatLng(r.toGeoPoint()));
				}
			}
		}
		
		/**
		 * 因为十字中心点永远在屏幕的中央，所以我们只需要拿到中央点的位置，然后进行右移即可
		 */
		private void moveToRight(){
			tencentMap.animateCamera(CameraUpdateFactory.scrollBy(-20, 0)) ;
		}
		
		private void moveToLeft(){
			tencentMap.animateCamera(CameraUpdateFactory.scrollBy(10, 0)) ;
		}
		
		private void moveToUp(){
			tencentMap.animateCamera(CameraUpdateFactory.scrollBy(0, 20)) ;
		}
		
		private void moveToDown(){
			tencentMap.animateCamera(CameraUpdateFactory.scrollBy(0, -10)) ;
		}
		
		@Override
		public void onMapLoaded() {
//			LatLng latlng = tencentMap.getCameraPosition().target;
			List<Result> result = mAppContext.getLocations(mKey, 1);
			
			System.out.println("result ===" + result.toString());
			
			if(result == null || result.equals("") || result.toString().equals("[]")){
				LatLng ll = new LatLng(39.908534 , 116.397510);
				tencentMap.animateCamera(
						CameraUpdateFactory.newLatLngZoom(ll  , 15), 1, new CancelableCallback() {
					@Override
					public void onFinish() {
					}
					@Override
					public void onCancel() {
					}
				});
			}else{
				Result loc = result.get(0);
				LatLng ll = new LatLng(loc.latitude, loc.longitude);
				tencentMap.animateCamera(
						CameraUpdateFactory.newLatLngZoom(ll  , 15), 1, new CancelableCallback() {
					@Override
					public void onFinish() {
					}
					@Override
					public void onCancel() {
					}
				});
			}
		}
	
}
