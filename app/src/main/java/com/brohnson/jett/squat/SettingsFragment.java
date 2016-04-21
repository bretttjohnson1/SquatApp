
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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class SettingsFragment extends Fragment implements View.OnClickListener{

    View rootView;
    Activity mainactivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Settings");
        mainactivity = (Activity)MainActivity.globalContext;
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        int reqdepth=Squat.REQUIRED_DEPTH;
        int timer=StartFragment.COUNTDOWN;
        boolean vibrate = StartFragment.VIBRATEATBOTTOM;

        SharedPreferences sp = mainactivity.getPreferences(Context.MODE_PRIVATE);
        reqdepth = sp.getInt(mainactivity.getString(R.string.REQUIRED_DEPTH), Squat.REQUIRED_DEPTH);
        timer = sp.getInt(mainactivity.getString(R.string.COUNTDOWN), StartFragment.COUNTDOWN);
        vibrate = sp.getBoolean(mainactivity.getString(R.string.VIBRATEONBOTTOM), StartFragment.VIBRATEATBOTTOM);

        ToggleButton tb = (ToggleButton)rootView.findViewById(R.id.toggleButton);
        tb.setChecked(vibrate);
        Button b = (Button)rootView.findViewById(R.id.save_button);
        b.setOnClickListener(this);
        EditText ed = (EditText)rootView.findViewById(R.id.required_depth_edit_text);
        ed.setText(reqdepth + "");
        EditText ed2 = (EditText)rootView.findViewById(R.id.countdown_edit_text);
        ed2.setText(timer+"");
        ed2.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        EditText ed2 = (EditText) rootView.findViewById(R.id.countdown_edit_text);
                        if (Integer.parseInt(ed2.getText().toString()) < 0 || Integer.parseInt(ed2.getText().toString()) > 20) {

                            ed2.setText(StartFragment.COUNTDOWN + "");
                        } else {
                            SeekBar seek = (SeekBar) rootView.findViewById(R.id.seekBar2);
                            seek.setProgress(Integer.parseInt(ed2.getText().toString()));
                        }

                    } catch (Exception e) {
                        EditText ed2 = (EditText) rootView.findViewById(R.id.countdown_edit_text);

                        ed2.setText(StartFragment.COUNTDOWN + "");
                    }
                    return true;
                }
                return  false;
            }

        });

        ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        EditText ed = (EditText) rootView.findViewById(R.id.required_depth_edit_text);

                        if (Integer.parseInt(ed.getText().toString())+20 < 0 || Integer.parseInt(ed.getText().toString())+20 > 40) {

                            ed.setText(Squat.REQUIRED_DEPTH + "");
                        } else {

                            SeekBar seek = (SeekBar) rootView.findViewById(R.id.seekBar);
                            seek.setProgress(20+Integer.parseInt(ed.getText().toString()));
                        }

                    } catch (Exception e) {
                        EditText ed = (EditText) rootView.findViewById(R.id.required_depth_edit_text);

                        ed.setText(Squat.REQUIRED_DEPTH + "");
                    }
                    return true;
                }
                return  false;
            }
        });
        SeekBar seek = (SeekBar)rootView.findViewById(R.id.seekBar);
        seek.setProgress(reqdepth+20);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EditText ed = (EditText)rootView.findViewById(R.id.required_depth_edit_text);
                ed.setText((progress-20)+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SeekBar seek2 = (SeekBar)rootView.findViewById(R.id.seekBar2);
        seek2.setProgress(timer);
        seek2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                EditText ed2 = (EditText)rootView.findViewById(R.id.countdown_edit_text);
                ed2.setText(progress+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return rootView;

    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.save_button){
            SharedPreferences sp = mainactivity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            EditText ed = (EditText)rootView.findViewById(R.id.required_depth_edit_text);
            EditText ed2 = (EditText)rootView.findViewById(R.id.countdown_edit_text);
            int reqdepth = Squat.REQUIRED_DEPTH;
            int timer = StartFragment.COUNTDOWN;
            ToggleButton tb = (ToggleButton)rootView.findViewById(R.id.toggleButton);
            boolean vibrate = tb.isChecked();
            editor.putBoolean(mainactivity.getString(R.string.VIBRATEONBOTTOM),vibrate);

            try{
                reqdepth = Integer.valueOf(ed.getText().toString());
                timer = Integer.valueOf(ed2.getText().toString());
            }catch (Exception e){

            }
            boolean isnotgood = true;
            if(!(reqdepth>20 || reqdepth<-40)) {
                editor.putInt(mainactivity.getString(R.string.REQUIRED_DEPTH), reqdepth);
                Squat.REQUIRED_DEPTH = reqdepth;
                isnotgood=false;
            } else
                ed.setText(Squat.REQUIRED_DEPTH+"");
            if(!(timer<0 || timer>60)) {
                editor.putInt(mainactivity.getString(R.string.COUNTDOWN), timer);
                StartFragment.COUNTDOWN = timer;
                isnotgood=false;
            } else
                ed2.setText(StartFragment.COUNTDOWN + "");
            editor.commit();
            if(!isnotgood)
                Toast.makeText(mainactivity, "Settings Saved", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mainactivity, "Setting Out of Bounds", Toast.LENGTH_SHORT).show();

        }
    }
}