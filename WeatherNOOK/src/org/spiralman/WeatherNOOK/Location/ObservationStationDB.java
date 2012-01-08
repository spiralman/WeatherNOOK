package org.spiralman.WeatherNOOK.Location;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Map;

import org.spiralman.WeatherNOOK.XmlParser.StackXmlParser;
import org.spiralman.WeatherNOOK.XmlParser.StackXmlParserState;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class StationParserState extends StackXmlParserState {
	protected ObservationStationDB m_db = null;
	
	public StationParserState( ObservationStationDB db ) {
		m_db = db;
	}
}

class InitialState extends StationParserState {

	public InitialState(ObservationStationDB db) {
		super(db);
	}

	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("station") ) {
			return new StationTag(m_db);
		} else {
			return this;
		}
	}
}

class StationTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public StationTag(ObservationStationDB db) {
		super(db);
		
		m_station = new ObservationStation();
	}

	@Override
	public StackXmlParserState startNewTag(String tagName, Map<String,String> attributes) {
		if( tagName.equals("station_id") ) {
			return new IdTag(m_station,m_db);
		} else if( tagName.equals("station_name") ) {
			return new NameTag(m_station,m_db);
		} else if( tagName.equals("xml_url") ) {
			return new UrlTag(m_station,m_db);
		} else if( tagName.equals("latitude") ) {
			return new LatitudeTag(m_station,m_db);
		} else if( tagName.equals("longitude") ) {
			return new LongitudeTag(m_station,m_db);
		} else {
			return this;
		}
	}

	@Override
	public void endThisTag() {
		m_db.addStation(m_station);
	}
}

class IdTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public IdTag(ObservationStation station, ObservationStationDB db) {
		super(db);
		
		m_station = station;
	}
	
	public void text(String text) {
		m_station.setId(text);
	}
}

class NameTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public NameTag(ObservationStation station, ObservationStationDB db) {
		super(db);
		
		m_station = station;
	}
	
	public void text(String text) {
		m_station.setName(text);
	}
}

class UrlTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public UrlTag(ObservationStation station, ObservationStationDB db) {
		super(db);
		
		m_station = station;
	}
	
	public void text(String text) {
		m_station.setUrl(text);
	}
}

class LatitudeTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public LatitudeTag(ObservationStation station, ObservationStationDB db) {
		super(db);
		
		m_station = station;
	}
	
	public void text(String text) {
		m_station.setLatitude(Double.parseDouble(text));
	}
}

class LongitudeTag extends StationParserState {
	private ObservationStation m_station = null;
	
	public LongitudeTag(ObservationStation station, ObservationStationDB db) {
		super(db);
		
		m_station = station;
	}
	
	public void text(String text) {
		m_station.setLongitude(Double.parseDouble(text));
	}
}

public class ObservationStationDB extends SQLiteOpenHelper {
	private static final String DB_NAME = "WeatherNOOK";
	private static final int DB_VERSION = 1;
	
	private static final String STATION_TABLE = "Stations";
	private static final String STATION_ID_COL = "ID";
	private static final String STATION_NAME_COL = "Name";
	private static final String STATION_URL_COL = "URL";
	private static final String STATION_LATITUDE_COL = "Latitude";
	private static final String STATION_LONGITUDE_COL = "Longitude";
	
	public static final int INITIALIZE_SUCCESS = 0;
	public static final int INITIALIZE_STARTING = 1;
	public static final int INITIALIZE_ERROR = 2;
	
	private static final String CREATE_STATION_TABLE = 
			"CREATE TABLE " + STATION_TABLE + " ( " +
					STATION_ID_COL + " TEXT PRIMARY KEY, " +
					STATION_NAME_COL + " TEXT, " +
					STATION_URL_COL + " TEXT, " +
					STATION_LATITUDE_COL + " REAL, " +
					STATION_LONGITUDE_COL + " REAL" +
					");";
	
	private boolean m_newDB = false;
	private boolean m_isInitialized = false;
	
	private SQLiteDatabase m_db = null;

	public ObservationStationDB(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	public void open() {
		if( m_db == null ) {
			m_db = getWritableDatabase();
			
			// Will be set to true if onCreate got called (indicating that the DB is empty)
			if( !m_newDB ) {
				m_isInitialized = true;
			}
		}
	}
	
	public void close() {
		m_db.close();
		m_db = null;
	}

	public void addStation(ObservationStation station) {
		ContentValues values = new ContentValues();
		
		values.put(STATION_ID_COL, station.getId());
		values.put(STATION_NAME_COL, station.getName());
		values.put(STATION_URL_COL, station.getUrl());
		values.put(STATION_LATITUDE_COL, station.getLatitude());
		values.put(STATION_LONGITUDE_COL, station.getLongitude());
		
		m_db.insert(STATION_TABLE, null, values);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d("ObservationStationDB", "Creating DB");
		db.execSQL(CREATE_STATION_TABLE);
		m_newDB = true;
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO: Handle DB Upgrade
	}
	
	public boolean isInitialized() {
		return  m_isInitialized;
	}
	
	public void importStations(Reader stationXML) throws IOException, XmlPullParserException, ParseException {
		Log.d("ObservationStationDB", "Importing Stations");
		StackXmlParser parser = new StackXmlParser();
			
		parser.parseXml(stationXML, new InitialState(this));
		
		m_isInitialized = true;
	
		Log.d("ObservationStationDB", "Done importing stations");
	}
	
	public ObservationStation getClosestStation(double latitude, double longitude) {
		ObservationStation closest = null;
		double closestDistance = Double.MAX_VALUE;
		
		Cursor resultCursor = m_db.query(STATION_TABLE, 
				new String[] {STATION_ID_COL, STATION_NAME_COL, STATION_URL_COL, STATION_LATITUDE_COL, STATION_LONGITUDE_COL}, 
				String.format("abs(? - %s) < 1.0 AND abs(? - %s) < 1.0", STATION_LATITUDE_COL, STATION_LONGITUDE_COL), 
				new String[] {Double.toString(latitude), Double.toString(longitude)}, 
				null, null, null);
		
		while(resultCursor.moveToNext()) {
			ObservationStation current = new ObservationStation();
			current.setId(resultCursor.getString(resultCursor.getColumnIndex(STATION_ID_COL)));
			current.setName(resultCursor.getString(resultCursor.getColumnIndex(STATION_NAME_COL)));
			current.setUrl(resultCursor.getString(resultCursor.getColumnIndex(STATION_URL_COL)));
			current.setLatitude(resultCursor.getDouble(resultCursor.getColumnIndex(STATION_LATITUDE_COL)));
			current.setLongitude(resultCursor.getDouble(resultCursor.getColumnIndex(STATION_LONGITUDE_COL)));
			
			double distance = current.distanceFrom(latitude, longitude);
			
			if( distance < closestDistance ) {
				closest = current;
				closestDistance = distance;
			}
		}
		
		return closest;
	}
}
