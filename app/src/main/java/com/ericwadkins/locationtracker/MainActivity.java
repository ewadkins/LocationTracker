package com.ericwadkins.locationtracker;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ericwadkins.tabbedlayout.R;

public class MainActivity extends AppCompatActivity {

    private static final int TRACKER_REQUEST_CODE = 1;
    private static PermissionHelper permissionHelper;

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

        permissionHelper = new PermissionHelper(this, true)
                .setPermissionGrantedCallback(new PermissionHelper.PermissionCallback() {
                    @Override
                    public void run(int requestCode, String[] granted, String[] denied) {
                        switch (requestCode) {
                            case TRACKER_REQUEST_CODE: startTracker(); break;
                        }
                    }
                })
                .setPermissionDeniedCallback(new PermissionHelper.PermissionCallback() {
                    @Override
                    public void run(int requestCode, String[] granted, String[] denied) {
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("Permissions required")
                                .content("The app will not be able to function properly until it" +
                                        " has the required permissions.")
                                .positiveText("OK")
                                .show();
                    }
                });

        startTracker();

    }

    public void startTracker() {
        if (!hasTrackerPermissions()) {
            getTrackerPermissions();
        }
        else {

            GPSTracker tracker = new GPSTracker(this);
            Location loc = tracker.getLocation();
            if (loc != null) {
                Log.e("debug", loc.toString());
            }
            else {
                Log.e("debug", "Location is null");
            }

        }
    }

    private boolean hasTrackerPermissions() {
        return permissionHelper.hasPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET
                });
    }

    private void getTrackerPermissions() {
        permissionHelper.requestPermissions(
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET},
                TRACKER_REQUEST_CODE,
                "This app needs to access the internet and your location! Just allow it...");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
