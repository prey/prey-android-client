package com.prey.actions.location;

public class PreyLocationManager {

	private PreyLocation lastLocation;
	private static PreyLocationManager _instance = null;

	private PreyLocationManager() {
	}

	public static PreyLocationManager getInstance() {
		if (_instance == null)
			_instance = new PreyLocationManager();
		return _instance;
	}

	public void setLastLocation(PreyLocation loc) {
		this.lastLocation = loc;
	}

	public PreyLocation getLastLocation() {
		return lastLocation == null ? new PreyLocation() : lastLocation;
	}

}
