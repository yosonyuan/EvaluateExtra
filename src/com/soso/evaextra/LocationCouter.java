package com.soso.evaextra;
/**
 * 定位失败计数
 * @kejiwang
 *
 */
public class LocationCouter {
	private long mTencentLocationCount;
	private long mBdLocationCount;
	private long mAMapLocationCount;
	private long mSogouLocationCount;

	public LocationCouter() {
	}
	public int increaseLocationCount(String key) {
		if (AppContext.AMAP.equals(key)) {
			mAMapLocationCount++;
		} else if (AppContext.BAIDU.equals(key)) {
			mBdLocationCount++;
		} else if (AppContext.TENCENT.equals(key)) {
			mTencentLocationCount++;
		}else if (AppContext.SOGOU.equals(key)) {
			mSogouLocationCount++;
		}
		return 0;
	}
	public int getLocationCount(String key) {
		if (AppContext.AMAP.equals(key)) {
			return (int) mAMapLocationCount;
		} else if (AppContext.BAIDU.equals(key)) {
			return (int) mBdLocationCount;
		} else if (AppContext.TENCENT.equals(key)) {
			return (int) mTencentLocationCount;
		} else if (AppContext.SOGOU.equals(key)) {
			return (int) mSogouLocationCount;
		}
		return 0;
	}
	public void resetLocationCount() {
		mTencentLocationCount = 0;
		mAMapLocationCount = 0;
		mBdLocationCount = 0;
		mSogouLocationCount = 0;
	}

}