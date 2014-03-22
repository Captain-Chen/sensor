package com.example.shaker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;


public class MainActivity extends Activity implements SensorEventListener {

// Sensor declarations	
private SensorManager mSensorManager;
private Sensor mAccelerometer;

// For calculations
private final float NOISE_THRESHOLD = (float)2.0;
private float[] gravity = new float[3];
private static final float ALPHA = 0.8f;

private boolean mInitialized;


// File Writer Stuff
private PrintWriter printWriter;
private static final String CSV_HEADER =
"X Axis,Y Axis,Z Axis,Acceleration,Time";
private static final char CSV_DELIM = ',';
private long mSensorTimeStamp;


// Called when the activity is first created
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	// Method that will run when device is turned back on, i.e. turn back on the sensors
	protected void onResume(){
	super.onResume();
	mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);	
	}
	
	@Override
	// Method that will run when the device is turned off, i.e. turn off the sensors to conserve batteries
	protected void onPause(){
	super.onPause();
	mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// do nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event){
		TextView tvX = (TextView)findViewById(R.id.x_axis);
		TextView tvY = (TextView)findViewById(R.id.y_axis);
		TextView tvZ = (TextView)findViewById(R.id.z_axis);
		ImageView iv = (ImageView)findViewById(R.id.image);
		TextView timeStamp = (TextView)findViewById(R.id.timestamp);
		
		// Done
        if(!mInitialized){
        	tvX.setText("0.0");
        	tvY.setText("0.0");
        	tvZ.setText("0.0");
        	mInitialized = true;
        }
		
		// Copies the x,y,z values into a new array 'values'
		float[] values = event.values.clone();
		
		// Pass the values of the array into the highPass method in order to filter out the noise
		// Returns filtered values
		values = highPass(values[0],values[1],values[2]);
		
		// Some math stuff
		double sumOfSquares = (values[0] * values[0])
                + (values[1] * values[1])
                + (values[2] * values[2]);
        double acceleration = Math.sqrt(sumOfSquares);
        
        // Checks for "movement" of the device by comparing its total acceleration to threshold
        if(acceleration > NOISE_THRESHOLD){
			tvX.setText(Float.toString(values[0]));
			tvY.setText(Float.toString(values[1]));
			tvZ.setText(Float.toString(values[2]));
			// Get the current time
			mSensorTimeStamp = event.timestamp;
			timeStamp.setText(Long.toString(mSensorTimeStamp));
			File dataFile = null;
			 try
		        {
		            printWriter = 
		                    new PrintWriter(new BufferedWriter(new FileWriter(dataFile)));
		            
		            printWriter.println(CSV_HEADER);
		        }
		        catch (IOException e)
		        {
		            System.out.println("Could not open CSV file(s)" + e);
		        }
        }
	}
	/*
     * This method derived from the Android documentation and is available under
     * the Apache 2.0 license.
     * 
     * @see http://developer.android.com/reference/android/hardware/SensorEvent.html
     */
    private float[] highPass(float x, float y, float z)
    {
        float[] filteredValues = new float[3];
        
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;

        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];
        
        return filteredValues;
    }
}

