package org.spiralman.WeatherNOOK;

import org.jsharkey.sky.ForecastUtils;
import org.jsharkey.sky.webservice.Forecast;

import android.content.Context;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class ForecastBinder implements ViewBinder {
	private static String tempHighFormat;
	private static String tempLowFormat;

	private static String dayFormat;

	private static String morningPrecipFormat;
	private static String eveningPrecipFormat;

	public static void prepareFormatStrings(Context context) {
		tempHighFormat = context.getString(R.string.high);
		tempLowFormat = context.getString(R.string.low);
		dayFormat = context.getString(R.string.dayFormat);
		morningPrecipFormat = context.getString(R.string.morningPrecipFormat);
		eveningPrecipFormat = context.getString(R.string.eveningPrecipFormat);
	}

	public boolean setViewValue(View view, Object data,
			String textRepresentation) {
		Forecast forecast = (Forecast) data;

		Time time = new Time();
		time.set(forecast.getValidStart());

		RelativeLayout layout = (RelativeLayout) view
				.findViewById(R.id.forecastLayout);

		TextView day = (TextView) view.findViewById(R.id.day);
		TextView condition = (TextView) view.findViewById(R.id.conditionString);
		ImageView forecastImage = (ImageView) view
				.findViewById(R.id.forecastImage);
		TextView high = (TextView) view.findViewById(R.id.high);
		TextView low = (TextView) view.findViewById(R.id.low);
		TextView morning = (TextView) view.findViewById(R.id.morningPrecip);
		TextView evening = (TextView) view.findViewById(R.id.eveningPrecip);

		day.setText(time.format(dayFormat));
		forecastImage.setImageResource(ForecastUtils.getIconForForecast(
				forecast.getConditions(), true));
		condition.setText(forecast.getConditions());
		high.setText(String.format(tempHighFormat, forecast.getTempHigh()));
		low.setText(String.format(tempLowFormat, forecast.getTempLow()));
		morning.setText(String.format(morningPrecipFormat,
				forecast.getMorningPrecip()));
		evening.setText(String.format(eveningPrecipFormat,
				forecast.getEveningPrecip()));

		layout.requestLayout();

		return true;
	}

}
