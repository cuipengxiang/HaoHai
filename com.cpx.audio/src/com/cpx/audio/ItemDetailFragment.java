package com.cpx.audio;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cpx.audio.dummy.CMD;
import com.cpx.audio.dummy.DummyContent;
import com.cpx.audio.util.FileUtil;
import com.huaxun.ftp.DB.ServerFileDBUtil;
import com.huaxun.ftp.base.Constants;
import com.huaxun.ftp.base.ServerFile;
import com.huaxun.ftp.tools.FTPFileOperateUtil;
import com.huaxun.ftp.tools.FTPUtil;

/**
 * A fragment representing a single Item detail screen. This fragment is either
 * contained in a {@link ItemListActivity} in two-pane mode (on tablets) or a
 * {@link ItemDetailActivity} on handsets.
 */
@SuppressLint({ "SdCardPath", "HandlerLeak" })
public class ItemDetailFragment extends Fragment implements Runnable {
	private ListView fileListView;
	private File currentParent;
	private File[] currentFiles;
	public LayoutInflater mInflater;
	private TextView currentRoute;
	private TextView currentFile;
	private Button beginRecord;
	private Button endRecord;
	private Button stopRecord;
	private Button reStartRecord;
	private Button beginPlay;
	private Button endPlay;
	private Button stopPlay;
	private Button reStartPlay;
	private File selectedFile;

	final String SAVE_PATH = "save";
	final String SAVE_PATH_KEY = "save_key";
	final String SEND_PATH = "send";
	final String SEND_PATH_KEY = "send_key";

	String HOST = "192.168.10.1";
	int SOCKET_PORT = 5000;
	String FTP_HOST = "192.168.10.1";
	int FTP_PORT = 21;

	SharedPreferences savePreferences;
	SharedPreferences sendPreferences;

	myFileListAdapter fileListAdapter;

	Thread mSocketThread;
	Socket mSocket;
	private BufferedReader in = null;
	private OutputStream out = null;
	String socketBack;

	private FTPClient ftpClient;
	private FTPUtil ftpUtil;
	private ServerFileDBUtil serverDBUtil;
	private String FTP_UserName = "audio";
	private String FTP_PassWord = "audio";

	private boolean uploadcompleted = false;
	private boolean hasConfirmed = false;

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private DummyContent.DummyItem mItem;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ItemDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mInflater = getLayoutInflater(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}

		File file = new File("/sdcard/config.txt");
		if (file.exists()) {
			String fileString = FileUtil.readFile("/sdcard/config.txt");
			String socket_config = fileString.split("\n")[0];
			String ftp_config = fileString.split("\n")[1];

			HOST = socket_config.split(" ")[0];
			SOCKET_PORT = Integer.valueOf(socket_config.split(" ")[1]);

			FTP_HOST = ftp_config.split(" ")[0];
			FTP_PORT = Integer.valueOf(ftp_config.split(" ")[1]);
			FTP_UserName = ftp_config.split(" ")[2];
			FTP_PassWord = ftp_config.split(" ")[3];
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = null;
		savePreferences = getActivity().getSharedPreferences(SAVE_PATH, 0);
		sendPreferences = getActivity().getSharedPreferences(SEND_PATH, 0);
		// Show the dummy content as text in a TextView.

		mSocketThread = null;
		mSocketThread = new Thread(this);
		mSocketThread.start();

		if (mItem != null && mItem.id.equals("1")) {
			rootView = inflater.inflate(R.layout.fragment_item_detail_record, container, false);
			createRecordView(rootView);
		} else if (mItem != null && mItem.id.equals("2")) {
			rootView = inflater.inflate(R.layout.fragment_item_detail_play, container, false);
			createPlayView(rootView);
		} else if (mItem != null && mItem.id.equals("3")) {
			rootView = inflater.inflate(R.layout.fragment_item_detail_setting, container, false);
			createSettingView(rootView);
		}

		return rootView;
	}

