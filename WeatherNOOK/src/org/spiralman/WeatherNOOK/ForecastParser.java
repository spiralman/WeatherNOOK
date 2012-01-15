package org.spiralman.WeatherNOOK;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jsharkey.sky.webservice.Forecast;
import org.jsharkey.sky.webservice.WebserviceHelper;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParser;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParserState;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

class TimeRange {
	public Date start;
	public Date end;

}

class ForecastParserState extends StackXmlParserState {
	protected ForecastParser m_parser;

	public ForecastParserState(ForecastParser parser) {
		m_parser = parser;
	}
}

class ForecastParserInitialState extends ForecastParserState implements
		ValueReceiver {
	public ForecastParserInitialState(ForecastParser parser) {
		super(parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("time-layout")) {
			return new TimeLayoutTag(m_parser);
		} else if (tagName.equals("temperature")) {
			if (attributes.get("type").equals("maximum")) {
				return new MaxTempState(attributes.get("time-layout"), m_parser);
			} else if (attributes.get("type").equals("minimum")) {
				return new MinTempState(attributes.get("time-layout"), m_parser);
			} else {
				return this;
			}
		} else if (tagName.equals("probability-of-precipitation")) {
			return new ProbabilityOfPrecipitation(
					attributes.get("time-layout"), m_parser);
		} else if (tagName.equals("weather")) {
			return new WeatherState(attributes.get("time-layout"), m_parser);
		} else if (tagName.equals("hazards")) {
			return new HazardsState(attributes.get("time-layout"), m_parser);
		} else if (tagName.equals("moreWeatherInformation")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}

	public void newValue(String value) {
		m_parser.setUrl(value);
	}
}

class TimeLayoutTag extends ForecastParserState {
	String key = null;
	List<TimeRange> ranges = new ArrayList<TimeRange>();

	private static final SimpleDateFormat m_dateParser = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss");

	private TimeRange m_currentRange = null;

	public TimeLayoutTag(ForecastParser parser) {
		super(parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("layout-key")) {
			return new LayoutKeyTag(this, m_parser);
		} else if (tagName.equals("start-valid-time")) {
			return new StartTimeTag(this, m_parser);
		} else if (tagName.equals("end-valid-time")) {
			return new EndTimeTag(this, m_parser);
		} else {
			return this;
		}
	}

	@Override
	public void endThisTag() {
		m_parser.addTimeLayout(key, ranges);
	}

	void newStartTime(Date t) {
		m_currentRange = new TimeRange();
		m_currentRange.start = t;
	}

	void newEndTime(Date t) {
		m_currentRange.end = t;
		ranges.add(m_currentRange);
		m_currentRange = null;
	}

	Date parseTime(String timeString) throws ParseException {
		return m_dateParser.parse(timeString);
	}
}

class LayoutKeyTag extends ForecastParserState {
	private TimeLayoutTag m_layout;

	public LayoutKeyTag(TimeLayoutTag layout, ForecastParser parser) {
		super(parser);
		m_layout = layout;
	}

	@Override
	public void text(String text) {
		m_layout.key = text;
	}
}

class StartTimeTag extends ForecastParserState {
	private TimeLayoutTag m_layout;

	public StartTimeTag(TimeLayoutTag layout, ForecastParser parser) {
		super(parser);
		m_layout = layout;
	}

	@Override
	public void text(String text) throws ParseException {
		m_layout.newStartTime(m_layout.parseTime(text));
	}
}

class EndTimeTag extends ForecastParserState {
	private TimeLayoutTag m_layout;

	public EndTimeTag(TimeLayoutTag layout, ForecastParser parser) {
		super(parser);
		m_layout = layout;
	}

	@Override
	public void text(String text) throws ParseException {
		m_layout.newEndTime(m_layout.parseTime(text));
	}
}

class TimeSeriesState extends ForecastParserState {
	protected String m_layoutKey = null;

	protected int m_currentIndex = 0;

	public TimeSeriesState(String layoutKey, ForecastParser parser) {
		super(parser);
		m_layoutKey = layoutKey;
	}

	public Date currentStartTime() {
		return m_parser.getStartTime(m_layoutKey, m_currentIndex);
	}

	public Forecast currentForecast() {
		return m_parser.getForecast(m_layoutKey, m_currentIndex);
	}
}

interface ValueReceiver {
	public void newValue(String value);
}

class ValueTag extends StackXmlParserState {
	private ValueReceiver m_parent;

	public ValueTag(ValueReceiver parent) {
		m_parent = parent;
	}

	public void text(String text) {
		m_parent.newValue(text);
	}
}

class MaxTempState extends TimeSeriesState implements ValueReceiver {
	public MaxTempState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}

	public void newValue(String value) {
		currentForecast().setTempHigh(Integer.parseInt(value));
		m_currentIndex++;
	}
}

