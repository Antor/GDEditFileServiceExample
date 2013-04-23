package com.example.texteditorforgood;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
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

	public static String readTextFile(File file) throws IOException {
		List<Byte> stringAsBytesList = new ArrayList<Byte>();
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);

			byte[] buffer = new byte[1024];
			int byteRead;
			while ((byteRead = inputStream.read(buffer)) != -1) {
				for (int i = 0; i < byteRead; i++) {
					stringAsBytesList.add(buffer[i]);
				}
			}

			byte[] stringAsBytesArray = new byte[stringAsBytesList.size()];
			for (int i = 0; i < stringAsBytesList.size(); i++) {
				stringAsBytesArray[i] = stringAsBytesList.get(i);
			}
			return new String(stringAsBytesArray, "UTF-8");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Do nothing
				}
			}
		}
	}

}
