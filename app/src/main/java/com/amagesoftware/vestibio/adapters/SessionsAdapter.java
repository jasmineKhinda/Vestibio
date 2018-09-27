package com.amagesoftware.vestibio.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.amagesoftware.vestibio.R;
import com.amagesoftware.vestibio.fragments.MetronomeFragment;
import com.amagesoftware.vestibio.metronome.Session;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jkhinda on 27/06/18.
 */

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private ArrayList<Session> sessions;
    private Context mContext;
    View v;
    private CardView cardview;

    private SessionsItemsListener sessionsItemsListener;

    public SessionsAdapter(Context context, ArrayList<Session> sessions, SessionsItemsListener programsItemsListener) {
        this.sessions = sessions;
        this.mContext = context;
        this.sessionsItemsListener = programsItemsListener;
    }

    public Session getItem(int position) {
        return sessions.get(position);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    @Override
    public SessionsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        SessionsAdapter.ViewHolder vh = new SessionsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(SessionsAdapter.ViewHolder holder, final int position) {
        final Session session = getItem(position);

        holder.tvTitle.setText(session.getTitle());
        holder.beatsNumber.setText(String.valueOf(session.getBpm()));
        holder.durationSession.setText(mContext.getString(R.string.duration_tag)+ MetronomeFragment.timeConversion(session.getTotalDuration()));

        long timeStampMillis = session.getTimeStamp();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy 'at' h:mma");
        Date resultdate = new Date(timeStampMillis);
        holder.date.setText(sdf.format(resultdate));

        holder.tvTitle.setOnClickListener(view -> editCard(session));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //implement onClick
                editCard(session);
            }
        });

    }

    private void editCard(Session session) {
        if(sessionsItemsListener !=null){
            sessionsItemsListener.onEditClicked(session);
            Log.d("Vestibio", "editCard: ");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.title)
        TextView tvTitle;
        @BindView(R.id.bpmTag)
        TextView bpmTag;
        @BindView(R.id.beatsNumber)
        TextView beatsNumber;
        @BindView(R.id.durationSession)
        TextView durationSession;
        @BindView(R.id.date)
        TextView date;
        @BindView(R.id.cvItem)
        CardView cardView;


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

        }
    }

    public interface SessionsItemsListener {
        void onEditClicked(Session session);
    }
}

