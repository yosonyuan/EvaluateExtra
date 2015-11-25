package com.soso.evaextra.model;
import java.io.Serializable;

public class DataBean implements Serializable{
	
	private String name;
	private double lat;
	private double lng;
	private double acc;
	private String time;
	private String imei;
	private String time_imei;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public double getAcc() {
		return acc;
	}
	public void setAcc(double acc) {
		this.acc = acc;
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getTime_imei() {
		return time_imei;
	}
	public void setTime_imei(String timeImei) {
		time_imei = timeImei;
	}
	@Override
	public String toString() {
//		return "\""+name+"\":{\"lat\":" +lat+",\"lng\":"+lng+",\"acc\":"+acc+",\"time\":"+"\""+time+"\""+",\"query\":"+imei+"}";
		return name +"," + lat +"," + lng +"," +acc +"," + time +"," + imei + "," + time_imei;
	}

}
