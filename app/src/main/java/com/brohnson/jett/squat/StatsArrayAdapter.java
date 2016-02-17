package com.brohnson.jett.squat;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;


import java.lang.reflect.Array;
import java.util.zip.Inflater;

/**
 * Created by brett on 2/14/16.
 */
public class StatsArrayAdapter extends ArrayAdapter<Squat> {
    Squat[] squats;
    Context context;
    public StatsArrayAdapter(Context context, int resource, Squat[] objects) {
        super(context, resource, objects);
        this.context= context;
        squats=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View rowview = inflate.inflate(R.layout.squat_list_item, null, true);
        if(squats[position].depth <= Squat.REQUIRED_DEPTH)
            ((TextView)rowview.findViewById(R.id.text_completed)).setText("Completed");
        else
            ((TextView)rowview.findViewById(R.id.text_completed)).setText("Failed");
        ((TextView)rowview.findViewById(R.id.text_angle)).setText("Depth Angle: "+squats[position].depth);
        if(squats[position].depth <= Squat.REQUIRED_DEPTH)
            ((ImageView)rowview.findViewById(R.id.image_completed)).setImageResource(R.drawable.greencheckmark);
        else
            ((ImageView)rowview.findViewById(R.id.image_completed)).setImageResource(R.drawable.redexx);

        return rowview;
    }
}
