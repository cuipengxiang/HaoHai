package com.huaxun.ftp.base;

import android.annotation.SuppressLint;

public class Constants {
	/**
	 * 工作空间
	 * */
	@SuppressLint("SdCardPath")
	public static final String workSpace="/sdcard/record/";
	/**
	 * 上传
	 * */
	public static final String TYPE_UPLOAD="TYPE_UPLOAD";
	/**
	 * 上传
	 * */
	public static final String TYPE_DOWNLOAD="TYPE_DOWNLOAD";
	/**
	 * 默认上传路径
	 * */
	public static final String DEFAULT_UPLOAD_PATH="/sd/play/";
	/**
	 * 默认下载路径
	 * */
	public static final String DEFAULT_DOWNLOAD_PATH="/sd/record/";
}
