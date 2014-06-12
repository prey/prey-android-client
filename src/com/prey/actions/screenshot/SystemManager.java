package com.prey.actions.screenshot;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.app.Activity;

public class SystemManager extends Activity {

	public static void RootCommand(String command) throws Exception {
		Process process = null;
		DataOutputStream os = null;
		process = Runtime.getRuntime().exec("su");
		os = new DataOutputStream(process.getOutputStream());
		os.writeBytes(command + "\n");
		os.writeBytes("exit\n");
		os.flush();
		process.waitFor();
		os.close();
		process.destroy();
	}

	public static String readFile(File file) throws Exception {
		String data = null;
		InputStream is = new FileInputStream(file);
		int length = is.available();
		char[] buff = new char[length];
		InputStreamReader isr = new InputStreamReader(is);
		int a = isr.read(buff);
		data = String.valueOf(buff, 0, a);
		isr.close();
		is.close();
		return data;
	}

	public static void writeFile(File file, String data) throws Exception {
		OutputStream os = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(os);
		osw.write(data.toCharArray());
		osw.flush();
		osw.close();
		os.close();
	}
}
