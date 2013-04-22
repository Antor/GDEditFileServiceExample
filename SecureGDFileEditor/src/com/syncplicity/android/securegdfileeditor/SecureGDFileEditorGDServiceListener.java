package com.syncplicity.android.securegdfileeditor;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import android.util.Log;

import com.good.gd.file.File;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceError;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;


public class SecureGDFileEditorGDServiceListener implements GDServiceListener {

	public interface OnOpenFileToEditListener {
		public void onOpenFileToEdit(File file, byte[] identificationData, String fromAplication);
	}

	public static class GDServiceMessage {

		private String application_;
		private String service_;
		private String version_;
		private String method_;
		private Object params_;
		private String[] attachments_;
		private String requestID_;

		public GDServiceMessage(String application, String service, String version, String method, Object params,
				String[] attachments, String requestID) {
			this.application_ = application;
			this.service_ = service;
			this.version_ = version;
			this.method_ = method;
			this.params_ = params;
			this.attachments_ = attachments;
			this.requestID_ = requestID;
		}

		public String getApplication() {
			return application_;
		}

		public String getService() {
			return service_;
		}

		public String getVersion() {
			return version_;
		}

		public String getMethod() {
			return method_;
		}

		public Object getParams() {
			return params_;
		}

		public String[] getAttachments() {
			return attachments_;
		}

		public String getRequestID() {
			return requestID_;
		}

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

	private static SecureGDFileEditorGDServiceListener instance_;

	private Queue<GDServiceMessage> pendingGDServiceMessages_;

	private OnOpenFileToEditListener onOpenFileToEditListener_;

	public void setOnOpenFileToEditListener_(OnOpenFileToEditListener onOpenFileToEditListener_) {
		this.onOpenFileToEditListener_ = onOpenFileToEditListener_;
	}

	public static SecureGDFileEditorGDServiceListener getInstance() {
		if (instance_ == null) {
			instance_ = new SecureGDFileEditorGDServiceListener();
		}
		return instance_;
	}

	private SecureGDFileEditorGDServiceListener() {
		pendingGDServiceMessages_ = new LinkedList<GDServiceMessage>();
	}

	public Queue<GDServiceMessage> getPendingGDServiceMessages() {
		return pendingGDServiceMessages_;
	}

	@Override
	public void onReceiveMessage(String application, String service, String version, String method,
			Object params, String[] attachments, String requestID) {
		Log.d("SecureGDFileOpener", String.format("Received message %s",
				params));
		if (onOpenFileToEditListener_ != null) {
			consumeReceivedMessage(application, service, version, method, params, attachments, requestID, onOpenFileToEditListener_);
		} else {
			pendingGDServiceMessages_.add(new GDServiceMessage(application, service, version, method, params, attachments, requestID));
		}

	}

	public void consumeReceivedMessage(String application, String service, String version, String method,
				Object params, String[] attachments, String requestID, OnOpenFileToEditListener onOpenFileToEditListener) {


		if (service.equals(EDIT_FILE_SERVICE_ID)
				&& version.equals(EDIT_FILE_SERVICE_VERSION_1_0_0_0)) {
			handleEditFileServiceRequest(application, method, params, attachments, requestID, onOpenFileToEditListener);
		} else {

			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Service %s v.%s is unsupported by application %s!", service, version, application), null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
		}
	}

	private void handleEditFileServiceRequest(String application, String method, Object params, String[] attachments,
			String requestID, OnOpenFileToEditListener onOpenFileToEditListener) {
		if (method.equals(EDIT_FILE_SERVICE_EDIT_FILE_METHOD)) {
			handleEditFileServiceRequestWithEditFileMethod(application, params, attachments, requestID, onOpenFileToEditListener);
		} else {
			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Service %s v.%s does not support %s method!", EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0, method), null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
		}
	}

