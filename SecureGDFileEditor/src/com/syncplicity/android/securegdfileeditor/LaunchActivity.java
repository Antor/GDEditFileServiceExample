package com.syncplicity.android.securegdfileeditor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;
import com.good.gd.GDAppEvent;
import com.good.gd.GDAppEventListener;
import com.good.gd.GDAppEventType;
import com.good.gd.error.GDError;

public class LaunchActivity extends Activity {

	private boolean authorizeCalled_ = false;

	public static void doAuthorization(Context context) {
		context.startActivity(new Intent(context, LaunchActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!authorizeCalled_) {
			try {
				GDAndroid.getInstance().authorize(new GDAppEventListener() {

					private boolean uiLaunched_ = false;

					@Override
					public void onGDEvent(GDAppEvent event) {
						if (event.getEventType() == GDAppEventType.GDAppEventAuthorized && !uiLaunched_) {
							startActivity(new Intent(LaunchActivity.this, MainActivity.class));
							uiLaunched_ = true;
						}
					}
				});

				authorizeCalled_ = true;
			} catch (GDError gderror) {
				showErrorPopup(gderror.getMessage());
			}
		} else {
			finish();
		}
	}

	private void showErrorPopup(String error) {
		new AlertDialog.Builder(this).setTitle("Initialization Error").setMessage(error)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						finish();
					}
				}).show();
	}
}
