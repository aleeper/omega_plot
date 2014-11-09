/*
 * Copyright 2012 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.adamleeper.omegaplot;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.WindowManager;
import android.widget.CheckBox;

import com.androidplot.util.PlotStatistics;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.*;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYStepMode;
import com.androidplot.xy.XYPlot;

// Monitor the device's gyroscope and plot the w_x, w_y, w_z values.
public class GyroPlotActivity extends Activity implements SensorEventListener
{
    private static String TAG = GyroPlotActivity.class.getSimpleName();

    private boolean mIsRecording = true;

    private static final int HISTORY_SIZE = 300;  // Number of points to plot in history.
    private SensorManager sensorMgr = null;
    private Sensor gyroSensor = null;

    private XYPlot mainPlot = null;

    private CheckBox hwAcceleratedCb;
    private CheckBox showFpsCb;
    private SimpleXYSeries omegaXSeries = null;
    private SimpleXYSeries omegaYSeries = null;
    private SimpleXYSeries omegaZSeries = null;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.gyro_plot_layout);

        // Setup the angular velocity plot.
        mainPlot = (XYPlot) findViewById(R.id.mainPlot);

        omegaXSeries = new SimpleXYSeries("w_x");
        omegaXSeries.useImplicitXVals();
        omegaYSeries = new SimpleXYSeries("w_y");
        omegaYSeries.useImplicitXVals();
        omegaZSeries = new SimpleXYSeries("w_z");
        omegaZSeries.useImplicitXVals();

        mainPlot.setRangeBoundaries(-40, 40, BoundaryMode.FIXED);
        mainPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        mainPlot.addSeries(omegaXSeries, new LineAndPointFormatter(Color.rgb(255, 100, 100), null, null, null));
        mainPlot.addSeries(omegaYSeries, new LineAndPointFormatter(Color.rgb(100, 255, 100), null, null, null));
        mainPlot.addSeries(omegaZSeries, new LineAndPointFormatter(Color.rgb(100, 100, 255), null, null, null));

        // Style the grid.
        mainPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 50);
        mainPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 10);
        mainPlot.getGraphWidget().setMarginTop(10);
        mainPlot.getGraphWidget().setMarginBottom(10);
        mainPlot.getGraphWidget().setMarginRight(15);
        //mainPlot.getGraphWidget().setGridPaddingTop(5);
        //mainPlot.getGraphWidget().setGridPaddingRight(5);
        mainPlot.getLegendWidget().setHeight(18);

        // Style the legend.
        mainPlot.getLegendWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_RIGHT,
                                            0, YLayoutStyle.ABSOLUTE_FROM_TOP,
                                            AnchorPosition.RIGHT_TOP);
        mainPlot.getLegendWidget().setWidth(125, SizeLayoutType.ABSOLUTE);

        // Style the title.
        mainPlot.setTitle("Angular Velocity in Body-Fixed Axes");
        mainPlot.setPlotPaddingTop(10);
        // Style the border.
        //mainPlot.setBorderStyle(XYPlot.BorderStyle.ROUNDED, 10.0f, 10.0f);
        //mainPlot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
        //mainPlot.getBackgroundPaint().setColor(Color.WHITE);
        //mainPlot.setBackgroundColor(Color.WHITE);

        // Set horizontal label.
        mainPlot.setDomainLabel("Gyro Sample Index");
        mainPlot.getDomainLabelWidget().position(0, XLayoutStyle.ABSOLUTE_FROM_CENTER,
                                                 0, YLayoutStyle.ABSOLUTE_FROM_BOTTOM,
                                                 AnchorPosition.BOTTOM_MIDDLE);
        mainPlot.getDomainLabelWidget().pack();

        // Set vertical label.
        mainPlot.setRangeLabel("Angular Velocity (rad/sec)");
        mainPlot.getRangeLabelWidget().pack();


        //Paint plotBackground = new Paint();
        //plotBackground.setARGB(255, 255, 255, 255);
        //mainPlot.getGraphWidget().setBackgroundPaint(plotBackground);
        //mainPlot.getGraphWidget().setSize(
        // new SizeMetrics(100, SizeLayoutType.FILL, 100, SizeLayoutType.FILL));
        //mainPlot.disableAllMarkup();

        // Register for orientation sensor events.
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE)) {
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyroSensor = sensor;
                Log.i(TAG, "Sensor range is: " + gyroSensor.getMaximumRange());
            }
        }

        if (gyroSensor == null) {
            // Bail if we can't access the gyroscope sensor.
            Log.e(TAG, "Failed to attach to gyroSensor. Aborting...");
            cleanup();
        }

        sensorMgr.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        mIsRecording = false;
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_record:
                Log.i(TAG, "Clicked on record!");
                toggleRecord();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleRecord() {
        mIsRecording = !mIsRecording;
    }


    private void cleanup() {
        // aunregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

    // Called whenever a new gyroSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        if (mIsRecording) {
            // Get rid the oldest sample in history.
            if (omegaZSeries.size() > HISTORY_SIZE) {
                omegaXSeries.removeFirst();
                omegaYSeries.removeFirst();
                omegaZSeries.removeFirst();
            }

            // Add the latest history sample.
            omegaXSeries.addLast(null, -sensorEvent.values[1]);
            omegaYSeries.addLast(null, sensorEvent.values[0]);
            omegaZSeries.addLast(null, sensorEvent.values[2]);

            // Redraw the plot.
            mainPlot.redraw();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
}