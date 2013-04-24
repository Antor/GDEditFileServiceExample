package com.example.filesholder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filesholder.GDAppDetailChooser.OnGdAppSelectedListener;
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

public class FilesHolderMainActivity extends Activity {

	public static String TAG = "FilesHolder";

	public static final String PATH_TO_TEST_TXT_FILE = "test.txt";
	private static final String DEFAULT_TEST_TXT_FILE_CONTENT = "Hello GD world!";

	private static final String EDIT_FILE_SERVICE_ID = "com.good.gdservice.edit-file";
	private static final String EDIT_FILE_SERVICE_VERSION = "1.0.0.0";
	private static final String EDIT_FILE_METHOD = "editFile";
	
	private static boolean isSaveEditedFileServiceWasSetUp_ = false;
	private static boolean isAppAuthorized_ = false;

	private Button resetButton_;
	private Button editButton_;
	private TextView fileContent_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_files_holder_main);

		resetButton_ = (Button) findViewById(R.id.reset_button);
		editButton_ = (Button) findViewById(R.id.edit_button);
		fileContent_ = (TextView) findViewById(R.id.file_content);
		
		FilesHolderGDServiceListener gdServiceListener = FilesHolderGDServiceListener.getInstance();	
		if (!isSaveEditedFileServiceWasSetUp_) {
			try {
				GDService.setServiceListener(gdServiceListener);
			} catch (GDServiceException e) {
				Log.e(TAG, "Error while initializing GDService: " + e.getMessage(), e);
			}
			isSaveEditedFileServiceWasSetUp_ = true;
		}
		

		// While authentication not passed disable UI
		resetButton_.setEnabled(false);
		editButton_.setEnabled(false);

		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
					isAppAuthorized_ = true;
					loadTestFileContent();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isAppAuthorized_) {
			loadTestFileContent();
		}
	}

	public void onResetTestFileClicked(View v) {
		File file = new File(PATH_TO_TEST_TXT_FILE);
		if (file.exists()) {
			file.delete();
		}
		try {
			GDFileUtils.createTextFile(file, DEFAULT_TEST_TXT_FILE_CONTENT);
			String fileContentString = GDFileUtils.readTextFile(file);
			fileContent_.setText(fileContentString);
		} catch (IOException e) {
			file.delete();
			Toast.makeText(this, "Error while resetting file: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
		editButton_.setEnabled(file.exists());
	}

	public void onEditStubFileClicked(View v) {
		// Find out all available applications to edit file
		List<GDAppDetail> availableAppsToEditFileIn = GDAndroid.getInstance().getApplicationDetailsForService(
				EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION);
		removeUnavailableAppsFromList(availableAppsToEditFileIn);

		if (availableAppsToEditFileIn.isEmpty()) {
			Toast.makeText(getApplicationContext(), "There are no apps to edit file!", Toast.LENGTH_LONG).show();
		} else if (availableAppsToEditFileIn.size() == 1) {
			openFileInApp(PATH_TO_TEST_TXT_FILE, availableAppsToEditFileIn.get(0));
		} else {
			// Let user to choose application if there are more then one available
			GDAppDetailChooser chooser = new GDAppDetailChooser("Choose app to edit file...",
					availableAppsToEditFileIn, new OnGdAppSelectedListener() {
						@Override
						public void onGdAppSelectedListener(GDAppDetail gdAppDetail) {
							openFileInApp(PATH_TO_TEST_TXT_FILE, gdAppDetail);
						}
					});
			chooser.show(this);
		}
	}

	private void removeUnavailableAppsFromList(List<GDAppDetail> apps) {
		for (Iterator<GDAppDetail> iterator = apps.iterator(); iterator.hasNext();) {
			GDAppDetail gdAppDetail = iterator.next();

			String localtion = gdAppDetail.getAddress();
			int lastDotIndex = localtion.lastIndexOf(".");
			String pkg = localtion.substring(0, lastDotIndex);
			ComponentName iccActivityComponentName = new ComponentName(pkg, localtion);
			try {
				getPackageManager().getActivityInfo(iccActivityComponentName, 0);
			} catch (NameNotFoundException e) {
				iterator.remove();
			}
		}
	}

	private void openFileInApp(String path, GDAppDetail gdAppDetail) {
		try {
			GDServiceClient.setServiceClientListener(new GDServiceClientListener() {

				@Override
				public void onReceiveMessage(String application, Object params, String[] attachments, String requestID) {
					Log.d(TAG, "GDServiceClient.onReceiveMessage(application=" + application + ", params=" + params +
							", attachments=" + Arrays.toString(attachments) + ", requestID=" + requestID + ")");
				}

				@Override
				public void onMessageSent(String application, String requestID, String[] attachments) {
					Log.d(TAG, "GDServiceClient.onMessageSent(application=" + application + ", requestID=" + requestID + ", attachments=" + Arrays.toString(attachments) + ")");
				}
			});

			GDServiceClient.sendTo(gdAppDetail.getAddress(), EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION,
					EDIT_FILE_METHOD, new HashMap<String, Object>(), new String[] { path },
					GDICCForegroundOptions.PreferPeerInForeground);
		} catch (GDServiceException e) {
			Log.e(TAG, "Error while sending request: " + e.getMessage(), e);
			Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private void loadTestFileContent() {
		File file = new File(PATH_TO_TEST_TXT_FILE);
		if (!file.exists()) {
			try {
				GDFileUtils.createTextFile(file, DEFAULT_TEST_TXT_FILE_CONTENT);
			} catch (IOException e) {
				file.delete();
			}
		}
		try {
			String fileContentString = GDFileUtils.readTextFile(file);
			fileContent_.setText(fileContentString);
		} catch (IOException e) {
			// Do nothing
		}
		editButton_.setEnabled(file.exists());
		resetButton_.setEnabled(true);
	}
}
