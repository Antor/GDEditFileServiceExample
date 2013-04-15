package com.syncplicity.android.securegdfileopener;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppDetail;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.error.GDInitializationError;
import com.good.gd.file.File;
import com.good.gd.file.FileOutputStream;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class MainActivity extends Activity {
	
	private static final String PATH_TO_TEST_TXT_FILE = "test.txt";
	
	private Button resetButton_;
	private Button editButton_;
	private TextView fileContent_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		resetButton_ = (Button) findViewById(R.id.reset_button);
		editButton_ = (Button) findViewById(R.id.edit_button);
		fileContent_ = (TextView) findViewById(R.id.file_content);
		
		// While app not authorized through GD library - block UI
		resetButton_.setEnabled(false);
		editButton_.setEnabled(false);
		
		initializeGD();
		
	}

	private void initializeGD() throws GDInitializationError {
		GDAndroid.getInstance().authorize(new GDAppEventListener() {
			@Override
			public void onGDEvent(GDAppEvent event) {
				switch (event.getEventType()) {
				case GDAppEventAuthorized:
					
					File testFile = new File(PATH_TO_TEST_TXT_FILE);
					if (!testFile.exists()) {
						createStubTxtFile(PATH_TO_TEST_TXT_FILE, "Hello GD world!");
					}
					// Load file content on screen
					FileReader fileReader = null;
					try {
						fileReader = new FileReader(testFile);
						StringBuilder fileContentBuilder = new StringBuilder();
						char nextChar;
						while ((nextChar = (char) fileReader.read()) != -1) {
							fileContentBuilder.append(nextChar);
						}
						fileContent_.setText(fileContentBuilder.toString());
					} catch (IOException e) {
						Toast.makeText(getApplicationContext(), "Error while init: " + e.getMessage(), Toast.LENGTH_LONG).show();
					} finally {
						if (fileReader != null) {
							try {
								fileReader.close();
							} catch (IOException e) {
								// Do nothing
							}
						}
					}
					
					resetButton_.setEnabled(true);
					editButton_.setEnabled(true);
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
					Log.d("SecureGDFileOpener", "GDServiceClientListener.onReceiveMessage()");
				}
				
				@Override
				public void onMessageSent (String application, String requestID, String[] attachments) {
					Log.d("SecureGDFileOpener", "GDServiceClientListener.onMessageSent()");
					
				}
			} );
			GDService.setServiceListener(new GDServiceListener() {

				@Override
				public void onReceiveMessage(String application, String service, String version, String method,
						Object params, String[] attachments, String requestID) {
					Log.d("SecureGDFileOpener", "GDServiceListener.onReceiveMessage()");
				}

				@Override
				public void onMessageSent(String application, String requestID, String[] attachments) {
					Log.d("SecureGDFileOpener", "GDServiceListener.onMessageSent()");
				}
			});
		} catch (GDServiceException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	
	public void onResetTestFileClicked(View v) {
		
		File testFile = new File(PATH_TO_TEST_TXT_FILE);
		
		testFile.delete();
		createStubTxtFile(PATH_TO_TEST_TXT_FILE, "Hello GD world!");
		
		// Load file content on screen
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(testFile);
			StringBuilder fileContentBuilder = new StringBuilder();
			char nextChar;
			while ((nextChar = (char) fileReader.read()) != -1) {
				fileContentBuilder.append(nextChar);
			}
			fileContent_.setText(fileContentBuilder.toString());
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Error while init: " + e.getMessage(), Toast.LENGTH_LONG).show();
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
		
		

	}
	
	public void onEditStubFileClicked(View v) {
		// TODO Implementation
		
//	try {
//		String application = "com.syncplicity.android.securegdfileeditor.MainActivity"; // packge of application
//		String service = "com.good.gdservice.edit-file"; // id of service
//		String version = "1.0.0.0"; // version of service
//		String method = "editFile"; // name method of service which we want to call
//		Map<String, Object> params = new HashMap<String, Object>();
//		String[] attachments = new String[1]; // Paths to files inside GD secure storages
//		attachments[0] = "test.txt";
//		createStubFile(attachments[0], 256);
//
////static String sendTo	(	String 	application,
////String 	service,
////String 	version,
////String 	method,
////Object 	params,
////String[] 	attachments,
////GDICCForegroundOptions 	option 
////)		 throws GDServiceException 
//		GDAndroid.getInstance().authorize("com.syncplicity.android.securegdfileopener", "1.0.0.0",
//				new GDAppEventListener() {
//
//					@Override
//					public void onGDEvent(GDAppEvent event) {
//						Log.d("SecureGDFileOpener", event.toString());
//					}
//				});
////		GDServiceClient.bringToFront("com.syncplicity.android.securegdfileeditor.MainActivity");
//		String requestID = GDServiceClient.sendTo(application, service, version, method, params, attachments, GDICCForegroundOptions.PreferPeerInForeground);
//		
//	} catch (GDServiceException e) {
//		e.printStackTrace();
//	}
	}
	
	private List<GDAppDetail> getListOfApplicationsThatSupportsEditFileService() {
		String serviceId = "com.good.gdservice.edit-file";
		String serviceVersion = "1.0.0.0";
		List<GDAppDetail> availableApps = GDAndroid.getInstance().getApplicationDetailsForService(serviceId, serviceVersion);
		return availableApps;
	}

	private void createStubTxtFile(String path, String initialContent) {
		File file = new File(path);
		if (file.isDirectory()) {
			file.delete();
		}
		String parent = file.getParent();
		if (parent != null) {
			File parentfile = new File(parent);
			parentfile.mkdirs();
		}

		byte[] data = initialContent.getBytes();

		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(file));
			out.write(data);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
