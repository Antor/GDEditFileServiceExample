package com.syncplicity.android.securegdfileeditor;

import java.util.Set;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;

public class IccReceivingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("SecureGDFileOpener", "com.syncplicity.android.securegdfileeditor.IccReceivingActivity.onCreate()");
		super.onCreate(savedInstanceState);

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

		// GDAndroid call to check if on loading this activity it should
		// launch the Launch Activity to authorize.
		if (GDAndroid.getInstance().IccReceiverShouldAuthorize()) {
			Intent i = new Intent(this, MainActivity.class);
			this.startActivity(i);
		}
		finish();
	}
}
