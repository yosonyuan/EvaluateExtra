package com.soso.evaextra;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import android.os.SystemClock;
import cn.edu.hust.cm.common.util.Strings;

/**
 * 当前运行状态, 非持久化. 应用完全退出后不保存
 *
 * @author kingcmchen
 *
 */
public class AppStatus {

	private String mTencentAddr;
	private String mBaiduAddr;
	private String mAmapAddr;
	private String mSogouAddr;

	public String getAddress(String key) {
		if (AppContext.TENCENT.equals(key)) {
			return Strings.nullToEmpty(mTencentAddr);
		} else if (AppContext.BAIDU.equals(key)) {
			return Strings.nullToEmpty(mBaiduAddr);
		} else if (AppContext.AMAP.equals(key)) {
			return Strings.nullToEmpty(mAmapAddr);
		} else if (AppContext.SOGOU.equals(key)) {
			return Strings.nullToEmpty(mSogouAddr);
		}
		return "";
	}
	public void setAddr(String key ,String addr){
		if (AppContext.TENCENT.equals(key)) {
			mTencentAddr = addr;
		} else if (AppContext.BAIDU.equals(key)) {
			mBaiduAddr = addr;
		} else if (AppContext.AMAP.equals(key)) {
			mAmapAddr = addr;
		} else if (AppContext.SOGOU.equals(key)) {
			mSogouAddr = addr;
		}
	}
	
	public void clearLocationAddr() {
		mTencentAddr = null;
		mBaiduAddr = null;
		mAmapAddr = null;
		mSogouAddr = null;
	}

	/**
	 * 是否正在定位测试
	 */
	private boolean mLocationRunning;

	/**
	 * 定位测试开始时间
	 */
	private long mLocationStart;

	long logId=-1;

	// TODO 移动到别的类中

	// 开始定位的时间
	long tencentStart = -1;
	long baiduStart = -1;
	long amapStart = -1;
	long sogouStart = -1;

	// 得到首次定位结果的时间
	long tencentFirst = -1;
	long baiduFirst = -1;
	long amapFirst = -1;
	long sogouFirst = -1;

	private int mMapLevel;

	private long mDownloadId;

	/**
	 * 流量, 单位字节
	 */
	private long mTraffic = -1;
	// 流量基准
	private long mTrafficBase = -1;

	// 测试时间限制, 到达这个时间后自动停止测试, 单位为 ms
	private long mTotalDuration;

	public float getTrafficInKB() {
		if (mTraffic == -1) {
			return 0.0f;
		}
		return (mTraffic - mTrafficBase) / 1024.0f;
	}

	public void setTraffic(long traffic) {
		if (traffic == -1) {
			mTrafficBase = -1; // reset
		}
		mTraffic = traffic;
	}

	public void setTrafficBase(long traffic) {
		mTrafficBase = traffic;
	}

	public void setMapLevel(int level) {
		mMapLevel = level;
	}

	public int getMapLevel() {
		return mMapLevel < 14 ? 14 : mMapLevel;
	}

	public boolean isLocationRunning() {
		return mLocationRunning;
	}

	public void setLocationRunning(boolean running) {
		mLocationRunning = running;
	}

	public void setLocationStart() {
		mLocationStart = SystemClock.elapsedRealtime();
	}

	public void clearLocationStart() {
		mLocationStart = 0;
	}

	public long getLocationDuration() {
		return SystemClock.elapsedRealtime() - mLocationStart;
	}

	public void setTotalDuration(long totalDuration) {
		this.mTotalDuration = totalDuration;
	}

	public long getTotalDuration() {
		return mTotalDuration;
	}

	public long getFirstCost(String key) {
		if (AppContext.TENCENT.equals(key)) {
			return getTencentFirstCost();
		} else if (AppContext.BAIDU.equals(key)) {
			return getBaiduFirstCost();
		} else if (AppContext.AMAP.equals(key)) {
			return getAmapFirstCost();
		} else if (AppContext.SOGOU.equals(key)) {
			return getSogouFirstCost();
		}
		return 0L;
	}

	public String getFirstLocationTime(String key) {
		long start = 0;
		if (AppContext.TENCENT.equals(key)) {
			start = tencentStart;
		} else if (AppContext.BAIDU.equals(key)) {
			start = baiduStart;
		} else if (AppContext.AMAP.equals(key)) {
			start = amapStart;
		} else if (AppContext.SOGOU.equals(key)) {
			start = sogouStart;
		}

		if (start == -1) {
			return "--";
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd kk:mm:ss",
					Locale.ENGLISH);
			long delta = SystemClock.elapsedRealtime() - start;
			return sdf.format(new Date(new Date().getTime() - delta));
		}
	}

	/**
	 * 返回腾讯首次定位第一次耗时
	 *
	 * @return
	 */
	private long getTencentFirstCost() {
		if (tencentFirst == -1) {
			return 0L;
		}

		return tencentFirst - tencentStart;
	}

	/**
	 * 返回百度首次定位第一次耗时
	 *
	 * @return
	 */
	private long getBaiduFirstCost() {
		if (baiduFirst == -1) {
			return 0L;
		}

		return baiduFirst - baiduStart;
	}

	/**
	 * 返回高德首次定位第一次耗时
	 *
	 * @return
	 */
	private long getAmapFirstCost() {
		if (amapFirst == -1) {
			return 0L;
		}

		return amapFirst - amapStart;
	}

	/**
	 * 返回sogou首次定位耗时
	 *
	 * @return
	 */
	private long getSogouFirstCost() {
		if (sogouFirst == -1) {
			return 0L;
		}

		return sogouFirst - sogouStart;
	}

	public void setDownloadId(long downloadId) {
		this.mDownloadId = downloadId;
	}

	public long getDownloadId() {
		return mDownloadId;
	}
}
