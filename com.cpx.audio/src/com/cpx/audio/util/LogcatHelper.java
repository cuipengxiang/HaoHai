package com.cpx.audio.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;
import android.os.Environment;

/**
 * log��־ͳ�Ʊ���
 * 
 * @author way
 * 
 */

public class LogcatHelper {

	private static LogcatHelper INSTANCE = null;
	private static String PATH_LOGCAT;
	private LogDumper mLogDumper = null;
	private int mPId;

	/**
	 * 
	 * ��ʼ��Ŀ¼
	 * 
	 * */
	public void init(Context context) {
		PATH_LOGCAT = "/sdcard/Logcat/";
		File file = new File(PATH_LOGCAT);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static LogcatHelper getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new LogcatHelper(context);
		}
		return INSTANCE;
	}

	private LogcatHelper(Context context) {
		init(context);
		mPId = android.os.Process.myPid();
	}

	public void start() {
		if (mLogDumper == null)
			mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
		mLogDumper.start();
	}

	public void stop() {
		if (mLogDumper != null) {
			mLogDumper.stopLogs();
			mLogDumper = null;
		}
	}

	private class LogDumper extends Thread {

		private Process logcatProc;
		private BufferedReader mReader = null;
		private boolean mRunning = true;
		String cmds = null;
		private String mPID;
		private FileOutputStream out = null;

		public LogDumper(String pid, String dir) {
			mPID = pid;
			try {
				File file = new File(dir, "Audio_" + MyDate.getDateEN() + ".log");
				if (!file.exists()) {
					file.createNewFile();
				}
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/**
			 * 
			 * ��־�ȼ���*:v , *:d , *:w , *:e , *:f , *:s
			 * 
			 * ��ʾ��ǰmPID����� E��W�ȼ�����־.
			 * 
			 * */

			// cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
			cmds = "logcat  | grep \"(" + mPID + ")\"";//��ӡ������־��Ϣ
			// cmds = "logcat -s way";//��ӡ��ǩ������Ϣ
			// cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

		}

		public void stopLogs() {
			mRunning = false;
		}

		@Override
		public void run() {
			try {
				logcatProc = Runtime.getRuntime().exec(cmds);
				mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
				String line = null;
				while (mRunning && (line = mReader.readLine()) != null) {
					if (!mRunning) {
						break;
					}
					if (line.length() == 0) {
						continue;
					}
					if (out != null && line.contains(mPID)) {
						out.write((MyDate.getDateEN() + "  " + line + "\n").getBytes());
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (logcatProc != null) {
					logcatProc.destroy();
					logcatProc = null;
				}
				if (mReader != null) {
					try {
						mReader.close();
						mReader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					out = null;
				}

			}

		}

	}

}