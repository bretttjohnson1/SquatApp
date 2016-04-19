
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
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by brett on 3/2/16.
 */
public class MenuAdapter extends ArrayAdapter<Integer> {
    Context context;
    Integer stringids[];
    Integer imageids[];
    public MenuAdapter(Context context, int resource,Integer[] stringids,Integer imageids[]) {
        super(context, resource,stringids);
        this.context=context;
        this.stringids=stringids;
        this.imageids=imageids;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflate = LayoutInflater.from(context);
        View rowview = inflate.inflate(R.layout.menu_listview, null, true);

        ImageView iv = (ImageView)rowview.findViewById(R.id.menu_image);
        TextView tv = (TextView)rowview.findViewById(R.id.menu_title);
        tv.setTextColor(ContextCompat.getColor(context,R.color.WhiteText));
        iv.setImageResource(imageids[position]);
        tv.setText(context.getString(stringids[position]));

        return rowview;
    }
}
