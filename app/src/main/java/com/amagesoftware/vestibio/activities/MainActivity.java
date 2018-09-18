package com.amagesoftware.vestibio.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;

import android.widget.TextView;
import android.widget.Toast;

import com.amagesoftware.vestibio.Constants;
import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.fragments.AboutVestibioFragment;
import com.amagesoftware.vestibio.fragments.DisclaimerFragment;
import com.amagesoftware.vestibio.fragments.DonateFragment;
import com.amagesoftware.vestibio.fragments.GraphFragment;
import com.amagesoftware.vestibio.fragments.MetronomeFragment;
import com.amagesoftware.vestibio.fragments.ResourcesFragment;
import com.amagesoftware.vestibio.fragments.SessionListFragment;
import com.amagesoftware.vestibio.metronome.Session;
import com.amagesoftware.vestibio.tools.AppRater;


import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    //    @BindView(R.id.slidingTabs)
//    SlidingTabLayout mSlidingTabLayout;
//    @BindView(R.id.viewPager)
//    CustomViewPager mViewPager;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView mBottomNavigationView;
    Toolbar toolbarTop;
    View layout;
    android.support.v4.app.Fragment fragment;
    private String selectedTab = "";
    private static String RATER_SHOW = "RATER_SHOW";
    private static String TAB = "TAB";
    private Fragment selectedFragment;
    private Boolean showRater= false;


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


//    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//must be called before setContentView(...)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            //window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        }

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();

                this.selectedTab =  bundle.getString(TAB);
                this.showRater = (Boolean) bundle.getBoolean(RATER_SHOW);
        }


//        if (getIntent().getExtras() != null) {
//            selectedTab = getIntent().getExtras().getString("TAB");
//        }

            if(showRater){
                AppRater.app_launched(this);
                showRater= false;
            }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //setTitle(getString(R.string.app_name_short));

        toolbarTop = (Toolbar) findViewById(R.id.toolbar);
        layout = (View) findViewById(R.id.rel);
        TextView mTitle = (TextView) toolbarTop.findViewById(R.id.toolbar_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        setToolbar(toolbarTop);

        Typeface font = Typeface.createFromAsset(
                getAssets(),
                "fonts/Variane Script.ttf");
        Log.d("vertigo", "onCreate:  font is" + font);

        //TextView actionbar_title = (TextView) findViewById(R.id.toolbar_title);
        mTitle.setTypeface(font);
        mTitle.setText(getText(R.string.app_name));
        mTitle.setTextColor(getResources().getColor(R.color.themeGradientLighter_background));
        mTitle.setTextSize(40);


        mBottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigation);


        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                 fragment = null;
                switch (item.getItemId()) {
                    case R.id.action_metronome:
                        fragment = MetronomeFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

                        break;

                    case R.id.action_session:
                        fragment = SessionListFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

                        break;

                    case R.id.action_graph:
                        fragment = GraphFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

                        break;

                }
                return true;
            }
        });



        if (selectedTab != null && selectedTab.equals(getResources().getString(R.string.session_tab))) {
            mBottomNavigationView.setSelectedItemId(R.id.action_session);
            Fragment selectedFragment = null;
            selectedFragment = SessionListFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            Log.d("vestibio", "in session tab? ");

        }


