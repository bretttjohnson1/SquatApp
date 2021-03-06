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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * Created by brett on 2/9/16.
 */
public class StatsFragment extends Fragment {
    View rootView;
    Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Stats");

        try {
            context=MainActivity.globalContext;
            rootView = inflater.inflate(R.layout.activity_squat_stats, container, false);
            /**
             * the following 18 lines of code read the raw squat data
             * and put it into a graphable form and array form
             */
            FileInputStream  fin = context.openFileInput("length.dat");
            DataInputStream din = new DataInputStream(fin);
            int arraylength = 0;
            if(din.available()>=4)
                arraylength=din.readInt();
            din.close();;
            fin = context.openFileInput("squats.dat");
            din = new DataInputStream(fin);

            int angles[] = new int[arraylength];
            long times[] = new long[arraylength];

            if(din.available()<12)
                throw new DataFormatException();

            Squat squats[] = Squat.readsquatdata(context,arraylength);
            if(squats.length==0)
                return rootView;

            for(int a = 0;a<arraylength;a++) {
                times[a]=din.readLong();
                angles[a] = din.readInt();
            }

            /**
             * the following 7 lines of code put data in to an array of datapoints based off the interval
             * ie: 100 means it reads every 100 points
             */
            final int interval=1;
            DataPoint data[] = new DataPoint[times.length/interval+1];
            if(times.length%interval==0){
                data = new DataPoint[times.length/interval];
            }
            for(int a =0;a<times.length;a+=interval){
                data[a/interval] = new DataPoint(times[a]-times[0],angles[a]);
            }

            /**
             * the following 21 lines of code set up the graph
             */
            GraphView graph = (GraphView)rootView.findViewById(R.id.graph_squat);
            LineGraphSeries<DataPoint> linegraph = new LineGraphSeries<DataPoint>(data);
            linegraph.setColor(ContextCompat.getColor(context, R.color.Blue200));
            linegraph.setTitle("Angle");
            graph.addSeries(linegraph);
            DataPoint[] zero = new DataPoint[2];
            zero[0] = new DataPoint(0,Squat.REQUIRED_DEPTH);
            zero[1] = new DataPoint(times[times.length-1]-times[0]+500,Squat.REQUIRED_DEPTH);
            LineGraphSeries<DataPoint> zeroline = new LineGraphSeries<DataPoint>(zero);
            zeroline.setTitle("Required Depth (" + Squat.REQUIRED_DEPTH + (char) 0x00B0 + ")");
            zeroline.setColor(ContextCompat.getColor(context,R.color.SoftRed));
            graph.addSeries(zeroline);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(times[times.length - 1] - times[0] + 500);
            graph.getViewport().setMinY(-40);
            graph.getViewport().setMaxY(90);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getViewport().setScalable(true);
            graph.getViewport().setScrollable(true);
            graph.getGridLabelRenderer().setGridColor(ContextCompat.getColor(context, R.color.GreyText));
            graph.getGridLabelRenderer().setHorizontalLabelsColor(ContextCompat.getColor(context, R.color.GreyText));
            graph.getGridLabelRenderer().setVerticalLabelsColor(ContextCompat.getColor(context, R.color.GreyText));
            graph.getLegendRenderer().setTextColor(ContextCompat.getColor(context, R.color.GreyText));
            graph.getGridLabelRenderer().reloadStyles();



            int averagedepth = 0;
            int averageupspeed = 0;
            int averagedownspeed = 0;
            int averagetimedownunder = 0;
            long averagepause=0;
            long maxpause=0;
            for(int a =0;a< squats.length;a++){
                averagedepth+=squats[a].depth;
                averageupspeed+=squats[a].averageupspeed;
                averagedownspeed+=squats[a].averagedownspeed;
                averagetimedownunder+=(squats[a].endtimeunder-squats[a].starttimeunder);
                averagepause+=times[squats[a].end]-times[squats[a].start];
                if(a<squats.length-1){
                    if((times[squats[a+1].start]-times[squats[a].end])>maxpause)
                        maxpause=times[squats[a+1].start]-times[squats[a].end];

                }
            }
            averagepause=(times[times.length-1]-averagepause-squats[0].starttimeunder)/squats.length;
            averagedepth/=squats.length;
            averageupspeed/=squats.length;
            averagedownspeed/=squats.length;
            averagetimedownunder/=squats.length;

            GridView listview = (GridView)rootView.findViewById(R.id.stats_gridview);
            int types[] = new int[]{1,0,0,2,2,2};
            int values[] = new int[]{averagedepth,averageupspeed,averagedownspeed,averagetimedownunder,(int)averagepause,(int)maxpause};
            String names[] = new String[]{"Average Depth","Average Upward\n Angular Speed","Average Downward\n  Angular Speed","Average Time\n  At Bottom"," Average Pause\nBetween Squats"," Longest Pause\nBetween Squats"};
            listview.setAdapter(new SquatGridArrayAdapter(context, names, values,types));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
            Toast.makeText(context, "No Squats Recorded Yet", Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }
}
