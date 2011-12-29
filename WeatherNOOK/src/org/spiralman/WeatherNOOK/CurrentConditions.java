package org.spiralman.WeatherNOOK;

public class CurrentConditions {
	private String m_conditions = "Unknown";
	private double m_temperature = 0.0;
	private double m_windSpeed = 0.0;
	private String m_windDir = "";
	private int m_humidity = -1;
	
	public String getConditions() {
		return m_conditions;
	}
	
	public void setConditions(String conditions) {
		m_conditions = conditions;
	}
	
	public double getTemperature() {
		return m_temperature;
	}
	
	public void setTemperature(double temperature) {
		m_temperature = temperature;
	}

	public double getWindSpeed() {
		return m_windSpeed;
	}

	public void setWindSpeed(double windSpeed) {
		m_windSpeed = windSpeed;
	}
	
	public String getWindDirAbbreviation() {
		String abbreviated = "";
		String direction = m_windDir.toLowerCase();
		
		if( direction.contains("north")) {
			abbreviated = "N";
		} else if( direction.contains("south")) {
			abbreviated = "S";
		}
		
		if( direction.contains("east")) {
			abbreviated += "E";
		} else if( direction.contains("west")) {
			abbreviated += "W";
		}
		
		return abbreviated;
	}

	public String getWindDir() {
		return m_windDir;
	}

	public void setWindDir(String windDir) {
		m_windDir = windDir;
	}

	public int getHumidity() {
		return m_humidity;
	}

	public void setHumidity(int humidity) {
		m_humidity = humidity;
	}
}
