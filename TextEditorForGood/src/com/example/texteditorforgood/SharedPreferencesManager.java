package com.example.texteditorforgood;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.good.gd.file.File;

public class SharedPreferencesManager {

	private static final String PREFRENCES_NAME = "TEXT_EDITOR_PREFRENCES";

	private static final String LAST_OPENED_FILE_KEY = "last_opened_file";
	private static final String IDENTIFICATION_DATA_KEY = "identification_data";

	public static File getLastOpenedFile(Context context) {
		String path = context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE).getString(LAST_OPENED_FILE_KEY, null);
		return path != null ? new File(path) : null;
	}

	public static byte[] getLastOpenedFileIdentificationData(Context context) {
		String encodedIdentificationData = context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE).getString(IDENTIFICATION_DATA_KEY, null);
		byte[] identificationData = encodedIdentificationData == null ? null : Base64.decode(encodedIdentificationData, Base64.DEFAULT);
		return identificationData;
	}

	public static void setLastOpenedFile(Context context, File lastOpenedFile, byte[] identificationData) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE);

		String encodedIdentificationData = identificationData == null ? null : Base64.encodeToString(identificationData, Base64.DEFAULT);

		sharedPreferences.edit().putString(LAST_OPENED_FILE_KEY, lastOpenedFile.getAbsolutePath())
				.putString(IDENTIFICATION_DATA_KEY, encodedIdentificationData).commit();
	}


}
