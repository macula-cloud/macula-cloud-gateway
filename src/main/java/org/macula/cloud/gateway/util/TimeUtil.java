package org.macula.cloud.gateway.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by linqina on 2018/12/19 3:35 PM.
 */
public class TimeUtil {

	/**
	 * 每日凌晨5点清除缓存
	 */
	private static final String expTime = " 05:00:00";

	/**
	 * 每30天清除JWT缓存
	 */
	private static final int expDay = 30;

	public static long expTimeInMillis() {
		long exp = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Calendar date = Calendar.getInstance();
			long currentTime = date.getTimeInMillis();
			date.set(Calendar.DATE, date.get(Calendar.DATE) + 1);
			String tomorrow = sdf.format(date.getTime()) + expTime;
			long tomorrowTime = sdf.parse(tomorrow).getTime();
			exp = tomorrowTime - currentTime;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return exp;
	}

	public static long expDayInMillis() {
		long exp = 0;
		Calendar date = Calendar.getInstance();
		long currentTime = date.getTimeInMillis();
		date.set(Calendar.DATE, date.get(Calendar.DATE) + expDay);
		long expDayTime = date.getTimeInMillis();
		exp = expDayTime - currentTime;
		return exp;
	}

	public static long expSecond() {
		return expTimeInMillis() / 1000;
	}

	public static int expHour() {
		return (int) (expTimeInMillis() / (60 * 60 * 1000));
	}

//    public static void main(String[] args) {
//        System.out.println(expTimeInMillis());
//        System.out.println(expSecond());
//        System.out.println(expHour());
//        System.out.println(expDayInMillis());
//    }
}
