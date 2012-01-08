package org.spiralman.WeatherNOOK.Location;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsharkey.sky.webservice.WebserviceHelper;
import org.jsharkey.sky.webservice.Forecast.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LocationRetrieval {
	private ObservationStationDB m_stationDB = null;
	
	public LocationRetrieval(ObservationStationDB stationDB) {
		m_stationDB = stationDB;
	}
	
	public ObservationStationDB getObservationStationDB() {
		return m_stationDB;
	}
	
	public List<ForecastLocation> getLocations(String searchString) throws IOException, ParseException {
		String result = WebserviceHelper.readApi(String.format("http://maps.googleapis.com/maps/api/geocode/json?address=%s&sensor=false", URLEncoder.encode(searchString)));
		List<ForecastLocation> locations = new ArrayList<ForecastLocation>();
		
		try {
			JSONObject resultObject = new JSONObject(result);
			
			JSONArray results = resultObject.getJSONArray("results");
			
			for( int i = 0; i < results.length(); ++i )
			{
				JSONObject address = results.getJSONObject(i);
				
				String fullName = address.getString("formatted_address");
				String city = null;
				String state = null;
				
				JSONArray addressComponents = address.getJSONArray("address_components");
				for( int ac = 0; i < addressComponents.length(); ++ac ) {
					JSONObject component = addressComponents.getJSONObject(ac);
					
					JSONArray types = component.getJSONArray("types");
					
					for( int t = 0; t < types.length(); ++t ) {
						if( types.getString(t).equals("administrative_area_level_1") ) {
							state = component.getString("short_name");
							break;
						} else if( types.getString(t).equals("locality") ) {
							city = component.getString("long_name");
							break;
						}
					}
					
					if( city != null && state != null ) {
						break;
					}
				}
				
				double latitude = address.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
				double longitude = address.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
				
				ObservationStation closestStation = m_stationDB.getClosestStation(latitude, longitude);
				
				locations.add(new ForecastLocation(city, state, fullName, latitude, longitude, closestStation));
			}
		} catch(JSONException e) {
			throw new ParseException("Error parsing Location results: " + e.getMessage() );
		}
		
		return locations;
	}
}