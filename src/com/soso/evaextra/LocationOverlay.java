package com.soso.evaextra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;

import com.soso.evaextra.model.GeoPointExt;
import com.soso.evaextra.model.Result;
import com.tencent.tencentmap.mapsdk.maps.Projection;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;


public class LocationOverlay{

	private final Bitmap mMarkerTencent;
	private final Bitmap mMarkerAmap;
	private final Bitmap mMarkerBd;
	private final Bitmap mMarkerSg;

	private final List<String> mKeys = new ArrayList<String>();
	private HashMap<String, Integer> mColorMap;
	private HashMap<String, Bitmap> mMarkerMap;

	final Paint paint = new Paint();

	private final AppContext mAppContext;

	private int mCount = 1;
	private final String mMyKey;
	
	public LocationOverlay(AppContext appContext, Bitmap tencent, Bitmap amap,
			Bitmap bd, Bitmap sg, String myKey) {
		mMyKey = myKey;
		addKey(myKey);

		mAppContext = appContext;
		mMarkerTencent = tencent;
		mMarkerAmap = amap;
		mMarkerBd = bd;
		mMarkerSg = sg;

		mColorMap = new HashMap<String, Integer>();
		mColorMap.put(AppContext.AMAP, Color.rgb(255, 0, 0));
		mColorMap.put(AppContext.BAIDU, Color.rgb(0, 255, 0));
		mColorMap.put(AppContext.TENCENT, Color.rgb(0, 0, 255));
		mColorMap.put(AppContext.SOGOU, Color.rgb(127, 127, 127));

		mMarkerMap = new HashMap<String, Bitmap>();
		mMarkerMap.put(AppContext.AMAP, mMarkerAmap);
		mMarkerMap.put(AppContext.BAIDU, mMarkerBd);
		mMarkerMap.put(AppContext.TENCENT, mMarkerTencent);
		mMarkerMap.put(AppContext.SOGOU, mMarkerSg);
		setShowAll(true);
	}

	public void setShowAll(boolean show) {
		String[] keys = AppContext.ALL_KEYS;
		if (show) {
			for (String k : keys) {
				if (!mKeys.contains(k)) {
					mKeys.add(k);
				}
			}
		} else {
			Iterator<String> it = mKeys.iterator();
			while (it.hasNext()) {
				String e = it.next();
				if (!mMyKey.equals(e)) {
					it.remove();
				}
			}

		}
		System.out.println(mKeys);
	}

	public boolean isShowAll() {
		return mKeys.containsAll(Arrays.asList(AppContext.ALL_KEYS));
	}

	public void addKey(String key) {
		mKeys.add(key);
	}

	public void removeKey(String key) {
		mKeys.remove(key);
	}

	public void draw(Canvas canvas, TencentMap tencentMap) {
		
		Projection mapProjection = tencentMap.getProjection();
		
		final LatLng latlng = new LatLng(0,0);
		for(String key : mKeys){
			List<Result> results = mAppContext.getLocations(key,
					AppContext.POINT_COUNT[mAppContext.getAppConfig()
					                       .getShowCountIndex()]);
			for(Result result : results){
				//skip(0,0)
				if(result.latitude < 0 && result.longitude < 0){
					continue;
				}
				latlng.latitude = result.latitude;
				latlng.longitude = result.longitude;
				drawPoint(canvas, mapProjection, latlng, mMarkerMap.get(key), mColorMap.get(key),tencentMap);
				tencentMap.addMarker(new MarkerOptions().position(latlng)).setIcon(BitmapDescriptorFactory.fromBitmap(mMarkerMap.get(key)));
			}
			mLastPoint = null;
		}

	}

	private void drawPoint(Canvas canvas, Projection mapProjection, LatLng g,
			Bitmap bmpMarker, int color , TencentMap tencentMap) {
		Point ptMap = mapProjection.toScreenLocation(g);
		paint.setColor(color);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(5.0f);
		paint.setAlpha(255);

		if (mAppContext.getAppConfig().isShowLine()) {
			if (mLastPoint != null) {
				tencentMap.addPolyline(new PolylineOptions().add(mapProjection.fromScreenLocation(mLastPoint) , mapProjection.fromScreenLocation(ptMap)).width(5.0f));
			}
			mLastPoint = ptMap;
		}
	}

	@SuppressWarnings("unused")
	private void drawPoint(Canvas canvas, Projection mapProjection,
			GeoPointExt g, Bitmap bmpMarker, int color) {
//		Point ptMap = mapProjection.toScreenLocation(new LatLng(g.to().getLatitudeE6(), g.to().getLongitudeE6()));
//		paint.setColor(color);
//		paint.setStyle(Style.STROKE);
//		paint.setStrokeWidth(5.0f);
//		paint.setAlpha(255);
//		//画在画布的中央
//		canvas.drawBitmap(bmpMarker, ptMap.x - bmpMarker.getWidth() / 2,
//				ptMap.y - bmpMarker.getHeight() / 2, paint);
//
//		if (mLastPoint != null) {
//			// 绘制连线
//			canvas.drawLine(mLastPoint.x, mLastPoint.y, ptMap.x, ptMap.y, paint);
//		}
//		mLastPoint = ptMap;
	}

	private Point mLastPoint;
	
}
