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
	 * ɾ����������ָ���ļ�
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
	 * ���������ϵ��ļ�����������
	 * */
	public static void renameFTPFile(Context context,
			final FTPClient ftpClient, final FTPFile file) {
		Builder builder = new AlertDialog.Builder(context);
		// ���öԻ���ı���
		builder.setTitle("������");
		final EditText edit = new EditText(context);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		edit.setHint(file.getName());
		builder.setView(edit);
		builder.setPositiveButton("ȷ��", new OnClickListener() {
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
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	/**
	 * ����
	 * */
	public static void cutFTPFile(SavePreferencesData savePreData,
			String cutFTPFileAbsolutePath) {
		savePreData.putStringData("cut", cutFTPFileAbsolutePath);
	}

	/**
	 * ճ��
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
	 * ��ʾ�ļ�����
	 * */
	public static void showFTPFileProperty(Context context, FTPFile file) {
		Builder builder = new AlertDialog.Builder(context);
		// ���öԻ���ı���
		builder.setTitle("����");
		String type = file.getType() == FTPFile.TYPE_DIRECTORY ? "�ļ���" : "�ļ�";
		builder.setMessage("�ļ���: " + file.getName() + " \n �ļ�����: " + type
				+ "\n �ļ���С�� " + file.getSize() + " �ֽ� \n �ϴ��޸�ʱ��: "
				+ file.getModifiedDate());
		builder.create().show();
	}

	/**
	 * ����ָ���ļ�
	 * */
	public static void downloadFTPFile(FTPUtil util, Map<String, String> item) {
		util.setFile(item);
		util.preTransFile();
	}

	/**
	 * �½��ļ���
	 * */
	public static void newFTPFileDirectory(Context context,
			final FTPClient ftpClient) throws IllegalStateException,
			IOException, FTPIllegalReplyException, FTPException {
		Builder builder = new AlertDialog.Builder(context);
		// ���öԻ���ı���
		builder.setTitle("�½��ļ���");
		final EditText edit = new EditText(context);
		edit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		builder.setView(edit);
		builder.setPositiveButton("ȷ��", new OnClickListener() {
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
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}
