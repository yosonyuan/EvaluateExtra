package com.soso.evaextra;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.TimingLogger;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.soso.evaextra.SimpleDb.LogEntry;
import com.soso.evaextra.SimpleDb.SimpleDbUtil;
import com.soso.evaextra.config.Auto;
import com.soso.evaextra.config.IO;
import com.soso.evaextra.model.Result;
import com.soso.evaextra.util.SosoLocUtils;
import com.soso.evaluateextra.R;

public class LogListActivity extends ActionBarActivity implements
		OnItemClickListener, OnItemLongClickListener, OnClickListener,
		OnNavigationListener {

	private static final int ALL = 3;
	private static final int UNUPLOAD = 2;
	private static final int YESTERDAY = 1;
	private static final int TODAY = 0;
	private ListView mListView;
	private Ad mLogAdapter;
	private int mIndex;
	private AppConfig mAppConfig;

	public static void startMe(Context context) {
		Intent intent = new Intent(context, LogListActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_loglist);

		mAppConfig = AppConfig.getAppConfig(this);

		initActionBar();

		mListView = (ListView) findViewById(R.id.listview);
		mLogAdapter = new Ad(this, android.R.layout.simple_list_item_2,
				new ArrayList<SimpleDb.LogEntry>());
		mListView.setAdapter(mLogAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		int logCategory = mAppConfig.getShowLogCategory();
		List<LogEntry> logs = getLogsFromDb(logCategory);
		mLogAdapter.updateData(logs);
	}

	private List<LogEntry> getLogsFromDb(int logCategory) {
		List<LogEntry> logs = null;

		SimpleDb db = new SimpleDb(this);
		TimingLogger logger = new TimingLogger("tag", "label");
		logger.addSplit("find all");

		if (logCategory == TODAY) {
			logs = SimpleDbUtil.findToday(db);
		} else if (logCategory == YESTERDAY) {
			logs = SimpleDbUtil.findYesterday(db);
		} else if (logCategory == UNUPLOAD) {
			logs = SimpleDbUtil.findUnuploaded(db);
		} else if (logCategory == ALL) {
			logs = SimpleDbUtil.findAll(db);
		}
		db.close();
		logger.dumpToLog();

		return logs != null ? logs : Collections.<LogEntry> emptyList();
	}

	private void initActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		SpinnerAdapter spinner = ArrayAdapter.createFromResource(this,
				R.array.log_category,
				android.R.layout.simple_spinner_dropdown_item);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(spinner, this);

		// 显示哪个log_category
		actionBar.setSelectedNavigationItem(mAppConfig.getShowLogCategory());
	}

	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		if (position == mAppConfig.getShowLogCategory()) {
			return true;
		}
		mAppConfig.setShowLogCategory(position);

		// 更新 listview
		int logCategory = mAppConfig.getShowLogCategory();
		List<LogEntry> logs = getLogsFromDb(logCategory);
		mLogAdapter.updateData(logs);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	class Ad extends ArrayAdapter<LogEntry> {

		private final List<LogEntry> mLogs;

		public Ad(Context context, int textViewResourceId,
				List<LogEntry> objects) {
			super(context, textViewResourceId, objects);
			mLogs = objects;
		}

		public void updateData(List<LogEntry> newLogs) {
			mLogs.clear();
			mLogs.addAll(newLogs);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// View v = super.getView(position, convertView, parent);

			View v = convertView;
			if (v == null) {
				v = getLayoutInflater().inflate(R.layout.list_item_log, parent,
						false);
			}
			TextView logName = (TextView) v.findViewById(android.R.id.text1);
			TextView uploaded = (TextView) v.findViewById(android.R.id.text2);

			final LogEntry entry = mLogs.get(position);

			String prefix = entry.uploaded ? "√ " : "+ ";
			logName.setText(prefix + entry.log_name);

			if (entry.log_path != null) {
				File file = new File(entry.log_path);
				String size = String.format(Locale.ENGLISH, "%.2fKB",
						file.length() / 1024.0);
				uploaded.setText(size);
			} else {
				uploaded.setText("未知文件");
			}
			final ImageView showInMap = (ImageView) v.findViewById(R.id.icon);
			showInMap.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO check entry
					if(entry.log_path != null){
						File file = new File(entry.log_path);
						recreatePointsFromLog(file);
		
						Bundle extras = new Bundle();
						extras.putBoolean(LocationMapActivity.RECREATE_FROM_LOG,
								true);
						LocationMapActivity.startMe(LogListActivity.this,
								AppContext.TENCENT, extras);
					}else{
//						Toast.makeText(LogListActivity.this, "文件不存在", Toast.LENGTH_SHORT).show();
					}
				}
			});
			return v;
		}

		private void recreatePointsFromLog(File file) {
			try {
				List<String> lines = Files.readLines(file,
						Charset.defaultCharset());
				String key = null;
				for (String line : lines) {
					if (line.startsWith("B|")) {
						key = AppContext.BAIDU;
						parseLine(key, line);
					} else if (line.startsWith("A|")) {
						key = AppContext.AMAP;
						parseLine(key, line);
					} else if (line.startsWith("T|")) {
						key = AppContext.TENCENT;
						parseLine(key, line);
					} else if (line.startsWith("S|")) {
						key = AppContext.SOGOU;
						parseLine(key, line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void parseLine(String key, String line) {
			int end = line.indexOf('|', 2);
			if (end == -1) {
				return;
			}

			String data = line.substring(2, end);
			String[] arr = data.split(",");
			double lat = 0;
			double lng = 0;
			float accu = 0;
			if (arr.length == 3) {
				try {
					lat = Double.parseDouble(arr[0]);
					lng = Double.parseDouble(arr[1]);
					accu = Float.parseFloat(arr[2]);
					AppContext.getInstance(LogListActivity.this).putLocation(
							key,
							new Result(key).setLat(lat).setLng(lng)
									.setRadius(accu));
				} catch (NumberFormatException e) {
					Log.e("LogListActivity", e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mIndex = position;
		LogEntry entry = mLogAdapter.getItem(position);
		String alert = "";
		if (entry.uploaded) {
			alert = "重新上传!!! ";
		}

		if (entry.log_path == null) {
			Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
			return;
		}
		File file = new File(entry.log_path);
		new AlertDialog.Builder(this)
				.setTitle("上传")
				.setMessage(
						String.format(alert + "将log(%.2fKB)上传到服务器?",
								file.length() / 1024.0))
				.setPositiveButton("确认", this).setNegativeButton("取消", this)
				.show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				if (which == DialogInterface.BUTTON_POSITIVE) {
					LogEntry entry = mLogAdapter.getItem(position);
					SimpleDb db = new SimpleDb(LogListActivity.this);
					db.delete(entry._id);
					db.close();

					mLogAdapter.remove(entry);
				}
			}
		};
		new AlertDialog.Builder(this).setTitle("删除").setMessage("删除本条记录")
				.setPositiveButton("确认", listener)
				.setNegativeButton("取消", listener).show();
		return false;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		if (which == DialogInterface.BUTTON_POSITIVE) {
			LogEntry entry = mLogAdapter.getItem(mIndex);
			new UploadTask(entry).execute();
		}
	}

	public static void upload(File file) throws IOException {
		AndroidHttpClient client = AndroidHttpClient.newInstance("tencent");
		// HttpPost post = new HttpPost(
		// "http://111.161.52.33:10001/DataWarehouse/compare.jsp");
		String filename = file.getName().replace(" ", "_");
		// config
		if (Auto.AUTO_UPLOAD && filename.length() > 4) {
			filename = "2048" + filename.substring(4, filename.length());
		}
		HttpPost post = new HttpPost(
				"http://111.161.52.33:10001/DataWarehouse/UploadFile?type=compare&path="
						+ filename);

		// MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		// builder.addBinaryBody("path", file);
		// post.setEntity(builder.build());

		File tmp = null;
		if (IO.ENCRYPY_LOG) {
			tmp = File.createTempFile("prefix", "suffix");
			SosoLocUtils.decrypteFile(file, tmp);
		} else {
			tmp = file;
		}

		post.setEntity(new FileEntity(tmp, "text/plain"));
		HttpResponse resp = null;
		HttpEntity entity = null;
		try {
			resp = client.execute(post);
			if (resp.getStatusLine().getStatusCode() == 200) {
				entity = resp.getEntity();
				// System.out.println(EntityUtils.toString(entity));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (entity != null) {
				entity.consumeContent();
			}
			client.close();
			tmp.delete();
		}
	}

	class UploadTask extends AsyncTask<Void, Void, Boolean> {

		private LogEntry mLogEntry;
		private ProgressDialog mDialog;
		private SimpleDb mDb;

		UploadTask(LogEntry logEntry) {
			super();
			this.mLogEntry = logEntry;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = ProgressDialog.show(LogListActivity.this, "上传中", "请稍候");
			mDialog.setCancelable(true);

			mDb = new SimpleDb(LogListActivity.this);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				upload(new File(mLogEntry.log_path));

				mLogEntry.uploaded = true;
				SimpleDbUtil.update(mDb, mLogEntry);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} finally {
				mDb.close();
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			boolean res = (result != null && result);
			Toast.makeText(LogListActivity.this, res ? "上传成功" : "上传失败",
					Toast.LENGTH_SHORT).show();
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}

			mLogAdapter.notifyDataSetChanged();
			// mListView.setAdapter(new Ad(LogListActivity.this,
			// android.R.layout.simple_list_item_1, m));
		}

	}
}
