package com.tencent.map.geolocation.iodetector;

import android.content.Context;
import android.os.Looper;
import android.util.SparseArray;

import com.tencent.map.geolocation.iodetector.detector.FusionDetector;
import com.tencent.map.geolocation.iodetector.listener.IODetectorDataListener;
import com.tencent.map.geolocation.iodetector.listener.IODetectorListener;

import static com.tencent.map.geolocation.iodetector.DetectionProfile.FUSION_TYPE_SIZE;

/**
 * Created by toveyliu on 2016/8/26.
 */

public class IODetectorManager {
	private static final String TAG = "IODetectorManager";

	public static final int ENV_CLASS_NUM = 3;
	public static final int INDOOR = 0;
	public static final int OUTDOOR = 1;
	public static final int UNKNOW = 2;

	private static SparseArray<String> environmentMap;

	static {
		environmentMap = new SparseArray<String>(FUSION_TYPE_SIZE);
		environmentMap.put(UNKNOW, "unknow");
		environmentMap.put(INDOOR, "indoor");
		environmentMap.put(OUTDOOR, "outdoor");
	}

	private Context mContext;
	private static IODetectorManager mIOManager;
	private FusionDetector mFusionDetector;

	private IODetectorManager(Context context) {
		if (context != null) {
			mContext = context.getApplicationContext();
			mFusionDetector = new FusionDetector(mContext);
		}
	}

	/**
	 * 获取室内外检测实例
	 *
	 * @param context
	 *            传入App上下文
	 * @return 室内外检测实例
	 */
	public static synchronized IODetectorManager getInstance(Context context) {
		if (context == null) {
			throw new IllegalStateException("context cannot be null!");
		}
		if (mIOManager == null) {
			synchronized (IODetectorManager.class) {
				mIOManager = new IODetectorManager(context.getApplicationContext());
			}
		}
		return mIOManager;
	}

	public static String getEnvironmentDesc(int env) {
		return environmentMap.get(env);
	}

	/**
	 * 开始检测
	 */
	public void start(Looper looper) {
		if (!mFusionDetector.isRunning()) {
			mFusionDetector.start(looper);
		}
	}

	/**
	 * 停止检测
	 */
	public void stop() {
		if (mFusionDetector.isRunning()) {
			mFusionDetector.stop();
		}
	}

	/**
	 * 设置回调结果时长，默认为1000ms
	 *
	 * @param callbackTime
	 *            单位：ms
	 */
	public void setCallbackTimeDelay(long callbackTime) {
		if (mFusionDetector.isRunning()) {
			mFusionDetector.setCallbackTimeDelay(callbackTime);
		}
	}

	// /**
	// * 添加室内外融合检测结果监听器<br/>
	// *
	// * @param listener
	// * 室内外检测结果监听器
	// */
	// public void addIODetecterFusionListener(IODetectorListener listener) {
	// mFusionDetector.addDetectorListener(listener);
	// }
	//
	// /**
	// * 添加室内外融合检测结果描述监听器<br/>
	// *
	// * @param listener
	// * 室内外检测结果描述监听器
	// */
	// public void addIODetecterDataDescFusionListener(IODetectorDataListener
	// listener) {
	// mFusionDetector.addDetectorDataDescListener(listener);
	// }

	public void addIOListener(IOListener listener) {
		if (listener == null)
			return;
		mFusionDetector.addIOListener(listener);
	}

	public void removeIOListener(IOListener listener) {
		mFusionDetector.removeIOListener(listener);
	}
}
