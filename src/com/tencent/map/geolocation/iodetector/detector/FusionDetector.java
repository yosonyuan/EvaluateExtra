package com.tencent.map.geolocation.iodetector.detector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

import com.tencent.map.geolocation.iodetector.IODetectorManager;
import com.tencent.map.geolocation.iodetector.IOListener;
import com.tencent.map.geolocation.iodetector.listener.IODetectorDataListener;
import com.tencent.map.geolocation.iodetector.listener.IODetectorListener;
import com.tencent.map.geolocation.iodetector.DetectionProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by toveyliu on 2016/8/26.
 */

public class FusionDetector extends AbstractDetector implements SensorEventListener, IODetectorListener {
	private static final String TAG = "FusionDetector";

	private static class DetectorAttr {
		/**
		 * 检测器融合时的权重
		 */
		float weight;
		/**
		 * 检测器检测结果的有效时间:ms
		 */
		long effectTime;

		public DetectorAttr(float weight, long effectTime) {
			this.weight = weight;
			this.effectTime = effectTime;
		}
	}

	private static SparseArray<DetectorAttr> attrArr;

	static {
		attrArr = new SparseArray<DetectorAttr>(DetectionProfile.FUSION_TYPE_SIZE);
		// GPS、WiFi、Light三者判断的权重
		// TODO 权重需要调整
		attrArr.put(DetectionProfile.TYPE_GPS, new DetectorAttr(3.0F, 10 * 1000));
		attrArr.put(DetectionProfile.TYPE_WIFI, new DetectorAttr(2.0F, 10 * 1000));
		attrArr.put(DetectionProfile.TYPE_LIGHT, new DetectorAttr(1.0F, 60 * 1000));
	}

	/**
	 * 定时回调室内外判断结果
	 */
	private static final int MSG_ID_TIMED_CALLBACK = 21000;

	/**
	 * 是否将所有检测器的检测结果回调给外部<br/>
	 * 当发布时，需要置为false
	 */
	protected boolean isOutputAllDetectorToUser = false;

	private List<AbstractDetector> detectorList;
	private SparseArray<DetectionProfile> profilesArr;

	private int sensorSampleRate = SensorManager.SENSOR_DELAY_NORMAL; // 传感器的采样频率
	private SensorManager mSensorManager;
	private Sensor sensorLight; // 光线传感器
	private Sensor sensorProximity; // 距离传感器
	private Sensor sensorGravity; // 重力加速度计

	private List<IOListener> mIOListeners;
	private Context mContext;

	private MyHandler mHandler;

	private long callbackDelayTime = 1000L;

	public FusionDetector(Context context) {
		this.mContext = context.getApplicationContext();
		this.mSensorManager = (SensorManager) this.mContext.getSystemService(Context.SENSOR_SERVICE);
		this.sensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		this.sensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		this.sensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

		detectorList = new ArrayList<AbstractDetector>(DetectionProfile.FUSION_TYPE_SIZE);
		detectorList.add(GpsDetector.getInstance());
		detectorList.add(WifiDetector.getInstance());
		detectorList.add(LightDetector.getInstance());

		for (int i = 0; i < detectorList.size(); i++) {
			detectorList.get(i).addDetectorListener(this);
		}

		profilesArr = new SparseArray<DetectionProfile>(DetectionProfile.FUSION_TYPE_SIZE);

		mIOListeners = new ArrayList<IOListener>();
	}

	private AbstractDetector getDetector(int type) {
		for (int i = 0; i < detectorList.size(); i++) {
			if (type == detectorList.get(i).getDetectorType()) {
				return detectorList.get(i);
			}
		}
		return null;
	}

	@Override
	@Deprecated
	public void start() {
	}

	public void start(Looper looper) {
		// 开启自身(FusionDetector)
		this.start();
		prepareHandler(looper);
		// 开启其它Detector
		for (int i = 0; i < detectorList.size(); i++) {
			detectorList.get(i).start();
		}

		mSensorManager.registerListener(this, sensorLight, sensorSampleRate);
		mSensorManager.registerListener(this, sensorProximity, sensorSampleRate);
		mSensorManager.registerListener(this, sensorGravity, sensorSampleRate);

		if (callbackDelayTime > 0) {
			mHandler.sendEmptyMessageDelayed(MSG_ID_TIMED_CALLBACK, callbackDelayTime);
		}
	}

	@Override
	public void stop() {
		super.stop();
		for (int i = 0; i < detectorList.size(); i++) {
			detectorList.get(i).stop();
		}
		mSensorManager.unregisterListener(this);

		mHandler.shutdown();
	}

	/**
	 * 停止某一类型的IO检测器
	 *
	 * @param type
	 * @see DetectionProfile
	 */
	private void stop(int type) {
		getDetector(type).stop();
	}

	@Override
	public int getDetectorType() {
		return DetectionProfile.TYPE_FUSION;
	}

