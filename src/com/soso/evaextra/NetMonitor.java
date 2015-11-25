package com.soso.evaextra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.soso.evaextra.config.Auto;

/**
 * 监听网络状态, 若当前为wifi则自动上传log
 * 
 * @author kingcmchen
 * 
 */
public class NetMonitor extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("NetMonitor", "onReceive");
		Log.i("NetMonitor", "如果这句话经常出现是因为wifi不稳定经常在各ap间跳转");
		if (intent == null) {
			return;
		}

		AppContext appContext = (AppContext) context.getApplicationContext();
		AppStatus appStatus = appContext.getAppStatus();
		if (appStatus.isLocationRunning()) {
			Log.w("NetMonitor", "location is running");
			return;
		}

		String bssid = intent.getStringExtra(WifiManager.EXTRA_BSSID);
		if (bssid != null) {
			System.out.println("connected to " + bssid);
			uploadIfNeed(context);
		}
	}

	private static boolean isWifiConnected(Context context) {
		try {
			ConnectivityManager connMgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiInfo = connMgr
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			boolean connected = false;
			if (wifiInfo != null) {
				connected = wifiInfo.isConnected();
			}
			return connected;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void notifyService(Context context) {
		Intent service = new Intent(context, AutoUploadService.class);
		service.setAction(AutoUploadService.ACTION_AUTO_UPLOAD);
		context.startService(service);
	}

	public static void uploadIfNeed(Context context) {
		if (Auto.AUTO_UPLOAD) {
			notifyService(context);
		}
	}

}
