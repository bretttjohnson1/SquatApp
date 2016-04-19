
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by brett on 2/20/16.
 */
public class SquatGridArrayAdapter extends BaseAdapter {
    int values[];
    String  names[];
    int types[];
    Context context;
    public SquatGridArrayAdapter(Context context, String names[], int values[], int types[]) {
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
            case (0):
                value.setText(Math.abs(values[position]) + "" + (char) 0x00B0 + "/s");
                break;
            default:
                value.setText(values[position]+"");
        };
        return rowview;
    }


}
