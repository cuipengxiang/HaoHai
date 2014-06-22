package com.huaxun.ftp.DB;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huaxun.ftp.base.ServerFile;

public class ServerFileDBUtil {
	private Context context = null;
	private SQLiteDatabase db = null;
	private final String dbName = "serverInfo.db";

	public ServerFileDBUtil(Context context) {
		this.context = context;
		// 得到数据库
		db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		// 创建表appTrends，如果不存在。
		db.execSQL("CREATE TABLE if not exists serverFile(localPath VARCHAR,serverPath VARCHAR,modificationOnServer VARCHAR)");
		db.close();
	}

	/**
	 * 插入一条数据
	 * */
	public void insertData(ServerFile serverFile) {
		db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		try {
			db.execSQL("insert into serverFile values(?,?,?)", new String[] {
					serverFile.getLocalPath(), serverFile.getServerPath(),
					serverFile.getModificationOnServer() });
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();
		}
	}

	/**
	 * 按本地存储路径查询否则查询全部
	 * */
	public List<ServerFile> queryByLocalPath(String localPath) {
		db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		Cursor cursor = null;
		List<ServerFile> list = new ArrayList<ServerFile>();
		if (localPath == null)
			cursor = db.rawQuery("select * from serverFile", null);
		else
			cursor = db.rawQuery("select * from serverFile where localPath=?",
					new String[] { localPath });
		if (cursor != null) {
			int count = cursor.getCount();
			cursor.moveToFirst();
			try {
				for (int i = 0; i < count; i++) {
					ServerFile serverFile = new ServerFile();
					serverFile.setLocalPath(cursor.getString(0));
					serverFile.setServerPath(cursor.getString(1));
					serverFile.setModificationOnServer(cursor.getString(2));
					list.add(serverFile);
					cursor.moveToNext();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
				db.close();
			}
		}
		return list;
	}

	/**
	 * 按条件删除记录否则删除全部
	 * */
	public void deleteByLocalPath(String localPath) {
		db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		if (localPath == null)
			db.execSQL("delete from serverFile");
		else
			db.execSQL("delete from serverFile where localPath=?",
					new String[] { localPath });
		db.close();
	}

	/**
	 * 更新记录，如果记录不存在则退出
	 * */
	public void updateServerFileInfo(String localPath, ServerFile serverFile) {
		if (localPath == null || serverFile == null
				|| queryByLocalPath(localPath).size() == 0)
			return;
		db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
		db.execSQL(
				"update serverFile set localPath=?, serverPath=?, modificationOnServer=? where localPath=?",
				new String[] { serverFile.getLocalPath(),
						serverFile.getServerPath(),
						serverFile.getModificationOnServer(), localPath });
		db.close();
	}
}
