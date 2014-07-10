package com.cpx.audio.dummy;

public class CMD {
	public static final byte[] BEGIN_RECORD = {0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00};
	public static final byte[] END_RECORD = {0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00};
	public static final byte[] STOP_RECORD = {0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00};
	public static final byte[] RESTART_RECORD = {0x01, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00};
	public static final byte[] RENAME = {0x01, 0x00, 0x00, 0x00, 0x05, 0x00, 0x00, 0x00};
	public static final byte[] BEGIN_PLAY = {0x01, 0x00, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00};
	public static final byte[] END_PLAY = {0x01, 0x00, 0x00, 0x00, 0x07, 0x00, 0x00, 0x00};
	public static final byte[] STOP_PLAY = {0x01, 0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00};
	public static final byte[] RESTART_PLAY = {0x01, 0x00, 0x00, 0x00, 0x09, 0x00, 0x00, 0x00};
	
	public static final String sIDLE = "0100";
	public static final String sPL_NORM = "0200";
	public static final String sPL_PAUSE = "0300";
	public static final String sRC_NORM = "0400";
	public static final String sRC_PAUSE = "0500";
	public static final String sRC_STOP = "0600";
	
	public static final String RET_OK = "0100";
	public static final String RET_NODEF = "0200";
	public static final String RET_FILEERROR = "0300";
	public static final String RET_ERRTIME = "0400";
	public static final String RET_FMTERROR = "0500";
	public static final String RET_DEVICE = "0600";
	public static final String RET_MEM = "0700";
	public static final String RET_OTHER = "0800";
	
	public static final String sIDLE_STRING = "����";
	public static final String sPL_NORM_STRING = "��������";
	public static final String sPL_PAUSE_STRING = "������ͣ";
	public static final String sRC_NORM_STRING = "����¼��";
	public static final String sRC_PAUSE_STRING = "¼����ͣ";
	public static final String sRC_STOP_STRING = "¼��ֹͣ";
	
	public static final String RET_OK_STRING = "����ִ�гɹ�";
	public static final String RET_NODEF_STRING = "δ��������";
	public static final String RET_FILEERROR_STRING = "���ļ�����";
	public static final String RET_ERRTIME_STRING = "��������ʱ�����";
	public static final String RET_FMTERROR_STRING = "WAV�ļ���ʽ����";
	public static final String RET_DEVICE_STRING = "��Ƶ�豸����";
	public static final String RET_MEM_STRING = "�ڴ����";
	public static final String RET_OTHER_STRING = "��������";
}
