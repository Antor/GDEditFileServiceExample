package com.example.texteditorforgood;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.texteditorforgood.TextEditorGDServiceListener.OnOpenFileToEditListener;
import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.file.File;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;

public class TextEditorMainActivity extends Activity {
	
	private static final String SAVE_EDITED_FILE_SERVICE_ID = "com.good.gdservice.save-edited-file";
	private static final String SAVE_EDITED_FILE_SERVICE_VERSION = "1.0.0.1";
	private static final String SAVE_EDIT_METHOD = "saveEdit";
	private static final String RELEASE_EDIT_METHOD = "releaseEdit";

	public static String TAG = "TextEditorForGood";

	private static boolean isEditFileServiceWasSetUp_ = false;
	private static boolean isAppAuthorized_ = false;

	private Button saveButton_;
	private Button cancelButton_;
	private EditText fileContent_;
	
	private boolean resumed_ = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_text_editor_main);

		saveButton_ = (Button) findViewById(R.id.save_button);
		cancelButton_ = (Button) findViewById(R.id.cancel_button);
		fileContent_ = (EditText) findViewById(R.id.file_content);

		TextEditorGDServiceListener gdServiceListener = TextEditorGDServiceListener.getInstance(this);
		gdServiceListener.setOnOpenFileToEditListener(new OnOpenFileToEditListener() {
			@Override
			public void onFileToEditReceived() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (resumed_ && isAppAuthorized_) {
							loadLastOpenedFile();
						}
						
					}
				});
			}
		});
		
		if (!isEditFileServiceWasSetUp_) {
			try {
				GDService.setServiceListener(gdServiceListener);
			} catch (GDServiceException e) {
				Log.e(TextEditorMainActivity.TAG, "Error while initializing GDService: " + e.getMessage(), e);
			}
			isEditFileServiceWasSetUp_ = true;
		}

		// Block interface until file will be opened from another app
		saveButton_.setEnabled(false);
		cancelButton_.setEnabled(false);
		fileContent_.setEnabled(false);

		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
					isAppAuthorized_ = true;
					loadLastOpenedFile();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isAppAuthorized_) {
			loadLastOpenedFile();
		}
		resumed_ = true;
	}
	
	private void loadLastOpenedFile() {
		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		if (lastOpenedFile != null) {
			try {
				String fileContentString = GDFileUtils.readTextFile(lastOpenedFile);
				fileContent_.setText(fileContentString);
				
				saveButton_.setEnabled(true);
				cancelButton_.setEnabled(true);
				fileContent_.setEnabled(true);
			} catch (IOException e) {
				Log.e(TextEditorMainActivity.TAG, "Error while opening file: " + e.getMessage(), e);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isAppAuthorized_) {
			saveLastOpenedFileToDisk();
		}
		resumed_ = false;
	}
	
	private void saveLastOpenedFileToDisk() {
		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		if (lastOpenedFile != null) {
			try {
				String fileContentString = fileContent_.getText().toString();
				lastOpenedFile.delete();
				GDFileUtils.createTextFile(lastOpenedFile, fileContentString);
			} catch (IOException e) {
				Log.e(TextEditorMainActivity.TAG, "Error while saving file: " + e.getMessage(), e);
			}
		}
	}

	public void onSaveAndSendFileBackClicked(View v) {
		
		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		byte[] identificationData = SharedPreferencesManager.getLastOpenedFileIdentificationData(this);
		String applicationFileOpenedFrom = SharedPreferencesManager.getLastOpenedFileFromApplication(this);
		
		if (lastOpenedFile != null) {
			saveLastOpenedFileToDisk();
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
				Map<String, Object> params = new HashMap<String, Object>();
				if (identificationData != null) {
					params.put("identificationData", identificationData);
				}
				GDServiceClient.sendTo(applicationFileOpenedFrom, 
						SAVE_EDITED_FILE_SERVICE_ID, 
						SAVE_EDITED_FILE_SERVICE_VERSION, 
						SAVE_EDIT_METHOD, 
						params,
						new String[] { lastOpenedFile.getAbsolutePath() }, 
						GDICCForegroundOptions.PreferPeerInForeground);
			} catch (GDServiceException e) {
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}

	public void onCancelFileEditingClicked(View v) {
		File lastOpenedFile = SharedPreferencesManager.getLastOpenedFile(this);
		byte[] identificationData = SharedPreferencesManager.getLastOpenedFileIdentificationData(this);
		String applicationFileOpenedFrom = SharedPreferencesManager.getLastOpenedFileFromApplication(this);
		
		if (lastOpenedFile != null) {
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
				Map<String, Object> params = new HashMap<String, Object>();
				if (identificationData != null) {
					params.put("identificationData", identificationData);
				}
				GDServiceClient.sendTo(applicationFileOpenedFrom, 
						SAVE_EDITED_FILE_SERVICE_ID, 
						SAVE_EDITED_FILE_SERVICE_VERSION, 
						RELEASE_EDIT_METHOD, 
						params,
						new String[] { lastOpenedFile.getAbsolutePath() }, 
						GDICCForegroundOptions.PreferPeerInForeground);
				
				SharedPreferencesManager.clear(this);
				lastOpenedFile.delete();
				saveButton_.setEnabled(false);
				cancelButton_.setEnabled(false);
				fileContent_.setEnabled(false);
				fileContent_.setText("");
			} catch (GDServiceException e) {
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
}

