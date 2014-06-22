package com.huaxun.ftp.tools;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

public class Util {
	/**
	 * 获取本地IP地址ַ
	 * 
	 * @return
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress
									.getHostAddress())) {
						System.out.println(inetAddress.getHostAddress()
								.toString());
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("NewWorkUtil", ex.toString());
		}
		return null;
	}

	/**
	 * 获取FTP服务器路径ַ
	 * */
	public static String getFtpServerIpAddress() {
		String localIpAddress = getLocalIpAddress();
		if(localIpAddress==null)return "172.16.4.2";
		return localIpAddress.substring(0, localIpAddress.lastIndexOf(".") + 1) + 209;
	}

	/**
	 * 将字符串编码格式转成GB2312
	 * 
	 * @param str
	 * @return
	 */
	public static String TranEncodeTOGB(String str) {
		try {
			String strEncode = getEncoding(str);
			String temp = new String(str.getBytes(strEncode), "GBK");
			return temp;
		} catch (java.io.IOException ex) {
			return "";
		}
	}

	/**
	 * 判断字符串的编码
	 * 
	 * @param str
	 * @return
	 */
	public static String getEncoding(String str) {
		String encode = "GB2312";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s = encode;
				return s;
			}
		} catch (Exception exception) {
		}
		encode = "ISO-8859-1";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s1 = encode;
				return s1;
			}
		} catch (Exception exception1) {
		}
		encode = "UTF-8";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s2 = encode;
				return s2;
			}
		} catch (Exception exception2) {
		}
		encode = "GBK";
		try {
			if (str.equals(new String(str.getBytes(encode), encode))) {
				String s3 = encode;
				return s3;
			}
		} catch (Exception exception3) {
		}
		return "GBK";
	}

	/**
	 * 时间格式化
	 * */
	public static String formatDate(Object date) {
		Date myDate = null;
		if (date instanceof String) {
			myDate = new Date((String) date);
		} else if (date instanceof Date) {
			myDate = (Date) date;
		}else if(date instanceof Long){
			myDate=new Date((Long)date);
		}
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return formate.format(myDate);
	}
}
