
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
import android.content.Intent;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by brett on 2/14/16.
 */
public class SquatsListArrayAdapter extends ArrayAdapter<Squat> implements AdapterView.OnItemClickListener {
    Squat[] squats;
    Context context;
    public SquatsListArrayAdapter(Context context, int resource, Squat[] objects) {
        super(context, resource, objects);
        this.context= context;
        squats=objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View rowview = inflate.inflate(R.layout.squat_list_item, null, true);

        rowview.setBackgroundColor((position % 2 == 0) ? ContextCompat.getColor(context, R.color.LightIndigo) : ContextCompat.getColor(context, R.color.Indigo));

        if(squats[position].depth <= Squat.REQUIRED_DEPTH)
            ((TextView)rowview.findViewById(R.id.text_completed)).setText("Completed");
        else
            ((TextView)rowview.findViewById(R.id.text_completed)).setText("Failed");
        ((TextView)rowview.findViewById(R.id.text_angle)).setText("Depth Angle: " + squats[position].depth);
        ImageView image = (ImageView)rowview.findViewById(R.id.imageView);
        image.setScaleX(.8f);
        image.setScaleY(.8f);
        if(squats[position].depth <= Squat.REQUIRED_DEPTH)
            image.setBackground(ContextCompat.getDrawable(context, R.drawable.check));
        else
            image.setBackground(ContextCompat.getDrawable(context, R.drawable.cross));


        return rowview;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(context, SquatActivity.class);
        intent.putExtra("Squat", squats[position]);
        ;
    }
}
