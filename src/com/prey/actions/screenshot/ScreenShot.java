package com.prey.actions.screenshot;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


public class ScreenShot {
	public static int width = 0;
	public static int height = 0;
	public static int deepth = 0;
	public static final String file_name = "/dev/graphics/fb0";
	public static int alpha = 0;
	//public static final String copyPermission = "cat /dev/graphics/fb0 > /sdcard/frame.raw";
	
	public static final String getPermission = "chmod 777 /dev/graphics/fb0\n";
	public static final String givePermission = "chmod 660 /dev/graphics/fb0\n";

	public static void init(Context context) throws Exception {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = WM.getDefaultDisplay();
		display.getMetrics(metrics);
		height = metrics.heightPixels;
		width = metrics.widthPixels;
		int pixelformat = display.getPixelFormat();
		PixelFormat localPixelFormat1 = new PixelFormat();
		PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);
		deepth = localPixelFormat1.bytesPerPixel;
	}

	public static Bitmap getScreenBitmap(Context context) throws Exception {
		SystemManager.RootCommand(getPermission);
		init(context);
		byte[] piex = new byte[width * height * deepth];
		InputStream stream = new FileInputStream(new File(file_name));
		DataInputStream dStream = new DataInputStream(stream);
		dStream.readFully(piex);
		dStream.close();
		stream.close();
		SystemManager.RootCommand(givePermission);
		int[] data = convertToColor(piex);
		return Bitmap.createBitmap(data, width, height, Bitmap.Config.ARGB_8888);
	}

	public static InputStream getInputStream() throws Exception {
		FileInputStream buf = new FileInputStream(new File(file_name));
		return buf;
	}

	public static int[] convertToColor(byte[] piex) throws Exception {
		switch (deepth) {
		case 2:
			return convertToColor_2byte(piex);
		case 3:
			return convertToColor_3byte(piex);
		case 4:
			return convertToColor_4byte(piex);
		default:
			throw new Exception("Deepth Error!");
		}
	}

	public static int[] convertToColor_2byte(byte[] piex) {
		int[] colors = new int[width * height];
		int len = piex.length;
		for (int i = 0; i < len; i += 2) {
			int colour = (piex[i + 1] & 0xFF) << 8 | (piex[i] & 0xFF);
			int r = ((colour & 0xF800) >> 11) * 8;
			int g = ((colour & 0x07E0) >> 5) * 4;
			int b = (colour & 0x001F) * 8;
			int a = 0xFF;
			colors[i / 2] = (a << 24) + (r << 16) + (g << 8) + b;
		}
		return colors;
	}

	public static int[] convertToColor_3byte(byte[] piex) {
		int[] colors = new int[width * height];
		int len = piex.length;
		for (int i = 0; i < len; i += 3) {
			int r = (piex[i] & 0xFF);
			int g = (piex[i + 1] & 0xFF);
			int b = (piex[i + 2] & 0xFF);
			int a = 0xFF;
			colors[i / 3] = (a << 24) + (r << 16) + (g << 8) + b;
		}
		return colors;
	}

	public static int[] convertToColor_4byte(byte[] piex) {
		int[] colors = new int[width * height];
		int len = piex.length;
		for (int i = 0; i < len; i += 4) {
			int r = (piex[i] & 0xFF);
			int g = (piex[i + 1] & 0xFF);
			int b = (piex[i + 2] & 0xFF);
			int a = (piex[i + 3] & 0xFF);
			colors[i / 4] = (a << 24) + (r << 16) + (g << 8) + b;
		}
		return colors;
	}

}
