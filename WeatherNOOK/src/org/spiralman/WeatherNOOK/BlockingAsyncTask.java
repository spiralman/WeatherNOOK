package org.spiralman.WeatherNOOK;

import android.os.AsyncTask;
import android.util.Log;

public abstract class BlockingAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {
	protected WeatherNOOKActivity m_currentActivity = null;

	private Result m_result = null;
	private String m_refreshMessage = null;

	public BlockingAsyncTask(WeatherNOOKActivity activity) {
		m_currentActivity = activity;
	}

	public String getRefreshMessage() {
		return m_refreshMessage;
	}

	public void setRefreshMessage(String refreshMessage) {
		m_refreshMessage = refreshMessage;
	}

	protected abstract void notifyComplete(Result result);

	@Override
	protected void onPreExecute() {
		Log.d("WeatherNOOK", "Starting initialize Station DB");

		m_currentActivity.setRefreshMessage(m_refreshMessage);
		m_currentActivity.showRefreshDialog();
	}

	@Override
	protected void onPostExecute(Result result) {
		m_result = result;

		if (m_currentActivity != null) {
			notifyComplete(m_result);
		}
	}

	public void setActivity(WeatherNOOKActivity activity) {
		m_currentActivity = activity;

		if (m_currentActivity != null) {
			m_currentActivity.setRefreshMessage(m_refreshMessage);
		}

		if (m_result != null) {
			notifyComplete(m_result);
		}
	}
}
