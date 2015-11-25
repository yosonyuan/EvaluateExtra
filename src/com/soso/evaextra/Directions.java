package com.soso.evaextra;

import java.util.LinkedList;

import android.location.Location;

import com.tencent.map.geolocation.TencentLocation;

/**
 * http://www.cnblogs.com/shaocm/archive/2012/08/11/2633640.html
 * 
 * @author kingcmchen
 * 
 */

public class Directions {

	private static final double ZERO = 1e-7;
	private final static int CAP = 3;

	// 点跟点之间的距离不超过20km
	// 角跟角之间的变化不超过??

	// 最多 CAP 个点
	private final LinkedList<TencentLocation> mLocations = new LinkedList<TencentLocation>();
	// 最多 CAP - 1 个角度
	private final LinkedList<Double> mDirs = new LinkedList<Double>();

	private int mCounter;

	private static float dist(double startLatitude, double startLongitude,
			double endLatitude, double endLongitude) {
		float[] results = new float[2];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude,
				endLongitude, results);
		return results[0];
	}

	public boolean addAndCheck(TencentLocation location) {
		mCounter++;
		// 连续定10次后才生效
		if (mCounter < 10) {
			return true;
		}

		makeSureSize();

		int pointNum = mLocations.size();
		if (pointNum > 0) {
			TencentLocation start = mLocations.get(pointNum - 1);
			TencentLocation end = location;
			double distance = dist(start.getLatitude(), start.getLongitude(),
					end.getLatitude(), end.getLongitude());
			if (distance < 20 * 1000) {
				// 点跟点之间的距离不超过20km
				mLocations.add(location);
			} else {
				return false;
			}
		}

		pointNum = mLocations.size();

		if (pointNum >= 2) {
			TencentLocation start = mLocations.get(pointNum - 2);
			TencentLocation end = location;
			// 当前点跟前一个点的角度
			mDirs.add(GetJiaoDu(start.getLatitude(), start.getLongitude(),
					end.getLatitude(), end.getLongitude()));
		}

		int num = mDirs.size();
		if (num == 2) {

		}
		return true;
	}

	public void shutdown() {
		mLocations.clear();
		mDirs.clear();
		mCounter = 0;
	}

	private void makeSureSize() {
		if (mLocations.size() == CAP) {
			mLocations.removeFirst();
			mDirs.removeFirst();
		}
	}

	public static double GetJiaoDu(double lat1, double lng1, double lat2,
			double lng2) {
		double x1 = lng1;
		double y1 = lat1;
		double x2 = lng2;
		double y2 = lat2;
		double pi = Math.PI;
		double w1 = y1 / 180 * pi;
		double j1 = x1 / 180 * pi;
		double w2 = y2 / 180 * pi;
		double j2 = x2 / 180 * pi;
		double ret;
		if (Math.abs(j1 - j2) < ZERO) {
			if (w1 > w2)
				return 270; // 北半球的情况，南半球忽略
			else if (w1 < w2)
				return 90;
			else
				return -1;// 位置完全相同
		}
		ret = 4
				* Math.pow(Math.sin((w1 - w2) / 2), 2)
				- Math.pow(
						Math.sin((j1 - j2) / 2) * (Math.cos(w1) - Math.cos(w2)),
						2);
		ret = Math.sqrt(ret);
		double temp = (Math.sin(Math.abs(j1 - j2) / 2) * (Math.cos(w1) + Math
				.cos(w2)));
		ret = ret / temp;
		ret = Math.atan(ret) / pi * 180;
		if (j1 > j2) // 1为参考点坐标
		{
			if (w1 > w2)
				ret += 180;
			else
				ret = 180 - ret;
		} else if (w1 > w2)
			ret = 360 - ret;
		return ret;
	}

}
