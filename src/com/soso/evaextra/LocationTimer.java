package com.soso.evaextra;

import android.os.Handler;

public class LocationTimer {
	// /////////////////////////////////////////
	// (开始定位后) 时间计数器, 每 100 ms 增加 1次
	private static long sTimerLoops;
	private static final Handler sTimerHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				sTimerLoops++;
				sendEmptyMessageDelayed(1, 100);
			}
		}
	};

	public void startLocationTimer() {
		sTimerHandler.sendEmptyMessage(1);
	}

	public void stopLocationTimer(boolean reset) {
		if (reset) {
			sTimerLoops = 0;
		}
		sTimerHandler.removeMessages(1);
	}

	public long getLocationTimerLoop() {
		return sTimerLoops;
	}

	public void resetLocationTimerLoop() {
		sTimerLoops = 0;
	}

	// /////////////////////////////////////////
}
