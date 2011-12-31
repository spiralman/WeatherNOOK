package org.spiralman.WeatherNOOK;

import java.util.Map;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParserState;

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
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
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

