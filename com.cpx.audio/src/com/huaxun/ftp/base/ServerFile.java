package com.huaxun.ftp.base;

/**
 * �洢������ļ�bean
 * */
public class ServerFile {
	/*���ش洢λ��*/
	private String localPath;
	/*���ļ��ڷ���˵�·��*/
	private String serverPath;
	/*���ļ��ڷ���˵�����޸�ʱ��*/
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
