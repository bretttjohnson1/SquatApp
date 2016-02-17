package com.brohnson.jett.squat;


import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by brett on 2/9/16.
 */
public class StartFragment extends Fragment implements SensorEventListener, View.OnClickListener {
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
    /*
    @Override
    public void onResume()
    {
        super.onResume();

        // mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_FASTEST);
    }
    */

    DataOutputStream dout;
    FileOutputStream fout;
    private boolean recording = false;
    public void record(View view){
        if(!recording) {
            try {
                fout =  context.openFileOutput("squats.dat", Context.MODE_PRIVATE);
                dout = new DataOutputStream(fout);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try{Thread.sleep(5000);}catch(InterruptedException e){e.printStackTrace();};
            Vibrator v = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            if(v.hasVibrator())
                v.vibrate(1000);
            wl.acquire();;
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
            recording=true;

        }
        else if(recording) {
            mSensorManager.unregisterListener(this);
            recording=false;
            filteredpitch=Float.MAX_VALUE;
            parity=0;
            past=false;

            try {
                fout.close();
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
    int squats = 0;
    private void done() throws IOException {
        FileInputStream fin = context.openFileInput("squats.dat");
        DataInputStream din = new DataInputStream(fin);

        Log.d("Size",din.available()+"");

        Squat squatlist[] = new Squat[squats];
        ArrayList<Integer> angle = new ArrayList<Integer>();
        ArrayList<Long> times = new ArrayList<Long>();
        boolean writing = false;
        ArrayList<Integer> anglestowrite = new ArrayList<Integer>();
        ArrayList<Long> timestowrite = new ArrayList<Long>();

        Log.d("loops",din.available()/12+"");
        int count  = 0;
        for(int a =0;din.available()/12>0;a++){
            times.add(din.readLong());
            angle.add(din.readInt());
            if(!writing){
                if(angle.get(a)<60){
                    writing=true;
                    anglestowrite = new ArrayList<Integer>();
                    timestowrite = new ArrayList<Long>();
                }

            }
            Log.d("angle",angle.get(a)+"");
            if(writing){
                if(angle.get(a)>=60){
                    writing=false;
                    Long t[] = timestowrite.toArray(new Long[timestowrite.size()]);
                    Integer s[] = anglestowrite.toArray(new Integer[anglestowrite.size()]);
                    squatlist[count] = new Squat(s,t);
                    count++;

                }
                anglestowrite.add(angle.get(a));
                timestowrite.add(times.get(a));

                //Log.d("loops", din.available() / 12 + "");
            }
        }
        Log.d("count",count+"");
        fin.close();
        din.close();
        ListView squatlistview = (ListView)rootView.findViewById(R.id.squat_list_view);
        squatlistview.setAdapter(new StatsArrayAdapter(context,R.id.squat_list_view, squatlist));
    }

    float[] geomagnetic;
    float[] gravity;
    float filteredpitch = Float.MAX_VALUE;
    int parity = 0;
    boolean past = false;
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            gravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            geomagnetic = event.values;
        float rot[] = new float[9];
        float I[] = new float[9];
        if (geomagnetic != null && gravity != null) {
            boolean success = SensorManager.getRotationMatrix(rot, I, gravity, geomagnetic);
            float orientation[] = new float[3];
            if (success) {

                SensorManager.getOrientation(rot, orientation);
                if (filteredpitch == Float.MAX_VALUE) {
                    filteredpitch = orientation[1];
                    parity = (filteredpitch<0)?1:-1;
                } else {
                    filteredpitch += (orientation[1] - filteredpitch) * .05f;
                }
                TextView t = (TextView) (rootView.findViewById(R.id.showangle));
                t.setText((int) (-90 * filteredpitch / Math.PI * 2)*parity + " ");

                //writefile
                try {
                    dout.writeLong(System.currentTimeMillis());
                    dout.writeInt((int) (-90 * filteredpitch / Math.PI * 2)*parity);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                if(((int) (-90 * filteredpitch / Math.PI * 2))*parity<Squat.REQUIRED_DEPTH && !past){
                    past = true;
                    Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
                    if (v.hasVibrator())
                        v.vibrate(1000);
                    squats++;
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
}
