package com.andreyaleev.metrosong.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andreyaleev.metrosong.R;
import com.andreyaleev.metrosong.activities.SongActivity;
import com.andreyaleev.metrosong.db.SongsDataSource;
import com.andreyaleev.metrosong.metronome.Song;
import com.andreyaleev.metrosong.metronome.SongSnippet;
import com.andreyaleev.metrosong.tools.OnBackPressedListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import pntanasis.android.metronome.Metronome;
import pntanasis.android.metronome.MidiNotes;

/**
 * Created by Andrey Aleev on 09.09.2015.
 */
public class SongsListFragment extends MetronomableFragment implements OnBackPressedListener {

    private final static String TAG = "ProgramsListFragment";

    @BindView(R.id.rvPrograms)
    RecyclerView rvPrograms;
    @BindView(R.id.llSongPlayback)
    LinearLayout llSongPlayback;
    @BindView(R.id.btnStopCurrent)
    Button btnStopCurrent;
    @BindView(R.id.edtSnippet)
    EditText edtSnippet;
    @BindView(R.id.edtBPM)
    EditText edtBPM;
    @BindView(R.id.edtCurrentBeat)
    EditText edtCurrentBeat;
    @BindView(R.id.edtCurrentBar)
    EditText edtCurrentBar;
    @BindView(R.id.edtBarCount)
    EditText edtBarCount;
    @BindView(R.id.fabNew)
    com.github.clans.fab.FloatingActionButton fabNew;

    private ProgramsAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<Song> songs;
    private MetronomeAsyncTask metroTask;

    private Handler mHandler;
    private SongsDataSource dataSource;

