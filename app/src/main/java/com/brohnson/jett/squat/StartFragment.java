/*
 * This Program checks user squat depth, gathers data, and provides useful feedback
 *     Copyright (C) <2016>  <Brett Johnson>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.brohnson.jett.squat;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by brett on 2/9/16.
 */
public class StartFragment extends Fragment implements SensorEventListener, View.OnClickListener, AdapterView.OnItemClickListener {
    SensorManager mSensorManager;
    PowerManager power;
    PowerManager.WakeLock wl;
    View rootView;
    Context context;
    //time in ms until recording starts after button press
    public static int COUNTDOWN = 10;
    public static boolean VIBRATEATBOTTOM = true;
    long waittime = COUNTDOWN*1000;


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
        getActivity().setTitle("Check Form");
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
                squatlistview.setAdapter(new SquatsListArrayAdapter(context, R.id.squat_list_view, squats));
                squatlistview.setOnItemClickListener(this);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gengrid(squats);


        return rootView;
    }

    @Override
    public void onDestroy() {
        if(recording)
            record(rootView);
        super.onDestroy();
    }


    //array of squats to be filled
    Squat squats[];
    int arraylength=0;
    int count=0;
    //streams for writing files
    DataOutputStream dout;
    FileOutputStream fout;
    //used for waiting 5 seconds without stopping thread before recording data
    long timepressed = 0;
    //is it recording
    private boolean recording = false;

    private void gengrid(Squat squats[]){
        int types[] = new int[]{1, 3};
        int values[] = new int[2];
        String names[] = new String[]{"Average Depth", "Total Squats"};
        GridView gridview = (GridView) rootView.findViewById(R.id.stats_gridview);
        if(squats!=null)
        if(squats.length!=0) {
            int averagedepth = 0;
            for (int a = 0; a < squats.length; a++) {
                averagedepth += squats[a].depth;
            }
            averagedepth /= squats.length;
            values = new int[]{averagedepth, squats.length};
        }
        gridview.setAdapter(new SquatGridArrayAdapter(context, names, values, types));

    }


    public void record(View view){
        if(!recording) {
            startrecord();
        }else if(!started){
            handler.removeCallbacks(initrec);
            handler.removeCallbacks(countdown);
            ((Button)rootView.findViewById(R.id.startbutton)).setText("Start");
            ((Button)rootView.findViewById(R.id.startbutton)).setBackground(ContextCompat.getDrawable(context, R.drawable.start_button));
            recording=false;
        }
        else if(recording) {
            endrecord();
            gengrid(squats);
        }
    }

    /**
     * the following 13 lines of code
     * set up the runnable and handler for the countdown text display for the button
     */
    final Handler handler = new Handler();
    class Countdown implements Runnable{
        Button startbutton;
        Countdown(Button b){
            startbutton=b;
        }
        @Override
        public void run() {
            //this writes the time left rounded to 1 decimal
            startbutton.setText(getString(R.string.starting_in, (float) (-(System.currentTimeMillis() - waittime - timepressed)) / 1000));
            handler.postDelayed(this, 10);
        }
    };

    class InitializeRecord implements  Runnable{

        @Override
        public void run() {
            /**
             * this try statement begins recording data by clearing the current squat data file
             * it also resets the parity, past, started and arraylength variables
             * it then sets the start button color red
             */
            try {
                filteredpitch=Float.MAX_VALUE;
                parity=0;
                past=false;
                arraylength=0;
                count=0;
                ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
                squatlistview.setAdapter(null);

                fout =  context.openFileOutput("squats.dat",Context.MODE_PRIVATE);
                dout = new DataOutputStream(fout);

                /**
                 * the following statements stop the countdown and causes a vibration
                 * indicating recording has started
                 */
                handler.removeCallbacks(countdown);
                started = true;
                Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
                if(v.hasVibrator())
                    v.vibrate(1000);
                handler.removeCallbacks(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    //runnable for countdown
    Countdown countdown;
    InitializeRecord initrec;

    private void startrecord(){

        /**
         * the following resets the timepressed variable
         * registers the appropriate sensor listners
         * and activates the countdown
         * it also sets the button to red instead of blue
         */

        ((Button)rootView.findViewById(R.id.startbutton)).setBackground(ContextCompat.getDrawable(context, R.drawable.start_button_cancel));
        timepressed = System.currentTimeMillis();
        countdown =  new Countdown((Button)rootView.findViewById(R.id.startbutton));
        initrec = new InitializeRecord();
        handler.postDelayed(countdown, 0);
        handler.postDelayed(initrec,waittime);
        wl.acquire();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_FASTEST);
        recording=true;
    }
    private void endrecord(){
        ((Button)rootView.findViewById(R.id.startbutton)).setText("Start");
        if(System.currentTimeMillis()-waittime<timepressed)
            handler.removeCallbacks(countdown);
        mSensorManager.unregisterListener(this);
        recording=false;
        started=false;
        wl.release();
        /**
         * the following try statement reads squat data and formats the list view with squats
         * it then writes the length variable for later use
         * it returns the startbutton color to normal blue
         */
        try {
            dout.close();
            ((Button)rootView.findViewById(R.id.startbutton)).setBackground(ContextCompat.getDrawable(context, R.drawable.start_button));
            squats =  Squat.readsquatdata(MainActivity.globalContext,arraylength);
            ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
            squatlistview.setAdapter(new SquatsListArrayAdapter(context, R.id.squat_list_view, squats));
            squatlistview.setOnItemClickListener(this);

            FileOutputStream fout = context.openFileOutput("length.dat",Context.MODE_PRIVATE);
            DataOutputStream dout = new DataOutputStream(fout);
            dout.writeInt(arraylength);
            arraylength=0;
            count=0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Error could not close datastream ", Toast.LENGTH_SHORT).show();
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

                /**
                 * this try statement writes to the squat data
                 */
                if (count % 50 ==0 ){
                    try {
                        dout.writeLong(System.currentTimeMillis());
                        dout.writeInt((int) (-90 * filteredpitch / Math.PI * 2) * parity);
                        arraylength++;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                count++;


                /**
                 * checks if the required depth in squat has been achieved and vibrates if so
                 */
                if(((int) (-90 * filteredpitch / Math.PI * 2))*parity<=Squat.REQUIRED_DEPTH && !past && VIBRATEATBOTTOM){
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
