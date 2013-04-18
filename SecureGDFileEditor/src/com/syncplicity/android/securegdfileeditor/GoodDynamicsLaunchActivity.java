package com.syncplicity.android.securegdfileeditor;

import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.error.GDError;


public class GoodDynamicsLaunchActivity extends Activity {

	private boolean authorizeCalled_ = false;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Set<String> keys = bundle.keySet();
			keys.size();
		}
		Set<String> categories = intent.getCategories();
		if (categories != null) {
			categories.size();
		}
		String action = intent.getAction();
		if (action != null) {
			action.length();
		}
		Uri data = intent.getData();
		if (data != null) {
			data.getScheme();
		}

	}

	@Override
	public void onResume() {
		super.onResume();

		if (authorizeCalled_) {
			finish();
		} else {
			try {
				GDAndroid.getInstance().authorize(new GDAppEventListener() {
					@Override
					public void onGDEvent(GDAppEvent event) {
						if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
							startActivity(new Intent(GoodDynamicsLaunchActivity.this, MainActivity.class));
						}
					}
				});
				authorizeCalled_ = true;
			} catch (GDError gderror) {
				showErrorPopup(gderror.getMessage());
			}

		}
	}

	private void showErrorPopup(String error) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle("Initialization Error");
		dialogBuilder.setMessage(error);
		dialogBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				finish();
			}
		});
		dialogBuilder.show();
	}
}