	public void notifyFileListChanged() {
		currentFiles = currentParent.listFiles();
		selectedFile = null;

		if (currentFiles == null) {
			currentParent = currentParent.getParentFile();
			currentFiles = currentParent.listFiles();
			Toast.makeText(getActivity(), "无法打开", Toast.LENGTH_SHORT).show();
		} else {
			if (currentRoute != null) {
				currentRoute.setText(currentParent.getAbsolutePath());
			}
			if (currentFile != null) {
				currentFile.setText("");
			}

			List<Map<String, Object>> listDatas = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < currentFiles.length; i++) {
				Map<String, Object> data = new HashMap<String, Object>();
				if (currentFiles[i].isDirectory()) {
					data.put("icon", R.drawable.icon_folder);
				} else {
					data.put("icon", R.drawable.icon_file);
				}

				data.put("name", currentFiles[i].getName());

				listDatas.add(data);
			}
			fileListAdapter.setDatas(listDatas);
			fileListAdapter.notifyDataSetChanged();
		}

	}

	public void notifyDialogFileListChanged(myDialogFileListAdapter adapter, TextView dialogText) {
		currentFiles = currentParent.listFiles();

		if (currentFiles == null) {
			currentParent = currentParent.getParentFile();
			currentFiles = currentParent.listFiles();
			Toast.makeText(getActivity(), "无法打开", Toast.LENGTH_SHORT).show();
		} else {
			dialogText.setText("当前路径：" + currentParent.getAbsolutePath());
			List<Map<String, Object>> listDatas = new ArrayList<Map<String, Object>>();
			Map<String, Object> data0 = new HashMap<String, Object>();
			data0.put("icon", R.drawable.folder_back);
			data0.put("name", "../返回上一级");
			listDatas.add(data0);
			for (int i = 0; i < currentFiles.length; i++) {
				Map<String, Object> data = new HashMap<String, Object>();
				if (currentFiles[i].isDirectory()) {
					data.put("icon", R.drawable.icon_folder);
				} else {
					data.put("icon", R.drawable.icon_file);
				}

				data.put("name", currentFiles[i].getName());

				listDatas.add(data);
			}
			adapter.setDatas(listDatas);
			adapter.notifyDataSetChanged();
		}
	}

	public void showPathSelectDialog(final TextView textView) {
		LinearLayout dialogLayout = (LinearLayout) mInflater.inflate(R.layout.dialog_view, null);
		ListView dialogList = (ListView) dialogLayout.findViewById(R.id.setting_dialog_list);
		final TextView dialogText = (TextView) dialogLayout.findViewById(R.id.setting_dialog_path);

		String path = "/mnt/sdcard";
		if (textView.getId() == R.id.setting_save_edit) {
			path = savePreferences.getString(SAVE_PATH_KEY, "/mnt/sdcard/record");
		} else if (textView.getId() == R.id.setting_send_edit) {
			path = sendPreferences.getString(SEND_PATH_KEY, "/mnt/sdcard/play");
		}

		File rootFile = new File(path);
		final myDialogFileListAdapter adapter = new myDialogFileListAdapter();
		currentParent = rootFile;
		currentFiles = rootFile.listFiles();
		dialogText.setText("当前路径：" + currentParent.getAbsolutePath());

		List<Map<String, Object>> listDatas = new ArrayList<Map<String, Object>>();
		Map<String, Object> data0 = new HashMap<String, Object>();
		data0.put("icon", R.drawable.folder_back);
		data0.put("name", "../返回上一级");
		listDatas.add(data0);
		for (int i = 0; i < currentFiles.length; i++) {
			Map<String, Object> data = new HashMap<String, Object>();
			if (currentFiles[i].isDirectory()) {
				data.put("icon", R.drawable.icon_folder);
			} else {
				data.put("icon", R.drawable.icon_file);
			}

			data.put("name", currentFiles[i].getName());

			listDatas.add(data);
		}

		adapter.setDatas(listDatas);
		dialogList.setAdapter(adapter);
		dialogList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (position == 0) {
					if (currentParent.getParentFile() == null) {
						Toast.makeText(getActivity(), "无上级目录", Toast.LENGTH_SHORT).show();
						return;
					}
					currentParent = currentParent.getParentFile();
					notifyDialogFileListChanged(adapter, dialogText);
				} else {
					if (currentFiles[position - 1].isDirectory()) {
						currentParent = currentFiles[position - 1];
						notifyDialogFileListChanged(adapter, dialogText);
					}
				}
			}
		});

		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle("选择路径");
		builder.setView(dialogLayout);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor editor = null;
				if (textView.getId() == R.id.setting_save_edit) {
					editor = savePreferences.edit();
					editor.putString(SAVE_PATH_KEY, currentParent.getAbsolutePath());
					editor.commit();
				} else if (textView.getId() == R.id.setting_send_edit) {
					editor = sendPreferences.edit();
					editor.putString(SEND_PATH_KEY, currentParent.getAbsolutePath());
					editor.commit();
				}

				textView.setText(currentParent.getAbsolutePath());
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();

	}

	/*
	 * 创建设置View
	 */
	public void createSettingView(View rootView) {
		String path = null;
		Button setting_save = (Button) rootView.findViewById(R.id.setting_save_button);
		final TextView savePath = (TextView) rootView.findViewById(R.id.setting_save_edit);
		path = savePreferences.getString(SAVE_PATH_KEY, "/mnt/sdcard/record");
		savePath.setText(path);
		setting_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showPathSelectDialog(savePath);
			}
		});
		Button setting_send = (Button) rootView.findViewById(R.id.setting_send_button);
		final TextView sendPath = (TextView) rootView.findViewById(R.id.setting_send_edit);
		path = sendPreferences.getString(SEND_PATH_KEY, "/mnt/sdcard/play");
		sendPath.setText(path);
		setting_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showPathSelectDialog(sendPath);
			}
		});
	}

	/*
	 * 创建 发送View
	 */
	public void createPlayView(View rootView) {
		String path = sendPreferences.getString(SEND_PATH_KEY, "/mnt/sdcard/play");
		File rootFile = new File(path);
		if (!rootFile.exists()) {
			rootFile.mkdir();
		}
		fileListAdapter = new myFileListAdapter();
		currentParent = rootFile;
		currentFiles = rootFile.listFiles();
		List<Map<String, Object>> listDatas = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < currentFiles.length; i++) {
			Map<String, Object> data = new HashMap<String, Object>();
			if (currentFiles[i].isDirectory()) {
				data.put("icon", R.drawable.icon_folder);
			} else {
				data.put("icon", R.drawable.icon_file);
			}

			data.put("name", currentFiles[i].getName());

			listDatas.add(data);
		}

		currentRoute = (TextView) rootView.findViewById(R.id.send_route_edit);
		currentRoute.setText(currentParent.getAbsolutePath());
		currentFile = (TextView) rootView.findViewById(R.id.play_file_edit);

		beginPlay = (Button) rootView.findViewById(R.id.begin_play_button);
		stopPlay = (Button) rootView.findViewById(R.id.stop_play_button);
		reStartPlay = (Button) rootView.findViewById(R.id.restart_play_button);
		endPlay = (Button) rootView.findViewById(R.id.end_play_button);
		beginPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (selectedFile == null) {
					Toast.makeText(getActivity(), "未选择文件", Toast.LENGTH_SHORT).show();
					return;
				}
				final File newFile = selectedFile;
				if (!newFile.exists()) {
					Toast.makeText(getActivity(), "本地未找到该文件", Toast.LENGTH_SHORT).show();
					return;
				} else {
					if (newFile.isDirectory()) {
						Toast.makeText(getActivity(), "不能对文件夹进行操作", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setMessage("确定选择：" + '\n' + newFile.getAbsolutePath() + "？");
				builder.setTitle("选择文件");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						FTPClientUpLoad(newFile);
						showPlayDialog(newFile);
						dialog.dismiss();
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
			}
		});

		endPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSocketCMD(CMD.END_PLAY);
			}
		});

		stopPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSocketCMD(CMD.STOP_PLAY);
			}
		});

		reStartPlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSocketCMD(CMD.RESTART_PLAY);
			}
		});

		ImageButton folder_back = (ImageButton) rootView.findViewById(R.id.send_folder_back);
		folder_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (currentParent.getParentFile() == null) {
					Toast.makeText(getActivity(), "无上级目录", Toast.LENGTH_SHORT).show();
				} else {
					currentParent = currentParent.getParentFile();
					notifyFileListChanged();
				}
			}
		});

		fileListView = (ListView) rootView.findViewById(R.id.send_file_list);
		fileListAdapter.setDatas(listDatas);
		fileListView.setAdapter(fileListAdapter);
		fileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (currentFiles[position].isDirectory()) {
					currentParent = currentFiles[position];
					notifyFileListChanged();
				} else if (currentFiles[position].isFile()) {
					view.setSelected(true);
					selectedFile = currentFiles[position];
					currentFile.setText(selectedFile.getName());
				} else {
					Toast.makeText(getActivity(), "无访问权限", Toast.LENGTH_SHORT).show();
				}
			}
		});
		fileListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				final int posi = position;
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setMessage("确认要删除" + currentFiles[position].getName() + "吗？");
				builder.setTitle("提示");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						delete(currentFiles[posi]);
						notifyFileListChanged();
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
				return false;
			}
		});
	}

	public void showPlayDialog(final File newFile) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage("确认要播放 " + newFile.getAbsolutePath() + "吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (uploadcompleted) {
					Log.d(">>>>>>>>>>>>>>", "upload was completed");
					byte[] filename = (Constants.DEFAULT_UPLOAD_PATH + newFile.getName()).getBytes();
					byte[] cmd = new byte[CMD.BEGIN_PLAY.length + filename.length];
					System.arraycopy(CMD.BEGIN_PLAY, 0, cmd, 0, CMD.BEGIN_PLAY.length);
					System.arraycopy(filename, 0, cmd, CMD.BEGIN_PLAY.length, filename.length);
					sendSocketCMD(cmd);
					uploadcompleted = false;
				} else {
					hasConfirmed = true;
				}
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}

	/*
	 * 创建接受View
	 */
	public void createRecordView(View rootView) {
		String path = savePreferences.getString(SAVE_PATH_KEY, "/mnt/sdcard/record");
		File rootFile = new File(path);
		if (!rootFile.exists()) {
			rootFile.mkdir();
		}
		fileListAdapter = new myFileListAdapter();
		currentParent = rootFile;
		currentFiles = rootFile.listFiles();
		List<Map<String, Object>> listDatas = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < currentFiles.length; i++) {
			Map<String, Object> data = new HashMap<String, Object>();
			if (currentFiles[i].isDirectory()) {
				data.put("icon", R.drawable.icon_folder);
			} else {
				data.put("icon", R.drawable.icon_file);
			}

			data.put("name", currentFiles[i].getName());

			listDatas.add(data);
		}

		currentRoute = (TextView) rootView.findViewById(R.id.route_edit);
		currentRoute.setText(currentParent.getAbsolutePath());

		beginRecord = (Button) rootView.findViewById(R.id.begin_record_button);
		stopRecord = (Button) rootView.findViewById(R.id.stop_record_button);
		reStartRecord = (Button) rootView.findViewById(R.id.restart_record_button);
		endRecord = (Button) rootView.findViewById(R.id.end_record_button);

		beginRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setMessage("确认将文件保存在" + currentRoute.getText() + "吗？");
				builder.setTitle("提示");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						sendSocketCMD(CMD.BEGIN_RECORD);
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
			}
		});

		stopRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSocketCMD(CMD.STOP_RECORD);
			}
		});

		reStartRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSocketCMD(CMD.RESTART_RECORD);
			}
		});

		endRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinearLayout editLayout = (LinearLayout) mInflater.inflate(R.layout.dialog_edit, null);
				final EditText fileName = (EditText) editLayout.findViewById(R.id.dialog_filename_edit);
				sendSocketCMD(CMD.END_RECORD);
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setMessage("请输入文件名(不需要输入扩展名)：");
				builder.setTitle("保存文件");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						byte[] filename = (Constants.DEFAULT_DOWNLOAD_PATH + fileName.getText().toString() + ".wav").getBytes();
						byte[] cmd = new byte[CMD.RENAME.length + filename.length];
						System.arraycopy(CMD.RENAME, 0, cmd, 0, CMD.RENAME.length);
						System.arraycopy(filename, 0, cmd, CMD.RENAME.length, filename.length);
						sendSocketCMD(cmd);
						dialog.dismiss();
						showFTPDialog(fileName.getText().toString() + ".wav");
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.setView(editLayout);

				builder.create().show();
			}
		});

		ImageButton folder_add = (ImageButton) rootView.findViewById(R.id.folder_add);
		folder_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText editText = new EditText(getActivity());
				editText.setHint("请输入文件夹名");

				new AlertDialog.Builder(getActivity()).setTitle("新建文件夹").setIcon(null).setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (editText.getText().toString().equals("")) {
							Toast.makeText(getActivity(), "请输入文件夹名", Toast.LENGTH_SHORT).show();
						} else {
							File newfolder = new File(currentParent, editText.getText().toString());
							if (newfolder.exists()) {
								Toast.makeText(getActivity(), "文件夹已存在", Toast.LENGTH_SHORT).show();
								return;
							}
							if (!newfolder.mkdir()) {
								Toast.makeText(getActivity(), "文件夹名不合法", Toast.LENGTH_SHORT).show();
							} else {
								notifyFileListChanged();
							}
						}
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).setView(editText).show();

			}
		});
		ImageButton folder_back = (ImageButton) rootView.findViewById(R.id.folder_back);
		folder_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (currentParent.getParentFile() == null) {
					Toast.makeText(getActivity(), "无上级目录", Toast.LENGTH_SHORT).show();
				} else {
					currentParent = currentParent.getParentFile();
					notifyFileListChanged();
				}
			}
		});

		fileListView = (ListView) rootView.findViewById(R.id.file_list);
		fileListAdapter.setDatas(listDatas);
		fileListView.setAdapter(fileListAdapter);
		fileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (currentFiles[position].isDirectory()) {
					currentParent = currentFiles[position];
					notifyFileListChanged();
				}
			}
		});
		fileListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				final int posi = position;
				AlertDialog.Builder builder = new Builder(getActivity());
				builder.setMessage("确认要删除" + currentFiles[position].getName() + "吗？");
				builder.setTitle("提示");
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						delete(currentFiles[posi]);
						notifyFileListChanged();
					}
				});

				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});

				builder.create().show();
				return false;
			}
		});
	}

	public void showFTPDialog(final String filename) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage("确认下载该文件吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						FTPClientDownLoad(filename);
					}
				}, 500);
			}
		});

		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.create().show();
	}
	
	public void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	public final class ViewHolder {
		public ImageView img;
		public TextView title;
		// public TextView info;
	}

	class myFileListAdapter extends BaseAdapter {
		List<Map<String, Object>> listDatas;

		public void setDatas(List<Map<String, Object>> listDatas) {
			this.listDatas = listDatas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listDatas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.file_list_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.item_img);
				holder.title = (TextView) convertView.findViewById(R.id.item_title);
				// holder.info = (TextView)
				// convertView.findViewById(R.id.item_info);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			holder.img.setBackgroundResource((Integer) listDatas.get(position).get("icon"));
			holder.title.setText((String) listDatas.get(position).get("name"));
			// holder.info.setText((String) mData.get(position).get("info"));

			return convertView;
		}

	}

	class myDialogFileListAdapter extends BaseAdapter {
		List<Map<String, Object>> listDatas;

		public void setDatas(List<Map<String, Object>> listDatas) {
			this.listDatas = listDatas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listDatas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return listDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder = null;
			if (convertView == null) {

				holder = new ViewHolder();

				convertView = mInflater.inflate(R.layout.file_list_item, null);
				holder.img = (ImageView) convertView.findViewById(R.id.item_img);
				holder.title = (TextView) convertView.findViewById(R.id.item_title);
				// holder.info = (TextView)
				// convertView.findViewById(R.id.item_info);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}

			holder.img.setBackgroundResource((Integer) listDatas.get(position).get("icon"));
			holder.title.setText((String) listDatas.get(position).get("name"));
			// holder.info.setText((String) mData.get(position).get("info"));

			return convertView;
		}

	}

	public void sendSocketCMD(final byte[] cmd) {
		if (mSocket != null && mSocket.isConnected()) {
			if (!mSocket.isOutputShutdown()) {
				try {
					out.write(cmd);
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			Toast.makeText(getActivity(), "连接中断，正在重新连接，请稍候重试", Toast.LENGTH_LONG).show();
			mSocketThread = null;
			mSocketThread = new Thread(this);
			mSocketThread.start();
		}
	}

	@Override
	public void run() {
		try {
			if (mSocket != null) {
				Log.d(">>>>>>>>>>>", "mSocket != null");
				mSocket.close();
				mSocket = null;
			}
			mSocket = new Socket(HOST, SOCKET_PORT);
			mSocket.setKeepAlive(true);
			in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			out = mSocket.getOutputStream();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		try {
			while (true) {
				if (mSocket.isConnected()) {
					if (!mSocket.isInputShutdown()) {
						if ((socketBack = in.readLine()) != null) {
							socketBack += "\n";
							mHandler.sendMessage(mHandler.obtainMessage());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			mSocket = null;
		}
	}

	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			
		};
	};

	public void FTPClientDownLoad(final String fileName) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				serverDBUtil = new ServerFileDBUtil(getActivity());
				ftpUtil = new FTPUtil(getActivity(), serverDBUtil);
				ftpClient = ftpUtil.login(FTP_HOST, FTP_PORT, FTP_UserName, FTP_PassWord);
				File workSpace = new File(Constants.workSpace);
				if (!workSpace.exists())
					workSpace.mkdirs();
				refreshDB();
				if (ftpClient != null) {
					try {
						ftpClient.changeDirectory(Constants.DEFAULT_DOWNLOAD_PATH);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (FTPIllegalReplyException e) {
						e.printStackTrace();
					} catch (FTPException e) {
						e.printStackTrace();
					}
					try {
						ftpClient.download(Constants.DEFAULT_DOWNLOAD_PATH + fileName, new File(currentRoute.getText().toString() + "/" + fileName),
								new FTPDataTransferListener() {

									@Override
									public void transferred(int length) {
										// TODO Auto-generated method stub

									}

									@Override
									public void started() {
										// TODO Auto-generated method stub

									}

									@Override
									public void failed() {
										// TODO Auto-generated method stub

									}

									@Override
									public void completed() {
										// TODO Auto-generated method stub
										try {
											ftpClient.disconnect(true);
										} catch (IllegalStateException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (FTPIllegalReplyException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (FTPException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}

									@Override
									public void aborted() {
										// TODO Auto-generated method stub

									}
								});
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FTPIllegalReplyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FTPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FTPDataTransferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (FTPAbortedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void FTPClientUpLoad(final File file) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				serverDBUtil = new ServerFileDBUtil(getActivity());
				ftpUtil = new FTPUtil(getActivity(), serverDBUtil);
				ftpClient = ftpUtil.login(FTP_HOST, FTP_PORT, FTP_UserName, FTP_PassWord);// "172.16.4.2",
																							// 21,
																							// "jiaodongav",
																							// "av1.jiaodong.net.cn");
				File workSpace = new File(Constants.workSpace);
				if (!workSpace.exists())
					workSpace.mkdirs();
				refreshDB();
				if (ftpClient != null) {
					try {
						ftpClient.changeDirectory(Constants.DEFAULT_UPLOAD_PATH);// "/TV/news/");
						ftpClient.upload(file, new FTPDataTransferListener() {

							@Override
							public void transferred(int length) {
								// TODO Auto-generated method stub

							}

							@Override
							public void started() {
								// TODO Auto-generated method stub

							}

							@Override
							public void failed() {
								// TODO Auto-generated method stub

							}

							@Override
							public void completed() {
								// TODO Auto-generated method stub
								Log.d(">>>>>>>>>>>>>", "update complete");
								if (hasConfirmed) {
									Log.d(">>>>>>>>>>>>>", "has Confirmed");
									byte[] filename = (Constants.DEFAULT_UPLOAD_PATH + file.getName()).getBytes();
									byte[] cmd = new byte[CMD.BEGIN_PLAY.length + filename.length];
									System.arraycopy(CMD.BEGIN_PLAY, 0, cmd, 0, CMD.BEGIN_PLAY.length);
									System.arraycopy(filename, 0, cmd, CMD.BEGIN_PLAY.length, filename.length);
									sendSocketCMD(cmd);
									hasConfirmed = false;
								} else {
									uploadcompleted = true;
									Log.d(">>>>>>>>>>>>>", "uploadcompleted = true");
								}
								try {
									ftpClient.disconnect(true);
								} catch (IllegalStateException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (FTPIllegalReplyException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (FTPException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void aborted() {
								// TODO Auto-generated method stub

							}
						});
					} catch (FTPDataTransferException e) {
						e.printStackTrace();
					} catch (FTPAbortedException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (FTPIllegalReplyException e) {
						e.printStackTrace();
					} catch (FTPException e) {
						e.printStackTrace();
					}

					/*
					 * Map<String, String> item = new HashMap<String, String>();
					 * item.put(Constants.TYPE_UPLOAD, file.getAbsolutePath());
					 * ftpUtil.setFile(item);
					 * ftpUtil.setUploadPath(Constants.DEFAULT_UPLOAD_PATH);
					 * ftpUtil.preTransFile();
					 */
				}
			}
		}).start();
	}

	/**
	 * 更新数据库，删除不存在的文件
	 * */
	public void refreshDB() {
		List<ServerFile> serverFiles = serverDBUtil.queryByLocalPath(null);
		File file = null;
		for (ServerFile serverFile : serverFiles) {
			file = new File(serverFile.getLocalPath());
			if (!file.exists())
				serverDBUtil.deleteByLocalPath(serverFile.getLocalPath());
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
