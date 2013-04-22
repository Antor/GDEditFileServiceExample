//package com.syncplicity.android.securegdfileeditor;
//
//import android.content.Context;
//import android.content.Intent;
//import android.net.Uri;
//import android.os.Bundle;
//
//import com.good.gd.Activity;
//import com.good.gd.GDAndroid;
//import com.good.gd.GDAppEvent;
//import com.good.gd.GDAppEventListener;
//import com.good.gd.GDAppEventType;
//import com.good.gd.error.GDNotAuthorizedError;
//import com.good.gd.icc.GDService;
//import com.good.gd.icc.GDServiceException;
//
//
//public class GoodDynamicsLaunchActivity extends Activity {
//
//	private enum ReasonOfCreation { ConnectionRequest, FrontRequest, Unknown }
//
//	private static boolean isGDServiceListenerWasSet_ = false;
//
//	private ReasonOfCreation reasonOfCreation_;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		if (!isGDServiceListenerWasSet_) {
//			try {
//				SecureGDFileEditorGDServiceListener gdServiceListener = SecureGDFileEditorGDServiceListener
//						.getInstance();
//				GDService.setServiceListener(gdServiceListener);
//				isGDServiceListenerWasSet_ = true;
//			} catch (GDServiceException e) {
//				e.printStackTrace();
//			}
//		}
//
//		Intent intent = getIntent();
//		String action = intent.getAction();
//		if (action.equals("android.intent.action.MAIN")) {
//			// Activity was launched by user manually
//			if (isGDAndroidAuthorized()) {
//				startActivity(new Intent(this, MainActivity.class));
//			} else {
//				GDAndroid.getInstance().authorize(new GDAppEventListener() {
//
//					@Override
//					public void onGDEvent(GDAppEvent event) {
//						if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
//							startActivity(new Intent(GoodDynamicsLaunchActivity.this, MainActivity.class));
//						}
//					}
//				});
//			}
//		} else if (action.equals("com.good.gd.intent.action.ACTION_ICC_COMMAND")) {
//			// Activity was launched through GD ICC
//			Uri data = intent.getData();
//			if (data != null) {
//				String path = data.getPath();
//				if (path != null) {
//					if (path.equals("/CON_REQ")) {
//						reasonOfCreation_ = ReasonOfCreation.ConnectionRequest;
//					} else if (path.equals("/FRONT")) {
//						reasonOfCreation_ = ReasonOfCreation.FrontRequest;
//					} else {
//						reasonOfCreation_ = ReasonOfCreation.Unknown;
//					}
//				}
//
//				if (isGDAndroidAuthorized()) {
//					if (reasonOfCreation_ == ReasonOfCreation.FrontRequest) {
//						startActivity(new Intent(this, MainActivity.class));
//					}
//				} else {
//					GDAndroid.getInstance().authorize(new GDAppEventListener() {
//						@Override
//						public void onGDEvent(GDAppEvent event) {
//							if (event.getEventType() == GDAppEventType.GDAppEventAuthorized) {
//								if (reasonOfCreation_ == ReasonOfCreation.FrontRequest) {
//									startActivity(new Intent(GoodDynamicsLaunchActivity.this, MainActivity.class));
//								}
//							}
//						}
//					});
//				}
//			}
//
//		} else {
//			// Unknown situation
//		}
//
//
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		if (isGDAndroidAuthorized()) {
//			finish();
//		}
//	}
//
//
//
//	private boolean isGDAndroidAuthorized() {
//		try {
//			GDAndroid.getInstance().getApplicationConfig();
//			return true;
//		} catch (GDNotAuthorizedError e) {
//			return false;
//		}
//	}
//}
