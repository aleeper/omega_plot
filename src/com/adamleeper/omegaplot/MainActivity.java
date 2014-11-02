package com.adamleeper.omegaplot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.util.Log;

public class MainActivity extends Activity
{
    private static String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_MESSAGE = "com.adamleeper.omegaplot.MESSAGE";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button buttonSend = (Button) findViewById(R.id.button_send);
        // buttonSend.setOnClickListener(this);
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
