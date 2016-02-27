package com.brohnson.jett.squat;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by brett on 2/14/16.
 */
public class Squat implements Parcelable{
    public static int REQUIRED_DEPTH = 3;
    public static int MINIMUM_START_DEPTH=60;

    boolean completed;
    int depth,start,end;
    int maxdropspeed;
    int maxupspeed;
    int averageupspeed;
    int averagedownspeed;
    long starttimeunder;
    long endtimeunder;
    long times[];
    int angles[];
    public Squat(int angles[], long times[], int start, int end){
        int min = 90;
        for(int a =start;a<end;a++)
            if(min>angles[a])min=angles[a];
        this.depth = min;
        completed = depth<=REQUIRED_DEPTH;
        this.times = times;
        this.angles=angles;
        this.start = start;
        this.end = end;

        maxdropspeed = 0;
        maxupspeed = 0;
        averageupspeed = 0;
        averagedownspeed = 0;
        starttimeunder=0;
        endtimeunder=0;

        for(int a = start;a<end-100;a++){
            int speed = (angles[a+100]-angles[a])*1000/(int)(times[a+100]-times[a]);
            if(speed>maxupspeed)
                maxupspeed=speed;
            if(speed<maxdropspeed)
                maxdropspeed=speed;
        }

        for(int a = start ;a<end;a++){
            if(angles[a]==depth) {
                if(a!= end)
                    averageupspeed = (angles[end]-angles[a])*1000/(int)(times[end]-times[a]);
                if(a != start)
                    averagedownspeed = (angles[a]-angles[start])*1000/(int)(times[a]-times[start]);
                break;
            }
        }

        for(int a = start;a<end;a++){
            if(starttimeunder==0 && angles[a]<=REQUIRED_DEPTH-1)
                starttimeunder=times[a];
            else if(starttimeunder!=0 && endtimeunder==0 && angles[a]>=REQUIRED_DEPTH+1)
                endtimeunder = times[a];
        }
    }

    public static final Parcelable.Creator<Squat> CREATOR = new Parcelable.Creator<Squat>() {
        public Squat createFromParcel(Parcel in) {
            return new Squat(in);
        }

        public Squat[] newArray(int size) {
            return new Squat[size];
        }
    };
    private Squat(Parcel p){
        depth = p.readInt();
        int arrraylength = p.readInt();
        start = p.readInt();
        end = p.readInt();
        maxupspeed = p.readInt();
        maxdropspeed = p.readInt();
        averageupspeed = p.readInt();
        averagedownspeed = p.readInt();
        starttimeunder = p.readLong();
        endtimeunder = p.readLong();
        angles = new int[arrraylength];
        times = new long[arrraylength];
        p.readIntArray(angles);
        p.readLongArray(times);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(depth);
        dest.writeInt(angles.length);
        dest.writeInt(start);
        dest.writeInt(end);
        dest.writeInt(maxupspeed);
        dest.writeInt(maxdropspeed);
        dest.writeInt(averageupspeed);
        dest.writeInt(averagedownspeed);
        dest.writeLong(starttimeunder);
        dest.writeLong(endtimeunder);
        dest.writeIntArray(angles);
        dest.writeLongArray(times);
    }

    public static Squat[] readsquatdata(Context context,int arraylength) throws IOException {
        FileInputStream fin = context.openFileInput("squats.dat");
        DataInputStream din = new DataInputStream(fin);
        ArrayList<Squat> squatsarraylist = new ArrayList<Squat>();
        ArrayList<Integer>endpts = new ArrayList<Integer>();
        ArrayList<Integer>startpts = new ArrayList<Integer>();
        Log.d("Available", din.available() + "");
        boolean below = false;
        int[] angles = new int[arraylength];
        long[] times = new long[arraylength];
        for(int a = 0; a<arraylength;a++){
            times[a]=din.readLong();
            angles[a]=din.readInt();
            if(angles[a]<60 && !below) {
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
            if(s.depth<50)
                squatsarraylist.add(s);
        }

        Squat squats[] = squatsarraylist.toArray(new Squat[squatsarraylist.size()]);
        return squats;
    }
}
