package org.spiralman.WeatherNOOK;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsharkey.sky.ForecastUtils;
import org.jsharkey.sky.webservice.Forecast;
import org.jsharkey.sky.webservice.WebserviceHelper;
import org.spiralman.WeatherNOOK.Location.LocationRetrieval;
import org.spiralman.WeatherNOOK.Location.ForecastLocation;
import org.spiralman.WeatherNOOK.Location.ObservationStation;
import org.spiralman.WeatherNOOK.Location.ObservationStationDB;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class WeatherNOOKActivity extends Activity {
	private final int REFRESH_DIALOG = 0;
	private final int CONFIGURE_DIALOG = 1;
	private final int ERROR_DIALOG = 2;
	
	private final int FETCH_SUCCESS = 0;
	private final int FETCH_ERROR = 1;
	
	private final String LATITUDE_KEY = "ForecastLatitude";
	private final String LONGITUDE_KEY = "ForecastLongitude";
	private final String LOCATION_KEY = "ForecastLocation";
	
	private static final String STATION_ID_KEY = "StationID";
	private static final String STATION_NAME_KEY = "StationName";
	private static final String STATION_URL_KEY = "StationURL";
	private static final String STATION_LATITUDE_KEY = "StationLatitude";
	private static final String STATION_LONGITUDE_KEY = "StationLongitude";
	
	private ProgressDialog m_progressDialog = null;
	private RefreshThread m_refreshThread = null;
	private AlertDialog m_configDialog = null;
	
	private ForecastLocation m_location = null;
	
	private String m_lastError = null;
	
	private String m_refreshMessage = null;
	
	private String m_locationFormat = null;
	private String m_tempFormat = null;
	private String m_windFormat = null;
	private String m_humidityFormat = null;
	
	private LocationRetrieval m_locationRetrieval = null;
	
	private Runnable m_onStationDBInitComplete = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ForecastBinder.prepareFormatStrings(this);
        WebserviceHelper.prepareUserAgent(this);
        
        loadLocation();
        
        m_locationRetrieval = new LocationRetrieval(this, dbInitializeHandler);
        
        m_locationFormat = getString(R.string.locationFormat);
        m_tempFormat = getString(R.string.currentTempFormat);
        m_windFormat = getString(R.string.windFormat);
        m_humidityFormat = getString(R.string.humidityFormat);
        
        setContentView(R.layout.main);
        
        View current = findViewById(R.id.currentConditionLayout);
        current.setVisibility(View.INVISIBLE);
        
        if( m_location != null ) {
        	refresh();
        } else {
        	showConfigDialog();
        }
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	m_locationRetrieval.getObservationStationDB().close();
    }
    
    private void refresh() {
    	if( m_location != null) {
    		TextView location = (TextView) findViewById(R.id.currentLocation);
	        location.setText(String.format(m_locationFormat, m_location));
	        m_refreshMessage = String.format("Loading Forecast for %s...", m_location.toString());
    		showDialog(REFRESH_DIALOG);
    		
    		m_progressDialog.setProgress(0);
        	m_refreshThread = new RefreshThread(refreshHandler);
        	m_refreshThread.start();
    	}
    }
    
    private void saveLocation() {
    	SharedPreferences settings = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString(LOCATION_KEY, m_location.toString());
		editor.putFloat(LATITUDE_KEY, (float)m_location.getLatitude());
        editor.putFloat(LONGITUDE_KEY, (float)m_location.getLongitude());
        
        editor.putString(STATION_ID_KEY, m_location.getObservationStation().getId());
        editor.putString(STATION_NAME_KEY, m_location.getObservationStation().getName());
        editor.putString(STATION_URL_KEY, m_location.getObservationStation().getUrl());
        
        editor.putFloat(STATION_LATITUDE_KEY, (float)m_location.getObservationStation().getLatitude());
        editor.putFloat(STATION_LONGITUDE_KEY, (float)m_location.getObservationStation().getLongitude());
        
        editor.commit();
    }
    
    private void loadLocation() {
    	SharedPreferences settings = getPreferences(MODE_PRIVATE);
    	
    	if( settings.contains(LOCATION_KEY) ) {
	    	
	        String locationName = settings.getString(LOCATION_KEY, "Unknown Location");
	        double latitude = settings.getFloat(LATITUDE_KEY, 0.0F);
	        double longitude = settings.getFloat(LONGITUDE_KEY, 0.0F);
	        
	        ObservationStation station = new ObservationStation();
	        
	        station.setId(settings.getString(STATION_ID_KEY, ""));
	        station.setName(settings.getString(STATION_NAME_KEY, "Unknown Location"));
	        station.setUrl(settings.getString(STATION_URL_KEY, ""));
	        station.setLatitude(settings.getFloat(STATION_LATITUDE_KEY, 0.0F));
	        station.setLongitude(settings.getFloat(STATION_LONGITUDE_KEY, 0.0F));
	        
	        m_location = new ForecastLocation(locationName, latitude, longitude, station);
    	} else {
    		m_location = null;
    	}
    }
    
    private AlertDialog createConfigDialog() {
    	LayoutInflater inflater = LayoutInflater.from(this);
		View content = inflater.inflate(R.layout.configure_layout, null);
		
		Button find = (Button) content.findViewById(R.id.locationSearch);
		final EditText location = (EditText) content.findViewById(R.id.location);
		final ListView resultsList = (ListView) content.findViewById(R.id.locationResults);
		find.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				try {
					String locationText = location.getText().toString();
					if( !TextUtils.isEmpty(locationText))
					{
						List<ForecastLocation> locations = m_locationRetrieval.getLocations(locationText);
						
						resultsList.setAdapter(new ArrayAdapter<ForecastLocation>(WeatherNOOKActivity.this, android.R.layout.select_dialog_singlechoice, locations.toArray(new ForecastLocation[0])));
						resultsList.setItemChecked(0, true);
					}
				} catch (Exception e) {
					Log.d("WeatherNOOK", "Problem using geocoder: " + e.getMessage());
				}
				
			}
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose Location");
		builder.setView(content);
		builder.setCancelable(true);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				int selectedPos = resultsList.getCheckedItemPosition();
				if( selectedPos != ListView.INVALID_POSITION ) {
					m_location = (ForecastLocation) resultsList.getAdapter().getItem(selectedPos);
					Log.d("WeatherNOOK", "Selected location: " + m_location.toString());
					Log.d("WeatherNOOK", "Current Condition URL: " + m_location.getObservationStation().getUrl());
					
					saveLocation();
					
			        refresh();
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
    	
    	return builder.create();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    
    private void showConfigDialog() {
    	if( m_locationRetrieval.getObservationStationDB().isInitialized() ) {
			showDialog(CONFIGURE_DIALOG);
		} else {
			m_onStationDBInitComplete = new Runnable() {
				public void run() {
					showDialog(CONFIGURE_DIALOG);
				}
			};
			m_refreshMessage = "Initializing Weather Station Database...";
			showDialog(REFRESH_DIALOG);
		}
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem selected) {
		switch(selected.getItemId()) {
		case R.id.refresh:
			Log.d("WeatherNOOK", "refresh");
			refresh();
			return true;
		case R.id.configure:
			Log.d("WeatherNOOK", "configure");
			showConfigDialog();
			return true;
		default:
			return super.onOptionsItemSelected(selected);
		}
	}
    
    protected Dialog onCreateDialog(int id) {
        switch(id) {
        case REFRESH_DIALOG:
        	m_progressDialog = new ProgressDialog(WeatherNOOKActivity.this);
            m_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            return m_progressDialog;
        case CONFIGURE_DIALOG:
        	m_configDialog = createConfigDialog();
        	return m_configDialog;
        case ERROR_DIALOG:
        	AlertDialog.Builder builder = new AlertDialog.Builder(WeatherNOOKActivity.this);
			builder.setMessage("Error: " + m_lastError)
			       .setCancelable(false)
			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			           }
			       });
			AlertDialog alert = builder.create();
			return alert;
        default:
            return null;
        }
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	super.onPrepareDialog(id, dialog);
    	
    	switch(id) {
    	case REFRESH_DIALOG:
    		m_progressDialog.setMessage(m_refreshMessage);
    		break;
    	case ERROR_DIALOG:
    		AlertDialog errorDialog = (AlertDialog) dialog;
    		errorDialog.setMessage("Error: " + m_lastError);
    		break;
    	}
    }
    
    final Handler dbInitializeHandler = new Handler() {
    	public void handleMessage(Message m) {
    		switch(m.what) {
    		case ObservationStationDB.INITIALIZE_STARTING:
		        break;
    		case ObservationStationDB.INITIALIZE_SUCCESS:
    			dismissDialog(REFRESH_DIALOG);
    			if( m_onStationDBInitComplete != null ) {
    				m_onStationDBInitComplete.run();
    				m_onStationDBInitComplete = null;
    			}
    			break;
    		case ObservationStationDB.INITIALIZE_ERROR:
    			dismissDialog(REFRESH_DIALOG);
    			m_lastError = (String)m.obj;
    			showDialog(ERROR_DIALOG);
    			break;
    		}
    	}
    };
    
    final Handler refreshHandler = new Handler() {
    	public void handleMessage(Message m) {
    		dismissDialog(REFRESH_DIALOG);
    		switch(m.what) {
    		case FETCH_SUCCESS:
    			WeatherReport report = (WeatherReport)m.obj;
    			CurrentConditions conditions = report.getCurrentConditions();
    			
    			List< Map<String, Forecast> > forecastMap = new ArrayList< Map<String,Forecast> >();
    			
    			boolean isDaytime = ForecastUtils.isDaytimeNow();
				
				for( Forecast forecast : report.getForecast() ) {
            		Map<String, Forecast> columnMap = new HashMap<String, Forecast>();
            		columnMap.put("Forecast", forecast);
            		forecastMap.add(columnMap);
            	}
				
				SimpleAdapter adapter = new SimpleAdapter(WeatherNOOKActivity.this, forecastMap, R.layout.forecast_entry, new String[] {"Forecast"}, new int[] {R.id.forecastLayout});
		        adapter.setViewBinder(new ForecastBinder());
		        
		        ListView list = (ListView) findViewById(R.id.forecastList);
		        list.setAdapter(adapter);
		        
		        ImageView conditionImage = (ImageView) findViewById(R.id.currentImage);
		        TextView conditionLabel = (TextView) findViewById(R.id.currentCondition);
		        TextView tempLabel = (TextView) findViewById(R.id.currentTemp);
		        TextView windLabel = (TextView) findViewById(R.id.currentWind);
		        TextView humidityLabel = (TextView) findViewById(R.id.currentHumidity);
		        
		        conditionImage.setImageResource(ForecastUtils.getIconForForecast(conditions.getConditions(), isDaytime));
		        conditionLabel.setText(conditions.getConditions());
		        tempLabel.setText(String.format(m_tempFormat, Math.round(conditions.getTemperature())));
		        windLabel.setText(String.format(m_windFormat, Math.round(conditions.getWindSpeed()), conditions.getWindDirAbbreviation()));
		        
		        if( conditions.getHumidity() >= 0 ) {
		        	humidityLabel.setVisibility(View.VISIBLE);
		        	humidityLabel.setText(String.format(m_humidityFormat, conditions.getHumidity()));
		        } else {
		        	humidityLabel.setVisibility(View.INVISIBLE);
		        }
		        
		        View current = findViewById(R.id.currentConditionLayout);
		        current.setVisibility(View.VISIBLE);
		        
		        break;
    		case FETCH_ERROR:
    			m_lastError = (String)m.obj;
    			showDialog(ERROR_DIALOG);
    			break;
    		}
    	}
    };
    
    private class RefreshThread extends Thread {
    	Handler m_handler = null;
    	
    	RefreshThread(Handler handler) {
    		m_handler = handler;
    	}
    	
    	public void run() {
    		Message done = m_handler.obtainMessage();
    		
            try
            {
            	WeatherReport report = WeatherReport.getWeather(m_location);
            	
            	done.what = FETCH_SUCCESS;
            	done.obj = report;
            }
            catch(Exception e)
            {
            	Log.d("WeatherNOOK", e.getClass().toString());
            	for( StackTraceElement element : e.getStackTrace()) {
            		Log.d("WeatherNOOK", element.toString());
            	}
            	done.what = FETCH_ERROR;
            	done.obj = e.getMessage();
            }
            
            m_handler.sendMessage(done);
    	}
    }
}