package com.soso.evaextra.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.os.Build;

import com.google.common.io.ByteSink;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.soso.evaextra.AppContext;
import com.soso.evaextra.config.IO;

public class FileLogger {

	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd",
			Locale.ENGLISH);
	private final HashMap<String, File> fileMap = new HashMap<String, File>();
	private final String path;
	private final String name;
	private final boolean multi;

	public FileLogger(File dir, String name) {
		File subdir = new File(dir, SDF.format(new Date()));
		if (!subdir.exists()) {
			subdir.mkdirs();
		}
		this.path = subdir.getAbsolutePath();
		this.name = name;
		this.multi = true;
	}

	public void log(String tag, String message) {
		File file = null;
		if (multi) {
			file = new File(path, name + tag);
			fileMap.put(name + "_" + tag, file);
		} else {
			file = new File(path, name);
		}

		try {
			// Files.append(message + "\n", file, Charset.defaultCharset());
			ByteSink sink = Files.asByteSink(file, FileWriteMode.APPEND);
			message += "\n";
			sink.write(IO.ENCRYPY_LOG ? SosoLocUtils.encryptBytes(message
					.getBytes()) : message.getBytes());
		} catch (IOException e) {
			// ignore
		}
	}
	public void logWithoutEncrypt(String tag, String message) {
		File file = null;
		if (multi) {
			file = new File(path, name + tag);
			fileMap.put(name + "_" + tag, file);
		} else {
			file = new File(path, name);
		}
		try {
			Files.append(message + "\n", file, Charset.defaultCharset());
		} catch (IOException e) {
			// ignore
		}
	}

	public String mergePointShowLog(long duration, String deviceId, File tmp) {
		File merge = new File(path, name.replace("_pointshow", "") + "_"
				+ duration + "_" + Build.MODEL + "_" + deviceId);
		ByteSink sink = Files.asByteSink(merge, FileWriteMode.APPEND);

		for (File log : fileMap.values()) {
			if (log.getName().endsWith(AppContext.TENCENT)) {
				continue;
			}

			try {
				Files.asByteSource(log).copyTo(sink);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			Files.asByteSource(tmp).copyTo(sink);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return merge.getAbsolutePath();
	}

}
