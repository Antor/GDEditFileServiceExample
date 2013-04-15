package com.syncplicity.android.securegdfileopener;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppDetail;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.file.File;
import com.good.gd.file.FileOutputStream;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);

		// Authorize app
		boolean authorized = false;
		
			final String APP_ID = "com.good.gd.example.services.greetings.client";
			final String APP_VERSION = "1.0.0.0";
			GDAndroid.getInstance().authorize("com.syncplicity.android.securegdfileopener", "1.0.0.0",
					new GDAppEventListener() {

						@Override
						public void onGDEvent(GDAppEvent event) {
							Log.d("SecureGDFileOpener", event.toString());
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
						// TODO Auto-generated method stub
						Log.d("SecureGDFileOpener", "GDServiceListener.onReceiveMessage()");
					}

					@Override
					public void onMessageSent(String application, String requestID, String[] attachments) {
						// TODO Auto-generated method stub
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
	
	private static final String _greetingServerId = "com.good.gd.example.services.greetings.server.GreetingsServer";
	
	public void onGenerateStubFileClicked(View v) {
//		try {
//			GDServiceClient.bringToFront("com.syncplicity.android.securegdfileeditor.MainActivity");
//		} 
//		catch (GDServiceException e) {
//			e.printStackTrace();
//		}
		
		// TODO Implementation
		String serviceId = "com.good.gdservice.edit-file";
		String serviceVersion = "1.0.0.0";
		Vector<GDAppDetail> availableApps = GDAndroid.getInstance().getApplicationDetailsForService(serviceId, serviceVersion);
		Log.d("SecureGDFileOpener", String.format("Found %d implementation of service %s:%s", availableApps.size(), serviceId, serviceVersion));
		for (GDAppDetail gdAppDetail : availableApps) {
			Log.d("SecureGDFileOpener", String.format("Address: %s | ApplicationId: %s | VersionId: %s", 
					gdAppDetail.getAddress(), gdAppDetail.getApplicationId(), gdAppDetail.getVersionId()));
		}
		
////		createStubFile("test.txt", 256);
		
//		try {
//			String application = "com.syncplicity.android.securegdfileeditor.MainActivity"; // packge of application
//			String service = "com.good.gdservice.edit-file"; // id of service
//			String version = "1.0.0.0"; // version of service
//			String method = "editFile"; // name method of service which we want to call
//			Map<String, Object> params = new HashMap<String, Object>();
//			String[] attachments = new String[1]; // Paths to files inside GD secure storages
//			attachments[0] = "test.txt";
//			createStubFile(attachments[0], 256);
//
////static String sendTo	(	String 	application,
////String 	service,
////String 	version,
////String 	method,
////Object 	params,
////String[] 	attachments,
////GDICCForegroundOptions 	option 
////)		 throws GDServiceException 
//			GDAndroid.getInstance().authorize("com.syncplicity.android.securegdfileopener", "1.0.0.0",
//					new GDAppEventListener() {
//
//						@Override
//						public void onGDEvent(GDAppEvent event) {
//							Log.d("SecureGDFileOpener", event.toString());
//						}
//					});
////			GDServiceClient.bringToFront("com.syncplicity.android.securegdfileeditor.MainActivity");
//			String requestID = GDServiceClient.sendTo(application, service, version, method, params, attachments, GDICCForegroundOptions.PreferPeerInForeground);
//			
//		} catch (GDServiceException e) {
//			e.printStackTrace();
//		}
	}
	
	public void onEditStubFileClicked(View v) {
		// TODO Implementation
	}

	private void createStubFile(String path, int size) {
		File file = new File(path);
		if (file.isDirectory()) {
			file.delete();
		}
		String parent = file.getParent();
		if (parent != null) {
			File parentfile = new File(parent);
			parentfile.mkdirs();
		}

		byte[] data = new byte[size];
		for (int i = 0; i < data.length; ++i) {
			data[i] = 2; // just fill the file with a random value
		}

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
