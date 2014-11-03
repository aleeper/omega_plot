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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.androidplot.util.PlotStatistics;
import com.androidplot.xy.*;
import com.androidplot.xy.LineAndPointFormatter;


import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

// Monitor the phone's orientation sensor and plot the resulting azimuth pitch and roll values.
// See: http://developer.android.com/reference/android/hardware/SensorEvent.html
public class GyroPlotActivity extends Activity implements SensorEventListener
{
    private static String TAG = GyroPlotActivity.class.getSimpleName();

    private boolean mIsRecording = true;

    /**
     * A simple formatter to convert bar indexes into sensor names.
     */
    private class APRIndexFormat extends Format {
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            Number num = (Number) obj;

            // using num.intValue() will floor the value, so we add 0.5 to round instead:
            int roundNum = (int) (num.floatValue() + 0.5f);
            switch(roundNum) {
                case 0:
                    toAppendTo.append("Azimuth");
                    break;
                case 1:
                    toAppendTo.append("Pitch");
                    break;
                case 2:
                    toAppendTo.append("Roll");
                    break;
                default:
                    toAppendTo.append("Unknown");
            }
            return toAppendTo;
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null;  // We don't use this so just return null for now.
        }
    }

    private static final int HISTORY_SIZE = 300;            // number of points to plot in history
    private SensorManager sensorMgr = null;
    private Sensor orSensor = null;

//    private XYPlot aprLevelsPlot = null;
    private XYPlot aprHistoryPlot = null;

    private CheckBox hwAcceleratedCb;
    private CheckBox showFpsCb;
    private SimpleXYSeries aprLevelsSeries = null;
    private SimpleXYSeries azimuthHistorySeries = null;
    private SimpleXYSeries pitchHistorySeries = null;
    private SimpleXYSeries rollHistorySeries = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyro_plot_layout);

//        // setup the APR Levels plot:
//        aprLevelsPlot = (XYPlot) findViewById(R.id.aprLevelsPlot);
//
//        aprLevelsSeries = new SimpleXYSeries("APR Levels");
//        aprLevelsSeries.useImplicitXVals();
//        aprLevelsPlot.addSeries(aprLevelsSeries,
//                new BarFormatter(Color.argb(100, 0, 200, 0), Color.rgb(0, 80, 0)));
//        aprLevelsPlot.setDomainStepValue(3);
//        aprLevelsPlot.setTicksPerRangeLabel(3);
//
//        // per the android documentation, the minimum and maximum readings we can get from
//        // any of the orientation sensors is -180 and 359 respectively so we will fix our plot's
//        // boundaries to those values.  If we did not do this, the plot would auto-range which
//        // can be visually confusing in the case of dynamic plots.
//        aprLevelsPlot.setRangeBoundaries(-180, 359, BoundaryMode.FIXED);
//
//        // use our custom domain value formatter:
//        aprLevelsPlot.setDomainValueFormat(new APRIndexFormat());
//
//        // update our domain and range axis labels:
//        aprLevelsPlot.setDomainLabel("Axis");
//        aprLevelsPlot.getDomainLabelWidget().pack();
//        aprLevelsPlot.setRangeLabel("Angle (Degs)");
//        aprLevelsPlot.getRangeLabelWidget().pack();
//        aprLevelsPlot.setGridPadding(15, 0, 15, 0);

        // setup the APR History plot:
        aprHistoryPlot = (XYPlot) findViewById(R.id.aprHistoryPlot);

        azimuthHistorySeries = new SimpleXYSeries("w_x");
        azimuthHistorySeries.useImplicitXVals();
        pitchHistorySeries = new SimpleXYSeries("w_y");
        pitchHistorySeries.useImplicitXVals();
        rollHistorySeries = new SimpleXYSeries("w_z");
        rollHistorySeries.useImplicitXVals();

        aprHistoryPlot.setRangeBoundaries(-30, 30, BoundaryMode.FIXED);
        aprHistoryPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        aprHistoryPlot.addSeries(azimuthHistorySeries, new LineAndPointFormatter(Color.rgb(100, 100, 200), null, null, null));
        aprHistoryPlot.addSeries(pitchHistorySeries, new LineAndPointFormatter(Color.rgb(100, 200, 100), null, null, null));
        aprHistoryPlot.addSeries(rollHistorySeries, new LineAndPointFormatter(Color.rgb(200, 100, 100), null, null, null));
        aprHistoryPlot.setDomainStepValue(5);
        aprHistoryPlot.setTicksPerRangeLabel(3);
        aprHistoryPlot.setDomainLabel("Sample Index");
        aprHistoryPlot.getDomainLabelWidget().pack();
        aprHistoryPlot.setRangeLabel("Angular Velocity (rad/sec)");
        aprHistoryPlot.getRangeLabelWidget().pack();

