package com.huaxun.ftp.base;

/**
 * 存储服务端文件bean
 * */
public class ServerFile {
	/*本地存储位置*/
	private String localPath;
	/*该文件在服务端的路径*/
	private String serverPath;
	/*该文件在服务端的最后修改时间*/
	private String modificationOnServer;

	public ServerFile() {
		super();
	}	
	public ServerFile(String localPath, String serverPath,
			String modificationOnServer) {
		super();
		this.localPath = localPath;
		this.serverPath = serverPath;
		this.modificationOnServer = modificationOnServer;
	}

	public String getLocalPath() {
		return localPath;
	}
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}
	public String getServerPath() {
		return serverPath;
	}
	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}
	public String getModificationOnServer() {
		return modificationOnServer;
	}
	public void setModificationOnServer(String modificationOnServer) {
		this.modificationOnServer = modificationOnServer;
	}
	
}
