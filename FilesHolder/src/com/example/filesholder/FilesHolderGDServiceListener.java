package com.example.filesholder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
import com.good.gd.file.FileOutputStream;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class FilesHolderGDServiceListener implements GDServiceListener {

	private static final String SAVE_EDITED_FILE_SERVICE_ID = "com.good.gdservice.save-edited-file";
	private static final String SAVE_EDITED_FILE_SERVICE_VERSION_1_0_0_1 = "1.0.0.1";
	private static final String SAVE_EDIT_METHOD = "saveEdit";

	private static FilesHolderGDServiceListener instance_;

	public static FilesHolderGDServiceListener getInstance() {
		if (instance_ == null) {
			instance_ = new FilesHolderGDServiceListener();
		}
		return instance_;
	}

	private FilesHolderGDServiceListener() {
	}

	@Override
	public void onReceiveMessage(String application, String service, String version, String method, Object params,
			String[] attachments, String requestID) {
		if (service.equals(SAVE_EDITED_FILE_SERVICE_ID) && version.equals(SAVE_EDITED_FILE_SERVICE_VERSION_1_0_0_1)) {
			if (method.equals(SAVE_EDIT_METHOD) && attachments != null && attachments.length == 1) {
				File editedFile = new File(attachments[0]);
				try {
					copyFile(editedFile, new File(FilesHolderMainActivity.PATH_TO_TEST_TXT_FILE));
				} catch (IOException e) {
					Log.e(FilesHolderMainActivity.TAG, e.getMessage(), e);
				}
				// TODO notify application to make changes visible
			}

			Object emptyResponse = null;
			try {
				GDService.replyTo(application, emptyResponse, GDICCForegroundOptions.PreferMeInForeground,
						new String[0], requestID);
			} catch (GDServiceException e) {
				Log.e(FilesHolderMainActivity.TAG, e.getMessage(), e);
			}
		}
	}
	
	private void copyFile(File source, File destination) throws IOException {
		InputStream sourceStream = null;
		OutputStream output = null;
		try {
			sourceStream = new FileInputStream(source);
			output = new FileOutputStream(destination);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			while ((bytesRead = sourceStream.read(buffer)) > 0) {
				output.write(buffer, 0, bytesRead);
			}

		} finally {
			try {
				if (sourceStream != null) {
					sourceStream.close();
				}
			} catch (IOException e) {
				// Do nothing
			}
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				// Do nothing
			}
		}
	}

	@Override
	public void onMessageSent(String application, String requestID, String[] attachments) {
		// Do nothing
	}
}
