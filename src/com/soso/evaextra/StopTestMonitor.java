package com.soso.evaextra;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.soso.evaluateextra.R;

public class StopTestMonitor extends BroadcastReceiver {

	private static final int MY_REQ_CODE = 1000;

	private static final int DONE_NOTI_ID = 121314;

	/**
	 * 停止定位
	 */
	public static final String ACTION_STOP = "com.soso.evaextra.ACTION_STOP";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent != null && ACTION_STOP.equals(intent.getAction())) {
			Log.i("StopTestMonitor", "onReceive");
			Intent service = new Intent(context, LocationService.class);
			service.setAction(LocationService.ACTION_STOP_LOCATION);
			context.startService(service);
			NetMonitor.uploadIfNeed(context);
			doneNoti(context);
		}
	}

	private void doneNoti(Context context) {
		CharSequence appName = context.getText(R.string.app_name);
		Notification noti = new NotificationCompat.Builder(context)
				.setContentTitle(appName).setContentText(appName + "已完成测试")
				.setSmallIcon(R.drawable.ic_stat_done).setAutoCancel(true)
				.setContentIntent(getPi(context)).build();

		NotificationManager notiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.notify(DONE_NOTI_ID, noti);
	}

	private PendingIntent getPi(Context context) {
		Intent intent = new Intent(context, LocationTestActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pi = PendingIntent.getActivity(context, MY_REQ_CODE,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pi;
	}
}
