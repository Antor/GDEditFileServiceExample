package com.syncplicity.android.testsharefile;

import java.io.IOException;
import java.io.OutputStream;

import com.good.gd.file.File;
import com.good.gd.file.FileOutputStream;

public class GDFileUtils {

	public static void createTextFile(File file, String content) throws IOException {
		String parent = file.getParent();
		if (parent != null) {
			File parentfile = new File(parent);
			parentfile.mkdirs();
		}

		byte[] data = content.getBytes("UTF-8");
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(data);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
	}

}
