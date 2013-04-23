package com.example.texteditorforgood;

import android.content.Intent;
import android.os.Bundle;

import com.good.gd.Activity;
import com.good.gd.GDAndroid;

public class IccReceivingActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	if (GDAndroid.getInstance().IccReceiverShouldAuthorize()) {
            Intent i = new Intent(this, TextEditorMainActivity.class);
            this.startActivity(i);
    	}
    	finish();
    }
}
