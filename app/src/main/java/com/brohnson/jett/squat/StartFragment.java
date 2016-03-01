package com.brohnson.jett.squat;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by brett on 2/9/16.
 */
public class StartFragment extends Fragment implements SensorEventListener, View.OnClickListener, AdapterView.OnItemClickListener {
    SensorManager mSensorManager;
    PowerManager power;
    PowerManager.WakeLock wl;
    View rootView;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.context=MainActivity.globalContext;

        /**
         * sets up views for the fragment and sets up a wake lock
         * sets up sensormanager
         */
        power = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_start, container, false);
        getActivity().setTitle("Start");
        rootView.findViewById(R.id.startbutton).setOnClickListener(this);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        wl = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "squatting");

        /**
         * reads previous squat data if any
         */
        try {
            FileInputStream fin = context.openFileInput("length.dat");
            DataInputStream din = new DataInputStream(fin);
            if(din.available()>0) {
                arraylength = din.readInt();
                Log.d("length", arraylength+"");
                squats = Squat.readsquatdata(MainActivity.globalContext, arraylength);
                ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
                squatlistview.setAdapter(new StatsArrayAdapter(context, R.id.squat_list_view, squats));
                squatlistview.setOnItemClickListener(this);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return rootView;
    }
    //array of squats to be filled
    Squat squats[];
    int arraylength=0;
    //streams for writing files
    DataOutputStream dout;
    FileOutputStream fout;
    //used for waiting 5 seconds without stopping thread before recording data
    long timepressed = 0;
    //is it recording
    private boolean recording = false;

    /**
     * TODO: prevent duplicate clicks and squatting processes
     *
     */
    public void record(View view){
        if(!recording) {
            /**
             * this try statement begins recording data by clearing the list of current squats and the current squat data file
             * it also resets the parity, past, started and arraylength variables
             */
            try {
                filteredpitch=Float.MAX_VALUE;
                parity=0;
                past=false;
                started=false;
                arraylength=0;
                ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
                squatlistview.setAdapter(null);

                fout =  context.openFileOutput("squats.dat",Context.MODE_PRIVATE);
                dout = new DataOutputStream(fout);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /**
             * the following resets the timepressed variable and registers the appropriate sensor listners
             */
            timepressed = System.currentTimeMillis();
            wl.acquire();;
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
            recording=true;

        }
        else if(recording) {
            mSensorManager.unregisterListener(this);
            recording=false;

            /**
             * the following try statement reads squat data and formats the list view with squats
             * it then writes the length variable for later use
             */
            try {
                dout.close();
                squats =  Squat.readsquatdata(MainActivity.globalContext,arraylength);

                ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
                squatlistview.setAdapter(new StatsArrayAdapter(context, R.id.squat_list_view, squats));
                squatlistview.setOnItemClickListener(this);

                FileOutputStream fout = context.openFileOutput("length.dat",Context.MODE_PRIVATE);
                DataOutputStream dout = new DataOutputStream(fout);
                dout.writeInt(arraylength);
                arraylength=0;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error could not close datastream ", Toast.LENGTH_SHORT).show();
            }

            wl.release();
        }
    }

    //float array for storing geomagnetic vector data
    float[] geomagnetic;
    //float array for storing gravity vector
    float[] gravity;
    //pitch that has been filtered
    float filteredpitch = Float.MAX_VALUE;
    //whether the phone started upside down or rightside up
    int parity = 0;
    //has the phone past parallel See: Squat.REQUIRED_DEPTH
    boolean past = false;
    //whether the 5 second timer has past
    boolean started = false;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY)
            gravity = event.values;

        if(System.currentTimeMillis()-5000>timepressed && !started){
            started = true;
            Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            if(v.hasVibrator())
                v.vibrate(1000);
        }
        //this statement checks if arrays are not null and then caps the accelerometer data to eliminate bad data
        //one seems to be a good threshold
        if (geomagnetic != null && gravity != null && started ) {
            float rot[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(rot, I, gravity, geomagnetic);
            float orientation[] = new float[3];
            if (success) {

                SensorManager.getOrientation(rot, orientation);

                /**
                 *filters the pitch and sets the parity
                 */
                if (filteredpitch == Float.MAX_VALUE) {
                    filteredpitch = orientation[1];
                    parity = (filteredpitch < 0) ? 1 : -1;
                } else {
                    filteredpitch += (orientation[1] - filteredpitch) * .01f;
                }
                TextView t = (TextView) (rootView.findViewById(R.id.showangle));
                t.setText((int) (-90 * filteredpitch / Math.PI * 2) * parity + " ");

                /**
                 * this try statement writes to the squat data
                 */
                try {
                    dout.writeLong(System.currentTimeMillis());
                    dout.writeInt((int) (-90 * filteredpitch / Math.PI * 2) * parity);
                    arraylength++;

                } catch (IOException e) {
                    e.printStackTrace();
                }


                /**
                 * checks if the required depth in squat has been achieved and vibrates if so
                 */
                if(((int) (-90 * filteredpitch / Math.PI * 2))*parity<Squat.REQUIRED_DEPTH && !past){
                    past = true;
                    Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                    if (v.hasVibrator())
                        v.vibrate(1000);
                }
                if(((int) (-90 * filteredpitch / Math.PI * 2))*parity>Squat.REQUIRED_DEPTH+20 && past){
                    past=false;
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startbutton:
                record(v);

        }

    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(context, SquatActivity.class);
        intent.putExtra("Squat", squats[position]);
        startActivity(intent);
    }
}
