package com.andreyaleev.metrosong.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andreyaleev.metrosong.R;

/**
 * Created by jkhinda on 22/06/18.
 */

public class GraphFragment extends Fragment {

    public static GraphFragment newInstance() {
        GraphFragment frag = new GraphFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);


       // populateGraphView(view);

        return view;
    }
}