//        // setup checkboxes:
//        hwAcceleratedCb = (CheckBox) findViewById(R.id.hwAccelerationCb);
//        final PlotStatistics levelStats = new PlotStatistics(1000, false);
//        final PlotStatistics histStats = new PlotStatistics(1000, false);

//        aprLevelsPlot.addListener(levelStats);
//        aprHistoryPlot.addListener(histStats);
//        hwAcceleratedCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if(b) {
//                    aprLevelsPlot.setLayerType(View.LAYER_TYPE_NONE, null);
//                    aprHistoryPlot.setLayerType(View.LAYER_TYPE_NONE, null);
//                } else {
//                    aprLevelsPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//                    aprHistoryPlot.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//                }
//            }
//        });

//        showFpsCb = (CheckBox) findViewById(R.id.showFpsCb);
//        showFpsCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                levelStats.setAnnotatePlotEnabled(b);
//                histStats.setAnnotatePlotEnabled(b);
//            }
//        });

//        // get a ref to the BarRenderer so we can make some changes to it:
//        BarRenderer barRenderer = (BarRenderer) aprLevelsPlot.getRenderer(BarRenderer.class);
//        if(barRenderer != null) {
//            // make our bars a little thicker than the default so they can be seen better:
//            barRenderer.setBarWidth(25);
//        }

        // register for orientation sensor events:
        sensorMgr = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        for (Sensor sensor : sensorMgr.getSensorList(Sensor.TYPE_GYROSCOPE)) {
            if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                orSensor = sensor;
                Log.e(TAG, "Sensor range is: " + orSensor.getMaximumRange());
            }
        }

        // if we can't access the orientation sensor then exit:
        if (orSensor == null) {
            System.out.println("Failed to attach to orSensor.");
            cleanup();
        }

        sensorMgr.registerListener(this, orSensor, SensorManager.SENSOR_DELAY_FASTEST);

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
        // Handle presses on the action bar items
        int id = item.getItemId();
        switch (id) {
//            case R.id.action_search:
//                Log.e(TAG, "Clicked on search!");
//                return true;
//            case R.id.action_settings:
//                Log.e(TAG, "Clicked on settings!");
//                return true;
            case R.id.action_record:
                Log.e(TAG, "Clicked on record!");
                onStartRecord();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onStartRecord() {
        mIsRecording = !mIsRecording;
    }


    private void cleanup() {
        // aunregister with the orientation sensor before exiting:
        sensorMgr.unregisterListener(this);
        finish();
    }

    // Called whenever a new orSensor reading is taken.
    @Override
    public synchronized void onSensorChanged(SensorEvent sensorEvent) {
        if (mIsRecording) {

            // update instantaneous data:
//        Number[] series1Numbers = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};
//        aprLevelsSeries.setModel(Arrays.asList(series1Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);

            // get rid the oldest sample in history:
            if (rollHistorySeries.size() > HISTORY_SIZE) {
                rollHistorySeries.removeFirst();
                pitchHistorySeries.removeFirst();
                azimuthHistorySeries.removeFirst();
            }

            // add the latest history sample:
            azimuthHistorySeries.addLast(null, sensorEvent.values[0]);
            pitchHistorySeries.addLast(null, sensorEvent.values[1]);
            rollHistorySeries.addLast(null, sensorEvent.values[2]);

            // redraw the Plots:
//        aprLevelsPlot.redraw();
            aprHistoryPlot.redraw();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Not interested in this event
    }
}