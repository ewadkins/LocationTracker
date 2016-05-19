package com.ericwadkins.locationtracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ericwadkins.tabbedlayout.R;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        // Setup view pager
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        final TabLayout.Tab home = tabLayout.newTab();
        final TabLayout.Tab tab2 = tabLayout.newTab();
        final TabLayout.Tab tab3 = tabLayout.newTab();

        home.setText("Home");
        tab2.setText("Tab 2");
        tab3.setText("Tab 3");

        tabLayout.addTab(home, 0);
        tabLayout.addTab(tab2, 1);
        tabLayout.addTab(tab3, 2);

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.colorTabText));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.colorAccent));

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        startTracker();
    }

    public void startTracker() {
        if (!hasTrackerPermissions()) {
            getTrackerPermissions();
        }
        else {
            if (!GPSTracker.isGPSEnabled(this)) {
                GPSTracker.requestEnableGPS(this);
            }
            else {
                GPSTracker tracker = new GPSTracker(this);
                Location location = tracker.getLocation();
                if (location != null) {
                    Log.e("debug", location.getLatitude() + ", " + location.getLongitude());
                }
                else {
                    Log.e("debug", "null");
                }

                Intent intent = new Intent(this, GPSTracker.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this,  0, intent, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.add(Calendar.SECOND, 1); // first time
                long frequency = 5 * 1000; // Frequency in milliseconds
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), frequency, pendingIntent);
            }

        }
    }

    private boolean hasTrackerPermissions() {
        return PermissionHelper.hasPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                });
    }

    private void getTrackerPermissions() {
        PermissionHelper.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET},
                "This app needs access to the Internet and GPS to work properly!",
                new PermissionHelper.PermissionCallback() {
                    @Override
                    public void run(boolean successful, String[] granted, String[] denied) {
                        if (!successful) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title("Permissions required")
                                    .content("This app will not be able to work properly until it" +
                                            " has the required permissions.")
                                    .positiveText("OK")
                                    .show();
                        }
                        else {
                            startTracker();
                        }
                    }
                });
    }

    // This is required if you want to use PermissionHelper
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case 0: fragment = new HomePageFragment(); break;
                case 1: fragment = new Tab2PageFragment(); break;
                case 2: fragment = new Tab3PageFragment(); break;
                default: throw new RuntimeException();
            }
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public static class HomePageFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.home_main, null);

            return rootView;
        }

    }

    public static class Tab2PageFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tab2_main, null);

            return rootView;
        }

    }

    public static class Tab3PageFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.tab3_main, null);

            return rootView;
        }

    }
}
