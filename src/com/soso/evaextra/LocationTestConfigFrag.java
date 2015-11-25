package com.soso.evaextra;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;

import com.soso.evaextra.config.Auto;
import com.soso.evaluateextra.R;

public class LocationTestConfigFrag extends DialogFragment implements
		OnClickListener {

	public static LocationTestConfigFrag newInstance(MenuItem menuItem) {
		LocationTestConfigFrag f = new LocationTestConfigFrag();
		f.setMenuItem(menuItem);
		return f;
	}

	private MenuItem mMenuItem;

	public void setMenuItem(MenuItem menuItem) {
		this.mMenuItem = menuItem;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog d = new AlertDialog.Builder(getActivity())
				.setTitle("设置测试时间")
				.setSingleChoiceItems(R.array.test_duration, 2, this).create();
		d.setCanceledOnTouchOutside(true);
		return d;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		int[] values = getResources().getIntArray(R.array.test_duration_value);
		if (which < 0 || which >= values.length) {
			return;
		}

		int value = values[which];

		if (value == 0) {
			// ignore
			// 无时间限制
		} else {
			// 有时间限制
			AppContext appContext = AppContext.getInstance(getActivity());
			AppStatus appStatus = appContext.getAppStatus();
			appStatus.setTotalDuration(value * 1000);

			// 定时停止测试
			scheduleStop(value * 1000);
		}

		((LocationTestActivity) getActivity()).runOrStop(mMenuItem);
	}

	private void scheduleStop(long duration) {
		AlarmManager alarmManager = (AlarmManager) getActivity()
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(StopTestMonitor.ACTION_STOP);
		PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 1234,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.set(AlarmManager.RTC, System.currentTimeMillis()
				+ duration, pi);
	}

	public static void cancelScheduleStop(Context context) {
		if (!Auto.AUTO_EXIT) {
			return;
		}

		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(StopTestMonitor.ACTION_STOP);
		PendingIntent pi = PendingIntent.getBroadcast(context, 1234, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager.cancel(pi);
	}
}
