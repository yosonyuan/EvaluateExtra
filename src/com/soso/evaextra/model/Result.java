package com.soso.evaextra.model;

import java.io.Serializable;
import java.text.DecimalFormat;

import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import android.os.Parcel;
import android.os.Parcelable;


public class Result implements Serializable, Parcelable {
	private static final DecimalFormat df = new DecimalFormat("#.000000");

	public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {

		@Override
		public Result createFromParcel(Parcel source) {
			return new Result(source);
		}

		@Override
		public Result[] newArray(int size) {
			return new Result[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(lat);
		dest.writeString(lng);
		dest.writeString(time);
		dest.writeString(radius);
		dest.writeString(offSet);
		dest.writeString(data);

		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeFloat(accurancy);

		dest.writeString(error);
		dest.writeString(reason);
		dest.writeDouble(distance);
		dest.writeDouble(speed);
		dest.writeString(provider);
	}

	public void readFromParcel(Parcel in) {
		lat = in.readString();
		lng = in.readString();
		time = in.readString();
		radius = in.readString();
		offSet = in.readString();
		data = in.readString();

		latitude = in.readDouble();
		longitude = in.readDouble();
		accurancy = in.readFloat();

		error = in.readString();
		reason = in.readString();
		distance = in.readDouble();
		speed = in.readDouble();
		provider = in.readString();
	}

	public Result(Parcel in) {
		readFromParcel(in);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String lat;
	private String lng;
	private String time;
	private String radius;
	private String offSet;
	private String data;
	private String imei;

	public double latitude;
	public double longitude;
	public float accurancy;
	public double distance;
	public double speed;
	private String provider;

	/**
	 * 错误码
	 */
	private String error;
	/**
	 * 错误描述
	 */
	private String reason;

	private String key;

	public Result(String key) {
		this.key = key;
	}

	public String getLat() {
		return lat;
	}
	public Result setLat(double lat) {
		latitude = lat;
		this.lat = df.format(lat) + "";
		return this;
	}
	public String getLng() {
		return lng;
	}
	public Result setLng(double lng) {
		longitude = lng;
		this.lng = df.format(lng) + "";
		return this;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getRadius() {
		return radius;
	}
	public Result setRadius(float radius) {
		accurancy = (int) radius;
		this.radius = (int) radius + "";
		return this;
	}
	public String getOffSet() {
		return offSet;
	}
	public void setOffSet(String offSet) {
		this.offSet = offSet;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		return time + ",\t" + reason + ",\t响应码=" + error + ",\t" + lat + ",\t" + lng + ",\t"
				+ radius + ",\t" + imei;
	}
	
	public String toSingleString() {
		return time + "," + reason + ",响应码=" + error + "," + lat + "," + lng + ","
				+ radius + "," + imei;
	}
	
	
	

	/**
	 * 生成可用于 PointShow.exe 进行分析的数据
	 * @return
	 */
	public String toPointShowString() {
		return key + "|" + lat + "," + lng + "," + accurancy;
	}

	public LatLng toGeoPoint() {
		return new LatLng(latitude, longitude);
	}

}
