//package com.prey;
//
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//
//import android.content.Context;
//import android.location.Criteria;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.location.LocationProvider;
//import android.os.Bundle;
//import android.os.Handler;
//import android.widget.Toast;
//
//public class CustomLocationManager {
//
//	/**
//	 * The context that creat
//	 */
//	final WeakReference m_refContext;
//
//	/**
//	 * Last known location
//	 */
//	static Location m_lastKnownLocation;
//
//	/**
//	 * The location manager
//	 */
//	private static LocationManager m_locationManager;
//
//	/**
//	 * The location listener
//	 */
//	private LocationListener m_locationListener;
//
//	/**
//	 * Location provider connection flag
//	 */
//	private boolean m_providerConnected = false;
//
//	/**
//	 * The network provider
//	 */
//	private String m_provider;
//
//	/**
//	 * List of all the callbacks
//	 */
//	private static ArrayList m_callbacks;
//
//	public CustomLocationManager(Context context) {
//		super();
//		m_refContext = new WeakReference(context);
//
//	}
//
//	/**
//	 * Adds a new callback to the location manager
//	 * 
//	 * @param callback
//	 *            A LocationCallback
//	 */
//	public void addCallback(LocationCallback callback) {
//		if (m_callbacks == null) {
//			m_callbacks = new ArrayList();
//		}
//		m_callbacks.add(callback);
//		if (m_callbacks.size() == 1) {
//			connect();
//		} else {
//			updateLocation(callback);
//			updateProvider(callback);
//		}
//	}
//
//	/**
//	 * Removes a callback
//	 * 
//	 * @param callback
//	 *            A locationcallback that needs to be removed
//	 */
//	public void removeCallback(LocationCallback callback) {
//
//		if (m_callbacks == null) {
//			disconnect();
//		} else {
//			m_callbacks.remove(callback);
//			if (m_callbacks.size() == 0) {
//				disconnect();
//			}
//		}
//
//	}
//
//	private void connect() {
//
//		if (m_locationManager == null) {
//			m_locationManager = (LocationManager) m_refContext.get()
//					.getSystemService(Context.LOCATION_SERVICE);
//		}
//
//		if (m_locationListener == null) {
//			m_locationListener = new CustomLocationListener();
//		}
//
//		m_provider = m_locationManager.getBestProvider(
//				getLocationProviderCriteria(), true);
//		if (m_provider != null) {
//			updateProvider();
//			// Since we can’t update maps I want to return some kind of a result
//			// Later the user needs to refresh the map, to get the latest
//			// location
//			// On no maps it shouldn’t matter
//			m_lastKnownLocation = m_locationManager
//					.getLastKnownLocation(m_provider);
//			updateLocation();
//
//			m_locationManager.requestLocationUpdates(m_provider, 0, 0,
//					m_locationListener);
//			// updateLocation();
//			m_providerConnected = true;
//		} else {
//			m_lastKnownLocation = null;
//			m_providerConnected = false;
//		}
//
//	}
//
//	/**
//	 * Generate the location provider criteria
//	 * 
//	 * @return A location provider criteria
//	 */
//	private Criteria getLocationProviderCriteria() {
//
//		Criteria c = new Criteria();
//		c.setAccuracy(Criteria.ACCURACY_FINE);
//		c.setAltitudeRequired(false);
//		c.setCostAllowed(false);
//		c.setSpeedRequired(false);
//		return c;
//
//	}
//
//	/**
//	 * Disconnects from the location manager and stop reciving updates
//	 */
//	private void disconnect() {
//		if (m_locationManager != null) {
//			m_locationManager.removeUpdates(m_locationListener);
//		}
//	}
//
//	/**
//	 * Updates the location in all the callbacks
//	 */
//	void updateLocation() {
//
//		final Handler handler = new Handler();
//
//		for (int i = 0; i < m_callbacks.size(); i++) {
//			final LocationCallback callback = m_callbacks.get(i);
//			if (callback != null) {
//				handler.post(new Runnable() {
//					@Override
//					public void run() {
//						callback.onLocationChanged(m_lastKnownLocation);
//
//					}
//
//				});
//			}
//		}
//	}
//
//	/**
//	 * Sends a call back with the latest known location
//	 * 
//	 * @param callback
//	 *            The callback to send
//	 */
//	void updateLocation(final LocationCallback callback) {
//
//		new Handler().post(new Runnable() {
//
//			@Override
//			public void run() {
//				callback.onLocationChanged(m_lastKnownLocation);
//
//			}
//
//		});
//	}
//
//	/**
//	 * Updates the provider in all the callbacks
//	 */
//	void updateProvider() {
//
//		final Handler handler = new Handler();
//
//		for (int i = 0; i < m_callbacks.size(); i++) {
//			final LocationCallback callback = m_callbacks.get(i);
//			if (callback != null) {
//				handler.post(new Runnable() {
//
//					@Override
//					public void run() {
//						callback.onProviderChanged(m_provider);
//
//					}
//
//				});
//			}
//		}
//	}
//
//	/**
//	 * Sends a call back with the latest used provider
//	 * 
//	 * @param callback
//	 *            The call back to send
//	 */
//	void updateProvider(final LocationCallback callback) {
//
//		new Handler().post(new Runnable() {
//
//			@Override
//			public void run() {
//				callback.onProviderChanged(m_provider);
//
//			}
//
//		});
//	}
//
//	/**
//	 * The LocationListener of the thread
//	 * 
//	 * @author Ilan
//	 * 
//	 */
//	private class CustomLocationListener implements LocationListener {
//
//		@Override
//		public void onLocationChanged(Location loc) {
//			m_providerConnected = true;
//			if (loc != null) {
//				m_lastKnownLocation = loc;
//				updateLocation();
//
//			}
//		}
//
//		@Override
//		public void onProviderDisabled(String provider) {
//			if (m_refContext.get() != null) {
//				Toast.makeText(m_refContext.get(),
//						R.string.location_provider_disabled, Toast.LENGTH_LONG)
//						.show();
//			}
//			// connectToProvider();
//
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//			if (!m_providerConnected) {
//				m_providerConnected = true;
//				if (m_refContext.get() != null) {
//					Toast.makeText(
//							m_refContext.get(),
//							String.format(m_refContext.get().getString(
//									R.string.location_provider_reconnect),
//									provider), Toast.LENGTH_LONG).show();
//				}
//				m_provider = provider;
//			}
//			updateProvider();
//
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//
//			switch (status) {
//			case LocationProvider.TEMPORARILY_UNAVAILABLE:
//
//				m_providerConnected = false;
//
//			}
//		}
//
//	}
//
//	/**
//	 * Clean all the callback and disconnect from the location manager
//	 */
//	public void clean() {
//		m_callbacks.clear();
//		disconnect();
//	}
//
//	/**
//	 * Converts A Location to GeoPoint
//	 * 
//	 * @param Location
//	 *            The Location to be converted
//	 * @return A GeoPoint converted from Location object
//	 */
//	public static GeoPoint convertLocationToGeoPoint(Location location) {
//
//		final int latitude = new Double(
//				m_lastKnownLocation.getLatitude() * 1000000).intValue();
//		final int longtiude = new Double(
//				m_lastKnownLocation.getLongitude() * 1000000).intValue();
//		return new GeoPoint(latitude, longtiude);
//	}
//
// }