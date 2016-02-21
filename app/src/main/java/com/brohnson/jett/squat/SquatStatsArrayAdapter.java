package com.brohnson.jett.squat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by brett on 2/20/16.
 */
public class SquatStatsArrayAdapter extends ArrayAdapter<String> {
    int values[];
    String  names[];
    Context context;
    public SquatStatsArrayAdapter(Context context, int resource, String names[], int values[]) {
        super(context, resource,names);
        this.values=values;
        this.names = names;
        this.context=context;

    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View rowview = inflate.inflate(R.layout.squat_stats_listview_individual, null, true);
        TextView name = (TextView)rowview.findViewById(R.id.name);
        TextView value = (TextView)rowview.findViewById(R.id.value);
        name.setText(names[position]);
        switch (position) {
            case (5):
                value.setText(((float)values[position])/1000 + "s");
                break;
            case (0):
                value.setText(Math.abs(values[position]) + "" + (char) 0x00B0);
                break;
            default:
                value.setText(Math.abs(values[position]) + "" + (char) 0x00B0 + "/s");
                break;
        };
        return rowview;
    }

}
