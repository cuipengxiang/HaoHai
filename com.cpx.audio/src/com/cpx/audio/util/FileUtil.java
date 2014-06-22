package com.cpx.audio.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

	public FileUtil() {
		// TODO Auto-generated constructor stub
	}

	public static String readFile(String filename) {
		String jsonString = new String();
		try {
			InputStream instream = new FileInputStream(filename);
			if (instream != null) {
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);
				String line;
				while ((line = buffreader.readLine()) != null) {
					jsonString = jsonString + line + "\n";
				}
				buffreader.close();
			}
			instream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return jsonString;
	}
}
