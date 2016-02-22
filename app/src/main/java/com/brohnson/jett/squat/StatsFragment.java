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
            ArrayList<Integer> anglesdata = new ArrayList<>();
            ArrayList<Long> timesdata = new ArrayList<>();
            FileInputStream fin = context.openFileInput("squats.dat");

            DataInputStream din = new DataInputStream(fin);

            if(din.available()==0)
                throw new DataFormatException();
            while(din.available()>0){
                timesdata.add(din.readLong());
                anglesdata.add(din.readInt());
            }
            Integer angles[] = anglesdata.toArray(new Integer[anglesdata.size()]);
            Long times[] = timesdata.toArray(new Long[timesdata.size()]);
            int interval=100;
            DataPoint data[] = new DataPoint[times.length/interval+1];

            if(times.length%interval==0){
                data = new DataPoint[times.length/interval];
            }
            for(int a =0;a<times.length;a+=interval){
                data[a/interval] = new DataPoint(times[a]-times[0],angles[a]);
            }
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
            graph.getViewport().setMaxX(times[times.length-1]-times[0] + 500);
            graph.getViewport().setMinY(-40);
            graph.getViewport().setMaxY(90);
            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getViewport().setScalable(true);
            graph.getViewport().setScrollable(true);

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
