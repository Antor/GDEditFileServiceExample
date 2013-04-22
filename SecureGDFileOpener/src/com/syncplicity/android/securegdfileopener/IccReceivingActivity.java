package com.syncplicity.android.securegdfileopener;

import android.content.Intent;
import android.os.Bundle;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;

public class IccReceivingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (GDAndroid.getInstance().IccReceiverShouldAuthorize()) {
            startActivity(new Intent(this, LaunchActivity.class));
    	}
    	finish();
    }
}
