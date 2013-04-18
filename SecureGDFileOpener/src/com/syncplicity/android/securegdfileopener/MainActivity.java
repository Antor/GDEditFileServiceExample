package com.syncplicity.android.securegdfileopener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppDetail;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
import com.good.gd.file.FileOutputStream;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;
import com.syncplicity.android.securegdfileopener.IntentHelper.OnGdAppSelectedListener;

public class MainActivity extends Activity {

	private static final String PATH_TO_TEST_TXT_FILE = "test.txt";
	private static final String DEFAULT_TEST_TXT_FILE_CONTENT = "Hello GD world!";

	private Button resetButton_;
	private Button editButton_;
	private TextView fileContent_;

	private AsyncTask<Void, Void, Void> lastStartedAsyncTask_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("SecureGDFileOpener", "com.syncplicity.android.securegdfileopener.MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		resetButton_ = (Button) findViewById(R.id.reset_button);
		editButton_ = (Button) findViewById(R.id.edit_button);
		fileContent_ = (TextView) findViewById(R.id.file_content);

		// While app not authorized through GD library - block UI
		resetButton_.setEnabled(false);
		editButton_.setEnabled(false);

		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				switch (event.getEventType()) {
				case GDAppEventAuthorized:
					loadTestFileContentFromDiskAsync(false);
					break;
				case GDAppEventNotAuthorized:
					// If app not authorized through GD library - block UI
					resetButton_.setEnabled(false);
					editButton_.setEnabled(false);
					break;
				case GDAppEventPolicyUpdate:
					break;
				case GDAppEventRemoteSettingsUpdate:
					break;
				case GDAppEventServicesUpdate:
					break;
				}
			}
		});

		try {
			GDServiceClient.setServiceClientListener( new GDServiceClientListener() {

				@Override
				public void onReceiveMessage (String application, Object params, String[] attachments, String requestID) {
					Log.d("SecureGDFileOpener", String.format("Received message %s",
							params));
				}

				@Override
				public void onMessageSent (String application, String requestID, String[] attachments) {
					Log.d("SecureGDFileOpener", String.format("Sent message to application=%s",
							application));
				}
			} );
		} catch (GDServiceException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (lastStartedAsyncTask_ != null && !lastStartedAsyncTask_.isCancelled()) {
			lastStartedAsyncTask_.cancel(false);
		}
	}

	public void onResetTestFileClicked(View v) {
		loadTestFileContentFromDiskAsync(true);
	}

	public void onEditStubFileClicked(View v) {
		final String SERVICE_ID = "com.good.gdservice.edit-file";
		final String SERVICE_VERSION = "1.0.0.0";
		List<GDAppDetail> availableAppsToEditFileIn = GDAndroid.getInstance().getApplicationDetailsForService(SERVICE_ID, SERVICE_VERSION);
		IntentHelper.showChooser(this, "Choose app:", availableAppsToEditFileIn, new OnGdAppSelectedListener() {

			@Override
			public void onGdAppSelectedListener(GDAppDetail gdAppDetail) {
				// Hack to find out class name of launch activity. Cause we cannot use just android package name for application parameter
				Intent intent = getPackageManager().getLaunchIntentForPackage(gdAppDetail.getApplicationId());
				String className = intent.getComponent().getClassName();

				try {
					String application = gdAppDetail.getApplicationId() + ".IccReceivingActivity";//className;
					String service = SERVICE_ID; // id of service
					String version = SERVICE_VERSION; // version of service
					String method = "editFile"; // name method of service which we want to call
					Map<String, Object> params = new HashMap<String, Object>();
					String[] attachments = new String[] { PATH_TO_TEST_TXT_FILE }; // Paths to files inside GD secure storages
					String requestID = GDServiceClient.sendTo(application, service, version, method, params,
							attachments, GDICCForegroundOptions.PreferPeerInForeground);
				} catch (GDServiceException e) {
					Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void loadTestFileContentFromDiskAsync(final boolean resetToDefault) {
		lastStartedAsyncTask_ = new AsyncTask<Void, Void, Void>() {

			private IOException exception_ = null;
			private String fileContentString_;

			@Override
			protected void onPreExecute() {
				resetButton_.setEnabled(false);
				editButton_.setEnabled(false);
			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					File testFile = new File(PATH_TO_TEST_TXT_FILE);
					if (resetToDefault || !testFile.exists()) {
						createTextFile(testFile, DEFAULT_TEST_TXT_FILE_CONTENT);
					}
					fileContentString_ = readTextFile(testFile);
				} catch (IOException e) {
					exception_ = e;
				}
				return null;
			}

			private void createTextFile(File file, String content) throws IOException {
				String parent = file.getParent();
				if (parent != null) {
					File parentfile = new File(parent);
					parentfile.mkdirs();
				}

				byte[] data = content.getBytes("UTF-8");
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
					out.write(data);
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// Do nothing
						}
					}
				}
			}

			private String readTextFile(File testFile) throws IOException {
				// Load file content on screen
				List<Byte> stringAsBytesList = new ArrayList<Byte>();

				InputStream inputStream = null;

				try {
					inputStream = new FileInputStream(testFile);

					byte[] buffer = new byte[1024];
					int byteRead;
					while ((byteRead = inputStream.read(buffer)) != -1) {
						for (int i = 0; i < byteRead; i++) {
							stringAsBytesList.add(buffer[i]);
						}
					}

					byte[] stringAsBytesArray = new byte[stringAsBytesList.size()];
					for (int i = 0; i < stringAsBytesList.size(); i++) {
						stringAsBytesArray[i] = stringAsBytesList.get(i);
					}
					String fileContent = new String(stringAsBytesArray, "UTF-8");
					return fileContent;
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							// Do nothing
						}
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				if (exception_ != null) {
					Toast.makeText(getApplicationContext(), "Error while init: " + exception_.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					fileContent_.setText(fileContentString_);
				}
				resetButton_.setEnabled(true);
				editButton_.setEnabled(true);
			}
		};
		lastStartedAsyncTask_.execute();
	}
}
