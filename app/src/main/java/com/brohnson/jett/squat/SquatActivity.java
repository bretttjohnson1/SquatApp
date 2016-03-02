package com.brohnson.jett.squat;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
      GraphView graph = (GraphView)findViewById(R.id.graph_squat_individual);
      int interval = 30;

      DataPoint data[] = new DataPoint[(squat.end-squat.start)/interval+1];
      if((squat.end-squat.start)%interval==0){
         data = new DataPoint[(squat.end-squat.start)/interval];
      }
      Log.d("size", squat.end - squat.start + " " + squat.times.length + " " + data.length);
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
      zeroline.setTitle("Required Depth (" + Squat.REQUIRED_DEPTH + (char) 0x00B0 + ")");
      zeroline.setColor(Color.RED);
      graph.addSeries(zeroline);
      graph.getViewport().setYAxisBoundsManual(true);
      graph.getViewport().setXAxisBoundsManual(true);
      graph.getViewport().setMaxX(squat.times[squat.end] - squat.times[squat.start] + 500);
      if(squat.depth<=Squat.REQUIRED_DEPTH)
         graph.getViewport().setMinY(90-4*(int)((90-squat.depth+10)/4));
      else
         graph.getViewport().setMinY(-10);
      graph.getViewport().setMaxY(90);
      graph.getLegendRenderer().setVisible(true);
      graph.getLegendRenderer().setBackgroundColor(Color.TRANSPARENT);
      graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
      graph.getViewport().setScalable(true);
      graph.getViewport().setScrollable(true);


      ListView listview = (ListView)findViewById(R.id.individual_stats_listview);
      int types[] = new int[]{1,0,0,0,0,2};
      int values[] = new int[]{squat.depth, squat.averageupspeed,squat.averagedownspeed,squat.maxupspeed,squat.maxdropspeed,(int)(squat.endtimeunder-squat.starttimeunder)};
      String names[] = new String[]{"Depth","Average Upward Angular Speed","Average Downward Angular Speed","Max Upward Angular Speed","Max Downward Angular Speed","Time At Bottom"};
      listview.setAdapter(new SquatStatsArrayAdapter(this,R.id.individual_stats_listview,names,values,types));

   }
}
