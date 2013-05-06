package com.syncplicity.android.testsharefile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppDetail;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.file.File;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class ShareFileMainActivity extends Activity {

	public static String TAG = "ShareFileMainActivity";

	private static final String TRANSFER_FILE_SERVICE_ID = "com.good.gdservice.transfer-file";
	private static final String TRANSFER_FILE_SERVICE_VERSION_1_0_0_0 = "1.0.0.0";
	private static final String TRANSFER_FILE_METHOD = "transferFile";

	private static boolean isEditFileServiceWasSetUp_ = false;
	private static boolean isAppAuthorized_ = false;

	private TextView lastSharedFileName_;
	private boolean isResumed_ = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_share_file_main);

		lastSharedFileName_ = (TextView) findViewById(R.id.last_shared_file_name);

		if (!isEditFileServiceWasSetUp_) {
			try {
				GDService.setServiceListener(new GDServiceListener() {

					@Override
					public void onReceiveMessage(String application, String service, String version, String method,
							Object params, final String[] attachments, String requestID) {
						if (service.equals(TRANSFER_FILE_SERVICE_ID)
								&& version.equals(TRANSFER_FILE_SERVICE_VERSION_1_0_0_0)
								&& method.equals(TRANSFER_FILE_METHOD)) {
							if (attachments.length == 1) {
								SharedPreferencesManager.setPathToLastSharedFile(getApplicationContext(), attachments[0]);
								if (isResumed_) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											lastSharedFileName_.setText(new File(attachments[0]).getName());
										}
									});
								}
							}
						}
					}

					@Override
					public void onMessageSent(String arg0, String arg1, String[] arg2) {
						// Do nothing
					}
				});
			} catch (GDServiceException e) {
				Log.e(ShareFileMainActivity.TAG, "Error while initializing GDService: " + e.getMessage(), e);
			}
			isEditFileServiceWasSetUp_ = true;
		}

		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
					isAppAuthorized_ = true;
					final String path = SharedPreferencesManager.getPathToLastSharedFile(getApplicationContext());
					if (isResumed_ && path != null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								lastSharedFileName_.setText(new File(path).getName());
							}
						});
					}
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		isResumed_ = true;
		if (isAppAuthorized_) {
			String path = SharedPreferencesManager.getPathToLastSharedFile(getApplicationContext());
			if (path != null) {
				lastSharedFileName_.setText(new File(path).getName());
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isResumed_ = false;
	}

	public void onShareFileClicked(View v) {

		// TODO
		// 1. Create stub file to send if not exists
		// 2. Determine to which app to share file and show user list to shoose from if needed

		String path = "";
		GDAppDetail gdAppDetail = new GDAppDetail("com.syncplicity.android", "com.syncplicity.android.gdsupport.IccReceivingActivity", "2.3.0.0");

		try {
			GDServiceClient.setServiceClientListener(new GDServiceClientListener() {
				@Override
				public void onReceiveMessage(String application, Object params, String[] attachments, String requestID) {
					// Do nothing
				}

				@Override
				public void onMessageSent(String application, String requestID, String[] attachments) {
					// Do nothing
				}
			});

			//"com.syncplicity.android.IccReceivingActivity" if "gdAppDetail.getAddress()" doesn't work
			GDServiceClient.sendTo(gdAppDetail.getAddress(), TRANSFER_FILE_SERVICE_ID, TRANSFER_FILE_SERVICE_VERSION_1_0_0_0,
					TRANSFER_FILE_METHOD, null, new String[] { path },
					GDICCForegroundOptions.PreferPeerInForeground);
		} catch (GDServiceException e) {
			Log.e(TAG, "Error while sending request: " + e.getMessage(), e);
			Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
