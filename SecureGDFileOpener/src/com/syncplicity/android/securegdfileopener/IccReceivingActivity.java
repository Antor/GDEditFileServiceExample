package com.syncplicity.android.securegdfileopener;

import android.content.Intent;
import android.os.Bundle;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;

public class IccReceivingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// GDAndroid call to check if on loading this activity it should
		// launch the Launch Activity to authorize.
		if (GDAndroid.getInstance().IccReceiverShouldAuthorize()) {
			Intent i = new Intent(this, MainActivity.class);
			this.startActivity(i);
		}
		finish();
	}
}
