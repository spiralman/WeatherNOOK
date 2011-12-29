package org.spiralman.WeatherNOOK.Location;

public class ForecastLocation {
	private ObservationStation m_station = null;
	
	private String m_formatted = "Uknown location";
	private double m_lat = 0.0;
	private double m_lng = 0.0;
	
	public ForecastLocation(String name, double latitude, double longitude, ObservationStation station)
	{
		m_formatted = name;
		m_lat = latitude;
		m_lng = longitude;
		m_station = station;
	}
	
	public String toString()
	{
		return m_formatted;
	}
	
	public double getLatitude() {
		return m_lat;
	}
	
	public double getLongitude() {
		return m_lng;
	}
	
	public ObservationStation getObservationStation() {
		return m_station;
	}
}
