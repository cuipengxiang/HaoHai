package com.cpx.audio.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDate {
	public static String getFileName() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;
	}

	public static String getDateEN() {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		Date date = new Date(System.currentTimeMillis());
		String date1 = format1.format(date);
		return date1;
	}

}
