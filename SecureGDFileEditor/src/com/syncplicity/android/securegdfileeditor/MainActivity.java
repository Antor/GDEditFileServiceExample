package com.syncplicity.android.securegdfileeditor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.good.gd.Activity;
import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.syncplicity.android.securegdfileeditor.SecureGDFileEditorGDServiceListener.GDServiceMessage;
import com.syncplicity.android.securegdfileeditor.SecureGDFileEditorGDServiceListener.OnOpenFileToEditListener;

public class MainActivity extends Activity implements OnOpenFileToEditListener {

	private Button saveButton_;
	private Button cancelButton_;
	private EditText fileContent_;

	private File openedFile_;
	private byte[] openedFileIdentificationData_;
	private String openedFileFromAplication_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("SecureGDFileOpener", "com.syncplicity.android.securegdfileeditor.MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		saveButton_ = (Button) findViewById(R.id.save_button);
		cancelButton_ = (Button) findViewById(R.id.cancel_button);
		fileContent_ = (EditText) findViewById(R.id.file_content);

		saveButton_.setEnabled(false);
		cancelButton_.setEnabled(false);
		fileContent_.setEnabled(false);



		try {
			try {
				SecureGDFileEditorGDServiceListener gdServiceListener = SecureGDFileEditorGDServiceListener
						.getInstance();
				GDServiceMessage gdServiceMessage = gdServiceListener.getPendingGDServiceMessages().poll();
				if (gdServiceMessage != null) {
					SecureGDFileEditorGDServiceListener.getInstance().consumeReceivedMessage(
							gdServiceMessage.getApplication(), gdServiceMessage.getService(), gdServiceMessage.getVersion(), gdServiceMessage.getMethod(),
							gdServiceMessage.getParams(), gdServiceMessage.getAttachments(), gdServiceMessage.getRequestID(),  this);
				}
				gdServiceListener.setOnOpenFileToEditListener_(this);
				GDService.setServiceListener(gdServiceListener);
			} catch (GDServiceException e) {
				e.printStackTrace();
			}

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

	public void onSaveAndSendFileBackClicked(View v) {
		if (openedFile_ != null) {
			final String SERVICE_ID = "com.good.gdservice.save-edited-file";
			final String SERVICE_VERSION = "1.0.0.1";

			try {
				String application = openedFileFromAplication_;
				String service = SERVICE_ID;
				String version = SERVICE_VERSION;
				String method = "saveEdit";
				Map<String, Object> params = new HashMap<String, Object>();
				if (openedFileIdentificationData_ != null) {
					params.put("identificationData", openedFileIdentificationData_);
				}
				String[] attachments = new String[] { openedFile_.getAbsolutePath() };
				String requestID = GDServiceClient.sendTo(application, service, version, method, params,
						attachments, GDICCForegroundOptions.PreferPeerInForeground);
				finish();
			} catch (GDServiceException e) {
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	public void onCancelFileEditingClicked(View v) {
		if (openedFile_ != null) {
			final String SERVICE_ID = "com.good.gdservice.save-edited-file";
			final String SERVICE_VERSION = "1.0.0.1";

			try {
				String application = openedFileFromAplication_;
				String service = SERVICE_ID;
				String version = SERVICE_VERSION;
				String method = "releaseEdit";
				Map<String, Object> params = new HashMap<String, Object>();
				if (openedFileIdentificationData_ != null) {
					params.put("identificationData", openedFileIdentificationData_);
				}
				String[] attachments = new String[] {  };
				String requestID = GDServiceClient.sendTo(application, service, version, method, params,
						attachments, GDICCForegroundOptions.PreferPeerInForeground);
				finish();
			} catch (GDServiceException e) {
				Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onOpenFileToEdit(final File file, final byte[] identificationData, final String fromAplication) {

		new AsyncTask<Void, Void, Void>() {

			private IOException exception_ = null;
			private String fileContentString_;

			@Override
			protected void onPreExecute() {
				saveButton_.setEnabled(false);
				cancelButton_.setEnabled(false);
				fileContent_.setEnabled(false);
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

					saveButton_.setEnabled(true);
					cancelButton_.setEnabled(true);
					fileContent_.setEnabled(true);
				}
			}
		}.execute();
	}
}
