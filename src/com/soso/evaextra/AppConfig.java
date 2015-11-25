package com.soso.evaextra;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public final class AppConfig {
	private Context mContext;
	private static AppConfig appConfig;
	private SharedPreferences mPref;

	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			appConfig = new AppConfig();
			appConfig.mContext = context.getApplicationContext();
			appConfig.mPref = context.getSharedPreferences("configs",
					Context.MODE_PRIVATE);
		}
		return appConfig;
	}

	/**
	 * 获取Preference设置
	 */
	public SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	public boolean isKeepScreenOn() {
		// TODO
		return false;
	}

	public void setKeepScreenOn() {
		// TODO
	}

	public boolean isKeepWakeup() {
		// TODO
		return false;
	}

	public void setKeepWakeup() {
		// TODO
	}

	public void setCheckedSdk(boolean[] checked) {
		Editor editor = mPref.edit();
		editor.putBoolean(AppContext.TENCENT, checked[0]);
		editor.putBoolean(AppContext.BAIDU, checked[1]);
		editor.putBoolean(AppContext.AMAP, checked[2]);
		editor.putBoolean(AppContext.SOGOU, checked[3]);
		editor.commit();
	}

	public boolean[] getCheckedSdk() {
		return new boolean[] { mPref.getBoolean(AppContext.TENCENT, true), // tencent
				mPref.getBoolean(AppContext.BAIDU, true), // amap
				mPref.getBoolean(AppContext.AMAP, true), // baidu
				mPref.getBoolean(AppContext.SOGOU, true), // sogou
		};
	}

	/**
	 * 是否有 sdk 被选中
	 * 
	 * @return
	 */
	public boolean anySdkChecked() {
		for (String key : AppContext.ALL_KEYS) {
			if (mPref.getBoolean(key, false)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检查是否仅当前 sdk 被选中
	 * 
	 * @param key
	 * @return
	 */
	public boolean onlySdkChecked(String key) {
		for (String k : AppContext.ALL_KEYS) {
			if (!k.equals(key)) {
				if (mPref.getBoolean(k, false)) {
					return false;
				}
			}
		}
		return mPref.getBoolean(key, false);
	}

	public boolean isSdkChecked(String key) {
		return mPref.getBoolean(key, false);
	}

	/**
	 * 设置是否在地图上显示 sdk 的位置
	 * 
	 * @param key
	 * @param show
	 */
	public void setShowInMap(String key, boolean show) {
		Editor editor = mPref.edit();
		editor.putBoolean(key, show);
		editor.commit();
	}

	/**
	 * 检查是否在地图上显示 sdk 的位置
	 * 
	 * @param key
	 * @return
	 */
	public boolean isShowInMap(String key) {
		return mPref.getBoolean(key, false);
	}

	/**
	 * 在地图上显示所有 sdk 的位置
	 */
	public void setShowAllSdk() {
		Editor editor = mPref.edit();
		for (String key : AppContext.ALL_KEYS) {
			editor.putBoolean(key, true);
		}
		editor.commit();
	}

	/**
	 * 在地图上仅显示 key对应 的 sdk 的位置
	 * 
	 * @param key
	 */
	public void setShowOnly(String key) {
		Editor editor = mPref.edit();
		for (String k : AppContext.ALL_KEYS) {
			editor.putBoolean(k, false);
		}
		editor.putBoolean(key, true); // show this only
		editor.commit();
	}

	/**
	 * 设置是否在地图上显示连线
	 * 
	 * @param show
	 */
	public void setShowLine(boolean show) {
		Editor editor = mPref.edit();
		editor.putBoolean("show_line", show);
		editor.commit();
	}

	public boolean isShowLine() {
		return mPref.getBoolean("show_line", false);
	}

	public void setShowCountIndex(int count) {
		Editor editor = mPref.edit();
		editor.putInt("show_count", count);
		editor.commit();
	}

	public int getShowCountIndex() {
		return mPref.getInt("show_count", 0);
	}

	public void setShowLogCategory(int category) {
		Editor editor = mPref.edit();
		editor.putInt("show_log_category", category);
		editor.commit();
	}

	public int getShowLogCategory() {
		return mPref.getInt("show_log_category", 0);
	}
}
