package com.syncplicity.android.securegdfileeditor;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
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
		if (!authorized) {
			GDAndroid.getInstance().authorize("com.syncplicity.android.securegdfileeditor", "1.0.0.0",
					new GDAppEventListener() {

						@Override
						public void onGDEvent(GDAppEvent event) {
							// TODO Auto-generated method stub

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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onGenerateStubFileClicked(View v) {
		// TODO Implementation
	}
	
	public void onEditStubFileClicked(View v) {
		// TODO Implementation
	}

}
