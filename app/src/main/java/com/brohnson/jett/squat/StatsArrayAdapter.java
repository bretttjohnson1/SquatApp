package com.brohnson.jett.squat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.TextView;

/**
 * Created by brett on 2/14/16.
 */
public class StatsArrayAdapter extends ArrayAdapter<Squat> implements AdapterView.OnItemClickListener {
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
        ((TextView)rowview.findViewById(R.id.text_angle)).setText("Depth Angle: " + squats[position].depth);

        if(squats[position].depth <= Squat.REQUIRED_DEPTH)
            ((TextView) rowview.findViewById(R.id.text_completed)).setTextColor(ContextCompat.getColor(context,R.color.Blue500));
        else
            ((TextView) rowview.findViewById(R.id.text_completed)).setTextColor(ContextCompat.getColor(context,R.color.Red600));

        return rowview;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(context, SquatActivity.class);
        intent.putExtra("Squat", squats[position]);
        ;
    }
}
