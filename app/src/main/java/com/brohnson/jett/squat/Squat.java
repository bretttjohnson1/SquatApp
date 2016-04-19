
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
    public static int REQUIRED_DEPTH = 5;
    public static int MINIMUM_START_DEPTH=50;

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
        int starttimeunderindex = -1;
        int endtimeunderindex = -1;
        for(int a = start;a<end;a++){
            if(starttimeunder==0 && angles[a]<=REQUIRED_DEPTH-1) {
                starttimeunderindex=a;
                starttimeunder = times[a];
            }
            else if(starttimeunder!=0 && endtimeunder==0 && angles[a]>=REQUIRED_DEPTH+1) {
                endtimeunderindex=a;
                endtimeunder = times[a];
            }
        }

        for(int a = start;a<end-10;a++){
            int speed = (angles[a+10]-angles[a])*1000/(int)(times[a+10]-times[a]);
            if(speed>maxupspeed)
                maxupspeed=speed;
            if(speed<maxdropspeed)
                maxdropspeed=speed;
        }


        if(endtimeunderindex != -1 && endtimeunderindex != end)
            averageupspeed = (angles[end]-angles[endtimeunderindex])*1000/(int)(times[end]-times[endtimeunderindex]);
        if(starttimeunderindex != -1 && starttimeunderindex != start)
            averagedownspeed = (angles[starttimeunderindex]-angles[start])*1000/(int)(times[starttimeunderindex]-times[start]);


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
            if(angles[a]<MINIMUM_START_DEPTH && !below) {
                below = true;
                startpts.add(a);
            }else if(angles[a]>MINIMUM_START_DEPTH && below){
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
