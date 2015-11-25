package com.soso.evaextra.config;

import android.view.View;

import com.soso.evaluateextra.R;

public class UI {

	/**
	 * 控制 LocationStatFrag 中是否显示图示
	 */
	public static final int NO_SHOW_IN_MAP = View.INVISIBLE;

	/**
	 * 控制 LocationTestActivity 中是否显示选择 sdk 和 查看日志
	 */
	public static final int ACTION_MENU_RES_ID = R.menu.location_test_activity_actions; // R.menu.location_test_activity_actions;

	/**
	 * 隐藏 sdk 的名字
	 */
	public static final boolean HIDE_SDK_NAME = false;

	/**
	 * 打乱 sdk 的顺序
	 */
	public static final boolean SHUFFLE_SDK_SORT = false;

	public static final boolean HIDE_LOCATION_TIMES = false;
	public static final boolean HIDE_LOCATION_ADDR = false;
}
