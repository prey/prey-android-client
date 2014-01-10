package com.prey.analytics;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Random;

import android.content.Context;

import com.prey.FileConfigReader;
import com.prey.PreyLogger;
import com.prey.managers.PreyWindowsManager;
import com.prey.net.PreyRestHttpClient;

public class PreyGoogleAnalytics {

	private static PreyGoogleAnalytics instance = null;

	private PreyGoogleAnalytics() {

	}

	public static PreyGoogleAnalytics getInstance() {
		if (instance == null) {
			instance = new PreyGoogleAnalytics();
		}
		return instance;
	}

	private String refererURL = "http://beta.preyproject.com/";
	private static final String TRACKING_URL_Prefix = "http://www.google-analytics.com/__utm.gif";
	private static final Random random = new Random();
	private static final String NAME = "PreyJGoogleAnalytics";

	private String getHostName() {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			hostName = "localhost";
		}
		return hostName;
	}

	private static final String URI_SEPARATOR = "/";
	//private static final String TITLE_SEPARATOR = "-";

	private String buildURL(Context ctx, String page) {

		int cookie = random.nextInt();
		int randomValue = random.nextInt(2147483647) - 1;
		long now = new Date().getTime();

		StringBuffer url = new StringBuffer(TRACKING_URL_Prefix);
		url.append("?utmwv=1"); // Urchin/Analytics version
		url.append("&utmn=" + random.nextInt());
		url.append("&utmcs=UTF-8"); // document encoding
		url.append("&utmsr=" + PreyWindowsManager.getInstance(ctx).getWidth() + "x" + PreyWindowsManager.getInstance(ctx).getHeight()); // screen resolution
		url.append("&utmsc=32-bit"); // color depth
		url.append("&utmul=en-us"); // user language
		url.append("&utmje=1"); // java enabled
		url.append("&utmfl=9.0%20%20r28"); // flash
		url.append("&utmcr=1"); // carriage return
		url.append("&utmdt=" + getContentTitle()); // The optimum keyword density //document title
		url.append("&utmhn=" + getHostName());// document hostname
		url.append("&utmr=" + refererURL); // referer URL
		url.append("&utmp=" + getContentURI(page));// document page URL
		url.append("&utmac=" + FileConfigReader.getInstance(ctx).getAnalyticsUA());// Google Analytics account
		url.append("&utmcc=__utma%3D'" + cookie + "." + randomValue + "." + now + "." + now + "." + now);
		url.append(".2%3B%2B__utmb%3D" + cookie + "%3B%2B__utmc%3D" + cookie + "%3B%2B__utmz%3D" + cookie + ".");
		url.append(now + ".2.2.utmccn%3D(direct)%7Cutmcsr%3D(direct)%7Cutmcmd%3D(none)%3B%2B__utmv%3D" + cookie);
		return url.toString();
	}

	private String encode(String name) {
		try {
			return URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return name;
		}
	}

	private String getContentURI(String page) {
		StringBuffer contentURIBuffer = new StringBuffer();
		contentURIBuffer.append(URI_SEPARATOR);
		contentURIBuffer.append(encode(page));
		return contentURIBuffer.toString();
	}

	private String getContentTitle() {
		return encode(NAME);
	}

	public void trackSynchronously(Context ctx, String page) {
		/*PreyLogger.i("PreyJGoogleAnalytics: Tracking synchronously focusPoint=" + page);
		try {
			PreyRestHttpClient.getInstance(ctx).get(buildURL(ctx, page));
		} catch (IOException e) {
			PreyLogger.e("Error PreyGoogleAnalytics Synchronously:"+e.getMessage(),e);
		}*/

	}

	public void trackAsynchronously(Context ctx, String page) {
		/*PreyLogger.i("PreyJGoogleAnalytics: Tracking Asynchronously focusPoint=" + page);
		new TrackingThread(ctx, page).start();
		*/
	}

	private class TrackingThread extends Thread {
		private String page;
		private Context ctx;

		public TrackingThread(Context ctx, String page) {
			this.page = page;
			this.setPriority(Thread.MIN_PRIORITY);
			this.ctx = ctx;
		}

		public void run() {
			try {
				PreyRestHttpClient.getInstance(ctx).get(buildURL(ctx, page));
			} catch (IOException e) {
				PreyLogger.e("Error PreyGoogleAnalytics Asynchronously:"+e.getMessage(),e);
			}
		}
	}
}
