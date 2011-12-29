package org.spiralman.WeatherNOOK;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.jsharkey.sky.webservice.Forecast;
import org.jsharkey.sky.webservice.Forecast.ParseException;
import org.jsharkey.sky.webservice.ForecastSource;
import org.jsharkey.sky.webservice.NoaaSource;
import org.jsharkey.sky.webservice.WebserviceHelper;
import org.spiralman.WeatherNOOK.Location.ForecastLocation;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParser;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParserState;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

class ConditionsParserState extends StackXmlParserState {
	protected CurrentConditions m_conditions = null;
	
	ConditionsParserState(CurrentConditions conditions) {
		m_conditions = conditions;
	}
}

class ConditionsInitialState extends ConditionsParserState {
	ConditionsInitialState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName) {
		if( tagName.equals("weather") ) {
			Log.d("ConditionParser", "parsing weather tag");
			return new WeatherTagState(m_conditions);
		} else if( tagName.equals("temp_f")) {
			Log.d("ConditionParser", "parsing temperature tag");
			return new TemperatureTagState(m_conditions);
		} else if( tagName.equals("relative_humidity")) {
			Log.d("ConditionParser", "parsing humidity tag");
			return new HumidityTagState(m_conditions);
		} else if( tagName.equals("wind_dir")) {
			Log.d("ConditionParser", "parsing wind direction tag");
			return new WindDirectionTagState(m_conditions);
		} else if( tagName.equals("wind_mph")) {
			Log.d("ConditionParser", "parsing wind speed tag");
			return new WindSpeedTagState(m_conditions);
		} else {
			return this;
		}
	}
}

class WeatherTagState extends ConditionsParserState {
	WeatherTagState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public void text(String text) {
		Log.d("ConditionParser", "conditions: " + text);
		m_conditions.setConditions(text);
	}
}

class TemperatureTagState extends ConditionsParserState {
	TemperatureTagState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public void text(String text) {
		Log.d("ConditionParser", "temperature: " + text);
		m_conditions.setTemperature(Double.parseDouble(text));
	}
}

class HumidityTagState extends ConditionsParserState {
	HumidityTagState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public void text(String text) {
		Log.d("ConditionParser", "humidity: " + text);
		m_conditions.setHumidity(Integer.parseInt(text));
	}
}

class WindDirectionTagState extends ConditionsParserState {
	WindDirectionTagState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public void text(String text) {
		Log.d("ConditionParser", "wind direction: " + text);
		m_conditions.setWindDir(text);
	}
}

class WindSpeedTagState extends ConditionsParserState {
	WindSpeedTagState(CurrentConditions conditions) {
		super(conditions);
	}
	
	@Override
	public void text(String text) {
		Log.d("ConditionParser", "temperature: " + text);
		m_conditions.setWindSpeed(Double.parseDouble(text));
	}
}

public class WeatherReport {
	private List<Forecast> m_forecast;
	
	private CurrentConditions m_currentConditions;
	
	public static WeatherReport getWeather(ForecastLocation location) throws ParseException, IOException, XmlPullParserException {
		ForecastSource source = new NoaaSource();
		StackXmlParser conditionParser = new StackXmlParser();
		
		WeatherReport report = new WeatherReport();
		
		report.m_forecast = source.getForecasts(location.getLatitude(), location.getLongitude(), 5);
		report.m_currentConditions = new CurrentConditions();
		
		Log.d("ConditionParser", "Conditions from: " + location.getObservationStation().getUrl());
		
		Reader conditionXml = WebserviceHelper.queryApi(location.getObservationStation().getUrl());
		conditionParser.parseXml(conditionXml, new ConditionsInitialState(report.m_currentConditions));
		
		return report;
	}

	public List<Forecast> getForecast() {
		return m_forecast;
	}

	public void setForecast(List<Forecast> forecast) {
		m_forecast = forecast;
	}

	public CurrentConditions getCurrentConditions() {
		return m_currentConditions;
	}

	public void setCurrentConditions(CurrentConditions currentConditions) {
		m_currentConditions = currentConditions;
	}
}
