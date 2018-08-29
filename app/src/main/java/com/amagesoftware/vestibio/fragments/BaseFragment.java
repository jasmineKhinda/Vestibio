package com.amagesoftware.vestibio.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.amagesoftware.vestibio.App;
import com.amagesoftware.vestibio.activities.MainActivity;

/**
 * Created by Andrey Aleev on 11.09.2015.
 */
public class BaseFragment extends Fragment {

    protected MainActivity activity;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.getBus().register(this);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getBus().unregister(this);
    }
}
