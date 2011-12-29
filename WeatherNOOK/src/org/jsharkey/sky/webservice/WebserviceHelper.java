/*
 * Copyright (C) 2009 Jeff Sharkey, http://jsharkey.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsharkey.sky.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsharkey.sky.webservice.Forecast.ParseException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Helper class to handle querying a webservice for forecast details and parsing
 * results into {@link ForecastProvider}.
 */
public class WebserviceHelper {
    private static final String TAG = "ForcastHelper";
    
    public static final String COUNTRY_US = "US";

    /**
     * Timeout to wait for webservice to respond. Because we're in the
     * background, we don't mind waiting for good data.
     */
    static final long WEBSERVICE_TIMEOUT = 30 * DateUtils.SECOND_IN_MILLIS;

    /**
     * User-agent string to use when making requests. Should be filled using
     * {@link #prepareUserAgent(Context)} before making any other calls.
     */
    private static String sUserAgent = null;
    
    private static HttpClient sClient = new DefaultHttpClient();

    /**
     * Prepare the internal User-Agent string for use. This requires a
     * {@link Context} to pull the package name and version number for this
     * application.
     */
    public static void prepareUserAgent(Context context) {
        try {
            // Read package name and version number from manifest
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sUserAgent = String.format("%s/%s (Linux; Android)",
                    info.packageName, info.versionName);
            
        } catch(NameNotFoundException e) {
            Log.e(TAG, "Couldn't find package information in PackageManager", e);
        }
    }
    
    /**
     * Open a request to the given URL, returning a {@link Reader} across the
     * response from that API.
     */
    public static Reader queryApi(String url) throws ParseException {
        if (sUserAgent == null) {
            throw new ParseException("Must prepare user agent string");
        }
        
        Reader reader = null;
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", sUserAgent);

        try {
            HttpResponse response = sClient.execute(request);

            StatusLine status = response.getStatusLine();
            Log.d(TAG, "Request returned status " + status);

            HttpEntity entity = response.getEntity();
            reader = new InputStreamReader(entity.getContent());

        } catch (IOException e) {
            throw new ParseException("Problem calling forecast API", e);
        }
        
        return reader;
    }
    
    public static String readApi(String url) throws ParseException, IOException {
    	String result = "";
    	Reader reader = queryApi(url);
    	
    	BufferedReader lineReader = new BufferedReader(reader);
    	
    	String line = lineReader.readLine();
    	while( line != null ) {
    		result = result + line;
    		
    		line = lineReader.readLine();
    	}
    	
    	return result;
    }

}
