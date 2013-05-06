package com.syncplicity.android.testsharefile;

import java.util.Iterator;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.Toast;

import com.good.gd.GDAndroid;
import com.good.gd.GDAppDetail;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.syncplicity.android.testsharefile.GDAppDetailChooser.OnGdAppSelectedListener;

/**
 * Currently provides functionality to open file to edit through GD AppKinetics
 */
public class AppKineticsHelper {

	private static final String TRANSFER_FILE_SERVICE_ID = "com.good.gdservice.transfer-file";
	private static final String TRANSFER_FILE_SERVICE_VERSION_1_0_0_0 = "1.0.0.0";
	private static final String TRANSFER_FILE_METHOD = "transferFile";

	private static final GDServiceClientListener GD_SERVICE_CLIENT_LISTENER_STUB = new GDServiceClientListener() {
		@Override
		public void onReceiveMessage(String application, Object params, String[] attachments, String requestID) {
			// Do nothing
		}

		@Override
		public void onMessageSent(String application, String requestID, String[] attachments) {
			// Do nothing
		}
	};

	public static void shareFile(Context context, com.good.gd.file.File file) {
		shareFile(context, file, "No application for such action.", "Share this file using…");
	}

	public static void shareFile(final Context context, final com.good.gd.file.File file, String noApplicationMessage, String chooserTitle) {
		// 1. Discover
		List<GDAppDetail> availableAppsToShareFileIn = GDAndroid.getInstance().getApplicationDetailsForService(
				TRANSFER_FILE_SERVICE_ID, TRANSFER_FILE_SERVICE_VERSION_1_0_0_0);
		removeNotInstalledAppsFromList(context, availableAppsToShareFileIn);

		// 2. Open
		if (availableAppsToShareFileIn.isEmpty()) {
			Toast.makeText(context, noApplicationMessage, Toast.LENGTH_SHORT).show();
		} else if (availableAppsToShareFileIn.size() == 1) {
			shareFileInApp(context, file, availableAppsToShareFileIn.get(0));
		} else {
			GDAppDetailChooser chooser = new GDAppDetailChooser(chooserTitle,
					availableAppsToShareFileIn, new OnGdAppSelectedListener() {
						@Override
						public void onGdAppSelectedListener(GDAppDetail gdAppDetail) {
							shareFileInApp(context, file, gdAppDetail);						}
					});
			chooser.show(context);
		}
	}

	private static void shareFileInApp(final Context context, com.good.gd.file.File file, GDAppDetail app) {
		try {
			GDServiceClient.setServiceClientListener(new GDServiceClientListener() {

				@Override
				public void onReceiveMessage(String application, Object params, String[] attachments, String requestID) {
					try {
						GDServiceClient.setServiceClientListener(GD_SERVICE_CLIENT_LISTENER_STUB);
					} catch (GDServiceException e) {
						// Do nothing
					}
				}

				@Override
				public void onMessageSent(String application, String requestID, String[] attachments) {
					// Do nothing
				}
			});
			GDServiceClient.sendTo(app.getAddress(), TRANSFER_FILE_SERVICE_ID, TRANSFER_FILE_SERVICE_VERSION_1_0_0_0,
					TRANSFER_FILE_METHOD, null, new String[] { file.getAbsolutePath() },
					GDICCForegroundOptions.PreferPeerInForeground);
		} catch (GDServiceException e) {
			// TODO Find out better error message
			Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	private static void removeNotInstalledAppsFromList(Context context, List<GDAppDetail> apps) {
		PackageManager packageManager = context.getPackageManager();

		for (Iterator<GDAppDetail> iterator = apps.iterator(); iterator.hasNext();) {
			GDAppDetail gdAppDetail = iterator.next();

			String localtion = gdAppDetail.getAddress();
			int lastDotIndex = localtion.lastIndexOf(".");
			String pkg = localtion.substring(0, lastDotIndex);
			ComponentName iccActivityComponentName = new ComponentName(pkg, localtion);
			try {
				packageManager.getActivityInfo(iccActivityComponentName, 0);
			} catch (NameNotFoundException e) {
				iterator.remove();
			}
		}
	}
}
