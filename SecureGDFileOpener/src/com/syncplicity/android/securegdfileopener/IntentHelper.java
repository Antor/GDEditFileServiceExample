package com.syncplicity.android.securegdfileopener;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.good.gd.GDAppDetail;

/**
 * Contains methods to create intents for external apps.
 */
public class IntentHelper {
	
	public interface OnGdAppSelectedListener {
		public void onGdAppSelectedListener(GDAppDetail gdAppDetail);
	}
	
	public static class GDServiceApplicationInfo {
		
		private GDAppDetail gdAppDetail_;
		private ApplicationInfo androidApplicationInfo_;
		
		public GDServiceApplicationInfo(GDAppDetail gdAppDetail, ApplicationInfo androidApplicationInfo) {
			this.gdAppDetail_ = gdAppDetail;
			this.androidApplicationInfo_ = androidApplicationInfo;
		}

		public GDAppDetail getGdAppDetail() {
			return gdAppDetail_;
		}

		public ApplicationInfo getAndroidApplicationInfo() {
			return androidApplicationInfo_;
		}		
	}
	
	private static class GDServiceApplicationInfoAdapter extends ArrayAdapter<GDServiceApplicationInfo> {

		private LayoutInflater inflater_;
		private PackageManager packageManager_;
		
		public GDServiceApplicationInfoAdapter(Context context) {
			super(context, 0);
			inflater_ = (LayoutInflater) context.getSystemService(Context. LAYOUT_INFLATER_SERVICE);
			packageManager_ = context.getPackageManager();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = inflater_.inflate(R.layout.application_item, null, true);
			}
			
			ImageView applicationIcon = (ImageView) view.findViewById(R.id.application_icon);
			TextView applicationName = (TextView) view.findViewById(R.id.application_name);
			
			GDServiceApplicationInfo gdServiceApplicationInfo = getItem(position);
			
			applicationIcon.setImageDrawable(gdServiceApplicationInfo.getAndroidApplicationInfo().loadIcon(packageManager_));
			applicationName.setText(gdServiceApplicationInfo.getAndroidApplicationInfo().loadLabel(packageManager_));
			
			return view;
		}	
	}

	public static void showChooser(Context context, String title, List<GDAppDetail> availableAppsToEditFileIn, final OnGdAppSelectedListener listener) {
		
		PackageManager packageManager = context.getPackageManager();
		
		List<GDServiceApplicationInfo> suitableApplications = new ArrayList<IntentHelper.GDServiceApplicationInfo>();
		for (GDAppDetail gdAppDetail : availableAppsToEditFileIn) {
			try {
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(gdAppDetail.getAddress(), 0);
				GDServiceApplicationInfo gdServiceApplicationInfo = new GDServiceApplicationInfo(gdAppDetail, applicationInfo);
				suitableApplications.add(gdServiceApplicationInfo);
			} catch (NameNotFoundException e) {
				// Do nothing, just ignore such apps
			}
		}
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title);
		final GDServiceApplicationInfoAdapter adapter = new GDServiceApplicationInfoAdapter(context);
		for (GDServiceApplicationInfo gdServiceApplicationInfo : suitableApplications) {
			adapter.add(gdServiceApplicationInfo);
		}
		dialogBuilder.setAdapter(adapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GDServiceApplicationInfo application = adapter.getItem(which);
				listener.onGdAppSelectedListener(application.getGdAppDetail());
			}
		});
		dialogBuilder.show();
	}


}