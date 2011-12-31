package org.spiralman.WeatherNOOK;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.jsharkey.sky.webservice.Forecast;
import org.jsharkey.sky.webservice.Forecast.ParseException;
import org.jsharkey.sky.webservice.ForecastSource;
import org.jsharkey.sky.webservice.NoaaSource;
import org.jsharkey.sky.webservice.WebserviceHelper;
import org.spiralman.WeatherNOOK.Location.ForecastLocation;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class WeatherReport {
	private Collection<Forecast> m_forecast;
	
	private CurrentConditions m_currentConditions;
	
	public static WeatherReport getWeather(ForecastLocation location) throws ParseException, IOException, XmlPullParserException, java.text.ParseException {
		ForecastParser forecastParser = new ForecastParser();
		StackXmlParser conditionParser = new StackXmlParser();
		
		WeatherReport report = new WeatherReport();
		
		report.m_forecast = forecastParser.getForecasts(location.getLatitude(), location.getLongitude(), 5);
		report.m_currentConditions = new CurrentConditions();
		
		Log.d("ConditionParser", "Conditions from: " + location.getObservationStation().getUrl());
		
		Reader conditionXml = WebserviceHelper.queryApi(location.getObservationStation().getUrl());
		conditionParser.parseXml(conditionXml, new ConditionsInitialState(report.m_currentConditions));
		
		return report;
	}

	public Collection<Forecast> getForecast() {
		return m_forecast;
	}

	public void setForecast(Collection<Forecast> forecast) {
		m_forecast = forecast;
	}

	public CurrentConditions getCurrentConditions() {
		return m_currentConditions;
	}

	public void setCurrentConditions(CurrentConditions currentConditions) {
		m_currentConditions = currentConditions;
	}
}
