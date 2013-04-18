package com.syncplicity.android.securegdfileeditor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.error.GDNotAuthorizedError;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class IccReceivingActivity extends Activity {

	private enum ReasonOfCreation { ConnectionRequest, FrontRequest, Unknown }

	private static boolean isGDServiceListenerWasSet_ = false;

	private ReasonOfCreation reasonOfCreation_;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		Log.d("SecureGDFileOpener", "Intent " + intent.toString());

		Uri data = intent.getData();
		if (data != null) {
			String path = data.getPath();
			if (path != null) {
				if (path.equals("/CON_REQ")) {
					reasonOfCreation_ = ReasonOfCreation.ConnectionRequest;
				} else if (path.equals("/FRONT")) {
					reasonOfCreation_ = ReasonOfCreation.FrontRequest;
				} else {
					reasonOfCreation_ = ReasonOfCreation.Unknown;
				}
			}
		}

		if (!isGDServiceListenerWasSet_) {
			try {
				GDService.setServiceListener(new GDServiceListener() {

					@Override
					public void onReceiveMessage(String application, String service, String version, String method,
							Object params, String[] attachments, String requestID) {
						Log.d("SecureGDFileOpener", String.format("Received message %s", params));

					}

					@Override
					public void onMessageSent(String application, String requestID, String[] attachments) {
						Log.d("SecureGDFileOpener", String.format("Sent message to application=%s", application));
					}
				});
				isGDServiceListenerWasSet_ = true;
			} catch (GDServiceException e) {
				e.printStackTrace();
			}
		}

		if (isGDLibraryAuthorized()) {
			if (reasonOfCreation_ == ReasonOfCreation.FrontRequest) {
				startActivity(new Intent(IccReceivingActivity.this, MainActivity.class));
			}
		} else {
			GDAndroid.getInstance().authorize(new GDAppEventListener() {

				@Override
				public void onGDEvent(GDAppEvent event) {
					if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
						if (reasonOfCreation_ == ReasonOfCreation.FrontRequest) {
							startActivity(new Intent(IccReceivingActivity.this, MainActivity.class));
						}
					}
				}
			});
		}
		finish();

//		// GDAndroid call to check if on loading this activity it should
//		// launch the Launch Activity to authorize.
//		if (GDAndroid.getInstance().IccReceiverShouldAuthorize()) {
//			Log.d("SecureGDFileOpener", "GDAndroid.getInstance().IccReceiverShouldAuthorize() == true");
//			Intent i = new Intent(this, MainActivity.class);
//			this.startActivity(i);
//		}
//		finish();
	}

	private boolean isGDLibraryAuthorized() {
		try {
			GDAndroid.getInstance().getApplicationConfig();
			return true;
		} catch (GDNotAuthorizedError e) {
			return false;
		}
	}
}
