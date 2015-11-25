package com.soso.evaextra;

import java.util.Observable;
import java.util.Observer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import cn.edu.hust.cm.common.app.WidgetUtils;

import com.soso.evaluateextra.R;

public class LocationService extends Service {
	private static final String TAG = "LocationService";

	/**
	 * 停止定位并清理之前的定位状态参数
	 */
	public static final String ACTION_STOP_LOCATION = "com.soso.evaextra.ACTION_STOP_LOCATION";

	final ObservableExt mObservable;
	private Proxy mProxy;
	private WakeLock mWakeLock;

	public LocationService() {
		super();
		mObservable = new ObservableExt();
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "LocationService.onBind(): ");
		return new LocationServiceProxy();
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "LocationService.onCreate(): ");
		super.onCreate();

		mProxy = new Proxy(this);
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
				"location_in_bg");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (ACTION_STOP_LOCATION.equals(action)) {
				stopLocation();

				AppContext appContext = AppContext.getInstance(this);
				AppContext.clear(appContext);
				WidgetUtils.toast(this, "定位对比测试已停止");
				
				stopSelf(startId);
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 防止休眠锁未释放
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	public void startLocation(boolean[] checked) {
		startForeground();

		mWakeLock.acquire();
		mProxy.startLocation(checked);
	}

	public void stopLocation() {
		stopForeground();
		mProxy.stopLocation();
		if (mWakeLock.isHeld()) {
			mWakeLock.release();
		}
	}

	// ///////// public methods
	public void addObserver(Observer observer) {
		mObservable.addObserver(observer);
	}

	public void deleteObserver(Observer observer) {
		mObservable.deleteObserver(observer);
	}

	public void startForeground() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);

		Intent intent = new Intent(this, LocationTestActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(this, 999, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		builder.setOngoing(true);

		CharSequence appName = getText(R.string.app_name);
		builder.setContentTitle(appName);
		builder.setContentText(appName + "正在运行");
		builder.setSmallIcon(R.drawable.ic_stat_ongoing);
		builder.setContentIntent(pi);
		builder.setTicker(appName + "已启动");
		builder.setWhen(System.currentTimeMillis());

		Notification noti = builder.build();
		startForeground(10000, noti);
	}

	public void stopForeground() {
		stopForeground(true);
	}

	// ///////// util methods
	public static void start(Context context) {
		context.startService(new Intent(context, LocationService.class));
	}

	public static void bind(Context context, ServiceConnection conn) {
		context.bindService(new Intent(context, LocationService.class), conn,
				Context.BIND_AUTO_CREATE);
	}

	public static void startAndBind(Context context, ServiceConnection conn) {
		Log.i(TAG, "LocationService.startAndBind(): ");
		start(context);
		bind(context, conn);
	}

	public static void unbind(Context context, ServiceConnection conn) {
		Log.i(TAG, "LocationService.unbind(): ");
		context.unbindService(conn);
	}

	public static void unbindAndStop(Context context, ServiceConnection conn) {
		Log.i(TAG, "LocationService.unbindAndStop(): ");
		context.unbindService(conn);
		context.stopService(new Intent(context, LocationService.class));
	}

	public class LocationServiceProxy extends Binder {

		public LocationService getService() {
			return LocationService.this;
		}
	}

	static class ObservableExt extends Observable {
		@Override
		public void setChanged() {
			super.setChanged();
		}
	}

}
