package com.dev.bruno.sentimentanalysis.tweets.model;

import java.io.Serializable;

public class GeoLocation implements Serializable {

	private static final long serialVersionUID = 897104013820726318L;

	private Double latitude = 0d;
	
	private Double longitude = 0d;

	public GeoLocation(){}

	public GeoLocation(Double latitude, Double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}