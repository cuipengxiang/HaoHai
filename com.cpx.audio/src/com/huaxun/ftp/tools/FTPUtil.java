package com.huaxun.ftp.tools;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.huaxun.ftp.DB.ServerFileDBUtil;
import com.huaxun.ftp.base.Constants;
import com.huaxun.ftp.base.ServerFile;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class FTPUtil {
	public static final String TAG = "FTPUtil";
	FTPClient mFtpClient;
	ArrayList<Map<String, String>> mQueue = new ArrayList<Map<String, String>>();
	ArrayList<String> uploadPathQueue = new ArrayList<String>();
	private Context context;
	private boolean isTransing;
	private ServerFileDBUtil serverDBUtil;
	private String operateType, filePath;

	public FTPUtil(Context context, ServerFileDBUtil serverDBUtil) {
		this.context = context;
		this.serverDBUtil = serverDBUtil;
	}

	/**
	 * 这里在外面有非ui线程来处理
	 * 
	 * @param host
	 *            ftp主机
	 * @param port
	 *            端口
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 */
	public FTPClient login(String host, int port, String username,
			String password) {
		if (null != mFtpClient)
			return mFtpClient;
		try {
			mFtpClient = new FTPClient();
			mFtpClient.connect(host, port);
			mFtpClient.login(username, password, null);
			return mFtpClient;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 注销
	 */
	public void logout() {
		if (null != mFtpClient) {
			try {
				mFtpClient.logout();
				mFtpClient.disconnect(true);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (FTPIllegalReplyException e) {
				e.printStackTrace();
			} catch (FTPException e) {
				e.printStackTrace();
			}
		}
	}

	Runnable uploadRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				transFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public FTPClient getFtpClient() {
		return mFtpClient;
	}

	public void setFile(Map<String, String> item) {
		mQueue.add(item);
	}

	public void setUploadPath(String uploadPath) {
		uploadPathQueue.add(uploadPath);
	}

	/**
	 * 开始传输
	 * 
	 * @return -1表示需要传输的文件列表为空，-2表示没有初始化FtpClient，0表示准备就绪，可以传输了
	 */
	public int preTransFile() {
		if (mQueue.size() < 1) {
			Log.e(TAG, "no file to trans.");
			isTransing = false;
			return -1;
		}

		if (null == mFtpClient || !mFtpClient.isConnected()) {
			Log.e(TAG, "mFtpClient is null or it's disconnected.");
			isTransing = false;
			return -2;
		}
		if (!isTransing) {
			Thread thread = new Thread(uploadRunnable);
			thread.start();
		}
		return 0;
	}

	/**
	 * 传输文件
	 * 
	 * @throws FTPAbortedException
	 * @throws FTPDataTransferException
	 * @throws FTPException
	 * @throws FTPIllegalReplyException
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws IllegalStateException
	 */
	private void transFile() throws IllegalStateException,
			FileNotFoundException, IOException, FTPIllegalReplyException,
			FTPException, FTPDataTransferException, FTPAbortedException {
		operateType = (String) mQueue.get(0).keySet().toArray()[0];
		filePath = mQueue.get(0).get(operateType);
		if (operateType.equals(Constants.TYPE_DOWNLOAD)) {
			File file = new File(Constants.workSpace
					+ filePath.substring(filePath.lastIndexOf("/") + 1,
							filePath.length()));
			mFtpClient
					.download(filePath, file, new MyFTPDataTransferListener());
		} else if (operateType.equals(Constants.TYPE_UPLOAD)) {
			File file = new File(filePath);
			if (file.exists()) {
				System.out.println("upload path ----> " + uploadPathQueue.get(0));
				mFtpClient.changeDirectory(uploadPathQueue.get(0));
				mFtpClient.upload(file, new MyFTPDataTransferListener());
			}
		}
	}

	public void abort() {
		if (null != mFtpClient) {
			try {
				mFtpClient.abortCurrentDataTransfer(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class MyFTPDataTransferListener implements FTPDataTransferListener {

		@Override
		public void transferred(int length) {
			System.out.println("upload length --->" + length);
		}

		@Override
		public void started() {
			System.out.println("transFile start--------------");
			isTransing = true;
		}

		@Override
		public void failed() {
			preTransFile();
		}

		@Override
		public void completed() {
			System.out.println("transFile completed-------------");
			mQueue.remove(0);
			if (operateType.equals(Constants.TYPE_DOWNLOAD))
				serverDBUtil.insertData(new ServerFile(Constants.workSpace
						+ filePath.substring(filePath.lastIndexOf("/") + 1),
						filePath.substring(0, filePath.lastIndexOf("/")),
						new Date().getTime() + ""));
			else if (operateType.equals(Constants.TYPE_UPLOAD)){
				serverDBUtil.updateServerFileInfo(filePath, new ServerFile(
						filePath, uploadPathQueue.get(0), new Date().getTime() + ""));
				uploadPathQueue.remove(0);
			}
			preTransFile();
		}

		@Override
		public void aborted() {
			System.out.println("transFile aborted-----------------");
			isTransing = false;
		}
	};

	public void destroy() {
		abort();
		logout();
		mFtpClient = null;
	}
}
