package com.utahere.objects;

import java.util.Date;
import java.util.Map;

public class SatalliteSystemObj {
	public String name;
	public Date timestamp;
	public String GPSWeek;
	public Map<String, VehicleObj> vehicleMap;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getGPSWeek() {
		return GPSWeek;
	}
	public void setGPSWeek(String gPSWeek) {
		GPSWeek = gPSWeek;
	}
	public Map<String, VehicleObj> getVehicleMap() {
		return vehicleMap;
	}
	public void setVehicleMap(Map<String, VehicleObj> vehicleMap) {
		this.vehicleMap = vehicleMap;
	}
}
