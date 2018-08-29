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
import com.amagesoftware.vestibio.fragments.SessionListFragment;


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
    private String selectedTab ="";



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


        if(getIntent().getExtras() != null){
            selectedTab = getIntent().getExtras().getString("TAB");
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
        Log.d("vertigo", "onCreate:  font is"+ font);

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
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.action_metronome:
                        selectedFragment = MetronomeFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                        break;

                    case R.id.action_session:
                        selectedFragment = SessionListFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                        break;

                    case R.id.action_graph:
                        selectedFragment = GraphFragment.newInstance();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                        break;

                }
                return true;
            }
        });

        Log.d("Vestibio", " is nav bar shown?"+ hasNavBar(getResources()));
        Log.d("Vestibio", " is nav bar shown?"+ hasNavBar());


        if(selectedTab!=null && selectedTab.equals(getResources().getString(R.string.session_tab))){
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

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
//            case R.id.action_settings:
//                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        fragment = null;
        Log.d("Vestibio", " display?"+ hasNavBar());
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_metronome:
                mBottomNavigationView.setVisibility(View.VISIBLE);
                if(selectedTab!=null && selectedTab.equals(getResources().getString(R.string.session_tab))){
                    fragment = new SessionListFragment();
                    selectedTab="";
                }
                else
                    fragment = new MetronomeFragment();
                Log.d("Vestibio", " metronome?");
                break;
            case R.id.nav_disclaimer:
                Log.d("Vestibio", " disclaimer?");
                mBottomNavigationView.setVisibility(View.GONE);
                fragment = new DisclaimerFragment();
                break;
            case R.id.nav_donate:
                startActivity(new Intent(this, DonateFragment.class));
                break;
            case R.id.nav_about:
                mBottomNavigationView.setVisibility(View.GONE);
                fragment = new AboutVestibioFragment();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                break;
        }
        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment);
            ft.commit();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }




    public boolean hasNavBar (Resources resources)
    {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    public boolean hasNavBar() {
        Display d = getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        d.getRealMetrics(dm);
        int realHeight = dm.heightPixels;
        int realWidth = dm.widthPixels;
        d.getMetrics(dm);
        int displayHeight = dm.heightPixels;
        int displayWidth = dm.widthPixels;
        int h= realHeight - displayHeight;
        Log.d("Vestibio", "hasNavBar height: " + h);
        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;

    }

    public void setToolbar(Toolbar toolbar){
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
//        Realm realm = Realm.getDefaultInstance();
//        RealmResults<ListCategory> cat = realm.where(ListCategory.class).findAll();
//        for(ListCategory category: cat) {
//
//            if ((!(category.getName().trim().equalsIgnoreCase(getResources().getString(R.string.category_Inbox).trim()))) && (!(category.getName().trim().equalsIgnoreCase(getResources().getString(R.string.category_Project).trim()))))
//            {
//
//                subMenu.add(category.getName()).setIcon(R.drawable.ic_list_black_24dp);
//
//            }
//
//
//        }
    }


    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, " unable to find market app", Toast.LENGTH_LONG).show();
        }}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }
    @Override
    protected void onResume() {
        super.onResume();

        if (null == fragment){
            displaySelectedScreen(R.id.nav_metronome);
        }else if(fragment.getClass().equals(MetronomeFragment.class)){
            displaySelectedScreen(R.id.nav_metronome);
        }else if(fragment.getClass().equals(DisclaimerFragment.class)){
            displaySelectedScreen(R.id.nav_disclaimer);
        }else if(fragment.getClass().equals(AboutVestibioFragment.class)){
            displaySelectedScreen(R.id.nav_about);
        }else if(fragment.getClass().equals(DonateFragment.class)){
            launchMarket();
        }



        Log.d("GOALS", "fragment in OnResume");
//        if (true== fragment.getClass().equals(InboxMenu.class)) {
//            InboxMenu ref = (InboxMenu) fragment;
//            ref.refreshDataset();
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.replace(R.id.content_frame, fragment);
//            ft.commit();
//
//        }


    }
}
