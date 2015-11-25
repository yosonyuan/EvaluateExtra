package com.soso.evaextra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.soso.evaluateextra.R;

public class LocationSettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String KEY_LOCATION_SETTINGS_URL_INDEX = "key_location_settings_url_index";

	public static void start(Context context) {
		Intent intent = new Intent(context, LocationSettingsActivity.class);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.location_settigns);
		update(getPreferenceScreen().getSharedPreferences(),
				KEY_LOCATION_SETTINGS_URL_INDEX);
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (KEY_LOCATION_SETTINGS_URL_INDEX.equals(key)) {
			update(sharedPreferences, key);
		}
	}

	private void update(SharedPreferences sharedPreferences, String key) {
		Preference urlIndexPref = findPreference(key);
		String value = sharedPreferences.getString(key, "0");
		String title = getResources().getStringArray(R.array.url_title)[Integer
				.valueOf(value)];
		urlIndexPref.setSummary(title);
	}
}