	private void handleEditFileServiceRequestWithEditFileMethod(String application, Object params, String[] attachments,
			String requestID, OnOpenFileToEditListener onOpenFileToEditListener) {
		// Request should contains exactly 1 attachment
		if (attachments.length != 1) {
			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format("Method %s of service %s v.%s should contains exactly 1 attachement!", EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0), null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
			return;
		}

		// Parameters of method should be specified and be instance of java.util.Map
		if (params == null || !(params instanceof Map)) {
			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format(
							"Parameters of method %s of service %s v.%s should be specified and be instance of java.util.Map!",
							EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
			return;
		}

		Map<?, ?> paramsMap = (Map<?, ?>) params;
		Set<?> keys = paramsMap.keySet();

		// Only one optional parameter is possible for method
		if (keys.size() > 1) {
			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
					String.format(
							"Only one optional parameter is possible for method %s of service %s v.%s!",
							EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
					null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
			return;
		}

		if (keys.size() == 1) {
			for (Object key : keys) {
				if (!(key instanceof String)) {
					GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
							String.format(
									"Key for single optional parameter should be instance of String for method %s of service %s v.%s should be specified and be instance of java.util.Map!",
									EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
							null);
					new Exception(gdServiceError.getMessage()).printStackTrace();
					try {
						GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
					} catch (GDServiceException e) {
						// Do nothing
						e.printStackTrace();
					}
					return;
				}
				String keyAsString = (String) key;
				if (!keyAsString.equals("identificationData")) {
					GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
							String.format(
									"Key for single optional parameter should be equal to 'identificationData' for method %s of service %s v.%s!",
									EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
							null);
					new Exception(gdServiceError.getMessage()).printStackTrace();
					try {
						GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
					} catch (GDServiceException e) {
						// Do nothing
						e.printStackTrace();
					}
					return;
				}
			}
			// 'identificationData' parameter value should be instance of byte[]
			Object identificationData = paramsMap.get("identificationData");
			if (!(identificationData instanceof byte[])) {
				GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_INVALID_REQUEST_ERROR_CODE,
						String.format(
								"'identificationData' parameter value should be instance of byte[] for method %s of service %s v.%s!",
								EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID, EDIT_FILE_SERVICE_VERSION_1_0_0_0),
						null);
				new Exception(gdServiceError.getMessage()).printStackTrace();
				try {
					GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
				} catch (GDServiceException e) {
					// Do nothing
					e.printStackTrace();
				}
				return;
			}
		}

		// Only files with extension txt are supported
		File file = new File(attachments[0]);
		if (!file.getName().endsWith(".txt")) {
			GDServiceError gdServiceError = new GDServiceError(EDIT_FILE_SERVICE_UNSUPPORTED_FILE_TYPE_ERROR_CODE,
					String.format("Method %s of service %s v.%s support only editing files with extension txt!", EDIT_FILE_SERVICE_EDIT_FILE_METHOD, EDIT_FILE_SERVICE_ID,
							EDIT_FILE_SERVICE_VERSION_1_0_0_0), null);
			new Exception(gdServiceError.getMessage()).printStackTrace();
			try {
				GDService.replyTo(application, gdServiceError, GDICCForegroundOptions.PreferPeerInForeground, new String[0], requestID);
			} catch (GDServiceException e) {
				// Do nothing
				e.printStackTrace();
			}
			return;
		}

		byte[] identificationData = (byte[]) paramsMap.get("identificationData");
		openFileToEdit(file, identificationData, application, onOpenFileToEditListener);

		// File was successfully received so send empty response as sign of success
		try {
			GDService.replyTo(application, null, GDICCForegroundOptions.PreferMeInForeground, new String[0], requestID);
		} catch (GDServiceException e) {
			// Do nothing
			e.printStackTrace();
		}
	}

	private void openFileToEdit(File file, byte[] identificationData, String fromAplication, OnOpenFileToEditListener onOpenFileToEditListener) {
		if (onOpenFileToEditListener != null) {
			onOpenFileToEditListener.onOpenFileToEdit(file, identificationData, fromAplication);
		}
	}

	@Override
	public void onMessageSent(String application, String requestID, String[] attachments) {
		Log.d("SecureGDFileOpener", String.format("Sent message to application=%s",
				application));
	}


}