class MinTempState extends TimeSeriesState implements ValueReceiver {
	public MinTempState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}

	public void newValue(String value) {
		currentForecast().setTempLow(Integer.parseInt(value));
		m_currentIndex++;
	}
}

class ProbabilityOfPrecipitation extends TimeSeriesState implements
		ValueReceiver {
	public ProbabilityOfPrecipitation(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}

	public void newValue(String value) {
		if (currentStartTime().getHours() < 12) {
			currentForecast().setMorningPrecip(Integer.parseInt(value));
		} else {
			currentForecast().setEveningPrecip(Integer.parseInt(value));
		}

		m_currentIndex++;
	}
}

class WeatherState extends TimeSeriesState {
	public WeatherState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("weather-conditions")) {
			Forecast current = currentForecast();
			// TODO: Better would be to store alert objects with a forecast.
			// Especially since there can,
			// technically, be multiple alerts per day. However, this is how
			// jsharkey's code stores it,
			// and we don't really have a way to display multiple alerts yet,
			// anyway.
			if (!current.hasAlert()) {
				currentForecast().setConditions(
						attributes.get("weather-summary"));
			}
			m_currentIndex++;
		}

		return this;
	}
}

class HazardsState extends TimeSeriesState {
	public HazardsState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("hazard-conditions")) {
			return new HazardConditionsState(this);
		}

		return this;
	}
}

class HazardConditionsState extends StackXmlParserState implements
		ValueReceiver {
	private HazardsState m_parent;

	public HazardConditionsState(HazardsState parent) {
		m_parent = parent;
	}

	@Override
	public StackXmlParserState startNewTag(String tagName,
			Map<String, String> attributes) {
		if (tagName.equals("hazard")) {
			Forecast current = m_parent.currentForecast();
			current.setAlert(true);
			current.setConditions(attributes.get("phenomena") + " "
					+ attributes.get("significance"));
		} else if (tagName.equals("hazardTextURL")) {
			return new ValueTag(this);
		}

		return this;
	}

	public void endThisTag() {
		m_parent.m_currentIndex++;
	}

	public void newValue(String value) {
		// Only the hazard URL is encoded as a text value.
		m_parent.currentForecast().setUrl(value.trim());
	}
}

public class ForecastParser {
	static final String WEBSERVICE_URL = "http://www.weather.gov/forecasts/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php?&lat=%f&lon=%f&format=24+hourly&numDays=%d";

	private Map<Date, Forecast> m_forecasts = new TreeMap<Date, Forecast>();
	private Map<String, List<TimeRange>> m_timeLayouts = new HashMap<String, List<TimeRange>>();

	private String m_url = "http://www.weather.gov/";

	void addTimeLayout(String key, List<TimeRange> ranges) {
		m_timeLayouts.put(key, ranges);
	}

	Forecast getForecast(String layout, int index) {
		Date periodStart = (Date) m_timeLayouts.get(layout).get(index).start
				.clone();
		periodStart.setHours(0);
		periodStart.setMinutes(0);
		periodStart.setSeconds(0);

		if (m_forecasts.containsKey(periodStart)) {
			return m_forecasts.get(periodStart);
		} else {
			Forecast forecast = new Forecast();
			forecast.setValidStart(periodStart.getTime());
			m_forecasts.put(periodStart, forecast);
			return forecast;
		}
	}

	Date getStartTime(String layout, int index) {
		return m_timeLayouts.get(layout).get(index).start;
	}

	void setUrl(String url) {
		m_url = url;
	}

	public String getUrl() {
		// TODO: Find a better way to return the URL with the forecast list.
		return m_url;
	}

	public Collection<Forecast> getForecasts(double latitude, double longitude,
			int numDays)
			throws org.jsharkey.sky.webservice.Forecast.ParseException,
			IOException, XmlPullParserException, ParseException {
		StackXmlParser xmlParser = new StackXmlParser();
		
		String forecastURL = String.format(WEBSERVICE_URL,
				latitude, longitude, numDays);
		
		Log.d("ForecastParser", "Fetching forecast from: " + forecastURL);

		Reader reader = WebserviceHelper.queryApi(forecastURL);

		xmlParser.parseXml(reader, new ForecastParserInitialState(this));
		
		for( Map.Entry<Date, Forecast> forecast : m_forecasts.entrySet() ) {
			if( !forecast.getValue().isPopulated() ) {
				m_forecasts.remove(forecast.getKey());
			}
		}

		return m_forecasts.values();
	}
}
