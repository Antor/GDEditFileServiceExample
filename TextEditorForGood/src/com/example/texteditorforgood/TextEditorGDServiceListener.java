package com.example.texteditorforgood;

import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.good.gd.file.File;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceError;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;


public class TextEditorGDServiceListener implements GDServiceListener {

	public interface OnOpenFileToEditListener {
		public void onFileToEditReceived();
	}

	private static final String EDIT_FILE_SERVICE_ID = "com.good.gdservice.edit-file";
	private static final String EDIT_FILE_SERVICE_VERSION_1_0_0_0 = "1.0.0.0";
	private static final String EDIT_FILE_SERVICE_EDIT_FILE_METHOD = "editFile";

	/**
	 * The service provider returns this error if the request is invalid in some way that does not relate to the file
	 * attachment. For example, this error would be returned if the request had no file attachments, or included an
	 * unknown parameter.
	 */
	private static final int EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE = 1;

	/**
	 * The service provider returns this error if it can only handle certain types of file, and the file in the service
	 * request is of a type that cannot be handled.
	 */
	private static final int EDIT_FILE_SERVICE_UNSUPPORTED_FILE_TYPE_ERROR_CODE = 2;

	/**
	 * The service provider returns this error if the file in the service request cannot be processed. This error could
	 * be used if, for example, a file of a known format appeared incomplete or corrupt.
	 */
	private static final int EDIT_FILE_SERVICE_FILE_NOT_PROCESSED_ERROR_CODE = 3;

	private static TextEditorGDServiceListener instance_;

	private Context context_;
	private OnOpenFileToEditListener onOpenFileToEditListener_;

	public static TextEditorGDServiceListener getInstance(Context context) {
		if (instance_ == null) {
			instance_ = new TextEditorGDServiceListener(context);
		}
		return instance_;
	}

	private TextEditorGDServiceListener(Context context) {
		this.context_ = context.getApplicationContext();
	}

	public void setOnOpenFileToEditListener(OnOpenFileToEditListener onOpenFileToEditListener) {
		this.onOpenFileToEditListener_ = onOpenFileToEditListener;
	}

	@Override
	public void onReceiveMessage(String application, String service, String version, String method,
			Object params, String[] attachments, String requestID) {
		if (service.equals(EDIT_FILE_SERVICE_ID)
				&& version.equals(EDIT_FILE_SERVICE_VERSION_1_0_0_0)) {
			handleEditFileServiceRequest(application, method, params, attachments, requestID);
		} else {
			sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Service %s v.%s is unsupported by application %s!", service, version, application),
					application, requestID);
		}
	}

	private void handleEditFileServiceRequest(String application, String method, Object params, String[] attachments,
			String requestID) {
		if (method.equals(EDIT_FILE_SERVICE_EDIT_FILE_METHOD)) {
			handleEditFileServiceRequestWithEditFileMethod(application, params, attachments, requestID);
		} else {
			sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Service %s v.%s does not support %s method!", EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0, method),
					application, requestID);
		}
	}

	private void handleEditFileServiceRequestWithEditFileMethod(String application, Object params, String[] attachments,
			String requestID) {
		// Request should contains exactly 1 attachment
		if (attachments.length != 1) {
			sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Method %s of service %s v.%s should contains exactly 1 attachement!", EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					application, requestID);
			return;
		}

		// Parameters of method should be specified and be instance of java.util.Map
		if (params == null || !(params instanceof Map)) {
			sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format(
							"Parameters of method %s of service %s v.%s should be specified and be instance of java.util.Map!",
							EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					application, requestID);
			return;
		}

		Map<?, ?> paramsMap = (Map<?, ?>) params;
		Set<?> keys = paramsMap.keySet();

		// Only one optional parameter is possible for method
		if (keys.size() > 1) {
			sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Only one optional parameter is possible for method %s of service %s v.%s!",
							EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					application, requestID);
			return;
		}

		if (keys.size() == 1) {
			for (Object key : keys) {
				if (!(key instanceof String)) {
					sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
							String.format("Key for single optional parameter should be instance of String for method %s of service %s v.%s should be specified and be instance of java.util.Map!",
									EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
							application, requestID);
					return;
				}
				String keyAsString = (String) key;
				if (!keyAsString.equals("identificationData")) {
					sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
							String.format("Key for single optional parameter should be equal to 'identificationData' for method %s of service %s v.%s!",
									EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
							application, requestID);
					return;
				}
			}
			// 'identificationData' parameter value should be instance of byte[]
			Object identificationData = paramsMap.get("identificationData");
			if (!(identificationData instanceof byte[])) {
				sendErrorResponse(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
						String.format("'identificationData' parameter value should be instance of byte[] for method %s of service %s v.%s!",
								EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
						application, requestID);
				return;
			}
		}

		// Only files with extension txt are supported
		File file = new File(attachments[0]);
		if (!file.getName().endsWith(".txt")) {
			sendErrorResponse(EDIT_FILE_SERVICE_UNSUPPORTED_FILE_TYPE_ERROR_CODE,
					String.format("Method %s of service %s v.%s support only editing files with extension txt!", EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					application, requestID);
			return;
		}

		byte[] identificationData = (byte[]) paramsMap.get("identificationData");
		SharedPreferencesManager.setLastOpenedFile(context_, file, identificationData, application);

		// File was successfully received so send empty response as sign of success
		try {
			GDService.replyTo(application, null, GDICCForegroundOptions.PreferMeInForeground, new String[0], requestID);
		} catch (GDServiceException e) {
			Log.e(TextEditorMainActivity.TAG, e.getMessage(), e);
		}

		if (onOpenFileToEditListener_ != null) {
			onOpenFileToEditListener_.onFileToEditReceived();
		}
	}

	private void sendErrorResponse(int errorCode, String message, String application, String requestID) {
		GDServiceError gdServiceError = new GDServiceError(errorCode, message, null);
		try {
			GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
		} catch (GDServiceException e) {
			Log.e(TextEditorMainActivity.TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onMessageSent(String application, String requestID, String[] attachments) {
		// Do nothing
	}
}
