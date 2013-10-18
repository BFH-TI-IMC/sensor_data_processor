/*
 * Copyright 2013 Bern University of Applied Sciences BFH
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

package ch.bfh.sensordataprocessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;
import ch.bfh.sensordataprocessor.math.HighPassFilter;
import ch.bfh.sensordataprocessor.math.IFilter;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

/**
 * Receives accelerometer events and writes them to CSV files and plots them on a graph.
 */
public class AccelerationEventListener implements SensorEventListener
{
    private static final String TAG = "AccelerationEventListener";
    private static final char CSV_DELIM = ',';
    private static final int THRESHHOLD = 2;
    private static final String CSV_HEADER = "X Axis,Y Axis,Z Axis,Acceleration,Time";
    private static final int MAX_SERIES_SIZE = 30;
    private static final int CHART_REFRESH = 125;

    private final IFilter xAxisHighPassFilter;
    private final IFilter yAxisHighPassFilter;
    private final IFilter zAxisHighPassFilter;

    private PrintWriter printWriter;
    private final long startTime;

    private final SimpleXYSeries xAxisSeries;
    private final SimpleXYSeries yAxisSeries;
    private final SimpleXYSeries zAxisSeries;
    private final SimpleXYSeries accelerationSeries;
    private final XYPlot xyPlot;
    private long lastChartRefresh;
    private final boolean useHighPassFilter;

	public AccelerationEventListener(XYPlot xyPlot, boolean useHighPassFilter,
 File dataFile) {
        this.xyPlot = xyPlot;
        this.useHighPassFilter = useHighPassFilter;

        xAxisHighPassFilter = new HighPassFilter();
        yAxisHighPassFilter = new HighPassFilter();
        zAxisHighPassFilter = new HighPassFilter();

        // -------------------------------------------------------------
        // for testing purposes only
        // xAxisHighPassFilter = new LowPassFilter();
        // yAxisHighPassFilter = new LowPassFilter();
        // zAxisHighPassFilter = new LowPassFilter();

        // xAxisHighPassFilter = new MovingAverageFilter(16);
        // yAxisHighPassFilter = new MovingAverageFilter(16);
        // zAxisHighPassFilter = new MovingAverageFilter(16);
        // -------------------------------------------------------------

        xAxisSeries = new SimpleXYSeries("X Axis");
        yAxisSeries = new SimpleXYSeries("Y Axis");
        zAxisSeries = new SimpleXYSeries("Z Axis");
        accelerationSeries = new SimpleXYSeries("Acceleration");

        startTime = SystemClock.uptimeMillis();

        try
        {
            printWriter = new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
            printWriter.println(CSV_HEADER);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not open CSV file(s)", e);
        }

        if (xyPlot != null)
        {
            xyPlot.addSeries(xAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.RED, null, null));
            xyPlot.addSeries(yAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.GREEN, null, null));
            xyPlot.addSeries(zAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.BLUE, null, null));
            xyPlot.addSeries(accelerationSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.CYAN, null, null));
        }
    }

    private void writeSensorEvent(PrintWriter printWriter,
                                  float x,
                                  float y,
                                  float z,
                                  double acceleration,
            long eventTime) {
        if (printWriter != null)
        {
            StringBuffer sb = new StringBuffer()
                .append(x).append(CSV_DELIM)
                .append(y).append(CSV_DELIM)
                .append(z).append(CSV_DELIM)
                .append(acceleration).append(CSV_DELIM)
                .append((eventTime / 1000000) - startTime);
            
            printWriter.println(sb.toString());
            if (printWriter.checkError())
            {
                Log.w(TAG, "Error writing sensor event data");
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values.clone();
        
        // Pass values through high-pass filter if enabled
        if (useHighPassFilter)
        {
            values = applyFilter(values[0],
                              values[1],
                              values[2]);
        }

            double sumOfSquares = (values[0] * values[0])
                    + (values[1] * values[1])
                    + (values[2] * values[2]);
            double acceleration = Math.sqrt(sumOfSquares);

            // Write to data file
            writeSensorEvent(printWriter,
                             values[0],
                             values[1],
                             values[2],
                             acceleration,
                             event.timestamp);

            // If the plot is null, the sensor is not active. Do not plot the
            // data or used the data to determine if the device is moving
            if (xyPlot != null)
 {
            long current = SystemClock.uptimeMillis();

            // Limit how much the chart gets updated
            if ((current - lastChartRefresh) >= CHART_REFRESH) {
                long timestamp = (event.timestamp / 1000000) - startTime;

                // Plot data
                addDataPoint(xAxisSeries, timestamp, values[0]);
                addDataPoint(yAxisSeries, timestamp, values[1]);
                addDataPoint(zAxisSeries, timestamp, values[2]);
                addDataPoint(accelerationSeries, timestamp, acceleration);

                xyPlot.redraw();

                lastChartRefresh = current;
            }

            // A "movement" is only triggered of the total acceleration is
            // above a threshold
            if (acceleration > THRESHHOLD) {
                Log.i(TAG, "Movement detected");
            }
        }
    }

    private void addDataPoint(SimpleXYSeries series, Number timestamp, Number value) {
        if (series.size() == MAX_SERIES_SIZE)
        {
            series.removeFirst();
        }
        
        series.addLast(timestamp, value);
    }

    private float[] applyFilter(float x, float y, float z) {
        float[] filteredValues = new float[3];
		
        // TODO For Students: Apply your math stuff here.
        filteredValues[0] = xAxisHighPassFilter.processValue(x);
        filteredValues[1] = yAxisHighPassFilter.processValue(y);
        filteredValues[2] = zAxisHighPassFilter.processValue(z);

        return filteredValues;
	}

    public void stop() {
        if (printWriter != null)
        {
            printWriter.close();
        }

		// if (printWriter.checkError()) // FIXME Put this in again.
		// {
		// Log.e(TAG, "Error closing writer");
		// }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // no-op
    }
}
