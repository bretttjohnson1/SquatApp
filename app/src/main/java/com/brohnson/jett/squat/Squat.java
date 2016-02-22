package com.brohnson.jett.squat;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by brett on 2/14/16.
 */
public class Squat implements Parcelable{
    public static int REQUIRED_DEPTH = 6;
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
                averageupspeed = (angles[end]-angles[a])*1000/(int)(times[end]-times[a]);
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
}
