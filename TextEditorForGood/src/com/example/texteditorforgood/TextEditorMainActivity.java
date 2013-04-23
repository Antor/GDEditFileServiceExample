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
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceException;

public class TextEditorMainActivity extends Activity {

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

		if (!isEditFileServiceWasSetUp_) {
			TextEditorGDServiceListener gdServiceListener = TextEditorGDServiceListener.getInstance(this);
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
		// TODO implementation
	}

	public void onCancelFileEditingClicked(View v) {
		// TODO implementation
	}
}