//        Fragment selectedFragment = MetronomeFragment.newInstance();
//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();


    }

    public BottomNavigationView getBottomViewNavigation() {
        return mBottomNavigationView;
    }

    public Toolbar getViewToolbar() {
        return toolbar;
    }

    public Toolbar getToolbar() {
        return toolbarTop;
    }

    public SharedPreferences getSettings() {
        return getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_help:
               // android.app.Fragment f = getFragmentManager().getFragments();
                if(null != fragment){
                   if(fragment.getClass() == MetronomeFragment.class){
                       launchHelp(1);
                   }else if(fragment.getClass() == SessionListFragment.class){
                       launchHelp(4);
                   }else if(fragment.getClass() == GraphFragment.class){
                       launchHelp(5);
                   }
                }
                Log.d("Vestibio","Fragment is "+ fragment );
                return true;

        }


        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        fragment = null;

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottomNavigation);
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_metronome:
                bottomNavigationView.setVisibility(View.VISIBLE);
                if (selectedTab != null && selectedTab.equals(getResources().getString(R.string.session_tab))) {
                    fragment = new SessionListFragment();
                    selectedTab = "";
                } else
                    fragment = new MetronomeFragment();
                Log.d("Vestibio", " vrt metronome?");
                supportInvalidateOptionsMenu();
                break;
            case R.id.nav_disclaimer:
                Log.d("Vestibio", " vrt disclaimer?");
                bottomNavigationView.setVisibility(View.GONE);
                fragment = new DisclaimerFragment();
                invalidateOptionsMenu();
                break;
            case R.id.nav_resources:
                Log.d("Vestibio", " vrt resources");
                bottomNavigationView.setVisibility(View.GONE);
                fragment = new ResourcesFragment();
                invalidateOptionsMenu();

                break;
            case R.id.nav_donate:
                Log.d("Vestibio", " vrt donate");
                bottomNavigationView.setVisibility(View.GONE);
                startActivity(new Intent(this, DonateFragment.class));
                break;
            case R.id.nav_about:
                Log.d("Vestibio", " vrt about");
                bottomNavigationView.setVisibility(View.GONE);
                fragment = new AboutVestibioFragment();
                invalidateOptionsMenu();
                break;
            case R.id.nav_rate:
                Log.d("Vestibio", " vrt rate");
                bottomNavigationView.setVisibility(View.GONE);
                launchMarket();
                break;
            case R.id.nav_backup:
                Log.d("Vestibio", " vrt donate");
                bottomNavigationView.setVisibility(View.GONE);
                startActivity(new Intent(this, BackupActivity.class));
                break;
            default:
                Log.d("Vestibio", " vrt wrong");
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();
            Log.d("Vestibio", " vrt wrong");

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }



    public void setToolbar(Toolbar toolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_closed);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toggle.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final Menu menu = navigationView.getMenu();
        final SubMenu subMenu = menu.addSubMenu("Lists");

    }


    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private void launchHelp(int i) {
        Uri uri;

        if(4 ==i){
            uri = Uri.parse("http://www.vestibio.com/help-guide-session-details-add-edit-and-delete/");
        }else if (5==i){
            uri = Uri.parse("http://www.vestibio.com/help-guide-5-smart-graphs/");
        }
        else{
            uri = Uri.parse("http://www.vestibio.com/help-metronome-setup/");
        }
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " Unable to open help", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (null == fragment) {
            Log.d("Vestibio", "voo 1");
            displaySelectedScreen(R.id.nav_metronome);
        } else if (fragment.getClass().equals(MetronomeFragment.class)) {
            Log.d("Vestibio", "voo 2");
            displaySelectedScreen(R.id.nav_metronome);
        } else if (fragment.getClass().equals(DisclaimerFragment.class)) {
            Log.d("Vestibio", "voo 3");
            displaySelectedScreen(R.id.nav_disclaimer);
        } else if (fragment.getClass().equals(ResourcesFragment.class)) {
            Log.d("Vestibio", "voo 4");
            displaySelectedScreen(R.id.nav_resources);
        } else if (fragment.getClass().equals(AboutVestibioFragment.class)) {
            Log.d("Vestibio", "voo 5");
            displaySelectedScreen(R.id.nav_about);
        } else if (fragment.getClass().equals(DonateFragment.class)) {
            Log.d("Vestibio", "voo 6");

        }


        Log.d("Vestibio", "MainActivity in OnResume");


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(fragment != null){
            if(fragment.getClass() == MetronomeFragment.class ||fragment.getClass() == SessionListFragment.class || fragment.getClass() == GraphFragment.class){
                getMenuInflater().inflate(R.menu.menu, menu);

            }
        }
        Log.d("Vestibio", "onCreateOptionsMenu: ");

        return true;
    }
}
