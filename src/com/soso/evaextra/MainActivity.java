package com.soso.evaextra;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import cn.edu.hust.cm.common.app.WidgetUtils;

public class MainActivity extends ListActivity implements OnItemClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setAdapter(
				new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, new String[] {
								"模拟GPS位置", "SDK对比测试", "腾讯SDK测试" }));
		getListView().setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (position) {
		case 0:
			WidgetUtils.toast(this, "模拟GPS位置");
			break;
		case 1:
			startActivity(new Intent(this, LocationTestActivity.class));
			break;
		case 2:
			WidgetUtils.toast(this, "腾讯定位SDK测试");
			break;

		default:
			break;
		}
	}
}
