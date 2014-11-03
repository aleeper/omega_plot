package com.adamleeper.omegaplot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.util.Log;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

// Plotting stuff
//import android.app.Activity;
//import android.os.Bundle;
import android.view.WindowManager;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;
import java.util.Arrays;

import java.lang.Override;

public class MainActivity extends Activity implements SensorEventListener {
    private static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "com.adamleeper.omegaplot.MESSAGE";

    private SensorManager mSensorManager;
    private Sensor mSensorGyro;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private boolean mIsRecording = false;
    private float mLastTimestamp = 0.f;

    // Plotting stuff
    private XYPlot plot;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasActionBar = requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensorGyro, SensorManager.SENSOR_DELAY_GAME);

        // creating Actionbar
        if (hasActionBar) {
            //ActionBar actionBar = getActionBar();
            //actionBar.setHomeButtonEnabled(true);
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            //actionBar.setBackgroundDrawable(new ColorDrawable(Color.BLUE));
            //actionBar.setDisplayShowTitleEnabled(true);
            //actionBar.show();
        } else {
            Log.e(TAG, "We don't have an action bar...");
        }

        // Set up button (callback is currently specified in XML.
        // Button buttonSend = (Button) findViewById(R.id.button_send);
        // buttonSend.setOnClickListener(this);

        // Plotting stuff.
        // fun little snippet that prevents users from taking screenshots
        // on ICS+ devices :-)
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
//                WindowManager.LayoutParams.FLAG_SECURE);

//        setContentView(R.layout.simple_xy_plot_example);

        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        if (plot == null) {
            Log.e(TAG, "plot is null!!!");
        }

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series

        // same as above
        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        // same as above:
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        plot.addSeries(series2, series2Format);

        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.getGraphWidget().setDomainLabelOrientation(-45);
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


    @Override
    public void onSensorChanged(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.

        if (mIsRecording && mLastTimestamp != 0.f) {
            final float dT = (event.timestamp - mLastTimestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float w_x = event.values[0];
            float w_y = event.values[1];
            float w_z = event.values[2];
            Log.e(TAG, String.format("Gyro (%f): [%5.3f, %5.3f, %5.3f]", dT, w_x, w_y, w_z));
        }
        mLastTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

//    public void sendMessage(View view) {
//        Log.e(TAG, "Got a click on view id: " + view.getId());
//        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.edit_message);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
//        startActivity(intent);
//    }
}
