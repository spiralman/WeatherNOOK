package org.spiralman.WeatherNOOK.Location;

public class ObservationStation {
	private String m_id = null;
	private String m_name = null;
	private String m_url = null;
	private double m_latitude = 0.0;
	private double m_longitude = 0.0;
	
	public String getId() {
		return m_id;
	}
	
	public void setId(String id) {
		m_id = id;
	}
	
	public String getName() {
		return m_name;
	}
	
	public void setName(String name) {
		m_name = name;
	}
	
	public String getUrl() {
		return m_url;
	}
	
	public void setUrl(String url) {
		m_url = url;
	}
	
	public double getLatitude() {
		return m_latitude;
	}
	
	public void setLatitude(double latitude) {
		m_latitude = latitude;
	}
	
	public double getLongitude() {
		return m_longitude;
	}
	
	public void setLongitude(double longitude) {
		m_longitude = longitude;
	}

	public double distanceFrom(double latitude, double longitude) {
		return Math.sqrt(Math.pow(m_latitude - latitude, 2) + Math.pow(m_longitude - longitude, 2));
	}
}
