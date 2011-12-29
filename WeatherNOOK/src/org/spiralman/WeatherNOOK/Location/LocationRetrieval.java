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

import android.content.Context;
import android.os.Handler;

public class LocationRetrieval {
	private ObservationStationDB m_stationDB = null;
	
	public LocationRetrieval(Context context, Handler initializeHandler) {
		m_stationDB = new ObservationStationDB(context);
		m_stationDB.open(initializeHandler);
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
				
				String name = address.getString("formatted_address");
				double latitude = address.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
				double longitude = address.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
				
				ObservationStation closestStation = m_stationDB.getClosestStation(latitude, longitude);
				
				locations.add(new ForecastLocation(name, latitude, longitude, closestStation));
			}
		} catch(JSONException e) {
			throw new ParseException("Error parsing Location results: " + e.getMessage() );
		}
		
		return locations;
	}
}