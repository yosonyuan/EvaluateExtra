package com.soso.evaextra;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.soso.evaextra.SimpleDb.LogEntry;
import com.soso.evaextra.SimpleDb.SimpleDbUtil;
import com.soso.evaextra.config.Auto;
import com.soso.evaextra.update.AppUpdater;
import com.soso.evaextra.update.AppUpdater.AppUpdateInfo;

public class AutoUploadService extends IntentService {
	private static final String TAG = "AutoUploadService";

	public static final String ACTION_AUTO_UPLOAD = "com.soso.evaextra.ACTION_UPLOAD_LOG";

	private int skipCounter=0;
	public AutoUploadService() {
		super("auto_upload");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (ACTION_AUTO_UPLOAD.equals(action)) {
			SimpleDb db = new SimpleDb(this);
			List<LogEntry> entries = SimpleDbUtil.findUnuploaded(db);
			Log.i(TAG, "start upload after 5s, entry size is " + entries.size());
			try {
				TimeUnit.SECONDS.sleep(5); // 避免网络没有完全连通
			} catch (InterruptedException e) {
				// ignore
			}
			skipCounter=0;
			for (LogEntry entry : entries) {
				if (upload(entry)) {
					entry.uploaded = true;
					SimpleDbUtil.update(db, entry);
				}
			}
			if (skipCounter>0) {
				Log.w(TAG, "skip "+skipCounter+" uploaded logs and short logs");
			}
			Log.i(TAG, "end upload");
			db.close();
		} else if ("com.soso.evaextra.ACTION_CHECK_UPDATE".equals(action)) {
			AppContext appContext = AppContext.getInstance(this);
			AppUpdater updater = new AppUpdater();
			AppUpdateInfo info = updater.check();
			if (info.getCode() > appContext.getVersionCode()) {
				newPkgFoundNoti();
			}
		}
	}

	private void newPkgFoundNoti() {
		// TODO
	}

	private boolean upload(LogEntry entry) {
		if (entry.uploaded || entry.log_duration < Auto.SHORT_LOG_LIMIT) {
			//Log.w(TAG, "skip uploaded logs and short logs");
			skipCounter++;
			return false;
		}
		if (TextUtils.isEmpty(entry.log_path)) {
			Log.w(TAG, "skip non-exist logs");
			return false;
		}
		File file = new File(entry.log_path);
		if (file.exists() && (file.length() > 1024 * 20 )) {
			Log.i(TAG, "uploading " + file.getAbsolutePath());
			try {
				LogListActivity.upload(file);
				Log.i(TAG, file.getAbsolutePath() + " uploaded!");
				return true;
			} catch (IOException e) {
				Log.e(TAG, "failed to upload " + file.getAbsolutePath(), e);
			}
		}

		return false;
	}
}
