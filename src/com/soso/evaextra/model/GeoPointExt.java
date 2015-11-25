package com.soso.evaextra.model;

import com.tencent.tencentmap.mapsdk.maps.model.LatLng;


public class GeoPointExt {
	final int latitude;
	final int longitude;
	final float accuracy;

	private LatLng mGeoPoint;

	/**
	 * Title:
	 * Description: 带精度的点 单位为微度
	 * @param latitudeE6
	 * @param longitudeE6
	 * @param accuracy
	 */
	public GeoPointExt(int latitudeE6, int longitudeE6, float accuracy) {
		super();
		this.latitude = latitudeE6;
		this.longitude = longitudeE6;
		this.accuracy = accuracy;
	}

	/**
	 * 
	* <p>Title: </p> 
	* <p>Description: 带精度的点 单位为度</p> 
	* @param latitude
	* @param longitude
	* @param accuracy
	 */
	public GeoPointExt(double latitude, double longitude, float accuracy) {
		super();
		this.latitude = (int) (latitude * 1E6);
		this.longitude = (int) (longitude * 1E6);
		this.accuracy = accuracy;
	}
	/**
	 * @Title: to
	 * @Description: 转换为经纬度坐标
	 * @return GeoPoint
	 */
	public LatLng to() {
		if (mGeoPoint == null) {
			mGeoPoint = new LatLng(latitude, longitude);
		}
		return mGeoPoint;
	}
}
