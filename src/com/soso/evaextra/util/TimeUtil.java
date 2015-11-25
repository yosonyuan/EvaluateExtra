package com.soso.evaextra.util;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeUtil {

	public static String format(long millis) {
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds %= 60;
		minutes %= 60;

		String h = hours < 10 ? ("0" + hours) : ("" + hours);
		String m = minutes < 10 ? ("0" + minutes) : ("" + minutes);
		String s = seconds < 10 ? ("0" + seconds) : ("" + seconds);
		return h + ":" + m + ":" + s;
	}

	// 日期操作
	public static Date getDate() {
		Calendar c = Calendar.getInstance();
		int theYear = c.get(Calendar.YEAR);
		int theMonth = c.get(Calendar.MONTH);
		int theDay = c.get(Calendar.DAY_OF_MONTH);
		return new Date(theYear, theMonth, theDay);
	}

	// 时间操作
	public static String getTime() {
		Calendar c = Calendar.getInstance();
		int theHour = c.get(Calendar.HOUR_OF_DAY);
		int theMinute = c.get(Calendar.MINUTE);
		int theSecond = c.get(Calendar.SECOND);
		return (new Time(theHour, theMinute, theSecond)).toString();
	}

	public static String getCurTime() {
		Calendar c = Calendar.getInstance();
		int theYear = c.get(Calendar.YEAR);
		int theMonth = c.get(Calendar.MONTH) + 1;
		int theDay = c.get(Calendar.DAY_OF_MONTH);
		return theYear+"-"+theMonth + "-" + theDay;
	}

	// 将毫秒转为日期
	public static String millsToStr(String timeMills) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.valueOf(timeMills));
		String str = formatter.format(calendar.getTime());

		return str;
	}
	
	public static String millsToStrList(String timeMills) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.valueOf(timeMills));
		String str = formatter.format(calendar.getTime());

		return str;
	}

	// 将日期转为毫秒
	public static long datetomills(String mills) {
		try {
			SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			long millionSeconds = sim.parse(mills).getTime();
			return millionSeconds;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	// 计算天数
		public static String parsemills(long insertmillionSeconds,
				long currentmillionSeconds) {
			String time = "";
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

				Date insertdate = new Date(Long.valueOf(insertmillionSeconds));
				String[] inserttimes = format.format(insertdate).split("-");

				Date currentdate = new Date(Long.valueOf(currentmillionSeconds));
				String[] currenttimes = format.format(currentdate).split("-");

				String insertyear = inserttimes[0];
				String insertmonth = inserttimes[1];
				String insertday = inserttimes[2];
				String insertHours = inserttimes[3];
				String insertmintues = inserttimes[4];

				String currentyear = currenttimes[0];
				String currentmonth = currenttimes[1];
				String currentday = currenttimes[2];

				if (insertyear.equals(currentyear)) {
					if (insertmonth.equals(currentmonth)) {
						if (insertday.equals(currentday)) {
							time = insertHours + ":" + insertmintues;
						} else {
							time = insertmonth + "/" + insertday;
						}
					} else {
						time = insertmonth + "/" + insertday;
					}
				} else {
					time = insertyear + "/" + insertmonth + "/" + insertday;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			return time;
		}

	public static String time0(int x) {
		String t = "";
		if (x >= 0 && x < 10) {
			t = "0" + x;
		} else {
			t = x + "";
		}
		return t;
	}

	public static long parsemillsToday(long insertmillionSeconds,
			long currentmillionSeconds) {
		long day = 0L;
		try {
			long commillionSeconds = currentmillionSeconds
					- insertmillionSeconds;
			day = commillionSeconds / (24 * 60 * 60 * 1000);
			if(day<=0)
			{
				day=1;
			}
			else
			{
				day=day+1;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return day;
	}

}
