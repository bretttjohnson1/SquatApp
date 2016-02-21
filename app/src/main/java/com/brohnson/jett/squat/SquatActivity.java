package com.brohnson.jett.squat;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class SquatActivity extends AppCompatActivity {

    Squat squat;
   @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_squat);
       squat = (Squat)getIntent().getParcelableExtra("Squat");
       GraphView graph = (GraphView)findViewById(R.id.graph_squat);
       int interval = 30;

       DataPoint data[] = new DataPoint[(squat.end-squat.start)/interval+1];
       if((squat.end-squat.start)%interval==0){
           data = new DataPoint[(squat.end-squat.start)/interval];
       }
       Log.d("size", squat.end-squat.start + " " + squat.times.length + " " + data.length);
       for(int a =squat.start;a<squat.end;a+=interval){
           data[(a-squat.start)/interval] = new DataPoint(squat.times[a]-squat.times[squat.start],squat.angles[a]);
       }


       LineGraphSeries<DataPoint> linegraph = new LineGraphSeries<DataPoint>(data);
       linegraph.setColor(ContextCompat.getColor(this,R.color.Blue600));
       linegraph.setTitle("Angle");
       graph.addSeries(linegraph);
       DataPoint[] zero = new DataPoint[2];
       zero[0] = new DataPoint(0,Squat.REQUIRED_DEPTH);
       zero[1] = new DataPoint(squat.times[squat.end]-squat.times[squat.start]+500,Squat.REQUIRED_DEPTH);
       LineGraphSeries<DataPoint> zeroline = new LineGraphSeries<DataPoint>(zero);
       zeroline.setTitle("Required Depth ("+Squat.REQUIRED_DEPTH+(char) 0x00B0+")");
       zeroline.setColor(Color.RED);
       graph.addSeries(zeroline);
       graph.getViewport().setYAxisBoundsManual(true);
       graph.getViewport().setXAxisBoundsManual(true);
       graph.getViewport().setMaxX(squat.times[squat.end] - squat.times[squat.start] + 500);
       graph.getViewport().setMinY(((int) (-10 + squat.depth) / 10) * 10 - 10);
       graph.getViewport().setMaxY(90);
       graph.getLegendRenderer().setVisible(true);
       graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
       graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
       graph.getViewport().setScalable(true);
       graph.getViewport().setScrollable(true);

       int maxdropspeed = 0;
       int maxupspeed = 0;

       for(int a = squat.start;a<squat.end-100;a++){
           int speed = (squat.angles[a+100]-squat.angles[a])*1000/(int)(squat.times[a+100]-squat.times[a]);
           if(speed>maxupspeed)
               maxupspeed=speed;
           if(speed<maxdropspeed)
               maxdropspeed=speed;
        }
       int averageupspeed = 0;
       int averagedownspeed = 0;
       for(int a = squat.start ;a<squat.end;a++){
            if(squat.angles[a]==squat.depth) {
                averageupspeed = (squat.angles[squat.end]-squat.angles[a])*1000/(int)(squat.times[squat.end]-squat.times[a]);
                averagedownspeed = (squat.angles[a]-squat.angles[squat.start])*1000/(int)(squat.times[a]-squat.times[squat.start]);
                break;
            }
       }
       long starttimeunder=0;
       long endtimeunder=0;
       for(int a = squat.start;a<squat.end;a++){
           if(starttimeunder==0 && squat.angles[a]<=Squat.REQUIRED_DEPTH-1)
               starttimeunder=squat.times[a];
           else if(starttimeunder!=0 && endtimeunder==0 && squat.angles[a]>=Squat.REQUIRED_DEPTH+1)
               endtimeunder = squat.times[a];
       }
       ListView listview = (ListView)findViewById(R.id.individual_stats_listview);
       int values[] = new int[]{squat.depth, averageupspeed,averagedownspeed,maxupspeed,maxdropspeed,(int)(endtimeunder-starttimeunder)};
       String names[] = new String[]{"Depth","Average Upward Angular Speed","Average Downward Angular Speed","Max Upward Angular Speed","Max Downward Angular Speed","Time At Bottom"};
       listview.setAdapter(new SquatStatsArrayAdapter(this,R.id.individual_stats_listview,names,values));




    }
}