    // have in mind that: http://stackoverflow.com/questions/11407943/this-handler-class-should-be-static-or-leaks-might-occur-incominghandler
    // in this case we should be fine as no delayed messages are queued
    private Handler getHandler() {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Metronome.SONG_ENDED) {
                    stopSong();
                    return;
                }
                Bundle data = msg.getData();
                if (data != null) {
//                    Log.d(TAG, "handleMessage bpm" + data);
                    Integer bpm = data.getInt(Metronome.BPM);
                    Integer currentBeat = data.getInt(Metronome.CURRENT_BEAT);
                    Integer noteValue = data.getInt(Metronome.NOTE_VALUE);
                    Integer barCount = data.getInt(Metronome.BARS_COUNT);
                    Integer beatsPerBar = data.getInt(Metronome.BEATS_PER_BAR);
                    Integer barsPassed = data.getInt(Metronome.BARS_PASSED) + 1;
                    Integer snippetNumber = data.getInt(Metronome.SNIPPET_NUMBER) + 1;
                    updateUI(snippetNumber, bpm, currentBeat, barCount, noteValue, beatsPerBar, barsPassed);
                }
            }
        };
    }

    private void updateUI(Integer snippetNumber, Integer bpm, Integer currentBeat, Integer barCount, Integer noteValue, Integer beatsPerBar, Integer barsPassed) {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.snippet_number), snippetNumber);
        edtSnippet.setText(text);

        edtCurrentBeat.setText(String.valueOf(currentBeat));
        if (currentBeat != null && currentBeat == 1) {
            edtCurrentBeat.setTextColor(getResources().getColor(R.color.accent));
        } else {
            edtCurrentBeat.setTextColor(getResources().getColor(R.color.primary_text));
        }

        edtBPM.setText(String.valueOf(bpm));
        edtCurrentBar.setText((beatsPerBar) + "/" + noteValue);
        edtBarCount.setText(barsPassed + "/" + barCount);
    }

    public static SongsListFragment newInstance() {
        SongsListFragment frag = new SongsListFragment();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_programs_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        btnStopCurrent.setOnClickListener(view1 -> stopSong());
    }

    private void stopSong() {
        if (metroTask != null && (metroTask.getStatus() == AsyncTask.Status.RUNNING)) {
            // My AsyncTask is currently doing work in doInBackground()
            metroTask.stop();
        }
        metroTask = new MetronomeAsyncTask();
        Runtime.getRuntime().gc();
        llSongPlayback.setVisibility(View.GONE);
        llSongPlayback.setKeepScreenOn(false);
        activity.getSlidingTabLayout().setVisibility(View.VISIBLE);
        activity.getViewPager().setPagingEnabled(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity.addBackPressedListenr(this);
        mLayoutManager = new LinearLayoutManager(activity);
        rvPrograms.setLayoutManager(mLayoutManager);
        rvPrograms.setOnLongClickListener(view -> {
            // TODO remove item ??? Possible ??
            return false;
        });
        fabNew.setOnClickListener(view -> startActivity(new Intent(activity, SongActivity.class)));
        dataSource = new SongsDataSource(activity);
        dataSource.open();
        getSongs();
    }


    private void getSongs() {
        songs = new ArrayList<>();
        dataSource.open();

        //debug
        dataSource.showAllSnippets();

        songs = dataSource.getAllSongs();
        for (Song song : songs) {
            Log.d(TAG, song.toString());
        }
        mAdapter = new ProgramsAdapter(activity, songs);
        rvPrograms.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        if (llSongPlayback.getVisibility() == View.VISIBLE) {
            stopSong();
        } else {
            activity.superBack();
        }
    }


    class ProgramsAdapter extends RecyclerView.Adapter<ProgramsAdapter.ViewHolder> {

        private ArrayList<Song> songs;
        private Context mContext;

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.tvTitle) TextView tvTitle;
            @BindView(R.id.tvDescription) TextView tvDescription;
            @BindView(R.id.ivPlayCard) ImageView ivPlayCard;
            @BindView(R.id.ivEditCard) ImageView ivEditCard;

            public ViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }

        public ProgramsAdapter(Context context, ArrayList<Song> songs) {
            this.songs = songs;
            this.mContext = context;
        }

        public Song getItem(int position) {
            return songs.get(position);
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }

        @Override
        public ProgramsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_program, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            final Song song = getItem(position);
            holder.tvTitle.setText(song.getTitle());
            holder.tvDescription.setText(String.format(getString(R.string.snippets), song.getSnippets().size()));
            holder.ivPlayCard.setOnClickListener(view -> {
                activity.getSlidingTabLayout().setVisibility(View.GONE);
                activity.getViewPager().setPagingEnabled(false);
                llSongPlayback.setVisibility(View.VISIBLE);
                llSongPlayback.setKeepScreenOn(true);

                metroTask = new MetronomeAsyncTask(song.getSnippets());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                } else {
                    metroTask.execute();
                }

            });
            holder.tvTitle.setOnClickListener(view -> editCard(song));
            holder.tvDescription.setOnClickListener(view -> editCard(song));
            holder.ivEditCard.setOnClickListener(view -> editCard(song));
        }

        private void editCard(Song song) {
            Intent intent = new Intent(mContext, SongActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(SongActivity.SONG_TAG, song);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }

    }

    @Override
    public void onResume() {
        getSongs();
        super.onResume();
    }

    @Override
    public void onPause() {
        dataSource.close();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (metroTask != null) {
            metroTask.stop();
        }
        Runtime.getRuntime().gc();
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, initialVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {

        Metronome metronome;
        ArrayList<SongSnippet> snippets;

        MetronomeAsyncTask(ArrayList<SongSnippet> snippets) {
            mHandler = getHandler();
            this.metronome = new Metronome(mHandler);
            this.snippets = snippets;
        }

        MetronomeAsyncTask() {
            mHandler = getHandler();
            metronome = new Metronome(mHandler);
        }

        protected String doInBackground(Void... params) {
            metronome.setBeat(beats);
            metronome.setNoteValue(noteValue);
            metronome.setBpm(bpm);
//            metronome.setBeatSound(beatSound);
//            metronome.setSound(sound);
            metronome.setBeatSound(MidiNotes.frequency(MidiNotes.A5));
            metronome.setSound(MidiNotes.frequency(MidiNotes.FisGb5));
            if (snippets == null) {
                metronome.play();
            } else {
                metronome.playProgram(snippets);
            }

            return null;
        }

        public void stop() {
            metronome.stop();
            metronome = null;
        }

        public void setBpm(short bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }

        public void setBeat(short beat) {
            if (metronome != null)
                metronome.setBeat(beat);
        }

    }
}
