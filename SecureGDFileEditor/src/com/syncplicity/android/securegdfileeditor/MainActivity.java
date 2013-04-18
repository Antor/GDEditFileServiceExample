package com.syncplicity.android.securegdfileeditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceException;
import com.syncplicity.android.securegdfileeditor.SecureGDFileEditorGDServiceListener.OnOpenFileToEditListener;

public class MainActivity extends Activity implements OnOpenFileToEditListener {

	private EditText fileContent_;

	private File openedFile_;
	private byte[] openedFileIdentificationData_;
	private String openedFileFromAplication_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("SecureGDFileOpener", "com.syncplicity.android.securegdfileeditor.MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		fileContent_ = (EditText) findViewById(R.id.file_content);

		try {
			SecureGDFileEditorGDServiceListener gdServiceListener = SecureGDFileEditorGDServiceListener
					.getInstance();
			gdServiceListener.setOnOpenFileToEditListener_(MainActivity.this);
			GDService.setServiceListener(gdServiceListener);
		} catch (GDServiceException e) {
			e.printStackTrace();
		}
	}


	public void onGenerateStubFileClicked(View v) {
		// TODO Implementation
	}

	public void onEditStubFileClicked(View v) {
		// TODO Implementation
	}

	@Override
	public void onOpenFileToEdit(final File file, final byte[] identificationData, final String fromAplication) {

		new AsyncTask<Void, Void, Void>() {

			private IOException exception_ = null;
			private String fileContentString_;

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Void doInBackground(Void... params) {
				try {
					fileContentString_ = readTextFile(file);
				} catch (IOException e) {
					e.printStackTrace();
					exception_ = e;
				}
				return null;
			}

			private String readTextFile(File file) throws IOException {
				// Load file content on screen
				List<Byte> stringAsBytesList = new ArrayList<Byte>();

				InputStream inputStream = null;

				try {
					inputStream = new FileInputStream(file);

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
							e.printStackTrace();
							// Do nothing
						}
					}
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				if (exception_ != null) {
					Toast.makeText(getApplicationContext(), "Error while reading file content: " + exception_.getMessage(),
							Toast.LENGTH_LONG).show();
				} else {
					openedFile_ = file;
					openedFileIdentificationData_ = identificationData;
					openedFileFromAplication_ = fromAplication;
					fileContent_.setText(fileContentString_);
				}
			}
		}.execute();
	}
}
