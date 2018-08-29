package com.amagesoftware.vestibio.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.activities.SessionActivity;

import com.amagesoftware.vestibio.adapters.SessionsAdapter;
import com.amagesoftware.vestibio.db.SessionsDataSource;
import com.amagesoftware.vestibio.metronome.Session;


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
    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches







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

        if(sessions.size() ==10){

        }
    }

    public static void app_launched(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(mContext, editor);
            }
        }

        editor.commit();
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

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        final String APP_TITLE = mContext.getResources().getString(R.string.app_name);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);

        Button b1 = new Button(mContext);
        b1.setText("Rate " + APP_TITLE);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_TITLE)));
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();

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

