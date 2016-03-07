package com.brohnson.jett.squat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;


public class SettingsFragment extends Fragment implements View.OnClickListener{

    View rootView;
    Activity mainactivity;
    public SettingsFragment(Activity main){
        super();
        mainactivity=main;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        int reqdepth=Squat.REQUIRED_DEPTH;
        int timer=StartFragment.COUNTDOWN;

        SharedPreferences sp = mainactivity.getPreferences(Context.MODE_PRIVATE);
        reqdepth = sp.getInt(mainactivity.getString(R.string.REQUIRED_DEPTH),Squat.REQUIRED_DEPTH);
        timer = sp.getInt(mainactivity.getString(R.string.COUNTDOWN),StartFragment.COUNTDOWN);


        Button b = (Button)rootView.findViewById(R.id.save_button);
        b.setOnClickListener(this);
        EditText ed = (EditText)rootView.findViewById(R.id.required_depth_edit_text);
        ed.setText(reqdepth + "");
        EditText ed2 = (EditText)rootView.findViewById(R.id.countdown_edit_text);
        ed2.setText(timer+"");
        ed2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(Integer.parseInt(s.toString())<0 || Integer.parseInt(s.toString())>20) {
                        EditText ed2 = (EditText) rootView.findViewById(R.id.countdown_edit_text);

                        ed2.setText(StartFragment.COUNTDOWN + "");
                    }else {
                        SeekBar seek = (SeekBar) rootView.findViewById(R.id.seekBar2);
                        seek.setProgress(Integer.parseInt(s.toString()));
                    }

                }catch (Exception e){
                    EditText ed2 = (EditText)rootView.findViewById(R.id.countdown_edit_text);

                    ed2.setText(StartFragment.COUNTDOWN + "");
                }
            }
        });
        ed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                    if(Integer.parseInt(s.toString())<0 || Integer.parseInt(s.toString())>40) {

                        EditText ed = (EditText) rootView.findViewById(R.id.required_depth_edit_text);
                        ed.setText(Squat.REQUIRED_DEPTH + "");
                    }else {

                        SeekBar seek = (SeekBar) rootView.findViewById(R.id.seekBar);
                        seek.setProgress(Integer.parseInt(s.toString()));
                    }

                }catch (Exception e){
                    EditText ed = (EditText)rootView.findViewById(R.id.required_depth_edit_text);

                    ed.setText(Squat.REQUIRED_DEPTH + "");
                }
            }
        });
        SeekBar seek = (SeekBar)rootView.findViewById(R.id.seekBar);
        seek.setProgress(reqdepth);
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