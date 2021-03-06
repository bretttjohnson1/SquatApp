
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

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class SquatActivity extends AppCompatActivity {

    Squat squat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squat_stats);
        squat = (Squat)getIntent().getParcelableExtra("Squat");
        GraphView graph = (GraphView)findViewById(R.id.graph_squat);
        final int interval = 1;

        DataPoint data[] = new DataPoint[(squat.end-squat.start)/interval+1];
        if((squat.end-squat.start)%interval==0){
            data = new DataPoint[(squat.end-squat.start)/interval];
        }
        Log.d("size", squat.end - squat.start + " " + squat.times.length + " " + data.length);
        for(int a =squat.start;a<squat.end;a+=interval){
            data[(a-squat.start)/interval] = new DataPoint(squat.times[a]-squat.times[squat.start],squat.angles[a]);
        }


        LineGraphSeries<DataPoint> linegraph = new LineGraphSeries<DataPoint>(data);
        linegraph.setColor(ContextCompat.getColor(this,R.color.Blue200));
        linegraph.setTitle("Angle");
        graph.addSeries(linegraph);
        DataPoint[] zero = new DataPoint[2];
        zero[0] = new DataPoint(0,Squat.REQUIRED_DEPTH);
        zero[1] = new DataPoint(squat.times[squat.end]-squat.times[squat.start]+500,Squat.REQUIRED_DEPTH);
        LineGraphSeries<DataPoint> zeroline = new LineGraphSeries<DataPoint>(zero);
        zeroline.setTitle("Required Depth (" + Squat.REQUIRED_DEPTH + (char) 0x00B0 + ")");
        zeroline.setColor(ContextCompat.getColor(this,R.color.SoftRed));
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
        graph.getGridLabelRenderer().setGridColor(ContextCompat.getColor(this, R.color.GreyText));
        graph.getGridLabelRenderer().setHorizontalLabelsColor(ContextCompat.getColor(this, R.color.GreyText));
        graph.getGridLabelRenderer().setVerticalLabelsColor(ContextCompat.getColor(this, R.color.GreyText));
        graph.getLegendRenderer().setTextColor(ContextCompat.getColor(this, R.color.GreyText));
        graph.getGridLabelRenderer().reloadStyles();



        GridView listview = (GridView)findViewById(R.id.stats_gridview);
        int types[] = new int[]{1,0,0,0,0,2};
        int values[] = new int[]{squat.depth, squat.averageupspeed,squat.averagedownspeed,squat.maxupspeed,squat.maxdropspeed,(int)(squat.endtimeunder-squat.starttimeunder)};
        String names[] = new String[]{"Depth","Average Upward\n Angular Speed","Average Downward\n   Angular Speed"," Max Upward\nAngular Speed","Max Downward\n Angular Speed","Time At Bottom"};
        listview.setAdapter(new SquatGridArrayAdapter(this,names,values,types));

    }
}
