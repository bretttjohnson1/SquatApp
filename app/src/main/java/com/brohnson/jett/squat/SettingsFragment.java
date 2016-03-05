package com.brohnson.jett.squat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
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
            editor.putInt(mainactivity.getString(R.string.REQUIRED_DEPTH),reqdepth);
            editor.putInt(mainactivity.getString(R.string.COUNTDOWN),timer);

            Squat.REQUIRED_DEPTH = reqdepth;
            StartFragment.COUNTDOWN=timer;
            editor.commit();
            Toast.makeText(mainactivity, "Settings Saved", Toast.LENGTH_SHORT).show();
        }
    }

}