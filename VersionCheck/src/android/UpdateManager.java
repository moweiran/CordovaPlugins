package com.txw.plugin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class UpdateManager {
	private static final int DOWNLOAD = 1;
	private static final int DOWNLOAD_FINISH = 2;
	private static final int HAVE_NEW_VERSION_FINISH = 3;
	private static final int NOT_HAVE_NEW_VERSION_FINISH = 4;
	private HashMap<String, String> mHashMap;
	private String mSavePath;
	private int progress;
	private boolean cancelUpdate = false;

	private Activity mContext;
	private ProgressBar mProgress;
	private Dialog mDownloadDialog;
	private Handler mNewVersionhandler;
	private Boolean needCancel = false;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case DOWNLOAD:
				mProgress.setProgress(progress);
				break;
			case DOWNLOAD_FINISH:
				installApk();
				break;
			case HAVE_NEW_VERSION_FINISH:
				if (needCancel)
					return;

				if (mNewVersionhandler != null) {
					mNewVersionhandler.sendEmptyMessage(0);
				}

				showNoticeDialog();
				break;
			case NOT_HAVE_NEW_VERSION_FINISH:
				break;
			default:
				break;
			}
		}
	};

	public UpdateManager(Activity context, Handler newVersionhandler) {
		this.mContext = context;
		mNewVersionhandler = newVersionhandler;
	}

	public void checkUpdate(String updatePath, String packageInfo) {
		new VersionThread(updatePath, packageInfo).start();
	}

	public void cancelCheck() {
		needCancel = true;

	}

	private class VersionThread extends Thread {
		private String versoinPath;
		private String packageInfo;

		public VersionThread(String path, String info) {
			versoinPath = path;
			packageInfo = info;
		}

		@Override
		public void run() {
			try {
				getUpdateInfo(versoinPath, packageInfo);
			} catch (Exception ex) {
			}
		}
	}

	@SuppressLint("NewApi")
	private void getUpdateInfo(String updatePath, String packageInfo) {
		int versionCode = getVersionCode(mContext, packageInfo);

		URL url = null;
		try {
			url = new URL(updatePath);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		InputStream inStream = null;

		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
		// .detectNetwork().penaltyLog().build());
		// if (android.os.Build.VERSION.SDK_INT > 10) // 2.3.3
		// {
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		// .detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		// }

		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setReadTimeout(3 * 1000);
			if (conn.getResponseCode() == 200) {
				inStream = conn.getInputStream();
			}
		} catch (IOException e1) {
			mHandler.sendEmptyMessage(NOT_HAVE_NEW_VERSION_FINISH);
			return;
		}

		if (inStream == null) {
			mHandler.sendEmptyMessage(NOT_HAVE_NEW_VERSION_FINISH);
			return;
		}

		ParseXmlService service = new ParseXmlService();
		try {
			if (needCancel)
				return;

			mHashMap = service.parseXml(inStream);
			if (null != mHashMap) {
				int serviceCode = Integer.valueOf(mHashMap.get("version"));
				if (serviceCode > versionCode) {
					mHandler.sendEmptyMessage(HAVE_NEW_VERSION_FINISH);
					return;
				}
			}
		} catch (Exception e) {
			mHandler.sendEmptyMessage(NOT_HAVE_NEW_VERSION_FINISH);
			return;
		}

		mHandler.sendEmptyMessage(NOT_HAVE_NEW_VERSION_FINISH);
	}

	private int getVersionCode(Context context, String packageInfo) {
		int versionCode = 0;
		try {
			versionCode = context.getPackageManager().getPackageInfo(packageInfo, 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	private void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);

		builder.setTitle(this.mContext.getResources().getIdentifier("soft_update_title", "string",
				this.mContext.getPackageName()));

		builder.setMessage(this.mContext.getResources().getIdentifier("soft_update_info", "string",
				this.mContext.getPackageName()));

		builder.setPositiveButton(this.mContext.getResources().getIdentifier("soft_update_updatebtn", "string",
				this.mContext.getPackageName()), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showDownloadDialog();
					}
				});

		builder.setNegativeButton(this.mContext.getResources().getIdentifier("soft_update_later", "string",
				this.mContext.getPackageName()), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						if (mNewVersionhandler != null) {
							mNewVersionhandler.sendEmptyMessage(1);
						}
					}
				});
		Dialog noticeDialog = builder.create();
		noticeDialog.setCancelable(false);
		noticeDialog.show();
	}

	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setCancelable(false);

		builder.setTitle(
				this.mContext.getResources().getIdentifier("soft_updating", "string", this.mContext.getPackageName()));
		final LayoutInflater inflater = LayoutInflater.from(mContext);
		int progressLayout = this.mContext.getResources().getIdentifier("softupdate_progress", "layout",
				this.mContext.getPackageName());
		View v = inflater.inflate(progressLayout, null);
		int progressId = this.mContext.getResources().getIdentifier("update_progress", "id",
				this.mContext.getPackageName());
		mProgress = (ProgressBar) v.findViewById(progressId);
		builder.setView(v);

		builder.setNegativeButton(this.mContext.getResources().getIdentifier("soft_update_cancel", "string",
				this.mContext.getPackageName()), new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						cancelUpdate = true;
						if (mNewVersionhandler != null) {
							mNewVersionhandler.sendEmptyMessage(1);
						}
					}
				});

		mDownloadDialog = builder.create();
		mDownloadDialog.show();
		downloadApk();
	}

	private void downloadApk() {
		new downloadApkThread().start();
	}

	private class downloadApkThread extends Thread {
		@Override
		public void run() {
			try {
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					String sdpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
							+ "/";
					mSavePath = sdpath + "download";
					URL url = new URL(mHashMap.get("url"));
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.connect();
					int length = conn.getContentLength();
					InputStream is = conn.getInputStream();

					File file = new File(mSavePath);

					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(mSavePath, mHashMap.get("name") + ".apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;

					byte buf[] = new byte[1024];

					do {
						int numread = is.read(buf);
						count += numread;

						progress = (int) (((float) count / length) * 100);

						mHandler.sendEmptyMessage(DOWNLOAD);
						if (numread <= 0) {

							mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
							break;
						}

						fos.write(buf, 0, numread);
					} while (!cancelUpdate);
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			mDownloadDialog.dismiss();
		}
	}

	private void installApk() {

		File apkfile = new File(mSavePath, mHashMap.get("name") + ".apk");
		if (!apkfile.exists()) {
			return;
		}
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			// 判读版本是否在7.0以上
			Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".application.provider",
					apkfile);
			// 在AndroidManifest中的android:authorities值
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			install.setDataAndType(apkUri, "application/vnd.android.package-archive");
			mContext.startActivity(install);
		} else {
			Intent install = new Intent(Intent.ACTION_VIEW);
			install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			install.setDataAndType(Uri.parse("file://" + apkfile.toString()),
					"application/vnd.android.package-archive");
			mContext.startActivity(install);
		}

	}
}
