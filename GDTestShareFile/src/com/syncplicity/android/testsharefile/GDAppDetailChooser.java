package com.syncplicity.android.testsharefile;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
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
 * Shows dialog to chose GDAppDetail from. Similar to IntentChooser
 */
public class GDAppDetailChooser {

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

	private String title_;
	private List<GDAppDetail> apps_;
	private OnGdAppSelectedListener onGdAppSelectedListener_;

	/**
	 * Creates chooser of GD enabled app
	 * @param title Title of dialog that will be shown
	 * @param apps List of GD enabled apps to chose from
	 * @param onGdAppSelectedListener listener to get user choice when it was done
	 */
	public GDAppDetailChooser(String title, List<GDAppDetail> apps, OnGdAppSelectedListener onGdAppSelectedListener) {
		this.title_ = title;
		this.apps_ = apps;
		this.onGdAppSelectedListener_ = onGdAppSelectedListener;
	}

	/**
	 * Create and shows dialog with list of apps
	 */
	public void show(Context context) {
		PackageManager packageManager = context.getPackageManager();

		List<GDServiceApplicationInfo> suitableApplications = new ArrayList<GDServiceApplicationInfo>();
		for (GDAppDetail gdAppDetail : apps_) {
			try {
				String address = gdAppDetail.getAddress();
				int lastDotIndex = address.lastIndexOf(".");
				String pkg = address.substring(0, lastDotIndex);
				ComponentName iccActivityComponentName = new ComponentName(pkg, address);

				ActivityInfo activityInfo = packageManager.getActivityInfo(iccActivityComponentName, 0);
				ApplicationInfo applicationInfo = activityInfo.applicationInfo;

				GDServiceApplicationInfo gdServiceApplicationInfo = new GDServiceApplicationInfo(gdAppDetail, applicationInfo);
				suitableApplications.add(gdServiceApplicationInfo);
			} catch (NameNotFoundException e) {
				// Do nothing, just ignore such apps
			}
		}

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setTitle(title_);
		final GDServiceApplicationInfoAdapter adapter = new GDServiceApplicationInfoAdapter(context);
		for (GDServiceApplicationInfo gdServiceApplicationInfo : suitableApplications) {
			adapter.add(gdServiceApplicationInfo);
		}
		dialogBuilder.setAdapter(adapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				GDServiceApplicationInfo application = adapter.getItem(which);
				if (onGdAppSelectedListener_ != null) {
					onGdAppSelectedListener_.onGdAppSelectedListener(application.getGdAppDetail());
				}
			}
		});
		dialogBuilder.show();
	}
}