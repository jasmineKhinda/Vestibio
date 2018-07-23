package com.andreyaleev.metrosong.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;

import android.widget.TextView;

import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.fragments.GraphFragment;
import com.andreyaleev.metrosong.fragments.MetronomeFragment;
import com.andreyaleev.metrosong.fragments.SessionListFragment;


import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

//    @BindView(R.id.slidingTabs)
//    SlidingTabLayout mSlidingTabLayout;
//    @BindView(R.id.viewPager)
//    CustomViewPager mViewPager;
     @BindView(R.id.bottomNavigation)
     BottomNavigationView mBottomNavigationView;
     Toolbar toolbarTop;
     View layout;


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

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //setTitle(getString(R.string.app_name_short));

        toolbarTop = (Toolbar) findViewById(R.id.toolbar);
        layout = (View) findViewById(R.id.rel);
        TextView mTitle = (TextView) toolbarTop.findViewById(R.id.toolbar_title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);



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
//        BottomNavigationView menuView = (BottomNavigationView) mBottomNavigationView.getChildAt(0);
//
//        for (int i = 0; i < menuView.getChildCount(); i++) {
//            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
//            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
//            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
//            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, displayMetrics);
//            iconView.setLayoutParams(layoutParams);
//        }

//        final TextView fragmentNameTv = (TextView)
//                findViewById(R.id);
//
//        fragmentNameTv.setText(getResources().getString(R.string.fragment_home_title));


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

        Fragment selectedFragment = MetronomeFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();



//        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//
//        mSlidingTabLayout.setDistributeEvenly(true);
//        mSlidingTabLayout.setViewPager(mViewPager);
//        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
//    }
//
//    public SlidingTabLayout getSlidingTabLayout() {
//        return mSlidingTabLayout;
//    }
//
//    public CustomViewPager getViewPager() {
//        return mViewPager;
//    }
//
//    public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//        public SectionsPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            if (position == 0)
//                return MetronomeFragment.newInstance();
//            else if (position == 1)
//                return SongsListFragment.newInstance();
//            else if (position == 2)
//                return GraphFragment.newInstance() ;
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            return 3;
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            Locale l = Locale.getDefault();
//            switch (position) {
//                case 0:
//                    return getString(R.string.title_section1).toUpperCase(l);
//                case 1:
//                    return getString(R.string.title_section2).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
//            }
//            return null;
//        }
    }

    public BottomNavigationView getBottomViewNavigation() {
        return mBottomNavigationView;
    }

    public View getViewToolbar() {
        return layout;
    }
    public SharedPreferences getSettings() {
        return getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
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

}
