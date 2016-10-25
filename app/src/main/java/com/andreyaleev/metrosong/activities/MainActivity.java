package com.andreyaleev.metrosong.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.andreyaleev.metrosong.Constants;
import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.fragments.MetronomeFragment;
import com.andreyaleev.metrosong.fragments.SongsListFragment;
import com.andreyaleev.metrosong.tools.slidingTab.SlidingTabLayout;
import com.andreyaleev.metrosong.tools.CustomViewPager;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.slidingTabs)
    SlidingTabLayout mSlidingTabLayout;
    @BindView(R.id.viewPager)
    CustomViewPager mViewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setTitle(getString(R.string.app_name_short));

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
    }

    public SlidingTabLayout getSlidingTabLayout() {
        return mSlidingTabLayout;
    }

    public CustomViewPager getViewPager() {
        return mViewPager;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return MetronomeFragment.newInstance();
            else if (position == 1)
                return SongsListFragment.newInstance();
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    public SharedPreferences getSettings() {
        return getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
    }


}
