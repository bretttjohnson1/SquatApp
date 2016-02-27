package com.brohnson.jett.squat;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
        try {
            context=MainActivity.globalContext;
            rootView = inflater.inflate(R.layout.fragment_stats, container, false);
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
            for(int a = 0;a<arraylength;a++) {
                times[a]=din.readLong();
                angles[a] = din.readInt();
            }


            /**
             * the following 7 lines of code put data in to an array of datapoints based off the interval
             * ie: 100 means every 100 points
             */
            final int interval=100;
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
            linegraph.setColor(ContextCompat.getColor(context, R.color.Blue600));
            linegraph.setTitle("Angle");
            graph.addSeries(linegraph);
            DataPoint[] zero = new DataPoint[2];
            zero[0] = new DataPoint(0,Squat.REQUIRED_DEPTH);
            zero[1] = new DataPoint(times[times.length-1]-times[0]+500,Squat.REQUIRED_DEPTH);
            LineGraphSeries<DataPoint> zeroline = new LineGraphSeries<DataPoint>(zero);
            zeroline.setTitle("Required Depth (" + Squat.REQUIRED_DEPTH + (char) 0x00B0 + ")");
            zeroline.setColor(Color.RED);
            graph.addSeries(zeroline);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMaxX(times[times.length - 1] - times[0] + 500);
            graph.getViewport().setMinY(-40);
            graph.getViewport().setMaxY(90);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getViewport().setScalable(true);
            graph.getViewport().setScrollable(true);


            /**
             * TODO: make average, max, and min rest times
             */
            long starttime = 0;
            long endttime = 0;
            for(int a = 0;a<arraylength;a++){
                if(starttime==0 && angles[a]>=Squat.MINIMUM_START_DEPTH-1)
                    starttime=times[a];
                else if(starttime!=0 && endttime==0 && angles[a]<=Squat.MINIMUM_START_DEPTH+1){
                    endttime = times[a];
                }

            }

           // ListView listview = (ListView)rootView.findViewById(R.id.individual_stats_listview);
          //  int values[] = new int[]{squat.depth, squat.averageupspeed,squat.averagedownspeed,squat.maxupspeed,squat.maxdropspeed,(int)(squat.endtimeunder-squat.starttimeunder)};
          //String names[] = new String[]{"Depth","Average Upward Angular Speed","Average Downward Angular Speed","Max Upward Angular Speed","Max Downward Angular Speed","Time At Bottom"};
           // listview.setAdapter(new SquatStatsArrayAdapter(this, R.id.individual_stats_listview, names, values));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DataFormatException e) {
            e.printStackTrace();
            Toast.makeText(context, "No Data Recorded Yet", Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }
}