	@Override
	public void updateProfile() {
		mProfile.setConfidence(0.0f, 0.0f, 0.0f);

		long curT = System.currentTimeMillis();
		// 如果有GPS的，直接用GPS的
		DetectionProfile tmpGpsPro = profilesArr.get(DetectionProfile.TYPE_GPS);
		if (tmpGpsPro != null) {
			// 在有效时间内
			if (tmpGpsPro.getTime() - curT < attrArr.get(DetectionProfile.TYPE_GPS).effectTime) {
				mProfile.setConfidence(tmpGpsPro.getConfidence());
				return;
			}
		}
		// ① GPS信息不可用时，用WiFi和Light的来进行比较
		// ② 光照判断为室外时，置信度会非常高，则直接置为室外
		// ③ 光照判断为室内时，暂时不可信，需要进一步根据Wi-Fi来进行判断
		// 如果Wi-Fi判断为室内，置信度较高，可以直接判断为室内
		// （在某些行车状态下，可能有多个移动WiFi信号强度 >-55，该类需要通过服务器进行判断，过滤掉移动wifi的mac）
		// （加入行车判断，如果当前是行车，则直接置为室外）
		// 如果Wi-Fi判断为室外，则置为未知（摆渡车、厕所等情况）
		DetectionProfile tmpWifiPro = profilesArr.get(DetectionProfile.TYPE_WIFI);
		DetectionProfile tmpLightPro = profilesArr.get(DetectionProfile.TYPE_LIGHT);
		if (tmpWifiPro == null || tmpWifiPro.getEnvironment() == IODetectorManager.UNKNOW) {
			if (tmpLightPro != null) {
				mProfile.setConfidence(tmpLightPro.getConfidence());
			}
		} else if (tmpLightPro == null || tmpLightPro.getEnvironment() == IODetectorManager.UNKNOW) {
			if (tmpWifiPro != null) {
				mProfile.setConfidence(tmpWifiPro.getConfidence());
			}
		} else { // 二者均不为null或UNKNOW，判断二者是否判断结果相等
			int tmpLightEnv = tmpLightPro.getEnvironment();
			if (tmpLightEnv == IODetectorManager.OUTDOOR) { // 光照判断为室外，置信度比较高，则直接判断为室外
				mProfile.setConfidence(0.0f, 1.0f, 0.0f);
			} else {// 光照判断为室内
				int tmpWifiEnv = tmpWifiPro.getEnvironment();
				if (tmpWifiEnv == IODetectorManager.INDOOR) { // WiFi判断为室内，置信度比较高，
					mProfile.setConfidence(1.0f, 0.0f, 0.0f);
				}
				// 如果光照判断为室内，而wifi判断为室外，则置为未知，已默认为未知
			}
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		switch (sensorType) {
		case Sensor.TYPE_LIGHT:
		case Sensor.TYPE_PROXIMITY:
		case Sensor.TYPE_GRAVITY:
			((LightDetector) getDetector(DetectionProfile.TYPE_LIGHT)).onLightEvent(event);
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * 融合Wifi、gps、光照、磁场的室内外判断
	 *
	 * @param profile
	 */
	@Override
	public void onIODetecterUpdate(DetectionProfile profile) {
		if (profile == null)
			return;
		int type = profile.getDetectType();
		profilesArr.put(type, profile);
	}

	public void addIOListener(IOListener listener) {
		if (listener == null)
			return;

		mIOListeners.add(listener);
		Log.d(TAG, "addIOListener()");
	}

	public void removeIOListener(IOListener listener) {
		mIOListeners.remove(listener);
	}

	private void notifyIOListeners() {
		if (mIOListeners == null || mIOListeners.size() == 0)
			return;
		int curEnv = mProfile.getEnvironment();
		for (int i = 0; i < mIOListeners.size(); i++) {
			mIOListeners.get(i).onIOEnvListener(curEnv);
		}
	}

	/**
	 * 设置回调延迟时间，默认为1000ms
	 * 
	 * @param delayT
	 *            单位：ms
	 */
	public void setCallbackTimeDelay(long delayT) {
		this.callbackDelayTime = delayT;
	}

	@Override
	protected String getDetectorDataDesc() {
		return "Fusion结果";
	}

	private void prepareHandler(Looper looper) {
		if (Looper.myLooper() == null)
			Looper.prepare();
		if (mHandler == null) {
			mHandler = new MyHandler(looper);
			return;
		}

		mHandler.removeCallbacksAndMessages(null);
		if (mHandler.getLooper() != looper) {
			mHandler = new MyHandler(looper);
		}
	}

	private class MyHandler extends Handler {
		MyHandler(Looper looper) {
			super(looper);
		}

		public void shutdown() {
			removeCallbacksAndMessages(null);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ID_TIMED_CALLBACK:
				updateProfile();
				notifyIOListeners();

				if (callbackDelayTime > 0) {
					sendEmptyMessageDelayed(MSG_ID_TIMED_CALLBACK, callbackDelayTime);
				}
				break;
			}
		}
	}
}
