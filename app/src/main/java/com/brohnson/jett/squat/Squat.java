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
        dest.writeIntArray(angles);
        dest.writeLongArray(times);
    }
}
