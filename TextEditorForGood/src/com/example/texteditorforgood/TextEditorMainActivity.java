package com.example.texteditorforgood;

import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.file.File;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class TextEditorMainActivity extends Activity {

	public static String TAG = "TextEditorForGood";

	private static boolean isEditFileServiceWasSetUp_ = false;

	private Button saveButton_;
	private Button cancelButton_;
	private EditText fileContent_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!isEditFileServiceWasSetUp_) {
			setupEditFileService();
			isEditFileServiceWasSetUp_ = true;
		}


		setContentView(R.layout.ac_text_editor_main);

		saveButton_ = (Button) findViewById(R.id.save_button);
		cancelButton_ = (Button) findViewById(R.id.cancel_button);
		fileContent_ = (EditText) findViewById(R.id.file_content);

		// Block interface until file will be opened from another app
		saveButton_.setEnabled(false);
		cancelButton_.setEnabled(false);
		fileContent_.setEnabled(false);

		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
					//
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		if (lastOpenedFile != null) {
			try {
				String fileContentString = GDFileUtils.readTextFile(lastOpenedFile);
				fileContent_.setText(fileContentString);
				fileContent_.setEnabled(true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		if (lastOpenedFile != null) {
			try {
				String fileContentString = fileContent_.getText().toString();
				lastOpenedFile.delete();
				GDFileUtils.createTextFile(lastOpenedFile, fileContentString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setupEditFileService() {
		try {
			GDService.setServiceListener(new GDServiceListener() {
				@Override
				public void onReceiveMessage(String application, String service, String version, String method,
						Object params, String[] attachments, String requestID) {
					Log.d(TextEditorMainActivity.TAG, "GDService.onReceiveMessage(application=" + application +
							", service=" + service +", version=" + version + ", method=" + method + ", params=" + params +
							", attachments=" + attachments + ", requestID=" + requestID + ")");
					SharedPreferencesManager.setLastOpenedFile(getApplicationContext(), new File(attachments[0]), null);
					try {
						Object emptyResponse = null;
						GDService.replyTo(application, emptyResponse, GDICCForegroundOptions.PreferPeerInForeground,
								new String[0], requestID);
					} catch (GDServiceException e) {
						Log.e(TextEditorMainActivity.TAG, "Error while sending response: " + e.getMessage(), e);
					}
				}

				@Override
				public void onMessageSent(String application, String requestID, String[] attachments) {
					Log.d(TextEditorMainActivity.TAG, "GDService.onMessageSent(application=" + application + ", requestID=" + requestID + ", attachments=" + attachments + ")");
				}
			});
		} catch (GDServiceException e) {
			Log.e(TextEditorMainActivity.TAG, "Error while initializing GDService: " + e.getMessage(), e);
		}
	}

	public void onSaveAndSendFileBackClicked(View v) {
		// TODO implementation
	}

	public void onCancelFileEditingClicked(View v) {
		// TODO implementation
	}
}
