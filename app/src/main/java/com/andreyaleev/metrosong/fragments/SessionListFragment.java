package com.andreyaleev.metrosong.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.SessionActivity;
import com.andreyaleev.metrosong.activities.SongActivity;
import com.andreyaleev.metrosong.adapters.ProgramsAdapter;
import com.andreyaleev.metrosong.adapters.SessionsAdapter;
import com.andreyaleev.metrosong.db.SessionsDataSource;
import com.andreyaleev.metrosong.metronome.Session;
import com.andreyaleev.metrosong.metronome.Song;
import com.andreyaleev.metrosong.tools.OnBackPressedListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jkhinda on 26/06/18.
 */

public class SessionListFragment  extends BaseFragment  implements
        SessionsAdapter.SessionsItemsListener{

    @BindView(R.id.rvPrograms)
    RecyclerView rvPrograms;
    @BindView(R.id.empty_view)
    ImageView empty;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordlay;
    @BindView(R.id.empty_heading)
    TextView emptyTextHeading;
    @BindView(R.id.empty_instruction)
    TextView emptyTextInstruction;




    private SessionsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Session> sessions;

    private SessionsDataSource dataSource;






    public static SessionListFragment newInstance() {
        SessionListFragment frag = new SessionListFragment();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sessions_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLayoutManager = new LinearLayoutManager(activity);
        rvPrograms.setLayoutManager(mLayoutManager);


        rvPrograms.setOnLongClickListener(view -> {
            // TODO remove item ??? Possible ??
            return false;
        });

        dataSource = new SessionsDataSource(activity);
        dataSource.open();
        getSessions();
    }




    private void getSessions() {
        sessions = new ArrayList<>();
        dataSource.open();

        //debug
        dataSource.getAllSessions();

        sessions = dataSource.getAllSessions();
        for (Session session : sessions) {
            Log.d("Vestibio", session.toString());
        }
        mAdapter = new SessionsAdapter(activity, sessions, this);
        rvPrograms.setAdapter(mAdapter);
        if (mAdapter.getItemCount()<1) {
            rvPrograms.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            coordlay.setBackgroundColor(getResources().getColor(R.color.themeGradientLighter_background));
            emptyTextHeading.setVisibility(View.VISIBLE);
            emptyTextInstruction.setVisibility(View.VISIBLE);
        }
        else {
            rvPrograms.setVisibility(View.VISIBLE);
            empty.setVisibility(View.GONE);
            coordlay.setBackground(getResources().getDrawable(R.drawable.linear_gradient_drawable));
        }
    }



    @Override
    public void onEditClicked(Session session) {
        Intent intent = new Intent(getContext(), SessionActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(SessionActivity.SESSION_TAG, session);
        intent.putExtras(bundle);
        getContext().startActivity(intent);
    }

    @Override
    public void onResume() {
        getSessions();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }







}

