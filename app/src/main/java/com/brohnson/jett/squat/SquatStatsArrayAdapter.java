package com.brohnson.jett.squat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by brett on 2/20/16.
 */
public class SquatStatsArrayAdapter extends BaseAdapter {
    int values[];
    String  names[];
    int types[];
    Context context;
    public SquatStatsArrayAdapter(Context context, String names[], int values[], int types[]) {
        this.values=values;
        this.names = names;
        this.context=context;
        this.types=types;

    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View rowview = inflate.inflate(R.layout.squat_stats_gridview_individual, null, true);
        TextView name = (TextView)rowview.findViewById(R.id.name);
        TextView value = (TextView)rowview.findViewById(R.id.value);

        name.setText(names[position]);

        switch (types[position]) {
            case (2):
                value.setText(((float)values[position])/1000 + "s");
                break;
            case (1):
                value.setText(values[position]+ "" + (char) 0x00B0);
                break;
            default:
                value.setText(Math.abs(values[position]) + "" + (char) 0x00B0 + "/s");
                break;
        };
        return rowview;
    }


}
