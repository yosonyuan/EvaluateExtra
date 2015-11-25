package com.soso.evaextra.update;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.soso.evaextra.AppContext;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.text.format.DateFormat;
/**
 * 
* @ClassName: AppUpdater 
* @Description: APP更新	
*
* @date 2014-11-28 下午3:40:01 
*
 */
public class AppUpdater {
	private static final String UPDATE_URL = "http://lstest.map.qq.com/websvr/evaextra";

	/**
	 * 检查是否有新的包
	 */
	public AppUpdateInfo check() {
		AndroidHttpClient httpClient = AndroidHttpClient.newInstance("tencent");
		HttpGet request = new HttpGet(UPDATE_URL);

		try {
			HttpResponse response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String charset = EntityUtils.getContentCharSet(entity);
				String str = EntityUtils.toString(entity, charset);
				return AppUpdateInfo.from(str);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.close();
		}
		return AppUpdateInfo.NULL;
	}

	public void download(Context context, AppUpdateInfo info) {
		DownloadManager downloadManager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);

		Request request = new Request(Uri.parse(info.url));
		request.setShowRunningNotification(true);
		request.setTitle("正在下载 Evaextra V" + info.getVersion());
		request.setMimeType("application/vnd.android.package-archive");
		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS,
				"evaextra_v" + info.getVersion() + "_" + info.getDate()
						+ ".apk");
		long id = downloadManager.enqueue(request);

		if (context instanceof Activity) {
			AppContext appContext = AppContext.getInstance((Activity) context);
			appContext.getAppStatus().setDownloadId(id);
		}
	}

	public static class AppUpdateInfo {
		public static final AppUpdateInfo NULL = new AppUpdateInfo(0, null,
				null, null, null, -1L);

		private int code;
		private String date;
		private String version;
		private String url;
		private List<String> desc;
		private long size;

		private AppUpdateInfo(int code, String date, String version,
				String url, List<String> desc, long size) {
			super();
			this.code = code;
			this.date = date;
			this.version = version;
			this.url = url;
			this.desc = desc;
			this.size = size;
		}

		public int getCode() {
			return code;
		}

		public String getDate() {
			return date;
		}

		public String getVersion() {
			return version;
		}

		public String getUrl() {
			return url;
		}

		public long getSize() {
			return size;
		}

		public List<String> getDesc() {
			return new ArrayList<String>(desc);
		}

		@Override
		public String toString() {
			if (this == NULL) {
				return "NULL";
			}

			StringBuilder sb = new StringBuilder();
			sb.append("发布日期: ").append(date).append("\n");
			sb.append("版本: ").append(version).append("\n");
			sb.append("大小: ")
					.append(String.format("%.2fMB", size / 1024.0 / 1024))
					.append("\n");
			sb.append("问题修复及新增功能").append(":\n");
			for (int i = 0; i < desc.size(); i++) {
				sb.append(i).append(". ").append(desc.get(i)).append("\n");
			}
			return sb.toString();
		}

		public static AppUpdateInfo from(String json) {
			JSONObject obj = null;

			try {
				obj = new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
				return NULL;
			}

			if (!obj.has("url")) {
				return NULL;
			}

			final int code = obj.optInt("code", 0);
			final String date = obj.optString("date",
					DateFormat.format("yyyy-MM-dd", new Date()).toString());
			final String version = obj.optString("version", "未知版本");
			final String url = obj.optString("url", "");
			long size = obj.optLong("size");
			List<String> desc = null;

			JSONArray descArr = obj.optJSONArray("desc");
			if (descArr != null) {
				desc = new ArrayList<String>();
				for (int i = 0; i < descArr.length(); i++) {
					desc.add(descArr.optString(i, ""));
				}
			} else {
				desc = Collections.<String> emptyList();
			}

			return new AppUpdateInfo(code, date, version, url, desc, size);
		}
	}
}
