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

import java.lang.Override;

public class MainActivity extends Activity implements SensorEventListener {
    private static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "com.adamleeper.omegaplot.MESSAGE";

    private SensorManager mSensorManager;
    private Sensor mSensorGyro;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private boolean mIsRecording = false;
    private float mLastTimestamp = 0.f;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean hasActionBar = requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        // mSensorGyro.registerListener()
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
        Button buttonSend = (Button) findViewById(R.id.button_send);
        // buttonSend.setOnClickListener(this);
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
            case R.id.action_search:
                Log.e(TAG, "Clicked on search!");
                return true;
            case R.id.action_settings:
                Log.e(TAG, "Clicked on settings!");
                return true;
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

    public void sendMessage(View view) {
        Log.e(TAG, "Got a click on view id: " + view.getId());
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
