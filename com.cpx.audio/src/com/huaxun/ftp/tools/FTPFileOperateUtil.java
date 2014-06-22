package com.huaxun.ftp.tools;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPFile;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.IOException;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;

public class FTPFileOperateUtil {
	/**
	 * 删除服务器上指定文件
	 * 
	 * @throws FTPException
	 * @throws FTPIllegalReplyException
	 * @throws IOException
	 * @throws IllegalStateException
	 * */
	public static void removeFTPFile(FTPClient ftpClient,
			String removeFilePath, boolean isDirectory)
			throws IllegalStateException, IOException,
			FTPIllegalReplyException, FTPException {
		if (isDirectory) {
			ftpClient.deleteDirectory(removeFilePath);
			return;
		}
		ftpClient.deleteFile(removeFilePath);
	}

	/**
	 * 给服务器上的文件进行重命名
	 * */
	public static void renameFTPFile(Context context,
			final FTPClient ftpClient, final FTPFile file) {
		Builder builder = new AlertDialog.Builder(context);
		// 设置对话框的标题
		builder.setTitle("重命名");
		final EditText edit = new EditText(context);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		edit.setHint(file.getName());
		builder.setView(edit);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (edit.getText() != null && !"".equals(edit.getText())) {
					try {
						ftpClient.rename(file.getName(), edit.getText()
								.toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * 剪切
	 * */
	public static void cutFTPFile(SavePreferencesData savePreData,
			String cutFTPFileAbsolutePath) {
		savePreData.putStringData("cut", cutFTPFileAbsolutePath);
	}

	/**
	 * 粘贴
	 * 
	 * @throws FTPException
	 * @throws FTPIllegalReplyException
	 * @throws IOException
	 * @throws IllegalStateException
	 * */
	public static void pasteFTPFile(SavePreferencesData savePreData,
			FTPClient ftpClient, String newPath) throws IllegalStateException,
			IOException, FTPIllegalReplyException, FTPException {
		String cutPath = savePreData.getStringData("cut");
		if (!"".equals(cutPath)) {
			ftpClient.rename(
					cutPath,
					newPath
							+ "/"
							+ cutPath.substring(cutPath.lastIndexOf("/") + 1,
									cutPath.length()));
			savePreData.clear();
		}
	}

	/**
	 * 显示文件属性
	 * */
	public static void showFTPFileProperty(Context context, FTPFile file) {
		Builder builder = new AlertDialog.Builder(context);
		// 设置对话框的标题
		builder.setTitle("属性");
		String type = file.getType() == FTPFile.TYPE_DIRECTORY ? "文件夹" : "文件";
		builder.setMessage("文件名: " + file.getName() + " \n 文件类型: " + type
				+ "\n 文件大小： " + file.getSize() + " 字节 \n 上次修改时间: "
				+ file.getModifiedDate());
		builder.create().show();
	}

	/**
	 * 下载指定文件
	 * */
	public static void downloadFTPFile(FTPUtil util, Map<String, String> item) {
		util.setFile(item);
		util.preTransFile();
	}

	/**
	 * 新建文件夹
	 * */
	public static void newFTPFileDirectory(Context context,
			final FTPClient ftpClient) throws IllegalStateException,
			IOException, FTPIllegalReplyException, FTPException {
		Builder builder = new AlertDialog.Builder(context);
		// 设置对话框的标题
		builder.setTitle("新建文件夹");
		final EditText edit = new EditText(context);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		builder.setView(edit);
		builder.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (edit.getText() != null && !"".equals(edit.getText())) {
					try {
						ftpClient.createDirectory(edit.getText().toString());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		builder.setNegativeButton("取消", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}
