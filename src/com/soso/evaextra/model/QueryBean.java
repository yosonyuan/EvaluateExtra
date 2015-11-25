package com.soso.evaextra.model;

import java.io.Serializable;

public class QueryBean implements Serializable{
	
	private String query;
	private String timetag;
	private String time_imei;
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String getTimetag() {
		return timetag;
	}
	public void setTimetag(String timetag) {
		this.timetag = timetag;
	}
	public String getTime_imei() {
		return time_imei;
	}
	public void setTime_imei(String timeImei) {
		time_imei = timeImei;
	}
	@Override
	public String toString() {
//		return "{\"query\":" +query+",\"timetag\":"+timetag+"}";
		return query +"々々々" +timetag + "々々々" + time_imei; 
	}

}
