package com.huaxun.ftp.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class SavePreferencesData {

	/*
	 * @author WuJianhua
	 * 
	 */
	private final static String TAG = "SavePreferencesData";
	private SharedPreferences mSharePreferences;
	private SharedPreferences.Editor mEditor;
	
	public SavePreferencesData(Context context) {
		mSharePreferences = context.getSharedPreferences(TAG, 0);
		mEditor = mSharePreferences.edit();
	}
	
	public SavePreferencesData(Context context,String preferencesName) {
		mSharePreferences = context.getSharedPreferences(preferencesName, 0);
		mEditor = mSharePreferences.edit();
	}
	
	/**
	 *  保存String类型的数据
	 * @param key 
	 * @param value
	 */
	public void putStringData(String key,String value) {
		mEditor.putString(key, value);
		mEditor.commit();
	}
	
	/**
	 * 保留int类型的数据
	 * @param key
	 * @param value
	 */
	public void putIntegerData(String key,int value) {
		mEditor.putInt(key, value);
		mEditor.commit();
	}
	
	/**
	 * 根据key获取对应的值 (String)
	 * @param key
	 * @return
	 */
	public String getStringData(String key) {
		return mSharePreferences.getString(key, "");
	}
	
	/**
	 * 根据key获取对应的值 (int)
	 * @param key
	 * @return
	 */
	public int getIntegerData(String key) {
		return mSharePreferences.getInt(key, 0);
	}
	
	/**
	 * 删除全部数据
	 * @param key
	 */
	public void deleteKey(String key) {
		mEditor.remove(key);
		mEditor.commit();
	}

	/**
	 * 获得数据长度
	 * @return
	 */
	public int getSize() {
		int size = mSharePreferences.getAll().size();
		return size;
	}
	
	/**
	 * 删除配置文件中所有数�?
	 */
	public void clear() {
		mEditor.clear();
		mEditor.commit();
	}
}
