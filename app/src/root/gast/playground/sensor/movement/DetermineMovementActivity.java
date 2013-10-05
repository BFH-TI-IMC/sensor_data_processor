/*
 * Copyright 2013 HTA
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package root.gast.playground.sensor.movement;

import java.io.File;

import root.gast.playground.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;

/**
 * Determines when the device has been moved and notifies the user.
 */
public class DetermineMovementActivity extends Activity {
	private static final String TAG = "DetermineMovementActivity";
	private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
	private static final String USE_HIGH_PASS_FILTER_PREFERENCE_KEY = "USE_HIGH_PASS_FILTER_PREFERENCE_KEY";
	private static final String SELECTED_SENSOR_TYPE_PREFERENCE_KEY = "SELECTED_SENSOR_TYPE_PREFERENCE_KEY";

	private SensorManager sensorManager;
	private RadioGroup sensorSelector;
	private int selectedSensorType;
	private boolean readingAccelerationData;
	private SharedPreferences preferences;
	private AccelerationEventListener accelerometerListener;
	private AccelerationEventListener linearAccelerationListener;
	private boolean useHighPassFilter;
	private XYPlot xyPlot;
	private CheckBox highPassFilterCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.setContentView(R.layout.determine_movement);

		sensorSelector = (RadioGroup) findViewById(R.id.sensorSelector);
		highPassFilterCheckBox = (CheckBox) findViewById(R.id.highPassFilterCheckBox);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		readingAccelerationData = false;

		preferences = getPreferences(MODE_PRIVATE);

		useHighPassFilter = getResources().getBoolean(
				R.bool.useHighPassFilterDefaultValue);
		useHighPassFilter = preferences.getBoolean(
				USE_HIGH_PASS_FILTER_PREFERENCE_KEY, useHighPassFilter);
		((CheckBox) findViewById(R.id.highPassFilterCheckBox))
				.setChecked(useHighPassFilter);

		selectedSensorType = preferences.getInt(
				SELECTED_SENSOR_TYPE_PREFERENCE_KEY, Sensor.TYPE_ACCELEROMETER);

		if (selectedSensorType == Sensor.TYPE_ACCELEROMETER) {
			((RadioButton) findViewById(R.id.accelerometer)).setChecked(true);
		} else {
			((RadioButton) findViewById(R.id.linearAcceleration))
					.setChecked(true);
		}

		xyPlot = (XYPlot) findViewById(R.id.XYPlot);
		xyPlot.setDomainLabel("Elapsed Time (ms)");
		xyPlot.setRangeLabel("Acceleration (m/sec^2)");
		xyPlot.setBorderPaint(null);
		xyPlot.disableAllMarkup();
		xyPlot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
	}

	@Override
	protected void onPause() {
		onPause();
		stopReadingAccelerationData();
	}

	public void onSensorSelectorClick(View view) {
		int selectedSensorId = sensorSelector.getCheckedRadioButtonId();
		if (selectedSensorId == R.id.accelerometer) {
			selectedSensorType = Sensor.TYPE_ACCELEROMETER;
		} else if (selectedSensorId == R.id.linearAcceleration) {
			selectedSensorType = Sensor.TYPE_LINEAR_ACCELERATION;
		}

		preferences
				.edit()
				.putInt(SELECTED_SENSOR_TYPE_PREFERENCE_KEY, selectedSensorType)
				.commit();
	}

	public void onReadAccelerationDataToggleButtonClicked(View view) {
		ToggleButton toggleButton = (ToggleButton) view;

		if (toggleButton.isChecked()) {
			startReadingAccelerationData();
		} else {
			stopReadingAccelerationData();
		}
	}

	private void startReadingAccelerationData() {
		if (!readingAccelerationData) {
			// Clear any plot that may already exist on the chart
			xyPlot.clear();
			xyPlot.redraw();

			// Disable UI components so they cannot be changed while plotting
			// sensor data
			for (int i = 0; i < sensorSelector.getChildCount(); i++) {
				sensorSelector.getChildAt(i).setEnabled(false);
			}
			highPassFilterCheckBox.setEnabled(false);

			// Data files are stored on the external cache directory so they can
			// be pulled off of the device by the user
			File accelerometerDataFile = new File(getExternalCacheDir(),
					"accelerometer.csv");
			File linearAcceclerationDataFile = new File(getExternalCacheDir(),
					"linearAcceleration.csv");

			if (selectedSensorType == Sensor.TYPE_ACCELEROMETER) {
				xyPlot.setTitle("Sensor.TYPE_ACCELEROMETER");
				accelerometerListener = new AccelerationEventListener(xyPlot,
						useHighPassFilter, accelerometerDataFile);

				linearAccelerationListener = new AccelerationEventListener(
						null, useHighPassFilter, linearAcceclerationDataFile);
			} else {
				xyPlot.setTitle("Sensor.TYPE_LINEAR_ACCELERATION");
				accelerometerListener = new AccelerationEventListener(null,
						useHighPassFilter, accelerometerDataFile);

				linearAccelerationListener = new AccelerationEventListener(
						xyPlot, useHighPassFilter, linearAcceclerationDataFile);
			}

			sensorManager.registerListener(accelerometerListener,
					sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					RATE);

			sensorManager.registerListener(linearAccelerationListener,
					sensorManager
							.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
					RATE);

			readingAccelerationData = true;

			Log.d(TAG, "Started reading acceleration data");
		}
	}

	private void stopReadingAccelerationData() {
		if (readingAccelerationData) {
			// Re-enable sensor and options UI views
			for (int i = 0; i < sensorSelector.getChildCount(); i++) {
				sensorSelector.getChildAt(i).setEnabled(true);
			}
			highPassFilterCheckBox.setEnabled(true);

			sensorManager.unregisterListener(accelerometerListener);
			sensorManager.unregisterListener(linearAccelerationListener);

			// Tell listeners to clean up after themselves
			accelerometerListener.stop();
			linearAccelerationListener.stop();

			readingAccelerationData = false;

			Log.d(TAG, "Stopped reading acceleration data");
		}
	}

}
