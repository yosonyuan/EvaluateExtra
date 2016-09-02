package com.soso.evaextra;

import java.util.List;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.soso.evaextra.config.UI;
import com.soso.evaextra.model.Result;
import com.soso.evaluateextra.R;

/**
 * 定位统计信息
 * 
 * @author kingcmchen
 * 
 */
public class LocationStatusFrag extends Fragment implements OnClickListener {

	private String mTitle;
	private String mKey;
	private AppContext mAppContext;
	private AppStatus mAppStatus;

	public static LocationStatusFrag newInstance(String title, String key) {

		LocationStatusFrag frag = new LocationStatusFrag();
		Bundle bundle = new Bundle();
		bundle.putString("title", title);
		bundle.putString("key", key);
		frag.setArguments(bundle);

		return frag;
	}

	public void update() {
		update(getView());
	}

	private void update(View v) {
		TextView tvMore = (TextView) v.findViewById(R.id.show_in_map);
		// 增加一条下划线
		tvMore.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		tvMore.setOnClickListener(this);

		tvMore.setVisibility(UI.NO_SHOW_IN_MAP);

		TextView tvTitle = (TextView) v.findViewById(R.id.title);
		TextView tvFirstLocationTime = (TextView) v
				.findViewById(R.id.first_location_time);
		TextView tvLocationTime = (TextView) v.findViewById(R.id.location_time);
		TextView tvLocationCount = (TextView) v
				.findViewById(R.id.location_count);
		if (UI.HIDE_LOCATION_TIMES) {
			tvLocationCount.setVisibility(View.GONE);
		}
		

		TextView tvLocationTraffic = (TextView) v
				.findViewById(R.id.location_traffic);
		TextView tvLocationAddress = (TextView) v
				.findViewById(R.id.location_addr);
		View vLocationRecords = v.findViewById(R.id.location_records);
		vLocationRecords.setOnClickListener(this);

		TextView[] tvRecords = new TextView[3];
		tvRecords[0] = (TextView) v.findViewById(R.id.record1);
		tvRecords[1] = (TextView) v.findViewById(R.id.record2);
		tvRecords[2] = (TextView) v.findViewById(R.id.record3);

		List<Result> results = mAppContext.getLocations(mKey, 3);
		for (int i = 0; i < results.size(); i++) {
			Result result = results.get(i);
			String provider = (result.getProvider() == null || result.getProvider().equalsIgnoreCase("null"))?"gps":result.getProvider();
			String str = result.getTime() + "    " + result.getLat() + ","
					+ result.getLng() + "," + result.getRadius()+","+provider;
			tvRecords[i].setText(str);
		}

		if (results.isEmpty()) {
			for (TextView tv : tvRecords) {
				tv.setText("");
			}
		}

		tvTitle.setText(mTitle);
		String firstLocationTime = getFirstLocationTime();
		String locationTime = getLocationTime();
		String locationCount = getLocationCount();
		String locationTraffic = String
				.format(str(R.string.ph_location_traffic),
						results!=null &&results.size()>0?results.get(results.size()-1).distance:0);
		String locationAddr = String.format(str(R.string.ph_location_addr),
				mAppStatus.getAddress(mKey));
		tvFirstLocationTime.setText(firstLocationTime);
		tvLocationTime.setText(locationTime);
		tvLocationCount.setText(locationCount);
		tvLocationTraffic.setText(locationTraffic);
		tvLocationAddress.setText(locationAddr);
		if (UI.HIDE_LOCATION_ADDR) {
			tvLocationAddress.setVisibility(View.GONE);
		}
	}

	private String getFirstLocationTime() {
		return String.format(str(R.string.ph_first_location_time),
				mAppStatus.getFirstCost(mKey));
	}

	private String getLocationTime() {
		return String.format(str(R.string.ph_location_time),
				mAppStatus.getFirstLocationTime(mKey));
	}

	private String getLocationCount() {
		int count = 0;
		int failed = mAppContext.getLocationCouter().getLocationCount(mKey);
		count = mAppContext.getAllLocations(mKey).size();

		return String.format(str(R.string.ph_location_count), count, failed);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitle = getArguments().getString("title");
		mKey = getArguments().getString("key");
		mAppContext = AppContext.getInstance(getActivity());
		mAppStatus = mAppContext.getAppStatus();
	}

	private String str(int resId) {
		return getResources().getString(resId);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.frag_location_stat, container, false);
		update(v);
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.show_in_map) {
			LocationMapActivity.startMe(getActivity(), mKey);
		} else if (v.getId() == R.id.location_records) {
		}
	}
}
