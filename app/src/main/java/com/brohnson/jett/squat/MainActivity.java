
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

import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    //default startwindow (0 is placeholder)
    int defaultpostition = 0;
    Integer ids[];
    public static Context globalContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        SharedPreferences sp = getPreferences(Context.MODE_PRIVATE);
        Squat.REQUIRED_DEPTH = sp.getInt(getString(R.string.REQUIRED_DEPTH),Squat.REQUIRED_DEPTH);
        StartFragment.COUNTDOWN = sp.getInt(getString(R.string.COUNTDOWN),StartFragment.COUNTDOWN);
        StartFragment.VIBRATEATBOTTOM = sp.getBoolean(getString(R.string.VIBRATEONBOTTOM),StartFragment.VIBRATEATBOTTOM);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        StartFragment frag = new StartFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main, frag).commit();

        ListView listview = (ListView)findViewById(R.id.nav_view);
        ids = new Integer[]{R.string.check_form,R.string.stats,R.string.settings, R.string.help};
        listview.setAdapter(new MenuAdapter(this,R.id.nav_view,ids
                ,new Integer[]{R.drawable.ic_start,R.drawable.ic_stats,R.drawable.ic_menu_manage,R.drawable.ic_help}));
        listview.setOnItemClickListener(this);

        globalContext=this;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long idofcontainer) {
        int id = ids[position];
        defaultpostition=position;
        for(int i=0; i<parent.getChildCount(); i++)
        {
            if(i == position)
            {
                parent.getChildAt(i).setBackgroundColor(ContextCompat.getColor(this,R.color.LightIndigo));
            }
            else
            {
                parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
            }

        }

        if (id == R.string.check_form) {

            StartFragment frag = new StartFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main, frag).commit();

        } else if (id == R.string.stats) {

            StatsFragment frag = new StatsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main, frag).commit();
        } else if (id == R.string.settings) {

            SettingsFragment frag = new SettingsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main, frag).commit();
        } else if (id == R.string.help) {

            HelpFragment frag = new HelpFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main, frag).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
    }
}
