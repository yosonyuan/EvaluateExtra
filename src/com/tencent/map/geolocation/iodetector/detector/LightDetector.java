package com.tencent.map.geolocation.iodetector.detector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import com.tencent.map.geolocation.iodetector.DetectionProfile;

import java.util.Calendar;

/**
 * Created by toveyliu on 2016/8/26.
 */

public class LightDetector extends AbstractDetector {
	private static final String TAG = "LightDetector";

	private static final int THRESHOLD_HIGH = 2000;
	private static final int THRESHOLD_LOW = 50;

	private static final float MAX_GRAVITY_Z = 5.0f;
	private volatile float gravityZ = SensorManager.GRAVITY_EARTH;

	private volatile boolean lightBlocked = false;
	private volatile float lightIntensity; // 光线强度

	private static final int BUFFER_SIZE = 5; // 均值滤波
	private float[] lightBuffer = new float[BUFFER_SIZE];
	private float sumValue = 0.0f;
	private int mValueCount = 0;

	/**
	 * Singleton:static inner class
	 */
	private static class SingletonHolder {
		public static final LightDetector INSTANCE = new LightDetector();
	}

	private LightDetector() {
		super();
	}

	public static LightDetector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean isLightBlocked() {
		return this.lightBlocked;
	}

	public float getLightValue() {
		return this.lightIntensity;
	}

	@Override
	public void start() {
		initLightBuffer();
		super.start();
	}

	public void onLightEvent(SensorEvent event) {
		if (!isRunning()) {
			return;
		}
		int sensorType = event.sensor.getType();
		if (sensorType == Sensor.TYPE_GRAVITY) {
			gravityZ = event.values[2];
		} else if (sensorType == Sensor.TYPE_LIGHT) {
			// 更新sumValue和buffer的值
			int index = mValueCount % BUFFER_SIZE;
			sumValue -= lightBuffer[index];
			lightBuffer[index] = event.values[0];
			sumValue += lightBuffer[index];
			mValueCount++;

			// 对buffer中的值求均值
			if (mValueCount > BUFFER_SIZE) {
				lightIntensity = sumValue / BUFFER_SIZE;
				if (mValueCount == 10000) {// 防止无限增大
					mValueCount = 10000 % BUFFER_SIZE + BUFFER_SIZE;
				}
			} else { // 刚开始
				lightIntensity = sumValue / (mValueCount);
			}
		} else if (sensorType == Sensor.TYPE_PROXIMITY) {
			if (event.values[0] == event.sensor.getMaximumRange()) {
				this.lightBlocked = false;
			} else {
				this.lightBlocked = true;
				initLightBuffer();
			}
		}

		updateProfile();
		notifyDetecterListener(CALLBACK_DELAY_TIME);
		notifyDetecterDataDescListener(CALLBACK_DELAY_TIME);
	}

	private void initLightBuffer() {
		mValueCount = 0;
		sumValue = 0.0f;
		mProfile.setFcator(1.0f);
		for (int i = 0; i < BUFFER_SIZE; i++) {
			lightBuffer[i] = 0.0f;
		}
	}

	@Override
	public int getDetectorType() {
		return DetectionProfile.TYPE_LIGHT;
	}

	@Override
	protected void updateProfile() {
		if (lightBlocked || gravityZ < MAX_GRAVITY_Z) {
			mProfile.setConfidence(0.0f, 0.0f, 0.0f);
			return;
		}
		mProfile.setFcator(1.0f);

		if (lightIntensity > THRESHOLD_HIGH) { // > 2000 Lux
			mProfile.setConfidence(0.0f, 1.0f, 0.0f);
			// 当光照非常强时，增加室外的权重
			if (lightIntensity < 4000.0f) {
			} else if (lightIntensity < 10000.0f) {
				mProfile.setFcator(1.5f);
			} else {
				mProfile.setFcator(2.0f);
			}
		} else { // <= 2000 Lux
			int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			if (hourOfDay >= 8 && hourOfDay <= 17) { // day time
				mProfile.setConfidence(1.0f, 0.0f, 0.0f);
				// 白天时，小于50，增加室内权重
				// 之前设置为1.5f，但是在车内，光照基本上小于50，故需要减少光照权重
				if (lightIntensity < THRESHOLD_LOW) {
					mProfile.setFcator(1.3f);
				}
			} else { // night time
				if (lightIntensity <= THRESHOLD_LOW) { // <= 50Lux
					if (lightIntensity <= 6.0) {
						mProfile.setConfidence(0.0f, 0.0f, 0.0f);
					} else {
						float conf = (THRESHOLD_LOW - lightIntensity) / THRESHOLD_LOW;
						mProfile.setConfidence(1.0f, conf, 0.0f);
					}
				} else { // 50Lux < lightIntensity <= 2000Lux
					// TODO 感觉需要改进
					float conf = (THRESHOLD_HIGH - lightIntensity) / THRESHOLD_HIGH;
					mProfile.setConfidence(1.0f, 1.0f - conf, 0.0f);
				}
			}
		}
	}

	@Override
	protected String getDetectorDataDesc() {
		StringBuilder sb = new StringBuilder();
		sb.append("光照强度：").append(String.format("%.2f", lightIntensity)).append("\t,");
		sb.append("Blocked:").append(lightBlocked);
		return sb.toString();
	}
}
