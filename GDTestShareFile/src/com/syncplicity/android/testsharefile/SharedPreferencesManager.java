package com.syncplicity.android.testsharefile;

import android.content.Context;

public class SharedPreferencesManager {

	private static final String PREFRENCES_NAME = "SHARE_FILE";

	private static final String PATH_TO_LAST_SHARED_FILE = "path_to_last_shared_file";

	public static String getPathToLastSharedFile(Context context) {
		return context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE).getString(PATH_TO_LAST_SHARED_FILE, null);
	}

	public static void setPathToLastSharedFile(Context context, String path) {
		context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE).edit()
				.putString(PATH_TO_LAST_SHARED_FILE, path)
				.commit();
	}

	public static void clear(Context context) {
		context.getSharedPreferences(PREFRENCES_NAME, Context.MODE_PRIVATE).edit().clear().commit();
	}
}
