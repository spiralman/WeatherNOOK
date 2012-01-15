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

/**
 * Data for a specific forecast at a point in time.
 */
public class Forecast {
	private boolean alert = false;
	private long validStart = Long.MIN_VALUE;
	private int tempHigh = Integer.MIN_VALUE;
	private int tempLow = Integer.MIN_VALUE;

	private int morningPrecip = Integer.MIN_VALUE;
	private int eveningPrecip = Integer.MIN_VALUE;

	private String conditions = "Unknown";
	private String url;

	/**
	 * Exception to inform callers that we ran into problems while parsing the
	 * forecast returned by the webservice.
	 */
	public static final class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException(String detailMessage) {
			super(detailMessage);
		}

		public ParseException(String detailMessage, Throwable throwable) {
			super(detailMessage + ": " + throwable.getMessage(), throwable);
		}
	}

	public long getValidStart() {
		return validStart;
	}

	public void setValidStart(long validStart) {
		this.validStart = validStart;
	}

	public int getTempHigh() {
		return tempHigh;
	}

	public void setTempHigh(int tempHigh) {
		this.tempHigh = tempHigh;
	}

	public int getTempLow() {
		return tempLow;
	}

	public void setTempLow(int tempLow) {
		this.tempLow = tempLow;
	}

	public int getMorningPrecip() {
		return morningPrecip;
	}

	public void setMorningPrecip(int morningPrecip) {
		this.morningPrecip = morningPrecip;
	}

	public int getEveningPrecip() {
		return eveningPrecip;
	}

	public void setEveningPrecip(int eveningPrecip) {
		this.eveningPrecip = eveningPrecip;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions) {
		this.conditions = conditions;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean hasAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

}
