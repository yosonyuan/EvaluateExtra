package com.soso.evaextra.update;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.soso.evaextra.AppContext;
import com.soso.evaluateextra.R;

public class DownloadWatcher extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		long myId = AppContext.APP_CONTEXT.getAppStatus().getDownloadId();

		if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
			showDownloadManager(context);
		} else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			long curId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,
					0);

			if (curId == myId) {
				installIfSuccessful(context, curId);
			}
		}
	}

	private void installIfSuccessful(Context context, long curId) {
		DownloadManager dm = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Query query = new Query().setFilterById(curId);
		Cursor cursor = dm.query(query);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int statusIdx = cursor
						.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int uriIdx = cursor
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
				if (DownloadManager.STATUS_SUCCESSFUL == cursor
						.getInt(statusIdx)) {
					// install(context, Uri.parse(cursor.getString(uriIdx)));
					showDownloadedNoti(context,
							Uri.parse(cursor.getString(uriIdx)));
				} else {
					// System.out.println("download failed");
				}
			}
			cursor.close();
		}
	}

	private void showDownloadManager(Context context) {
		Intent intent = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	private void showDownloadedNoti(Context context, Uri uri) {
		Toast.makeText(context, "Evaextra 下载到 " + uri.getPath(),
				Toast.LENGTH_SHORT).show();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context);
		builder.setContentTitle("下载Evaextra成功").setContentText("点击安装")
				.setSmallIcon(R.drawable.ic_launcher);

		PendingIntent pi = PendingIntent.getActivity(context, 1111,
				getInstallIntent(context, uri),
				PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pi);

		NotificationManager notiManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notiManager.notify(11111, builder.build());
	}

	private Intent getInstallIntent(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, "application/vnd.android.package-archive");
		return intent;
	}
}
