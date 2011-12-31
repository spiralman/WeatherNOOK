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

class ForecastParserInitialState extends ForecastParserState {
	public ForecastParserInitialState(ForecastParser parser) {
		super(parser);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("time-layout")) {
			return new TimeLayoutTag(m_parser);
		} else if( tagName.equals("temperature") ) {
			if( attributes.get("type").equals("maximum")) {
				return new MaxTempState(attributes.get("time-layout"), m_parser);
			} else if( attributes.get("type").equals("minimum")) {
				return new MinTempState(attributes.get("time-layout"), m_parser);
			} else {
				return this;
			}
		} else if( tagName.equals("probability-of-precipitation") ) {
			return new ProbabilityOfPrecipitation(attributes.get("time-layout"), m_parser);
		} else {
			return this;
		}
	}
}

class TimeLayoutTag extends ForecastParserState {
	String key = null;
	List<TimeRange> ranges = new ArrayList<TimeRange>();
	
	private SimpleDateFormat m_dateParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); 
	
	private TimeRange m_currentRange = null;
	
	public TimeLayoutTag(ForecastParser parser) {
		super(parser);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("layout-key")) {
			return new LayoutKeyTag(this, m_parser);
		} else if( tagName.equals("start-valid-time") ) {
			return new StartTimeTag(this,m_parser);
		} else if( tagName.equals("end-valid-time") ) {
			return new EndTimeTag(this,m_parser);
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

abstract class TimeSeriesState extends ForecastParserState {
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
	
	abstract public void newValue(String value);
}

class ValueTag extends StackXmlParserState {
	private TimeSeriesState m_parent;
	
	public ValueTag(TimeSeriesState parent) {
		m_parent = parent;
	}
	
	public void text(String text) {
		m_parent.newValue(text);
	}
}

class MaxTempState extends TimeSeriesState {
	public MaxTempState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}
	
	@Override
	public void newValue(String value) {
		currentForecast().setTempHigh(Integer.parseInt(value));
		m_currentIndex++;
	}
}

class MinTempState extends TimeSeriesState {
	public MinTempState(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}
	
	@Override
	public void newValue(String value) {
		currentForecast().setTempLow(Integer.parseInt(value));
		m_currentIndex++;
	}
}

class ProbabilityOfPrecipitation extends TimeSeriesState {
	public ProbabilityOfPrecipitation(String layoutKey, ForecastParser parser) {
		super(layoutKey, parser);
	}
	
	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("value")) {
			return new ValueTag(this);
		} else {
			return this;
		}
	}
	
	@Override
	public void newValue(String value) {
		if( currentStartTime().getHours() < 12 ) {
			currentForecast().setMorningPrecip(Integer.parseInt(value));
		} else {
			currentForecast().setEveningPrecip(Integer.parseInt(value));
		}
		
		m_currentIndex++;
	}
}

public class ForecastParser {
	static final String WEBSERVICE_URL = "http://www.weather.gov/forecasts/xml/sample_products/browser_interface/ndfdBrowserClientByDay.php?&lat=%f&lon=%f&format=24+hourly&numDays=%d";
	
	private Map<Date, Forecast> m_forecasts = new TreeMap<Date, Forecast>();
	private Map<String, List<TimeRange> > m_timeLayouts = new HashMap<String, List<TimeRange> >();

	void addTimeLayout(String key, List<TimeRange> ranges) {
		m_timeLayouts.put(key, ranges);
	}

	Forecast getForecast(String layout, int index) {
		Date periodStart = (Date) m_timeLayouts.get(layout).get(index).start.clone();
		periodStart.setHours(0);
		periodStart.setMinutes(0);
		periodStart.setSeconds(0);
		
		if( m_forecasts.containsKey(periodStart) ) {
			return m_forecasts.get(periodStart);
		} else {
			Forecast forecast = new Forecast();
			forecast.setValidStart(periodStart.getTime());
			m_forecasts.put(periodStart, forecast);
			return forecast;
		}
	}
	
	Date getStartTime( String layout, int index ) {
		return m_timeLayouts.get(layout).get(index).start;
	}
	
	public Collection<Forecast> getForecasts(double latitude, double longitude, int numDays) throws org.jsharkey.sky.webservice.Forecast.ParseException, IOException, XmlPullParserException, ParseException {
		StackXmlParser xmlParser = new StackXmlParser();
		
		Reader reader = WebserviceHelper.queryApi(String.format(WEBSERVICE_URL, latitude, longitude, numDays));
		
		xmlParser.parseXml(reader, new ForecastParserInitialState(this));
		
		return m_forecasts.values();
	}
}
