package com.utahere.objects;

import java.util.Calendar;

public class VehicleLiteObj {
    public String system;
	public String vehicleId;

	//geographical coordinates:
	public double latitude;
	public double longitude;

    public Calendar date;

    public VehicleLiteObj() {
		super();
	}

    public String getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
