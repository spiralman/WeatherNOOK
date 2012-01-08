package org.spiralman.WeatherNOOK.Location;

import org.json.JSONException;
import org.json.JSONObject;

public class ForecastLocation {
	private ObservationStation m_station = null;
	
	private String m_json = null;
	
	private String m_fullName = "Uknown location";
	private String m_city = "Unknown";
	private String m_state = "";
	private double m_lat = 0.0;
	private double m_lng = 0.0;
	
	private String m_shortName = "Unknown location";
	
	public ForecastLocation() {
	}
	
	public ForecastLocation(String json) throws JSONException {
		m_json = json;
		
		JSONObject location = new JSONObject(json);
		
		m_fullName = location.getString("full_name");
		m_city = location.getString("city");
		m_state = location.getString("state");
		m_lat = location.getDouble("latitude");
		m_lng = location.getDouble("longitude");
		
		m_station = new ObservationStation(location.getJSONObject("observation_station"));
		
		initialize();
	}
	
	public ForecastLocation(String city, String state, String name, double latitude, double longitude, ObservationStation station)
	{
		m_city = city;
		m_state = state;
		m_fullName = name;
		m_lat = latitude;
		m_lng = longitude;
		m_station = station;
		
		initialize();
	}
	
	private void initialize() {
		m_shortName = m_city + ", " + m_state;
	}
	
	public String toString()
	{
		return m_fullName;
	}
	
	public String getShortName() {
		return m_shortName;
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
	
	public String toJSON() throws JSONException {
		if( m_json == null ) {
			JSONObject location = new JSONObject();
			
			location.put("full_name", m_fullName);
			location.put("city", m_city);
			location.put("state", m_state);
			location.put("latitude", m_lat);
			location.put("longitude", m_lng);
			location.put("observation_station", m_station.toJSONObject());
			
			m_json = location.toString();
		}
		
		return m_json;
	}
}
