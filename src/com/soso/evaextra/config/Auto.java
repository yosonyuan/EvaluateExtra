package com.soso.evaextra.config;

public class Auto {

	/**
	 * 自动退出
	 */
	public static final boolean AUTO_EXIT = false;

	/**
	 * 自动在 wifi 条件下上传log
	 */
	public static final boolean AUTO_UPLOAD = true;

	/**
	 * log 时间短于这个值的认为过小, 不必上传
	 */
	public static final long SHORT_LOG_LIMIT = 1 * 5 * 1000;
}
