package com.brohnson.jett.squat;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

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

        power = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        rootView = inflater.inflate(R.layout.fragment_start, container, false);
        getActivity().setTitle("Start");
        rootView.findViewById(R.id.startbutton).setOnClickListener(this);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        wl = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "squatting");

        return rootView;
    }

    DataOutputStream dout;
    FileOutputStream fout;
    long timepressed = 0;
    private boolean recording = false;
    public void record(View view){
        if(!recording) {
            try {
                filteredpitch=Float.MAX_VALUE;
                parity=0;
                past=false;
                arraylength=0;
                ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
                squatlistview.setAdapter(null);

                fout =  context.openFileOutput("squatsraw.dat",Context.MODE_PRIVATE);
                dout = new DataOutputStream(fout);
                } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            timepressed = System.currentTimeMillis();
            wl.acquire();;
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
            recording=true;

        }
        else if(recording) {
            mSensorManager.unregisterListener(this);
            recording=false;

            try {
                started=false;
                dout.close();
                done();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Error could not close datastream ", Toast.LENGTH_SHORT).show();
            }

            wl.release();
        }
    }
    int arraylength=0;
    Squat squats[];
    private void done() throws IOException {

        FileInputStream fin = context.openFileInput("squatsraw.dat");
        DataInputStream din = new DataInputStream(fin);
        ArrayList<Squat> squatsarraylist = new ArrayList<Squat>();
        ArrayList<Integer>endpts = new ArrayList<Integer>();
        ArrayList<Integer>startpts = new ArrayList<Integer>();
        Log.d("Available", din.available()+"");
        boolean below = false;
        int[] angles = new int[arraylength];
        long[] times = new long[arraylength];
        for(int a = 0; a<arraylength;a++){
            times[a]=din.readLong();
            angles[a]=din.readInt();
            if(angles[a]<55 && !below) {
                below = true;
                startpts.add(a);
            }else if(angles[a]>60 && below){
                below  = false;
                endpts.add(a);
            }

        }
        fin.getChannel().position(0);
        din.close();
        for(int a = 0;a<endpts.size();a++){
            Squat s = new Squat(angles,times,startpts.get(a),endpts.get(a));
                    if(s.depth<55)
                        squatsarraylist.add(s);
        }
        ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
        squats = squatsarraylist.toArray(new Squat[squatsarraylist.size()]);


        squatlistview.setAdapter(new StatsArrayAdapter(context, R.id.squat_list_view, squats));
        squatlistview.setOnItemClickListener(this);
    }

    float[] geomagnetic;
    float[] gravity;
    float filteredpitch = Float.MAX_VALUE;
    int parity = 0;
    boolean past = false;
    boolean started = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        if(System.currentTimeMillis()-5000>timepressed && !started){
            started = true;
            Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            if(v.hasVibrator())
                v.vibrate(1000);
        }
        if (geomagnetic != null && gravity != null &&started) {
            float rot[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(rot, I, gravity, geomagnetic);
            float orientation[] = new float[3];
            if (success) {

                SensorManager.getOrientation(rot, orientation);
                if (filteredpitch == Float.MAX_VALUE) {
                    filteredpitch = orientation[1];
                    parity = (filteredpitch<0)?1:-1;
                } else {
                    filteredpitch += (orientation[1] - filteredpitch) * .01f;
                }
                TextView t = (TextView) (rootView.findViewById(R.id.showangle));
                t.setText((int) (-90 * filteredpitch / Math.PI * 2)*parity + " ");

                //writefile
                try {
                    dout.writeLong(System.currentTimeMillis());
                    dout.writeInt((int) (-90 * filteredpitch / Math.PI * 2) * parity);
                    arraylength++;
                } catch (IOException e) {
                    e.printStackTrace();
                }


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
